package it.areson.aresonsomnium.placeholders;

import it.areson.aresonsomnium.AresonSomnium;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MultiplierPlaceholders extends PlaceholderExpansion {

    private final AresonSomnium aresonSomnium;

    public MultiplierPlaceholders(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "aresonsomnium";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Areson";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String value = "Nessun dato";

        if (params.equalsIgnoreCase("multipliervalue") && player instanceof Player) {
            value = aresonSomnium.getCachedMultiplier((Player) player) + "x";
        }

        return value;
    }

}
