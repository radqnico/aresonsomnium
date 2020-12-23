package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.MessageUtils;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;

public class GuiManager {

    private final TreeMap<String, CustomGUI> guis;
    private final TreeMap<Player, String> editingGuis;
    private final TreeMap<Player, String> openedGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public GuiManager(MySqlDBConnection connection, String tableName) {
        this.guis = new TreeMap<>();
        PlayerComparator playerComparator = new PlayerComparator();
        this.editingGuis = new TreeMap<>(playerComparator);
        this.openedGuis = new TreeMap<>(playerComparator);
        this.mySqlDBConnection = connection;
        this.tableName = tableName;
        fetchAllFromDB();
    }

    public TreeMap<String, CustomGUI> getGuis() {
        return guis;
    }

    public void fetchAllFromDB() {
        String query = "select guiName from " + tableName;
        String guiName = "ERRORE NON DOVUTO ALLA GUI";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            while (resultSet.next()) {
                // Presente
                guiName = resultSet.getString("guiName");
                addFromResultSet(guiName, resultSet);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare la GUI '" + guiName + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    private void addFromResultSet(String guiName, ResultSet resultSet) throws SQLException {
        guis.put(guiName, CustomGUI.getFromDB(mySqlDBConnection, guiName));
    }

    public boolean isPermanent(String guiName) {
        return guis.containsKey(guiName);
    }

    public CustomGUI getPermanentGui(String guiName) {
        return guis.get(guiName);
    }

    public CustomGUI createNewGui(String name, String guiTitle) {
        if (guis.containsKey(name)) {
            return guis.get(name);
        }
        CustomGUI customGUI = new CustomGUI(name, guiTitle, mySqlDBConnection);
        guis.put(name, customGUI);
        return customGUI;
    }

    public void beginEditGui(Player player, String guiName) {
        editingGuis.put(player, guiName);
    }

    public boolean endEditGui(Player player, Inventory inventory) {
        String guiName = editingGuis.remove(player);
        if (Objects.nonNull(guiName)) {
            CustomGUI customGUI = guis.get(guiName);
            customGUI.updateFromInventory(inventory);
            customGUI.saveToDB();
            return true;
        }
        return false;
    }

    public void openGuiToPlayer(Player player, String guiName) {
        CustomGUI customGUI = guis.get(guiName);
        if (Objects.nonNull(customGUI)) {
            player.openInventory(customGUI.createInventory());
            openedGuis.put(player, guiName);
        } else {
            player.sendMessage(MessageUtils.errorMessage("L'interfaccia '" + guiName + "' non esiste"));
        }
    }

    public void playerCloseGui(Player player) {
        openedGuis.remove(player);
    }

    public boolean isViewingCustomGui(Player player) {
        System.out.println(Arrays.toString(openedGuis.descendingKeySet().toArray(new Player[0])));
        return openedGuis.containsKey(player);
    }

    public boolean isEditingCustomGui(Player player) {
        return editingGuis.containsKey(player);
    }
}
