package darkkronicle.github.io.cloudfight.commands.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.buidler.MapEditContainer;
import darkkronicle.github.io.cloudfight.buidler.MapObjects;
import darkkronicle.github.io.cloudfight.commands.Command;
import darkkronicle.github.io.cloudfight.commands.SubCommand;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.game.maps.CaptureMap;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.ArmorStandBuilder;
import darkkronicle.github.io.cloudfight.utility.WorldManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapCommand extends Command {

    public MapCommand(CloudFight plugin) {
        super(plugin, "map", "cloudfight.map");
        subCommands = new ArrayList<>();

        // Save command
        subCommands.add(SubCommand.simple(plugin, this, "save", null, (sender, command, label, args) -> {
            Player player = (Player) sender;
            String name;
            if (args.length == 0) {
                name = player.getWorld().getName();
            } else {
                name = args[0];
            }
            boolean success = plugin.mapEditPool.unload(name);
            if (success) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.GREEN + "Map successfully unloaded!");
            } else {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Map couldn't be unloaded! Make sure you typed in the name correctly.");
            }
        }));

        subCommands.add(SubCommand.simple(plugin, this, "menu", null, ((sender, command, label, args) -> {
            Player player = (Player) sender;
            MapObjects.openInventory(player);
        })));

        // Create command
        subCommands.add(SubCommand.simple(plugin, this, "create", null, (sender, command, label, args) -> {
            String name = args[0];
            String from;
            if (args.length == 2) {
                from = args[1];
            } else {
                from = "default";
            }
            if (plugin.getMapPath().resolve(name).toFile().exists()) {
                sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "That map already exists!");
                return;
            }
            WorldManagement.copyDirectory(plugin.getMapPath().resolve(from).toFile(), new File(plugin.getMapPath().toFile().getPath() + "//" + name));
            // TODO Fix
            /* try {
                Map.save(Map.getNormalGson(), plugin.getMapPath().resolve(from).resolve("config_json.json").toFile(), new Map.MapConfiguration());
            } catch (IOException e) {
                sender.sendMessage(CloudFight.PREFIX + CloudFight.color("&cLooks like something went wrong..."));
                e.printStackTrace();
                return;
            } */

            sender.sendMessage(CloudFight.PREFIX + CloudFight.color("&aMap " + name + " has been created!"));
        }));

        subCommands.add(SubCommand.simple(plugin, this, "forward", null, (sender, command, label, args) -> {
            File base = plugin.getMapPath().toFile();
            for (File file : base.listFiles(File::isDirectory)) {
                try {
                    File configf = file.toPath().resolve("map_config.json").toFile();
                    System.out.println("Saving to " + file.toPath().getFileName().toString());
                    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                            .registerTypeAdapter(Location.class, new Map.LocationSerializer())
                            .create();
                    BufferedReader br = new BufferedReader(new FileReader(configf));
                    CaptureMap.CaptureMapConfiguration config = gson.fromJson(br, CaptureMap.CaptureMapConfiguration.class);
                    gson = Map.getNormalGson();
                    Map.MapConfiguration[] mapConfig = new Map.MapConfiguration[]{config};
                    try (FileWriter writer = new FileWriter(configf)) {
                        gson.toJson(mapConfig[0], writer);
                    }
                } catch (Exception e) {
                    System.out.println("Problem processing map: " + file.getName());
                    e.printStackTrace();
                }
            }
        }));

        // Edit command
        subCommands.add(SubCommand.simple(plugin, this, "edit", null, ((sender, command, label, args) -> {
            // Get map and try to load it.
            if (args.length == 0) {
                sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to specify a map name!");
                return;
            }
            MapEditContainer map;
            try {
                map = plugin.mapEditPool.getOrCreate(args[0]);
            } catch (Exception e) {
                // Let the player know what happened.
                sender.sendMessage(CloudFight.PREFIX + CloudFight.color("&cSomething went wrong when trying to load map " + args[0] + ". Make sure you typed it's name in exactly correct and that the map_config.yml exists. Error message &4" + e.getMessage()));
                return;
            }
            map.add((Player) sender);
        })));

        // Load command
        subCommands.add(SubCommand.simple(plugin, this, "load", null, (sender, command, label, args) -> {
            // Loads a map into the game.
            if (args.length == 0) {
                sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to specify a map name!");
                return;
            }

            GameContainer container = plugin.gamePool.getWherePlayer((Player) sender);
            if (container == null) {
                sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You aren't in a game!");
                return;
            }
            Map map = plugin.gamePool.mapStorage.get(args[0]);
            if (map == null) {
                sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "That map doesn't exist!");
            } else {
                container.map = map;
                sender.sendMessage(CloudFight.PREFIX + ChatColor.GREEN + "Map set!");
            }
        }));

        // Reload command
        subCommands.add(SubCommand.simple(plugin, this, "reload", null, (sender, command, label, args) -> {
            plugin.gamePool.mapStorage.load();
            sender.sendMessage(CloudFight.PREFIX + CloudFight.color("&a reloaded successful!"));
        }));

        subCommands.add(SubCommand.simple(plugin, this, "boundary", null, (sender, command, label, args) -> {
            Player player = (Player) sender;
            if (args.length != 2) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "/boundary <A/B> <#>");
                return;
            }
            ArmorStandBuilder build = ArmorStandBuilder.spawn(player.getLocation());
            if (args[0].equalsIgnoreCase("A")) {
                build.name("restricted-" + args[1] + "A").meta("map", "restricta", plugin);
            } else {
                build.name("restricted-" + args[1] + "B").meta("map", "restrictb", plugin);
            }
        }));

        subCommands.add(SubCommand.simple(plugin, this, "mode", null, ((sender, command, label, args) -> {
            Player player = (Player) sender;
            MapEditContainer container = plugin.mapEditPool.isEditing(player);
            if (container == null) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to be editing a map to set values!");
                return;
            }
            if (args.length == 0) {
                player.sendMessage(CloudFight.PREFIX + "Possible options: <mode>");
                return;
            }
            GameState.Mode mode;
            try {
                mode = GameState.Mode.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Mode " + args[0].toUpperCase() + " does not exist!");
                return;
            }
            ArmorStandBuilder.spawn(player.getLocation()).meta("map", "mode", plugin).name(mode.name());
            player.sendMessage(CloudFight.PREFIX + ChatColor.GREEN + "Mode set!");
        })));
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

    }


    @Override
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}
