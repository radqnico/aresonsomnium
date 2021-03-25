package it.areson.aresonsomnium.commands.newcommands;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AresonCommand("shopadmin editshopitems")
public class EditItemsCommand extends CommandParserCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length == 2) {
                try {
                    int page = Integer.parseInt(strings[1]) - 1;
                    AresonSomniumAPI.instance.shopItemsManager.openEditGuiToPlayer(player, page);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
                    player.sendMessage("La pagina non è un numero");
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
