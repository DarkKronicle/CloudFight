package darkkronicle.github.io.cloudfight.game.games;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.Items;
import darkkronicle.github.io.cloudfight.game.Shop;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.Counter;
import darkkronicle.github.io.cloudfight.utility.FormattingUtils;
import darkkronicle.github.io.cloudfight.utility.ScoreHelper;
import darkkronicle.github.io.cloudfight.utility.WorldManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;

public abstract class GameInstance<G extends Map> {

    // There is a game loop that ticks every second so we want newstate and oldstate to be persistent until then.

    private static final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1, true, false);
    protected final CloudFight plugin;
    /**
     * Map details
     */
    public G map;

    /**
     * Amount of seconds the game has been running
     */
    public int time = 0;
    public GameContainer container;

    private Shop shop;


    public GameInstance(CloudFight plugin, GameContainer container) {
        this.container = container;
        this.plugin = plugin;
    }


    /**
     * Starts the game. Moves players, loads the world, etc
     */
    public void start(Map gamemap) {
        container.newstate = GameState.State.STARTED;
        if (gamemap == null) {
            this.map = (G) plugin.gamePool.mapStorage.getRandom(container.mode);
        } else {
            if (gamemap.getType() != getType()) {
                this.map = (G) plugin.gamePool.mapStorage.getRandom(container.mode);
            } else {
                this.map = (G) gamemap;
            }
        }
        // Load world options
        WorldManagement.copyDirectory(map.directory, new File("./" + container.worldname));
        container.world = WorldManagement.loadWorld(container.worldname);
        container.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        container.world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        container.world.setGameRule(GameRule.KEEP_INVENTORY, true);
        container.world.setGameRule(GameRule.DISABLE_RAIDS, true);
        container.world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        container.world.setGameRule(GameRule.DO_TILE_DROPS, true);
        container.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        container.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        container.world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        container.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        container.world.setDifficulty(Difficulty.EASY);
        shop = new Shop(container);


        for (java.util.Map.Entry<Team, ArrayList<Player>> p : container.players.entrySet()) {
            startPlayer(p.getKey(), p.getValue().toArray(new Player[0]));
        }

        for (Player p : container.spectators) {
            spectatePlayer(p);
        }

    }

    /**
     * Overall tick function. Helps delegate the different states the game is in.
     */
    public void tick() {
        game_tick();
        if (container.tick % 20 == 0) {
            second();
        }
        for (Player p : container.allPlayers()) {
            if (p.getLocation().getY() < 0) {
                p.teleport(p.getLocation().subtract(0, 100, 0));
            }
        }
    }


    /**
     * Game loop tick. Only triggers when the game is started.
     */
    private void second() {
        time++;
        shop.second();
        game_loop();
        for (ExperienceOrb o : container.world.getEntitiesByClass(ExperienceOrb.class)) {
            if (o.getTicksLived() > 600) {
                o.remove();
            }
        }
    }

    /**
     * Game second
     */
    protected void game_loop() {
        for (Player p : container.allPlayers()) {
            p.addPotionEffect(NIGHT_VISION);
            updateScoreBoard(p);
        }
    }

    /**
     * Game tick
     */
    protected void game_tick() {

    }

    /**
     * Update scoreboard for a player
     *
     * @param player Player to set
     */
    protected void updateScoreBoard(Player player) {
        ScoreHelper score = ScoreHelper.getOrCreate(player);
        ArrayList<String> slots = new ArrayList<>();
        int minute = time / 60;
        int second = time % 60;
        String sec = "" + second;
        if (sec.length() == 1) {
            sec = "0" + sec;
        }
        score.setTitle("&5&lCloudFight");
        slots.add("&7&m---------------");
        slots.add("");
        slots.add(" Time: &d" + minute + ":" + sec);
        slots.add("");
        slots.add(" Deaths: &b" + container.deaths.get(player));
        slots.add(" Kills: &b" + container.kills.get(player));
        score.setSlotsFromList(slots);

        score.setSlotsFromList(slots);
    }


    /**
     * Ends the game with a winning team
     */
    public void endUnchecked(Team winner) {
        if (container.newstate != GameState.State.STARTED) {
            // Prevent some weird multi-resetting.
            return;
        }
        container.newstate = GameState.State.RESETTING;
        if (winner == null) {
            Bukkit.getServer().broadcastMessage(CloudFight.PREFIX + CloudFight.color("&l&6It's a tie!"));
        } else {
            String name = winner.getPrefix() + "&l" + winner.getName().substring(0, 1).toUpperCase() + winner.getName().substring(1);
            StringBuilder sb = new StringBuilder().append(FormattingUtils.centerMessage(
                    "{&6" + FormattingUtils.toMinuteSecond(time, false)
                            + "&8&m}", "-", "&8[&m", "&8]")).append("\n");
            sb.append(FormattingUtils.centerMessage(name + " won!")).append("\n");
            StringBuilder team = new StringBuilder();
            for (Player p : container.getTeamPlayers(winner)) {
                team.append(p.getName()).append(", ");
            }
            String t = team.toString();
            if (t.length() != 0) {
                t = t.substring(0, t.length() - 2);
            }
            String[] tlines = FormattingUtils.wrap(t, 200);
            for (String line : tlines) {
                sb.append(FormattingUtils.centerMessage("&7" + line)).append("\n");
            }
            sb.append("\n \n");
            java.util.Map.Entry<Player, Counter.MutableInteger> hkill = container.kills.highest();
            if (hkill != null) {
                sb.append(FormattingUtils.centerMessage("&cMost Kills: &f" + hkill.getValue().get() + " - " + hkill.getKey().getName())).append("\n");
            }
            sb.append(FormattingUtils.centerMessage("&8&m", "-", "&8[&m", "&8]"));
            Bukkit.broadcastMessage(sb.toString());
        }
        for (java.util.Map.Entry<Team, ArrayList<Player>> p : container.players.entrySet()) {
            if (p.getKey().equals(winner)) {
                endWinPlayer(p.getKey(), p.getValue().toArray(new Player[0]));
            } else {
                endLosePlayer(p.getKey(), p.getValue().toArray(new Player[0]));
            }
        }
    }

    /**
     * Stops the game instantly
     */
    public void stop() {
        Bukkit.broadcastMessage(CloudFight.PREFIX + ChatColor.GRAY + "Game reset!");
        plugin.gamePool.games.remove(container.worldname);

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : container.world.getPlayers()) {
                plugin.returnToSpawn(p);
                container.removePlayer(p);
            }
            for (Entity e : container.world.getEntities()) {
                e.remove();
            }
            Bukkit.getServer().unloadWorld(container.worldname, false);
            WorldManagement.deleteWorld(container.worldname);
        });
    }

    /**
     * Puts a player in the game. This works for start of game joining or mid game joining.
     *
     * @param team    Team the player is on
     * @param players The player (or players) you want to add.
     */
    protected void startPlayer(Team team, Player... players) {
        Location spawn = map.getConfig().getSpawns().get(team.getName()).clone();
        spawn.setWorld(container.world);
        for (Player p : players) {
            p.getInventory().clear();
            p.setHealth(p.getHealthScale());
            p.setVelocity(new Vector(0, 0, 0));
            p.teleport(spawn);
            p.setExp(0);
            p.setLevel(0);
            p.setGameMode(GameMode.SURVIVAL);
            p.setBedSpawnLocation(spawn, true);
            for (PotionEffect e : p.getActivePotionEffects()) {
                p.removePotionEffect(e.getType());
            }
            setInventory(p, team);
        }
    }

    public void spectatePlayer(Player player) {
        Location spawn = map.getConfig().getSpawns().get("spectator");
        if (spawn == null) {
            spawn = new Location(container.world, 0.5, 100, 0.5, 0, 90);
        } else {
            spawn.setWorld(container.world);
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(spawn);
    }

    /**
     * After a game end this handles the losing players.
     *
     * @param team    Losing team
     * @param players Losing players
     */
    protected void endLosePlayer(Team team, Player... players) {
        for (Player p : players) {
            p.setGameMode(GameMode.SPECTATOR);
            p.playSound(p.getLocation(), Sound.ENTITY_GHAST_HURT, 2, 1);
            p.sendTitle(CloudFight.color("&cYOU LOST!"), CloudFight.color("&7" + plugin.getRandomLose()), 10, 100, 10);
        }
    }

    /**
     * After a game end this handles the losing players.
     *
     * @param team    Winning team
     * @param players Winning players
     */
    protected void endWinPlayer(Team team, Player... players) {
        for (Player p : players) {
            p.setAllowFlight(true);
            p.setFlying(true);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 2, 1);
            p.sendTitle(CloudFight.color("&bYOU WON!"), CloudFight.color("&7" + plugin.getRandomWin()), 10, 100, 10);
            Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            FireworkEffect.Builder builder = FireworkEffect.builder();
            fireworkMeta.addEffect(builder.flicker(true).withColor(Color.WHITE).build());
            fireworkMeta.addEffect(builder.trail(true).with(FireworkEffect.Type.BALL_LARGE).build());
            fireworkMeta.addEffect(builder.withFade(Color.BLACK).build());
            fireworkMeta.setPower(3);
            firework.setFireworkMeta(fireworkMeta);
        }
    }

    /**
     * Set's default inventory for a player in a team
     *
     * @param player Player to set inventory
     * @param team   Team Team that player is a part of
     */
    public void setInventory(Player player, Team team) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] armor = inv.getArmorContents().clone();
        inv.clear();
        for (Items.Default item : Items.Default.values()) {
            if (item.slot != null) {
                if (item.team == null || item.team.equalsIgnoreCase(team.getName())) {
                    inv.setItem(item.slot, item.stack);
                }
            }
        }
        boolean allNull = true;
        for (ItemStack i : armor) {
            if (i != null) {
                allNull = false;
                break;
            }
        }
        if (!allNull) {
            inv.setArmorContents(armor);
        }
    }

    public void onRespawn(PlayerRespawnEvent event) {
        setInventory(event.getPlayer(), container.getPlayerTeam(event.getPlayer()));
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 4));
    }

    public void onClick(InventoryClickEvent event) {
        shop.handleClick(event);
    }

    /**
     * Get what type of game it is
     *
     * @return GameState.Mode type
     */
    public abstract GameState.Mode getType();

}
