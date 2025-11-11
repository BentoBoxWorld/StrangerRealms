package world.bentobox.stranger.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import world.bentobox.bentobox.api.events.team.TeamJoinedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.stranger.StrangerRealms;

/**
 * Listens to team changes and adjusts the claim size accordingly
 * @author tastybento
 */
public class TeamListener implements Listener {

    private final StrangerRealms addon;


    public TeamListener(StrangerRealms addon) {
        this.addon = addon;

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Check if player is in the world
        if (!addon.inWorld(player.getWorld())) {
            return;
        }
        // Check if the player has a claim and if so, resize it
        addon.getIslands().getIslands(player.getWorld(), player.getUniqueId()).forEach(this::resize);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        // Check if player is in the world
        if (!addon.inWorld(e.getTo())) {
            return;
        }
        // Check if the player has a claim
        addon.getIslands().getIslands(e.getTo().getWorld(), player.getUniqueId()).forEach(this::resize);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTeamJoin(TeamJoinedEvent e) {
        if (!addon.inWorld(e.getIsland().getWorld()) // Not in game world
                || e.getIsland().getMemberSet().size() == 1 // New claim
                || addon.getSettings().getMemberBonus() == 0 // No resizing
                ) {
            return;
        }
        int change =  resize(e.getIsland());
        // If there is no difference in size, explain why
        if (change == 0 && e.getIsland().getOwner() != null) {
            User.getInstance(e.getIsland().getOwner()).sendMessage("strangerrealms.claim.team-no-change");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTeamKick(TeamKickEvent e) {
        if (!addon.inWorld(e.getIsland().getWorld()) // Not in game world
                || addon.getSettings().getMemberBonus() != 0) {
            resize(e.getIsland());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTeamLeave(TeamLeaveEvent e) {
        if (!addon.inWorld(e.getIsland().getWorld()) // Not in game world
                || addon.getSettings().getMemberBonus() != 0) {
            resize(e.getIsland());
        }
    }

    protected int resize(Island claim) {
        // Remove old claim from grid
        addon.getPlugin().getIslands().getIslandCache().getIslandGrid(claim.getWorld()).removeFromGrid(claim);

        // Resize this claim
        int size = addon.getSettings().getIslandDistance() + addon.getSettings().getMemberBonus() * (claim.getMemberSet().size()-1);
        int oldSize = claim.getProtectionRange();
        // Try this size
        claim.setRange(size);
        // Loop until it fits
        while (!addon.getPlugin().getIslands().getIslandCache().getIslandGrid(claim.getWorld()).addToGrid(claim) && size > oldSize) {
            // It doesn't fit so it must shrink (maybe to the original size)
            size--;
            claim.setRange(size);
        }
        claim.setProtectionRange(size); // This should trigger an update to the border viewed via the event

        if (size == oldSize) {
            return 0;
        }
        // Determine the message key suffix based on whether the team size increased or decreased
        final String suffix = size > oldSize ? "increase" : "decrease";           

        // Notify players
        if (claim.isOwned()) {
            // Tell owner
            User.getInstance(claim.getOwner()).sendMessage("strangerrealms.claim.team-" + suffix + "-owner");
        }

        // Tell players on the claim (excluding the owner, if one exists)
        claim.getPlayersOnIsland().stream()
        .filter(p -> claim.getOwner() == null || !p.getUniqueId().equals(claim.getOwner()))
        .map(User::getInstance)
        .forEach(u -> u.sendMessage("strangerrealms.claim.team-" + suffix));

        return size - oldSize;
    }

}


