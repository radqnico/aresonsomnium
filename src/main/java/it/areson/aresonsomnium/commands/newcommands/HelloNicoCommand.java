package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelloNicoCommand extends CommandTreeNode {

    public HelloNicoCommand() {
        super("hellonico", "<nick>");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        Bukkit.broadcastMessage("Hello nico");
        return true;
    }
}
