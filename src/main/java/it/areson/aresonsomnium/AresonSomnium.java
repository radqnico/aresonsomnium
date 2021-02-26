package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.*;
import it.areson.aresonsomnium.commands.player.CheckCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.commands.player.StatsCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.file.GommaObjectsFileReader;
import it.areson.aresonsomnium.listeners.RightClickListener;
import it.areson.aresonsomnium.listeners.InventoryListener;
import it.areson.aresonsomnium.listeners.SomniumPlayerDBEvents;
import it.areson.aresonsomnium.placeholders.CoinsPlaceholders;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.listener.CustomGuiEventsListener;
import it.areson.aresonsomnium.shops.listener.SetPriceInChatListener;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.Debugger;
import it.areson.aresonsomnium.utils.file.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

public class AresonSomnium extends JavaPlugin {

    private static AresonSomnium instance;
    private SomniumPlayerManager somniumPlayerManager;
    private ShopManager shopManager;
    private ShopEditor shopEditor;
    private SomniumPlayerDBEvents playerDBEvents;
    private CustomGuiEventsListener customGuiEventsListener;
    private SetPriceInChatListener setPriceInChatListener;
    private RightClickListener rightClickListener;
    private InventoryListener inventoryListener;

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
    }

    private void initAllEvents() {
        playerDBEvents = new SomniumPlayerDBEvents(this);
        customGuiEventsListener = new CustomGuiEventsListener(this);
        setPriceInChatListener = new SetPriceInChatListener(this);
        inventoryListener = new InventoryListener(this);
        rightClickListener = new RightClickListener(this);

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
}
