package darkkronicle.github.io.cloudfight;

import darkkronicle.github.io.cloudfight.buidler.MapObjects;
import darkkronicle.github.io.cloudfight.commands.game.TeamMessageCommand;
import darkkronicle.github.io.cloudfight.game.Items;
import darkkronicle.github.io.cloudfight.game.Shop;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.game.maps.Map;
import darkkronicle.github.io.cloudfight.utility.Box;
import darkkronicle.github.io.cloudfight.utility.ScoreHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles game related events.
 */
public class EventListener implements Listener {

    private final CloudFight plugin;
    private final Random random = new Random();

    public EventListener(CloudFight plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(CloudFight.PREFIX + CloudFight.color("&6" + player.getName() + " joined CloudFight."));
        plugin.returnToSpawn(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(CloudFight.PREFIX + ChatColor.GOLD + player.getName() + " left CloudFight.");
        if (plugin.mapEditPool.isEditing(player.getWorld().getName().replaceFirst("maps/", ""))) {
            plugin.mapEditPool.removePlayer(player, false);
        }
        plugin.gamePool.removePlayer(player);
        ScoreHelper.removeScore(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        // No hunger
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        // Prevent crafting
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        if (p.getGameMode() != GameMode.CREATIVE && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
        MapObjects.onInteract(plugin, event);
        GameContainer game = plugin.gamePool.getGame(event.getWhoClicked());
        if (game != null) {
            if (game.gameInstance != null) {
                game.gameInstance.onClick(event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            if (Shop.isShop(event.getEntity())) {
                event.setCancelled(true);
            }
            return;
        }
        Player player = (Player) event.getEntity();
        // If they're not in a game they shouldn't take damage.
        if (plugin.gamePool.getGame(player) == null && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.teleport(player.getLocation().clone().add(0, 20, 0));
            event.setDamage(player.getHealth());
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onArmorStandInteract(PlayerArmorStandManipulateEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.gamePool.getGame(e.getPlayer()) == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        GameContainer game = plugin.gamePool.getGame(event.getPlayer());
        game.onBreak(event);
        for (ItemStack b : Items.Default.BLOCKS) {
            if (b.getType() == event.getBlock().getType()) {
                event.getBlock().getDrops().clear();
                event.getBlock().getDrops().add(Items.Default.NONE_BLOCK.stack);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingerSplash(LingeringPotionSplashEvent event) {
        if (event.getEntity().getItem().equals(Items.Shop.GRENADE.stack)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(ProjectileHitEvent event) {
        Entity hit = event.getHitEntity();
        Projectile projectile = event.getEntity();
        // Fireballs hit with some projectiles sometimes don't get their shooter set. This forces it to get set correctly.
        if (projectile.getType() == EntityType.SPLASH_POTION) {
            ThrownPotion potion = (ThrownPotion) projectile;
            if (potion.getItem().equals(Items.Shop.GRENADE.stack)) {
                Location loc = potion.getLocation().clone().add(0, -1, 0);
                for (int i = 0; i < 3; i++) {
                    loc.getWorld().spawn(loc, AreaEffectCloud.class, (cloud) -> {
                        cloud.setRadius(3);
                        cloud.setDuration(200);
                        cloud.setDurationOnUse(1);
                        cloud.setSource(potion.getShooter());
                        cloud.setColor(Color.RED);
                        cloud.setReapplicationDelay(60);
                        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 10, 1), true);
                        cloud.setWaitTime(5);
                    });
                    loc.add(0, 1, 0);
                }
            }
        }

        if (hit != null && hit.getType() == EntityType.FIREBALL && projectile.getShooter() != null) {
            Fireball fireball = (Fireball) hit;
            fireball.setShooter(projectile.getShooter());
            // Set velocity correctly
            fireball.setDirection(projectile.getVelocity());
        }

        // Projectile knock back
        if (projectile.getType().equals(EntityType.EGG) || projectile.getType().equals(EntityType.SNOWBALL) || projectile.getType().equals(EntityType.ENDER_PEARL) || projectile.getType().equals(EntityType.FIREBALL)) {
            if (hit == null || (projectile.hasMetadata("shooter") && projectile.getShooter().equals(hit))) {
                return;
            }
            if (projectile.getType() == EntityType.FIREBALL) {
                hit.setVelocity(projectile.getVelocity().multiply(1).add(new Vector(0, 0.1, 0)));
            } else {
                hit.setVelocity(projectile.getVelocity().multiply(2));
            }
            hit.getWorld().playSound(hit.getLocation(), Sound.ENTITY_ARMOR_STAND_FALL, 3, 1);
            Location particle = hit.getLocation();
            for (int i = 0; i < 6; i++) {
                // Create a randomized smoke cloud.
                double x = random.nextDouble() - .5;
                double y = random.nextDouble() - .5;
                double z = random.nextDouble() - .5;

                particle.add(x, y, z);
                particle.getWorld().spawnParticle(Particle.SMOKE_LARGE, particle, 0);
            }

            // Damage if they are in the game.
            if (hit instanceof LivingEntity) {
                if (plugin.gamePool.getGame(hit) == null) {
                    return;
                }
                LivingEntity livhit = (LivingEntity) hit;
                livhit.damage(5, (Entity) projectile.getShooter());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            // Just in case somehow InteractExecutor doesn't stop this.
            if (plugin.gamePool.getGame(event.getEntity()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message.length() > 1 && message.toCharArray()[0] == '!') {
            event.setCancelled(true);
            TeamMessageCommand.teamMessage(event.getPlayer(), plugin, message.subSequence(1, message.length()).toString());
            return;
        }
        event.setFormat(ChatColor.GRAY + "%s:" + ChatColor.WHITE + " %s");
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Automatic saving and unloading of maps.
        if (plugin.mapEditPool.isEditing(event.getFrom().getName().replaceFirst("maps/", ""))) {
            plugin.mapEditPool.removePlayer(event.getPlayer(), false);
        }
    }


    @EventHandler()
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameContainer game = plugin.gamePool.getGame(player);
        if (game != null) {
            // Increment deaths
            game.onKill(event);
            game.deaths.increment(player);
            Player killer = player.getKiller();
            if (killer != null) {
                killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 2);
                if (plugin.gamePool.getGame(killer).equals(game)) {
                    game.kills.increment(killer);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        double health = damaged.getHealth();
        double damage = event.getFinalDamage();
        double new_health = health - damage;
        int new_hearts = (int) Math.floor(new_health / 2);
        int damaged_hearts = (int) Math.floor(damage / 2);
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i <= new_hearts) {
                string.append(ChatColor.DARK_RED + "❤");
            } else if (i <= new_hearts + damaged_hearts) {
                string.append(ChatColor.RED + "❤");
            } else {
                string.append(ChatColor.DARK_GRAY + "❤");
            }
        }
        String message = string.toString();
        damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @EventHandler(ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        GameContainer game = plugin.gamePool.getGame(event.getPlayer());
        if (game != null) {
            game.onRespawn(event);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        GameContainer game = plugin.gamePool.getGame(event.getPlayer());
        if (game != null) {
            Map map = game.gameInstance.map;
            if (!map.getConfig().getBoundary().isInBox(event.getBlockPlaced().getLocation())) {
                event.setCancelled(true);
                return;
            }
            for (Box b : (List<Box>) map.getConfig().getRestricted()) {
                if (b.isInBox(event.getBlockPlaced().getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getBlockAgainst().getType() == Material.BARRIER) {
                event.setCancelled(true);
                return;
            }
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getBlock().getType() == Material.TNT) {
                event.getBlock().setType(Material.AIR);
                Location spawn = event.getBlock().getLocation().add(0.5, 0, 0.5);
                TNTPrimed tnt = (TNTPrimed) spawn.getWorld().spawnEntity(spawn, EntityType.PRIMED_TNT);
                spawn.getWorld().playSound(spawn, Sound.ENTITY_TNT_PRIMED, 3, 1);
                tnt.setSource(event.getPlayer());
                tnt.setYield(4);
            }
            game.onPlace(event);
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Shop.handleInteract(event);
        if (event.getRightClicked().getType() == EntityType.PLAYER) {
            GameContainer game = plugin.gamePool.getGame(event.getPlayer());
            if (game != null) {
                game.onPlayerInteract(event.getPlayer(), (Player) event.getRightClicked());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        GameContainer game = plugin.gamePool.getGame(event.getEntity());
        if (game != null) {
            game.onExplode(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUp(EntityPickupItemEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        GameContainer game = plugin.gamePool.getGame(player);
        if (game == null) {
            return;
        }
        Team team = game.getPlayerTeam(player);
        if (team == null) {
            return;
        }
        boolean in = false;
        for (ItemStack block : Items.Default.BLOCKS) {
            if (block.getType() == event.getItem().getItemStack().getType()) {
                in = true;
                break;
            }
        }
        if (!in) {
            return;
        }
        ArrayList<Player> players = new ArrayList<>();
        if (event.getItem().hasMetadata("gen")) {
            // Lock to teams
            String key = event.getItem().getMetadata("gen").get(0).asString();
            if (!key.equalsIgnoreCase(team.getName())) {
                event.setCancelled(true);
                return;
            }
            for (Entity ent : event.getItem().getNearbyEntities(1.5, 1.5, 1.5)) {
                if (ent.getType() == EntityType.PLAYER) {
                    Player p = (Player) ent;
                    Team t = game.getPlayerTeam(p);
                    if (t != null && t.equals(team)) {
                        players.add(p);
                    }
                }
            }
        }
        players.remove(player);

        ItemStack block = Items.Default.getBlock(team).clone();
        block.setAmount(event.getItem().getItemStack().getAmount());
        event.getItem().setItemStack(block);
        for (Player p : players) {
            p.getInventory().addItem(block);
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onItemInteract(PlayerInteractEvent event) {
        Items.onInteract(plugin, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onXPChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();
        event.setAmount(0);
        int progress = (int) Math.floor(player.getExp() * 50) + amount + (player.getLevel() * 50);
        if (progress < 0) {
            progress = 0;
        }
        player.setLevel((int) Math.floor((float) progress / 50));
        player.setExp((float) (progress % 50) / 50);

    }
}
