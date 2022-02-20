package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.game.maps.MapStorage;
import darkkronicle.github.io.cloudfight.utility.ScoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GamePool {

    /**
     * Current games created. They're stored by their world names.
     */
    public LinkedHashMap<String, GameContainer> games = new LinkedHashMap<>();

    private int tick = 0;

    /**
     * Stored maps
     */
    public MapStorage mapStorage;
    private boolean stop = false;
    private final CloudFight plugin;

    public GamePool(CloudFight plugin) {
        this.plugin = plugin;
        mapStorage = new MapStorage(plugin.getMapPath().toFile());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (stop) {
                    // Could honestly move this into an event if something becomes easy.
                    cancel();
                }
                onTick();
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Gets a game off of it's world/game name
     *
     * @param worldname World name the game uses
     * @return GameInstance of game found
     */
    public GameContainer getGame(String worldname) {
        return games.get(worldname);
    }

    /**
     * Force stops all currently active games
     */
    public void stop() {
        stop = true;
        List<GameContainer> started = new ArrayList<>(getGamesStarted());
        for (GameContainer g : started) {
            g.gameInstance.stop();
        }
    }

    /**
     * Get's a map name for a new game that isn't used.
     * <p>
     * This is a generic getter and forms a game name with `arena#`
     *
     * @return String with a map name
     */
    private String newMapName() {
        int i = 0;
        while (true) {
            String name = "arena" + i;
            if (!games.containsKey(name)) {
                return name;
            }
            i++;
        }
    }

    /**
     * Creates a new game with a specified world name.
     *
     * @param worldname Game world name to load
     * @throws IllegalArgumentException if the worldname is already a game
     */
    public void newGame(String worldname) {
        if (games.containsKey(worldname)) {
            throw new IllegalArgumentException(worldname + " already exists! Can't load!");
        }
        games.put(worldname, new GameContainer(plugin, worldname));
    }

    /**
     * Get a specific team a player is on
     *
     * @param player Player to check for
     * @return The team the player is on, null if none.
     */
    public Team getPlayerTeam(Player player) {
        for (GameContainer g : games.values()) {
            Team team = g.getPlayerTeam(player);
            if (team != null) {
                return team;
            }
        }
        return null;
    }

    /**
     * Tick each of the game's loops
     */
    public void onTick() {
        for (GameContainer g : games.values()) {
            g.tick();
        }
        tick++;
        if (tick % 20 == 0) {
            if (tick == 100) {
                tick = 1;
            }
            ScoreHelper.updateTeams(plugin.getCyan().getScoreboard());
        }
    }

    /**
     * Get's a GameInstance based off of the world the entity is. Recommended for use in games.
     *
     * @param entity Entity to check
     * @return GameInstance of the game they're in, null if none.
     */
    public GameContainer getGame(Entity entity) {
        // We can use world name to directly get the game it's in.
        String name = entity.getLocation().getWorld().getName();
        return games.getOrDefault(name, null);
    }

    /**
     * Gets what game a player is by checking each game. Not recommended in favor of getGame(Entity)
     *
     * @param player Player to get game
     * @return GameInstance of found game
     */
    public GameContainer getWherePlayer(Player player) {
        for (GameContainer g : games.values()) {
            if (g.allPlayers().contains(player)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Get all the games that have the state Started
     *
     * @return List of games that are started
     */
    public List<GameContainer> getGamesStarted() {
        ArrayList<GameContainer> gamesStarted = new ArrayList<>();
        for (GameContainer g : games.values()) {
            if (g.newstate == GameState.State.STARTED) {
                gamesStarted.add(g);
            }
        }
        return gamesStarted;
    }

    public Team getTeam(String team_name) {
        if (games.isEmpty()) {
            newGame(newMapName());
        }
        for (GameContainer game : games.values()) {
            Team team = game.getTeam(team_name);
            if (team != null) {
                return team;
            }
        }
        return null;
    }

    public void spectatePlayer(Player player) {
        spectatePlayer(player, null);
    }

    public void spectatePlayer(Player player, String gamename) {
        if (gamename == null) {
            if (games.isEmpty()) {
                newGame(newMapName());
            }

            gamename = games.keySet().iterator().next();
        }
        GameContainer game = games.get(gamename);
        game.addSpectator(player);
    }

    /**
     * Joins the first game in the list.
     *
     * @param player Player to join to a game
     * @return Team that the player joined
     */
    public Team joinPlayer(Player player) {
        // Get the first game.
        return joinPlayer(player, null, null);
    }

    /**
     * Add a player to a specific game. If the teams are unbalanced it will return null.
     *
     * @param player   Player to join
     * @param gamename Game name to add to
     * @param team     Team to try to add to
     * @return Team if successful on joining
     */
    public Team joinPlayer(Player player, String gamename, Team team) {
        if (gamename == null) {
            if (games.isEmpty()) {
                newGame(newMapName());
            }

            gamename = games.keySet().iterator().next();
        }
        for (GameContainer g : games.values()) {
            // Check to see if they already exist in a game.
            if (g.getPlayerTeam(player) != null) {
                return null;
            }
        }
        GameContainer game = games.get(gamename);
        if (game == null) {
            return null;
        }
        if (team == null) {
            team = game.getRandomTeam();
        }
        if (game.addPlayer(player, team)) {
            Bukkit.getServer().broadcastMessage(CloudFight.PREFIX + team.getPrefix() + player.getName() + " joined the " + team.getName() + " team!");
            return team;
        }
        return null;
    }

    /**
     * Remove a player from the game
     *
     * @param player Player to remove
     * @return If they were successfully removed, the team that they were on
     */
    public Team removePlayer(Player player) {
        player.setBedSpawnLocation(plugin.getSpawn(), true);
        for (GameContainer g : games.values()) {
            if (g.getPlayerTeam(player) != null) {
                return g.removePlayer(player);
            }
        }
        return null;
    }

}