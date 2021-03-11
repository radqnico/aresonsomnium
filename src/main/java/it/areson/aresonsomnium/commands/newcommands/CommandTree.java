package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTree implements CommandExecutor, TabCompleter {

    private @NotNull CommandTreeNode root;

    public CommandTree(JavaPlugin plugin, CommandTreeNode root) {
        this.root = root;
        PluginCommand command = plugin.getCommand(root.getCommand());
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            plugin.getLogger().severe("Il comando " + root.getCommand() + " non è stato dichiarato.");
        }
    }

    public @NotNull CommandTreeNode getRoot() {
        return root;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        if (arguments.length >= 1) {
            CommandTreeNode selected = root;
            for (String argument : arguments) {
                selected = selected.getChild(argument);
                if (selected == null) {
                    commandSender.sendMessage("Comando sconosciuto");
                    return false;
                }
                if (selected.isLeaf()) {
                    break;
                }
            }
            selected.onCommand(commandSender, command, alias, arguments);
        } else {
            root.onCommand(commandSender, command, alias, arguments);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        if (arguments.length >= 1) {
            CommandTreeNode selected = root;
            for (String argument : arguments) {
                selected = selected.getChild(argument);
                if (selected == null) {
                    commandSender.sendMessage("Comando sconosciuto");
                    return new ArrayList<>(Collections.singletonList("Comando sconosciuto"));
                }
                if (selected.isLeaf()) {
                    break;
                }
            }
            return selected.getChildren().stream().map(CommandTreeNode::getCommand).collect(Collectors.toList());
        } else {
            return root.getChildren().stream().map(CommandTreeNode::getCommand).collect(Collectors.toList());
        }
    }
}
