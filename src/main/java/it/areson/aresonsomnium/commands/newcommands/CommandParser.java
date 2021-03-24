package it.areson.aresonsomnium.commands.newcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.logging.Level;

public class CommandParser extends CommandParserCommand {
    private final JavaPlugin plugin;
    private final HashMap<String, CommandParserCommand> commands = new HashMap<>();
    private final HashMap<String, CommandParserCommand> commandBuffer = new HashMap<>();

    public CommandParser(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > this.depth) {
            CommandExecutor selectedCommand = commands.get(strings[this.depth].toLowerCase());
            if (selectedCommand != null) {
                return selectedCommand.onCommand(commandSender, command, s, strings);
            }
        }
        return false;
    }

    /**
     * Add a command to the buffer, for generate the tree call registerCommands().
     * The command is not effectively added, but ony buffered!
     *
     * @param executor The command executor
     * @throws Exception The provided command doesn't have the annotation @AresonCommand
     */
    public void addAresonCommand(CommandParserCommand executor) throws Exception {
        Annotation[] ann = executor.getClass().getAnnotations();
        boolean added = false;
        for (Annotation a : ann) {
            if (a instanceof AresonCommand) {
                commandBuffer.put(((AresonCommand) a).value(), executor);
                added = true;
                plugin.getLogger().log(Level.INFO, "Command inserted to buffer " + executor.getClass().getName() + " command");
            }
        }
        if (!added) {
            throw new Exception("Class " + executor.getClass().getName() + " doesn't have Annotation");
        }
    }

    /**
     * RESERVED
     *
     * @param command  Name of the command
     * @param executor Executor of the command
     */
    public void registerCommand(String command, CommandParserCommand executor) {
        String[] splitted = command.split(" ");
        if (splitted.length > 1) {
            CommandParserCommand selectedCommand = commands.get(splitted[0]);
            if (selectedCommand != null) {
                ((CommandParser) selectedCommand).registerCommand(String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length)), executor);
            } else {
                CommandParser parser = new CommandParser(plugin);
                parser.setDepth(this.depth + 1);
                parser.registerCommand(String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length)), executor);
                this.commands.put(splitted[0], parser);
            }
        } else {
            executor.setDepth(this.depth + 1);
            this.commands.put(command, executor);
        }
    }

    /**
     * Create the command tree of the buffered commands.
     */
    public void registerCommands() {
        for (Map.Entry<String, CommandParserCommand> s : commandBuffer.entrySet()) {
            registerCommand(s.getKey(), s.getValue());
        }
        commandBuffer.clear();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > this.depth + 1) {
            CommandParserCommand selectedCommand = commands.get(strings[this.depth].toLowerCase());
            if (selectedCommand != null) {
                return selectedCommand.onTabComplete(commandSender, command, s, strings);
            }
            return StringUtil.copyPartialMatches(strings[this.depth], this.commands.keySet(), new ArrayList<>());
        }
        return new ArrayList<>(this.commands.keySet());
    }
}
