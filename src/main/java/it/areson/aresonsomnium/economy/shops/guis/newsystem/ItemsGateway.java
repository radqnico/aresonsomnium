package it.areson.aresonsomnium.economy.shops.guis.newsystem;

import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ItemsGateway {

    private MySqlDBConnection mySqlDBConnection;
    private String itemsTableName;
    // id -> item
    private HashMap<Integer, ShopItem> cache;

    public ItemsGateway(MySqlDBConnection mySqlDBConnection, String itemsTableName) {
        this.mySqlDBConnection = mySqlDBConnection;
        this.itemsTableName = itemsTableName;
        cache = new HashMap<>();
    }

    public List<ShopItem> getAllItems(boolean invalidateCache) {
        String query = "SELECT * FROM " + itemsTableName;
        List<ShopItem> shopItems = new ArrayList<>();
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (invalidateCache) {
                cache.clear();
            }
            while (resultSet.next()) {
                ShopItem shopItem = readShopItemFromRecord(resultSet);
                shopItems.add(shopItem);
                cache.putIfAbsent(shopItem.getId(), shopItem);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return shopItems;
    }

    public Optional<ShopItem> getItemById(int itemId) {
        ShopItem cached = cache.get(itemId);
        if (Objects.nonNull(cached)) {
            return Optional.of(cached);
        }

        String query = "SELECT * FROM " + itemsTableName + "WHERE id=" + itemId;
        Optional<ShopItem> optionalShopItem = Optional.empty();
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            while (resultSet.next()) {
                ShopItem shopItem = readShopItemFromRecord(resultSet);
                optionalShopItem = Optional.of(shopItem);
                cache.putIfAbsent(shopItem.getId(), shopItem);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return optionalShopItem;
    }

    public boolean removeItem(int id) {
        String query = "DELETE FROM items WHERE id = " + id;
        try {
            Connection connection = mySqlDBConnection.connect();
            int affectedRows = mySqlDBConnection.update(connection, query);
            connection.close();
            if (affectedRows > 0) {
                cache.remove(id);
                return true;
            }
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    public boolean insertItem(ShopItem shopItem) {
        String query = "INSERT INTO " +
                "aresonSomnium.items(id, itemStack, amount, shoppingCoins, shoppingObols, shoppingGems, sellingCoins, sellingObols, sellingGems)" +
                "VALUES (generated, %s, %d, %d, %d, %d, %d, %d, %d)";
        String formatted = String.format(query,
                Base64.getEncoder().encodeToString(shopItem.getItemStack().serializeAsBytes()),
                shopItem.getAmount(),
                shopItem.getShoppingPrice().getCoins(),
                shopItem.getShoppingPrice().getObols(),
                shopItem.getShoppingPrice().getGems(),
                shopItem.getSellingPrice().getCoins(),
                shopItem.getSellingPrice().getObols(),
                shopItem.getSellingPrice().getGems()
        );
        try {
            Connection connection = mySqlDBConnection.connect();
            int affectedRows = mySqlDBConnection.update(connection, formatted);
            connection.close();
            if (affectedRows > 0) {
                cache.putIfAbsent(shopItem.getId(), shopItem);
                return true;
            }
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    private ShopItem readShopItemFromRecord(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int amount = resultSet.getInt("amount");
        String itemStack64 = resultSet.getString("itemStack");
        byte[] itemStackBytes = Base64.getDecoder().decode(itemStack64);
        long shoppingCoins = resultSet.getLong("shoppingCoins");
        long shoppingObols = resultSet.getLong("shoppingObols");
        long shoppingGems = resultSet.getLong("shoppingGems");
        long sellingCoins = resultSet.getLong("sellingCoins");
        long sellingObols = resultSet.getLong("sellingObols");
        long sellingGems = resultSet.getLong("sellingGems");
        Price shoppingPrice = new Price(shoppingCoins, shoppingObols, shoppingGems);
        Price sellingPrice = new Price(sellingCoins, sellingObols, sellingGems);
        return new ShopItem(id, ItemStack.deserializeBytes(itemStackBytes), amount, shoppingPrice, sellingPrice);
    }
}
