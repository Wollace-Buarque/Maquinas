package dev.cromo29.machines.commands;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.VaultAPI;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Type;
import dev.cromo29.machines.utility.Scroller;
import dev.cromo29.machines.utility.Utilitys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MachinesCommand extends DurkCommand {

    private final MachinePlugin plugin;

    public MachinesCommand(MachinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform() {

        Scroller scroller = Scroller.builder()
                .withSize(36)
                .withName("Máquinas")
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
        return true;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getCommand() {
        return "maquinas";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    private List<ItemStack> getItems() {

        List<ItemStack> items = new ArrayList<>();

        List<Type> typeList = new ArrayList<>(plugin.getTypeManager().getTypeSet());
        typeList.sort(Comparator.comparing(Type::getPrice));

        for (Type type : typeList) {

            items.add(new MakeItem(type.getItem())
                    .setName(type.getDisplayName())
                    .setLore(type.getLore())
                    .setNBTTag("machineName", type.getName())
                    //.setNBTTag("machinePrice", type.getPrice() + "")
                    .build());
        }

        return items;
    }

    private void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        String machineName = MakeItem.getNBTTag(event.getCurrentItem(), "machineName");

        if (machineName == null || machineName.isEmpty()) return;

        int amountToBuy = !event.isShiftClick() ? 1 : 64;

        Type type = plugin.getMachineManager().getMachineTypeByName(machineName);

        if (type == null) return;

        double price = type.getPrice() * amountToBuy;

        if (getDiscount(player) != 0) price = price - (price * (getDiscount(player) / 100));

        if (!VaultAPI.getEconomy().has(player, price)) {
            plugin.getMessageManager().sendMessage(player, "Enought money");
            return;
        }

        if (getEmptySlots(player) == 0) {
            plugin.getMessageManager().sendMessage(player, "Full inventory");
            return;
        }

        ItemStack toAdd = type.getItem().clone();
        toAdd.setAmount(amountToBuy);

        VaultAPI.getEconomy().withdrawPlayer(player, price);
        player.getInventory().addItem(toAdd);

        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

        plugin.getMessageManager().sendMessage(player, "Purchased machine",
                "{displayName}", type.getDisplayName(),
                "{price}", NumberUtil.formatNumberSimple(price),
                "{amount}", amountToBuy,
                "&", "§");
    }

    private double getDiscount(Player player) {

        for (double index = 100; index > 0; index--) {

            if (player.hasPermission("29Machines.Discount." + index)) return index;

        }

        return 0;
    }

}
