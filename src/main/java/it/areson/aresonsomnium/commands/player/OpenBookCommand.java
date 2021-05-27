package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
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
        Optional<Player> optionalPlayer = this.getPlayer(commandSender);
        optionalPlayer.ifPresent(player -> {
            ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
            bookMeta.setTitle("PROVA");
            bookMeta.setAuthor("VALERIO");
            bookMeta.addPages(Component.text("Ciao mamma sono su un libro!"));
            writtenBook.setItemMeta(bookMeta);
            player.openBook(writtenBook);
        });
        return true;
    }

    private Optional<Player> getPlayer(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            return Optional.of((Player)commandSender);
        }
        return Optional.empty();
    }
}
