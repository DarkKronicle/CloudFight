package darkkronicle.github.io.cloudfight.utility;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldManagement {

    public final static String[] COPY_WHITELIST = {"uid.dat", "map_config.yml", "map_config.json"};

    /**
     * Copy's a directory
     *
     * @param oldPath File to copy
     * @param newLocation File to copy to
     */
    public static void copyDirectory(File oldPath, File newLocation) {
        //This method just copies a world (used to copy an arena into the arena world)
        ArrayList<String> ignored = new ArrayList<>(Arrays.asList(COPY_WHITELIST));
        for (File file : oldPath.listFiles()) {
            if (!ignored.contains(file.getName())) {
                if (file.isDirectory()) {
                    new File(newLocation + "//" + file.getName()).mkdirs();
                    copyDirectory(new File(oldPath.getPath() + "//" + file.getName()), new File(newLocation.getPath() + "//" + file.getName()));
                } else {
                    try {
                        InputStream in = new FileInputStream(file.getAbsolutePath());
                        OutputStream out = new FileOutputStream(newLocation.getPath() + "//" + file.getName());
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Loads a world without auto-save
     *
     * @param worldName World to load
     * @return The loaded world
     */
    public static World loadWorld(String worldName) {
        // Loads world.
        WorldCreator worldCreator = new WorldCreator(worldName);
        World world = Bukkit.getServer().createWorld(worldCreator);
        world.setAutoSave(false);
        return world;
    }

    /**
     * Load's a world with auto-save
     *
     * @param worldName World to load
     * @return The loaded world
     */
    public static World editWorld(String worldName) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        World world = Bukkit.getServer().createWorld(worldCreator);
        world.setAutoSave(true);
        return world;
    }

    /**
     * Delete's a world
     *
     * @param worldName World to delete
     * @return If the path was successfully deleted
     */
    public static boolean deleteWorld(String worldName) {
        File path = new File(worldName);
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) {
                    deleteWorld(worldName + "//" + file.getName());
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }


}
