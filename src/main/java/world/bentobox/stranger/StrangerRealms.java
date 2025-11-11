package world.bentobox.stranger;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.jdt.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand;
import world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.stranger.border.BorderType;
import world.bentobox.stranger.border.PerPlayerBorderProxy;
import world.bentobox.stranger.border.ShowBarrier;
import world.bentobox.stranger.border.ShowWorldBorder;
import world.bentobox.stranger.commands.admin.WorldBorderCommand;
import world.bentobox.stranger.commands.player.ClaimCommand;
import world.bentobox.stranger.commands.player.SpawnCommand;
import world.bentobox.stranger.commands.player.UnclaimCommand;
import world.bentobox.stranger.generator.NetherBiomeProvider;
import world.bentobox.stranger.generator.NetherChunkMaker;
import world.bentobox.stranger.generator.NetherChunks;
import world.bentobox.stranger.listeners.BorderShower;
import world.bentobox.stranger.listeners.NetherRedstoneListener;
import world.bentobox.stranger.listeners.PlayerListener;
import world.bentobox.stranger.listeners.TeamListener;

/**
 * Main Boxed class - provides a survival game inside a box
 * @author tastybento
 */
public class StrangerRealms extends GameModeAddon {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";
    
    // Define a static key for the custom item, primarily for referencing its material later if needed.
    public static final Material WARPED_COMPASS_MATERIAL = Material.COMPASS;


    // Settings
    private Settings settings;

    private final Config<Settings> configObject = new Config<>(this, Settings.class);
    private BorderShower borderShower;
    private final Set<BorderType> availableBorderTypes = EnumSet.of(BorderType.VANILLA, BorderType.BARRIER);
    private int borderSize;
    private BukkitTask task;
    private PlayerListener playerListener;
    private NetherChunkMaker netherChunkMaker;

    @Override
    public boolean isFixIslandCenter() {
        return false;
    }
    
    @Override
    public boolean isEnforceEqualRanges() {
        return false;
    }

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Create the nether chunk listener; set it up in onEnable
        netherChunkMaker = new NetherChunkMaker(this);
        
        // Register commands
        playerCommand = new DefaultPlayerCommand(this) {
            @Override
            public void setup()
            {
                super.setup();
                // Commands
                new ClaimCommand(this);
                new UnclaimCommand(this);
                new SpawnCommand(this);
            }
        };

        adminCommand = new DefaultAdminCommand(this) {
            @Override
            public void setup()
            {
                super.setup();
                // Special commands
                new WorldBorderCommand(this);
            }
        };
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("Settings could not load! Addon disabled.");
            setState(State.DISABLED);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        // Check for recommended addons
        if (this.getPlugin().getAddonsManager().getAddonByName("Border").isPresent()) {
            this.logWarning("StrangerRealms has its own Border, so do not use Border in the Crowdbound world.");
        }
        if (this.getPlugin().getAddonsManager().getAddonByName("InvSwitcher").isEmpty()) {
            this.logWarning("StrangerRealms recommends the InvSwitcher addon.");
        }
        borderShower = this.createBorder();
        playerListener = new PlayerListener(this);
        this.registerListener(playerListener);
        this.registerListener(netherChunkMaker);
        this.registerListener(new TeamListener(this));
        this.registerListener(new NetherRedstoneListener(this));
        
        // Register recipe for warped compass
        registerWarpedCompassRecipe();
        
        // Set the initial spawn
        if (getPlugin().getIslands().getSpawn(getOverWorld()).isEmpty()) {
            // Make a spawn claim
            Island spawn = getPlugin().getIslands().createIsland(getOverWorld().getSpawnLocation());
            if (spawn != null) {
                spawn.setSpawn(true);
                spawn.setSpawnPoint(Map.of(Environment.NORMAL, getOverWorld().getSpawnLocation()));
                IslandsManager.saveIsland(spawn);
            } else {
                this.logError("Could not make a spawn claim. You will have to set one manually in the world.");
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("Reloaded settings");
        }
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void createWorlds() {
        String worldName = settings.getWorldName().toLowerCase();
        // Create overworld
        islandWorld = getWorld(worldName, World.Environment.NORMAL);

        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (Bukkit.getWorld(worldName + NETHER) == null) {
                // New world, or cleared
                netherChunkMaker.clearDatabase();
            }
            netherWorld = getWorld(worldName, World.Environment.NETHER);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            endWorld = getWorld(worldName, World.Environment.THE_END);
        }
    }

    /**
     * Gets a world or generates a new world if it does not exist
     * @param worldName2 - the overworld name
     * @param env - the environment
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env) {
        // Set world name
        worldName2 = env == World.Environment.NETHER ? worldName2 + NETHER : worldName2;
        worldName2 = env == World.Environment.THE_END ? worldName2 + THE_END : worldName2;
        World w = WorldCreator
                .name(worldName2)
                .environment(env)
                .generator(env == Environment.NETHER ? new NetherChunks() : null)
                .biomeProvider(env == Environment.NETHER ? new NetherBiomeProvider() : null)
                .seed(this.getSettings().getSeed())
                .createWorld();
        // Set spawn rates
        if (w != null) {
            setSpawnRates(w);
        }
        return w;

    }

    private void setSpawnRates(World w) {
        if (getSettings().getSpawnLimitMonsters() > 0) {
            w.setSpawnLimit(SpawnCategory.MONSTER, getSettings().getSpawnLimitMonsters());
        }
        if (getSettings().getSpawnLimitAmbient() > 0) {
            w.setSpawnLimit(SpawnCategory.AMBIENT, getSettings().getSpawnLimitAmbient());
        }
        if (getSettings().getSpawnLimitAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.ANIMAL, getSettings().getSpawnLimitAnimals());
        }
        if (getSettings().getSpawnLimitWaterAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.WATER_ANIMAL, getSettings().getSpawnLimitWaterAnimals());
        }
        if (getSettings().getTicksPerAnimalSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.ANIMAL, getSettings().getTicksPerAnimalSpawns());
        }
        if (getSettings().getTicksPerMonsterSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.MONSTER, getSettings().getTicksPerMonsterSpawns());
        }
    }

    @Override
    public WorldSettings getWorldSettings() {
        return getSettings();
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return Bukkit.getWorld(worldName).getGenerator();
    }

    @Override
    public void saveWorldSettings() {
        if (settings != null) {
            configObject.saveConfigObject(settings);
        }
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.Addon#allLoaded()
     */
    @Override
    public void allLoaded() {
        // Save settings. This will occur after all addons have loaded
        this.saveWorldSettings();
    }

    @Override
    public boolean isUsesNewChunkGeneration() {
        return true;
    }

    public Set<BorderType> getAvailableBorderTypesView() {
        return Collections.unmodifiableSet(availableBorderTypes);
    }

    private BorderShower createBorder() {
        BorderShower customBorder = new ShowBarrier(this);
        BorderShower wbapiBorder = new ShowWorldBorder(this);
        return new PerPlayerBorderProxy(this, customBorder, wbapiBorder);
    }

    public BorderShower getBorderShower() {
        return borderShower;
    }

    /**
     * Get the size of the general world border, which is determined by the number of users.
     * If the new size is less than the current size, e.g., players have left, then the border size is
     * gradually reduced over time.
     * @return border size
     */
    public double getBorderSize() {
        int newBorderSize = getSettings().isManualBorderSize() ? borderSize :
                Math.max(getSettings().getBarrierIncreaseBlocks(), (this.getSettings().getBarrierIncreaseBlocks() * Bukkit.getServer().getOnlinePlayers().size()));
         if (newBorderSize < borderSize) {
             // End any current task to replace it
             task.cancel();
             // Trigger gradual reduction of border
           task =  Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                if (borderSize > newBorderSize) {
                    borderSize--;
                    // Update the border for any online players
                    Bukkit.getOnlinePlayers().stream().filter(p -> inWorld(p.getWorld())).forEach(borderShower::showBorder);
                } else {
                    // We are done
                    task.cancel();
                }
            }, this.getSettings().getBarrierReductionSpeed() * 20L, this.getSettings().getBarrierReductionSpeed() * 20L);
        } else {
            borderSize = newBorderSize;
        }
        return borderSize;
    }

    /**
     * @param borderSize the borderSize to set
     */
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
    
    /**
     * Cancels any active border reduction task
     */
    public void cancelBorderTask() {
        this.task.cancel();
    }
    
    /**
     * Creates a new ItemStack representing the Warped Compass with all its custom metadata.
     * This method is used both for the recipe result and for checking items.
     * @return The custom Warped Compass ItemStack.
     */
    public static ItemStack createWarpedCompassItem() {
        ItemStack warpedCompass = new ItemStack(WARPED_COMPASS_MATERIAL);
        ItemMeta meta = warpedCompass.getItemMeta();

        // Set the custom display name using the Adventure API
        Component displayName = Component.text("Warped Compass", NamedTextColor.AQUA)
                                         .decorate(TextDecoration.BOLD);
        meta.displayName(displayName);

        // Set the lore/tooltip using the Adventure API
        List<Component> lore = Arrays.asList(
            Component.text("Hold fast to the needle when the realm is stale.", NamedTextColor.GRAY),
            Component.text("A single spark is all it takes to refresh the flame.", NamedTextColor.GRAY),
            Component.empty(), // Represents an empty line
            Component.text("Consumed upon entry to re-thread the Nether.", NamedTextColor.RED)
        );
        meta.lore(lore);
        warpedCompass.setItemMeta(meta);
        return warpedCompass;
    }
    
    /**
     * Checks if the given ItemStack is a Warped Compass based on its material and metadata.
     * @param item The ItemStack to check.
     * @return true if the item is a Warped Compass, false otherwise.
     */
    public static boolean isWarpedCompass(ItemStack item) {
        if (item == null || item.getType() != WARPED_COMPASS_MATERIAL) {
            return false;
        }

        // Use ItemMeta.equals() to check all custom metadata (name, lore, etc.)
        // We compare the item's meta against the meta of a freshly created reference item.
        ItemMeta referenceMeta = createWarpedCompassItem().getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        
        return itemMeta.equals(referenceMeta);
    }

    private void registerWarpedCompassRecipe() {
        // --- 1. Define the Resulting Custom Item: Warped Compass ---
        // Now uses the new static helper method for consistency.
        ItemStack warpedCompass = createWarpedCompassItem();

        // --- 2. Create the NamespacedKey and ShapedRecipe ---
        // A NamespacedKey is required for the recipe to be uniquely identified.
        NamespacedKey key = new NamespacedKey(this.getPlugin(), "warped_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, warpedCompass);

        // --- 3. Define the Recipe Shape ---        
        recipe.shape(
            "COC",
            "FRF",
            "COC"
        );
        
        // --- 4. Define the Ingredients ---
        // O = Obsidian
        // C = Crying Obsidian
        // F = Warped Fungus
        // L = Recovery Compass
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('C', Material.CRYING_OBSIDIAN);
        recipe.setIngredient('F', Material.WARPED_FUNGUS);
        recipe.setIngredient('R', Material.RECOVERY_COMPASS);


        // --- 5. Add the Recipe to the Server ---
        getServer().addRecipe(recipe);
    }

    /**
     * @return the playerListener
     */
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
}
