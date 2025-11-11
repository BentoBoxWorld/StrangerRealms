package world.bentobox.stranger.border;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.bentobox.util.teleport.SafeSpotTeleport;
import world.bentobox.stranger.StrangerRealms;
import world.bentobox.stranger.listeners.BorderShower;

/**
 * Show a border using Paper's WorldBorder API
 * @author tastybento
 *
 */
public class ShowWorldBorder implements BorderShower {

    private final StrangerRealms addon;

    public ShowWorldBorder(StrangerRealms addon) {
        this.addon = addon;
    }

    @Override
    public void showBorder(Player player) {
        if (addon.getSettings().isDisableWorldBorder() || !Objects.requireNonNull(User.getInstance(player)).getMetaData(BORDER_STATE_META_DATA).map(MetaDataValue::asBoolean).orElse(true)) {
            return;
        }
        addon.getIslands().getIslandAt(player.getLocation()).ifPresentOrElse(island -> {
            
            Location l = island.getProtectionCenter().toVector().toLocation(player.getWorld());
            // Check if the claim is entirely within the world barrier
            Location center = Objects.requireNonNullElse(addon.getIslands().getSpawnPoint(player.getWorld()), player.getWorld().getSpawnLocation());
            double dist = addon.getBorderSize() / 2D;
           BoundingBox worldBB = BoundingBox.of(center.toVector(), dist, dist, dist);
           if (worldBB.contains(island.getBoundingBox())) {
               showWorldBarrier(player);
               return;
           }
           // Claim is isolated so show the world barrier 
            WorldBorder wb = Bukkit.createWorldBorder();
            wb.setCenter(l);
            double size = Math.min(island.getRange() * 2D, (island.getProtectionRange()) * 2D);
            wb.setSize(size);
            wb.setWarningDistance(0);
            player.setWorldBorder(wb);
        },
                // No claim - show world border
                () -> showWorldBarrier(player));
    }
    
    private void showWorldBarrier(Player player) {
        if (!addon.inWorld(player.getWorld())) {
            // If the player is not in a world governed by this addon, skip
            return;
        }
        // Get the center of the barrier
        Location center = Objects.requireNonNullElse(addon.getIslands().getSpawnPoint(player.getWorld()), player.getWorld().getSpawnLocation());
        WorldBorder wb = Bukkit.createWorldBorder();
        wb.setCenter(center);
        double size = addon.getBorderSize();
        wb.setSize(size);
        wb.setWarningDistance(5);
        
        if (!wb.isInside(player.getLocation())) {
            User.getInstance(player).sendMessage("strangerrealms.teleporting-to-spawn");
            new SafeSpotTeleport.Builder(addon.getPlugin()).entity(player).location(center).build();
        }
        player.setWorldBorder(wb);
    }

    @Override
    public void hideBorder(User user) {
        user.getPlayer().setWorldBorder(null);
    }

    /**
     * Teleport player back within the island space they are in
     * @param p player
     */
    public void teleportPlayer(Player p) {
        addon.getIslands().getIslandAt(p.getLocation()).ifPresent(i -> {
            Vector unitVector = i.getCenter().toVector().subtract(p.getLocation().toVector()).normalize()
                    .multiply(new Vector(1, 0, 1));
            // Get distance from border
            Location to = p.getLocation().toVector().add(unitVector).toLocation(p.getWorld());
            to.setPitch(p.getLocation().getPitch());
            to.setYaw(p.getLocation().getYaw());
            Util.teleportAsync(p, to, TeleportCause.PLUGIN);
        });
    }

}
