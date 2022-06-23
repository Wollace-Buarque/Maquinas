package dev.cromo29.machines.commands;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.machines.managers.MachineManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FuelCommand extends DurkCommand {

    private final MachineManager machineManager;

    public FuelCommand(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @Override
    public void perform() {

        if (isArgsLength(2)) {

            if (isConsole()) {
                warnConsoleCannotPerform();
                return;
            }

            String toCheck = argAt(0);

            if (isValidBoolean(toCheck)) {

                String amount = argAt(1);

                if (!isValidInt(amount)) {
                    warnNotValidNumber(amount);
                    return;
                }

                if (getEmptySlots(asPlayer()) <= 0) {
                    machineManager.getMessageManager().sendMessage(getSender(), "Full inventory", "&", "§");
                    return;
                }

                ItemStack fuel = machineManager.getFuelManager().getFuelItem(-1, getInt(amount), false);

                asPlayer().getInventory().addItem(fuel);

                machineManager.getMessageManager().sendMessage(getSender(), "Gived infinite fuel",
                        "{amount}", amount,
                        "&", "§");

                playSound(Sound.ITEM_PICKUP, 1, 1);

            } else if (isValidInt(toCheck)) {

                String amount = argAt(1);

                if (!isValidInt(amount)) {
                    warnNotValidNumber(amount);
                    return;
                }

                if (getEmptySlots(asPlayer()) <= 0) {
                    machineManager.getMessageManager().sendMessage(getSender(), "Full inventory", "&", "§");
                    return;
                }

                ItemStack fuel = machineManager.getFuelManager().getFuelItem(getInt(toCheck), getInt(amount), false);

                asPlayer().getInventory().addItem(fuel);

                machineManager.getMessageManager().sendMessage(getSender(), "Gived fuel",
                        "{liters}", toCheck,
                        "{amount}", amount,
                        "&", "§");

            } else sendHelp();

        } else if (isArgsLength(3)) {

            Player target = getPlayerAt(0);
            String toCheck = argAt(1);

            if (target == null) {
                machineManager.getMessageManager().sendMessage(getSender(), "Target offline",
                        "{player}", argAt(0),
                        "&", "§");
                return;
            }

            if (isValidBoolean(toCheck)) {

                String amount = argAt(2);

                if (!isValidInt(amount)) {
                    warnNotValidNumber(amount);
                    return;
                }

                if (getEmptySlots(target) <= 0) {
                    machineManager.getMessageManager().sendMessage(getSender(), "Target full inventory", "&", "§");
                    return;
                }

                ItemStack fuel = machineManager.getFuelManager().getFuelItem(-1, getInt(amount), false);

                target.getInventory().addItem(fuel);

                machineManager.getMessageManager().sendMessage(getSender(), "Gived infinite fuel to player",
                        "{player}", target.getName(),
                        "{amount}", amount,
                        "&", "§");

                playSound(target, Sound.ITEM_PICKUP, 1, 1);

            } else if (isValidInt(toCheck)) {

                String amount = argAt(2);

                if (!isValidInt(amount)) {
                    warnNotValidNumber(amount);
                    return;
                }

                if (getEmptySlots(target) <= 0) {
                    machineManager.getMessageManager().sendMessage(getSender(), "Target full inventory", "&", "§");
                    return;
                }

                ItemStack fuel = machineManager.getFuelManager().getFuelItem(getInt(toCheck), getInt(amount), false);

                target.getInventory().addItem(fuel);

                machineManager.getMessageManager().sendMessage(getSender(), "Gived fuel to player",
                        "{player}", target.getName(),
                        "{liters}", toCheck,
                        "{amount}", amount,
                        "&", "§");

                playSound(target, Sound.ITEM_PICKUP, 1, 1);

            } else sendHelp();

        } else if (isArgsLength(4)) {

            Player target = getPlayerAt(0);
            String toCheck = argAt(1);

            if (target == null) {
                machineManager.getMessageManager().sendMessage(getSender(), "Target offline",
                        "{player}", argAt(0),
                        "&", "§");
                return;
            }

            if (!isValidInt(toCheck)) {
                sendHelp();
                return;
            }

            String amount = argAt(2);

            if (!isValidInt(amount)) {
                warnNotValidNumber(amount);
                return;
            }

            if (getEmptySlots(target) <= 0) {
                machineManager.getMessageManager().sendMessage(getSender(), "Target full inventory", "&", "§");
                return;
            }

            ItemStack fuel = machineManager.getFuelManager().getFuelItem(getInt(toCheck), getInt(amount), true);
            target.getInventory().addItem(fuel);

            machineManager.getMessageManager().sendMessage(getSender(), "Gived fuel to player",
                    "{player}", target.getName(),
                    "{liters}", toCheck,
                    "{amount}", amount,
                    "&", "§");

            playSound(target, Sound.ITEM_PICKUP, 1, 1);

        } else sendHelp();
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
        return "combustivel";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    private void sendHelp() {
        sendMessages("",
                " <b>⁕ <f>/" + getUsedCommand() + " <true/false> <quantidade> <e>- <7>Pegar combustivel infinito.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <litros> <quantidade> <e>- <7>Pegar combustivel com litros.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <jogador> <true/false> <quantidade> <e>- <7>Dar combustivel infinito.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <jogador> <litros> <quantidade> <e>- <7>Dar combustivel com litros.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <jogador> <litros> <quantidade> <true/false> <e>- <7>Dar combustivel com litros ativável em qualquer lugar.",
                "");
    }
}
