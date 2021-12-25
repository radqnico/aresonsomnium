package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.CommandParser;
import it.areson.aresonsomnium.commands.admin.GiveConsumableCommand;
import it.areson.aresonsomnium.commands.admin.ObolsCommand;
import it.areson.aresonsomnium.commands.admin.SomniumAdminCommand;
import it.areson.aresonsomnium.commands.admin.SomniumGommaCommand;
import it.areson.aresonsomnium.commands.player.OpenBookCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.commands.player.StatsCommand;
import it.areson.aresonsomnium.commands.repair.SomniumRepairCommand;
import it.areson.aresonsomnium.commands.shopadmin.*;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.BlockPrice;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import it.areson.aresonsomnium.elements.Multiplier;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.listeners.*;
import it.areson.aresonsomnium.placeholders.CoinsPlaceholders;
import it.areson.aresonsomnium.placeholders.MultiplierPlaceholders;
import it.areson.aresonsomnium.players.SomniumPlayer;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.time.Duration;
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
    public HashSet<String> playersWithAutoSellActive;
    private final HashMap<Material, String> blocksPermission = new HashMap<>() {{
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
    private SomniumPlayerManager somniumPlayerManager;
    private GatewayListener playerDBEvents;
    private GommaObjectsFileReader gommaObjectsFileReader;
    private MessageManager messageManager;
    private FileManager recaps;
    private FileManager briefing;
    private LastHitPvP lastHitPvP;
    //Repair
    private HashMap<String, LocalDateTime> fullRepairTimes;
    private long fullRepairDelay;
    private Price singleRepairCoinsPrice;
    private Price singleRepairObolsPrice;
    private Price singleRepairGemsPrice;
    private HashMap<String, LocalDateTime> singleRepairTimes;
    private long singleFreeRepairDelay;


    public ShopItemsManager shopItemsManager;
    public Optional<LuckPerms> luckPerms;

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        AresonSomniumAPI.instance = this;
        registerFiles();
        initListeners();
        registerCommands();

        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(this);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);
        shopItemsManager = new ShopItemsManager(this, mySqlDBConnection);

        // Placeholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiplierPlaceholders(this).register();
            new CoinsPlaceholders(this).register();
        }
        Recaps.initRecaps(recaps);

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

        new PlayerListener(this, messageManager);
        lastHitPvP = new LastHitPvP(this);

        //Repair
        fullRepairTimes = new HashMap<>();
        singleRepairTimes = new HashMap<>();
        fullRepairDelay = getConfig().getLong("repair.full-delay-seconds");
        singleRepairCoinsPrice = new Price(getConfig().getInt("repair.cost.coins"), 0, 0);
        singleRepairObolsPrice = new Price(0, getConfig().getInt("repair.cost.obols"), 0);
        singleRepairGemsPrice = new Price(0, 0, getConfig().getInt("repair.cost.gems"));
        singleFreeRepairDelay = getConfig().getLong("repair.single-delay-seconds");

        // Autosell task
        playersWithAutoSellActive = new HashSet<>();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> playersWithAutoSellActive.parallelStream().forEach(playerName -> {
            Player player = getServer().getPlayerExact(playerName);
            if (player != null) {
                BigDecimal sold = sellItems(player, player.getInventory().getContents());
                if (sold.compareTo(BigDecimal.ZERO) > 0) {
                    messageManager.sendPlainMessage(player, "autosell-sold", Pair.of("%money%", "" + sold));
                }
            } else {
                playersWithAutoSellActive.remove(playerName);
            }
        }), 0, 300);
    }

    public LastHitPvP getLastHitPvP() {
        return lastHitPvP;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private void registerFiles() {
        messageManager = new MessageManager(this, "messages.yml");
        gommaObjectsFileReader = new GommaObjectsFileReader(this, "gommaItems.yml");
        recaps = new FileManager(this, "recaps.yml");
        briefing = new FileManager(this, "briefing.yml");
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
        new SellCommand(this, Constants.AUTO_SELL_COMMAND);
//        new CheckCommand(this);
        new ObolsCommand(this);
        new GiveConsumableCommand(this);
        new SomniumRepairCommand(this, Constants.SINGLE_FREE_REPAIR_COMMAND);
        new SomniumRepairCommand(this, Constants.SINGLE_REPAIR_COMMAND);
        new SomniumRepairCommand(this, Constants.FULL_REPAIR_COMMAND);
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
        return newValue.value() > oldValue.value() ? newValue : oldValue;
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
                    String expiryString = defaultMultiplier.expiry();
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

        coinsToGive = coinsToGive.multiply(BigDecimal.valueOf(cachedMultiplier.value()));
        Wallet.addCoins(player, coinsToGive);
        return coinsToGive;
    }

    public void removePlayer(String playerName) {
        playerMultipliers.remove(playerName);
        playersWithAutoSellActive.remove(playerName);
    }

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

    public FileManager getBriefing() {
        return briefing;
    }

    public boolean hasDamage(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable damageable && damageable.hasDamage();
    }

    public boolean hasSomethingToRepair(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (hasDamage(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canFullRepairByLastRepair(Player player) {
        String playerName = player.getName();

        if (fullRepairTimes.containsKey(playerName)) {
            LocalDateTime lastRepair = fullRepairTimes.get(playerName);
            return Duration.between(lastRepair, LocalDateTime.now()).getSeconds() >= fullRepairDelay;
        } else {
            return true;
        }
    }

    public boolean fullRepair(Player player, boolean ignoreLastRepairTime, boolean ignorePermission) {
        if (ignorePermission || player.hasPermission(Constants.FULL_REPAIR_PERMISSION)) {
            if (hasSomethingToRepair(player)) {
                if (ignoreLastRepairTime || canFullRepairByLastRepair(player)) {
                    fullRepairTimes.put(player.getName(), LocalDateTime.now());
                    Arrays.stream(player.getInventory().getContents()).parallel().forEach(this::eventuallyRepairItemStack);
                    messageManager.sendPlainMessage(player, "full-repair-success");
                    return true;
                } else {
                    messageManager.sendPlainMessage(player, "cannot-repair-yet");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "no-permissions");
        }
        return false;
    }

    public void singleRepair(Player player, CoinType coinType) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isDamageable(itemInMainHand)) {
            if (hasDamage(itemInMainHand)) {
                Price repairPrice = getSingleRepairPrice(coinType);
                SomniumPlayer somniumPlayer = getSomniumPlayerManager().getSomniumPlayer(player);
                if (somniumPlayer != null && somniumPlayer.canAfford(repairPrice)) {
                    boolean result = somniumPlayer.takePriceAmount(repairPrice);
                    if (result) {
                        singleRepairTimes.put(player.getName(), LocalDateTime.now());
                        eventuallyRepairItemStack(itemInMainHand);
                        messageManager.sendPlainMessage(player, "single-repair-success");
                    } else {
                        messageManager.sendPlainMessage(player, "generic-error");
                    }
                } else {
                    messageManager.sendPlainMessage(player, "repair-not-enough-coins");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "repair-cant-repair");
        }
    }

    public void eventuallyRepairItemStack(ItemStack itemStack) {
        if (isDamageable(itemStack)) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();
            damageable.setDamage(0);
            itemStack.setItemMeta(damageable);
        }
    }

    public boolean isDamageable(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable;
    }


    public Price getSingleRepairPrice(CoinType coinType) {
        switch (coinType) {
            case MONETE -> {
                return singleRepairCoinsPrice;
            }
            case GEMME -> {
                return singleRepairGemsPrice;
            }
            case OBOLI -> {
                return singleRepairObolsPrice;
            }
            default -> {
            }
        }
        return singleRepairCoinsPrice;
    }


    public void singleFreeRepair(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isDamageable(itemInMainHand)) {
            if (hasDamage(itemInMainHand)) {
                if (canSingleRepairByLastRepair(player)) {
                    singleRepairTimes.put(player.getName(), LocalDateTime.now());
                    eventuallyRepairItemStack(itemInMainHand);
                    messageManager.sendPlainMessage(player, "single-repair-success");
                } else {
                    messageManager.sendPlainMessage(player, "cannot-repair-yet");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "repair-cant-repair");
        }
    }


    public boolean canSingleRepairByLastRepair(Player player) {
        String playerName = player.getName();

        if (singleRepairTimes.containsKey(playerName)) {
            LocalDateTime lastRepair = singleRepairTimes.get(playerName);
            return Duration.between(lastRepair, LocalDateTime.now()).getSeconds() >= singleFreeRepairDelay;
        } else {
            return true;
        }
    }

}
