package dev.cromo29.machines.objects;

import dev.cromo29.durkcore.util.MakeItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Type {

    private final ItemStack item;
    private final String name, displayName;
    private final List<String> lore;
    private final List<ItemStack> drops;

    private final int spawnTime;
    private final long fuelLimit;
    private final double price;

    public Type(ItemStack item, String name, String displayName, List<String> lore, List<ItemStack> drops, int spawnTime, long fuelLimit, double price) {
        this.name = name;
        this.displayName = displayName;
        this.lore = lore;
        this.drops = drops;

        this.item = new MakeItem(item.clone())
                .setName(displayName)
                .setLore(lore)
                .build();

        this.spawnTime = spawnTime;
        this.fuelLimit = fuelLimit;
        this.price = price;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public int getSpawnTime() {
        return spawnTime;
    }

    public long getFuelLimit() {
        return fuelLimit;
    }

    public double getPrice() {
        return price;
    }
}
