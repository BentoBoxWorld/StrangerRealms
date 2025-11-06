package world.bentobox.stranger.listeners;

import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.user.User;

/**
 * A border shower class
 * @author tastybento
 *
 */
public interface BorderShower {
    public static final String BORDER_STATE_META_DATA = "Crowdbound_border_state";

    /**
     * Show the barrier to the player
     * @param player - player to show
     */
    public void showBorder(Player player);

    /**
     * Hide the barrier
     * @param user - user
     */
    public void hideBorder(User user);

    /**
     * Removes any cache
     * @param user - user
     */
    public default void clearUser(User user) {
        // Do nothing
    }

    /**
     * Refreshes the barrier view, if required
     * @param user user 
     */
    public default void refreshView(User user){
        // Do nothing
    }

    /**
     * Teleports player back within the island space they are in
     * @param player player
     */
    public void teleportPlayer(Player player);

}
