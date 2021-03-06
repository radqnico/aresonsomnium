package it.areson.aresonsomnium.placeholders;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("NullableProblems")
public class CoinsPlaceholders extends PlaceholderExpansion {

    private final AresonSomnium aresonSomnium;

    public CoinsPlaceholders(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
    }

    @Override
    public String getIdentifier() {
        return "playerstats";
    }

    @Override
    public String getAuthor() {
        return "Areson";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String query) {
        if (offlinePlayer != null && offlinePlayer.getName() != null && offlinePlayer instanceof Player player) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                switch (query.toLowerCase()) {
                    case "coins":
                        return Wallet.getCoins(player).toPlainString();
                    case "obols":
                        return somniumPlayer.getWallet().getObols().toString();
                    case "gems":
                        return somniumPlayer.getWallet().getGems().toString();
                    case "playedseconds":
                        return somniumPlayer.getSecondsPlayedTotal() + "";
                    default:
                        break;
                }
            }
        }
        return "ERRORE";
    }
}
