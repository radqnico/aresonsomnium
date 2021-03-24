package it.areson.aresonsomnium.economy.shops.guis;

import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.exceptions.PriceConfigNotReadyException;
import it.areson.aresonsomnium.economy.shops.items.OldShopItem;

import java.math.BigDecimal;
import java.util.Objects;

public class EditPriceConfig {

    private final CustomShop customShop;
    private int slot = -1;
    private CoinType coinType;
    private BigDecimal price;
    private final boolean isSelling;

    public EditPriceConfig(CustomShop customShop, boolean isSelling) {
        this.customShop = customShop;
        this.isSelling = isSelling;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setCoinType(CoinType coinType) {
        this.coinType = coinType;
    }

    public void setPrice(BigDecimal bigDecimal) {
        this.price = bigDecimal;
    }

    public void execute() throws PriceConfigNotReadyException {
        if (slot < 0) {
            throw new PriceConfigNotReadyException("Manca lo slot");
        } else if (Objects.isNull(customShop)) {
            throw new PriceConfigNotReadyException("Manca lo shop");
        } else if (Objects.isNull(coinType)) {
            throw new PriceConfigNotReadyException("Manca il tipo di prezzo");
        } else if (Objects.isNull(price)) {
            throw new PriceConfigNotReadyException("Manca il prezzo");
        } else {
            OldShopItem oldShopItem = customShop.getItems().get(slot);
            if (!isSelling) {
                switch (coinType) {
                    case OBOLI:
                        oldShopItem.getShoppingPrice().setObols(price.toBigInteger());
                        break;
                    case GEMME:
                        oldShopItem.getShoppingPrice().setGems(price.toBigInteger());
                        break;
                    case MONETE:
                        oldShopItem.getShoppingPrice().setCoins(price);
                        break;
                }
            } else {
                switch (coinType) {
                    case OBOLI:
                        oldShopItem.getSellingPrice().setObols(price.toBigInteger());
                        break;
                    case GEMME:
                        oldShopItem.getSellingPrice().setGems(price.toBigInteger());
                        break;
                    case MONETE:
                        oldShopItem.getSellingPrice().setCoins(price);
                        break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "EditPriceConfig{" +
                "customShop=" + customShop +
                ", slot=" + slot +
                ", coinType=" + coinType.getCoinName() +
                ", price=" + price +
                ", isSelling=" + isSelling +
                '}';
    }
}
