package world.bentobox.stranger.border;

import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.user.User;

/**
 * A border shower class
 * @author tastybento
 *
 */
public interface BorderShower {
    String BORDER_STATE_META_DATA = "Stranger_border_state";

    /**
     * Show the barrier to the player
     * @param player - player to show
     */
    void showBorder(Player player);

    /**
     * Hide the barrier
     * @param user - user
     */
    void hideBorder(User user);

    /**
     * Removes any cache
     * @param user - user
     */
    default void clearUser(User user) {
        // Do nothing
    }

    /**
     * Refreshes the barrier view, if required
     * @param user user 
     */
    default void refreshView(User user){
        // Do nothing
    }

    /**
     * Teleports player back within the island space they are in
     * @param player player
     */
    void teleportPlayer(Player player);

}
