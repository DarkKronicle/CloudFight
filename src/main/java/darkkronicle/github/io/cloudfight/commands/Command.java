package darkkronicle.github.io.cloudfight.commands;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    protected final CloudFight plugin;
    public ArrayList<SubCommand> subCommands = null;
    public final String name;
    public final String permission;
    public final String usage;
    public final String description;
    public final boolean playerOnly = true;

    public Command(CloudFight plugin, String name, String permission) {
        this(plugin, name, permission, "", "No information here...");
    }

    public Command(CloudFight plugin, String name, String permission, String usage, String description) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.description = description;
    }

    public abstract void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args);

    public List<SubCommand> getSubCommands() {
        return null;
    }

    public interface CommandExecute {
        void execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args);
    }

}
