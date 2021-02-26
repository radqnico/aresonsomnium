package it.areson.aresonsomnium.shops.guis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.shops.items.SerializedShopItem;
import it.areson.aresonsomnium.shops.items.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;

public class CustomShop extends MySQLObject {

    private final String name;
    private final TreeMap<Integer, ShopItem> items;
    private String title;

    public CustomShop(String name, String title, MySqlDBConnection mySqlDBConnection) {
        super(mySqlDBConnection, GUIS_TABLE_NAME);
        this.title = title;
        this.name = name;
        this.items = new TreeMap<>();
    }

    public static CustomShop getFromDB(MySqlDBConnection connection, String name) {
        CustomShop customShop = new CustomShop(name, null, connection);
        if (customShop.updateFromDB()) {
            return customShop;
        } else {
            return null;
        }
    }

    public TreeMap<Integer, ShopItem> getItems() {
        return items;
    }

    public Inventory createInventory(boolean isShopping) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));
        for (Map.Entry<Integer, ShopItem> entry : items.entrySet()) {
            Integer key = entry.getKey();
            ShopItem shopItem = entry.getValue();
            ItemStack itemStack = new ItemStack(shopItem.getItemStack()).clone();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                List<String> lore = itemMeta.getLore();
                if (Objects.nonNull(lore)) {
                    lore.add("");
                } else {
                    lore = new ArrayList<>();
                    lore.add("");
                }
                if (shopItem.getShoppingPrice().isPriceReady()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "Prezzo di &lacquisto:"));
                    lore.addAll(shopItem.getShoppingPrice().toLore());
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cNon vendibile"));
                }
                if (shopItem.getSellingPrice().isPriceReady()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "Prezzo di &lvendita:"));
                    lore.addAll(shopItem.getSellingPrice().toLore());
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cNon acquistabile"));
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
            }
            inventory.setItem(key, itemStack);
        }
        if (isShopping) {
            ItemStack itemStack = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName("");
            }
            itemStack.setItemMeta(itemMeta);
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || Material.AIR.equals(item.getType())) {
                    inventory.setItem(i, itemStack);
                }
            }
        }

        return inventory;
    }

    @Override
    public void saveToDB() {
        String saveQuery = getSaveQuery();

        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, saveQuery);
            if (update >= 0) {
                mySqlDBConnection.getDebugger().debugSuccess("Aggiornata GUI '" + name + "' sul DB.");
            } else {
                mySqlDBConnection.getDebugger().debugWarning("GUI '\" + name + \"' NON aggiornata sul DB.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getDebugger().debugError("Impossibile connettersi per aggiornare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public Map<Integer, SerializedShopItem> getSerializedShopItems() {
        return items.entrySet().parallelStream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toSerializedShopItem()
        ));
    }

    public String getSaveQuery() {
        Gson gson = new Gson();
        String itemsJson = gson.toJson(getSerializedShopItems());
        return String.format("INSERT INTO %s (guiName, guiTitle, shopItems) " +
                        "values ('%s', '%s', '%s') ON DUPLICATE KEY " +
                        "UPDATE guiTitle='%s', shopItems='%s'",
                tableName,
                name, title, itemsJson,
                title, itemsJson
        );
    }

    @Override
    public boolean updateFromDB() {
        String query = "select * from " + GUIS_TABLE_NAME + " where guiName='" + name + "'";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (resultSet.next()) {
                // Presente
                setFromResultSet(resultSet);
                mySqlDBConnection.getDebugger().debugSuccess("Dati GUI '" + name + "' recuperati dal DB");
                return true;
            } else {
                // Non presente
                mySqlDBConnection.getDebugger().debugWarning("GUI '" + name + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getDebugger().debugError("Impossibile connettersi per recuperare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    private void setFromResultSet(ResultSet resultSet) throws SQLException {
        Gson gson = new Gson();
        this.items.clear();
        Type type = new TypeToken<HashMap<Integer, SerializedShopItem>>() {
        }.getType();
        Map<Integer, SerializedShopItem> serializedShopItemMap = gson.fromJson(resultSet.getString("shopItems"), type);
        this.items.putAll(serializedShopItemMap.entrySet().parallelStream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toShopItem()
        )));
        this.title = resultSet.getString("guiTitle");
    }

    public boolean isShopReady() {
        return items.values().stream().allMatch(value -> value.getShoppingPrice().isPriceReady());
    }

    @Override
    public String toString() {
        return "CustomShop{name=" + name + ",items:" + Arrays.toString(items.values().stream().map(shopItem -> shopItem.getItemStack().getType().name()).toArray()) + "}";
    }
}
