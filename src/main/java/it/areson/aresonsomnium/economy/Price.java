package it.areson.aresonsomnium.economy;

import it.areson.aresonsomnium.players.SomniumPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class Price {

    private BigDecimal coins;
    private BigInteger obols;
    private BigInteger gems;

    public Price(BigDecimal coins, BigInteger obols, BigInteger gems) {
        this.coins = coins;
        this.obols = obols;
        this.gems = gems;
    }

    public Price() {
        this(BigDecimal.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0));
    }

    public Price(long shoppingCoins, long shoppingObols, long shoppingGems) {
        this(BigDecimal.valueOf(shoppingCoins), BigInteger.valueOf(shoppingObols), BigInteger.valueOf(shoppingGems));
    }

    public static Price zero() {
        return new Price(0, 0, 0);
    }

    public BigDecimal getCoins() {
        return coins;
    }

    public void setCoins(BigDecimal coins) {
        this.coins = coins;
    }

    public BigInteger getObols() {
        return obols;
    }

    public void setObols(BigInteger obols) {
        this.obols = obols;
    }

    public BigInteger getGems() {
        return gems;
    }

    public void setGems(BigInteger gems) {
        this.gems = gems;
    }

    public boolean canAffordThis(SomniumPlayer somniumPlayer) {
        return Wallet.getCoins(somniumPlayer.getPlayer()).compareTo(coins) >= 0 &&
                somniumPlayer.getWallet().getObols().compareTo(obols) >= 0 &&
                somniumPlayer.getWallet().getGems().compareTo(gems) >= 0;
    }

    public void removeFrom(SomniumPlayer somniumPlayer) {
        Wallet.addCoins(somniumPlayer.getPlayer(), coins.negate());
        somniumPlayer.getWallet().changeObols(obols.negate());
        somniumPlayer.getWallet().changeGems(gems.negate());
    }

    public void addTo(SomniumPlayer somniumPlayer) {
        Wallet.addCoins(somniumPlayer.getPlayer(), coins);
        somniumPlayer.getWallet().changeObols(obols);
        somniumPlayer.getWallet().changeGems(gems);
    }

    public boolean isPriceReady() {
        return coins.compareTo(BigDecimal.valueOf(0)) > 0 ||
                obols.compareTo(BigInteger.valueOf(0)) > 0 ||
                gems.compareTo(BigInteger.valueOf(0)) > 0;
    }

    @Override
    public String toString() {
        return "Price{coins=" + coins + ",obols=" + obols + ",gems=" + gems + "}";
    }

    public List<Component> toLore(boolean isShopping) {
        List<Component> lore = new ArrayList<>();
        if (isPriceReady()) {
            TextComponent start = Component.text().content("Prezzo di ").decoration(ITALIC, false).color(GRAY).build();
            if (isShopping) {
                start = start.append(Component.text().content("acquisto:").color(GRAY).decoration(BOLD, true).decoration(ITALIC, false).build());
            } else {
                start = start.append(Component.text().content("vendita:").color(GRAY).decoration(BOLD, true).decoration(ITALIC, false).build());
            }
            lore.add(start);
            if (coins.compareTo(BigDecimal.valueOf(0)) > 0) {
                lore.add(Component.text().content(coins.toString() + " ⛃").color(YELLOW).decoration(ITALIC, false).build());
            }
            if (obols.compareTo(BigInteger.valueOf(0)) > 0) {
                lore.add(Component.text().content(obols.toString() + " ❂").color(GOLD).decoration(ITALIC, false).build());
            }
            if (gems.compareTo(BigInteger.valueOf(0)) > 0) {
                lore.add(Component.text().content(gems.toString() + " ♦").color(GREEN).decoration(ITALIC, false).build());
            }
        } else {
            TextComponent start = Component.text().content("Oggetto non ").color(RED).decoration(ITALIC, false).build();
            if (isShopping) {
                start = start.append(Component.text().content("acquistabile").color(RED).decoration(BOLD, true).decoration(ITALIC, false).build());
            } else {
                start = start.append(Component.text().content("vendibile").color(RED).decoration(BOLD, true).decoration(ITALIC, false).build());
            }
            lore.add(start);
        }
        lore.add(Component.empty());
        return lore;
    }

    public void setPrice(CoinType coinType, BigDecimal price) {
        switch (coinType) {
            case OBOLI:
                setObols(price.toBigInteger());
                break;
            case GEMME:
                setGems(price.toBigInteger());
                break;
            case MONETE:
                setCoins(price);
                break;
        }
    }
}
