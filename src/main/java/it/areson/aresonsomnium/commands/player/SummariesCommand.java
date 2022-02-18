package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.commands.shapes.RegisteredCommand;
import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;


@SuppressWarnings("NullableProblems")
public class SummariesCommand extends RegisteredCommand {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private HashMap<String, ItemStack> books;

    public SummariesCommand(AresonSomnium aresonSomnium, String command) {
        super(aresonSomnium, command);
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        initializeBooks();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player player) {
            if (arguments.length > 0) {
                ItemStack book = books.get(arguments[0]);
                if (book != null) {
                    player.openBook(book);
                } else {
                    messageManager.sendErrorMessage(commandSender, "Libro non trovato");
                }
            }
        }
        return true;
    }

    public void initializeBooks() {
        books = new HashMap<>();
        ConfigurationSection summaries = aresonSomnium.getSummariesFileManager().getYamlConfiguration().getConfigurationSection("summaries");
        if (summaries != null) {
            for (String key : summaries.getKeys(false)) {
                String title = summaries.getString(key + ".title");
                if (title == null) {
                    aresonSomnium.getLogger().severe("Title not found for summary book with id " + key);
                    return;
                }
                String description = summaries.getString(key + ".description");
                if (description == null) {
                    aresonSomnium.getLogger().severe("Description not found for summary book with id " + key);
                    return;
                }
                books.put(key, buildBook(title, description));
            }
        }
    }

    private ItemStack buildBook(String title, String description) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setTitle(title);
        bookMeta.setAuthor("Areson");

        ArrayList<String> pages = buildBookPages(description);
        for (String page : pages) {
            bookMeta.addPages(Component.text(ChatColor.translateAlternateColorCodes('&', page)));
        }

        book.setItemMeta(bookMeta);
        return book;
    }

    private ArrayList<String> buildBookPages(String content) {
        int CHARACTERS_PER_PAGE = 256;
        ArrayList<String> pages = new ArrayList<>();

        String[] words = content.split(" ");
        StringBuilder pageContent = new StringBuilder();
        for (String word : words) {
            if (pageContent.length() + word.length() < CHARACTERS_PER_PAGE) {
                pageContent.append(word).append(" ");
            } else {
                pages.add(pageContent.toString());
                pageContent = new StringBuilder(word + " ");
            }
        }
        pages.add(pageContent.toString());

        return pages;
    }

}
