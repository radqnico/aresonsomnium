package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.Pair;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MoveShopItemAction {

    private Pair<Inventory, Integer> source;
    private Pair<Inventory, Integer> destination;

    public MoveShopItemAction() {
        source = null;
        destination = null;
    }

    public MoveShopItemAction setSource(Pair<Inventory, Integer> source) {
        this.source = source;
        return this;
    }

    public MoveShopItemAction setDestination(Pair<Inventory, Integer> destination) {
        this.destination = destination;
        return this;
    }

    public ActionType getActionType() {
        if (InventoryType.PLAYER.equals(source.left().getType()) && InventoryType.CHEST.equals(destination.left().getType())) {
            return ActionType.ADD_NEW_TO_SHOP;
        }
        if (InventoryType.CHEST.equals(source.left().getType()) && InventoryType.PLAYER.equals(destination.left().getType())) {
            return ActionType.REMOVE_FROM_SHOP;
        }
        if (InventoryType.CHEST.equals(source.left().getType()) && InventoryType.CHEST.equals(destination.left().getType())) {
            return ActionType.MOVE_IN_SHOP;
        }
        if (InventoryType.PLAYER.equals(source.left().getType()) && InventoryType.PLAYER.equals(destination.left().getType())) {
            return ActionType.MOVE_IN_PLAYER;
        }
        return ActionType.INVALID;
    }

    public void executeIfValid(CustomShop customShop) {
        switch (getActionType()) {
            case REMOVE_FROM_SHOP:
                customShop.getItems().remove(source.right());
                break;
            case ADD_NEW_TO_SHOP:
                Inventory sourceInv = source.left();
                ItemStack item = sourceInv.getItem(source.right());
                customShop.getItems().put(destination.right(), new ShopItem(item));
                break;
            case MOVE_IN_SHOP:
                ShopItem moved = customShop.getItems().remove(source.right());
                customShop.getItems().put(destination.right(), moved);
                break;
            case INVALID:
                break;
        }
    }

    @Override
    public String toString() {
        return "MoveShopItemAction{source=" + source.toString() + ",destination=" + destination.toString() + "}";
    }

    public enum ActionType {
        REMOVE_FROM_SHOP,
        ADD_NEW_TO_SHOP,
        MOVE_IN_SHOP,
        MOVE_IN_PLAYER,
        INVALID
    }
}
