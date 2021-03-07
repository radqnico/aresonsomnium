package it.areson.aresonsomnium;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import it.areson.aresonsomnium.utils.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AresonSomniumTest {

    private static ServerMock server;
    private static AresonSomnium plugin;

    @BeforeAll
    public static void load() throws ManagedProcessException {
        System.out.println("Loading TESTS");
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(3306);
        if(System.getProperty("os.name").equalsIgnoreCase("Linux")) {
            System.out.println("Linux OS");
            configBuilder.setDataDir("/var/lib/jenkins/tmp");
            configBuilder.setBaseDir("/var/lib/jenkins/tmp");
            configBuilder.setLibDir("/var/lib/jenkins/tmp");
        }


        DB database = DB.newEmbeddedDB(configBuilder.build());
        database.start();
        database.run("CREATE DATABASE IF NOT EXISTS aresonSomnium;" +
                "USE aresonsomnium;" +
                "create table if not exists somniumGuis (" +
                "guiName varchar(255) primary key," +
                "guiTitle varchar(255) not null," +
                "shopItems text not null" +
                ");");

        server = MockBukkit.mock();
        plugin = MockBukkit.load(AresonSomnium.class);
    }

    @AfterAll
    public static void unload() {
        System.out.println("Unloading TESTS");
        MockBukkit.unmock();
    }

    @org.junit.jupiter.api.Test
    public void testingMultiplier() {
        PlayerMock player = server.addPlayer("username");
        Pair<Double, String> cachedMultiplier = plugin.getCachedMultiplier(player);
        assertEquals(cachedMultiplier.left(), 1.0, 0.0);
        assertEquals(cachedMultiplier.right(), "Permanente");
    }

}
