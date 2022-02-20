package darkkronicle.github.io.cloudfight.commands.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class LeaveCommand extends Command {

    public LeaveCommand(CloudFight plugin) {
        super(plugin, "leave", null);
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        Team team = plugin.gamePool.removePlayer(player);
        if (team == null) {
            plugin.returnToSpawn(player);
        } else {
            Bukkit.getServer().broadcastMessage(CloudFight.PREFIX + team.getPrefix() + player.getName() + " left the " + team.getName() + " team!");
        }
    }

}
