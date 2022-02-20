package darkkronicle.github.io.cloudfight.buidler;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.utility.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;

public class MapObjects {

    private MapObjects() {

    }

    private static final ItemStack PURPLE = new ItemBuilder(Material.PURPLE_CONCRETE).name("&5Purple").build();
    private static final ItemStack CYAN = new ItemBuilder(Material.CYAN_CONCRETE).name("&2Cyan").build();

    public enum Items {
        SPAWN(new ItemBuilder(Material.TOTEM_OF_UNDYING).name("&6Spawnpoints").build()),
        REMOVE_PLATFORMS(new ItemBuilder(Material.BARRIER).name("&cRemove Platforms").build()),
        ADD_PLATFORM(new ItemBuilder(Material.ENDER_PEARL).name("&aAdd Platform").build()),
        ENABLE(new ItemBuilder(Material.GREEN_CONCRETE).name("&aEnable Map").build()),
        DISABLE(new ItemBuilder(Material.RED_CONCRETE).name("&cDisable Map").build()),
        SET_BOUND_1(new ItemBuilder(Material.DIAMOND_BLOCK).name("&bSet Bound 1").build()),
        SET_BOUND_2(new ItemBuilder(Material.DIRT).name("&bSet Bound 2").build()),
        REMOVE_SHOPS(new ItemBuilder(Material.VILLAGER_SPAWN_EGG).name("&cRemove Shops").build()),
        ADD_SHOP(new ItemBuilder(Material.EMERALD).name("&aAdd Shop").build()),
        REMOVE_XP(new ItemBuilder(Material.WOODEN_SWORD).name("&cRemove XP").build()),
        ADD_XP(new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&aAdd XP").build()),
        GENERATOR(new ItemBuilder(Material.DISPENSER).name("&6Generators").build()),
        ;

        public final ItemStack stack;

        Items(ItemStack stack) {
            this.stack = stack;
        }
    }

    private interface Input {
        boolean accept(Player player, ItemStack item);
    }

    private static Inventory choice(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, MENU_TITLE);
        inventory.setItem(2, CYAN);
        inventory.setItem(6, PURPLE);
        return inventory;
    }

    private final static HashMap<Player, Input> WAITING_ON_INPUT = new HashMap<>();

    private final static String MENU_TITLE = ChatColor.BLUE + "Objects";

    private static void removeArmorTags(World world, String tag, String name) {
        for (ArmorStand arm : world.getEntitiesByClass(ArmorStand.class)) {
            if (arm.hasMetadata("map")) {
                if (name == null || (arm.getCustomName() != null && arm.getCustomName().equalsIgnoreCase(name))) {
                    String val = arm.getMetadata("map").get(0).asString();
                    if (val.equalsIgnoreCase(tag)) {
                        arm.remove();
                    }
                }
            }
        }
    }

    private static void removePlatforms(CloudFight plugin, Player player, MapEditContainer container) {
        removeArmorTags(container.world, "platform", null);
    }

    private static void addPlatform(CloudFight plugin, Player player, MapEditContainer container) {
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName("Platform");
        stand.setMetadata("map", new FixedMetadataValue(plugin, "platform"));
    }

    private static void removeXP(CloudFight plugin, Player player, MapEditContainer container) {
        removeArmorTags(container.world, "xp", null);
    }

    private static void addXP(CloudFight plugin, Player player, MapEditContainer container) {
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName("XP");
        stand.setMetadata("map", new FixedMetadataValue(plugin, "xp"));
    }

    private static void removeShops(CloudFight plugin, Player player, MapEditContainer container) {
        removeArmorTags(container.world, "shop", null);
    }

    private static void addShop(CloudFight plugin, Player player, MapEditContainer container) {
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName("Shop");
        stand.setMetadata("map", new FixedMetadataValue(plugin, "shop"));
    }

    private static void setActive(CloudFight plugin, Player player, MapEditContainer container, boolean active) {
        removeArmorTags(container.world, "disabled", null);
        removeArmorTags(container.world, "active", null);
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        if (active) {
            stand.setCustomName("Active");
            stand.setMetadata("map", new FixedMetadataValue(plugin, "active"));
        } else {
            stand.setCustomName("Disabled");
            stand.setMetadata("map", new FixedMetadataValue(plugin, "disabled"));
        }
    }

    private static void setBound1(CloudFight plugin, Player player, MapEditContainer container) {
        removeArmorTags(container.world, "bound1", null);
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setMetadata("map", new FixedMetadataValue(plugin, "bound1"));
        stand.setCustomName("Boundary 1--");
    }

    private static void setBound2(CloudFight plugin, Player player, MapEditContainer container) {
        removeArmorTags(container.world, "bound2", null);
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setMetadata("map", new FixedMetadataValue(plugin, "bound2"));
        stand.setCustomName("Boundary 2++");
    }

    private static void spawn(CloudFight plugin, Player player, MapEditContainer container) {
        player.openInventory(choice(player));
        WAITING_ON_INPUT.put(player, (player1, item) -> {
            if (item.equals(CYAN)) {
                World world = container.world;
                removeArmorTags(world, "spawn", "cyan-spawn");
                ArmorStand stand = (ArmorStand) world.spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName("cyan-spawn");
                stand.setMetadata("map", new FixedMetadataValue(plugin, "spawn"));
            } else if (item.equals(PURPLE)) {
                World world = container.world;
                removeArmorTags(world, "spawn", "purple-spawn");
                ArmorStand stand = (ArmorStand) world.spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName("purple-spawn");
                stand.setMetadata("map", new FixedMetadataValue(plugin, "spawn"));
            } else {
                return false;
            }
            return true;
        });
    }

    private static void generator(CloudFight plugin, Player player, MapEditContainer container) {
        player.openInventory(choice(player));
        WAITING_ON_INPUT.put(player, (player1, item) -> {
            if (item.equals(CYAN)) {
                World world = container.world;
                removeArmorTags(world, "gen", "cyan-generator");
                ArmorStand stand = (ArmorStand) world.spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName("cyan-generator");
                stand.setMetadata("map", new FixedMetadataValue(plugin, "gen"));
            } else if (item.equals(PURPLE)) {
                World world = container.world;
                removeArmorTags(world, "gen", "purple-generator");
                ArmorStand stand = (ArmorStand) world.spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName("purple-generator");
                stand.setMetadata("map", new FixedMetadataValue(plugin, "gen"));
            } else {
                return false;
            }
            return true;
        });
    }

    public static void openInventory(Player player) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (Items item : Items.values()) {
            items.add(item.stack);
        }
        int slots = (int) (Math.ceil((double) items.size() / 9)) * 9;
        Inventory inventory = Bukkit.createInventory(player, slots, MENU_TITLE);
        inventory.setContents(items.toArray(new ItemStack[0]));
        player.openInventory(inventory);
    }

    public static void onInteract(CloudFight plugin, InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equalsIgnoreCase(MENU_TITLE)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        MapEditContainer container = plugin.mapEditPool.isEditing(player);
        if (container == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        Input waiting = WAITING_ON_INPUT.get(player);
        if (waiting != null) {
            WAITING_ON_INPUT.remove(player);
            if (waiting.accept(player, item)) {
                return;
            }
        }
        if (item.equals(Items.SPAWN.stack)) {
            spawn(plugin, player, container);
        } else if (item.equals(Items.ADD_PLATFORM.stack)) {
            addPlatform(plugin, player, container);
        } else if (item.equals(Items.REMOVE_PLATFORMS.stack)) {
            removePlatforms(plugin, player, container);
        } else if (item.equals(Items.ENABLE.stack)) {
            setActive(plugin, player, container, true);
        } else if (item.equals(Items.DISABLE.stack)) {
            setActive(plugin, player, container, false);
        } else if (item.equals(Items.SET_BOUND_2.stack)) {
            setBound2(plugin, player, container);
        } else if (item.equals(Items.SET_BOUND_1.stack)) {
            setBound1(plugin, player, container);
        } else if (item.equals(Items.REMOVE_SHOPS.stack)) {
            removeShops(plugin, player, container);
        } else if (item.equals(Items.ADD_SHOP.stack)) {
            addShop(plugin, player, container);
        } else if (item.equals(Items.REMOVE_XP.stack)) {
            removeXP(plugin, player, container);
        } else if (item.equals(Items.ADD_XP.stack)) {
            addXP(plugin, player, container);
        } else if (item.equals(Items.GENERATOR.stack)) {
            generator(plugin, player, container);
        }

    }

}
