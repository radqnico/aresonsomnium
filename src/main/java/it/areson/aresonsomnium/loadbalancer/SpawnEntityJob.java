package it.areson.aresonsomnium.loadbalancer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class SpawnEntityJob implements Job {

    private final Location location;
    private final EntityType entityType;

    public SpawnEntityJob(Location location, EntityType entityType) {
        this.location = location;
        this.entityType = entityType;
    }

    @Override
    public void compute() {
        location.getWorld().spawnEntity(location, entityType);
    }
}