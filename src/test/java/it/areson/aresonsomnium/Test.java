package it.areson.aresonsomnium;


import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {

    private static ServerMock server;
    private static AresonSomnium plugin;

    @BeforeAll
    public static void load() throws ManagedProcessException {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(3306); // 0 => autom. detect free port
        DB database = DB.newEmbeddedDB(config.build());
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
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "");

        server = MockBukkit.mock();
        plugin = MockBukkit.load(AresonSomnium.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @org.junit.jupiter.api.Test
    public void test() {
        PlayerMock player = server.addPlayer("username");
        System.out.println(plugin.getCachedMultiplier(player).left());
        assertEquals(1, 1);
    }

}
