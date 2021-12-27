package it.areson.aresonsomnium.utils.file;

import it.areson.aresonlib.file.FileManager;
import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GommaObjectsFileReader extends FileManager {

    public GommaObjectsFileReader(AresonSomnium plugin, String fileName) {
        super(plugin, fileName);
    }

    public void checkGommaLocation() {
        if (!fileConfiguration.isConfigurationSection("location")) {
            fileConfiguration.createSection("location");
        }
    }

    public void checkItemsSection() {
        if (!fileConfiguration.isConfigurationSection("items")) {
            fileConfiguration.createSection("items");
        }
    }

    public void storeItem(ItemStack itemStack) {
        checkItemsSection();
        ConfigurationSection itemsSection = fileConfiguration.getConfigurationSection("items");
        int maxKey = -1;
        if (Objects.nonNull(itemsSection)) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    int i = Integer.parseInt(key);
                    if (i > maxKey) {
                        maxKey = i;
                    }
                } catch (NumberFormatException exception) {
                    javaPlugin.getLogger().severe("GOMMA GOMMA: chiave " + key + " non valida. Usa solo numeri interi positivi.");
                }
            }
            maxKey++;
            itemsSection.set(maxKey + ".item", Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()));
            itemsSection.set(maxKey + ".amount", itemStack.getAmount());
            itemsSection.set(maxKey + ".material-READONLY", itemStack.getType().name());
            saveFileConfiguration();
        }
    }

    public List<ItemStack> getItemList() {
        checkItemsSection();
        ConfigurationSection itemsSection = fileConfiguration.getConfigurationSection("items");
        List<ItemStack> itemStacks = new ArrayList<>();
        if (Objects.nonNull(itemsSection)) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    Integer.parseInt(key);
                    String itemStackBase64String = itemsSection.getString(key + ".item");
                    byte[] decode = Base64.getDecoder().decode(itemStackBase64String);
                    ItemStack itemStack = ItemStack.deserializeBytes(decode);
                    itemStack.setAmount(itemsSection.getInt(key + ".amount", 1));
                    itemStacks.add(itemStack);
                } catch (NumberFormatException exception) {
                    javaPlugin.getLogger().severe("GOMMA GOMMA: chiave " + key + " non valida. Usa solo numeri interi positivi.");
                }
            }
        }
        return itemStacks;
    }

    public Location getGommaBlock() {
        checkGommaLocation();
        ConfigurationSection locationSector = fileConfiguration.getConfigurationSection("location");
        if (Objects.nonNull(locationSector)) {
            String world = locationSector.getString("world");
            double x = locationSector.getDouble("x");
            double y = locationSector.getDouble("y");
            double z = locationSector.getDouble("z");
            float yaw = (float) locationSector.getDouble("yaw");
            float pitch = (float) locationSector.getDouble("pitch");
            if (Objects.nonNull(world)) {
                return new Location(javaPlugin.getServer().getWorld(world), x, y, z, yaw, pitch).toBlockLocation();
            }
        }
        return null;
    }

    public void setGommaBlock(Location location) {
        checkGommaLocation();
        location = location.toBlockLocation();
        ConfigurationSection locationSector = fileConfiguration.getConfigurationSection("location");
        if (Objects.nonNull(locationSector)) {
            locationSector.set("world", location.getWorld().getName());
            locationSector.set("x", location.getX());
            locationSector.set("y", location.getY());
            locationSector.set("z", location.getZ());
            locationSector.set("yaw", location.getYaw());
            locationSector.set("pitch", location.getPitch());
            saveFileConfiguration();
        }
    }

    public ItemStack getRandomItem() {
        List<ItemStack> itemList = getItemList();
        if (itemList.size() > 0) {
            Collections.shuffle(itemList);
            int random = new Random().nextInt(itemList.size());
            return itemList.get(random);
        }
        return new ItemStack(Material.AIR);
    }
}
