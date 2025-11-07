package world.bentobox.stranger.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.BoundingBox;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.ExpiringSet;
import world.bentobox.bentobox.util.Pair;
import world.bentobox.stranger.StrangerRealms;
import world.bentobox.stranger.database.NetherChunksMade;

/**
 * Listener for managing Nether chunk creation and player interactions with Nether portals.
 */
public class NetherChunkMaker implements Listener {

    private static final int ROOF_HEIGHT = 107;
    private static final String NETHER_CHUNKS_TABLE = "NetherChunks";
    private StrangerRealms addon;
    private Random rand = new Random();
    private final Database<NetherChunksMade> handler;
    private NetherChunksMade netherChunksMade;
    private final int maxChestFills;
    private ExpiringSet<UUID> portalPlayer = new ExpiringSet<>(10, TimeUnit.SECONDS);
    private final TreeMap<Integer, LootTable> chestContents;

    // Overworld Biome -> Nether Biome
    public static final Map<Biome, Biome> BIOME_MAPPING;
    static {
        Map<Biome, Biome> biomeMap = new HashMap<>();
        // Lush/Jungle/Wetlands (Crimson)
        biomeMap.put(Biome.SWAMP, Biome.CRIMSON_FOREST);
        biomeMap.put(Biome.MANGROVE_SWAMP, Biome.CRIMSON_FOREST);
        biomeMap.put(Biome.JUNGLE, Biome.CRIMSON_FOREST);
        biomeMap.put(Biome.BAMBOO_JUNGLE, Biome.CRIMSON_FOREST);
        biomeMap.put(Biome.DARK_FOREST, Biome.CRIMSON_FOREST);

        // Hot/Dry/Mountainous (Basalt)
        biomeMap.put(Biome.DESERT, Biome.BASALT_DELTAS);
        biomeMap.put( Biome.SAVANNA_PLATEAU, Biome.BASALT_DELTAS);
        biomeMap.put(Biome.BADLANDS, Biome.BASALT_DELTAS);
        biomeMap.put(Biome.JAGGED_PEAKS, Biome.BASALT_DELTAS);
        biomeMap.put(Biome.STONY_PEAKS, Biome.BASALT_DELTAS);

        // Cold/Snowy (Soul Sand)
        biomeMap.put(Biome.SNOWY_PLAINS, Biome.SOUL_SAND_VALLEY);
        biomeMap.put(Biome.TAIGA, Biome.SOUL_SAND_VALLEY);
        biomeMap.put( Biome.ICE_SPIKES, Biome.SOUL_SAND_VALLEY);
        biomeMap.put(Biome.GROVE, Biome.SOUL_SAND_VALLEY);

        // Unique (Warped)
        biomeMap.put(Biome.MUSHROOM_FIELDS, Biome.WARPED_FOREST);
        biomeMap.put(Biome.CHERRY_GROVE, Biome.WARPED_FOREST);

        // Special
        biomeMap.put(Biome.DEEP_DARK, Biome.DEEP_DARK);
        biomeMap.put(Biome.DRIPSTONE_CAVES, Biome.DEEP_DARK);
        biomeMap.put(Biome.LUSH_CAVES, Biome.DEEP_DARK);

        BIOME_MAPPING = Collections.unmodifiableMap(biomeMap);
    }

    public NetherChunkMaker(StrangerRealms addon) {
        super();
        this.addon = addon;
        handler = new Database<>(addon, NetherChunksMade.class);
        if (handler.objectExists(NETHER_CHUNKS_TABLE)) {
            netherChunksMade = handler.loadObject(NETHER_CHUNKS_TABLE);
        } else {
            // Initial entry
            netherChunksMade = new NetherChunksMade();
            handler.saveObjectAsync(netherChunksMade);
        }
        // Chests
        maxChestFills = addon.getSettings().getChestFills();
        chestContents = new TreeMap<>();
        chestContents.put(1, LootTables.BASTION_TREASURE.getLootTable()); // Rare
        chestContents.put(4, LootTables.NETHER_BRIDGE.getLootTable()); // Uncommon
        chestContents.put(9, LootTables.GHAST.getLootTable()); // Common
        chestContents.put(14, LootTables.PIGLIN_BARTERING.getLootTable()); // Common
    }

    /**
     * Clears the database of chunks made. Called if the world doesn't exist, e.g., admin deleted it manually
     */
    public void clearDatabase() {
        if (handler.objectExists(NETHER_CHUNKS_TABLE)) {
            handler.deleteID(NETHER_CHUNKS_TABLE);
        }
    }

    /**
     * Handles the event when a player enters a Nether portal.
     * 
     * @param e The event triggered when an entity enters a portal.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNetherPortalEnter(EntityPortalEnterEvent e) {
        // Only trigger if the player is going from the overworld to the nether
        if (e.getPortalType() != PortalType.NETHER 
                || e.getEntityType() != EntityType.PLAYER
                || !addon.inWorld(e.getLocation())
                || portalPlayer.contains(e.getEntity().getUniqueId())) { // If they are in the map, ignore
            return;
        }
        Player p = (Player)e.getEntity();
        // Add the player as teleporting
        portalPlayer.add(p.getUniqueId());
        if (e.getLocation().getWorld().getEnvironment() == Environment.NETHER) {
            return; // No action needed if already in the Nether
        }

        // Check if the player is holding a warped compass
        if (StrangerRealms.isWarpedCompass(p.getInventory().getItemInMainHand()) 
                || StrangerRealms.isWarpedCompass(p.getInventory().getItemInOffHand())) {
            // Refresh the Nether chunks
            refreshNetherChunks(p);
        }
    }

    /**
     * Handles the event when a player enters the UpsideDown.
     * 
     * @param e The event triggered
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNetherPortalExit(PlayerChangedWorldEvent e) {
        if (addon.inWorld(e.getPlayer().getWorld()) && e.getPlayer().getWorld().getEnvironment() == Environment.NETHER) {
            User.getInstance(e.getPlayer()).notify(addon.getNetherWorld(), "stranger.nether.welcome");
        }
    }

    /**
     * Refreshes the Nether chunks based on the player's location.
     * 
     * @param p The player who triggered the refresh.
     */
    private void refreshNetherChunks(Player p) {
        int chunkRadius = Bukkit.getViewDistance();
        int x = p.getLocation().getChunk().getX();
        int z = p.getLocation().getChunk().getZ();
        // Removing the listing of chunks from the database will cause them to be re-made
        for (int i = x - chunkRadius; i < x + chunkRadius; i++) {
            for (int j = z - chunkRadius; j < z + chunkRadius; j++) {
                this.netherChunksMade.getChunkSet().remove(Pair.of(i, j));
            }
        }
        handler.saveObject(netherChunksMade);
        User.getInstance(p).sendMessage("stranger.nether.refresh");
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> p.playSound(p, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1F, 1F));

        // Reduce the amount of the warped compass used
        reduceWarpedCompassAmount(p);
    }

    /**
     * Reduces the amount of the warped compass in the player's inventory.
     * 
     * @param p The player whose inventory is being checked.
     */
    private void reduceWarpedCompassAmount(Player p) {
        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        if (StrangerRealms.isWarpedCompass(mainHandItem)) {
            mainHandItem.subtract(1); // Reduce the amount by 1
            return;
        } 

        // If not in the main hand, check the off-hand
        ItemStack offHandItem = p.getInventory().getItemInOffHand();
        if (StrangerRealms.isWarpedCompass(offHandItem)) {
            offHandItem.subtract(1); // Reduce the amount by 1
        }
    }

    /**
     * Handles the event when a chunk is loaded in the Nether.
     * 
     * @param e The event triggered when a chunk is loaded.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!isValidChunkLoadEvent(e)) {
            return; // Ignore invalid chunk load events
        }

        if (!netherChunksMade.getChunkSet().add(Pair.of(e.getChunk().getX(), e.getChunk().getZ()))) {
            return; // Ignore if the chunk is already processed
        }

        handler.saveObjectAsync(netherChunksMade); // Save to database
        clearTileEntities(e); // Clear tile entities in the chunk
        clearEntities(e); // Clear entities in the chunk
        convertBlocks(e); // Convert blocks from overworld to Nether
        spawnNetherEntities(e); // Spawn Nether entities
    }

    /**
     * Validates if the chunk load event is applicable for processing.
     * 
     * @param e The chunk load event.
     * @return true if valid, false otherwise.
     */
    private boolean isValidChunkLoadEvent(ChunkLoadEvent e) {
        return e.getWorld().getEnvironment() == Environment.NETHER 
                && addon.getSettings().isUseUpsideDown()
                && addon.inWorld(e.getWorld());
    }

    /**
     * Clears tile entities in the loaded chunk that are below the roof height.
     * 
     * @param e The chunk load event.
     */
    private void clearTileEntities(ChunkLoadEvent e) {
        Arrays.stream(e.getChunk().getTileEntities())
        .filter(en -> en.getLocation().getBlockY() < ROOF_HEIGHT && en instanceof InventoryHolder)
        .forEach(tileEntity -> ((InventoryHolder) tileEntity).getInventory().clear());
    }

    /**
     * Clears entities in the loaded chunk that are below the roof height.
     * 
     * @param e The chunk load event.
     */
    private void clearEntities(ChunkLoadEvent e) {
        Arrays.stream(e.getChunk().getEntities())
        .filter(en -> en.getType() != EntityType.PLAYER && en.getLocation().getBlockY() < ROOF_HEIGHT)
        .forEach(Entity::remove);
    }

    /**
     * Converts blocks in the loaded chunk from overworld to Nether equivalents.
     * 
     * @param e The chunk load event.
     */
    private void convertBlocks(ChunkLoadEvent e) {
        // Get the overworld chunk we are copying from
        Chunk overworldChunk = addon.getOverWorld().getChunkAt(e.getChunk().getX(), e.getChunk().getZ());
        // Determine the attrition
        int rawAttritionValue = addon.getSettings().getAttrition();
        double attrition = (rawAttritionValue >= 0 && rawAttritionValue <= 100)
                ? rawAttritionValue / 100.0 // Calculate the percentage
                        : 0.05; // Default to 5%
        // Count chests
        int chestFills = 0;

        // Check for structures
        List<BoundingBox> structures = new ArrayList<>();
        e.getChunk().getStructures().forEach(gs -> structures.add(gs.getBoundingBox()));

        // Loop through the chunk and set blocks
        for (int y = e.getWorld().getMinHeight() + 7; y < ROOF_HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block overworldBlock = overworldChunk.getBlock(x, y, z);
                    Block newBlock = e.getChunk().getBlock(x, y, z);
                    newBlock.setBiome(BIOME_MAPPING.getOrDefault(overworldBlock.getBiome(), Biome.NETHER_WASTES)); // Set biome for the new block
                    if (newBlock.getBiome() == Biome.DEEP_DARK
                            || overworldBlock.getType() == newBlock.getType() 
                            || newBlock.getType() == Material.NETHER_PORTAL // We must not touch these otherwise errors occur
                            || y > 100 && rand.nextDouble() < attrition
                            || (inStructure(e.getChunk().getX(), e.getChunk().getZ(), x, y, z, structures) && (newBlock.getType() == Material.NETHER_BRICKS
                            || newBlock.getType() == Material.NETHER_BRICK_FENCE
                            || newBlock.getType() == Material.NETHER_BRICK_SLAB
                            || newBlock.getType() == Material.NETHER_BRICK_STAIRS
                            || newBlock.getType() == Material.NETHER_BRICK_WALL
                            || newBlock.getType() == Material.CHISELED_NETHER_BRICKS
                            || newBlock.getType() == Material.CRACKED_NETHER_BRICKS
                            || newBlock.getType() == Material.BONE_BLOCK
                            || newBlock.getType() == Material.SPAWNER
                            || newBlock.getType() == Material.CHEST))            
                            ) {
                        continue; // Skip if the block types are the same or if conditions are met
                    }
                    BlockData overworldBlockData = overworldBlock.getBlockData();
                    Material material = overworldBlockData.getMaterial(); // Get the material for the switch
                    BlockData newBlockData = overworldBlockData.clone(); // Clone the BlockData to modify it

                    if (newBlockData instanceof Waterlogged waterlogged) {
                        waterlogged.setWaterlogged(false);
                    }

                    // Biome
                    if (overworldBlock.getBiome() == Biome.DEEP_DARK) {
                        // Replicate the creepy deep dark
                    } else 
                        // --- Tag-Based Conversion ---

                        // Convert blocks based on their tags
                        if (Tag.BUTTONS.isTagged(material)) {
                            newBlockData = rand.nextBoolean() ? Material.AIR.createBlockData() : Material.STONE_BUTTON.createBlockData(); // Converts all buttons to stone
                        } else if (Tag.DOORS.isTagged(material) || Tag.FENCE_GATES.isTagged(material)) {
                            newBlockData = Material.AIR.createBlockData(); // Converts all doors to air
                        } else if (Tag.CORAL_BLOCKS.isTagged(material)) {
                            newBlockData = Material.NETHERRACK.createBlockData(); // Converts all coral blocks to netherrack
                        } else if (Tag.LOGS.isTagged(material)) {
                            if (newBlock.getBiome() == Biome.CRIMSON_FOREST) {
                                newBlockData = Material.CRIMSON_STEM.createBlockData(); // Converts all overworld logs to Warped Stem
                            } else {
                                newBlockData = Material.WARPED_STEM.createBlockData(); // Converts all overworld logs to Warped Stem
                                e.getChunk().getBlock(x, y, z).setBiome(Biome.WARPED_FOREST);
                            }
                        } else if (Tag.LEAVES.isTagged(material)) {
                            if (newBlock.getBiome() == Biome.CRIMSON_FOREST) {
                                newBlockData = Material.WARPED_WART_BLOCK.createBlockData(); // Converts all overworld leaves to Nether Wart Block
                            } else {
                                newBlockData = Material.NETHER_WART_BLOCK.createBlockData(); // Converts all overworld leaves to Nether Wart Block
                            }
                        } else if (Tag.STONE_BRICKS.isTagged(material)) {
                            newBlockData = rand.nextBoolean() ? Material.AIR.createBlockData() : Material.NETHER_BRICKS.createBlockData(); // Converts all stone bricks to Nether Bricks
                        } else if (Tag.SAND.isTagged(material)) {
                            newBlockData = Material.SOUL_SAND.createBlockData(); // Converts all types of sand to Soul Sand
                            e.getChunk().getBlock(x, y, z).setBiome(Biome.SOUL_SAND_VALLEY);
                        } else if (Tag.DIRT.isTagged(material)) {
                            newBlockData = Material.NETHERRACK.createBlockData(); // Converts all dirt/grass-like blocks to Netherrack
                        } else if (Tag.FLOWERS.isTagged(material) || Tag.SAPLINGS.isTagged(material)) {
                            if (newBlock.getBiome() == Biome.CRIMSON_FOREST) {
                                newBlockData = rand.nextDouble() < 0.2 ? newBlockData = Material.CRIMSON_FUNGUS.createBlockData() : Material.CRIMSON_ROOTS.createBlockData(); // Converts flowers/saplings to Crimson Roots
                            } else if (newBlock.getBiome() == Biome.WARPED_FOREST) {
                                newBlockData = rand.nextDouble() < 0.2 ? newBlockData = Material.WARPED_FUNGUS.createBlockData() : Material.WARPED_ROOTS.createBlockData(); // Converts flowers/saplings to Warped Roots
                            } else {
                                newBlockData = rand.nextBoolean() ? Material.RED_MUSHROOM.createBlockData() : Material.BROWN_MUSHROOM.createBlockData();
                            }
                        } else if (Tag.COAL_ORES.isTagged(material) || Tag.IRON_ORES.isTagged(material) || Tag.GOLD_ORES.isTagged(material)) {
                            // Converts overworld ores to their Nether equivalent (or just a common Nether block)
                            newBlockData = Material.NETHER_GOLD_ORE.createBlockData(); // Example conversion
                        } else if (Tag.CROPS.isTagged(material)) {
                            newBlockData = Material.NETHER_WART.createBlockData();
                            e.getChunk().getBlock(x, Math.max(y-1, e.getWorld().getMinHeight()), z).setType(Material.SOUL_SAND);
                        } else if (Tag.BANNERS.isTagged(material)) {
                            newBlockData = Material.BLACK_BANNER.createBlockData();
                        } else if (Tag.TRAPDOORS.isTagged(material)) {
                            newBlockData = Material.WARPED_TRAPDOOR.createBlockData();
                        } else if (Tag.BADLANDS_TERRACOTTA.isTagged(material)) {
                            newBlockData = Material.NETHER_BRICKS.createBlockData();
                        } else if(Tag.ALL_HANGING_SIGNS.isTagged(material)) {
                            newBlockData = Material.WARPED_HANGING_SIGN.createBlockData();
                        } else if(Tag.ALL_SIGNS.isTagged(material)) {
                            newBlockData = Material.WARPED_SIGN.createBlockData();
                        } else if(Tag.ANVIL.isTagged(material)) {
                            newBlockData = Material.CRACKED_POLISHED_BLACKSTONE_BRICKS.createBlockData();
                        } else if(Tag.CAMPFIRES.isTagged(material)) {
                            newBlockData = Material.SOUL_CAMPFIRE.createBlockData();
                        } else if(Tag.CANDLE_CAKES.isTagged(material)) {
                            newBlockData = Material.WARPED_HYPHAE.createBlockData();
                        } else if(Tag.COAL_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_QUARTZ_ORE.createBlockData();
                        } else if(Tag.COPPER_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_QUARTZ_ORE.createBlockData();
                        } else if(Tag.DIAMOND_ORES.isTagged(material)) {
                            newBlockData = Material.ANCIENT_DEBRIS.createBlockData();
                        } else if (Tag.EMERALD_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_GOLD_ORE.createBlockData();
                        } else if (Tag.GOLD_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_GOLD_ORE.createBlockData();
                        } else if (Tag.IRON_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_GOLD_ORE.createBlockData();
                        } else if (Tag.LAPIS_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_GOLD_ORE.createBlockData();
                        } else if (Tag.REDSTONE_ORES.isTagged(material)) {
                            newBlockData = Material.NETHER_QUARTZ_ORE.createBlockData();
                        } else if (Tag.BEDS.isTagged(material)) {
                            newBlockData = Material.GLOWSTONE.createBlockData();
                        } else if (Tag.BEEHIVES.isTagged(material)) {
                            newBlockData = Material.GLOWSTONE.createBlockData();
                        } else if (Tag.BARS.isTagged(material)) {
                            newBlockData = Material.IRON_BARS.createBlockData();
                        } else if (Tag.CAVE_VINES.isTagged(material)) {
                            newBlockData = Material.GLOWSTONE.createBlockData();
                        } else if (Tag.CAULDRONS.isTagged(material)) {
                            if (rand.nextBoolean()) {
                                newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() :Material.CAULDRON.createBlockData();
                            } else {
                                newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() :Material.LAVA_CAULDRON.createBlockData();
                            }
                        } else if (Tag.COPPER_CHESTS.isTagged(material)) {
                            newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() : Material.OXIDIZED_COPPER_CHEST.createBlockData();

                        }  else if (Tag.FENCES.isTagged(material)) {
                            newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() :Material.NETHER_BRICK_FENCE.createBlockData();
                        } else if (Tag.SLABS.isTagged(material)) {
                            newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() :Material.NETHER_BRICK_SLAB.createBlockData();
                        } else if (Tag.STAIRS.isTagged(material)) {

                            newBlockData = rand.nextDouble() < attrition ? Material.AIR.createBlockData() : Material.NETHER_BRICK_STAIRS.createBlockData() ;

                        } else if (Tag.WALLS.isTagged(material)) {
                            newBlockData = rand.nextDouble() < 0.1 ? Material.AIR .createBlockData() :  Material.NETHER_BRICK_WALL.createBlockData();
                        } else if (newBlock.getType() == Material.OBSIDIAN) {
                            newBlockData = Material.OBSIDIAN.createBlockData();
                        }
                    // Individual Block Conversion 
                        else { // Only proceed to switch if no Tag conversion was applied
                            switch (material) {
                            case AIR:
                                // Nothing to do here
                                break;
                            case BELL, ENCHANTING_TABLE, LECTERN:
                                // Turn into a spawner
                                newBlockData = Material.SPAWNER.createBlockData();
                            break;                          
                            case GRASS_BLOCK:
                                if (newBlock.getBiome() == Biome.WARPED_FOREST) {
                                    newBlockData = Material.WARPED_NYLIUM.createBlockData();
                                } else if (newBlock.getBiome() == Biome.CRIMSON_FOREST) {
                                    newBlockData = Material.CRIMSON_NYLIUM.createBlockData();
                                } else if (newBlock.getBiome() == Biome.SOUL_SAND_VALLEY) {
                                    newBlockData = rand.nextDouble() < 0.2 ? Material.SOUL_SOIL.createBlockData() : Material.SOUL_SAND.createBlockData();
                                } else {
                                    newBlockData = rand.nextDouble() < 0.2 ? Material.SOUL_SOIL.createBlockData() : Material.NETHERRACK.createBlockData();
                                }
                                break;
                            case NETHER_PORTAL:
                                if (e.getChunk().getBlock(x, y, z).getType() == Material.NETHER_PORTAL) {
                                    newBlockData = Material.NETHER_PORTAL.createBlockData();
                                } else {
                                    newBlockData = Material.AIR.createBlockData();
                                }
                                break;
                            case OBSIDIAN:
                                // Set Obi to air
                                newBlockData = Material.AIR.createBlockData();
                                break;
                            case HAY_BLOCK:
                                if (newBlock.getBiome() == Biome.DEEP_DARK) {
                                    newBlockData = Material.AIR.createBlockData();
                                } else if (newBlock.getBiome() == Biome.CRIMSON_FOREST || newBlock.getBiome() == Biome.WARPED_FOREST ) {
                                    newBlockData = Material.SHROOMLIGHT.createBlockData();
                                } else {
                                    newBlockData = Material.GLOWSTONE.createBlockData();
                                }
                                break;
                            case GRAVEL, RED_MUSHROOM, BROWN_MUSHROOM:
                                break;
                            case ANDESITE:
                                newBlockData = Material.SOUL_SOIL.createBlockData();
                                break;
                            case DIORITE:
                            case GRANITE:
                                newBlockData = Material.BASALT.createBlockData();
                                break;
                            case STONE:
                            case BUBBLE_COLUMN:
                                newBlockData = Material.NETHERRACK.createBlockData();
                                break;
                            case KELP:
                            case SEAGRASS:
                                newBlockData = Material.MAGMA_BLOCK.createBlockData();
                                break;
                            case WATER:
                                if (newBlock.getBiome() == Biome.DEEP_DARK) {
                                    newBlockData = Material.AIR.createBlockData();
                                } else {
                                    newBlockData = Material.LAVA.createBlockData();
                                }
                                break;
                            case LAVA:
                                // Keep lava as lava
                                break;
                            case TORCH:
                            case WALL_TORCH:
                                // Convert to a more intense light source
                                newBlockData = Material.SOUL_TORCH.createBlockData();
                                break;
                            case COBBLESTONE:
                                newBlockData = Material.BASALT.createBlockData();
                                break;
                            case TALL_GRASS:
                                newBlockData = Material.AIR.createBlockData();
                                break;
                            case SHORT_GRASS:
                                if (newBlock.getBiome() == Biome.WARPED_FOREST) {
                                    newBlockData = Material.NETHER_SPROUTS.createBlockData();
                                    break;
                                }
                            case SHORT_DRY_GRASS:
                                if (rand.nextDouble() < attrition) {
                                    newBlockData = Material.FIRE.createBlockData();
                                } else {
                                    newBlockData = Material.AIR.createBlockData();
                                }
                                break;
                            case GLASS:
                            case GLASS_PANE:
                                // Convert to a dark, smoky pane
                                newBlockData = Material.BLACK_STAINED_GLASS_PANE.createBlockData();
                                break;
                            case BEDROCK:
                                // Bedrock remains bedrock
                                break;
                            case BRICKS:
                                newBlockData = Material.NETHER_BRICK.createBlockData();
                                break;
                            case CHEST:
                                break;
                            case SPAWNER:
                                break;
                            case VINE, CAVE_VINES:
                                if (newBlock.getBiome() == Biome.WARPED_FOREST) {
                                    newBlockData = Material.WEEPING_VINES.createBlockData();
                                } else {
                                    newBlockData = Material.TWISTING_VINES.createBlockData();
                                }
                            break;
                            // Redstone survives, mostly
                            case REDSTONE_WIRE, REDSTONE_TORCH, REDSTONE_LAMP, REDSTONE_WALL_TORCH, COMPARATOR, LEVER, RAIL, POWERED_RAIL:
                                if (rand.nextDouble() < attrition) {
                                    newBlockData = Material.FIRE.createBlockData();
                                }
                            break;

                            case CALCITE, SMOOTH_BASALT, AMETHYST_CLUSTER, LARGE_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, 
                            SMALL_AMETHYST_BUD, AMETHYST_BLOCK, BUDDING_AMETHYST, INFESTED_COBBLESTONE, INFESTED_CRACKED_STONE_BRICKS,
                            END_PORTAL_FRAME,
                            GRAY_WOOL,
                            SOUL_FIRE, SOUL_SAND, REDSTONE_BLOCK, TARGET, SOUL_LANTERN, CANDLE,
                            SCULK, SCULK_VEIN,SCULK_SENSOR,SCULK_CATALYST,SCULK_SHRIEKER:
                                // These stay
                                break;
                            default:
                                newBlockData = Material.BLACKSTONE.createBlockData();
                                break;
                            }
                        }
                    // Apply the new BlockData to the shadow world chunk
                    newBlock.setBlockData(newBlockData, false);

                    // Set aspects of the block to match the overworld
                    if (overworldBlockData instanceof Fence fence && newBlockData instanceof Fence newFence) {
                        fence.getFaces().forEach(bf -> newFence.setFace(bf, true));
                    }
                    if (overworldBlockData instanceof Stairs stairs && newBlockData instanceof Stairs newStairs) {
                        newStairs.setFacing(stairs.getFacing());
                    }
                    if (overworldBlockData instanceof Slab slab && newBlockData instanceof Slab newSlab) {
                        newSlab.setType(slab.getType());
                    }
                    if (newBlock.getState() instanceof Chest chest && (maxChestFills < 0 || chestFills < maxChestFills)) {
                        chestFills++;
                        // If it's a chest, then put some random stuff in it
                        populateChest(overworldBlock, overworldBlockData, chest, newBlockData);
                    }
                    newBlock.setBlockData(newBlockData, false); // Finalize the block data

                    // Spawner
                    if (newBlock.getType() == Material.SPAWNER) {
                        BlockState state = newBlock.getState();
                        if (state instanceof CreatureSpawner spawner) {
                            spawner.setSpawnedType(EntityType.BLAZE);
                            spawner.setDelay(-1); 
                            spawner.update(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if a specific block coordinate (relative or absolute) is contained
     * within any of the provided list of BoundingBoxes.
     *
     * @param chunkX The chunk's X-coordinate (multiplied by 16) where the search is centered.
     * @param chunkZ The chunk's Z-coordinate (multiplied by 16) where the search is centered.
     * @param x The local X-coordinate (0-15) of the block within the chunk, or the absolute world X-coordinate.
     * @param y The world Y-coordinate of the block.
     * @param z The local Z-coordinate (0-15) of the block within the chunk, or the absolute world Z-coordinate.
     * @param structures A list of BoundingBox objects representing structures.
     * @return true if the block is inside any structure's bounding box, false otherwise.
     */
    private boolean inStructure(int chunkX, int chunkZ, int x, int y, int z, List<BoundingBox> structures) {
        final double worldX = chunkX * 16 + x;
        final double worldY = y;
        final double worldZ = chunkZ * 16 + z;

        for (BoundingBox box : structures) {
            if (box.contains(worldX, worldY, worldZ)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Populates a chest with loot from a defined LootTable.
     * @param overworldBlock 
     * @param bd 
     * 
     * @param chest The chest to populate.
     * @param newBlockData 
     */
    private void populateChest(Block overworldBlock, BlockData bd, Chest chest, BlockData newBlockData) {
        Location chestLocation = chest.getLocation();
        if (overworldBlock.getState() instanceof Chest oldChest && oldChest.getLootTable() != null) {
            chest.setLootTable(oldChest.getLootTable());
        } else {
            // Set the loot table
            // Total weight is 1 + 3 + 5 + 5 = 14
            int totalWeight = chestContents.lastKey(); 
            // Generate random number in the range [0, 13]
            int randomValue = rand.nextInt(totalWeight);
            // Select the correct loot table
            chest.setLootTable(chestContents.higherEntry(randomValue).getValue());
        }
        // Set a seed so it gets fills on first open.
        chest.getInventory().clear();
        chest.setSeed(System.currentTimeMillis() + chestLocation.hashCode());

        // Facing
        if (bd instanceof org.bukkit.block.data.type.Chest chestData && newBlockData instanceof org.bukkit.block.data.type.Chest newChest) {
            newChest.setFacing(chestData.getFacing());
        }
        chest.update(true);
    }

    /**
     * Spawns Nether entities based on the entities present in the loaded chunk.
     * 
     * @param e The chunk load event.
     */
    private void spawnNetherEntities(ChunkLoadEvent e) {
        // Now do Mobs
        Arrays.stream(e.getChunk().getEntities())
        .filter(en -> en instanceof LivingEntity)
        .forEach(en -> {
            EntityType newType = getNetherEnt(en.getType());
            if (newType != null) {
                addon.getNetherWorld().spawnEntity(en.getLocation().toVector().toLocation(addon.getNetherWorld()), newType, true);
            }
        });
    }

    private EntityType getNetherEnt(EntityType type) {
        return  switch (type) {
        case ALLAY:
            // A friendly flying helper, creatively mapped to the friendly, flying GHAST?
            yield EntityType.GHAST;
        case ARMADILLO:
            // A shell-armored creature, maybe a MAGMA_CUBE as a rolling/bouncing threat?
            yield EntityType.MAGMA_CUBE;
        case AXOLOTL:
            // An aquatic, helpful mob, creatively mapped to the fiery BLAZE
            yield EntityType.BLAZE;
        case BAT:
            // A small flying creature, mapped to the flying, hostile GHAST
            yield EntityType.GHAST;
        case BEE:
            // A stinging insect, maybe a smaller, aggressive MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case BLAZE:
            // Already a Nether mob, keep it
            yield EntityType.BLAZE;
        case BOGGED:
            // A variant of SKELETON, use WITHER_SKELETON
            yield EntityType.WITHER_SKELETON;
        case BREEZE:
            // Already a mob from a deep structure, could default or use a powerful Nether mob
            yield EntityType.BLAZE; // Or null, as it's not strictly Overworld
        case CAMEL:
            // A large rideable desert mob, mapped to the ridable STRIDER
            yield EntityType.STRIDER;
        case CAT:
            // A smaller, friendly ground mob, mapped to the aggressive HOGLIN
            yield EntityType.HOGLIN;
        case CAVE_SPIDER:
            yield EntityType.CAVE_SPIDER;
        case CHICKEN:
            // A small, passive mob, mapped to the small, aggressive ZOMBIFIED_PIGLIN
            yield EntityType.ZOMBIFIED_PIGLIN;
        case COD:
            // An Overworld fish, mapped to the hostile GHAST (flying over lava like fish in water)
            yield EntityType.GHAST;
        case COPPER_GOLEM:
            // A golem variant, he continues to exist here!
            yield EntityType.COPPER_GOLEM;
        case COW:
            // A large, passive mob, mapped to the large, aggressive HOGLIN
            yield EntityType.HOGLIN;
        case CREEPER:
            // An explosive mob, mapped to the projectile-shooting GHAST
            yield EntityType.GHAST;
        case DONKEY:
            // A pack animal, mapped to the rideable STRIDER
            yield EntityType.STRIDER;
        case ENDERMAN:
            // Already a mob that can spawn in the Nether, keep it
            yield EntityType.ENDERMAN;
        case ENDERMITE:
            // A small burrowing mob, mapped to the small MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case EVOKER:
            // An illager type, mapped to the powerful PIGLIN_BRUTE
            yield EntityType.PIGLIN_BRUTE;
        case FOX:
            // A cunning predator, mapped to the aggressive PIGLIN
            yield EntityType.PIGLIN;
        case FROG:
            // A jumping amphibian, mapped to the bouncing MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case GHAST:
            // Already a Nether mob, keep it
            yield EntityType.GHAST;
        case GLOW_SQUID:
            // A bioluminescent aquatic mob, mapped to the glowing MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case GOAT:
            // A mountain climber/jumper, mapped to the bounding MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case HOGLIN:
            // Already a Nether mob, keep it
            yield EntityType.HOGLIN;
        case HORSE:
            // A rideable animal, mapped to the rideable STRIDER
            yield EntityType.STRIDER;
        case HUSK:
            // A desert ZOMBIE, mapped to the ZOMBIE_VILLAGER's Nether equivalent
            yield EntityType.ZOMBIFIED_PIGLIN;
        case ILLUSIONER:
            // A ranged illager, mapped to the ranged PIGLIN
            yield EntityType.PIGLIN;
        case IRON_GOLEM:
            // A defensive golem, mapped to the defensive WITHER_SKELETON (Fortress guardian)
            yield EntityType.WITHER_SKELETON;
        case LLAMA:
            // A pack animal, mapped to the rideable STRIDER
            yield EntityType.STRIDER;
        case MAGMA_CUBE:
            // Already a Nether mob, keep it
            yield EntityType.MAGMA_CUBE;
        case MOOSHROOM:
            // A variant of COW, mapped to the HOGLIN
            yield EntityType.HOGLIN;
        case MULE:
            // A pack animal, mapped to the rideable STRIDER
            yield EntityType.STRIDER;
        case OCELOT:
            // A jungle cat, mapped to the aggressive PIGLIN
            yield EntityType.PIGLIN;
        case PANDA:
            // A large, rare Overworld mob, mapped to the PIGLIN_BRUTE
            yield EntityType.PIGLIN_BRUTE;
        case PARROT:
            // A small, flying pet, mapped to the flying, hostile GHAST
            yield EntityType.GHAST;
        case PHANTOM:
            // A flying undead mob, mapped to the flying, hostile GHAST
            yield EntityType.GHAST;
        case PIG:
            // A passive ground mob, naturally mapped to the ZOMBIFIED_PIGLIN
            yield EntityType.ZOMBIFIED_PIGLIN;
        case PIGLIN:
            // Already a Nether mob, keep it
            yield EntityType.PIGLIN;
        case PIGLIN_BRUTE:
            // Already a Nether mob, keep it
            yield EntityType.PIGLIN_BRUTE;
        case PILLAGER:
            // A ranged illager, mapped to the ranged PIGLIN
            yield EntityType.PIGLIN;
        case POLAR_BEAR:
            // A large, aggressive, cold-climate mob, mapped to the HOGLIN
            yield EntityType.HOGLIN;
        case PUFFERFISH:
            // A small, poisonous aquatic mob, mapped to the small MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case RABBIT:
            // A small, ground-dwelling mob, mapped to the ZOMBIFIED_PIGLIN
            yield EntityType.ZOMBIFIED_PIGLIN;
        case RAVAGER:
            // A large, powerful illager beast, mapped to the HOGLIN
            yield EntityType.HOGLIN;
        case SHEEP:
            // A wool-producing passive mob, mapped to the ZOMBIFIED_PIGLIN
            yield EntityType.ZOMBIFIED_PIGLIN;
        case SHULKER:
            // Already a mob from a specific structure, can default or use a powerful Nether mob
            yield EntityType.WITHER_SKELETON;
        case SILVERFISH:
            // A small stone-dwelling mob, mapped to the small MAGMA_CUBE
            yield EntityType.MAGMA_CUBE;
        case SKELETON:
            // An undead archer, mapped to the WITHER_SKELETON (Nether archer analog)
            yield EntityType.WITHER_SKELETON;
        case SKELETON_HORSE:
            // An undead rideable animal, mapped to the STRIDER
            yield EntityType.STRIDER;
        case SLIME:
            // A bouncy mob, mapped to the MAGMA_CUBE (Nether slime)
            yield EntityType.MAGMA_CUBE;
        case SNIFFER:
            // A large, ancient Overworld mob, mapped to the PIGLIN_BRUTE
            yield EntityType.PIGLIN_BRUTE;
        case SNOW_GOLEM:
            // A ranged, defensive golem, mapped to the ranged BLAZE
            yield EntityType.BLAZE;
        case SPIDER:
            yield EntityType.CAVE_SPIDER;
        case SQUID:
            // An aquatic mob, mapped to the fiery BLAZE
            yield EntityType.BLAZE;
        case STRAY:
            // A frozen SKELETON, mapped to the WITHER_SKELETON
            yield EntityType.WITHER_SKELETON;
        case STRIDER:
            // Already a Nether mob, keep it
            yield EntityType.STRIDER;
        case TURTLE:
            // A slow, armored reptile, mapped to the HOGLIN
            yield EntityType.HOGLIN;
        case VEX:
            // A small, flying summon, mapped to the BLAZE
            yield EntityType.BLAZE;
        case VILLAGER:
            // A passive humanoid, mapped to the PIGLIN (Nether equivalent of society/village)
            yield EntityType.PIGLIN;
        case VINDICATOR:
            // A melee illager, mapped to the PIGLIN_BRUTE
            yield EntityType.PIGLIN_BRUTE;
        case WANDERING_TRADER:
            // A traveling Overworld trader, mapped to the PIGLIN
            yield EntityType.PIGLIN;
        case WARDEN:
            // A deep, powerful mob, mapped to the WITHER
            yield EntityType.WITHER;
        case WITCH:
            // A ranged spellcaster, mapped to the ranged BLAZE
            yield EntityType.BLAZE;
        case WITHER:
            // Already a Nether-related boss, keep it
            yield EntityType.WITHER;
        case WITHER_SKELETON:
            // Already a Nether mob, keep it
            yield EntityType.WITHER_SKELETON;
        case WOLF:
            // A ground-based predator/companion, mapped to the aggressive HOGLIN
            yield EntityType.HOGLIN;
        case ZOGLIN:
            // Already a Nether mob, keep it
            yield EntityType.ZOGLIN;
        case ZOMBIE:
            // A common undead, mapped to the ZOMBIFIED_PIGLIN (Nether's common undead)
            yield EntityType.ZOMBIFIED_PIGLIN;
        case ZOMBIE_HORSE:
            // An undead rideable animal, mapped to the STRIDER
            yield EntityType.STRIDER;
        case ZOMBIE_VILLAGER:
            // An undead VILLAGER, mapped to the ZOMBIFIED_PIGLIN
            yield EntityType.ZOMBIFIED_PIGLIN;
        case ZOMBIFIED_PIGLIN:
            // Already a Nether mob, keep it
            yield EntityType.ZOMBIFIED_PIGLIN;
        case HAPPY_GHAST:
            // Happy Ghasts exist in both dimensions
            yield EntityType.HAPPY_GHAST;
        case MINECART:
            yield EntityType.MINECART;
        default:
            // Default for any remaining non-mob entities (projectiles, items) or unknowns
            yield null;
        };
    }
}
