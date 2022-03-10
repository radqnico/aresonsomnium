package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public abstract class GeneralEventListener implements Listener {

    protected final AresonSomnium aresonSomnium;

    public GeneralEventListener(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
    }

    public static boolean isNotValidItemStack(ItemStack itemStack) {
        return Objects.isNull(itemStack) || Objects.equals(itemStack.getType(), Material.AIR) || Objects.equals(itemStack.getType(), Material.CAVE_AIR);
    }

    public static boolean isLeftClicking(InventoryClickEvent event) {
        return event.isLeftClick() && isNotValidItemStack(event.getCursor());
    }

    public static boolean isRightClicking(InventoryClickEvent event) {
        return event.isRightClick() && isNotValidItemStack(event.getCursor());
    }

    public static boolean isShiftClicking(InventoryClickEvent event) {
        return event.isShiftClick() && isNotValidItemStack(event.getCursor());
    }

    public static boolean isPuttingNewItem(InventoryClickEvent event) {
        return event.isLeftClick() && !isNotValidItemStack(event.getCursor());
    }

    public void registerEvents() {
        aresonSomnium.getServer().getPluginManager().registerEvents(this, aresonSomnium);
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }

}
