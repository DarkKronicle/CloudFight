package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.utility.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public interface Items {

    enum Shop {
        STONE_SWORD(new ItemBuilder(Material.STONE_SWORD).unbreakable().build()),
        LADDERS(new ItemBuilder(Material.LADDER, 8).build()),
        BOW(new ItemBuilder(Material.BOW).unbreakable().build()),
        ARROWS(new ItemBuilder(Material.ARROW, 16).build()),
        TNT(new ItemBuilder(Material.TNT).build()),
        GAPPLE(new ItemBuilder(Material.GOLDEN_APPLE).build()),
        WATER(new ItemBuilder(Material.WATER_BUCKET).build()),
        BOOST(new ItemBuilder(Material.FEATHER).lore("Right click to launch!").build()),
        IRON_BOOTS(new ItemBuilder(Material.IRON_BOOTS).name("Iron Boots").unbreakable().build()),
        IRON_LEGGINGS(new ItemBuilder(Material.IRON_LEGGINGS).unbreakable().name("Iron Legs").build()),
        GRENADE(new ItemBuilder(Material.LINGERING_POTION).name("Grenade").build()),
        ;

        public final ItemStack stack;

        Shop(ItemStack stack) {
            this.stack = stack;
        }
    }

    enum Default {
        WOOD_SWORD(null,
                new ItemBuilder(Material.WOODEN_SWORD).name("&6Sword").unbreakable().build(), 0),
        WOOD_PICK(null,
                new ItemBuilder(Material.WOODEN_PICKAXE).name("&6Pick").unbreakable().addEnchantment(Enchantment.DIG_SPEED, 2).build(), 2),
        CYAN_BLOCK(CloudFight.CYAN_NAME,
                new ItemBuilder(Material.CYAN_CONCRETE).name("&dBlock").build(), null),
        PURPLE_BLOCK(CloudFight.PURPLE_NAME,
                new ItemBuilder(Material.PURPLE_CONCRETE).name("&dBlock").build(), null),
        NONE_BLOCK(null,
                new ItemBuilder(Material.WHITE_CONCRETE).name("&dBlock").build(), null),
        CYAN_CHESTPLATE(CloudFight.CYAN_NAME,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).name("Cyan Chestplate").color(Color.AQUA).unbreakable().build(), 38),
        CYAN_LEGGINGS(CloudFight.CYAN_NAME,
                new ItemBuilder(Material.LEATHER_LEGGINGS).name("Cyan Leggings").color(Color.AQUA).unbreakable().build(), 37),
        CYAN_BOOTS(CloudFight.CYAN_NAME,
                new ItemBuilder(Material.LEATHER_BOOTS).name("Cyan Boots").color(Color.AQUA).unbreakable().build(), 36),
        PURPLE_CHESTPLATE(CloudFight.PURPLE_NAME,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).name("Purple Chestplate").color(Color.PURPLE).unbreakable().build(), 38),
        PURPLE_LEGGINGS(CloudFight.PURPLE_NAME,
                new ItemBuilder(Material.LEATHER_LEGGINGS).name("Purple Leggings").color(Color.PURPLE).unbreakable().build(), 37),
        PURPLE_BOOTS(CloudFight.PURPLE_NAME,
                new ItemBuilder(Material.LEATHER_BOOTS).name("Purple Boots").color(Color.PURPLE).unbreakable().build(), 36);
        public final ItemStack stack;
        public final Integer slot;
        public String team;
        public static final ItemStack[] BLOCKS = new ItemStack[]{CYAN_BLOCK.stack, PURPLE_BLOCK.stack, NONE_BLOCK.stack};

        Default(String team, ItemStack stack, Integer slot) {
            this.stack = stack;
            this.slot = slot;
            this.team = team;
        }

        public static ItemStack getBlock(Team team) {
            if (team.getName().equalsIgnoreCase(CYAN_BLOCK.team)) {
                return CYAN_BLOCK.stack;
            }
            if (team.getName().equalsIgnoreCase(PURPLE_BLOCK.team)) {
                return PURPLE_BLOCK.stack;
            }
            return NONE_BLOCK.stack;
        }

    }

    static boolean itemEquals(ItemStack one, ItemStack two) {
        one = one.clone();
        one.setAmount(two.getAmount());
        return one.equals(two);
    }

    static void onInteract(CloudFight plugin, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Player player = event.getPlayer();
        if (itemEquals(Items.Shop.BOOST.stack, item)) {
            player.setVelocity(player.getLocation().getDirection().multiply(2));
            player.setFallDistance(0);
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
        }
    }

}
