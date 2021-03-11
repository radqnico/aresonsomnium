package it.areson.aresonsomnium;

import elements.Multiplier;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.*;
import it.areson.aresonsomnium.commands.newcommands.*;
import it.areson.aresonsomnium.commands.player.CheckCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.commands.player.StatsCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.economy.shops.guis.ShopEditor;
import it.areson.aresonsomnium.economy.shops.guis.ShopManager;
import it.areson.aresonsomnium.economy.shops.items.BlockPrice;
import it.areson.aresonsomnium.economy.shops.listener.CustomGuiEventsListener;
import it.areson.aresonsomnium.economy.shops.listener.SetPriceInChatListener;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.listeners.GatewayListener;
import it.areson.aresonsomnium.listeners.InventoryListener;
import it.areson.aresonsomnium.listeners.LuckPermsListener;
import it.areson.aresonsomnium.listeners.RightClickListener;
import it.areson.aresonsomnium.placeholders.CoinsPlaceholders;
import it.areson.aresonsomnium.placeholders.MultiplierPlaceholders;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.Debugger;
import it.areson.aresonsomnium.utils.file.GommaObjectsFileReader;
import it.areson.aresonsomnium.utils.file.MessageManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static it.areson.aresonsomnium.Constants.PERMISSION_MULTIPLIER;
import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class AresonSomnium extends JavaPlugin {

    private final Multiplier defaultMultiplier = new Multiplier();
    private final long jollyEventNumber = -1;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final HashMap<Material, String> blocksPermission = new HashMap<Material, String>() {{
        put(Material.COBBLESTONE, Constants.PERMISSION_SELVA);
        put(Material.NETHERRACK, Constants.PERMISSION_ANTINFERNO);
        put(Material.COAL_BLOCK, Constants.PERMISSION_SECONDO_GIRONE);
        put(Material.RED_NETHER_BRICKS, Constants.PERMISSION_QUARTO_GIRONE);
        put(Material.MAGMA_BLOCK, Constants.PERMISSION_SESTO_GIRONE);
        put(Material.RED_CONCRETE, Constants.PERMISSION_OTTAVO_GIRONE);
        put(Material.ANDESITE, Constants.PERMISSION_ANTIPURGATORIO);
        put(Material.POLISHED_ANDESITE, Constants.PERMISSION_PRIMA_CORNICE);
        put(Material.DIORITE, Constants.PERMISSION_TERZA_CORNICE);
        put(Material.POLISHED_DIORITE, Constants.PERMISSION_QUINTA_CORNICE);
        put(Material.LIME_CONCRETE, Constants.PERMISSION_SESTA_CORNICE);
        put(Material.PRISMARINE, Constants.PERMISSION_PRIMO_CIELO);
        put(Material.PRISMARINE_BRICKS, Constants.PERMISSION_TERZO_CIELO);
        put(Material.QUARTZ_BLOCK, Constants.PERMISSION_QUINTO_CIELO);
        put(Material.CHISELED_QUARTZ_BLOCK, Constants.PERMISSION_SETTIMO_CIELO);
    }};
    public Optional<LuckPerms> luckPerms;
    public HashMap<String, Multiplier> playerMultipliers;
    private SomniumPlayerManager somniumPlayerManager;
    private ShopManager shopManager;
    private ShopEditor shopEditor;
    private GatewayListener playerDBEvents;
    private SetPriceInChatListener setPriceInChatListener;
    private GommaObjectsFileReader gommaObjectsFileReader;
    private MessageManager messages;
    private Debugger debugger;

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {
        playerMultipliers = new HashMap<>();

        // Files
        registerFiles();

        debugger = new Debugger(this, Debugger.DebugLevel.LOW);
        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(this, debugger);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);
        shopManager = new ShopManager(this, mySqlDBConnection, GUIS_TABLE_NAME);
        shopEditor = new ShopEditor(this);

        // Files
        registerFiles();
        // Events
        initAllEvents();
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

        AresonSomniumAPI.instance = this;
    }

    public MessageManager getMessageManager() {
        return messages;
    }

    private void registerFiles() {
        messages = new MessageManager(this, "messages.yml");
        gommaObjectsFileReader = new GommaObjectsFileReader(this, "gommaItems.yml");
    }

    public GommaObjectsFileReader getGommaObjectsFileReader() {
        return gommaObjectsFileReader;
    }

    private void registerCommands() {

        CommandTree testTreeCommand = new CommandTree(this, new TestTreeCommand());
        HelloWorldCommand helloWorldCommand = new HelloWorldCommand();
        HelloNicoCommand helloNicoCommand = new HelloNicoCommand();
        helloNicoCommand.addChild(new SecondLevelCommand2("second"));
        helloWorldCommand.addChild(new SecondLevelCommand1("second"));
        testTreeCommand.getRoot().addChild(helloWorldCommand);
        testTreeCommand.getRoot().addChild(helloNicoCommand);


        new SomniumAdminCommand(this);
        new SomniumTestCommand(this);
        new OpenGuiCommand(this);
        new StatsCommand(this);
        new SomniumGommaCommand(this);
        new SellCommand(this, Constants.SELL_HAND_COMMAND);
        new SellCommand(this, Constants.SELL_ALL_COMMAND);
        new CheckCommand(this);
        new ObolsCommand(this);
        new GiveConsumableCommand(this);
    }

    private void initAllEvents() {
        playerDBEvents = new GatewayListener(this);
        CustomGuiEventsListener customGuiEventsListener = new CustomGuiEventsListener(this);
        setPriceInChatListener = new SetPriceInChatListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        RightClickListener rightClickListener = new RightClickListener(this);

        playerDBEvents.registerEvents();
        customGuiEventsListener.registerEvents();
        inventoryListener.registerEvents();
        rightClickListener.registerEvents();
    }

    public SomniumPlayerManager getSomniumPlayerManager() {
        return somniumPlayerManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public ShopEditor getShopEditor() {
        return shopEditor;
    }

    public SetPriceInChatListener getSetPriceInChatListener() {
        return setPriceInChatListener;
    }

    public void sendErrorMessage(CommandSender commandSender, String error) {
        commandSender.sendMessage(ChatColor.BLUE + "[Somnium] " + ChatColor.RED + error);
    }

    public void sendInfoMessage(CommandSender commandSender, String info) {
        commandSender.sendMessage(ChatColor.BLUE + "[Somnium] " + ChatColor.GOLD + info);
    }

    public void sendSuccessMessage(CommandSender commandSender, String success) {
        commandSender.sendMessage(ChatColor.BLUE + "[Somnium] " + ChatColor.GREEN + success);
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

    public CompletableFuture<Multiplier> forceMultiplierRefresh(Player player, Collection<Node> permissions, long eventNumber) {
        getLogger().info("Forcing the update of multiplier for player " + player.getName());
        CompletableFuture<Multiplier> multiplierFuture = extractPlayerMaxMultiplierTupleFromPermissions(permissions);

        return multiplierFuture.thenApplyAsync((maybeNewMultiplier) -> {
            String playerName = player.getName();

            Multiplier actualMultiplier = playerMultipliers.get(playerName);

            System.out.println(eventNumber == jollyEventNumber);
            System.out.println(actualMultiplier == null);
            if(actualMultiplier != null) {
                System.out.println(actualMultiplier.getEventNumber() <= eventNumber);
            }

            if (eventNumber == jollyEventNumber || actualMultiplier == null || actualMultiplier.getEventNumber() <= eventNumber) {
                long realEventNumber = eventNumber;

                if (realEventNumber == jollyEventNumber) {
                    realEventNumber = 0;
                }

                System.out.println("Real event number: " + realEventNumber);

                maybeNewMultiplier.setEventNumber(realEventNumber);
                playerMultipliers.put(player.getName(), maybeNewMultiplier);
                return maybeNewMultiplier;
            }

            return actualMultiplier;
        });
    }

    public CompletableFuture<Multiplier> forceMultiplierRefresh(Player player, long eventNumber) {
        if (luckPerms.isPresent()) {
            return luckPerms.get().getUserManager().loadUser(player.getUniqueId()).thenCompose(
                    (user) -> forceMultiplierRefresh(player, user.getNodes(), eventNumber)
            );
        } else {
            return CompletableFuture.completedFuture(defaultMultiplier);
        }
    }

    public CompletableFuture<Multiplier> forceMultiplierRefresh(Player player) {
        return forceMultiplierRefresh(player, jollyEventNumber);
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

}
