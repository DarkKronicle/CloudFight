package darkkronicle.github.io.cloudfight.buidler;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.WorldManagement;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapEditContainer {

    public Map map;
    public World world;
    private final CloudFight plugin;


    public static MapEditContainer fromName(CloudFight plugin, String mapname) {
        Path path = plugin.getMapPath().resolve(mapname);
        if (Files.notExists(path)) {
            throw new NullPointerException("Map " + mapname + " does not exist!");
        }
        Map map;
        try {
            map = Map.fromFile(path.toFile());
        } catch (FileNotFoundException e) {
            System.out.println(mapname + " could not be loaded :(");
            return null;
        }
        return new MapEditContainer(plugin, map);
    }

    private MapEditContainer(CloudFight plugin, Map map) {
        this.plugin = plugin;
        this.map = map;
        Path path = Bukkit.getWorldContainer().toPath().resolve("maps").resolve(this.map.getName());
        if (Files.notExists(path)) {
            throw new NullPointerException("Map " + this.map.getName() + " does not exist!");
        }
        this.world = WorldManagement.editWorld("maps/" + this.map.getName());
        map.loadVisual(world, plugin);
    }

    public boolean isEditing(Player player) {
        return world.getPlayers().contains(player);
    }

    public boolean add(Player player) {
        if (world.getPlayers().contains(player)) {
            return false;
        }
        Location to_tp;
        // TODO Make this good
//        if (map.config.spawns.containsKey("spectator")) {
//            to_tp = map.config.spawns.get("spectator").clone();
//            to_tp.setWorld(world);
//            player.teleport(to_tp);
//        } else {
            to_tp = new Location(world, 0.5, 100, 0.5, 0, -90);
//        }
        player.teleport(to_tp);
        player.setGameMode(GameMode.CREATIVE);
        player.setBedSpawnLocation(to_tp, true);
        player.setFlying(true);
        world.getPlayers().add(player);
        return true;
    }

    public boolean remove(Player player, boolean tp) {
        int size = world.getPlayers().size();
        if (!world.getPlayers().contains(player)) {
            if (size == 0) {
                unload();
            }
            return false;
        }
        if (tp) {
            plugin.returnToSpawn(player);
        }
        // If no one is editing, why have the world loaded?
        if (size == 1) {
            unload();
        }
        return true;
    }

    public void unload() {
        System.out.println("Unloading map");
        // Make sure we don't get any crazy pants loading weird things.
        plugin.mapEditPool.editing.remove(map.getName());
        map.saveVisual(world);
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : world.getPlayers()) {
                plugin.returnToSpawn(p);
            }
            boolean success = Bukkit.getServer().unloadWorld(world, true);
            if (!success) {
                System.out.println("Unloading map " + map.getName() + " was unsuccessful.");
            } else {
                System.out.println("Unloaded map.");
            }
        });
    }

}
