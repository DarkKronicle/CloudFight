package darkkronicle.github.io.cloudfight.buidler;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;

public class MapEditPool {
    protected HashMap<String, MapEditContainer> editing = new HashMap<>();
    private final CloudFight plugin;

    public MapEditPool(CloudFight plugin) {
        this.plugin = plugin;
    }

    public boolean isEditing(String string) {
        return editing.containsKey(string);
    }

    /**
     * @param mapname
     * @throws NullPointerException if map doesn't exist
     */
    public MapEditContainer getOrCreate(String mapname) {
        if (!editing.containsKey(mapname)) {
            editing.put(mapname, MapEditContainer.fromName(plugin, mapname));
        }
        return editing.get(mapname);
    }

    public MapEditContainer isEditing(Player player) {
        String worldname = player.getWorld().getName().replaceFirst("maps/", "");
        if (editing.containsKey(worldname)) {
            return editing.get(worldname);
        }
        return null;
    }

    public boolean removePlayer(Player player, boolean tp) {
        Iterator<MapEditContainer> iterator = editing.values().iterator();
        while (iterator.hasNext()) {
            MapEditContainer m = iterator.next();
            if (m.remove(player, tp)) {
                return true;
            }
        }
        return false;
    }

    public boolean unload(String mapname) {
        if (mapname.contains("maps/")) {
            mapname = mapname.replaceFirst("maps/", "");
        }
        if (editing.containsKey(mapname)) {
            MapEditContainer map = editing.get(mapname);
            map.unload();
            return true;
        }
        return false;
    }

}
