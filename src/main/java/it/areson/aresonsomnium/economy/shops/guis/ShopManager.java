package it.areson.aresonsomnium.economy.shops.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class ShopManager {

    private final AresonSomnium aresonSomnium;
    private final HashMap<String, CustomShop> guis;
    private final HashMap<Player, String> openedGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public ShopManager(AresonSomnium aresonSomnium, MySqlDBConnection connection, String tableName) {
        this.aresonSomnium = aresonSomnium;
        this.guis = new HashMap<>();
        this.openedGuis = new HashMap<>();
        this.mySqlDBConnection = connection;
        this.tableName = tableName;
        fetchAllFromDB();
    }

    public HashMap<String, CustomShop> getGuis() {
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

    public boolean isASavedGUI(String guiName) {
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
                openedGuis.put(player, guiName);

                openedGuis.forEach((a, b) -> System.out.println(a + " " + b));

                player.openInventory(customShop.createInventory(true));
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
