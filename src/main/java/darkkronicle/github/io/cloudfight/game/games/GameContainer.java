package darkkronicle.github.io.cloudfight.game.games;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.Items;
import darkkronicle.github.io.cloudfight.game.Stats;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.BlockUtils;
import darkkronicle.github.io.cloudfight.utility.Counter;
import darkkronicle.github.io.cloudfight.utility.DelayedIterator;
import darkkronicle.github.io.cloudfight.utility.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * GameInstances should be easy to swap out right until the game actually starts. To help with this this class was created.
 */
public class GameContainer {

    /**
     * How players are needed in each team.
     */
    private final int team_required_players = 1;

    /**
     * How many players total does it need.
     */
    private final int required_players = 2;

    private final Random random = new Random();

    /**
     * World name that it created. Typically like arena0
     */
    public String worldname;

    public World world;

    /**
     * All players int he game
     */
    protected HashMap<Team, ArrayList<Player>> players;

    protected ArrayList<Player> spectators = new ArrayList<>();

    /**
     * What state the game is currently in
     */
    public GameState.State newstate = GameState.State.READY;
    protected GameState.State state = newstate;

    protected GameState.State last_state = newstate;
    protected GameState.State last_new = newstate;

    public Stats stats = new Stats(this);

    /**
     * What mode the game should have
     */
    public GameState.Mode mode = null;

    public GameInstance gameInstance;

    protected long tick = 0;
    private final CloudFight plugin;
    private BossBar bar;
    private final int countdown = 30;
    private int current_countdown = countdown;
    private final int count_stop = 20;
    private int count_current = count_stop;
    public HashMap<GameState.Mode, ArrayList<UUID>> modevotes = new HashMap<>();
    public Map map = null;

    public Counter<Player> deaths = new Counter<>();
    public Counter<Player> kills = new Counter<>();

    public final ArrayList<Block> blocks = new ArrayList<>();


    public GameContainer(CloudFight plugin, String worldname) {
        this.plugin = plugin;
        this.worldname = worldname;
        players = new HashMap<>();
        players.put(plugin.getCyan(), new ArrayList<>());
        players.put(plugin.getPurple(), new ArrayList<>());
    }

    public void tick() {
        tick++;
        // Only once a second we want to update stuff.
        boolean sec = tick % 20 == 0;
        if (state != newstate) {
            last_state = state;
            last_new = newstate;
        }
        GameState.State oldstate = state;
        state = newstate;
        if (oldstate == GameState.State.COUNTDOWN && state == GameState.State.READY) {
            Bukkit.broadcastMessage(CloudFight.PREFIX + CloudFight.color("&cNot enough players in the game to start!"));
            current_countdown = countdown;
            if (bar != null) {
                bar.removeAll();
            }
        } else if (state == GameState.State.COUNTDOWN) {
            if (oldstate != state) {
                bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
            }
            if (sec) {
                if (current_countdown <= 0) {
                    start();
                    return;
                }
                if (current_countdown > 5 && allPlayers().size() == Bukkit.getOnlinePlayers().size()) {
                    current_countdown = 5;
                }
                if (current_countdown <= 5) {
                    bar.setColor(BarColor.RED);
                    for (Player p : allPlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 2);
                    }
                    if (current_countdown == 1) {
                        bar.setTitle(CloudFight.color("&7Game Starts in: &c" + current_countdown + " &7second!"));
                    } else {
                        bar.setTitle(CloudFight.color("&7Game Starts in: &c" + current_countdown + " &7seconds!"));
                    }
                } else {
                    bar.setTitle(CloudFight.color("&7Game Starts in: &b" + current_countdown + " &7seconds!"));
                }
                float progress = (float) current_countdown / countdown;
                bar.setVisible(true);
                bar.setProgress(progress);
                List<Player> already_players = bar.getPlayers();
                for (Player p : allPlayers()) {
                    if (!already_players.contains(p)) {
                        bar.addPlayer(p);
                    }
                }
                current_countdown--;
            }
        } else if (state == GameState.State.STARTED) {
            gameInstance.tick();
        } else if (state == GameState.State.RESETTING) {
            if (state != oldstate) {
                bar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
            }
            if (sec) {
                if (count_current <= 0) {
                    bar.removeAll();
                    gameInstance.stop();
                    return;
                }
                if (allPlayers().size() == 0 && count_current != 1) {
                    count_current = 1;
                }
                bar.setVisible(true);
                if (count_current == 1) {
                    bar.setTitle(CloudFight.color("&cResetting in " + count_current + " second!"));
                } else {
                    bar.setTitle(CloudFight.color("&cResetting in " + count_current + " seconds!"));
                }
                if (count_current <= 5) {
                    for (Player p : allPlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 2);
                    }
                }
                bar.setProgress((float) count_current / count_stop);
                List<Player> already_players = bar.getPlayers();
                for (Player p : allPlayers()) {
                    if (!already_players.contains(p)) {
                        bar.addPlayer(p);
                    }
                }
                count_current--;
            }
        }
        if (sec) {
            last_state = last_new;
        }
    }

    public Team getTeam(String team) {
        for (Team t : players.keySet()) {
            if (t.getName().equalsIgnoreCase(team)) {
                return t;
            }
        }
        return null;
    }

    public void start() {
        if (state == GameState.State.STARTED) {
            // Don't want it to do weird things
            return;
        }
        bar.removeAll();
        if (mode == null) {
            int playerCount = allPlayers().size();
            int toBeat = (int) Math.floor((float) playerCount / 2);
            int current = -1;
            int total = 0;
            ArrayList<GameState.Mode> win = new ArrayList<>();
            for (java.util.Map.Entry<GameState.Mode, ArrayList<UUID>> votes : modevotes.entrySet()) {
                total += votes.getValue().size();
                if (current == -1 || votes.getValue().size() > current) {
                    win.clear();
                    win.add(votes.getKey());
                    current = votes.getValue().size();
                } else if (votes.getValue().size() == current) {
                    win.add(votes.getKey());
                }
            }
            if (total < toBeat) {
                mode = GameState.Mode.CAPTURE;
            } else {
                mode = win.get(random.nextInt(win.size()));
            }
        }
        if (mode.supplier == null) {
            mode = new GameState.RandomMode().get();
        }
        for (Player p : allPlayers()) {
            p.sendMessage(CloudFight.PREFIX + CloudFight.color("&7The current mode is &b" + mode.name().toUpperCase()));
        }
        gameInstance = mode.supplier.newGame(plugin, this);
        gameInstance.start(map);
    }

    public void checkRequiredPlayers() {
        boolean over = false;
        int total_size = 0;
        ArrayList<Team> ready = new ArrayList<>();
        for (Team t : players.keySet()) {
            int size = getTeamSize(t);
            total_size += size;
            if (getTeamSize(t) >= team_required_players) {
                ready.add(t);
            }
        }
        if (total_size >= required_players) {
            over = true;
        }
        boolean good = over && (ready.size() == players.keySet().size());
        if (good) {
            if (newstate == GameState.State.READY) {
                newstate = GameState.State.COUNTDOWN;
            }
        } else if (newstate == GameState.State.COUNTDOWN) {
            newstate = GameState.State.READY;
        } else if (newstate == GameState.State.STARTED) {
            if (ready.size() == 1) {
                gameInstance.endUnchecked(ready.get(0));
            } else {
                gameInstance.endUnchecked(null);
            }
        }
    }

    /**
     * Directly add a player to the game without checking for team balance. Will send a message.
     *
     * @param player Player to join
     * @param team   Team for player to join
     */
    public void addPlayerUnchecked(Player player, Team team) {
        if (!players.containsKey(team)) {
            players.put(team, new ArrayList<>());
        }
        // Don't want there to be multiple entries of a player.
        removePlayer(player);
        players.get(team).add(player);
        team.addEntry(player.getName());
        player.setDisplayName(team.getColor() + player.getName());
        checkRequiredPlayers();
        if (state == GameState.State.STARTED) {
            // Mid game joining
            gameInstance.startPlayer(team, player);
        }
    }

    /**
     * Removes a player from the game
     *
     * @param player Player to remove
     * @return Team that they were removed from. Null if they aren't in the game.
     */
    public Team removePlayer(Player player) {
        for (java.util.Map.Entry<Team, ArrayList<Player>> p : players.entrySet()) {
            if (p.getValue().remove(player)) {
                p.getKey().removeEntry(player.getName());
                plugin.returnToSpawn(player);
                checkRequiredPlayers();
                return p.getKey();
            }
        }
        if (spectators.remove(player)) {
            plugin.returnToSpawn(player);
        }
        return null;
    }

    /**
     * Gets a player's team
     *
     * @param player Player to check
     * @return Team that the player is in
     */
    public Team getPlayerTeam(Player player) {
        for (java.util.Map.Entry<Team, ArrayList<Player>> p : players.entrySet()) {
            if (p.getValue().contains(player)) {
                return p.getKey();
            }
        }
        return null;
    }

    /**
     * Gets all the players, regardless of team, in the game.
     *
     * @return List of players in the game.
     */
    public List<Player> allPlayers() {
        ArrayList<Player> all_player = new ArrayList<>();
        for (ArrayList<Player> p : players.values()) {
            all_player.addAll(p);
        }
        all_player.addAll(spectators);
        return all_player;
    }

    /**
     * Gets the teams with the smallest amount of players. Used for balancing multiple teams.
     * If multiple teams are tied for smallest it will return the tied ones.
     *
     * @return Teams that have the smallest amount of players.
     */
    public List<Team> getSmallestTeams() {
        // We want to get the teams with the least amount of members. We can have multiple be the min
        // So it removes all that aren't the min and checks to see if the team is one of them.
        HashMap<Team, Integer> smallest = new HashMap<>();
        for (Team t : players.keySet()) {
            smallest.put(t, getTeamSize(t));
        }
        Integer min = Collections.min(smallest.values());
        smallest.entrySet().removeIf((set) -> !set.getValue().equals(min));
        return new ArrayList<>(smallest.keySet());
    }

    public Team getRandomTeam() {
        List<Team> small = getSmallestTeams();
        return small.get(random.nextInt(small.size()));
    }

    /**
     * Adds a player to the current game if possible. It will check to make sure the teams are balanced before doing so.
     *
     * @param player Player to join the game
     * @param team   Team that the player will be on. Can be null for a random choice.
     * @return false if unsuccessful (unbalanced teams), true if successful.
     */
    public boolean addPlayer(Player player, Team team) {
        // If the team doesn't exist, it is automatically the smallest.
        if (!players.containsKey(team)) {
            addPlayerUnchecked(player, team);
            return true;
        }

        if (getSmallestTeams().contains(team)) {
            addPlayerUnchecked(player, team);
            return true;
        }
        return false;
    }

    /**
     * Gets a team size
     *
     * @param team Team to check the size
     * @return Amount of pplayers in a team
     */
    public int getTeamSize(Team team) {
        if (players.containsKey(team)) {
            return players.get(team).size();
        }
        return 0;
    }

    public void modeVote(Player player, GameState.Mode mode) {
        ArrayList<UUID> votes = modevotes.get(mode);
        if (votes == null) {
            modevotes.put(mode, new ArrayList<>(Collections.singletonList(player.getUniqueId())));
            return;
        }
        votes.add(player.getUniqueId());
    }

    public boolean hasModeVoted(Player player) {
        UUID uuid = player.getUniqueId();
        for (ArrayList<UUID> players : modevotes.values()) {
            if (players.contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void setMode(GameState.Mode mode) {
        this.mode = mode;
    }

    public void onRespawn(PlayerRespawnEvent event) {
        if (gameInstance != null) {
            gameInstance.onRespawn(event);
        }
    }

    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!blocks.remove(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getBlock().getType() != Material.AIR) {
            blocks.add(event.getBlock());
        }
    }

    public void onExplode(EntityExplodeEvent event) {
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (!blocks.remove(block)) {
                iter.remove();
            }
        }
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getSource() != null && tnt.getSource().getType() == EntityType.PLAYER) {
                Player player = (Player) tnt.getSource();
                if (gameInstance != null) {
                    Team team = getPlayerTeam(player);
                    if (team == null) {
                        return;
                    }
                    Material type = Items.Default.getBlock(team).getType();
                    ArrayList<Material> mats = new ArrayList<>();
                    for (ItemStack i : Items.Default.BLOCKS) {
                        if (i.getType() != type) {
                            mats.add(i.getType());
                        }
                    }
                    HashSet<Block> bloc = new HashSet<>();
                    for (Block b : event.blockList()) {
                        if (mats.contains(b.getType())) {
                            bloc.addAll(BlockUtils.getConnectedBlocks(b, 10));
                        }
                    }
                    Iterator<Block> blockIterator = bloc.iterator();
                    DelayedIterator<Block> locIt = new DelayedIterator<>(blockIterator, (plug, it, block) -> {
                        if (mats.contains(block.getType()) && blocks.remove(block)) {
                            block.setType(Material.AIR);
                            block.getLocation().getWorld().spawnParticle(Particle.ASH, block.getLocation().add(0.5, 0.5, 0.5), 1);
                            block.getLocation().getWorld().playSound(block.getLocation(), Sound.BLOCK_SAND_BREAK, 1, (float) 0.75);
                        }
                        return true;
                    });
                    locIt.start(plugin, 0, 1);
                }
            }
        }
    }

    public void onKill(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer != null && !killer.equals(player)) {
            int steal = (int) Math.ceil(player.getLevel() * 0.2);
            // Steal 20 % of their XP
            player.setLevel(player.getLevel() - steal);
            killer.setLevel(killer.getLevel() + 3 + steal);
            String message = "&a+ " + (steal + 3) + " XP";
            HashMap<ItemStack, Integer> items = ItemBuilder.getAllPlayerItems(player, true);
            int blocks = 0;
            for (ItemStack s : Items.Default.BLOCKS) {
                Integer b = items.get(s);
                if (b != null) {
                    blocks = blocks + b;
                }
            }
            if (blocks > 0) {
                Team team = getPlayerTeam(killer);
                if (team != null) {
                    // Steal 50 % of blocks
                    ItemStack block = Items.Default.getBlock(team).clone();
                    blocks = (int) Math.ceil(blocks * 0.5);
                    block.setAmount(blocks);
                    killer.getInventory().addItem(block);
                    message = message + "\n&b+ " + blocks + " Blocks";
                }
            }
            killer.sendMessage(CloudFight.color(message));
        } else {
            // Remove some XP as a consequence
            player.setLevel((int) Math.floor(player.getLevel() * 0.8));
        }
    }

    public void addSpectator(Player player) {
        removePlayer(player);
        spectators.add(player);
        player.setDisplayName(ChatColor.BLUE + player.getName());
        Bukkit.broadcastMessage(CloudFight.PREFIX + player.getDisplayName() + " is now spectating!");
        if (state == GameState.State.STARTED) {
            // Mid game joining
            gameInstance.spectatePlayer(player);
        }
    }

    public List<Player> getTeamPlayers(Team team) {
        return players.get(team);
    }

    public void onPlayerInteract(Player damager, Player damaged) {
        Team dteam = getPlayerTeam(damaged);
        Team rteam = getPlayerTeam(damager);
        if (dteam != null && dteam.equals(rteam)) {
            if (damager.getLevel() > 0) {
                damager.setLevel(damager.getLevel() - 1);
                damaged.setLevel(damaged.getLevel() + 1);
            }
        }
    }
}
