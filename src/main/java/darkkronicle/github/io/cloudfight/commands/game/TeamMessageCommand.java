package darkkronicle.github.io.cloudfight.commands.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class TeamMessageCommand extends Command {

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Incorrect usage! Use: " + ChatColor.GRAY + usage);
        }
        teamMessage(player, plugin, String.join(" ", args));
    }

    public static void teamMessage(Player player, CloudFight plugin, String msg) {
        GameContainer game = plugin.gamePool.getWherePlayer(player);
        if (game == null) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to be in a game to message your team!");
            return;
        }
        Team team = game.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to be in a game to message your team!");
        }
        String message = CloudFight.color("&7-> ") + CloudFight.color("&8[&r" + team.getDisplayName() + "&8]") + " " + player.getDisplayName() + ": " + ChatColor.RESET + msg;
        for (Player p : game.getTeamPlayers(team)) {
            p.sendMessage(message);
        }
        // Log it to console
        System.out.println(ChatColor.stripColor(message));
    }

    public TeamMessageCommand(CloudFight plugin) {
        super(plugin, "teammsg", null, "/teammsg <message>", "Message your team");
    }
}
