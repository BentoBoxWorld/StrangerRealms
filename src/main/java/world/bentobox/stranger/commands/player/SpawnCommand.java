package world.bentobox.stranger.commands.player;

import java.util.List;
import java.util.Objects;

import org.bukkit.Location;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.DelayedTeleportCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.teleport.SafeSpotTeleport;

/**
 * Handles the island spawn command (/island spawn).
 * <p>
 * This command teleports players to the world spawn location with
 * configurable delay and safety checks. Extends {@link DelayedTeleportCommand}
 * to provide teleport delay functionality.
 * <p>
 * Features:
 * <ul>
 *   <li>Configurable teleport delay</li>
 *   <li>Fall protection (prevents teleporting while falling)</li>
 *   <li>Player-only command</li>
 *   <li>Permission-based access</li>
 * </ul>
 * <p>
 * Permission: {@code island.spawn}
 *
 * @author Poslovitch
 * @since 1.1
 */
public class SpawnCommand extends DelayedTeleportCommand {

    public SpawnCommand(CompositeCommand parent) {
        super(parent, "spawn");
    }

    @Override
    public void setup() {
        setPermission("spawn");
        setOnlyPlayer(true);
        setDescription("commands.spawn.description");
    }

    /**
     * Handles the spawn teleport request.
     * <p>
     * Process:
     * <ul>
     *   <li>Checks if player is falling (if flag is set)</li>
     *   <li>Initiates delayed teleport to spawn</li>
     * </ul>
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
       // Initiate delayed teleport to spawn
        Location spawnPoint = Objects.requireNonNullElse(getIslands().getSpawnPoint(getWorld()), getWorld().getSpawnLocation());
        this.delayCommand(user, () -> new SafeSpotTeleport.Builder(getPlugin()).location(spawnPoint).entity(user.getPlayer()).build());
        return true;
    }
}
