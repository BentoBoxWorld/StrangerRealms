package world.bentobox.stranger;

import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.stranger.listeners.BorderShower;

public final class PerPlayerBorderProxy implements BorderShower {

    private final BorderShower customBorder;
    private final BorderShower vanillaBorder;

    public PerPlayerBorderProxy(StrangerRealms addon, BorderShower customBorder, BorderShower vanillaBorder) {
        this.customBorder = customBorder;
        this.vanillaBorder = vanillaBorder;
    }

    @Override
    public void showBorder(Player player) {
        customBorder.showBorder(player);
        vanillaBorder.showBorder(player);
    }

    @Override
    public void hideBorder(User user) {
        customBorder.hideBorder(user);
        vanillaBorder.hideBorder(user);
    }

    @Override
    public void clearUser(User user) {
        customBorder.clearUser(user);
        vanillaBorder.clearUser(user);
    }

    @Override
    public void refreshView(User user) {
        customBorder.refreshView(user);
        vanillaBorder.refreshView(user);
    }


    @Override
    public void teleportPlayer(Player player) {
        customBorder.teleportPlayer(player);
        vanillaBorder.teleportPlayer(player);
    }
}
