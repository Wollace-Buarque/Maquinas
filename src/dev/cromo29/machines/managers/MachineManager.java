package dev.cromo29.machines.managers;

import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Machine;
import dev.cromo29.machines.objects.Type;
import dev.cromo29.machines.service.MachineService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineManager {

    private final MachinePlugin plugin;
    private final MachineService machineService;

    private final TypeManager typeManager;
    private final MessageManager messageManager;
    private final FuelManager fuelManager;
    private final StorageManager storageManager;

    public MachineManager(MachinePlugin plugin) {
        this.plugin = plugin;

        this.machineService = new MachineService();

        this.typeManager = new TypeManager(plugin);
        this.messageManager = new MessageManager(plugin);
        this.fuelManager = new FuelManager(plugin);
        this.storageManager = new StorageManager(plugin, machineService);
    }

    public boolean isMachine(ItemStack itemStack) {

        if (itemStack == null || !itemStack.hasItemMeta()) return false;

        Set<Type> typeSet = typeManager.getTypeSet();

        if (typeSet.isEmpty()) return false;

        for (Type type : typeSet) {
            if (type.getItem().isSimilar(itemStack)) return true;
        }

        return false;
    }

    public Type getMachineType(ItemStack itemStack) {

        if (itemStack == null) return null;

        Set<Type> typeSet = typeManager.getTypeSet();

        if (typeSet.isEmpty()) return null;

        for (Type type : typeSet) {
            if (type.getItem().isSimilar(itemStack)) return type;
        }

        return null;
    }

    public Type getMachineTypeByName(String typeName) {

        if (typeName == null) return null;

        Set<Type> typeSet = typeManager.getTypeSet();

        if (typeSet.isEmpty()) return null;

        for (Type type : typeSet) {
            if (type.getName().equalsIgnoreCase(typeName)) return type;
        }

        return null;
    }

    public void machineTask() {

        AtomicInteger id = new AtomicInteger(0);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            Set<Machine> machines = machineService.getEnabledMachines();

            if (machines.isEmpty()) return;

            if (id.get() >= machines.size() - 1) id.set(0);

            dropCache(new ArrayList<>(machines), id.get() + "");

            id.getAndIncrement();

        }, 60, 20);

    }

    private final Map<String, BukkitTask> check = new HashMap<>();

    private void dropCache(List<Machine> list, String id) {
        if (check.get(id) != null) return;

        check.put(id, plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int count = 0;

            @Override
            public void run() {
                int conta = count * 2;
                int total = list.size() - conta;

                int start = count * 2;
                if (total <= 2) {

                    for (int index = start; index < list.size(); index++) {
                        Machine machine = list.get(index);

                        machine.spawnDrop();
                    }

                    plugin.getServer().getScheduler().cancelTask(check.get(id).getTaskId());

                    check.put(id, null);

                } else {

                    for (int index = start; index < start + 2; index++) {
                        Machine machine = list.get(index);

                        machine.spawnDrop();
                    }

                }
                count++;

            }
        }, 20, 20));
    }


    public MachinePlugin getPlugin() {
        return plugin;
    }


    public MachineService getMachineService() {
        return machineService;
    }


    public TypeManager getTypeManager() {
        return typeManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public FuelManager getFuelManager() {
        return fuelManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
