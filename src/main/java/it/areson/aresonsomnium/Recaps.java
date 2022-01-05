package it.areson.aresonsomnium;

import it.areson.aresonlib.file.FileManager;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.Set;

public class Recaps {
    private static HashMap<Integer, ItemStack> recaps;

    public static void initRecaps(FileManager recapFile) {
        recaps = new HashMap<>();
        YamlConfiguration recapConfig = recapFile.getYamlConfiguration();
        if (!recapConfig.isConfigurationSection("recaps")) {
            AresonSomniumAPI.instance.getLogger().severe("Recaps not valid.");
            return;
        }
        ConfigurationSection recapsSection = recapConfig.getConfigurationSection("recaps");
        @SuppressWarnings("ConstantConditions") Set<String> keys = recapsSection.getKeys(false);
        int index = 1;
        for (String key : keys) {
            String title = recapsSection.getString("title", "");
            String content = recapsSection.getString("content", "");
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            bookMeta = bookMeta
                    .author(Component.text("§6Areson"))
                    .title(Component.text(ChatColor.translateAlternateColorCodes('&', title)));

            int nPages = content.length() / 256;
            Component[] pages = new Component[nPages];
            for (int i = 0; i < nPages; i++) {
                pages[i] = Component.text(content.substring(i * 256, i * 256 + 256));
            }

            bookMeta.addPages(pages);
            book.setItemMeta(bookMeta);
            recaps.put(index, book.clone());
            index++;
        }
    }

    public static void openRecapToPlayer(Player player, int recap) {
        player.openBook(recaps.get(recap));
    }
}
