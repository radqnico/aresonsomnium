package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.exceptions.PriceConfigNotReadyException;
import it.areson.aresonsomnium.shops.items.ShopItem;

import java.math.BigDecimal;
import java.util.Objects;

public class EditPriceConfig {

    private final CustomShop customShop;
    private int slot = -1;
    private CoinType coinType;
    private BigDecimal price;

    public EditPriceConfig(CustomShop customShop) {
        this.customShop = customShop;
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
        if (slot < 0 || Objects.isNull(customShop) || Objects.isNull(coinType) || Objects.isNull(price)) {
            ShopItem shopItem = customShop.getItems().get(slot);
            switch (coinType){
                case CHARON:
                    shopItem.getPrice().setCharonCoins(price.toBigInteger());
                    break;
                case FORCED:
                    shopItem.getPrice().setForcedCoins(price.toBigInteger());
                    break;
                case BASIC:
                    shopItem.getPrice().setBasicCoins(price);
                    break;
            }
        } else {
            throw new PriceConfigNotReadyException();
        }
    }
}
