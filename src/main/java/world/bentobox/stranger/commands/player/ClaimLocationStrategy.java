package world.bentobox.stranger.commands.player;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.island.NewIslandLocationStrategy;
import world.bentobox.bentobox.util.Util;
import world.bentobox.stranger.StrangerRealms;

public class ClaimLocationStrategy implements NewIslandLocationStrategy {

    protected final BentoBox plugin = BentoBox.getInstance();
    private StrangerRealms addon;
    
    public ClaimLocationStrategy(StrangerRealms addon) {
        this.addon = addon;
    }

    @Override
    public Location getNextLocation(World world, User user) {
        if (!Util.getWorld(user.getWorld()).equals(world)) {
            user.sendMessage("stranger.errors.not-in-world");
            return null;
        }
        Location location = user.getLocation();
        // Quick check using the claim grid cache.
        if (plugin.getIslands().isIslandAt(location)) {
            user.sendMessage("stranger.errors.already-claimed");
            return null;
        }

        // Check the four corners of the claim protection area to be more thorough.
        int dist = plugin.getIWM().getIslandDistance(world);
        Set<Location> locs = new HashSet<>();
        locs.add(location);

        // Define the corners of the claim's bounding box.
        locs.add(new Location(world, location.getX() - dist, 0, location.getZ() - dist));
        locs.add(new Location(world, location.getX() - dist, 0, location.getZ() + dist - 1));
        locs.add(new Location(world, location.getX() + dist - 1, 0, location.getZ() - dist));
        locs.add(new Location(world, location.getX() + dist - 1, 0, location.getZ() + dist - 1));

         for (Location l : locs) {
            // Check if a claim exists
            if (plugin.getIslands().getIslandAt(l).isPresent()) {
                user.sendMessage("stranger.errors.overlap", "[xyz]", Util.xyz(l.toVector()));
                return null;
            }
            // Check that everything is within the global world border
            if (!addon.getSettings().isDisableWorldBorder() &&  !user.getPlayer().getWorldBorder().isInside(l)) {
                user.sendMessage("stranger.errors.no-fit-inside");
                //BentoBox.getInstance().logDebug(l + " would be outside the world border");
                return null;
            }
        }
        
        return user.getLocation();
    }

    @Override
    public Location getNextLocation(World world) {
        return null;
    }

}
