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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;

public class OpenBookCommand implements CommandExecutor {

    public OpenBookCommand(AresonSomnium aresonSomnium) {
        PluginCommand command = aresonSomnium.getCommand("riassunto");
        if (command != null) {
            command.setExecutor(this);
        } else {
            aresonSomnium.getLogger().warning("Comando 'assegno' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            try {
                int numeroRiassunto = Integer.parseInt(args[0]);
                Optional<Player> optionalPlayer = this.getPlayer(commandSender);
                optionalPlayer.ifPresent(player -> {
                    BookBuilder builder = new BookBuilder();
                    FileConfiguration configuration = AresonSomniumAPI.instance.getRiassunti().getFileConfiguration();
                    String path = String.format("riassunti.%d.", numeroRiassunto);
                    String title = configuration.getString(path + "titolo");
                    String author = "Areson";
                    String content = configuration.getString(path + "testo");
                    builder.buildWrittenBook(title, author, content);
                    player.openBook(builder.getWrittenBook());
                });
            } catch (NumberFormatException exception) {
                System.out.println("Errore. Devi inserire il numero di riassunto. '"+ args[1] +"' non è valido.");
            }
        }
        return true;
    }

    private Optional<Player> getPlayer(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            return Optional.of((Player)commandSender);
        }
        return Optional.empty();
    }
}
