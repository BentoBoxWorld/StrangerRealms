package world.bentobox.stranger.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.events.island.IslandProtectionRangeChangeEvent;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.stranger.StrangerRealms;
import world.bentobox.stranger.border.BorderShower;

/**
 * Listens for player events and manages the border visualization and player containment.
 * This class handles player movement, teleportation, and ensures players stay within their
 * island boundaries using a visual border and movement restrictions.
 * 
 * @author tastybento
 */
public class PlayerListener implements Listener {
    // Vector used to compare x,z coordinates only (y = 0)
    private static final Vector XZ = new Vector(1,0,1);
    private final StrangerRealms addon;
    // Set to track players currently being teleported to prevent recursion
    private final Set<UUID> inTeleport;
    private final BorderShower show;
    // Map to track tasks for mounted players
    private final Map<Player, BukkitTask> mountedPlayers = new HashMap<>();

    public PlayerListener(StrangerRealms addon) {
        this.addon = addon;
        inTeleport = new HashSet<>();
        this.show = addon.getBorderShower();
    }

    /**
     * Handles player join events to set up border visualization.
     * Checks if the player is in a valid world and if borders are enabled for them.
     * 
     * @param e PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Check if player is in the world
        if (!addon.inWorld(player.getWorld())) {
            return;
        }
        if (isOn(player)) {
            // Run one-tick after joining because metadata cannot be set otherwise
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> processEvent(e));
        }
        // Update the border for any online players
        Bukkit.getOnlinePlayers().stream().filter(p -> addon.inWorld(p.getWorld())).forEach(show::showBorder);
    }

    protected void processEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!isOn(player) || !addon.inWorld(player.getWorld())) {
            return;
        }

        User user = User.getInstance(e.getPlayer());

        show.hideBorder(user);
        // Just for sure, disable world Border 
        user.getPlayer().setWorldBorder(null);

        // Show the border if required one tick after   
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> show.showBorder(e.getPlayer()));
    }

    /**
     * Clears the player's border and scheduled tasks on quit.
     * 
     * @param e PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        show.clearUser(User.getInstance(e.getPlayer()));
        // Wait for player to exit
        Bukkit.getScheduler().runTask(addon.getPlugin(), addon::getBorderSize);
    }

    /**
     * Manages border visibility and player teleportation on respawn.
     * 
     * @param e PlayerRespawnEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (isOn(player)) {
            show.clearUser(User.getInstance(e.getPlayer()));
            Bukkit.getScheduler().runTask(addon.getPlugin(), () -> show.showBorder(e.getPlayer()));
        }
    }

    /**
     * Check if the border is on or off
     * @param player player
     * @return true if the border is on, false if not
     */
    private boolean isOn(Player player) {
        // Check if border is off
        User user = User.getInstance(player);
        return user.getMetaData(BorderShower.BORDER_STATE_META_DATA).map(MetaDataValue::asBoolean)
                .orElse(true);

    }

    /**
     * Processes teleportation events to prevent players from teleporting outside their borders.
     * Special handling for ender pearls and other consumable effects.
     * 
     * @param e PlayerTeleportEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (!isOn(player)) {
            return;
        }
        Location to = e.getTo();

        show.clearUser(User.getInstance(player));

        //noinspection ConstantValue
        if (to == null || !addon.inWorld(to.getWorld())) {
            return;
        }

        TeleportCause cause = e.getCause();
        boolean isBlacklistedCause = cause == TeleportCause.ENDER_PEARL || cause == TeleportCause.CONSUMABLE_EFFECT;

        Bukkit.getScheduler().runTask(addon.getPlugin(), () ->
        addon.getIslands().getIslandAt(to).ifPresentOrElse(i -> {
            Optional<Flag> boxedEnderPearlFlag = i.getPlugin().getFlagsManager().getFlag("ALLOW_MOVE_BOX");

            if (isBlacklistedCause
                    && (!i.getProtectionBoundingBox().contains(to.toVector())
                            || !i.onIsland(player.getLocation()))) {
                e.setCancelled(true);
            }

            if (boxedEnderPearlFlag.isPresent()
                    && boxedEnderPearlFlag.get().isSetForWorld(to.getWorld())
                    && cause == TeleportCause.ENDER_PEARL) {
                e.setCancelled(false);
            }

            show.showBorder(player);
        }, () -> {
            if (isBlacklistedCause) {
                e.setCancelled(true);
                return;
            }
            show.hideBorder(User.getInstance(player));
            show.showBorder(player);
        })
                );
    }

    /**
     * Main method for preventing players from leaving their island's protection zone.
     * If a player tries to move outside, they are either:
     * 1. Teleported back to their previous location
     * 2. Moved to the nearest safe location along the border
     * 
     * @param e PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeaveIsland(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!isOn(p)) {
            return;
        }
        Location from = e.getFrom();
        // Check if player is attempting to move outside their allowed area
        if (!outsideCheck(e.getPlayer(), from, e.getTo())) {
            return;
        }
        
        // If player is still in protected area, cancel movement and teleport back
        if (addon.getIslands().getProtectedIslandAt(from).isPresent()) {
            e.setCancelled(true);
            inTeleport.add(p.getUniqueId());
            Util.teleportAsync(p, from).thenRun(() -> inTeleport.remove(p.getUniqueId()));
            return;
        }
        
        // If outside, calculate the closest safe position on border
        addon.getIslands().getIslandAt(p.getLocation()).ifPresent(i -> {
            // Calculate vector pointing from player to island center
            Vector unitVector = i.getProtectionCenter().toVector().subtract(p.getLocation().toVector()).normalize()
                    .multiply(new Vector(1,0,1));
            
            // Skip if no valid direction found
            if (unitVector.lengthSquared() <= 0D) {
                return;
            }
            
            // Perform ray trace to find intersection with border
            RayTraceResult r = i.getProtectionBoundingBox().rayTrace(p.getLocation().toVector(), unitVector, i.getRange());
            if (r != null && checkFinite(r.getHitPosition())) {
                inTeleport.add(p.getUniqueId());
                Location targetPos = r.getHitPosition().toLocation(p.getWorld(), p.getLocation().getYaw(), p.getLocation().getPitch());

                if (!e.getPlayer().isFlying() && !addon.getIslands().isSafeLocation(targetPos)) {
                    switch (targetPos.getWorld().getEnvironment()) {
                    case NETHER:
                        targetPos.getBlock().getRelative(BlockFace.DOWN).setType(Material.NETHERRACK);
                        break;
                    case THE_END:
                        targetPos.getBlock().getRelative(BlockFace.DOWN).setType(Material.END_STONE);
                        break;
                    default:
                        targetPos.getBlock().getRelative(BlockFace.DOWN).setType(Material.STONE);
                        break;
                    }
                }
                Util.teleportAsync(p, targetPos).thenRun(() -> inTeleport.remove(p.getUniqueId()));
            }
        });
    }

    /**
     * Checks if a vector has finite coordinates to prevent invalid teleportation.
     * 
     * @param toCheck Vector to validate
     * @return true if all coordinates are finite numbers
     */
    public boolean checkFinite(Vector toCheck) {
        return NumberConversions.isFinite(toCheck.getX()) && NumberConversions.isFinite(toCheck.getY())
                && NumberConversions.isFinite(toCheck.getZ());
    }

    /**
     * Determines if a player's movement would take them outside their allowed area.
     * Takes into account game mode, world validity, and border settings.
     * 
     * @param player Player being checked
     * @param from Starting location
     * @param to Destination location
     * @return true if movement would place player outside their allowed area
     */
    private boolean outsideCheck(Player player, Location from, Location to) {
        User user = Objects.requireNonNull(User.getInstance(player));

        if ((from.getWorld() != null && from.getWorld().equals(to.getWorld())
                && from.toVector().multiply(XZ).equals(to.toVector().multiply(XZ)))
                || !addon.inWorld(player.getWorld())
                || user.getPlayer().getGameMode() == GameMode.SPECTATOR
                // || !addon.getIslands().getIslandAt(to).filter(i -> addon.getIslands().locationIsOnIsland(player, i.getProtectionCenter())).isPresent()
                || !user.getMetaData(BorderShower.BORDER_STATE_META_DATA).map(MetaDataValue::asBoolean).orElse(true)) {
            return false;
        }
        return addon.getIslands().getIslandAt(to).filter(i -> !i.onIsland(to)).isPresent();
    }

    /**
     * Runs a task while the player is mounting an entity and eject
     * if the entity went outside the protection range
     * @param event - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player) || !isOn(player)) {
            return;
        }

        mountedPlayers.put(player, Bukkit.getScheduler().runTaskTimer(addon.getPlugin(), () -> {
            Location loc = player.getLocation();

            if (!addon.inWorld(loc.getWorld())) {
                return;
            }
            // Eject from mount if outside the protection range
            if (player.getWorldBorder() != null && !player.getWorldBorder().isInside(loc)) {
                // Force the dismount event for custom entities
                if (!event.getMount().eject()) {
                    var dismountEvent = new EntityDismountEvent(player, event.getMount());
                    Bukkit.getPluginManager().callEvent(dismountEvent);
                }
            }
        }, 1, 20));
    }

    /**
     * Cancel the running task if the player was mounting an entity
     * @param event - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (isOn(player)) {
            BukkitTask task = mountedPlayers.get(player);
            if (task == null) {
                return;
            }

            task.cancel();
            mountedPlayers.remove(player);
        }
    }


    /**
     * Refreshes the barrier view when the player moves (more than just moving their head)
     * @param e event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        // Remove head movement
        if (isOn(player) && !e.getFrom().toVector().equals(e.getTo().toVector())) {
            show.refreshView(User.getInstance(e.getPlayer()));
        }
    }

    /**
     * Refresh the view when riding in a vehicle
     * @param e event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent e) {
        // Remove head movement
        if (!e.getFrom().toVector().equals(e.getTo().toVector())) {
            e.getVehicle().getPassengers().stream().filter(Player.class::isInstance).map(Player.class::cast)
                    .filter(this::isOn).forEach(p -> show.refreshView(User.getInstance(p)));
        }
    }

    /**
     * Hide and then show the border to react to the change in protection area
     * @param e IslandProtectionRangeChangeEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProtectionRangeChange(IslandProtectionRangeChangeEvent e) {
        // Hide and show again
        e.getIsland().getPlayersOnIsland().forEach(player -> {
            if (isOn(player)) {
                show.hideBorder(User.getInstance(player));
                show.showBorder(player);
            }
        });
    }
}
