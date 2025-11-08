package world.bentobox.stranger.commands.player;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.BlueprintsManager;
import world.bentobox.bentobox.managers.island.NewIsland;
import world.bentobox.bentobox.managers.island.NewIslandLocationStrategy;
import world.bentobox.bentobox.panels.customizable.IslandCreationPanel;

/**
 * Handles the claim creation command.
 * A lot of this code comes from the stock create command in BentoBox, and it uses
 * locale from that too often. References to "island" can be read as "claim".
 * <p>
 * Features:
 * <ul>
 *   <li>Multiple claim support with permission-based limits</li>
 *   <li>Team member restrictions</li>
 *   <li>World claim limits</li>
 * </ul>
 * <p>
 * Permission nodes:
 * <ul>
 *   <li>{@code island.create} - Base permission</li>
 *   <li>{@code [gamemode].island.number.[number]} - Max concurrent islands</li>
 * </ul>
 *
 * @author tastybento
 * @since 1.0
 */
public class ClaimCommand extends CompositeCommand {

    private final NewIslandLocationStrategy strategy;

    /**
     * Command to create a claim
     * 
     * @param playerCommand - parent command
     */
    public ClaimCommand(CompositeCommand playerCommand) {
        super(playerCommand, "claim");
        strategy = new ClaimLocationStrategy(getAddon());
    }

    @Override
    public void setup() {
        setPermission("claim");
        setOnlyPlayer(true);
        setDescription("commands.claim.description");
    }

    /**
     * Checks if the command can be executed by this user.
     * <p>
     * Validation checks:
     * <ul>
     *   <li>Reserved island status</li>
     *   <li>Team member restrictions</li>
     *   <li>Concurrent island limits</li>
     *   <li>World island limits</li>
     * </ul>
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Check if the island is reserved
        @Nullable
        Island island = getIslands().getPrimaryIsland(getWorld(), user.getUniqueId());
        if (island != null) {
            // Reserved islands can be made
            if (island.isReserved()) {
                return true;
            }
        }
        // Check if this player is on a team in this world
        if (getIWM().getWorldSettings(getWorld()).isDisallowTeamMemberIslands()
                && getIslands().inTeam(getWorld(), user.getUniqueId()) && island != null
                && !user.getUniqueId().equals(island.getOwner())) {
            // Team members who are not owners cannot make additional claim
            user.sendMessage("commands.island.create.you-cannot-make-team");
            return false;
        }
        // Get how many claims this player has
        int num = this.getIslands().getNumberOfConcurrentIslands(user.getUniqueId(), getWorld());
        int max = user.getPermissionValue(
                this.getIWM().getAddon(getWorld()).map(GameModeAddon::getPermissionPrefix).orElse("") + "island.number",
                this.getIWM().getWorldSettings(getWorld()).getConcurrentIslands());
        if (num >= max) {
            // You cannot make a claim
            user.sendMessage("commands.island.create.you-cannot-make");
            return false;
        }
        if (getIWM().getMaxIslands(getWorld()) > 0
                && getIslands().getIslandCount(getWorld()) >= getIWM().getMaxIslands(getWorld())) {
            // There is too many claims in the world :(
            user.sendMessage("commands.island.create.too-many-islands");
            return false;
        }
        return true;
    }

    /**
     * Handles the claim creation process.
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Show panel only if there are multiple bundles available
        if (getPlugin().getBlueprintsManager().getBlueprintBundles(getAddon()).size() > 1) {
            // Show panel
            IslandCreationPanel.openPanel(this, user, label, false);
            return true;
        }
        return makeClaim(user, BlueprintsManager.DEFAULT_BUNDLE_NAME);
    }

    /**
     * Creates a new claim for the user using the specified blueprint.
     * Also handles reset cooldown if configured.
     * 
     * @param user The user getting the new claim
     * @param name The blueprint bundle name to use
     * @return true if claim creation was successful
     */
    private boolean makeClaim(User user, String name) {
        user.sendMessage("commands.island.create.creating-island");
        try {
            NewIsland.builder().player(user).addon(getAddon()).reason(Reason.CREATE).name(name).locationStrategy(strategy).noPaste().build();
        } catch (IOException e) {
            user.sendMessage(e.getMessage());
            return false;
        }
        if (getSettings().isResetCooldownOnCreate()) {
            getParent().getSubCommand("reset").ifPresent(
                    resetCommand -> resetCommand.setCooldown(user.getUniqueId(), getSettings().getResetCooldown()));
        }
        return true;
    }
}
