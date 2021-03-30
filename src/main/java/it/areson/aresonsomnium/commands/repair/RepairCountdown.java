package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.command.PluginCommand;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

public class RepairCountdown {

    private final HashMap<String, LocalDateTime> lastRepairTimes;

    public RepairCountdown() {
        this.lastRepairTimes = new HashMap<>();

    }

    public void setLastRepairTime(String playerName) {
        lastRepairTimes.put(playerName, LocalDateTime.now());
    }

    public Pair<Boolean, String> canRepair(String playerName) {
        if (!lastRepairTimes.containsKey(playerName)) {
            return Pair.of(true, null);
        }
        LocalDateTime lastRepairTime = lastRepairTimes.get(playerName);
        long timeToWait = AresonSomniumAPI.instance.getConfig().getLong("repair-time-seconds", 0);
        if (Duration.between(lastRepairTime, LocalDateTime.now()).getSeconds() >= timeToWait) {
            return Pair.of(true, null);
        } else {
            long remaining = Duration.between(LocalDateTime.now(), lastRepairTime.plus(timeToWait, SECONDS)).getSeconds();
            return Pair.of(false, AresonSomniumAPI.instance.getMessageManager().getPlainMessage("cannot-repair-yet", Pair.of("%seconds%", (remaining + 1) + "")));
        }
    }
}
