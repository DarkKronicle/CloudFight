package darkkronicle.github.io.cloudfight.commands.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModeCommand extends Command {


    public ModeCommand(CloudFight plugin) {
        super(plugin, "mode", "cloudfight.mode");
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You have to specify a mode name!");
            return;
        }

        GameContainer container = plugin.gamePool.getWherePlayer((Player) sender);
        if (container == null) {
            sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "You aren't in a game!");
            return;
        }
        GameState.Mode mode;
        try {
            mode = GameState.Mode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(CloudFight.PREFIX + ChatColor.RED + "That mode doesn't exist!");
            return;
        }
        container.setMode(mode);
        sender.sendMessage(CloudFight.PREFIX + ChatColor.GREEN + "Mode set!");
    }

}
