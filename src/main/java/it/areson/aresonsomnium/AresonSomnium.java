package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.SomniumAdminCommand;
import it.areson.aresonsomnium.commands.admin.SomniumTestCommand;
import it.areson.aresonsomnium.commands.player.OpenGuiCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.listeners.SomniumPlayerDBEvents;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.items.BlockPrice;
import it.areson.aresonsomnium.shops.listener.CustomGuiEventsListener;
import it.areson.aresonsomnium.shops.listener.SetPriceInChatListener;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.Debugger;
import it.areson.aresonsomnium.utils.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

public class AresonSomnium extends JavaPlugin {

    private SomniumPlayerManager somniumPlayerManager;
    private ShopManager shopManager;
    private ShopEditor shopEditor;
    private SomniumPlayerDBEvents playerDBEvents;
    private CustomGuiEventsListener customGuiEventsListener;
    private SetPriceInChatListener setPriceInChatListener;
    private FileManager dataFile;

    private Debugger debugger;

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {
        debugger = new Debugger(this, Debugger.DebugLevel.HIGH);
        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(debugger);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);
        shopManager = new ShopManager(mySqlDBConnection, GUIS_TABLE_NAME);
        shopEditor = new ShopEditor(this);

        // Events
        initAllEvents();

        // Commands
        registerCommands();

        // Files
        registerFiles();

        // Auto Save Task interval
        // 1m  = 1200
        // 10m = 12000
        AutoSaveManager.startAutoSaveTask(this, 6000); // 5m

        // Init prices map
        BlockPrice.initPrices();

        AresonSomniumAPI.instance = this;
    }

    private void registerFiles() {
        dataFile = new FileManager(this, "data.yml");
    }

    public FileManager getDataFile() {
        return dataFile;
    }

    private void registerCommands() {
        new SomniumAdminCommand(this);
        new SomniumTestCommand(this);
        new OpenGuiCommand(this);
    }

    private void initAllEvents() {
        playerDBEvents = new SomniumPlayerDBEvents(this);
        customGuiEventsListener = new CustomGuiEventsListener(this);
        setPriceInChatListener = new SetPriceInChatListener(this);

        playerDBEvents.registerEvents();
        customGuiEventsListener.registerEvents();
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
