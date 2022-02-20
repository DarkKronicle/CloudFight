package darkkronicle.github.io.cloudfight.utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Box {
    public final Location loc1;
    public final Location loc2;

    public Box(Location loc1, Location loc2) {
        // Simplify box
        this.loc1 = new Location(loc1.getWorld(), loc1.getBlockX(), Math.max(Math.min(256, loc1.getBlockY()), 0), loc1.getBlockZ());
        this.loc2 = new Location(loc2.getWorld(), loc2.getBlockX(), Math.max(Math.min(256, loc2.getBlockY()), 0), loc2.getBlockZ());
    }

    public void setWorld(World world) {
        loc1.setWorld(world);
        loc2.setWorld(world);
    }

    public static Box fromPoint(Location point, int radius) {
        return Box.fromPoint(point, radius, radius, radius);
    }

    public static Box fromPoint(Location point, int x, int y, int z) {
        return Box.fromPoint(point, x, y, z, x, y, z);
    }

    public static Box fromPoint(Location point, int subx, int suby, int subz, int addx, int addy, int addz) {
        return new Box(point.clone().subtract(subx, suby, subz), point.clone().add(addx, addy, addz));
    }

    @AllArgsConstructor
    @Data
    public static class Position {
        private double x;
        private double y;
        private double z;
    }

    public List<Location> list() {
        ArrayList<Location> locs = new ArrayList<>();
        for (int y = loc1.getBlockY(); y <= loc2.getBlockY(); y++) {
            for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); z++) {
                for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); x++) {
                    locs.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }
        return locs;
    }

    public Position getLower() {
        int x1 = loc1.getBlockX();
        int x2 = loc2.getBlockX();
        int y1 = loc1.getBlockY();
        int y2 = loc2.getBlockY();
        int z1 = loc1.getBlockZ();
        int z2 = loc2.getBlockZ();
        if (x1 > x2) {
            x1 = x2;
        }
        if (y1 > y2) {
            y1 = y2;
        }
        if (z1 > z2) {
            z1 = z2;
        }
        return new Position(x1, y1, z1);
    }

    public Position getHigher() {
        int x1 = loc1.getBlockX();
        int x2 = loc2.getBlockX();
        int y1 = loc1.getBlockY();
        int y2 = loc2.getBlockY();
        int z1 = loc1.getBlockZ();
        int z2 = loc2.getBlockZ();
        if (x1 > x2) {
            x2 = x1;
        }
        if (y1 > y2) {
            y2 = y1;
        }
        if (z1 > z2) {
            z2 = z1;
        }
        return new Position(x2, y2, z2);
    }

    public boolean isInBox(Location location) {
        Position low = getLower();
        Position high = getHigher();
        return !(location.getBlockX() > high.getX()) && !(location.getBlockX() < low.getX()) && !(location.getBlockY() > high.getY()) && !(location.getBlockY() < low.getY())
                && !(location.getBlockZ() > high.getZ()) && !(location.getBlockZ() < low.getZ());
    }

    public Location getCenter() {
        Position low = getLower();
        Position high = getHigher();
        double x = (high.getX() + low.getX()) / 2;
        double y = (high.getY() + low.getY()) / 2;
        double z = (high.getZ() + low.getZ()) / 2;
        return new Location(loc1.getWorld(), x, y, z);
    }
}
