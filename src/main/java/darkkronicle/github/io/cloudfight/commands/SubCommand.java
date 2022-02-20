package darkkronicle.github.io.cloudfight.commands;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.command.CommandSender;

public abstract class SubCommand extends Command {

    public final Command main;

    public SubCommand(CloudFight plugin, Command main, String name, String permission) {
        super(plugin, name, permission);
        this.main = main;
    }

    public SubCommand(CloudFight plugin, Command main, String name, String permission, String usage, String description) {
        super(plugin, name, permission, usage, description);
        this.main = main;
    }

    public static SubCommand simple(CloudFight plugin, Command main, String name, String permission, CommandExecute execute) {
        return new SubCommand(plugin, main, name, permission) {
            @Override
            public void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
                execute.execute(sender, command, label, args);
            }
        };
    }


}
