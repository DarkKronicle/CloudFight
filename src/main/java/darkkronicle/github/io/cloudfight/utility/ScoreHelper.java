package darkkronicle.github.io.cloudfight.utility;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A scoreboard utility class
 * <p>
 * Original author crisdev333
 * https://www.spigotmc.org/threads/create-scoreboards-in-a-simple-way.272337/
 * - Adapted by DarkKronicle
 */
public final class ScoreHelper {

    // It may seem weird to be static, but players can only have one scoreboard at once.
    // Being static also helps prevent duplicate scoreboards from being created.
    private static final HashMap<UUID, ScoreHelper> players = new HashMap<>();

    /**
     * Checks if a player has a scoreboard currently
     *
     * @param player Player to check
     * @return ScoreHelper
     */
    public static boolean hasScore(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    /**
     * If a player has a ScoreHelper return that, if not, return a new one.
     *
     * @param player Player to get ScoreHelper
     * @return ScoreHelper
     */
    public static ScoreHelper getOrCreate(Player player) {
        if (hasScore(player)) {
            return get(player);
        }
        return createScore(player);
    }

    /**
     * Creates a new ScoreHelper for a player
     * <p>
     * This method is safe to call if you want to remove an old one and replace it.
     *
     * @param player Player to create a new one
     * @return new ScoreHelper
     */
    public static ScoreHelper createScore(Player player) {
        return new ScoreHelper(player);
    }

    /**
     * Gets a ScoreHelper
     *
     * @param player Player to get from
     * @return ScoreHelper
     */
    public static ScoreHelper get(Player player) {
        return players.get(player.getUniqueId());
    }

    public static void updateTeams(Scoreboard main) {
        for (ScoreHelper s : players.values()) {
            for (Team t : main.getTeams()) {
                Team team = s.scoreboard.getTeam(t.getName());
                if (team == null) {
                    team = s.scoreboard.registerNewTeam(t.getName());
                }
                team.setAllowFriendlyFire(t.allowFriendlyFire());
                team.setPrefix(t.getPrefix());
                team.setDisplayName(t.getDisplayName());
                team.setColor(t.getColor());
                team.setOption(Team.Option.COLLISION_RULE, t.getOption(Team.Option.COLLISION_RULE));

                ArrayList<String> notAdded = new ArrayList<>(t.getEntries());
                notAdded.removeAll(team.getEntries());
                ArrayList<String> toRemove = new ArrayList<>(team.getEntries());
                toRemove.removeAll(t.getEntries());
                for (String ent : notAdded) {
                    team.addEntry(ent);
                }
                for (String ent : toRemove) {
                    team.removeEntry(ent);
                }
            }
        }
    }

    /**
     * Removes a player from ScoreHelper
     *
     * @param player Player to remove
     * @return ScoreHelper that was removed. (null if none)
     */
    public static ScoreHelper removeScore(Player player) {
        return players.remove(player.getUniqueId());
    }

    private final Scoreboard scoreboard;
    private final Objective sidebar;

    private ScoreHelper(Player player) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        sidebar = scoreboard.registerNewObjective("sidebar", "dummy", "sidebar");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        // Create Teams
        for (int i = 1; i <= 15; i++) {
            // Teams allow for more flexibility, and there can only be 15 scores.
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            // Scoreboard formatting goes PREFIX NAME SUFFIX. We only change PREFIX and SUFFIX for a maximum of 128 characters.
            // Each team will only have one entry to maximize amount of space. Entry will stay constant for easy callbacks.
            team.addEntry(generateEntry(i));
        }
        player.setScoreboard(scoreboard);
        // This will override if they have older ones. So not much problem doing this.
        players.put(player.getUniqueId(), this);
    }

    /**
     * Sets the title of the Scoreboard
     *
     * @param title String title. & can be used for color codes
     */
    public void setTitle(String title) {
        title = CloudFight.color(title);
        sidebar.setDisplayName(title.length() > 128 ? title.substring(0, 128) : title);
    }

    /**
     * Sets a specific slot in the scoreboard. Can be a maximum of 128 characters.
     *
     * @param slot Slot to set
     * @param text Text to set. & will be translated into colors.
     */
    public void setSlot(int slot, String text) {
        // It should be impossible for the team to be null
        Team team = scoreboard.getTeam("SLOT_" + slot);
        updateScore(slot);

        text = CloudFight.color(text);
        // Allow for 128 characters
        String pre = getFirstSplit(text);
        String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(text));
        team.setPrefix(pre);
        team.setSuffix(suf);
    }

    /**
     * Updates a slot's score. (Will just be slot number)
     * <p>
     * If a slot has been hidden due to #removeSlot(slot), this will show it again.
     *
     * @param slot Slot number to update
     */
    public void updateScore(int slot) {
        String entry = generateEntry(slot);
        if (!scoreboard.getEntries().contains(entry)) {
            sidebar.getScore(entry).setScore(slot);
        }
    }

    /**
     * Hides a line by resetting scores.
     * <p>
     * The slot still exists, it just won't show up.
     *
     * @param slot Slot to reset
     */
    public void removeSlot(int slot) {
        String entry = generateEntry(slot);
        if (scoreboard.getEntries().contains(entry)) {
            scoreboard.resetScores(entry);
        }
    }

    public void setSlotsFromList(List<String> list) {
        while (list.size() > 15) {
            list.remove(list.size() - 1);
        }

        int slot = list.size();

        // Blank out all slots not using
        for (int i = slot + 1; i <= 15; i++) {
            removeSlot(i);
        }

        // Set the new ones!
        for (String line : list) {
            setSlot(slot, line);
            slot--;
        }
    }

    /**
     * Return's a blank line
     * <p>
     * There are only 15 possible lines and there can't be any duplicates. So we use 'invisible' ChatColors.
     *
     * @param slot Slot to generate
     * @return Blank string containing only one ChatColor
     */
    private String generateEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    /**
     * Gets the first half of a string limited by Minecraft's 64 character limit. Will return 0-64
     *
     * @param s String to get first 64 characters
     * @return First 64 characters
     */
    private String getFirstSplit(String s) {
        return s.length() > 64 ? s.substring(0, 64) : s;
    }

    /**
     * Gets the second half of a split string. Second half is 64-126
     * <p>
     * It only goes to 126 because ChatColor gets appended at the beginning which is 2 characters.
     *
     * @param s String to get the second split
     * @return The string split from 64-126. Will return a blank string if it's shorter than 64
     */
    private String getSecondSplit(String s) {
        if (s.length() <= 64) {
            return "";
        }
        if (s.length() > 126) {
            s = s.substring(0, 126);
        }
        return s.substring(62);
    }

    public void reset() {
        // Blank out all slots
        for (int i = 1; i <= 15; i++) {
            removeSlot(i);
        }
    }
}

