package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandTreeNode implements CommandExecutor {

    private final String command;
    private final List<CommandTreeNode> children;
    private CommandTreeNode parent;
    private List<String> parameters;

    public CommandTreeNode(String command, CommandTreeNode parent, List<CommandTreeNode> children, String... parameters) {
        this.command = command;
        this.parent = parent;
        this.children = children;
        this.parameters = new ArrayList<>(Arrays.asList(parameters));
    }

    public CommandTreeNode(String command, CommandTreeNode parent, List<CommandTreeNode> children) {
        this(command, parent, children, new String[]{});
    }

    public CommandTreeNode(String command, String... parameters) {
        this(command, null, new ArrayList<>(), parameters);
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

    public int getNumberOfParams() {
        return parameters.size();
    }

    public boolean hasParams(){
        return !parameters.isEmpty();
    }

    @Override
    public abstract boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments);

    public List<String> getParams() {
        return parameters;
    }
}
