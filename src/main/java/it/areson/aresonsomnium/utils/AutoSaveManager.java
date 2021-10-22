package it.areson.aresonsomnium.utils;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class AutoSaveManager {

    public static BukkitTask bukkitTask = null;

    public static void startAutoSaveTask(AresonSomnium aresonSomnium, long interval) {
        stopAutoSaveTask();
        bukkitTask = aresonSomnium.getServer().getScheduler().runTaskTimerAsynchronously(
                aresonSomnium,
                () -> Bukkit.getScheduler().runTask(
                        aresonSomnium,
                        () -> aresonSomnium.getSomniumPlayerManager().saveAll()
                ),
                0L,
                interval
        );
    }

    public static void stopAutoSaveTask() {
        if (Objects.nonNull(bukkitTask)) {
            bukkitTask.cancel();
        }
    }

}
