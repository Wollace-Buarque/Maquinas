package dev.cromo29.machines;

import dev.cromo29.durkcore.api.DurkPlugin;
import dev.cromo29.durkcore.util.ConfigManager;
import dev.cromo29.machines.commands.*;
import dev.cromo29.machines.listeners.BreakEvent;
import dev.cromo29.machines.listeners.InteractEvent;
import dev.cromo29.machines.listeners.PlaceEvent;
import dev.cromo29.machines.managers.*;
import dev.cromo29.machines.service.MachineService;

public class MachinePlugin extends DurkPlugin {

    private MachineManager machineManager;

    private ConfigManager machinesFile, messagesFile, fuelsFile;

    @Override
    public void onStart() {
        this.machinesFile = new ConfigManager(this, "machines.yml");
        this.messagesFile = new ConfigManager(this, "messages.yml");
        this.fuelsFile = new ConfigManager(this, "fuels.yml");

        saveDefaultConfig();

        if (!getConfig().getBoolean("MySQL.Activate")) {
            log(" <c>O plugin nao funciona sem o MySQL!");
            disablePlugin(this);
            return;
        }

        this.machineManager = new MachineManager(this);

        if (getConfig().getBoolean("Transfer.Activate")) {
            getStorageManager().createTable(getConfig().getString("Transfer.To table"));
            getStorageManager().transfer();
            return;
        }

        registerCommands(new MachineCommand(machineManager), new MachinesCommand(this), new FuelCommand(machineManager), new FuelsCommand(this));
        setListeners(new PlaceEvent(machineManager), new BreakEvent(machineManager), new InteractEvent(machineManager));

        machineManager.getTypeManager().loadTypes();

        this.getServer().getScheduler().runTaskLater(this, () -> machineManager.getStorageManager().loadMachines(), 60);

        machineManager.machineTask();
    }

    @Override
    public void onStop() {

        if (getConfig().getBoolean("MySQL.Activate")) getStorageManager().closeConnection();

    }

    public static MachinePlugin get() {
        return getPlugin(MachinePlugin.class);
    }


    public ConfigManager getMachinesFile() {
        return machinesFile;
    }

    public ConfigManager getMessagesFile() {
        return messagesFile;
    }

    public ConfigManager getFuelsFile() {
        return fuelsFile;
    }


    public MachineService getMachineService() {
        return machineManager.getMachineService();
    }


    public MachineManager getMachineManager() {
        return machineManager;
    }

    public TypeManager getTypeManager() {
        return machineManager.getTypeManager();
    }

    public MessageManager getMessageManager() {
        return machineManager.getMessageManager();
    }

    public StorageManager getStorageManager() {
        return machineManager.getStorageManager();
    }

    public FuelManager getFuelManager() {
        return machineManager.getFuelManager();
    }

}
