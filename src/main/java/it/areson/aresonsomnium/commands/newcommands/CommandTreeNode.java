package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandTreeNode implements CommandExecutor {

    private final String command;
    private final List<CommandTreeNode> children;
    private CommandTreeNode parent;

    public CommandTreeNode(String command, CommandTreeNode parent, List<CommandTreeNode> children) {
        this.command = command;
        this.parent = parent;
        this.children = children;
    }

    public CommandTreeNode(String command, CommandTreeNode parent) {
        this(command, parent, new ArrayList<>());
    }

    public CommandTreeNode(String command) {
        this(command, null, new ArrayList<>());
    }

    public String getCommand() {
        return command;
    }

    public CommandTreeNode getParent() {
        return parent;
    }

    public void setParent(CommandTreeNode parent) {
        this.parent = parent;
    }

    public List<CommandTreeNode> getChildren() {
        return children;
    }

    public CommandTreeNode getChild(String command) {
        for (CommandTreeNode commandTreeNode : children) {
            if (commandTreeNode.getCommand().equals(command)) {
                return commandTreeNode;
            }
        }
        return null;
    }

    public void addChild(CommandTreeNode commandTreeNode) {
        commandTreeNode.setParent(this);
        children.add(commandTreeNode);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public abstract boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments);

}
