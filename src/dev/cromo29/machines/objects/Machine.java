package dev.cromo29.machines.objects;

import dev.cromo29.machines.MachinePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Machine {

    private static final ConcurrentHashMap<String, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private final int id;
    private final String owner;
    private boolean enabled;
    private final Location location;
    private Type type;
    private Fuel fuel;

    private long cooldown;

    public Machine(String owner, int id, Location location, Type type) {
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.id = id;
    }

    public Machine(String owner, Location location, Type type) {
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.id = ThreadLocalRandom.current().nextInt(0, 9999999);
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public boolean hasFuel() {
        return fuel != null && fuel.getLiters() > 0;
    }

    public void spawnDrop() {

        if (!enabled || fuel == null || type == null) return;

        final MachinePlugin plugin = MachinePlugin.get();

        if (!hasFuel()) {

            cooldown = 0;
            fuel = null;
            enabled = false;

            sendExpiredMessage();

            plugin.getStorageManager().updateMachineLiters(owner, this);
            return;
        }

        cooldown++;

        if (cooldown % plugin.getFuelManager().getFuelDecreaseTime() == 0 && cooldown != 0) {
            fuel.decrease();

            if (fuel.getLiters() % 5 == 0) plugin.getStorageManager().updateMachineLiters(owner, this);

        }

        if (cooldown % type.getSpawnTime() == 0 && cooldown != 0) {

            if (type.getDrops().size() > 1) Collections.shuffle(type.getDrops());

            location.getWorld().dropItem(location.clone().add(0.5, 1, 0.5), type.getDrops().get(0))
                    .setVelocity(new Vector(0, 0.25, 0));
        }

        if (cooldown % type.getSpawnTime() != 0 || cooldown == 0) return;

        int max = plugin.getFuelManager().getFuelDecreaseTime() + type.getSpawnTime();

        if (cooldown >= max) cooldown = 0;
    }


    private void sendExpiredMessage() {
        final MachinePlugin plugin = MachinePlugin.get();
        final Player player = plugin.getServer().getPlayer(owner);

        if (player == null) return;

        final long endTime = COOLDOWNS.getOrDefault(owner.toLowerCase(), 0L);

        if (System.currentTimeMillis() < endTime) return;

        plugin.getMessageManager().sendMessage(player, "Fuel expired",
                "{name}", type.getDisplayName());

        COOLDOWNS.put(owner.toLowerCase(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));
    }
}
