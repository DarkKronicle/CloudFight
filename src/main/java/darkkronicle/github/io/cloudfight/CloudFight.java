package darkkronicle.github.io.cloudfight;

import com.google.common.base.Charsets;
import darkkronicle.github.io.cloudfight.buidler.MapEditPool;
import darkkronicle.github.io.cloudfight.commands.CommandHandler;
import darkkronicle.github.io.cloudfight.game.GamePool;
import darkkronicle.github.io.cloudfight.utility.ScoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CloudFight extends JavaPlugin {

    // Globals.
    public static String PREFIX = ChatColor.DARK_GRAY + ">>" + ChatColor.RESET;
    public static int GAMES_AMOUNT = 1;
    public final static String CYAN_NAME = "cyan";
    public final static String PURPLE_NAME = "purple";

    public CommandHandler commandHandler;
    public GamePool gamePool;
    public EventListener eventListener;
    public MapEditPool mapEditPool;

    private final Path mapFolder;
    private final Path structureFolder;
    private Team cyan;
    private Team purple;
    private Location spawn;
    private static final Random random = new Random();

    private List<String> win_splash;
    private List<String> lose_splash;


    public Team getCyan() {
        return cyan;
    }

    public Team getPurple() {
        return purple;
    }

    public CloudFight() {
        structureFolder = this.getDataFolder().toPath().resolve("game");
        mapFolder = Bukkit.getWorldContainer().toPath().resolve("maps");
    }

    @Override
    public void onEnable() {
        commandHandler = new CommandHandler(this);
        gamePool = new GamePool(this);

        mapEditPool = new MapEditPool(this);
        eventListener = new EventListener(this);

        loadConfig();

        // Setup teams
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        boolean cyanfound = false;
        boolean purplefound = false;
        for (Team t : scoreboard.getTeams()) {
            if (t.getName().equalsIgnoreCase(CYAN_NAME)) {
                cyanfound = true;
                cyan = t;
            } else if (t.getName().equalsIgnoreCase(PURPLE_NAME)) {
                purplefound = true;
                purple = t;
            }
        }
        if (!cyanfound) {
            cyan = scoreboard.registerNewTeam(CYAN_NAME);
        }
        if (!purplefound) {
            purple = scoreboard.registerNewTeam(PURPLE_NAME);
        }
        cyan.setColor(ChatColor.DARK_AQUA);
        purple.setColor(ChatColor.DARK_PURPLE);
        for (Team t : new Team[]{cyan, purple}) {
            t.setAllowFriendlyFire(false);
            t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
            t.setPrefix(t.getColor() + "");
            t.setDisplayName(t.getColor() + t.getName().substring(0, 1).toUpperCase());
        }
    }

    @Override
    public void onDisable() {
        gamePool.stop();
    }

    public void loadConfig() {
        File file = getDataFolder().toPath().resolve("config.yml").toFile();
        if (!file.exists()) {
            this.saveDefaultConfig();
        }

        YamlConfiguration config = (YamlConfiguration) this.getConfig();
        ConfigurationSection spawn_sect = config.getConfigurationSection("spawn");
        spawn = new Location(Bukkit.getWorld("world"), spawn_sect.getDouble("x"), spawn_sect.getDouble("y"), spawn_sect.getDouble("z"), (float) spawn_sect.getDouble("yaw"), (float) spawn_sect.getDouble("pitch"));
        lose_splash = config.getStringList("lose-splash");
        win_splash = config.getStringList("win-splash");
    }

    public Path getMapPath() {
        return mapFolder;
    }

    public Path getStructurePath() {
        return structureFolder;
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public Location getSpawn() {
        return spawn;
    }

    /**
     * Teleports a player to spawn and configures them to be in the correct gamemode and stuff.
     * <p>
     * This is a general catch all and should be called whenever a player is to be returned to spawn
     *
     * @param player Player to return to spawn
     */
    public void returnToSpawn(Player player) {
        player.setDisplayName(ChatColor.GRAY + player.getName());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setBedSpawnLocation(getSpawn(), true);
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(getSpawn());
        player.setHealthScale(20);
        player.setHealth(player.getHealthScale());
        player.setLevel(0);
        player.setExp(0);
        for (PotionEffect e : player.getActivePotionEffects()) {
            player.removePotionEffect(e.getType());
        }

        ScoreHelper score = ScoreHelper.getOrCreate(player);
        ArrayList<String> slots = new ArrayList<>();
        score.setTitle("&5&lCloudFight");
        slots.add("&7&m---------------");
        slots.add("");
        slots.add("This is still in");
        slots.add("&b&lBETA!");
        slots.add("");
        slots.add("Do &b/join&r to start!");
        slots.add("");
        slots.add("&7&m---------------");

        score.setSlotsFromList(slots);
    }

    public String getRandomWin() {
        return win_splash.get(random.nextInt(win_splash.size()));
    }

    public String getRandomLose() {
        return lose_splash.get(random.nextInt(lose_splash.size()));
    }

}
