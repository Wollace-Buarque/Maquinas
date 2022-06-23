package dev.cromo29.machines.commands;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.ConfigManager;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.VaultAPI;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Fuel;
import dev.cromo29.machines.utility.Scroller;
import dev.cromo29.machines.utility.Utilitys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FuelsCommand extends DurkCommand {

    private final MachinePlugin plugin;

    public FuelsCommand(MachinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform() {

        Scroller scroller = Scroller.builder()
                .withSize(36)
                .withName("Combustíveis")
                .withItems(getItems())
                .withAllowedSlots(Utilitys.SLOTS)
                .withSkipPage(true)
                .withNextPageSlot(35)
                .withNextPageItem(new MakeItem(Material.ARROW)
                        .setName("<a>Próxima página")
                        .addLoreList("<7>(<page>/<pages>)",
                                "",
                                "<b>Botão direito para pular!",
                                "<e>Clique para mudar de página!").build())
                .withPreviousPageSlot(27)
                .withPreviousPageItem(new MakeItem(Material.ARROW)
                        .setName("<a>Página anterior")
                        .addLoreList("<7>(<page>/<pages>)",
                                "",
                                "<b>Botão direito para pular!",
                                "<e>Clique para mudar de página!").build())
                .withOnChooseItemEvent(this::onClick)
                .build();

        Utilitys.updatePages(scroller);

        scroller.open(asPlayer());

        asPlayer().playSound(asPlayer().getLocation(), Sound.CHEST_OPEN, 1, 1);
    }

    @Override
    public boolean canConsolePerform() {
        return false;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getCommand() {
        return "combustiveis";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    private List<ItemStack> getItems() {

        final List<ItemStack> items = new ArrayList<>();
        final ConfigManager fuelsFile = plugin.getFuelsFile();

        ItemStack item;
        String itemString = plugin.getConfig().getString("Settings.Fuel.Item");

        if (itemString.contains(":")) {
            int id = Integer.parseInt(itemString.split(":")[0].trim());
            int data = Integer.parseInt(itemString.split(":")[1].trim());

            item = new ItemStack(Material.getMaterial(id), 1, (short) data);
        } else item = new ItemStack(Material.getMaterial(getInt(itemString.trim())));

        for (String fuelId : fuelsFile.getConfigurationSection("Fuels")) {
            String path = "Fuels." + fuelId;

            String displayName = fuelsFile.getString(path + ".DisplayName");
            String type = fuelsFile.getString(path + ".Type");
            String liters = fuelsFile.getString(path + ".Liters");
            double price = fuelsFile.getDouble(path + ".Price");

            List<String> lore = new ArrayList<>();

            if (liters == null) liters = "1";

            Fuel fuel;

            if (type.equalsIgnoreCase("INFINITE")) fuel = new Fuel(true);
            else if (type.equalsIgnoreCase("ANYWHERE")) fuel = new Fuel(getInt(liters), true);
            else fuel = new Fuel(getInt(liters));

            for (String text : fuelsFile.getStringList(path + ".Lore")) {
                lore.add(text.replace("{price}", NumberUtil.format(price))
                        .replace("{liters}", liters));
            }

            items.add(new MakeItem(item)
                    .setName(displayName)
                    .setLore(lore)
                    .setNBTTag("fuel", fuel.isInfinite() ? "-1" : fuel.getLiters() + "")
                    .setNBTTag("fuelAnywhere", fuel.isAnyWhere() + "")
                    .setNBTTag("fuelPrice", price + "")
                    .build());
        }

        return items;
    }

    private void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();

        String fuelPrice = MakeItem.getNBTTag(currentItem, "fuelPrice");

        if (fuelPrice == null || fuelPrice.isEmpty()) return;

        int amountToBuy = !event.isShiftClick() ? 1 : 64;
        double price = getDouble(fuelPrice) * amountToBuy;

        if (getDiscount(player) != 0) price = price - (price * (getDiscount(player) / 100));

        if (!VaultAPI.getEconomy().has(player, price)) {
            plugin.getMessageManager().sendMessage(player, "Enought money");
            return;
        }

        if (getEmptySlots(player) == 0) {
            plugin.getMessageManager().sendMessage(player, "Full inventory");
            return;
        }

        Fuel fuel = plugin.getFuelManager().getSimpleFuel(currentItem);

        if (fuel == null) return;

        ItemStack fuelItem = plugin.getFuelManager().getFuelItem(fuel, amountToBuy);

        VaultAPI.getEconomy().withdrawPlayer(player, price);
        player.getInventory().addItem(fuelItem);

        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

        plugin.getMessageManager().sendMessage(player, "Purchased fuel",
                "{price}", NumberUtil.formatNumberSimple(price),
                "{amount}", amountToBuy);
    }

    private double getDiscount(Player player) {

        for (double index = 100; index > 0; index--) {

            if (player.hasPermission("29Machines.Discount." + index)) return index;

        }

        return 0;
    }

}
