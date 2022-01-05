package it.areson.aresonsomnium.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    public static void notEnoughArguments(CommandSender commandSender, Command command) {
        commandSender.sendMessage(errorMessage("Parametri non sufficienti"));
        commandSender.sendMessage(command.getUsage());
    }

    public static void tooManyArguments(CommandSender commandSender, Command command) {
        commandSender.sendMessage(errorMessage("Troppi parametri forniti"));
        commandSender.sendMessage(command.getUsage());
    }

    public static String successMessage(String message) {
        return ChatColor.GREEN + message + ChatColor.RESET;
    }

    public static String warningMessage(String message) {
        return ChatColor.YELLOW + message + ChatColor.RESET;
    }

    public static String errorMessage(String message) {
        return ChatColor.RED + message + ChatColor.RESET;
    }


}
