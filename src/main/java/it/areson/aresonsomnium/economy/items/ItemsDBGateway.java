package it.areson.aresonsomnium.economy.items;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Price;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static it.areson.aresonsomnium.Constants.DB_ITEMS_TABLE;

public class ItemsDBGateway {

    private final AresonSomnium aresonSomnium;
    private final MySqlDBConnection mySqlDBConnection;
    // id -> item
    private final HashMap<Integer, ShopItem> cache;

    public ItemsDBGateway(AresonSomnium aresonSomnium, MySqlDBConnection mySqlDBConnection) {
        this.aresonSomnium = aresonSomnium;
        this.mySqlDBConnection = mySqlDBConnection;
        cache = new HashMap<>();
    }

    public List<ShopItem> getAllItems(boolean invalidateCache) {
        String query = "SELECT * FROM " + DB_ITEMS_TABLE;
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
        if (itemId == -1) {
            return Optional.empty();
        }

        ShopItem cached = cache.get(itemId);
        if (Objects.nonNull(cached)) {
            return Optional.of(cached);
        }

        String query = "SELECT * FROM " + DB_ITEMS_TABLE + " WHERE id=" + itemId;
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

    public void removeItem(int id) {
        String query = "DELETE FROM " + DB_ITEMS_TABLE + " WHERE id = " + id;
        try {
            Connection connection = mySqlDBConnection.connect();
            int affectedRows = mySqlDBConnection.update(connection, query);
            connection.close();
            if (affectedRows > 0) {
                cache.remove(id);
            }
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public void upsertShopItem(ShopItem shopItem) {
        String query;
        String formatted;
        if (shopItem.getId() == -1) {
            query = "INSERT INTO " + DB_ITEMS_TABLE +
                    "(itemStack, amount, shoppingCoins, shoppingObols, shoppingGems, sellingCoins, sellingObols, sellingGems) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";
            formatted = String.format(query,
                    Base64.getEncoder().encodeToString(shopItem.getItemStack(false, false).serializeAsBytes()),
                    shopItem.getAmount(),
                    shopItem.getShoppingPrice().getCoins().toPlainString(),
                    shopItem.getShoppingPrice().getObols().toString(),
                    shopItem.getShoppingPrice().getGems().toString(),
                    shopItem.getSellingPrice().getCoins().toPlainString(),
                    shopItem.getSellingPrice().getObols().toString(),
                    shopItem.getSellingPrice().getGems().toString()
            );
        } else {
            query = "UPDATE " + DB_ITEMS_TABLE + " " +
                    "SET itemStack='%s', amount='%s', shoppingCoins='%s', shoppingObols='%s', shoppingGems='%s', sellingCoins='%s', sellingObols='%s', sellingGems='%s' " +
                    "WHERE id=%d";
            formatted = String.format(query,
                    Base64.getEncoder().encodeToString(shopItem.getItemStack(false, false).serializeAsBytes()),
                    shopItem.getAmount(),
                    shopItem.getShoppingPrice().getCoins().toPlainString(),
                    shopItem.getShoppingPrice().getObols().toString(),
                    shopItem.getShoppingPrice().getGems().toString(),
                    shopItem.getSellingPrice().getCoins().toPlainString(),
                    shopItem.getSellingPrice().getObols().toString(),
                    shopItem.getSellingPrice().getGems().toString(),
                    shopItem.getId()
            );
        }
        try {
            Connection connection = mySqlDBConnection.connect();
            mySqlDBConnection.update(connection, formatted);
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public Optional<ShopItem> getShopItemByMaterialAmount(Material material, int amount) {
        if (cache.isEmpty()) {
            getAllItems(true);
        }
        return cache.values().parallelStream().filter(shopItem -> {
            ItemStack itemStack = shopItem.getItemStack(false, false);
            return itemStack.getAmount() == amount && itemStack.getType().equals(material);
        }).findFirst();
    }

    private ShopItem readShopItemFromRecord(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int amount = resultSet.getInt("amount");
        String itemStack64 = resultSet.getString("itemStack");
        byte[] itemStackBytes = Base64.getDecoder().decode(itemStack64);
        BigDecimal shoppingCoins = new BigDecimal(resultSet.getString("shoppingCoins"));
        BigInteger shoppingObols = new BigInteger(resultSet.getString("shoppingObols"));
        BigInteger shoppingGems = new BigInteger(resultSet.getString("shoppingGems"));
        BigDecimal sellingCoins = new BigDecimal(resultSet.getString("sellingCoins"));
        BigInteger sellingObols = new BigInteger(resultSet.getString("sellingObols"));
        BigInteger sellingGems = new BigInteger(resultSet.getString("sellingGems"));
        Price shoppingPrice = new Price(shoppingCoins, shoppingObols, shoppingGems);
        Price sellingPrice = new Price(sellingCoins, sellingObols, sellingGems);
        return new ShopItem(aresonSomnium, id, ItemStack.deserializeBytes(itemStackBytes), amount, shoppingPrice, sellingPrice);
    }
}
