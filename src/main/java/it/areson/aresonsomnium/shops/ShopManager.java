package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.MessageUtils;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TreeMap;

public class ShopManager {

    private final TreeMap<String, CustomShop> guis;
    private final TreeMap<Player, String> editingGuis;
    private final TreeMap<Player, String> openedGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public ShopManager(MySqlDBConnection connection, String tableName) {
        this.guis = new TreeMap<>();
        PlayerComparator playerComparator = new PlayerComparator();
        this.editingGuis = new TreeMap<>(playerComparator);
        this.openedGuis = new TreeMap<>(playerComparator);
        this.mySqlDBConnection = connection;
        this.tableName = tableName;
        fetchAllFromDB();
    }

    public TreeMap<String, CustomShop> getGuis() {
        return guis;
    }

    public void fetchAllFromDB() {
        String query = "select guiName from " + tableName;
        String guiName = "ERRORE NON DOVUTO ALLA GUI";
        try {
            guis.clear();
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            while (resultSet.next()) {
                // Presente
                guiName = resultSet.getString("guiName");
                addFromName(guiName);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare la GUI '" + guiName + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    private void addFromName(String guiName) throws SQLException {
        guis.put(guiName, CustomShop.getFromDB(mySqlDBConnection, guiName));
    }

    public boolean isPermanent(String guiName) {
        return guis.containsKey(guiName);
    }

    public CustomShop getPermanentGui(String guiName) {
        return guis.get(guiName);
    }

    public CustomShop createNewGui(String name, String guiTitle) {
        if (guis.containsKey(name)) {
            return guis.get(name);
        }
        CustomShop customShop = new CustomShop(name, guiTitle, mySqlDBConnection);
        guis.put(name, customShop);
        return customShop;
    }

    public void beginEditGui(Player player, String guiName) {
        editingGuis.put(player, guiName);
    }

    public boolean endEditGui(Player player, Inventory inventory) {
        String guiName = editingGuis.remove(player);
        if (Objects.nonNull(guiName)) {
            CustomShop customShop = guis.get(guiName);
            customShop.updateFromInventory(inventory);
            customShop.saveToDB();
            return true;
        }
        return false;
    }

    public void openGuiToPlayer(Player player, String guiName) {
        CustomShop customShop = guis.get(guiName);
        if (Objects.nonNull(customShop)) {
            if (customShop.isShopReady()) {
                player.openInventory(customShop.createInventory());
                openedGuis.put(player, guiName);
            } else {
                player.sendMessage(MessageUtils.errorMessage("Il negozio richiesto non e' pronto. Segnala il problema allo staff."));
            }
        } else {
            player.sendMessage(MessageUtils.errorMessage("L'interfaccia '" + guiName + "' non esiste"));
        }
    }

    public void playerCloseGui(Player player) {
        openedGuis.remove(player);
    }

    public boolean isViewingCustomGui(Player player) {
        return openedGuis.containsKey(player);
    }

    public CustomShop getViewingCustomShop(Player player) {
        if (isViewingCustomGui(player)) {
            return guis.get(openedGuis.get(player));
        }
        return null;
    }

    public boolean isEditingCustomGui(Player player) {
        return editingGuis.containsKey(player);
    }
}
