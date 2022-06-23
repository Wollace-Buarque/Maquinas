package dev.cromo29.machines.listeners;

import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.machines.managers.MachineManager;
import dev.cromo29.machines.objects.Machine;
import dev.cromo29.machines.service.MachineService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakEvent implements Listener {

    private final MachineManager machineManager;

    public BreakEvent(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakMachineEvent(BlockBreakEvent event) {

        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        MachineService machineService = machineManager.getMachineService();
        Machine machine = machineService.getMachine(location);

        if (machine == null) return;

        event.setCancelled(true);

        if (!machine.getOwner().equalsIgnoreCase(player.getName()) && !player.hasPermission("29Machines.ADM")) {
            machineManager.getMessageManager().sendMessage(player, "Only owner");
            return;
        }

        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand.getType() != Material.DIAMOND_PICKAXE || !itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            machineManager.getMessageManager().sendMessage(player, "Only with silk");
            return;
        }

        if (machine.isEnabled()) {
            machineManager.getMessageManager().sendMessage(player, "Break while enabled");
            return;
        }

        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, machine.getType().getItem());

        machineService.removeMachine(machine.getOwner(), machine);

        if (machineManager.getPlugin().isEnabled())
            TXT.runAsynchronously(machineManager.getPlugin(), () -> machineManager.getStorageManager().removeMachine(machine.getOwner(), machine.getId()));
        else machineManager.getStorageManager().removeMachine(machine.getOwner(), machine.getId());

        player.playSound(player.getLocation(), Sound.BURP, 1, 1);
    }

}
