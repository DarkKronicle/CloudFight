package darkkronicle.github.io.cloudfight.game.games;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.Generator;
import darkkronicle.github.io.cloudfight.game.Platform;
import darkkronicle.github.io.cloudfight.game.Shop;
import darkkronicle.github.io.cloudfight.game.maps.CaptureMap;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.Counter;
import darkkronicle.github.io.cloudfight.utility.FormattingUtils;
import darkkronicle.github.io.cloudfight.utility.ScoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class CaptureGame extends GameInstance<CaptureMap> {

    private int freeze_seconds = 180;

    private int last_freeze = 180;

    private final Counter<Team> points = new Counter<>();

    private final ArrayList<Platform> platforms = new ArrayList<>();

    private final HashMap<String, Generator> generators = new HashMap<>();

    private final int required = 200;

    private int xpTime = 10;

    private BossBar status = Bukkit.createBossBar(" ", BarColor.RED, BarStyle.SOLID);

    public CaptureGame(CloudFight plugin, GameContainer container) {
        super(plugin, container);
    }

    public void setFreezeSeconds(int amount) {
        if (freeze_seconds > 0 && amount < last_freeze) {
            freeze_seconds = amount;
        } else {
            freeze_seconds = amount;
            last_freeze = amount;
        }
    }

    @Override
    public void start(Map gamemap) {
        CaptureMap gmap;
        if (gamemap != null && gamemap.getType() == GameState.Mode.CAPTURE) {
            gmap = (CaptureMap) gamemap;
        } else {
            gmap = null;
        }
        super.start(gmap);
        int current = 0;
        for (Platform.PlatformStorage p : ((CaptureMap.CaptureMapConfiguration) map.getConfig()).platforms) {
            current++;
            p.getActivation().setWorld(container.world);
            p.getPlatform().setWorld(container.world);
            p.setPrefix(current + "");
            platforms.add(new Platform(plugin, container.world, p));
        }

        // Set shops
        for (Location s : ((CaptureMap.CaptureMapConfiguration) map.getConfig()).shop) {
            s = s.clone();
            s.setWorld(container.world);
            Shop.spawnShop(s);
        }

        // Setup generators
        for (java.util.Map.Entry<String, Location> g : ((CaptureMap.CaptureMapConfiguration) map.getConfig()).generators.entrySet()) {
            Location loc = g.getValue().clone();
            loc.setWorld(container.world);
            generators.put(g.getKey(), new Generator(plugin, this, loc, g.getKey()));
        }

        // Setup points
        for (java.util.Map.Entry<Team, ArrayList<Player>> p : container.players.entrySet()) {
            points.set(p.getKey(), 0);
        }
    }

    public void addFreeze(int freeze) {
        setFreezeSeconds(freeze_seconds + freeze);
    }

    @Override
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
        for (java.util.Map.Entry<Team, Counter.MutableInteger> entry : points.entrySet()) {
            String upper = entry.getKey().getName().substring(0, 1).toUpperCase() + entry.getKey().getName().substring(1);
            slots.add(" &l" + entry.getKey().getDisplayName() + "&r " + upper + ":&b " + (required - entry.getValue().get()));
        }
        slots.add("");
        // Platforms captured
        for (Platform p : platforms) {
            String acquired = p.getAcquired();
            String circle = "⬤";
            String one;
            String two;
            float percent;
            if (acquired == null) {
                one = "&7-";
            } else {
                Team team = container.getTeam(acquired);
                if (team != null) {
                    one = team.getColor() + "" + circle;
                } else {
                    one = "&f" + circle;
                }
            }
            if (p.isAcquiring()) {
                String to_acquire = p.getAcquiring();
                percent = p.getPercent();
                if (to_acquire == null) {
                    two = "&7-";
                } else {
                    Team team = container.getTeam(to_acquire);
                    if (team != null) {
                        two = team.getColor() + "" + circle;
                    } else {
                        two = "&f" + circle;
                    }
                }
            } else {
                percent = 1;
                two = one;
            }
            percent = 1 - Math.min(1, Math.max(0, percent));
            int total = 5;
            StringBuilder build = new StringBuilder();
            for (int i = 0; i < total; i++) {
                float current_percent = (float) i / total;
                if (current_percent <= percent) {
                    // Who acquired
                    build.append(two);
                } else {
                    // Acquiring
                    build.append(one);
                }
            }
            slots.add(" &l" + p.getPrefix() + "&r:&b " + build.toString());

        }
        slots.add("");
        // Misc stats
        slots.add(" Deaths: &b" + container.deaths.get(player));
        slots.add(" Kills: &b" + container.kills.get(player));
        score.setSlotsFromList(slots);

        score.setSlotsFromList(slots);
    }

    @Override
    protected void game_loop() {
        for (Platform p : platforms) {
            p.second(this, container.players.entrySet());
            if (p.getAcquired() != null && !p.isAcquiring() && freeze_seconds <= 0) {
                points.increment(container.getTeam(p.getAcquired()));
            }
        }
        for (java.util.Map.Entry<Team, Counter.MutableInteger> e : points.entrySet()) {
            if (e.getValue().get() >= required) {
                e.getValue().set(required);
                endUnchecked(e.getKey());
                break;
            }
        }

        if (freeze_seconds > 0) {
            if (freeze_seconds % 60 == 0) {
                for (Player p : container.allPlayers()) {
                    p.sendMessage(CloudFight.color(CloudFight.PREFIX + "&7Points are frozen for &c&l" + (freeze_seconds / 60) + "&7 minutes!"));
                }
            } else if (freeze_seconds == 30) {
                for (Player p : container.allPlayers()) {
                    p.sendMessage(CloudFight.color(CloudFight.PREFIX + "&7Points are frozen for &c&l30&7 seconds!"));
                }
            } else if (freeze_seconds <= 5) {
                for (Player p : container.allPlayers()) {
                    p.sendMessage(CloudFight.color(CloudFight.PREFIX + "&7Points are frozen for &c&l" + freeze_seconds + "&7 seconds!"));
                }
            }
            // TODO remove players who aren't in game
            for (Player p : container.allPlayers()) {
                status.addPlayer(p);
            }
            status.setProgress(Math.max(0, Math.min(1, (float) freeze_seconds / last_freeze)));
            status.setTitle(ChatColor.RED + "POINTS ARE FROZEN " + FormattingUtils.toMinuteSecond(freeze_seconds, false));
            status.setVisible(true);
        } else {
            status.removeAll();
        }
        freeze_seconds--;
        freeze_seconds = Math.max(freeze_seconds, 0);
        super.game_loop();
    }

    @Override
    protected void game_tick() {
        for (Generator g : generators.values()) {
            g.tick();
        }
        for (Platform p : platforms) {
            p.tick();
        }
        if (container.tick % xpTime == 0) {
            xpTime = (int) Math.max((-1 * Math.ceil(Math.exp(((float) time / 60) / 7)) + 11), 3);
            for (Location x : ((CaptureMap.CaptureMapConfiguration) map.getConfig()).xp) {
                x = x.clone();
                x.setWorld(container.world);
                container.world.spawn(x, ExperienceOrb.class, (xp) -> {
                    xp.setVelocity(new Vector(0, -1, 0));
                    xp.setExperience(2);
                });
            }
        }
    }

    @Override
    public void endUnchecked(Team winner) {
        // Remove players from platform boss bars
        for (Platform p : platforms) {
            p.removeAll();
        }
        status.removeAll();
        super.endUnchecked(winner);
    }

    public Generator getGenerator(String key) {
        return generators.get(key);
    }

    @Override
    public GameState.Mode getType() {
        return GameState.Mode.CAPTURE;
    }
}
