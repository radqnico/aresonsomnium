package it.areson.aresonsomnium.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public abstract class CommandParserCommand implements CommandExecutor, TabCompleter {
    protected int depth = 0;

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
