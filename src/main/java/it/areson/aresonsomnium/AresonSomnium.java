package it.areson.aresonsomnium;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.admin.SomniumAdminCommand;
import it.areson.aresonsomnium.commands.admin.SomniumTestCommand;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.entities.SomniumPlayerManager;
import it.areson.aresonsomnium.listeners.SomniumPlayerDBEvents;
import it.areson.aresonsomnium.utils.AutoSaveManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static it.areson.aresonsomnium.database.MySqlConfig.PLAYER_TABLE_NAME;

public class AresonSomnium extends JavaPlugin {

    private SomniumPlayerManager somniumPlayerManager;

    private SomniumPlayerDBEvents playerDBEvents;

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

        // Events
        initAllEvents();
        registerAllEvents();

        // Commands
        registerCommands();

        // Auto Save Task interval
        // 1m  = 1200
        // 10m = 12000
        AutoSaveManager.startAutoSaveTask(this, 6000); // 5m

        AresonSomniumAPI.instance = this;
    }

    private void registerCommands() {
        new SomniumAdminCommand(this);
        new SomniumTestCommand(this);
    }

    private void initAllEvents() {
        playerDBEvents = new SomniumPlayerDBEvents(this);
    }

    private void registerAllEvents() {
        playerDBEvents.registerEvents();
    }

    public SomniumPlayerManager getSomniumPlayerManager() {
        return somniumPlayerManager;
    }
}
