package darkkronicle.github.io.cloudfight.game.maps;

import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.games.GameInstance;
import darkkronicle.github.io.cloudfight.utility.Bag;
import darkkronicle.github.io.cloudfight.utility.Box;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.io.File;
import java.util.HashMap;


/**
 * A class to keep track of different maps
 */
public class MapStorage {

    public enum Configuration {
        CAPTURE(CaptureMap.CaptureMapConfiguration.class, GameState.Mode.CAPTURE)
        ;
        public Class<? extends Map.MapConfiguration<?>> config;
        public GameState.Mode mode;

        Configuration(Class<? extends Map.MapConfiguration<?>> config, GameState.Mode mode) {
            this.config = config;
            this.mode = mode;
        }
    }

    /**
     * Directory that contains all the maps
     */
    private final File base;

    /**
     * Maps stored based off of their name
     */
    private HashMap<String, Map> maps = new HashMap<>();
    private HashMap<GameState.Mode, Bag<Map>> mapBag;

    /**
     * Loads map storage based off of a directory given
     *
     * @param base Directory that contains all the maps
     */
    public MapStorage(File base) {
        this.base = base;
        load();
    }

    public Map get(String mapname) {
        if (maps.containsKey(mapname)) {
            return maps.get(mapname);
        }
        return null;
    }

    /**
     * Loads all the maps. Is safe to be called multiple times.
     */
    public void load() {
        // Go through all folders and grab each directory.
        maps = new HashMap<>();
        for (File file : base.listFiles(File::isDirectory)) {
            try {
                Map map = Map.fromFile(file);
                maps.put(map.getName(), map);
            } catch (Exception e) {
                System.out.println("Problem processing map: " + file.getName());
                e.printStackTrace();
            }
        }
        setRandom();
    }

    private void setRandom() {
        mapBag = new HashMap<>();
        for (GameState.Mode mode : GameState.Mode.values()) {
            mapBag.put(mode, Bag.fromCollection(maps.values(), map -> map.getType() == mode && map.getConfig().isActive()));
        }
    }

    /**
     * Gets a new map from the rotation
     *
     * @return Map object
     */
    public Map getRandom(GameState.Mode mode) {
        if (mapBag.get(mode).size() == 0) {
            return mapBag.get(GameState.Mode.CAPTURE).getValues().get(0);
        }
        return mapBag.get(mode).get();
    }

}
