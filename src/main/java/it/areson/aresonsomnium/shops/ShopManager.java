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

    private final TreeMap<String, CustomShop> shops;
    private final TreeMap<Player, String> editingShops;
    private final TreeMap<Player, String> openedShops;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public ShopManager(MySqlDBConnection connection, String tableName) {
        this.shops = new TreeMap<>();
        PlayerComparator playerComparator = new PlayerComparator();
        this.editingShops = new TreeMap<>(playerComparator);
        this.openedShops = new TreeMap<>(playerComparator);
        this.mySqlDBConnection = connection;
        this.tableName = tableName;
        fetchAllFromDB();
    }

    public TreeMap<String, CustomShop> getShops() {
        return shops;
    }

    public void fetchAllFromDB() {
        String query = "select guiName from " + tableName;
        String guiName = "ERRORE NON DOVUTO ALLA GUI";
        try {
            shops.clear();
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
        shops.put(guiName, CustomShop.getFromDB(mySqlDBConnection, guiName));
    }

    public boolean isShop(String guiName) {
        return shops.containsKey(guiName);
    }

    public CustomShop getShop(String guiName) {
        return shops.get(guiName);
    }

    public CustomShop createNewGui(String name, String guiTitle) {
        if (shops.containsKey(name)) {
            return shops.get(name);
        }
        CustomShop customShop = new CustomShop(name, guiTitle, mySqlDBConnection);
        shops.put(name, customShop);
        return customShop;
    }

    public void beginEditGui(Player player, String guiName) {
        editingShops.put(player, guiName);
    }

    public boolean endEditGui(Player player, Inventory inventory) {
        String guiName = editingShops.remove(player);
        if (Objects.nonNull(guiName)) {
            CustomShop customShop = shops.get(guiName);
            customShop.updateFromInventory(inventory);
            customShop.saveToDB();
            return true;
        }
        return false;
    }

    public void openGuiToPlayer(Player player, String guiName) {
        CustomShop customShop = shops.get(guiName);
        if (Objects.nonNull(customShop)) {
            if (customShop.isShopReady()) {
                player.openInventory(customShop.createInventory());
                openedShops.put(player, guiName);
            } else {
                player.sendMessage(MessageUtils.errorMessage("Il negozio richiesto non e' pronto. Segnala il problema allo staff."));
            }
        } else {
            player.sendMessage(MessageUtils.errorMessage("L'interfaccia '" + guiName + "' non esiste"));
        }
    }

    public void playerCloseGui(Player player) {
        openedShops.remove(player);
    }

    public boolean isViewingCustomGui(Player player) {
        return openedShops.containsKey(player);
    }

    public CustomShop getViewingCustomShop(Player player) {
        if (isViewingCustomGui(player)) {
            return shops.get(openedShops.get(player));
        }
        return null;
    }

    public CustomShop getEditingCustomShop(Player player){
        if(isEditingCustomGui(player)){
            return shops.get(editingShops.get(player));
        }
        return null;
    }

    public boolean isEditingCustomGui(Player player) {
        return editingShops.containsKey(player);
    }
}
