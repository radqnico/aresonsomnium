package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonlib.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.elements.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

//TODO Viene usata?
public class RepairCountdown {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final HashMap<String, LocalDateTime> lastRepairTimes;

    public RepairCountdown(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
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
        //TODO Ogni volta viene riletto
        long timeToWait = aresonSomnium.getConfig().getLong("repair.delay-seconds");
        if (Duration.between(lastRepairTime, LocalDateTime.now()).getSeconds() >= timeToWait) {
            return Pair.of(true, null);
        } else {
            long remaining = Duration.between(LocalDateTime.now(), lastRepairTime.plus(timeToWait, SECONDS)).getSeconds();
            return Pair.of(false, messageManager.getMessage("cannot-repair-yet", new Substitution("%seconds%", (remaining + 1) + "")));
        }
    }

}
