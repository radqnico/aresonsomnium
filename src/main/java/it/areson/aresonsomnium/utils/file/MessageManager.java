package it.areson.aresonsomnium.utils.file;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class MessageManager extends FileManager {

    private final String prefix;
    private final String messageNotFound;

    public MessageManager(AresonSomnium plugin, String fileName) {
        super(plugin, fileName);
        prefix = fileConfiguration.getString("prefix", "");
        messageNotFound = prefix + ChatColor.RED + "Errore: messaggio non trovato";
    }

    public String getPlainMessage(String messageKey) {
        String message = fileConfiguration.getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!");
        }
    }

    @SafeVarargs
    public final String getPlainMessage(String messageKey, Pair<String, String>... substitutions) {
        String message = fileConfiguration.getString(messageKey);
        if (Objects.nonNull(message)) {
            for (Pair<String, String> stringPair : substitutions) {
                message = message.replaceAll(stringPair.left(), stringPair.right());
            }

            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return messageNotFound;
        }
    }

    public void sendPlainMessage(CommandSender commandSender, String messageKey) {
        String message = fileConfiguration.getString(messageKey);
        if (Objects.nonNull(message)) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            commandSender.sendMessage(messageNotFound);
        }
    }

    @SafeVarargs
    public final void sendPlainMessage(CommandSender commandSender, String messageKey, Pair<String, String>... substitutions) {
        String message = fileConfiguration.getString(messageKey);
        if (Objects.nonNull(message)) {
            for (Pair<String, String> stringPair : substitutions) {
                message = message.replaceAll(stringPair.left(), stringPair.right());
            }

            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            commandSender.sendMessage(messageNotFound);
        }
    }

    public String getPlainMessageNoPrefix(String messageKey) {
        String message = fileConfiguration.getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', "&cError: '" + messageKey + "' message does not exists!");
        }
    }

}
