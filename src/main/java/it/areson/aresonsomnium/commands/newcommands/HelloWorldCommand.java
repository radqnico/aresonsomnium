package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HelloWorldCommand extends CommandTreeNode {

    public HelloWorldCommand() {
        super("helloworld");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        return false;
    }
}
