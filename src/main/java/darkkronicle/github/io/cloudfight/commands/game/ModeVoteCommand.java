package darkkronicle.github.io.cloudfight.commands.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModeVoteCommand extends Command {
    public ModeVoteCommand(CloudFight plugin) {
        super(plugin, "modevote", null);
    }

    public static String getList(String seperator) {
        StringBuilder builder = new StringBuilder();
        for (GameState.Mode mode : GameState.Mode.values()) {
            builder.append(mode.name().toLowerCase()).append(seperator);
        }
        return builder.substring(0, builder.length() - seperator.length());
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            String builder = CloudFight.PREFIX +
                    CloudFight.color("&7use &b/modevote <mode>&7 to vote for a mode! Allowed modes:&b ") +
                    getList(", ");
            player.sendMessage(builder);
            return;
        }
        GameContainer game = plugin.gamePool.getWherePlayer(player);
        if (game == null) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You're not in a game!");
            return;
        }
        if (game.newstate == GameState.State.STARTED || game.newstate == GameState.State.RESETTING) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Game can't be started!");
            return;
        }
        if (game.hasModeVoted(player)) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You can only vote once!");
            return;
        }
        String modeName = args[0].toUpperCase();
        GameState.Mode mode;
        try {
            mode = GameState.Mode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Invalid mode! Allowed modes: " + ChatColor.GRAY + getList(", "));
            return;
        }
        game.modeVote(player, mode);
        Bukkit.broadcastMessage(CloudFight.PREFIX + CloudFight.color(player.getDisplayName() + "&7 has voted for &b" + mode));
    }

}
