package darkkronicle.github.io.cloudfight.utility;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class ArmorStandBuilder {

    private interface LocationBuilder {
        Location loc();
    }

    private ArmorStand stand;

    private ArmorStandBuilder(World world, Location location) {
        this(((LocationBuilder) () -> {
            Location loc = location.clone();
            loc.setWorld(world);
            return loc;
        }).loc());
    }

    private ArmorStandBuilder(Location location) {
        this((ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND));
    }

    public ArmorStandBuilder(ArmorStand stand) {
        this.stand = stand;
        stand.setGravity(false);
    }

    public static ArmorStandBuilder spawn(World world, Location location) {
        return new ArmorStandBuilder(world, location);
    }

    public static ArmorStandBuilder spawn(Location location) {
        return new ArmorStandBuilder(location);
    }

    public ArmorStandBuilder pitch(int pitch) {
        stand.getLocation().setPitch(pitch);
        return this;
    }

    public ArmorStandBuilder yaw(int yaw) {
        stand.getLocation().setPitch(yaw);
        return this;
    }

    public ArmorStandBuilder meta(String key, String value, Plugin plugin) {
        stand.setMetadata(key, new FixedMetadataValue(plugin, value));
        return this;
    }

    public ArmorStandBuilder name(String name) {
        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        return this;
    }

}
