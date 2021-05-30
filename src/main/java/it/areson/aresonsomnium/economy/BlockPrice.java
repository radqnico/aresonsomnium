package it.areson.aresonsomnium.economy;

import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public class BlockPrice {

    private static final HashMap<Material, BigDecimal> prices = new HashMap<Material, BigDecimal>() {{
        put(Material.NETHERRACK, BigDecimal.valueOf(1));
        put(Material.GRANITE, BigDecimal.valueOf(2.5));
        put(Material.BLACKSTONE, BigDecimal.valueOf(5));
        put(Material.POLISHED_GRANITE, BigDecimal.valueOf(12));
        put(Material.POLISHED_BLACKSTONE_BRICKS, BigDecimal.valueOf(21));
        put(Material.RED_CONCRETE, BigDecimal.valueOf(32));
        put(Material.ANDESITE, BigDecimal.valueOf(52));
        put(Material.POLISHED_ANDESITE, BigDecimal.valueOf(74));
        put(Material.DIORITE, BigDecimal.valueOf(98));
        put(Material.POLISHED_DIORITE, BigDecimal.valueOf(114));
        put(Material.LIME_CONCRETE, BigDecimal.valueOf(142));
        put(Material.PRISMARINE, BigDecimal.valueOf(182));
        put(Material.PRISMARINE_BRICKS, BigDecimal.valueOf(224));
        put(Material.DARK_PRISMARINE, BigDecimal.valueOf(268));
        put(Material.CYAN_CONCRETE, BigDecimal.valueOf(362));
        put(Material.WHITE_CONCRETE, BigDecimal.valueOf(420));
    }};

    public static BigDecimal getPrice(Material material) throws MaterialNotSellableException {
        BigDecimal price = prices.get(material);
        if (Objects.nonNull(price)) {
            return price;
        } else {
            throw new MaterialNotSellableException("Questo materiale (" + material.name() + ") non ha un prezzo");
        }
    }

}
