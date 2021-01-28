package it.areson.aresonsomnium.shops.items;

import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.TreeMap;

public class BlockPrice {

    static final TreeMap<Material, BigDecimal> prices = new TreeMap<>();

    public static void initPrices() {
        prices.put(Material.COBBLESTONE, BigDecimal.valueOf(1));
        prices.put(Material.NETHERRACK, BigDecimal.valueOf(2.5));
        prices.put(Material.COAL_BLOCK, BigDecimal.valueOf(6));
        prices.put(Material.RED_NETHER_BRICKS, BigDecimal.valueOf(11));
        prices.put(Material.MAGMA_BLOCK, BigDecimal.valueOf(25));
        prices.put(Material.RED_CONCRETE, BigDecimal.valueOf(55));
        prices.put(Material.ANDESITE, BigDecimal.valueOf(90));
        prices.put(Material.POLISHED_ANDESITE, BigDecimal.valueOf(170));
        prices.put(Material.DIORITE, BigDecimal.valueOf(290));
        prices.put(Material.POLISHED_DIORITE, BigDecimal.valueOf(480));
        prices.put(Material.LIME_CONCRETE, BigDecimal.valueOf(800));
        prices.put(Material.PRISMARINE, BigDecimal.valueOf(1400));
        prices.put(Material.PRISMARINE_BRICKS, BigDecimal.valueOf(2450));
        prices.put(Material.QUARTZ_BLOCK, BigDecimal.valueOf(3800));
        prices.put(Material.CHISELED_QUARTZ_BLOCK, BigDecimal.valueOf(8000));
        prices.put(Material.LIGHT_BLUE_CONCRETE, BigDecimal.valueOf(18000));
    }

    public static BigDecimal getPrice(Material material) throws MaterialNotSellableException {
        BigDecimal price = prices.get(material);
        if (Objects.nonNull(price)) {
            return price;
        } else {
            throw new MaterialNotSellableException("Questo materiale (" + material.name() + ") non ha un prezzo");
        }
    }

}
