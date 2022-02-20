package darkkronicle.github.io.cloudfight.commands;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.commands.game.JoinCommand;
import darkkronicle.github.io.cloudfight.commands.game.LeaveCommand;
import darkkronicle.github.io.cloudfight.commands.game.ModeCommand;
import darkkronicle.github.io.cloudfight.commands.game.ModeVoteCommand;
import darkkronicle.github.io.cloudfight.commands.game.SpectateCommand;
import darkkronicle.github.io.cloudfight.commands.game.TeamMessageCommand;
import darkkronicle.github.io.cloudfight.commands.main.MainCommand;
import darkkronicle.github.io.cloudfight.commands.map.MapCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor {

    private final ArrayList<darkkronicle.github.io.cloudfight.commands.Command> commands = new ArrayList<>();
    private final CloudFight plugin;

    public CommandHandler(CloudFight plugin) {
        this.plugin = plugin;
        commands.add(new MainCommand(plugin));
        commands.add(new JoinCommand(plugin));
        commands.add(new LeaveCommand(plugin));
        commands.add(new MapCommand(plugin));
        commands.add(new ModeCommand(plugin));
        commands.add(new SpectateCommand(plugin));
        commands.add(new ModeVoteCommand(plugin));
        commands.add(new TeamMessageCommand(plugin));

        for (darkkronicle.github.io.cloudfight.commands.Command command : commands) {
            try {
                plugin.getCommand(command.name).setExecutor(this);
            } catch (NullPointerException e) {
                System.out.println("Could not register command " + command.name + e);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (darkkronicle.github.io.cloudfight.commands.Command cmd : commands) {
            if (cmd.name.equalsIgnoreCase(command.getName())) {
                if (cmd.permission != null && !sender.hasPermission(cmd.permission)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return false;
                }
                if (cmd.playerOnly && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You have to be a player to do this!");
                    return false;
                }
                List<SubCommand> subs = cmd.getSubCommands();
                if (args.length != 0 && args[0].equalsIgnoreCase("help")) {
                    StringBuilder message = new StringBuilder().append(ChatColor.GRAY).append(ChatColor.ITALIC).append("/").append(cmd.name).append(" ").append(cmd.usage).append("\n  ").append(ChatColor.RESET).append(cmd.description);
                    for (SubCommand sub : subs) {
                        if (sub.permission == null || sender.hasPermission(sub.permission)) {
                            message.append("\n").append(ChatColor.GRAY).append("- ").append(ChatColor.ITALIC).append(sub.name).append(" ").append(sub.usage).append("\n   ").append(ChatColor.RESET).append(sub.description);
                        }
                    }
                    sender.sendMessage(message.toString());
                    return true;
                }
                if (args.length != 0 && subs != null && subs.size() != 0) {
                    for (SubCommand sub : subs) {
                        if (args[0].equalsIgnoreCase(sub.name)) {
                            if (sub.permission != null && !sender.hasPermission(sub.permission)) {
                                sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                                return false;
                            }
                            sub.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                            return true;
                        }
                    }
                }
                cmd.execute(sender, command, label, args);
                return true;
            }
        }
        return false;
    }

}
