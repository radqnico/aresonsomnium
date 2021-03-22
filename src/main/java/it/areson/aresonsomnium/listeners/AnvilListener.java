package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class AnvilListener extends GeneralEventListener {

    public AnvilListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onAnvilUpdate(PrepareAnvilEvent event) {

        System.out.println(event.getInventory().toString());

//        ItemStack itemRenamed = event.getResult();
//        if (itemRenamed != null) {
//            Material material = itemRenamed.getType();
//            if (material != Material.PAPER && material != Material.TRIPWIRE_HOOK) {
//                ItemMeta itemMeta = itemRenamed.getItemMeta();
//                if (itemMeta != null) {
//                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemMeta.getDisplayName()));
//                    itemRenamed.setItemMeta(itemMeta);
//                }
//                event.setResult(itemRenamed);
//            } else {
//                InventoryView view = event.getView();
//                view.close();
//                view.getPlayer().sendMessage(net.md_5.bungee.api.ChatColor.BLUE + "[Areson] " + ChatColor.RED + "Non è possibile rinominare quest'oggetto");
//                event.setResult(airStack);
//            }
//        }
    }

}
