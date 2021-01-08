package it.areson.aresonsomnium.utils;

import it.areson.aresonsomnium.AresonSomnium;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;

public class MessageManager extends FileManager {

    private final String prefix;

    public MessageManager(AresonSomnium plugin, String fileName) {
        super(plugin, fileName);
        prefix = getFileConfiguration().getString("prefix", "");
    }

    public void sendPlainMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }

    public void sendJsonMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            BaseComponent[] rawMessage = ComponentSerializer.parse(message);
            player.spigot().sendMessage(rawMessage);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }

    @SafeVarargs
    public final String getPlainMessage(String messageKey, Pair<String, String>... substitutions) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            for (Pair<String, String> stringPair : substitutions) {
                message = message.replaceAll(stringPair.left(), stringPair.right());
            }

            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!");
        }
    }

    public String getPlainMessage(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!");
        }
    }

    public BaseComponent[] getJsonMessage(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ComponentSerializer.parse(message);
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }
}
