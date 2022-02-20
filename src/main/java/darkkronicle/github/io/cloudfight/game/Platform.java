package darkkronicle.github.io.cloudfight.game;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.GameInstance;
import darkkronicle.github.io.cloudfight.utility.Box;
import darkkronicle.github.io.cloudfight.utility.Counter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Platform {

    @Data
    @AllArgsConstructor
    public static class PlatformStorage {
        private int requiredTicks;
        private Box platform;
        private Box activation;
        private ArrayList<String> blacklisted;
        private String prefix;

        public PlatformStorage() {
            requiredTicks = 100;
            platform = Box.fromPoint(new Location(null, 0, 100, 0), 5, 0, 5, 5, 0, 5);
            activation = Box.fromPoint(new Location(null, 0, 105, 0), 5);
            blacklisted = new ArrayList<>();
            prefix = "A";
        }
    }

    private final HashMap<String, ArrayList<BlockState>> blocks = new HashMap<>();
    private final ArrayList<BlockState> repair = new ArrayList<>();
    private final BossBar bar = Bukkit.createBossBar(" ", BarColor.WHITE, BarStyle.SOLID);
    private final HashMap<String, BarColor> colors = new HashMap<>();
    private final PlatformStorage storage;

    private String acquired = null;
    private String to_acquire = null;
    private int acquire_ticks_left = 0;
    private int current_ticks = 0;

    public Platform(CloudFight plugin, World world, PlatformStorage storage) {
        this.storage = storage;
        storage.getPlatform().loc1.setWorld(world);
        storage.getPlatform().setWorld(world);
        storage.getActivation().setWorld(world);
        loadAndSave(plugin, "game/structures/platform.nbt", null, BarColor.WHITE, () ->
                loadAndSave(plugin, "cyan/structures/platform.nbt", "cyan", BarColor.BLUE, () ->
                        loadAndSave(plugin, "purple/structures/platform.nbt", "purple", BarColor.PURPLE, () ->
                                loadAndSave(plugin, "game/structures/blankplatform.nbt", "none", BarColor.RED, () -> {
                                }))));
    }

    public String getPrefix() {
        return storage.prefix;
    }

    public void loadAndSave(CloudFight plugin, String name, String key, BarColor color, Runnable done) {
        if (key != null) {
            key = key.toLowerCase();
        }
        colors.put(key, color);
        final String k = key;
        StructureBlockLibApi.INSTANCE
                .loadStructure(plugin)
                .at(storage.getPlatform().loc1)
                .loadFromPath(plugin.getStructurePath().resolve(name))
                .onException(e -> plugin.getLogger().log(Level.SEVERE, "Failed to load structure.", e))
                .onResult(e -> {
                    ArrayList<BlockState> snapShot = new ArrayList<>();
                    for (Location loc : storage.getPlatform().list()) {
                        Block block = loc.getBlock();
                        if (block.getType() != Material.AIR) {
                            snapShot.add(block.getState());
                        }
                    }
                    blocks.put(k, snapShot);
                    done.run();
                });
    }

    public void update() {
        if (repair.size() > 0) {
            Collections.shuffle(repair);
            int amount = (int) Math.ceil((double) repair.size() / 20);
            for (int i = 0; i < amount; i++) {
                if (repair.size() != 0) {
                    BlockState r = repair.get(0);
                    repair.remove(0);
                    r.update(true, false);
                }
            }
        }
        ArrayList<BlockState> blockStates;
        if (acquire_ticks_left <= 0) {
            blockStates = blocks.get(acquired);
        } else {
            blockStates = blocks.get(to_acquire);
        }
        if (blockStates == null) {
            blockStates = blocks.get("none");
        }
        if (blockStates != null) {
            for (BlockState b : blockStates) {
                if (b.getLocation().getBlock().getState().getType() != b.getType()) {
                    if (!repair.contains(b)) {
                        repair.add(b);
                    }
                }
            }
        }
    }

    public String getAcquired() {
        return acquired;
    }

    public String getAcquiring() {
        return to_acquire;
    }

    public void tick() {
        current_ticks++;
        if (current_ticks % 5 == 0) {
            update();
        }
        if ((acquired == null && to_acquire == null) || (to_acquire != null && to_acquire.equalsIgnoreCase(acquired))) {
            return;
        }
        acquire_ticks_left--;
        // Prevent negatives from surfacing
        acquire_ticks_left = Math.max(acquire_ticks_left, 0);
    }

    public void setToAcquire(String acquire) {
        // If the outcome is different, we reset the to_acquire counter.
        if (acquire == null) {
            if (to_acquire != null) {
                acquire_ticks_left = storage.getRequiredTicks();
                to_acquire = null;
                bar.setColor(colors.get(acquire));
            }
        } else if (to_acquire == null || !to_acquire.equalsIgnoreCase(acquire)) {
            acquire_ticks_left = storage.getRequiredTicks();
            to_acquire = acquire;
            bar.setColor(colors.get(acquire));
        }
    }

    public void removeAll() {
        bar.removeAll();
    }

    public void second(GameInstance instance, Set<Map.Entry<Team, ArrayList<Player>>> playerTeams) {
        // We start by getting all players on the platform and dividing it by teams
        Counter<Team> current = new Counter<>();
        ArrayList<Player> allPlayers = new ArrayList<>();
        ArrayList<Player> inPlayers = new ArrayList<>();
        for (Map.Entry<Team, ArrayList<Player>> players : playerTeams) {
            allPlayers.addAll(players.getValue());
            for (Player p : players.getValue()) {
                if (storage.getActivation().isInBox(p.getLocation())) {
                    inPlayers.add(p);
                    current.increment(players.getKey());
                }
            }
        }
        // If there are multiple players on the platform, we set the to_acquire to null (they're canceling each other out)
        if (current.size() == 0) {
            // Return things back to normal if they couldn't get it working.
            setToAcquire(acquired);
        } else if (current.size() > 1) {
            setToAcquire(null);
        } else {
            // Now that one team is in there we can now choose them to set acquire
            Team winning = current.entrySet().iterator().next().getKey();
            setToAcquire(winning.getName());
        }

        current_ticks++;

        for (Player p : inPlayers) {
            bar.addPlayer(p);
        }

        for (Player b : bar.getPlayers()) {
            if (!inPlayers.contains(b)) {
                bar.removePlayer(b);
            }
        }

        // We can move into decrementing who it needs to be acquired.
        if ((to_acquire == null && acquired == null)) {
            // It's already been captured
            bar.setProgress(1);
            bar.setColor(colors.get(acquired));
            bar.setTitle("No one has captured the point!");
            return;
        } else if (to_acquire != null && to_acquire.equalsIgnoreCase(acquired)) {
            bar.setProgress(1);
            bar.setColor(colors.get(acquired));
            bar.setTitle(acquired + " has captured the point!");
            return;
        }
        float percent = (float) acquire_ticks_left / storage.getRequiredTicks();
        percent = Math.max(0, percent);
        percent = Math.min(1, percent);
        bar.setProgress(1 - percent);
        String a = acquired == null ? "no one" : acquired;
        String t = to_acquire == null ? "No one" : to_acquire;
        bar.setTitle(t + " is taking the point away from " + a);

        if (acquire_ticks_left <= 0) {
            for (Map.Entry<Team, ArrayList<Player>> p : playerTeams) {
                if (acquired != null && p.getKey().getName().equalsIgnoreCase(acquired)) {
                    for (Player play : p.getValue()) {
                        play.playSound(play.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
                    }
                }
                if (to_acquire != null && p.getKey().getName().equalsIgnoreCase(to_acquire)) {
                    for (Player play : p.getValue()) {
                        play.playSound(play.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1);
                    }
                }
            }
            acquired = to_acquire;
            current_ticks = 0;
            String acquire;
            if (acquired == null) {
                acquire = CloudFight.PREFIX + "Point " + storage.getPrefix() + " has been uncaptured!";
            } else {
                acquire = CloudFight.PREFIX + acquired + " has captured point " + storage.getPrefix() + "!";
            }
            for (Player p : instance.container.allPlayers()) {
                p.sendMessage(acquire);
            }
        }

    }

    public float getPercent() {
        return (float) acquire_ticks_left / storage.getRequiredTicks();
    }

    public boolean isAcquiring() {
        if (acquired == null && to_acquire != null) {
            return true;
        }
        if (to_acquire == null && acquired != null) {
            return true;
        }
        if (to_acquire != null && !to_acquire.equals(acquired)) {
            return true;
        }
        return false;
    }
}
