package darkkronicle.github.io.cloudfight.game.maps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.utility.Box;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class to store configuration values of a map
 */
public abstract class Map {


    /**
     * A class that stores a map's configuration values
     */
    public static abstract class MapConfiguration<M extends Map> {
        protected transient boolean serialized = false;

        public boolean active;
        public HashMap<String, Location> spawns;
        public Box boundary;
        protected ArrayList<Box> restricted;
        protected GameState.Mode mode;
        private transient final Class<M> parent;

        public MapConfiguration(Class<M> parent) {
            active = false;
            this.parent = parent;
            spawns = new HashMap<>();
            mode = GameState.Mode.CAPTURE;
            restricted = new ArrayList<>();
            boundary = new Box(new Location(null, -100, 50, -30), new Location(null, 100, 100, 30));
        }

        public abstract M toMap(File directory);

        public Class<M> getParent() {
            return parent;
        }

        public GameState.Mode getType() {
            return mode;
        }

        public boolean isActive() {
            return active;
        }

        public java.util.Map<String, Location> getSpawns() {
            return spawns;
        }

        public Box getBoundary() {
            return boundary;
        }

        public List<Box> getRestricted() {
            return restricted;
        }

    }

    public abstract GameState.Mode getType();

    public abstract MapConfiguration<?> getConfig();

    public abstract void save() throws IOException;

    public abstract String getName();

    public boolean isActive() {
        return getConfig().isActive();
    }

    /**
     * A serializer and deserializer for the org.bukkit.Location objects.
     * This only stores x, y z, yaw, and pitch
     */
    public static class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {

        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            obj.addProperty("yaw", src.getYaw());
            obj.addProperty("pitch", src.getPitch());
            return obj;
        }

        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            double x = obj.get("x").getAsDouble();
            double y = obj.get("y").getAsDouble();
            double z = obj.get("z").getAsDouble();
            int yaw = obj.get("yaw").getAsInt();
            int pitch = obj.get("pitch").getAsInt();
            return new Location(null, x, y, z, yaw, pitch);
        }
    }

    public static class MapSerializer implements JsonSerializer<MapConfiguration<?>>, JsonDeserializer<MapConfiguration<?>> {

        @Override
        public JsonElement serialize(MapConfiguration src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            MapStorage.Configuration config = MapStorage.Configuration.CAPTURE;
            for (MapStorage.Configuration c : MapStorage.Configuration.values()) {
                if (c.mode == src.getType()) {
                    config = c;
                    break;
                }
            }
            result.add("type", new JsonPrimitive(config.name()));
            result.add("properties", context.serialize(src, src.getClass()));
            return result;
        }

        @Override
        public MapConfiguration<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String name;
            if (jsonObject.get("type") == null) {
                name = "CAPTURE";
            } else {
                name = jsonObject.get("type").getAsString();
            }
            MapStorage.Configuration config = null;
            for (MapStorage.Configuration c : MapStorage.Configuration.values()) {
                if (c.name().equalsIgnoreCase(name)) {
                    config = c;
                    break;
                }
            }
            if (config == null) {
                config = MapStorage.Configuration.CAPTURE;
            }
            // Set type and string and then put that in the JSON
            JsonElement element = jsonObject.get("properties");

            try {

                return context.deserialize(element, config.config);
            } catch (Exception cnfe) {
                throw new JsonParseException("Unknown element type: " + name, cnfe);
            }
        }
    }

    public static class MapTypeFactory implements TypeAdapterFactory {

        public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            // Return this if the
            return MapConfiguration.class.isAssignableFrom(type.getRawType())
                    ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (com.google.gson.reflect.TypeToken<MapConfiguration>) type)
                    : null;
        }

        private TypeAdapter<MapConfiguration> customizeMyClassAdapter(Gson gson, com.google.gson.reflect.TypeToken<MapConfiguration> type) {
            final TypeAdapter<MapConfiguration> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<MapConfiguration> mapAdapater = gson.getAdapter(MapConfiguration.class);
            return new TypeAdapter<MapConfiguration>() {

                @Override
                public void write(JsonWriter out, MapConfiguration value) throws IOException {
                    JsonElement tree = delegate.toJsonTree(value);
                    if (tree.isJsonObject()) {
                        JsonObject obj;
                        if (!value.serialized) {
                            value.serialized = true;
                            obj = (JsonObject) mapAdapater.toJsonTree(value);
                            tree = obj;
                        }
                    }
                    elementAdapter.write(out, tree);
                }

                @Override
                public MapConfiguration read(JsonReader in) throws IOException {
                    JsonElement tree = elementAdapter.read(in);
                    return delegate.fromJsonTree(tree);
                }
            };
        }
    }

    public final File directory;
    public final File configFile;
    public String name;

    protected final Gson gson = getNormalGson();

    protected Map(File directory) {
        this.directory = directory;
        this.configFile = directory.toPath().resolve("map_config.json").toFile();
        name = this.directory.getName();
    }

    /**
     * Save map configuration into a file
     *
     * @param gson Gson to use
     * @param file File to save to
     * @param config Configuration to save
     * @throws IOException If the filewriter doesn't work
     */
    public static void save(Gson gson, File file, MapConfiguration config) throws IOException {
        System.out.println("Saving to " + file.toPath().getFileName().toString());
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(config, writer);
        }
    }


    public static Gson getNormalGson() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls()
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .registerTypeAdapter(MapConfiguration.class, new MapSerializer())
                .registerTypeAdapterFactory(new MapTypeFactory())
                .create();
    }

    /**
     * Create's a map object from a file using Gson
     *
     * @param file Directory of the map
     * @return The created map
     * @throws FileNotFoundException If the directory doesn't exist
     */
    public static Map fromFile(File file) throws FileNotFoundException {
        Path path = file.toPath();
        System.out.println("Loading map " + path.getFileName().toString());
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Map has to be a directory. " + path.getFileName().toString());
        }
        Path config_file;
        config_file = path.resolve("map_config.json");
        if (Files.notExists(config_file)) {
            throw new NullPointerException("map_config.json does not exist for " + path.getFileName().toString());
        }
        BufferedReader br = new BufferedReader(new FileReader(config_file.toFile()));
        MapConfiguration config = getNormalGson().fromJson(br, MapConfiguration.class);
        return config.toMap(file);
    }

    // TODO remove
    protected static Location getLocationWithDirection(Location location) {
        Location loc = location.clone();
        loc.setPitch(location.getPitch());
        loc.setYaw(location.getYaw());
        return loc;
    }

    protected static Location getBlockLocation(Location location) {
        return new Location(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Loads armor stands that follow the configuration values of the map
     *
     * @param world  World to load it to
     * @param plugin Master plugin
     */
    public abstract void loadVisual(World world, Plugin plugin);

    /**
     * Take a loaded visual and save it into map_config.yml
     *
     * @param world World to get the visual from
     */
    public abstract void saveVisual(World world);


    /**
     * Delete's all parts of the visual
     *
     * @param world World to delete visual
     */
    public void deleteVisual(World world) {
        for (ArmorStand arm : world.getEntitiesByClass(ArmorStand.class)) {
            if (arm.hasMetadata("map")) {
                arm.remove();
            }
        }
    }

}
