package dev.cromo29.machines.commands;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.machines.managers.MachineManager;
import dev.cromo29.machines.objects.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MachineCommand extends DurkCommand {

    private final MachineManager machineManager;

    public MachineCommand(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @Override
    public void perform() {

        if (isArgsLength(2)) {

            if (isConsole()) {
                warnConsoleCannotPerform();
                return;
            }

            String machineName = argAt(0);

            if (!isValidInt(argAt(1))) {
                warnNotValidNumber(argAt(1));
                return;
            }

            if (getEmptySlots(asPlayer()) <= 0) {
                machineManager.getMessageManager().sendMessage(getSender(), "Full inventory");
                return;
            }

            int amount = getInt(argAt(1));

            Type machineType = machineManager.getMachineTypeByName(machineName);

            if (machineType == null) {
                machineManager.getMessageManager().sendMessage(getSender(), "Unknown machine",
                        "{name}", machineName,
                        "{amount}", amount,
                        "&", "§");
                return;
            }

            ItemStack itemStack = machineType.getItem().clone();
            itemStack.setAmount(amount);

            asPlayer().getInventory().addItem(itemStack);

            machineManager.getMessageManager().sendMessage(getSender(), "Gived machine",
                    "{name}", machineType.getDisplayName(),
                    "{amount}", amount,
                    "&", "§");

            playSound(Sound.ITEM_PICKUP, 1, 1);

        } else if (isArgsLength(3)) {

            Player target = getPlayerAt(0);
            String machineName = argAt(1);

            if (!isValidInt(argAt(2))) {
                warnNotValidNumber(argAt(2));
                return;
            }

            if (target == null) {
                machineManager.getMessageManager().sendMessage(getSender(), "Target offline",
                        "{player}", argAt(0));
                return;
            }

            if (getEmptySlots(target) <= 0) {
                machineManager.getMessageManager().sendMessage(getSender(), "Target full inventory",
                        "{player}", target.getName());
                return;
            }

            int amount = getInt(argAt(2));

            Type machineType = machineManager.getMachineTypeByName(machineName);

            if (machineType == null) {
                machineManager.getMessageManager().sendMessage(getSender(), "Unknown machine",
                        "{name}", machineName,
                        "{amount}", amount,
                        "&", "§");
                return;
            }

            ItemStack itemStack = machineType.getItem().clone();
            itemStack.setAmount(amount);

            target.getInventory().addItem(itemStack);

            machineManager.getMessageManager().sendMessage(getSender(), "Gived machine to player",
                    "{player}", target.getName(),
                    "{name}", machineType.getDisplayName(),
                    "{amount}", amount,
                    "&", "§");

            if (isPlayer()) playSound(Sound.CLICK, 1, 1);
            playSound(target, Sound.ITEM_PICKUP, 1, 1);

        } else sendMessages("",
                " <b>⁕ <f>/" + getUsedCommand() + " <maquina> <quantidade> <e>- <7>Pegar uma máquina.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <jogador> <maquina> <quantidade> <e>- <7>Dar uma máquina.",
                "");

    }

    @Override
    public boolean canConsolePerform() {
        return true;
    }

    @Override
    public String getPermission() {
        return "29Machines.ADM";
    }

    @Override
    public String getCommand() {
        return "maquina";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }
}
