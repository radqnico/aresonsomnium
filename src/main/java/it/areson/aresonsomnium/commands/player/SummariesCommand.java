package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.commands.shapes.RegisteredCommand;
import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;

import static org.bukkit.ChatColor.*;


@SuppressWarnings("NullableProblems")
public class SummariesCommand extends RegisteredCommand {

    private final AresonSomnium aresonSomnium;
    private HashMap<Integer, ItemStack> books;

    public SummariesCommand(AresonSomnium aresonSomnium, String command) {
        super(aresonSomnium, command);
        this.aresonSomnium = aresonSomnium;
        initializeBooks();
    }

    //TODO
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player player) {
            if (arguments.length == 1) {
                try {
                    int briefNumber = Integer.parseInt(arguments[0]);
                    //player.openBook(builder.getWrittenBook());
                } catch (NumberFormatException exception) {
                    player.sendMessage("Errore. Devi inserire il numero di riassunto. '" + arguments[0] + "' non è un numero.");
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
                String description = summaries.getString(key + ".description");
                String totalContent = RED + "" + BOLD + title + "\n\n" + BLACK + description;
                books.put(key, buildBook(totalContent));
            }
        }
    }

    public ItemStack buildBook(String content) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();


        book.setItemMeta(bookMeta);
        return book;
    }

}
