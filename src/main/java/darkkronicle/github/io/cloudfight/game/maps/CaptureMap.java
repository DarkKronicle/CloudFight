package darkkronicle.github.io.cloudfight.game.maps;

import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.Platform;
import darkkronicle.github.io.cloudfight.utility.ArmorStandBuilder;
import darkkronicle.github.io.cloudfight.utility.Box;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CaptureMap extends Map {

    public static class CaptureMapConfiguration extends MapConfiguration<CaptureMap> {
        public HashMap<String, Location> generators;
        public List<Location> shop;
        public List<Platform.PlatformStorage> platforms;
        public List<Location> xp;
        public String name;

        public CaptureMapConfiguration() {
            super(CaptureMap.class);
            active = false;
            spawns = new HashMap<>();
            shop = new ArrayList<>();
            platforms = new ArrayList<>();
            generators = new HashMap<>();
            xp = new ArrayList<>();
            mode = GameState.Mode.CAPTURE;
            name = "Unknown";
        }

        @Override
        public CaptureMap toMap(File directory) {
            return new CaptureMap(directory, this);
        }

        @Override
        public GameState.Mode getType() {
            return GameState.Mode.CAPTURE;
        }

        @Override
        public Class<CaptureMap> getParent() {
            return CaptureMap.class;
        }
    }

    private CaptureMapConfiguration config;

    protected CaptureMap(File directory, CaptureMapConfiguration config) {
        super(directory);
        this.config = config;
    }

    @Override
    public GameState.Mode getType() {
        return GameState.Mode.CAPTURE;
    }

    @Override
    public MapConfiguration<CaptureMap> getConfig() {
        return config;
    }

    @Override
    public String getName() {
        return directory.getName();
    }

    @Override
    public void save() throws IOException {
        save(gson, directory.toPath().resolve("map_config.json").toFile(), config);
    }

    /**
     * Loads armor stands that follow the configuration values of the map
     *
     * @param world  World to load it to
     * @param plugin Master plugin
     */
    @Override
    public void loadVisual(World world, Plugin plugin) {
        ArmorStandBuilder stand = ArmorStandBuilder.spawn(world, new Location(world, 0.5, 150, 0.5));
        if (config.active) {
            stand.name("Active").meta("map", "active", plugin);
        } else {
            stand.name("Disabled").meta("map", "disabled", plugin);
        }
        for (java.util.Map.Entry<String, Location> s : config.spawns.entrySet()) {
            ArmorStandBuilder.spawn(world, s.getValue()).name(s.getKey() + "-spawn").meta("map", "spawn", plugin);
        }
        for (java.util.Map.Entry<String, Location> g : config.generators.entrySet()) {
            ArmorStandBuilder.spawn(world, g.getValue()).name(g.getKey() + "-generator").meta("map", "gen", plugin);
        }
        for (Location s : config.shop) {
            ArmorStandBuilder.spawn(world, s).name("Shop").meta("map", "shop", plugin);
        }
        ArmorStandBuilder.spawn(world, config.getBoundary().loc1).name("Boundary 1--").meta("map", "bound1", plugin);
        ArmorStandBuilder.spawn(world, config.getBoundary().loc2).name("Boundary 2++").meta("map", "bound2", plugin);
        for (Platform.PlatformStorage p : config.platforms) {
            ArmorStandBuilder.spawn(world, p.getPlatform().getCenter()).name("Platform").meta("map", "platform", plugin);
        }
        for (Location p : config.xp) {
            ArmorStandBuilder.spawn(world, p).name("XP").meta("map", "xp", plugin);
        }
        int bound = 0;
        for (Box b : config.restricted) {
            bound++;
            ArmorStandBuilder.spawn(world, b.loc1).name("restricted-" + bound + "A").meta("map", "restricta", plugin);
            ArmorStandBuilder.spawn(world, b.loc2).name("restricted-" + bound + "B").meta("map", "restrictb", plugin);
        }
        ArmorStandBuilder.spawn(world, new Location(null, 0.5, 95, 0.5)).name(config.mode.name()).meta("map", "mode", plugin);
    }

    @Override
    public void saveVisual(World world) {
        boolean active = false;
        HashMap<String, Location> spawns = new HashMap<>();
        HashMap<String, Location> gens = new HashMap<>();
        ArrayList<Platform.PlatformStorage> platforms = new ArrayList<>();
        ArrayList<Location> shops = new ArrayList<>();
        ArrayList<Location> xp = new ArrayList<>();
        HashMap<String, ArmorStand> waiting = new HashMap<>();
        ArrayList<Box> restricted = new ArrayList<>();
        Location bound1 = null;
        Location bound2 = null;
        GameState.Mode mode = GameState.Mode.CAPTURE;
        for (ArmorStand arm : world.getEntitiesByClass(ArmorStand.class)) {
            if (!arm.hasMetadata("map")) {
                continue;
            }
            String val = arm.getMetadata("map").get(0).asString();
            if (val.equalsIgnoreCase("disabled")) {
                active = false;
            } else if (val.equalsIgnoreCase("active")) {
                active = true;
            } else if (val.equalsIgnoreCase("spawn")) {
                spawns.put(arm.getCustomName().toLowerCase().replace("-spawn", ""), getLocationWithDirection(arm.getLocation()));
            } else if (val.equalsIgnoreCase("gen")) {
                gens.put(arm.getCustomName().toLowerCase().replace("-generator", ""), getLocationWithDirection(arm.getLocation()));
            } else if (val.equalsIgnoreCase("platform")) {
                Location loc = getBlockLocation(arm.getLocation());
                platforms.add(new Platform.PlatformStorage(100,
                        Box.fromPoint(loc,
                                5, 0, 5, 5, 0, 5),
                        Box.fromPoint(loc.clone().add(0, 1, 0),
                                5, 0, 5, 5, 4, 5),
                        new ArrayList<>(), "A"));
            } else if (val.equalsIgnoreCase("shop")) {
                shops.add(getLocationWithDirection(arm.getLocation()));
            } else if (val.equalsIgnoreCase("xp")) {
                xp.add(arm.getLocation());
            } else if (val.equalsIgnoreCase("bound2")) {
                bound2 = arm.getLocation();
            } else if (val.equalsIgnoreCase("bound1")) {
                bound1 = arm.getLocation();
            } else if (val.equalsIgnoreCase("restricta")) {
                String name = arm.getCustomName().substring(0, arm.getCustomName().length() - 1);
                if (waiting.containsKey(name)) {
                    restricted.add(new Box(waiting.remove(name).getLocation(), arm.getLocation()));
                } else {
                    waiting.put(name, arm);
                }
            } else if (val.equalsIgnoreCase("restrictb")) {
                String name = arm.getCustomName().substring(0, arm.getCustomName().length() - 1);
                if (waiting.containsKey(name)) {
                    restricted.add(new Box(arm.getLocation(), waiting.remove(name).getLocation()));
                } else {
                    waiting.put(name, arm);
                }
            } else if (val.equalsIgnoreCase("mode")) {
                GameState.Mode m;
                try {
                    m = GameState.Mode.valueOf(arm.getCustomName().toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }
                mode = m;
            }
        }
        if (bound1 == null) {
            bound1 = new Location(null, -100, 50, -30);
        }
        if (bound2 == null) {
            bound2 = new Location(null, 100, 80, 30);
        }
        config.spawns = spawns;
        config.platforms = platforms;
        config.shop = shops;
        config.xp = xp;
        config.active = active;
        config.boundary = new Box(bound1, bound2);
        config.restricted = restricted;
        config.generators = gens;
        config.mode = mode;
        try {
            save();
        } catch (IOException e) {
            System.out.println("Couldn't save map: " + config.name);
            e.printStackTrace();
        }
        deleteVisual(world);
    }

}
