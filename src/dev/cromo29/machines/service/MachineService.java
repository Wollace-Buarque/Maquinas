package dev.cromo29.machines.service;

import dev.cromo29.machines.objects.Machine;
import org.bukkit.Location;

import java.util.*;

public class MachineService {

    private final Map<String, List<Machine>> machineMap;

    public MachineService() {
        this.machineMap = new HashMap<>();
    }

    public boolean hasMachine(String user) {
        return machineMap.containsKey(user.toLowerCase());
    }

    public List<Machine> getMachines(String user) {
        return machineMap.getOrDefault(user.toLowerCase(), new ArrayList<>());
    }

    public List<Machine> getMachines() {
        List<Machine> machineList = new ArrayList<>();

        for (List<Machine> machines : machineMap.values()) machineList.addAll(machines);

        return machineList;
    }

    public Set<Machine> getEnabledMachines() {
        Set<Machine> machines = new HashSet<>();
        for (Machine machine : getMachines()) {
            if (machine.isEnabled()) machines.add(machine);
        }

        return machines;
    }

    public Machine getMachine(Location location) {
        for (Machine machine : getMachines()) {
            if (machine.getLocation().equals(location)) return machine;
        }

        return null;
    }

    public void putMachine(String user, List<Machine> machines) {
        List<Machine> machinesClone = getMachines(user);

        machinesClone.addAll(machines);

        machineMap.put(user.toLowerCase(), machinesClone);
    }

    public void putMachine(String user, Machine machine) {
        List<Machine> machines = getMachines(user);

        machines.add(machine);

        machineMap.put(user.toLowerCase(), machines);
    }

    public void removeMachine(String user, Machine machine) {
        List<Machine> machines = getMachines(user);

        machines.remove(machine);

        if (machines.isEmpty()) machineMap.remove(user.toLowerCase());
        else machineMap.put(user.toLowerCase(), machines);
    }
}
