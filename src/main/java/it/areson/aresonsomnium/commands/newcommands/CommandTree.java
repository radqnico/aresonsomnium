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
    private JavaPlugin plugin;

    public CommandTree(JavaPlugin plugin, CommandTreeNode root) {
        this.plugin = plugin;
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
            for (int i = 0; i < arguments.length; i++) {
                selected = selected.getChild(arguments[i]);
                if (selected == null) {
                    commandSender.sendMessage("Comando sconosciuto");
                    return false;
                }
                if (selected.isLeaf()) {
                    break;
                }
                i += selected.getNumberOfParams();
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
            int i, lastValidIndex = 0;
            for (i = 0; i < arguments.length - 1; i++) {
                selected = selected.getChild(arguments[i]);
                if (selected == null) {
                    return new ArrayList<>(Collections.singletonList("Comando sconosciuto"));
                }
                lastValidIndex = i;
                i += selected.getNumberOfParams();
                if (selected.isLeaf()) {
                    break;
                }
            }
            // Still writing params
            if (selected.hasParams() && i > lastValidIndex) {
                int writingParamIndex = i - lastValidIndex - 1;
                return Collections.singletonList(selected.getParams().get(writingParamIndex));
            }
            // No params or already written them all
            return selected.getChildren().stream().map(CommandTreeNode::getCommand).collect(Collectors.toList());
        } else {
            return root.getChildren().stream().map(CommandTreeNode::getCommand).collect(Collectors.toList());
        }
    }
}
