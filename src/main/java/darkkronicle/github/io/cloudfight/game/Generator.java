package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.GameInstance;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;

public class Generator {
    /**
     * Where the generator is located
     */
    private final Location location;

    /**
     * Current amount of ticks
     */
    private int ticks = 0;

    /**
     * How many ticks it takes to spawn an item
     */
    private int speed = 20;

    /**
     * How many players use this generator. Used to determine how many items spawn for each drop.
     * <p>
     * Equation is ItemAmount = floor(log (size) * 3)
     */
    private int size = 0;

    /**
     * How much the generator is upgraded.
     */
    public int level = 0;

    /**
     * How many items can be by this generator until it stops generating
     */
    private final int max = 128;

    private final CloudFight plugin;
    private final GameInstance game;

    /**
     * The team it is part of
     */
    private final String key;

    public Generator(CloudFight plugin, GameInstance container, Location location, String key) {
        this.location = location;
        this.game = container;
        this.plugin = plugin;
        this.key = key;
    }

    public void tick() {
        ticks++;
        if (ticks % speed == 0) {
            if (!overMax()) {
                spawnItem();
            }
            // Recalculate speed
            List<Player> p = game.container.getTeamPlayers(game.container.getTeam(key));
            size = 1;
            if (p != null) {
                size = p.size();
            }
            if (size <= 0) {
                size = 1;
            }
            speed = 20 - (level * 5);
            if (speed < 5) {
                speed = 5;
            }
        }
    }

    /**
     * Checks if there are more than the maximum amount of items for this generator
     *
     * @return If it is over the maximum
     */
    public boolean overMax() {
        int amount = 0;
        for (Entity ent : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (ent.getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item) ent;
                ItemStack stack = item.getItemStack().clone();
                stack.setAmount(1);
                if (!stack.equals(Items.Default.NONE_BLOCK.stack)) {
                    continue;
                }
                amount = amount + item.getItemStack().getAmount();
            }
        }
        return amount > max;
    }

    /**
     * Triggers a spawn item
     */
    public void spawnItem() {
        location.getWorld().spawn(location, Item.class, (i) -> {
            ItemStack stack = Items.Default.NONE_BLOCK.stack.clone();
            // Calculate the modified item size
            stack.setAmount((int) Math.floor(Math.log(size) * 3 + 1));
            i.setItemStack(Items.Default.NONE_BLOCK.stack);
            // Don't want it flying around
            i.setVelocity(new Vector(0, 0, 0));
            i.setPickupDelay(10);
            i.setMetadata("gen", new FixedMetadataValue(plugin, key));
        });
    }

}
