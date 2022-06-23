package dev.cromo29.machines.listeners;

import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.machines.managers.MachineManager;
import dev.cromo29.machines.objects.Machine;
import dev.cromo29.machines.objects.Type;
import dev.cromo29.machines.service.MachineService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceEvent implements Listener {

    private final MachineManager machineManager;

    public PlaceEvent(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceMachineEvent(BlockPlaceEvent event) {

        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();

        if (!machineManager.isMachine(itemStack)) return;

        MachineService machineService = machineManager.getMachineService();
        Type type = machineManager.getMachineType(itemStack);

        Machine machine = new Machine(player.getName(), event.getBlockPlaced().getLocation(), type);
        machineService.putMachine(player.getName(), machine);

        if (machineManager.getPlugin().isEnabled())
            TXT.runAsynchronously(machineManager.getPlugin(), () -> machineManager.getStorageManager().putMachine(machine.getOwner(), machine));
        else machineManager.getStorageManager().putMachine(machine.getOwner(), machine);

        player.playSound(event.getBlockPlaced().getLocation(), Sound.CLICK, 1, 1);
    }
}
