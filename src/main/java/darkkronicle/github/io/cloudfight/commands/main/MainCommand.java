package darkkronicle.github.io.cloudfight.commands.main;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.Command;
import darkkronicle.github.io.cloudfight.commands.SubCommand;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.utility.WorldManagement;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainCommand extends Command {

    public MainCommand(CloudFight plugin) {
        super(plugin, "cloudfight", "cloudfight.admin");
        subCommands = new ArrayList<>();
        subCommands.add(SubCommand.simple(plugin, this, "instastart", null, (sender, command, label, args) -> {
            GameContainer game = plugin.gamePool.getWherePlayer((Player) sender);
            if (game == null) {
                sender.sendMessage(ChatColor.RED + "You aren't in a game!");
                return;
            }
            game.start();
        }));
        subCommands.add(SubCommand.simple(plugin, this, "reloadstructures", null, (sender, command, label, args) -> {
            File world = new File("./world/generated/");
            File folder = plugin.getStructurePath().toFile();
            WorldManagement.copyDirectory(world, folder);
            sender.sendMessage(CloudFight.PREFIX + "Done!");
        }));
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        sender.sendMessage("Hello!");
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}
