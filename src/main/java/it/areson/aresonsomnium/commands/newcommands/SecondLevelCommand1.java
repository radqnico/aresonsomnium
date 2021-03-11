package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SecondLevelCommand1 extends CommandTreeNode {

    public SecondLevelCommand1() {
        super("second");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        Bukkit.broadcastMessage("Second level 1");
        return true;
    }

}
