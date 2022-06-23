package dev.cromo29.machines.managers;

import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.ConfigManager;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Type;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeManager {

    private final MachinePlugin plugin;
    private final Set<Type> typeSet;

    public TypeManager(MachinePlugin plugin) {
        this.plugin = plugin;
        this.typeSet = new HashSet<>();

        loadTypes();
    }

    public Set<Type> getTypeSet() {
        return typeSet;
    }

    public void loadTypes() {
        typeSet.clear();

        final ConfigManager machinesFile = plugin.getMachinesFile();

        for (String machineName : machinesFile.getConfigurationSection("Machines")) {
            String path = "Machines." + machineName;

            String blockString = machinesFile.getString(path + ".Block");
            String displayName = machinesFile.getString(path + ".DisplayName");
            int cooldown = machinesFile.getInt(path + ".Cooldown");
            double price = machinesFile.getDouble(path + ".Price");
            long fuelLimit = machinesFile.getLong(path + ".Fuel limit");
            List<String> lore = new ArrayList<>();
            List<ItemStack> items = new ArrayList<>();

            for (String text : machinesFile.getStringList(path + ".Lore")) {
                lore.add(text
                        .replace("{displayName}", displayName)
                        .replace("{cooldown}", cooldown + "")
                        .replace("{fuelLimit}", fuelLimit + "")
                        .replace("{price}", NumberUtil.format(price)));
            }

            for (String itemPath : machinesFile.getStringList(path + ".Drops")) {

                ItemStack itemStack;
                try {

                    String itemID = itemPath.split(";")[0];
                    String itemName = itemPath.split(";")[1].replace("&", "§");
                    String amount = itemPath.split(";")[2];
                    String loreString = itemPath.split(";")[3].replace("&", "§");

                    boolean hasName = !itemName.equalsIgnoreCase("null");
                    boolean hasLore = !loreString.equalsIgnoreCase("null");

                    int data = 0;
                    if (itemID.contains(":")) {
                        data = NumberUtil.getInt(itemID.split(":")[1]);
                        itemID = itemID.split(":")[0];
                    }

                    itemStack = new MakeItem(Material.WEB)
                            .setMaterial(NumberUtil.getInt(itemID))
                            .setAmount(NumberUtil.getInt(amount) == 0 ? 1 : NumberUtil.getInt(amount))
                            .setData(data)
                            .build();

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (hasName) itemMeta.setDisplayName(itemName);

                    if (hasLore) {
                        List<String> itemLore = new ArrayList<>();

                        for (String text : loreString.split("::")) {
                            if (text == null) continue;

                            itemLore.add(text);
                        }

                        itemMeta.setLore(itemLore);
                    }

                    itemStack.setItemMeta(itemMeta);

                } catch (Exception exception) {
                    plugin.log(" <c>Erro ao carregar item: <f>" + itemPath + "<c>!");
                    continue;
                }

                items.add(itemStack);
            }

            ItemStack item;

            if (blockString.contains(":")) {
                String[] splitedPath = blockString.split(":");

                int id = Integer.parseInt(splitedPath[0].trim());
                int data = Integer.parseInt(splitedPath[1].contains(",")
                        ? splitedPath[1].split(",")[0].trim()
                        : splitedPath[1].trim());

                item = new ItemStack(Material.getMaterial(id), 1, (short) data);
            } else item = new ItemStack(Material.getMaterial(Integer.parseInt(blockString.trim())));

            if (item.getType() == null || !item.getType().isBlock()) {
                plugin.log(" <c>A maquina <f>" + machineName + " <c>precisa ser um bloco!");
                return;
            }

            Type type = new Type(item, machineName, displayName, lore, items, cooldown, fuelLimit, price);
            typeSet.add(type);
        }

    }
}
