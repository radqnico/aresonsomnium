package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.books.BookBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class OpenBookCommand implements CommandExecutor {

    public OpenBookCommand(AresonSomnium aresonSomnium) {
        PluginCommand command = aresonSomnium.getCommand("riassunto");
        if (command != null) {
            command.setExecutor(this);
        } else {
            aresonSomnium.getLogger().warning("Comando 'assegno' non dichiarato");
        }
    }

    //TODO From it to en
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            Optional<Player> optionalPlayer = this.getPlayer(commandSender);
            optionalPlayer.ifPresent(player -> {
                try {
                    int briefNumber = Integer.parseInt(arguments[0]);
                    BookBuilder builder = new BookBuilder();
                    FileConfiguration configuration = AresonSomniumAPI.instance.getBriefing().getFileConfiguration();
                    String path = String.format("riassunti.%d", briefNumber);
                    if (configuration.isConfigurationSection(path)) {
                        // TODO Max 32 char title length
                        String chapterTitle = configuration.getString(path + ".titolo");
                        String title = chapterTitle.length() > 32 ? "Blank" : chapterTitle;
                        String author = "Areson";
                        String content = "&c&l" + chapterTitle + "&r&0\n\n" + configuration.getString(path + ".testo");
                        builder.buildWrittenBook(title, author, content);
                        player.openBook(builder.getWrittenBook());
                    } else {
                        String message = String.format("Il riassunto numero %d non esiste. Inserisci un numero di riassunto valido.", briefNumber);
                        player.sendMessage(message);
                    }
                } catch (NumberFormatException exception) {
                    player.sendMessage("Errore. Devi inserire il numero di riassunto. '" + arguments[0] + "' non è un numero.");
                }
            });
        }
        return true;
    }

    private Optional<Player> getPlayer(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            return Optional.of((Player) commandSender);
        }
        return Optional.empty();
    }
}
