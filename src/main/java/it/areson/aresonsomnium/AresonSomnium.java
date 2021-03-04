package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.*;
import it.areson.aresonsomnium.commands.player.CheckCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.commands.player.StatsCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.listeners.GatewayListener;
import it.areson.aresonsomnium.listeners.InventoryListener;
import it.areson.aresonsomnium.listeners.LuckPermsListener;
import it.areson.aresonsomnium.listeners.RightClickListener;
import it.areson.aresonsomnium.placeholders.CoinsPlaceholders;
import it.areson.aresonsomnium.placeholders.MultiplierPlaceholders;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.listener.CustomGuiEventsListener;
import it.areson.aresonsomnium.shops.listener.SetPriceInChatListener;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.Debugger;
import it.areson.aresonsomnium.utils.Pair;
import it.areson.aresonsomnium.utils.file.GommaObjectsFileReader;
import it.areson.aresonsomnium.utils.file.MessageManager;
import net.luckperms.api.LuckPerms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import static it.areson.aresonsomnium.Constants.PERMISSION_MULTIPLIER;
import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class AresonSomnium extends JavaPlugin {

    private static AresonSomnium instance;
    public Optional<LuckPerms> luckPerms;
    public HashMap<String, Double> playerMultipliers;
    private SomniumPlayerManager somniumPlayerManager;
    private ShopManager shopManager;
    private ShopEditor shopEditor;
    private GatewayListener playerDBEvents;
    private SetPriceInChatListener setPriceInChatListener;
    private GommaObjectsFileReader gommaObjectsFileReader;

    private MessageManager messages;
    private Debugger debugger;

    public static AresonSomnium getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {
        instance = this;
        playerMultipliers = new HashMap<>();

        // Files
        registerFiles();

        debugger = new Debugger(this, Debugger.DebugLevel.LOW);
        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(debugger);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);
        shopManager = new ShopManager(mySqlDBConnection, GUIS_TABLE_NAME);
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
        gommaObjectsFileReader = new GommaObjectsFileReader(this, "gommaitems.yml");
    }

    public GommaObjectsFileReader getGommaObjectsFileReader() {
        return gommaObjectsFileReader;
    }

    private void registerCommands() {
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

    public double extractPlayerMaxMultiplierFromPermissions(Player player) {
        return player.getEffectivePermissions().parallelStream().reduce(1.0, (multiplier, permissionAttachmentInfo) -> {
            double tempMultiplier = 1.0;
            String permission = permissionAttachmentInfo.getPermission();

            if (permission.startsWith(PERMISSION_MULTIPLIER)) {
                int lastDotPosition = permission.lastIndexOf(".");
                String stringMultiplier = permission.substring(lastDotPosition + 1);

                try {
                    tempMultiplier = Double.parseDouble(stringMultiplier) / 100;
                } catch (NumberFormatException event) {
                    getLogger().severe("Error while parsing string multiplier to double: " + stringMultiplier);
                }
            }

            return tempMultiplier;
        }, Double::max);
    }

    private Optional<Double> getSecondIfPresent(Optional<Double> first, Optional<Double> second) {
        return second.isPresent() ? second : first;
    }

    public Pair<Double, Duration> extractPlayerMaxMultiplierTupleFromPermissions(Player player) {
        luckPerms.ifPresent(perms -> perms.getUserManager().loadUser(player.getUniqueId()).thenApplyAsync((user) -> {

            Optional<Double> reduce = user.getNodes().parallelStream().reduce(Optional.empty(), (optionalValue, node) -> {
                String permission = node.getKey();
                System.out.println("Evaluating " + permission + ", expiry: " + node.getExpiry());

                if (permission.startsWith(PERMISSION_MULTIPLIER)) {
                    int lastDotPosition = permission.lastIndexOf(".");
                    String stringMultiplier = permission.substring(lastDotPosition + 1);

                    try {
                        double newValue = Double.parseDouble(stringMultiplier);
                        return optionalValue.map(oldValue -> Optional.of(Double.max(oldValue, newValue)).orElse(newValue));
                    } catch (NumberFormatException event) {
                        getLogger().severe("Error while parsing string multiplier to double: " + stringMultiplier);
                    }
                }

                return Optional.empty();
            }, this::getSecondIfPresent);

            System.out.println(reduce);

            return "we";
        }));

        return Pair.emptyMultiplier();
    }

    public Double forceMultiplierRefresh(Player player) {
        extractPlayerMaxMultiplierTupleFromPermissions(player);

        double multiplier = extractPlayerMaxMultiplierFromPermissions(player);
        playerMultipliers.put(player.getName(), multiplier);

        return multiplier;
    }

    public double getCachedMultiplier(Player player) {
        Double multiplier = playerMultipliers.get(player.getName());

        if (multiplier == null) {
            multiplier = forceMultiplierRefresh(player);
        }

        return multiplier;
    }

}
