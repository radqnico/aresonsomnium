package it.areson.aresonsomnium.utils;

import it.areson.aresonsomnium.AresonSomnium;

public class Debugger {

    private final AresonSomnium aresonSomnium;
    private DebugLevel debugLevel;

    public Debugger(AresonSomnium aresonSomnium, DebugLevel debugLevel) {
        this.aresonSomnium = aresonSomnium;
        this.debugLevel = debugLevel;
    }

    public void setDebugLevel(DebugLevel debugLevel) {
        this.debugLevel = debugLevel;
    }

    public void debugSuccess(String message) {
        switch (debugLevel) {
            case LOW:
                aresonSomnium.getLogger().info(MessageUtils.successMessage(message));
                break;
            case HIGH:
                aresonSomnium.getLogger().info(MessageUtils.successMessage(message));
                aresonSomnium.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOp()) {
                        player.sendMessage(MessageUtils.successMessage("DEBUG: " + message));
                    }
                });
                break;
        }
    }

    public void debugInfo(String message) {
        switch (debugLevel) {
            case LOW:
                aresonSomnium.getLogger().info(message);
                break;
            case HIGH:
                aresonSomnium.getLogger().info(message);
                aresonSomnium.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOp()) {
                        player.sendMessage("DEBUG: " + message);
                    }
                });
                break;
        }
    }

    public void debugWarning(String message) {
        switch (debugLevel) {
            case LOW:
                aresonSomnium.getLogger().warning(message);
                break;
            case HIGH:
                aresonSomnium.getLogger().warning(message);
                aresonSomnium.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOp()) {
                        player.sendMessage(MessageUtils.warningMessage("DEBUG: " + message));
                    }
                });
                break;
        }
    }

    public void debugError(String message) {
        switch (debugLevel) {
            case LOW:
                aresonSomnium.getLogger().severe(message);
                break;
            case HIGH:
                aresonSomnium.getLogger().severe(message);
                aresonSomnium.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOp()) {
                        player.sendMessage(MessageUtils.errorMessage("DEBUG: " + message));
                    }
                });
                break;
        }
    }

    public enum DebugLevel {
        LOW, // Only Errors
        HIGH // All that happens in console
    }
}
