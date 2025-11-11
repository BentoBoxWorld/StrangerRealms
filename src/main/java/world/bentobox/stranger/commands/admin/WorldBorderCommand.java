package world.bentobox.stranger.commands.admin;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.stranger.StrangerRealms;

/**
 * Handles world border management
 * @author tastybento
 */
public class WorldBorderCommand extends CompositeCommand {

    private final StrangerRealms addon;
    /**
     * Command to create a claim
     * 
     * @param islandCommand - parent command
     */
    public WorldBorderCommand(CompositeCommand islandCommand) {
        super(islandCommand, "worldborder", "wb");
        addon = getAddon();
    }

    @Override
    public void setup() {
        setPermission("admin.worldborder");
        setOnlyPlayer(false);
        setDescription("commands.admin.worldborder.description");
        setParametersHelp("commands.admin.worldborder.parameters");
    }

    /**
     * Checks if the command can be executed by this user.
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
            // Show help
            this.showHelp(this, user);
            return false;
        }
        return true;
    }

    /**
     * Handles the world border setting
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.isEmpty()) return false;
        return switch (args.getFirst()) {
        case "off" -> turnOff(user);
        case "on" -> turnOn(user);
        case "info" -> showInfo(user);
        case "auto" -> setAuto(user);
        case "set" -> setSize(user, args);
        default -> {
            user.sendMessage("commands.admin.worldborder.unknown-command");
            yield false;
        }
        };
    }


    private boolean turnOn(User user) {
        addon.getSettings().setDisableWorldBorder(false);
        addon.saveWorldSettings();
        Bukkit.getServer().getOnlinePlayers().forEach(addon.getBorderShower()::showBorder);
        user.sendMessage("general.success");
        showInfo(user);
        return true;
    }

    private boolean turnOff(User user) {
        addon.getSettings().setDisableWorldBorder(true);
        addon.saveWorldSettings();
        addon.cancelBorderTask();
        Bukkit.getServer().getOnlinePlayers().stream().map(User::getInstance).forEach(addon.getBorderShower()::hideBorder);
        user.sendMessage("general.success");
        return true;
    }

    private boolean setAuto(User user) {
        // Turn on auto
        addon.getSettings().setManualBorderSize(false);
        addon.saveWorldSettings();
        Bukkit.getServer().getOnlinePlayers().forEach(addon.getBorderShower()::showBorder);
        user.sendMessage("general.success");
        showInfo(user);
        return true;
    }

    private boolean setSize(User user, List<String> args) {
        // Check the arg is a number
        if (args.size() != 2) {
            user.sendMessage("commands.admin.worldborder.set-size.needs-value");
            return false;
        }
        String argument = args.get(1);
        // Attempt to parse the string into an Integer using a try-catch block
        try {
            // Check for null or empty string before parsing, though parse will throw for empty
            if (argument == null || argument.trim().isEmpty()) {
                user.sendMessage("commands.admin.worldborder.set-size.needs-value");
                return false;
            }
            // Stop any border changing
            addon.cancelBorderTask();
            // Validation and extraction
            int result = Integer.parseInt(argument.trim()); 
            addon.setBorderSize(result);
            // Turn off auto
            addon.getSettings().setManualBorderSize(true);
            addon.saveWorldSettings();
            // Update all players
            Bukkit.getServer().getOnlinePlayers().forEach(addon.getBorderShower()::showBorder);
            user.sendMessage("general.success");
            showInfo(user);
            return true;

        } catch (NumberFormatException e) {
            user.sendMessage("commands.admin.worldborder.set-size.needs-value");
            return false;
        }
    }

    private boolean showInfo(User user) {
        user.sendMessage("commands.admin.worldborder.info", TextVariables.NUMBER, String.valueOf((int)((StrangerRealms)getAddon()).getBorderSize()));
        return true;
    }
    
    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
    {
        return Optional.of(List.of("on", "off", "set", "auto", "info"));
    }

}