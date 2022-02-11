package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.commands.shapes.RegisteredCommand;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.books.BookBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@SuppressWarnings("NullableProblems")
public class SummariesCommand extends RegisteredCommand {

    private final AresonSomnium aresonSomnium;

    public SummariesCommand(AresonSomnium aresonSomnium, String command) {
        super(aresonSomnium, command);
        this.aresonSomnium = aresonSomnium;
    }

    //TODO
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player player) {
            if (arguments.length == 1) {
                try {
                    int briefNumber = Integer.parseInt(arguments[0]);
                    BookBuilder builder = new BookBuilder();
                    YamlConfiguration yamlConfiguration = aresonSomnium.getSummariesFileManager().getYamlConfiguration();
                    String path = String.format("riassunti.%d", briefNumber);
                    if (yamlConfiguration.isConfigurationSection(path)) {
                        // TODO Max 32 char title length
                        String chapterTitle = yamlConfiguration.getString(path + ".titolo");
                        String title = chapterTitle.length() > 32 ? "Blank" : chapterTitle;
                        String author = "Areson";
                        String content = "&c&l" + chapterTitle + "&r&0\n\n" + yamlConfiguration.getString(path + ".testo");
                        builder.buildWrittenBook(title, author, content);
                        player.openBook(builder.getWrittenBook());
                    } else {
                        String message = String.format("Il riassunto numero %d non esiste. Inserisci un numero di riassunto valido.", briefNumber);
                        player.sendMessage(message);
                    }
                } catch (NumberFormatException exception) {
                    player.sendMessage("Errore. Devi inserire il numero di riassunto. '" + arguments[0] + "' non è un numero.");
                }
            }
        }
        return true;
    }

}
