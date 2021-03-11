package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TestTreeCommand extends CommandTreeNode {

    public TestTreeCommand() {
        super("testtree");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        return false;
    }
}
