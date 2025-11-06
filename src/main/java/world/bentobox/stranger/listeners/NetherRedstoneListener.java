package world.bentobox.stranger.listeners;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import world.bentobox.bentobox.util.Util;
import world.bentobox.stranger.StrangerRealms;

/**
 * Listens to redstone events and makes them happen in the overworld - maybe. Spooky!
 * @author tastybento
 */
public class NetherRedstoneListener implements Listener {

    private static final Random rand = new Random();
    private final StrangerRealms addon;


    public NetherRedstoneListener(StrangerRealms addon) {
        this.addon = addon;

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        // Check if this is in the world
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK
                || !addon.getSettings().isUseUpsideDown() 
                || addon.getSettings().getRedstoneChance() > rand.nextInt(101)
                || addon.getNetherWorld() == null 
                || !addon.getNetherWorld().equals(e.getPlayer().getWorld())) {
            return;
        }
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Material material = clickedBlock.getType();
        // Check the material for specific interactable blocks
        if (Tag.BUTTONS.isTagged(material) || material == Material.LEVER) {
            Location overWorldLoc = clickedBlock.getLocation().toVector().toLocation(addon.getNetherWorld());
            Util.getChunkAtAsync(overWorldLoc).thenRun(() -> {
                Block b = overWorldLoc.getBlock();
                if (b.getType() == material) {
                    // Get the BlockData object
                    BlockData blockData = b.getBlockData(); 

                    // Check if the BlockData can be powered
                    if (blockData instanceof Powerable powerable) {

                        // This is a button or lever (or other powerable block)
                        // Check if it is currently powered
                        boolean isPowered = powerable.isPowered();

                        // Toggle the power state
                        powerable.setPowered(!isPowered);

                        // Apply the change back to the block
                        b.setBlockData(powerable);
                    }
                }
            });
        }
    }
}


