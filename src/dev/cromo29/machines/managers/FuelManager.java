package dev.cromo29.machines.managers;

import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Fuel;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FuelManager {

    private final MachinePlugin plugin;
    private final int fuelDecreaseTime;

    public FuelManager(MachinePlugin plugin) {
        this.plugin = plugin;
        this.fuelDecreaseTime = plugin.getConfig().getInt("Settings.Fuel decrease time");
    }


    public Fuel getSimpleFuel(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        FileConfiguration config = plugin.getConfig();

        String itemString = config.getString("Settings.Fuel.Item");

        int id;
        int data = 0;
        if (itemString.contains(":")) {
            id = Integer.parseInt(itemString.split(":")[0].trim());
            data = Integer.parseInt(itemString.split(":")[1].trim());

        } else id = Integer.parseInt(itemString.trim());

        if (id != itemStack.getTypeId() || data != itemStack.getDurability()) return null;

        String fuelTag = MakeItem.getNBTTag(itemStack, "fuel");
        String anyWhere = MakeItem.getNBTTag(itemStack, "fuelAnywhere");

        if (fuelTag == null || fuelTag.isEmpty()) return null;

        Fuel fuel;

        if (fuelTag.equals("-1")) fuel = new Fuel(true);
        else {
            fuel = new Fuel(Integer.parseInt(fuelTag));

            if (anyWhere != null && anyWhere.equalsIgnoreCase("true")) fuel.setAnyWhere(true);
        }

        return fuel;
    }

    public Fuel getFuel(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        FileConfiguration config = plugin.getConfig();

        String itemString = config.getString("Settings.Fuel.Item");

        int id;
        int data = 0;
        if (itemString.contains(":")) {
            id = Integer.parseInt(itemString.split(":")[0].trim());
            data = Integer.parseInt(itemString.split(":")[1].trim());

        } else id = Integer.parseInt(itemString.trim());

        if (id != itemStack.getTypeId() || data != itemStack.getDurability()) return null;

        String fuelTag = MakeItem.getNBTTag(itemStack, "fuel");
        String anyWhere = MakeItem.getNBTTag(itemStack, "fuelAnywhere");

        if (fuelTag == null || fuelTag.isEmpty()) return null;

        Fuel fuel;

        if (fuelTag.equals("-1")) fuel = new Fuel(true);
        else {
            fuel = new Fuel(Integer.parseInt(fuelTag));

            if (anyWhere != null && anyWhere.equalsIgnoreCase("true")) fuel.setAnyWhere(true);
        }

        return fuel;
    }

    public ItemStack getFuelItem(Fuel fuel, int amount) {

        FileConfiguration config = plugin.getConfig();

        String displayName = config.getString("Settings.Fuel.DisplayName");
        List<String> lore = new ArrayList<>();

        ItemStack item;
        String itemString = config.getString("Settings.Fuel.Item");

        if (itemString.contains(":")) {
            int id = Integer.parseInt(itemString.split(":")[0].trim());
            int data = Integer.parseInt(itemString.split(":")[1].trim());

            item = new ItemStack(Material.getMaterial(id), amount, (short) data);
        } else item = new ItemStack(Material.getMaterial(Integer.parseInt(itemString.trim())), amount);

        String liters = config.getString("Settings.Liters");

        if (liters != null && !fuel.isInfinite()) {
            liters = liters.replace("{x}", fuel.getLiters() + "")
                    .replace("{anyWhere}", fuel.isAnyWhere() ? config.getString("Settings.AnyWhere") : "");
        }

        String infinite = config.getString("Settings.Infinite");

        for (String text : config.getStringList("Settings.Fuel.Lore")) {

            if (fuel.isInfinite()) text = text.replace("{fuel}", infinite);
            else if (liters != null) text = text.replace("{fuel}", liters);

            lore.add(text);
        }

        if (displayName.contains("{fuelType}")) {

            String toReplace = config.getString("Settings.Infinite name");

            if (!fuel.isInfinite() && !fuel.isAnyWhere()) toReplace = config.getString("Settings.Normal name");
            else if (fuel.isAnyWhere()) toReplace = config.getString("Settings.AnyWhere name");

            displayName = displayName.replace("{fuelType}", toReplace);
        }

        item = new MakeItem(item.clone())
                .setName(displayName)
                .setLore(lore)
                .setNBTTag("fuel", fuel.isInfinite() ? "-1" : fuel.getLiters() + "")
                .setNBTTag("fuelAnywhere", fuel.isAnyWhere() + "")
                .build();

        return item;
    }

    public ItemStack getFuelItem(int liters, int amount, boolean anyWhere) {

        final FileConfiguration config = plugin.getConfig();

        String displayName = config.getString("Settings.Fuel.DisplayName");
        List<String> lore = new ArrayList<>();

        ItemStack item;
        String itemString = config.getString("Settings.Fuel.Item");

        if (itemString.contains(":")) {
            int id = Integer.parseInt(itemString.split(":")[0].trim());
            int data = Integer.parseInt(itemString.split(":")[1].trim());

            item = new ItemStack(Material.getMaterial(id), amount, (short) data);
        } else item = new ItemStack(Material.getMaterial(Integer.parseInt(itemString.trim())), amount);

        String litersString = config.getString("Settings.Liters")
                .replace("{x}", liters + "");

        String infinite = config.getString("Settings.Infinite");

        for (String text : config.getStringList("Settings.Fuel.Lore")) {

            if (liters == -1) text = text.replace("{fuel}", infinite);
            else {
                text = text.replace("{fuel}", litersString)
                        .replace("{anyWhere}", anyWhere ? config.getString("Settings.AnyWhere") : "");
            }

            lore.add(text);
        }

        if (displayName.contains("{fuelType}")) {

            String toReplace = config.getString("Settings.Infinite name");

            if (liters != -1 && !anyWhere) toReplace = config.getString("Settings.Normal name");
            else if (anyWhere) toReplace = config.getString("Settings.AnyWhere name");

            displayName = displayName.replace("{fuelType}", toReplace);
        }

        item = new MakeItem(item.clone())
                .setName(displayName)
                .setLore(lore)
                .setNBTTag("fuel", liters + "")
                .setNBTTag("fuelAnywhere", anyWhere + "")
                .build();

        return item;
    }

    public int getFuelDecreaseTime() {
        return fuelDecreaseTime;
    }
}
