package it.areson.aresonsomnium.economy;

import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public class BlockPrice {

    private static final HashMap<Material, BigDecimal> prices = new HashMap<Material, BigDecimal>() {{
        put(Material.COBBLESTONE, BigDecimal.valueOf(1));
        put(Material.NETHERRACK, BigDecimal.valueOf(2.5));
        put(Material.COAL_BLOCK, BigDecimal.valueOf(6));
        put(Material.RED_NETHER_BRICKS, BigDecimal.valueOf(11));
        put(Material.MAGMA_BLOCK, BigDecimal.valueOf(25));
        put(Material.RED_CONCRETE, BigDecimal.valueOf(55));
        put(Material.ANDESITE, BigDecimal.valueOf(90));
        put(Material.POLISHED_ANDESITE, BigDecimal.valueOf(170));
        put(Material.DIORITE, BigDecimal.valueOf(290));
        put(Material.POLISHED_DIORITE, BigDecimal.valueOf(480));
        put(Material.LIME_CONCRETE, BigDecimal.valueOf(800));
        put(Material.PRISMARINE, BigDecimal.valueOf(1400));
        put(Material.PRISMARINE_BRICKS, BigDecimal.valueOf(2450));
        put(Material.QUARTZ_BLOCK, BigDecimal.valueOf(3800));
        put(Material.CHISELED_QUARTZ_BLOCK, BigDecimal.valueOf(8000));
        put(Material.LIGHT_BLUE_CONCRETE, BigDecimal.valueOf(18000));
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
