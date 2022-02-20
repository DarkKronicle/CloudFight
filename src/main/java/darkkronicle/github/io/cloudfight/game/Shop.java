package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.utility.ItemBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Shop {

    public final static String MENU_TITLE = ChatColor.GOLD + "Shop";
    private final static ItemStack TEAM_ITEM = new ItemBuilder(Material.TOTEM_OF_UNDYING).name("Team Upgrades").build();
    private final static ItemStack SHOP_ITEM = new ItemBuilder(Material.WOODEN_SWORD).name("Item Shop").build();

    public enum Category {
        BLOCK(new ItemBuilder(Material.WHITE_CONCRETE).name("Blocks").build()),
        WEAPON(new ItemBuilder(Material.DIAMOND_SWORD).name("Weapons").build()),
        UTILITY(new ItemBuilder(Material.PISTON).name("Utility").build()),
        ;

        public final ItemStack stack;

        Category(ItemStack stack) {
            this.stack = stack;
        }
    }

    public enum Item {
        STONE_SWORD(Category.WEAPON, 32, new ItemBuilder(Material.STONE_SWORD).lore("32 Blocks").build(), (player) -> {
            player.getInventory().remove(Material.WOODEN_SWORD);
            player.getInventory().addItem(Items.Shop.STONE_SWORD.stack);
        }),
        LADDERS(Category.BLOCK, 16, new ItemBuilder(Material.LADDER, 8).lore("16 Blocks").build(), Items.Shop.LADDERS.stack),
        BOW(Category.WEAPON, 64, new ItemBuilder(Material.BOW).lore("64 Blocks").build(), Items.Shop.BOW.stack),
        ARROWS(Category.WEAPON, 32, new ItemBuilder(Material.ARROW, 16).lore("32 Blocks").build(), Items.Shop.ARROWS.stack),
        TNT(Category.UTILITY, 16, new ItemBuilder(Material.TNT).lore("16 Blocks").build(), Items.Shop.TNT.stack),
        GAPPLE(Category.UTILITY, 16, new ItemBuilder(Material.GOLDEN_APPLE).lore("16 Blocks").build(), Items.Shop.GAPPLE.stack),
        WATER(Category.UTILITY, 32, new ItemBuilder(Material.WATER_BUCKET).lore("32 Blocks").build(), Items.Shop.WATER.stack),
        BOOST(Category.UTILITY, 32, new ItemBuilder(Material.FEATHER).name("Boost").lore("32 Blocks").build(), Items.Shop.BOOST.stack),
        GRENADE(Category.WEAPON, 48, new ItemBuilder(Material.LINGERING_POTION).name("Grenade").lore("48 Blocks").build(), Items.Shop.GRENADE.stack),
        IRON_ARMOR(Category.WEAPON,  64, new ItemBuilder(Material.IRON_BOOTS).lore("64 blocks").build(), (player) -> {
            player.getInventory().setItem(36, Items.Shop.IRON_BOOTS.stack);
            player.getInventory().setItem(37, Items.Shop.IRON_LEGGINGS.stack);
        }, (player) -> !Objects.equals(player.getInventory().getItem(36), Items.Shop.IRON_BOOTS.stack)),
        ;

        public final int cost;
        public final ItemStack menu;
        public final Consumer<Player> give;
        public final Function<Player, Boolean> can_give;
        public final Category category;

        Item(Category category, int cost, ItemStack menu, ItemStack give) {
            this(category, cost, menu, (player) -> player.getInventory().addItem(give));
        }

        Item(Category category, int cost, ItemStack menu, Consumer<Player> give) {
            this(category, cost, menu, give, (player) -> true);
        }

        Item(Category category, int cost, ItemStack menu, Consumer<Player> give, Function<Player, Boolean> can_give) {
            this.cost = cost;
            this.menu = menu;
            this.give = give;
            this.can_give = can_give;
            this.category = category;
        }
    }

    protected final GameContainer game;
    private final TeamUpgrades upgrades;

    public Shop(GameContainer game) {
        this.game = game;
        this.upgrades = new TeamUpgrades(this);
    }

    public static void spawnShop(Location location) {
        World world = location.getWorld();
        world.spawn(location, Villager.class, (villager) -> {
            villager.setAI(false);
            villager.setProfession(Villager.Profession.NITWIT);
            villager.setSilent(true);
            villager.setCustomName("Right click!");
            villager.setCustomNameVisible(true);
            villager.setInvulnerable(true);
            villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1000);
            villager.setHealth(1000);
        });
    }

    public static boolean isShop(Entity entity) {
        if (entity.getType() != EntityType.VILLAGER) {
            return false;
        }
        return entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase("Right click!");
    }

    public static void openShop(Player player, Category category) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (Item item : Item.values()) {
            if (item.category == category) {
                items.add(item.menu);
            } else if (category == null) {
                items.add(item.menu);
            }
        }
        int slots = 54;
        Inventory inventory = Bukkit.createInventory(player, slots, MENU_TITLE);
        int current = 0;
        for (Category cat : Category.values()) {
            if (cat == category) {
                inventory.setItem(current, new ItemBuilder(cat.stack.clone()).addEnchantment(Enchantment.DURABILITY, 1).addFlags(ItemFlag.HIDE_ENCHANTS).build());
            } else {
                inventory.setItem(current, cat.stack);
            }
            current++;
        }
        int row = (int) Math.ceil((float) current / 9) + 1;
        int slot = 0;
        for (ItemStack item : items) {
            int pos = slot % 7;
            int r = row + (int) Math.floor((float) slot / 7);
            inventory.setItem((r * 9) + 1 + pos, item);
            slot++;
        }
        player.openInventory(inventory);
    }

    public static void openShop(Player player) {
        openShop(player, null);
    }

    public static void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, MENU_TITLE);
        inventory.setItem(2, SHOP_ITEM);
        inventory.setItem(6, TEAM_ITEM);
        player.openInventory(inventory);
    }

    public static void handleInteract(PlayerInteractEntityEvent event) {
        if (!isShop(event.getRightClicked())) {
            return;
        }
        event.setCancelled(true);
        openMenu(event.getPlayer());
    }

    public void handleClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equalsIgnoreCase(MENU_TITLE)) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        if (item.equals(TEAM_ITEM)) {
            upgrades.openShop((Player) event.getWhoClicked());
            return;
        } else if (item.equals(SHOP_ITEM)) {
            openShop((Player) event.getWhoClicked());
            return;
        }
        for (Category cat : Category.values()) {
            if (cat.stack.equals(item)) {
                openShop((Player) event.getWhoClicked(), cat);
                return;
            }
        }

        Item clicked = null;
        for (Item i : Item.values()) {
            if (i.menu.equals(item)) {
                clicked = i;
                break;
            }
        }
        if (clicked == null) {
            upgrades.handleClick(event);
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!clicked.can_give.apply(player)) {
            return;
        }
        HashMap<ItemStack, Integer> items = ItemBuilder.getAllPlayerItems(player, true);
        int current = 0;
        ItemStack[] currency = Items.Default.BLOCKS;
        for (ItemStack c : currency) {
            if (items.containsKey(c)) {
                current += items.get(c);
            }
        }
        if (current >= clicked.cost) {
            PlayerInventory inv = player.getInventory();
            int paid = 0;
            for (ItemStack c : currency) {
                ItemStack offhand = inv.getItemInOffHand();
                int camount = offhand.getAmount();
                ItemStack coff = offhand.clone();
                coff.setAmount(1);
                if (coff.equals(c)) {
                    offhand.setAmount(camount - (clicked.cost - paid));
                    paid = Math.min(camount + paid, clicked.cost);
                    inv.setItemInOffHand(offhand);
                }
                if (paid >= clicked.cost) {
                    break;
                }
                ItemStack remove = c.clone();
                remove.setAmount(clicked.cost - paid);
                HashMap<Integer, ItemStack> not = player.getInventory().removeItem(remove);
                if (not.size() == 0) {
                    break;
                }
                paid = paid + (clicked.cost - not.values().iterator().next().getAmount());
            }
            player.updateInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            player.spigot().sendMessage(TextComponent.fromLegacyText(CloudFight.color("&aPurchased!")));
            clicked.give.accept(player);
        } else {
            player.spigot().sendMessage(TextComponent.fromLegacyText(CloudFight.color("&cYou need " + (clicked.cost - current) + " more blocks!")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }

    public void second() {
        upgrades.second();
    }

}
