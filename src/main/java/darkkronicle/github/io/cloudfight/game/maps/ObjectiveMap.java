package darkkronicle.github.io.cloudfight.game.maps;

import darkkronicle.github.io.cloudfight.game.GameState;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ObjectiveMap extends Map {
    protected ObjectiveMap(File directory) {
        super(directory);
    }

    @Override
    public GameState.Mode getType() {
        return null;
    }

    @Override
    public MapConfiguration<?> getConfig() {
        return null;
    }

    @Override
    public void save() throws IOException {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void loadVisual(World world, Plugin plugin) {

    }

    @Override
    public void saveVisual(World world) {

    }
}
