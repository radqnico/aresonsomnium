package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@SuppressWarnings("FieldCanBeLocal")
public class BlockBreakListener extends GeneralEventListener {

    public BlockBreakListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.isCancelled()) {
            Collection<ItemStack> drops = e.getBlock().getDrops();
            boolean shouldDestroy = false;
            for (ItemStack itemStack : drops) {
                if (!e.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                    e.setCancelled(true);
                    return;
                } else {
                    shouldDestroy = true;
                }
            }
            if (shouldDestroy) {
                e.getBlock().setType(Material.AIR);
            }
        }
    }

}
