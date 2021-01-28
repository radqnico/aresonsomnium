package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.SomniumAdminCommand;
import it.areson.aresonsomnium.commands.admin.SomniumTestCommand;
import it.areson.aresonsomnium.commands.player.OpenGuiCommand;
import it.areson.aresonsomnium.commands.player.SellCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.listeners.CustomGuiEventsListener;
import it.areson.aresonsomnium.listeners.SomniumPlayerDBEvents;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.shops.BlockPrice;
import it.areson.aresonsomnium.shops.ShopManager;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import it.areson.aresonsomnium.utils.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;
import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

public class AresonSomnium extends JavaPlugin {

    private SomniumPlayerManager somniumPlayerManager;
    private ShopManager shopManager;
    private SomniumPlayerDBEvents playerDBEvents;
    private CustomGuiEventsListener customGuiEventsListener;
    private FileManager dataFile;

    @Override
    public void onDisable() {
        somniumPlayerManager.saveAll();
        playerDBEvents.unregisterEvents();
    }

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection(logger);
        somniumPlayerManager = new SomniumPlayerManager(mySqlDBConnection, PLAYER_TABLE_NAME);
        shopManager = new ShopManager(mySqlDBConnection, GUIS_TABLE_NAME);

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
        new SellCommand(this);
    }

    private void initAllEvents() {
        playerDBEvents = new SomniumPlayerDBEvents(this);
        customGuiEventsListener = new CustomGuiEventsListener(this, shopManager);

        playerDBEvents.registerEvents();
        customGuiEventsListener.registerEvents();
    }

    public SomniumPlayerManager getSomniumPlayerManager() {
        return somniumPlayerManager;
    }

    public ShopManager getGuiManager() {
        return shopManager;
    }
}
