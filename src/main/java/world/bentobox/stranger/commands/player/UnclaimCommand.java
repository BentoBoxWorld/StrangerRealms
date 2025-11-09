package world.bentobox.stranger.commands.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.events.IslandBaseEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;
import world.bentobox.stranger.StrangerRealms;

/**
 * Handles removing a claim
 *
 * @author tastybento
 */
public class UnclaimCommand extends ConfirmableCommand {

    private StrangerRealms addon;
    /**
     * Command to create a claim
     * 
     * @param playerCommand - parent command
     */
    public UnclaimCommand(CompositeCommand playerCommand) {
        super(playerCommand, "unclaim");
    }

    @Override
    public void setup() {
        setPermission("unclaim");
        setOnlyPlayer(true);
        setDescription("commands.unclaim.description");
        addon = (StrangerRealms)getAddon();
    }

    /**
     * Checks if the command can be executed by this user.
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // If args are not right, show help
        if (!args.isEmpty()) {
            showHelp(this, user);
            return false;
        }
        if (!getIslands().hasIsland(getWorld(), user)) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        // Check if the player is in a claim
        Optional<Island> opClaim =  getIslands().getIslandAt(user.getLocation())
                .filter(is -> ((StrangerRealms)getAddon()).inWorld(is.getWorld()))
                .filter(is -> is.getOwner() != null && is.getOwner().equals(user.getUniqueId()));
        if (opClaim.isEmpty()) {
            user.sendMessage("strangerrealms.errors.must-be-in");
            return false;
        }
        askConfirmation(user, () -> deleteClaim(user,opClaim.get() ));
        return true;
    }

    /**
     * Handles the unclaim creation process.
     * There isn't a neat way to do this through IslandsManager so it has to be done manually 
     */
    public boolean deleteClaim(User user, Island claim) {
        UUID uuid = user.getUniqueId();
        // Fire preclear event
        IslandEvent.builder().involvedPlayer(uuid).reason(Reason.PRECLEAR).island(claim)
        .oldIsland(claim).location(claim.getCenter()).build();
        // Fire delete event
        IslandBaseEvent event = IslandEvent.builder().island(claim).involvedPlayer(uuid)
                .reason(Reason.DELETE).build();
        if (event.getNewEvent().map(IslandBaseEvent::isCancelled).orElse(event.isCancelled())) {
            return false;
        }
        // Get a list of any players in the claim
        List<Player> players = claim.getPlayersOnIsland();
        // Set the owner of the island to no one.
        claim.setOwner(null);
        // Remove players from island
        getIslands().removePlayersFromIsland(claim);
        // Mark island as deletable
        claim.setDeletable(true);
        // Remove island from the cache
        getIslands().getIslandCache().deleteIslandFromCache(claim);
        // Delete the island from the database
        getIslands().deleteIslandId(claim.getUniqueId());
        // Fire the deletion event immediately
        IslandEvent.builder().deletedIslandInfo(new IslandDeletion(claim)).reason(Reason.DELETED).build();
        // Tell user
        user.sendMessage("strangerrealms.commands.unclaim.success");     
        // Refresh borders
        players.forEach(p -> addon.getBorderShower().showBorder(p));
        return true;
    }

}
