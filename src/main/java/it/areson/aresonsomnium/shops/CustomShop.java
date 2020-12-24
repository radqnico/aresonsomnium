package it.areson.aresonsomnium.shops;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
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

    public static String tableQuery = "create table if not exists %s (" +
            "guiName varchar(255) not null primary key,\n" +
            "guiTitle varchar(255) not null,\n" +
            "guiObjects text not null,\n" +
            "prices text not null\n" +
            ");";

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

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));
        for (Map.Entry<Integer, ShopItem> entry : items.entrySet()) {
            Integer key = entry.getKey();
            ItemStack value = entry.getValue().getItemStack();
            ItemMeta itemMeta = value.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                List<String> lore = itemMeta.getLore();
                if (Objects.nonNull(lore)) {
                    lore.add("\n" + ChatColor.GOLD + ChatColor.BOLD + "" + entry.getValue().getPrice() + " Basic Coins");
                } else {
                    lore = new ArrayList<>();
                    lore.add("\n" + ChatColor.GOLD + ChatColor.BOLD + "" + entry.getValue().getPrice() + " Basic Coins");
                }
                itemMeta.setLore(lore);
                value.setItemMeta(itemMeta);
            }
            inventory.setItem(key, value);
        }
        return inventory;
    }

    public void updateFromInventory(Inventory inventory) {
        items.clear();
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if (Objects.nonNull(item) && !item.getType().equals(Material.AIR)) {
                items.put(i, new ShopItem(item, -1));
            }
        }
    }

    @Override
    public void saveToDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String saveQuery = getSaveQuery();
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, saveQuery);
            if (update >= 0) {
                mySqlDBConnection.getLogger().info("Aggiornata GUI '" + name + "' sul DB.");
            } else {
                mySqlDBConnection.getLogger().warning("GUI '\" + name + \"' NON aggiornata sul DB.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per aggiornare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public String getSaveQuery() {
        Gson gson = new Gson();
        Map<String, String> serializedItems = items.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> Base64.getEncoder().encodeToString(e.getValue().getItemStack().serializeAsBytes())
        ));
        Map<String, String> serializedPrices = items.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().getPrice() + "")
        );
        String itemsJson = gson.toJson(serializedItems);
        String pricesJson = gson.toJson(serializedPrices);
        return String.format("INSERT INTO %s (guiName, guiTitle, guiItems, prices) " +
                        "values ('%s', '%s', '%s', '%s') ON DUPLICATE KEY " +
                        "UPDATE guiTitle='%s', guiItems='%s', prices='%s'",
                tableName,
                name, title, itemsJson, pricesJson,
                title, itemsJson, pricesJson
        );
    }

    @Override
    public boolean updateFromDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String query = "select * from somniumGuis where guiName='" + name + "'";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (resultSet.next()) {
                // Presente
                setFromResultSet(resultSet);
                mySqlDBConnection.getLogger().info("Dati GUI '" + name + "' recuperati dal DB");
                return true;
            } else {
                // Non presente
                mySqlDBConnection.getLogger().warning("GUI '" + name + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    private void setFromResultSet(ResultSet resultSet) throws SQLException {
        this.title = resultSet.getString("guiTitle");
        this.items.clear();
        String guiItems = resultSet.getString("guiItems");
        String prices = resultSet.getString("prices");

        setItems(guiItems);
        setPrices(prices);
    }

    public Float getPriceOfSlot(int slot) {
        return items.get(slot).getPrice();
    }

    public String getPricesJSON() {
        Gson gson = new Gson();
        Map<String, String> serializedPrices = items.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().getPrice() + "")
        );
        return gson.toJson(serializedPrices);
    }

    public String getIndexAndNameJSON() {
        Gson gson = new Gson();
        Map<String, String> serializedPrices = items.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().getItemStack().getItemMeta().getDisplayName().equals("") ?
                        e.getValue().getItemStack().getType().name() :
                        e.getValue().getItemStack().getItemMeta().getDisplayName())
        );
        return gson.toJson(serializedPrices);
    }

    public boolean isShopReady() {
        return items.values().stream().noneMatch(value -> value.getPrice() < 0);
    }

    public void setPrices(String pricesJson) {
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Gson gson = new Gson();
        HashMap<String, String> serializedPrices = gson.fromJson(pricesJson, type);

        for (Map.Entry<String, String> entry : serializedPrices.entrySet()) {
            try {
                int key = Integer.parseInt(entry.getKey());
                ShopItem shopItem = items.get(key);
                try {
                    float price = Float.parseFloat(entry.getValue());
                    if (Objects.nonNull(shopItem)) {
                        shopItem.setPrice(price);
                    } else {
                        Bukkit.getLogger().warning("Prezzo non corrispondente a nessun oggetto nello slot '" + key + "' : " + entry.toString());
                    }
                } catch (NumberFormatException exception) {
                    shopItem.setPrice(-1);
                    Bukkit.getLogger().severe("Prezzo invalido trovato nella GUI '" + title + "' : " + entry.toString());
                }
            } catch (NumberFormatException exception) {
                Bukkit.getLogger().severe("Chiave prezzo invalida nello shop '" + title + "' : " + entry.toString());
                exception.printStackTrace();
            }
        }
    }

    public void setItems(String itemsJson) {
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Gson gson = new Gson();
        HashMap<String, String> serializedItems = gson.fromJson(itemsJson, type);

        for (Map.Entry<String, String> entry : serializedItems.entrySet()) {
            try {
                items.put(
                        Integer.parseInt(entry.getKey()),
                        new ShopItem(ItemStack.deserializeBytes(Base64.getDecoder().decode(entry.getValue())), -1)
                );
            } catch (Exception exception) {
                Bukkit.getLogger().severe("Oggetto invalido trovato nella GUI '" + title + "' : " + entry.toString());
                exception.printStackTrace();
            }
        }
    }
}
