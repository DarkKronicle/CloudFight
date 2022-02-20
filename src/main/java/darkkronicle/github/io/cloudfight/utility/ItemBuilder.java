package darkkronicle.github.io.cloudfight.utility;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A class to help build an ItemStack
 */
public class ItemBuilder {

    private final ItemStack stack;

    /**
     * Creates an ItemBuilder from an ItemStack
     *
     * @param item ItemStack to build from. Recommended to clone beforehand.
     */
    public ItemBuilder(ItemStack item) {
        this.stack = item;
    }

    /**
     * Creates an ItemBuilder from a Material with amount 1
     *
     * @param mat Material the item will have
     */
    public ItemBuilder(Material mat) {
        this(mat, 1);
    }

    /**
     * Creates an ItemBuilder from a Material with a custom amount
     * @param mat Material the item will have
     * @param amount The amount the item will have
     */
    public ItemBuilder(Material mat, int amount) {
        this.stack = new ItemStack(mat, amount);
    }

    /**
     * Get the item that the ItemBuilder built
     *
     * @return The built ItemStack
     */
    public ItemStack build() {
        return this.stack;
    }

    /**
     * Set's the name of the item.
     *
     * @param name The new custom name of the item. Accepts & for color codes.
     * @return Returns the ItemBuilder
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(CloudFight.color(name));
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Set's an item to unbreakable
     *
     * @return The ItemBuilder
     */
    public ItemBuilder unbreakable() {
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Add's an enchantment to the item
     *
     * @param enchantment Enchantment to add
     * @param level Level of enchantment
     * @return The ItemBuilder
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the color of a piece of leather armor.
     *
     * @param color
     * @return The ItemBuilder
     *
     * @throws ClassCastException if it is not a piece of leather armor
     */
    public ItemBuilder color(Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) stack.getItemMeta();
        armorMeta.setColor(color);
        stack.setItemMeta(armorMeta);
        return this;
    }

    /**
     * Set the amount the item has
     *
     * @param amount The new amount for the item
     * @return The ItemBuilder
     */
    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        List<String> lore_list = new ArrayList<>();
        ItemMeta meta = stack.getItemMeta();
        for (String l : lore) {
            ChatColor.translateAlternateColorCodes('&', l);
            lore_list.add(ChatColor.translateAlternateColorCodes('&', l));
        }
        meta.setLore(lore_list);
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Add item flags to the item
     *
     * @param flag ItemFlags to add to
     * @return The ItemBuilder
     */
    public ItemBuilder addFlags(ItemFlag... flag) {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(flag);
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Get's the lore
     * @return Null if the ItemMeta doesn't exist. Empty list if there is no lore, or the lore that it has.
     */
    public List<String> getLore() {
        List<String> lore = stack.getItemMeta().getLore();
        if (lore == null || lore.size() == 0) {
            return new ArrayList<>();
        }
        return lore;
    }

    /**
     * Get's the custom name of the item
     *
     * @return Returns null if there is no ItemMeta. Returns the localized name if there is no displayname,
     *         then the displayname if one exists.
     */
    public String getName() {
        if (!stack.getItemMeta().hasDisplayName()) {
            return stack.getItemMeta().getLocalizedName();
        }
        return stack.getItemMeta().getDisplayName();
    }

    /**
     * Utility function to get what items the player has based off of count.
     *
     * @param player Player to get inventory from
     * @param flat Whether or not the items should be cloned and amount set to 1
     * @return HashMap containing the ItemStack as the key and an Integer as the value for how many items it has.
     */
    public static HashMap<ItemStack, Integer> getAllPlayerItems(Player player, boolean flat) {
        PlayerInventory playerinv = player.getInventory();
        // Get every item in the inventory
        ArrayList<ItemStack> inventory = new ArrayList<>(Arrays.asList(playerinv.getContents()));
        inventory.addAll(Arrays.asList(playerinv.getExtraContents()));
        inventory.addAll(Arrays.asList(playerinv.getArmorContents()));
        HashMap<ItemStack, Integer> inventory_flat = new HashMap<>();
        // Go through and map the amount to a single item
        for (ItemStack i : inventory) {
            if (i == null) {
                continue;
            }
            ItemStack i_one;
            if (flat) {
                i_one = i.clone();
                i_one.setAmount(1);
            } else {
                i_one = i;
            }
            if (inventory_flat.containsKey(i_one)) {
                int old = inventory_flat.get(i_one);
                inventory_flat.replace(i_one, old + i.getAmount());
            } else {
                inventory_flat.put(i_one, i.getAmount());
            }
        }
        return inventory_flat;
    }


}
