package darkkronicle.github.io.cloudfight.game;

import com.google.common.collect.ImmutableMap;
import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.CaptureGame;
import darkkronicle.github.io.cloudfight.utility.FormattingUtils;
import darkkronicle.github.io.cloudfight.utility.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// @TODO this works for now, but TeamUpgrades aren't always universal. Need to make this abstract...
public class TeamUpgrades {

    private static class UpgradeKeeper {
        public Upgrade speed = Upgrade.fromItem(new int[]{10, 20}, new String[]{"Speed I", "Speed II"},
                new ItemBuilder(Material.FEATHER).name("Speed").build());
        public Upgrade freeze = Upgrade.fromItem(new int[]{15, 20}, new String[]{"Freeze Platforms for 30 Seconds", "Freeze Platforms for 30 seconds"},
                new ItemBuilder(Material.ICE).name("Point Freeze").build());
        public Upgrade protection = Upgrade.fromItem(new int[]{10, 15, 20, 25}, new String[]{"Protection I", "Protection II", "Protection III", "Protection IV"},
                new ItemBuilder(Material.IRON_CHESTPLATE).name("Protection").build());
        public Upgrade self_gen = Upgrade.fromItem(new int[]{5, 10, 15}, new String[]{"5 Seconds", "2 Seconds", "Every Second"},
                new ItemBuilder(Material.BEACON).name("Self Generator").build());
        public Upgrade generator = Upgrade.fromItem(new int[]{5, 10, 15}, new String[]{"3/4 Seconds", "1/2 Seconds", "1/4 Seconds"},
                new ItemBuilder(Material.FURNACE).name("Team Generator").build());
    }

    /**
     * A class to keep track of an upgrade
     */
    public static class Upgrade {
        private final ImmutableMap<Integer, String> items;
        private final ItemStack item;
        private int current = 0;

        private Upgrade(LinkedHashMap<Integer, String> items, ItemStack item) {
            this.item = item;
            this.items = ImmutableMap.copyOf(items);
        }

        /**
         * Creates an Upgrade based on prices, lables, and an item
         *
         * @param prices Array of prices. Goes in order.
         * @param labels Array of lables. Matches with prices.
         * @param item The ItemStack to base off the item from
         * @return An Upgrade
         */
        public static Upgrade fromItem(int[] prices, String[] labels, ItemStack item) {
            if (prices.length != labels.length) {
                throw new IllegalArgumentException("Prices and labels need to be the same length!");
            }
            LinkedHashMap<Integer, String> hash = new LinkedHashMap<>();
            for (int i = 0; i < prices.length; i++) {
                hash.put(prices[i], labels[i]);
            }
            return new Upgrade(hash, item);
        }

        /**
         * Gets a formatted item based off of the current upgrades and the levels the player has.
         *
         * @param currentLevels Levels the player has.
         * @return A formatted Item with information.
         */
        public ItemStack getItem(int currentLevels) {
            ItemBuilder i = new ItemBuilder(item.clone());
            i.addFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            // Set enchanted if it has been bought
            if (getCurrent() > 0) {
                i.amount(getCurrent()).addEnchantment(Enchantment.DAMAGE_ALL, 1);
            }
            // Add the current level to the name
            String name = i.getName();
            if (getCurrent() > 0) {
                name = "&a" + name + " " + FormattingUtils.toRoman(getCurrent());
            } else {
                name = "&7" + name;
            }
            i.name(name);
            // Add different level information
            ArrayList<String> lore = new ArrayList<>(i.getLore());
            lore.add(" ");
            int cur = 0;
            for (Map.Entry<Integer, String> item : items.entrySet()) {
                cur++;
                if (cur <= getCurrent()) {
                    lore.add("&a" + item.getValue() + " " + "&7" + item.getKey() + " Levels");
                } else {
                    lore.add("&7" + item.getValue() + " " + "&b" + item.getKey() + " Levels");
                }
            }
            lore.add(" ");
            if (currentLevels < getCost()) {
                lore.add("&cNot enough levels!");
            } else {
                lore.add("&aClick to purchase!");
            }
            i.lore(lore.toArray(new String[0]));
            return i.build();
        }

        /**
         * Get's the amount of times this can be bought
         *
         * @return How many times it can be bought
         */
        public int getMax() {
            return items.size();
        }

        /**
         * Get's the current level this upgrade is on
         *
         * @return The current level
         */
        public int getCurrent() {
            return current;
        }

        /**
         * Get's how much the item costs to upgrade one level
         *
         * @return Cost in levels
         */
        public int getCost() {
            int cur = Math.min(getMax() - 1, current);
            return items.keySet().asList().get(cur);
        }

        /**
         * Checks if the item can be upgraded further
         *
         * @return If the upgrade can continue to be upgraded
         */
        public boolean isMaxed() {
            return current >= getMax();
        }

        /**
         * Increments what the current level of the upgrade is.
         */
        public void increment() {
            current++;
        }
    }

    private final Shop shop;
    private final HashMap<Team, UpgradeKeeper> currentUpgrades = new HashMap<>();
    private int second = 0;

    public TeamUpgrades(Shop shop) {
        this.shop = shop;
    }

    public void openShop(Player player) {
        ArrayList<ItemStack> items = new ArrayList<>();
        Team team = shop.game.getPlayerTeam(player);
        if (team == null) {
            return;
        }
        int level = player.getLevel();
        UpgradeKeeper upg = currentUpgrades.get(team);
        if (upg == null) {
            upg = new UpgradeKeeper();
            currentUpgrades.put(team, upg);
        }
        items.add(upg.speed.getItem(level));
        items.add(upg.protection.getItem(level));
        items.add(upg.generator.getItem(level));
        items.add(upg.self_gen.getItem(level));
        if (shop.game.gameInstance.getType() == GameState.Mode.CAPTURE) {
            items.add(upg.freeze.getItem(level));
        }
        int slots = (int) (Math.ceil((double) items.size() / 9)) * 9;
        Inventory inventory = Bukkit.createInventory(player, slots, Shop.MENU_TITLE);
        inventory.setContents(items.toArray(new ItemStack[0]));
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        Team team = shop.game.getPlayerTeam(player);
        if (team == null) {
            return;
        }
        UpgradeKeeper current = currentUpgrades.get(team);
        if (current == null) {
            current = new UpgradeKeeper();
            currentUpgrades.put(team, current);
        }
        int xp = player.getLevel();
        if (item.equals(current.speed.getItem(xp))) {
            if (xp >= current.speed.getCost() && !current.speed.isMaxed()) {
                player.setLevel(xp - current.speed.getCost());
                current.speed.increment();
                for (Player p : shop.game.getTeamPlayers(team)) {
                    p.sendMessage(CloudFight.PREFIX + player.getDisplayName() + " upgraded speed to " + current.speed.getCurrent() + "!");
                }
                update();
            }
        } else if (item.equals(current.self_gen.getItem(xp))) {
            if (xp >= current.self_gen.getCost() && !current.self_gen.isMaxed()) {
                player.setLevel(xp - current.self_gen.getCost());
                current.self_gen.increment();
                for (Player p : shop.game.getTeamPlayers(team)) {
                    p.sendMessage(CloudFight.PREFIX + player.getDisplayName() + " upgraded self generator to " + current.self_gen.getCurrent() + "!");
                }
                update();
            }
        } else if (item.equals(current.protection.getItem(xp))) {
            if (xp >= current.protection.getCost() && !current.protection.isMaxed()) {
                player.setLevel(xp - current.protection.getCost());
                current.protection.increment();
                for (Player p : shop.game.getTeamPlayers(team)) {
                    p.sendMessage(CloudFight.PREFIX + player.getDisplayName() + " upgraded protection to " + current.protection.getCurrent() + "!");
                }
                update();
            }
        } else if (item.equals(current.freeze.getItem(xp))) {
            if (xp >= current.freeze.getCost() && !current.freeze.isMaxed()) {
                player.setLevel(xp - current.freeze.getCost());
                current.freeze.increment();
                if (shop.game.gameInstance.getType() == GameState.Mode.CAPTURE) {
                    ((CaptureGame) shop.game.gameInstance).addFreeze(30);
                }
                for (Player p : shop.game.getTeamPlayers(team)) {
                    p.sendMessage(CloudFight.PREFIX + player.getDisplayName() + " has frozen points for 30 seconds!");
                }
            }
        } else if (item.equals(current.generator.getItem(xp))) {
            if (xp >= current.generator.getCost() && !current.generator.isMaxed()) {
                player.setLevel(xp - current.generator.getCost());
                current.generator.increment();
                Generator gen = null;
                if (shop.game.gameInstance.getType() == GameState.Mode.CAPTURE) {
                    gen = ((CaptureGame) shop.game.gameInstance).getGenerator(team.getName());
                }
                if (gen != null) {
                    gen.level = current.generator.getCurrent();
                }
                for (Player p : shop.game.getTeamPlayers(team)) {
                    p.sendMessage(CloudFight.PREFIX + player.getDisplayName() + " upgraded the team generator to " + current.generator.getCurrent() + "!");
                }
            }
        } else {
            return;
        }
        openShop(player);
    }

    /**
     * Triggers generator upgrades and other upgrades
     */
    public void second() {
        second++;
        for (Map.Entry<Team, UpgradeKeeper> upg : currentUpgrades.entrySet()) {
            int gen = upg.getValue().self_gen.getCurrent();
            if (gen > 0) {
                ItemStack stack = Items.Default.getBlock(upg.getKey());
                if (gen == 3) {
                    for (Player p : shop.game.getTeamPlayers(upg.getKey())) {
                        p.getInventory().addItem(stack);
                    }
                } else if (gen == 2) {
                    if (second % 2 == 0) {
                        for (Player p : shop.game.getTeamPlayers(upg.getKey())) {
                            p.getInventory().addItem(stack);
                        }
                    }
                } else if (gen == 1) {
                    if (second % 5 == 0) {
                        for (Player p : shop.game.getTeamPlayers(upg.getKey())) {
                            p.getInventory().addItem(stack);
                        }
                    }
                }
            }
        }
        if (second % 5 != 0) {
            return;
        }
        update();
    }

    /**
     * Updates all team upgrades
     */
    public void update() {
        for (Map.Entry<Team, UpgradeKeeper> upg : currentUpgrades.entrySet()) {
            UpgradeKeeper upgrades = upg.getValue();
            for (Player p : shop.game.getTeamPlayers(upg.getKey())) {
                if (upgrades.speed.getCurrent() > 0) {
                    PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 1000, upgrades.speed.getCurrent() - 1);
                    p.addPotionEffect(speed);
                }
                if (upgrades.protection.getCurrent() > 0) {
                    for (ItemStack armor : p.getInventory().getArmorContents()) {
                        if (armor != null) {
                            armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, upgrades.protection.getCurrent());
                        }
                    }
                }
            }
        }
    }

}
