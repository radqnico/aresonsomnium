package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SecondLevelCommand2 extends CommandTreeNode {

    public SecondLevelCommand2(String command) {
        super(command,false, 0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        Bukkit.broadcastMessage("Second level 2");
        return true;
    }

}
