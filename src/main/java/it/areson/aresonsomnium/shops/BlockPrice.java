package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.TreeMap;

public class BlockPrice {

    static final TreeMap<Material, BigDecimal> prices = new TreeMap<>();

    public static void initPrices() {
        prices.put(Material.COBBLESTONE, BigDecimal.valueOf(1.5));
        prices.put(Material.NETHERRACK, BigDecimal.valueOf(3));
        prices.put(Material.COAL_BLOCK, BigDecimal.valueOf(7));
        prices.put(Material.RED_NETHER_BRICKS, BigDecimal.valueOf(13));
        prices.put(Material.MAGMA_BLOCK, BigDecimal.valueOf(27));
        prices.put(Material.RED_CONCRETE, BigDecimal.valueOf(60));
        prices.put(Material.ANDESITE, BigDecimal.valueOf(100));
        prices.put(Material.POLISHED_ANDESITE, BigDecimal.valueOf(180));
        prices.put(Material.DIORITE, BigDecimal.valueOf(300));
        prices.put(Material.POLISHED_DIORITE, BigDecimal.valueOf(500));
        prices.put(Material.LIME_CONCRETE, BigDecimal.valueOf(850));
        prices.put(Material.PRISMARINE, BigDecimal.valueOf(1500));
        prices.put(Material.PRISMARINE_BRICKS, BigDecimal.valueOf(2650));
        prices.put(Material.QUARTZ_BLOCK, BigDecimal.valueOf(4250));
        prices.put(Material.CHISELED_QUARTZ_BLOCK, BigDecimal.valueOf(9000));
        prices.put(Material.LIGHT_BLUE_CONCRETE, BigDecimal.valueOf(20000));
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
