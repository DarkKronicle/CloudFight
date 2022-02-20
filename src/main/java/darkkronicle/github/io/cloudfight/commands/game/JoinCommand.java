package darkkronicle.github.io.cloudfight.commands.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class JoinCommand extends Command {

    public JoinCommand(CloudFight plugin) {
        super(plugin, "join", null);
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            Team joined = plugin.gamePool.joinPlayer(player);
            if (joined == null) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You're already in a game!");
            }
        } else {
            Team team = plugin.gamePool.getTeam(args[0]);
            if (team == null) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "That team doesn't exist!");
                return;
            }
            Team joined = plugin.gamePool.joinPlayer(player, null, team);
            if (joined == null) {
                player.sendMessage(CloudFight.PREFIX + ChatColor.RED + "Teams are unbalanced or you're already in a game!");
            }
        }
    }

}
