package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.CommandParser;
import it.areson.aresonsomnium.commands.admin.GiveConsumableCommand;
import it.areson.aresonsomnium.commands.admin.ObolsCommand;
import it.areson.aresonsomnium.commands.admin.SomniumAdminCommand;
import it.areson.aresonsomnium.commands.admin.SomniumGommaCommand;
import it.areson.aresonsomnium.commands.player.CheckCommand;
import it.areson.aresonsomnium.commands.player.OpenBookCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.commands.player.StatsCommand;
import it.areson.aresonsomnium.commands.repair.SomniumRepairCommand;
import it.areson.aresonsomnium.commands.shopadmin.*;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.BlockPrice;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import it.areson.aresonsomnium.elements.Multiplier;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.listeners.*;
import it.areson.aresonsomnium.placeholders.CoinsPlaceholders;
import it.areson.aresonsomnium.placeholders.MultiplierPlaceholders;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.pvp.LastHitPvP;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.file.FileManager;
import it.areson.aresonsomnium.utils.file.GommaObjectsFileReader;
import it.areson.aresonsomnium.utils.file.MessageManager;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static it.areson.aresonsomnium.Constants.PERMISSION_MULTIPLIER;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class AresonSomnium extends JavaPlugin {

    private final Multiplier defaultMultiplier = new Multiplier();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final String messagePrefix = "[Somnium]";
    private final HashMap<Material, String> blocksPermission = new HashMap<Material, String>() {{
        put(Material.NETHERRACK, Constants.PERMISSION_SELVA);
        put(Material.GRANITE, Constants.PERMISSION_ANTINFERNO);
        put(Material.BLACKSTONE, Constants.PERMISSION_SECONDO_GIRONE);
        put(Material.POLISHED_GRANITE, Constants.PERMISSION_QUARTO_GIRONE);
        put(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, Constants.PERMISSION_SESTO_GIRONE);
        put(Material.RED_CONCRETE, Constants.PERMISSION_OTTAVO_GIRONE);
        put(Material.ANDESITE, Constants.PERMISSION_LUCIFERO);
        put(Material.POLISHED_ANDESITE, Constants.PERMISSION_PRIMA_CORNICE);
        put(Material.DIORITE, Constants.PERMISSION_TERZA_CORNICE);
        put(Material.POLISHED_DIORITE, Constants.PERMISSION_QUINTA_CORNICE);
        put(Material.LIME_CONCRETE, Constants.PERMISSION_SESTA_CORNICE);
        put(Material.PRISMARINE, Constants.PERMISSION_EDEN);
        put(Material.PRISMARINE_BRICKS, Constants.PERMISSION_SECONDO_CIELO);
        put(Material.DARK_PRISMARINE, Constants.PERMISSION_QUARTO_CIELO);
        put(Material.CYAN_CONCRETE, Constants.PERMISSION_SESTO_CIELO);
        put(Material.LIGHT_BLUE_CONCRETE, Constants.PERMISSION_SETTIMO_CIELO);
        put(Material.WHITE_CONCRETE, Constants.PERMISSION_NONO_CIELO);
    }};
    private final HashMap<String, Multiplier> playerMultipliers = new HashMap<>();
    public ShopItemsManager shopItemsManager;
    public Optional<LuckPerms> luckPerms;
    private SomniumPlayerManager somniumPlayerManager;
    private GatewayListener playerDBEvents;
    private GommaObjectsFileReader gommaObjectsFileReader;
    private MessageManager messages;
    private FileManager recaps;
    private FileManager riassunti;

    private LastHitPvP lastHitPvP;

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        AresonSomniumAPI.instance = this;
        // Files
        registerFiles();

        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(this);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);

        shopItemsManager = new ShopItemsManager(this, mySqlDBConnection);

        // Files
        registerFiles();
        // Events
        initListeners();
        // Commands
        registerCommands();
        // Placeholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiplierPlaceholders(this).register();
        }

        // LuckPerms
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = Optional.of(provider.getProvider());
            new LuckPermsListener(this, provider.getProvider());
        } else {
            getLogger().warning("LuckPerms API not found");
            luckPerms = Optional.empty();
        }

        // Auto Save Task interval
        // 1m  = 1200
        // 10m = 12000
        AutoSaveManager.startAutoSaveTask(this, 12000);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CoinsPlaceholders(this).register();
        }
        Recaps.initRecaps(recaps);

        new PlayerListener(this);
        lastHitPvP = new LastHitPvP();
    }

    public LastHitPvP getLastHitPvP(){
        return lastHitPvP;
    }

    public MessageManager getMessageManager() {
        return messages;
    }

    private void registerFiles() {
        messages = new MessageManager(this, "messages.yml");
        gommaObjectsFileReader = new GommaObjectsFileReader(this, "gommaItems.yml");
        recaps = new FileManager(this, "recaps.yml");
        riassunti = new FileManager(this, "riassunti.yml");
    }

    public GommaObjectsFileReader getGommaObjectsFileReader() {
        return gommaObjectsFileReader;
    }

    private void registerCommands() {

        CommandParser parserShopAdmin = new CommandParser(this);
        PluginCommand command = this.getCommand("shopadmin");
        if (command == null) {
            this.getLogger().log(Level.SEVERE, "Cannot register shopadmin commands");
            return;
        }

        try {
            parserShopAdmin.addAresonCommand(new EditItemsCommand());
            parserShopAdmin.addAresonCommand(new ReloadItemsCommand());
            parserShopAdmin.addAresonCommand(new SetItemPriceCommand());
            parserShopAdmin.addAresonCommand(new BuyItemCommand());
            parserShopAdmin.addAresonCommand(new SellItemCommand());
            parserShopAdmin.registerCommands();
            parserShopAdmin.addAresonCommand(new EditItemsCommand());
            parserShopAdmin.addAresonCommand(new ReloadItemsCommand());
            parserShopAdmin.addAresonCommand(new SetItemPriceCommand());
            parserShopAdmin.addAresonCommand(new BuyItemCommand());
            parserShopAdmin.addAresonCommand(new SellItemCommand());
            parserShopAdmin.addAresonCommand(new SellLootableCommand());
            parserShopAdmin.registerCommands();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        command.setExecutor(parserShopAdmin);
        command.setTabCompleter(parserShopAdmin);

        new SomniumAdminCommand(this);
        new StatsCommand(this);
        new SomniumGommaCommand(this);
        new SellCommand(this, Constants.SELL_HAND_COMMAND);
        new SellCommand(this, Constants.SELL_ALL_COMMAND);
        new CheckCommand(this);
        new ObolsCommand(this);
        new GiveConsumableCommand(this);
        new SomniumRepairCommand(this);
        new OpenBookCommand(this);
    }

    private void initListeners() {
        playerDBEvents = new GatewayListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        RightClickListener rightClickListener = new RightClickListener(this);
        AnvilListener anvilListener = new AnvilListener(this);

        playerDBEvents.registerEvents();
        inventoryListener.registerEvents();
        rightClickListener.registerEvents();
        anvilListener.registerEvents();
    }

    public SomniumPlayerManager getSomniumPlayerManager() {
        return somniumPlayerManager;
    }

    public void sendErrorMessage(CommandSender commandSender, String error) {
        commandSender.sendMessage(ChatColor.BLUE + messagePrefix + " " + ChatColor.RED + error);
    }

    public void sendInfoMessage(CommandSender commandSender, String info) {
        commandSender.sendMessage(ChatColor.BLUE + messagePrefix + " " + ChatColor.GOLD + info);
    }

    public void sendSuccessMessage(CommandSender commandSender, String success) {
        commandSender.sendMessage(ChatColor.BLUE + messagePrefix + " " + ChatColor.GREEN + success);
    }

    private Multiplier getMaxMultiplier(Multiplier newValue, Multiplier oldValue) {
        return newValue.getValue() > oldValue.getValue() ? newValue : oldValue;
    }

    public CompletableFuture<Multiplier> extractPlayerMaxMultiplierTupleFromPermissions(Collection<Node> permissions) {
        return CompletableFuture.supplyAsync(() -> permissions.parallelStream().reduce(defaultMultiplier, (previousValue, node) -> {
            String permission = node.getKey();

            if (permission.startsWith(PERMISSION_MULTIPLIER)) {
                int lastDotPosition = permission.lastIndexOf(".");
                String stringMultiplier = permission.substring(lastDotPosition + 1);

                try {
                    double newValue = Double.parseDouble(stringMultiplier) / 100;

                    Instant expiry = node.getExpiry();
                    String expiryString = defaultMultiplier.getExpiry();
                    if (expiry != null) {
                        expiryString = dateTimeFormatter.format(LocalDateTime.ofInstant(expiry, ZoneId.systemDefault()));
                    }

                    return new Multiplier(newValue, expiryString);
                } catch (NumberFormatException event) {
                    getLogger().severe("Error while parsing string multiplier to double: " + stringMultiplier);
                }
            }

            return previousValue;
        }, this::getMaxMultiplier));
    }

    public CompletableFuture<Multiplier> forceMultiplierRefresh(Player player, Collection<Node> permissions) {

        getLogger().info("Forcing the update of multiplier for player " + player.getName());

        return extractPlayerMaxMultiplierTupleFromPermissions(permissions).thenApplyAsync((latestMultiplier) -> {
            playerMultipliers.put(player.getName(), latestMultiplier);
            return latestMultiplier;
        });
    }

    public CompletableFuture<Multiplier> forceMultiplierRefresh(Player player) {
        if (luckPerms.isPresent()) {
            return luckPerms.get().getUserManager().loadUser(player.getUniqueId()).thenCompose(
                    (user) -> forceMultiplierRefresh(player, user.getNodes())
            );
        } else {
            return CompletableFuture.completedFuture(defaultMultiplier);
        }
    }

    public Multiplier getCachedMultiplier(Player player) {
        Multiplier cachedMultiplier = playerMultipliers.get(player.getName());

        if (cachedMultiplier == null) {
            cachedMultiplier = forceMultiplierRefresh(player).join();
        }

        return cachedMultiplier;
    }

    public BigDecimal sellItems(Player player, ItemStack[] itemStacks) {
        Multiplier cachedMultiplier = getCachedMultiplier(player);
        //Getting amount
        BigDecimal coinsToGive = Arrays.stream(itemStacks).parallel().reduce(BigDecimal.ZERO, (total, itemStack) -> {
            try {
                if (itemStack != null) {
                    String permissionRequired = blocksPermission.get(itemStack.getType());
                    if (permissionRequired != null && player.hasPermission(permissionRequired)) {
                        BigDecimal itemValue = BlockPrice.getPrice(itemStack.getType());
                        itemValue = itemValue.multiply(BigDecimal.valueOf(itemStack.getAmount()));

                        total = total.add(itemValue);
                        player.getInventory().remove(itemStack);
                    }
                }
            } catch (MaterialNotSellableException ignored) {
            }
            return total;
        }, BigDecimal::add);

        coinsToGive = coinsToGive.multiply(BigDecimal.valueOf(cachedMultiplier.getValue()));
        Wallet.addCoins(player, coinsToGive);
        return coinsToGive;
    }

    public void removePlayer(String playerName) {
        playerMultipliers.remove(playerName);
    }

    // TODO getLore deprecated
    public boolean isALockedEnchantFromEnchants(ItemStack itemStack) {
        boolean isLocked = false;
        List<Component> clickedLore = itemStack.lore();

        if (clickedLore != null && !clickedLore.isEmpty()) {
            isLocked = clickedLore.parallelStream().reduce(false, (status, loreLine) -> loreLine.toString().contains("Immodificabile"), Boolean::logicalOr);
        }

        return isLocked;
    }

    public boolean hasCompatibleEnchants(ItemStack itemStack, Map<Enchantment, Integer> storedEnchants) {
        if (itemStack != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                return storedEnchants.entrySet().parallelStream().reduce(true, (valid, entry) -> {
                    Enchantment enchantment = entry.getKey();
                    Integer currentEnchantmentLevel = itemStack.getEnchantments().get(enchantment);

                    ItemMeta clonedItemMeta = itemMeta.clone();
                    clonedItemMeta.removeEnchant(enchantment);

                    return enchantment.canEnchantItem(itemStack)
                            && !clonedItemMeta.hasConflictingEnchant(enchantment)
                            && ((currentEnchantmentLevel == null && entry.getValue() == 1) || (currentEnchantmentLevel != null && currentEnchantmentLevel + 1 == entry.getValue()));
                }, Boolean::logicalAnd);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public FileManager getRiassunti() {
        return riassunti;
    }

}
