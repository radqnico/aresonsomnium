package it.areson.aresonsomnium;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
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
        DB database = DB.newEmbeddedDB(3306);
        database.start();
        database.run("CREATE DATABASE IF NOT EXISTS aresonsomnium;\n" +
                "\n" +
                "USE aresonsomnium;\n" +
                "\n" +
                "create table if not exists somniumGuis (\n" +
                "    guiName varchar(255) primary key,\n" +
                "    guiTitle varchar(255) not null,\n" +
                "    shopItems text not null\n" +
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
