package dev.cromo29.machines.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.durkcore.util.TimeFormat;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.managers.MachineManager;
import dev.cromo29.machines.objects.Fuel;
import dev.cromo29.machines.objects.Machine;
import dev.cromo29.machines.service.MachineService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InteractEvent implements Listener {

    private final MachineManager machineManager;

    public InteractEvent(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractAtMachineEvent(PlayerInteractEvent event) {

        if (event.isCancelled()) return;

        if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        MachineService machineService = machineManager.getMachineService();
        Machine machine = machineService.getMachine(block.getLocation());

        if (machine == null) return;

        if (!event.hasItem()) {
            long fuel = machine.getFuel() != null ? machine.getFuel().getLiters() : 0;
            int decreaseTime = machineManager.getPlugin().getConfig().getInt("Settings.Fuel decrease time");

            String path = machine.isEnabled() ? "Machine status on" : "Machine status off";

            machineManager.getMessageManager().sendMessage(player, path,
                    "{currentFuel}", fuel,
                    "{maxFuel}", machine.getType().getFuelLimit(),
                    "{timeLeft}", TimeFormat.format(TimeUnit.SECONDS.toMillis(decreaseTime * fuel)).replace("agora", "Nenhum"));

            return;
        }

        Fuel fuel = machineManager.getFuelManager().getFuel(event.getItem());

        if (fuel == null) return;

        if (!machine.getOwner().equalsIgnoreCase(player.getName())) {

            if (!isPlotMember(player) || !addFuel(player, machine, true)) return;

            String path = !machine.isEnabled() ? "Enabled machine" : "Restocked machine";
            if (!machine.isEnabled()) machine.setEnabled(true);

            machineManager.getMessageManager().sendMessage(player, path,
                    "{machineName}", machine.getType().getDisplayName(),
                    "{owner}", machine.getOwner(),
                    "{currentFuel}", machine.getFuel().getLiters(),
                    "{maxFuel}", machine.getType().getFuelLimit(),
                    "&", "§");

            machineManager.getStorageManager().updateMachine(machine.getOwner(), machine);
            return;
        }

        if (!addFuel(player, machine, true)) return;

        machine.setEnabled(true);

        machineManager.getMessageManager().sendMessage(player, "Enabled machine",
                "{machineName}", machine.getType().getDisplayName(),
                "{owner}", machine.getOwner(),
                "{currentFuel}", machine.getFuel().getLiters(),
                "{maxFuel}", machine.getType().getFuelLimit(),
                "&", "§");

        machineManager.getStorageManager().updateMachine(machine.getOwner(), machine);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        if (!event.hasItem()) return;

        Player player = event.getPlayer();
        MachineService machineService = machineManager.getMachineService();

        /*if (event.hasBlock()) {

            Machine machine = machineService.getMachine(event.getClickedBlock().getLocation());

            if (machine != null) return;
        }*/

        List<Machine> machineList = machineService.getMachines(player.getName());

        if (machineList.isEmpty()) return;

        Fuel fuelInHand = machineManager.getFuelManager().getFuel(player.getItemInHand());

        if (fuelInHand == null || !fuelInHand.isAnyWhere()) return;

        //event.setCancelled(true);

        List<Machine> restockedMachines = new ArrayList<>();
        for (Machine machine : machineList) {

            Fuel machineFuel = machine.getFuel() == null ? new Fuel(0) : machine.getFuel();
            long machineFuelLimit = machine.getType().getFuelLimit();

            if (machineFuel.getLiters() >= machineFuelLimit) continue;

            if (!addFuel(player, machine, false)) continue;

            machine.setEnabled(true);

            restockedMachines.add(machine);
        }

        String path = !restockedMachines.isEmpty() ? "Restocked machines" : "No machines restocked";

        machineManager.getMessageManager().sendMessage(player, path,
                "{amount}", restockedMachines.size());


        if (!restockedMachines.isEmpty()) {
            // if (!fuelInHand.isInfinite()) restockedMachines.forEach(machine -> machineManager.getStorageManager().updateMachineLiters(player.getName(), machine));

            restockedMachines.forEach(machine -> {

                if (MachinePlugin.get().isEnabled())

                    TXT.runAsynchronously(MachinePlugin.get(), () -> machineManager.getStorageManager().updateMachineLiters(machine.getOwner(), machine));

                else machineManager.getStorageManager().updateMachineLiters(machine.getOwner(), machine);

            });
        }
    }

    private boolean addFuel(Player player, Machine machine, boolean warn) {
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand.getAmount() > 1) {

            if (warn) machineManager.getMessageManager().sendMessage(player, "Fuel amount");

            return false;
        }

        Fuel fuelInHand = machineManager.getFuelManager().getFuel(itemInHand);
        Fuel machineFuel = machine.getFuel();

        if (fuelInHand == null) return false;

        long machineFuelLimit = machine.getType().getFuelLimit();

        if (fuelInHand.isInfinite()) {

            machineFuel = machineFuel == null ? new Fuel(0) : machineFuel;

            if (machineFuel.getLiters() >= machineFuelLimit) {

                if (warn) machineManager.getMessageManager().sendMessage(player, "Fuel limit",
                        "{limit}", machineFuelLimit);

                return false;
            }

            machineFuel.setLiters(machineFuelLimit);
            machine.setFuel(machineFuel);

            return true;
        }

        if (machineFuel != null) {

            if (machineFuel.getLiters() >= machineFuelLimit) {

                if (warn) machineManager.getMessageManager().sendMessage(player, "Fuel limit",
                        "{limit}", machineFuelLimit);

                return false;
            }

            long freeFuel = machineFuelLimit - machineFuel.getLiters();

            if (fuelInHand.getLiters() > freeFuel) {
                machineFuel.setLiters(machineFuel.getLiters() + freeFuel);
                machine.setFuel(machineFuel);

                long liters = fuelInHand.getLiters() - freeFuel;

                if (liters > 0) {
                    fuelInHand.setLiters(liters);

                    player.setItemInHand(machineManager.getFuelManager().getFuelItem(fuelInHand, 1));
                } else player.setItemInHand(null);

            } else {

                machineFuel.setLiters(machineFuel.getLiters() + fuelInHand.getLiters());
                machine.setFuel(machineFuel);

                player.setItemInHand(null);

            }

        } else {

            long fuelLimit = machine.getType().getFuelLimit();
            Fuel fuel = new Fuel(1);

            if (fuelInHand.getLiters() > fuelLimit) {
                fuel.setLiters(fuelLimit);
                machine.setFuel(fuel);

                long liters = fuelInHand.getLiters() - fuelLimit;

                if (liters > 0) {
                    fuelInHand.setLiters(liters);

                    player.setItemInHand(machineManager.getFuelManager().getFuelItem(fuelInHand, 1));
                } else player.setItemInHand(null);

            } else {

                fuel.setLiters(fuelInHand.getLiters());
                machine.setFuel(fuel);

                player.setItemInHand(null);

            }

        }

        return true;
    }

    private boolean isPlotMember(Player player) {
        PlotPlayer wrap = PlotPlayer.wrap(player.getUniqueId());

        if (wrap == null) return false;

        Plot currentPlot = wrap.getCurrentPlot();

        if (currentPlot == null) return false;

        return currentPlot.isAdded(player.getUniqueId()) || currentPlot.isOwner(player.getUniqueId());
    }
}
