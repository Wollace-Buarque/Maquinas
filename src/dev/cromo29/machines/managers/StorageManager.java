package dev.cromo29.machines.managers;

import dev.cromo29.durkcore.specificutils.LocationUtil;
import dev.cromo29.machines.MachinePlugin;
import dev.cromo29.machines.objects.Fuel;
import dev.cromo29.machines.objects.Machine;
import dev.cromo29.machines.objects.Type;
import dev.cromo29.machines.service.MachineService;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {

    private final MachinePlugin plugin;
    private final MachineService machineService;

    private final String machineTable;

    private Connection connection;

    public StorageManager(MachinePlugin plugin, MachineService machineService) {
        this.plugin = plugin;
        this.machineService = machineService;

        this.machineTable = plugin.getConfig().getString("MySQL.Table");

        openConnection();
        createTable(machineTable);
    }

    public void loadMachines() {

        if (connection == null) return;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + machineTable + "`;");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String owner = resultSet.getString("owner");
                int machineId = resultSet.getInt("machineId");
                Location location = LocationUtil.unserializeLocation(resultSet.getString("location"));
                String type = resultSet.getString("type");
                boolean enabled = resultSet.getBoolean("enabled");
                int liters = resultSet.getInt("liters");

                Type machineType = plugin.getTypeManager().getTypeSet().stream()
                        .filter(typeFilter -> typeFilter.getName().equalsIgnoreCase(type))
                        .findFirst().orElse(null);

                if (machineType == null) {
                    plugin.log(" <c>Maquina de <f>" + owner + " <c>nao possui um tipo valido! Tipo: <f>" + type);
                    continue;
                }

                Machine machine = new Machine(owner, machineId, location, machineType);
                machine.setEnabled(enabled);
                machine.setFuel(new Fuel(liters));

                machineService.putMachine(owner, machine);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void updateMachine(String user, Machine machine) {
        user = user.toLowerCase();

        checkConnection();

        try {

            String statement = "UPDATE `" + machineTable + "` SET `enabled` = ?, `location` = ?, `type` = ?, `liters` = ? WHERE `owner` = ? AND `machineId` = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            long liters = machine.getFuel() == null ? 0 : machine.getFuel().getLiters();

            preparedStatement.setBoolean(1, machine.isEnabled());
            preparedStatement.setString(2, LocationUtil.serializeSimpleLocation(machine.getLocation()));
            preparedStatement.setString(3, machine.getType().getName());
            preparedStatement.setLong(4, liters);
            preparedStatement.setString(5, user.toLowerCase());
            preparedStatement.setInt(6, machine.getId());

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    public void updateMachineLiters(String user, Machine machine) {
        user = user.toLowerCase();

        checkConnection();

        try {

            String statement = "UPDATE `" + machineTable + "` SET `enabled` = ?, `liters` = ? WHERE `owner` = ? AND `machineId` = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            long liters = machine.getFuel() == null ? 0 : machine.getFuel().getLiters();

            preparedStatement.setBoolean(1, machine.isEnabled());
            preparedStatement.setLong(2, liters);
            preparedStatement.setString(3, user.toLowerCase());
            preparedStatement.setInt(4, machine.getId());

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    public void putMachine(String user, Machine machine) {
        putMachine(user, machineTable, machine);
    }

    public void putMachine(String user, String table, Machine machine) {
        user = user.toLowerCase();

        checkConnection();

        try {

            String statement = "INSERT INTO `" + table + "` (owner, machineId, enabled, location, type, liters) VALUES (?, ?, ?, ?, ?, ?);";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            long liters = machine.getFuel() == null ? 0 : machine.getFuel().getLiters();

            preparedStatement.setString(1, user.toLowerCase());
            preparedStatement.setInt(2, machine.getId());
            preparedStatement.setBoolean(3, machine.isEnabled());
            preparedStatement.setString(4, LocationUtil.serializeSimpleLocation(machine.getLocation()));
            preparedStatement.setString(5, machine.getType().getName());
            preparedStatement.setLong(6, liters);

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void removeMachine(String user, int machineId) {
        user = user.toLowerCase();

        checkConnection();

        try {

            String statement = "DELETE FROM `" + machineTable + "` WHERE `owner` = ? AND `machineId` = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            preparedStatement.setString(1, user.toLowerCase());
            preparedStatement.setInt(2, machineId);

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private List<Machine> fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            List<Machine> machineList;

            machineList = (List<Machine>) dataInput.readObject();

            dataInput.close();
            return machineList;
        } catch (ClassNotFoundException exception) {
            throw new IOException("Unable to decode class type.", exception);
        }
    }

    public void transfer() {

        checkConnection();

        final String fromTable = plugin.getConfig().getString("Transfer.From table");
        final String toTable = plugin.getConfig().getString("Transfer.To table");

        final Map<String, List<Machine>> cachedMachines = new HashMap<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + fromTable + "`");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String owner = resultSet.getString("owner");
                String serializedMachines = resultSet.getString("machines");

                List<Machine> machineList = fromBase64(serializedMachines);

                cachedMachines.put(owner.toLowerCase(), machineList);
            }

        } catch (SQLException | IOException exception) {

            String cause = exception.getMessage();

            if (exception.getCause() != null) cause = exception.getCause().getMessage();

            if (cause.contains("Column 'machines' not found.")) {
                plugin.log(" <c>A tabela nao possui o formato antigo!");
                plugin.disablePlugin(plugin);
                return;
            }

            exception.printStackTrace();
        }

        cachedMachines.forEach((owner, machines) -> machines.forEach(machine -> putMachine(owner, toTable, machine)));

        int amount = 0;
        for (List<Machine> machines : cachedMachines.values()) {
            amount += machines.size();
        }

        plugin.log(" <a>Transferencia de <f>" + amount + " <a>maquinas concluidas!");
        plugin.log(" <a>Plugin desligado!");

        plugin.getConfig().set("Transfer.Activate", false);
        plugin.saveConfig();

        plugin.disablePlugin(plugin);
    }


    public void openConnection() {

        final FileConfiguration config = plugin.getConfig();

        String user = config.getString("MySQL.User");
        String password = config.getString("MySQL.Password");
        String host = config.getString("MySQL.Host");
        int port = config.getInt("MySQL.Port");
        String database = config.getString("MySQL.Database");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void createTable(String table) {
        if (connection == null) return;

        try {

            String statement = "CREATE TABLE IF NOT EXISTS `" + table + "` (`id` INT NOT NULL AUTO_INCREMENT, `owner` TEXT, `machineId` INT, `enabled` BOOLEAN, `location` TEXT, `type` TEXT, `liters` LONG, PRIMARY KEY (`id`));";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void checkConnection() {
        try {

            if (connection.isClosed()) openConnection();

        } catch (SQLException ignored) {
        }
    }

    public void closeConnection() {
        try {
            if (connection == null || connection.isClosed()) return;
        } catch (SQLException throwables) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
