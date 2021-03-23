package it.areson.aresonsomnium.economy.shops.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TreeMap;

public class ShopManager {

    private final AresonSomnium aresonSomnium;
    private final TreeMap<String, CustomShop> guis;
    private final TreeMap<Player, String> openedGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public ShopManager(AresonSomnium aresonSomnium, MySqlDBConnection connection, String tableName) {
        this.aresonSomnium = aresonSomnium;
        this.guis = new TreeMap<>();
        PlayerComparator playerComparator = new PlayerComparator();
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
            mySqlDBConnection.getDebugger().debugError("Impossibile connettersi per recuperare la GUI '" + guiName + "'");
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

    public void openShopToPlayer(Player player, String guiName) {
        CustomShop customShop = guis.get(guiName);
        if (Objects.nonNull(customShop)) {
            if (customShop.isShopReady()) {
                player.openInventory(customShop.createInventory(true));
                openedGuis.put(player, guiName);
            } else {
                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("shop-not-ready"));
            }
        } else {
            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("shop-not-ready"));
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


}
