package world.bentobox.stranger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.stranger.border.BorderType;
import world.bentobox.stranger.border.PerPlayerBorderProxy;
import world.bentobox.stranger.database.NetherChunksMade;

class StrangerRealmsTest extends RanksManagerTestSetup {
    private static File jFile;
    private AbstractDatabaseHandler<NetherChunksMade> h;
    private StrangerRealms addon;
    @Mock
    private AddonsManager am;
    @Mock
    private CommandsManager cm;
    private Settings settings;

    @BeforeAll
    public static void beforeClass() throws IOException {
        deleteAll(new File("config.yml"));
        deleteAll(new File("addon.jar"));
        // Make the addon jar
        jFile = new File("addon.jar");
        // Copy over config file from src folder
        Path fromPath = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(fromPath, path);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry entry = new JarEntry(path.toString());
                tempJarOutputStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // Flags Manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        // Commands Manager
        when(plugin.getCommandsManager()).thenReturn(cm);
         
        // Addon
        addon = new StrangerRealms();
        File dataFolder = new File("addons/StrangerRealms");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "StrangerRealms", "1.3").description("test").authors("tastybento").build();
        addon.setDescription(desc);

        
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getAddonByName("Border")).thenReturn(Optional.of(addon));
        when(am.getAddonByName("invSwitcher")).thenReturn(Optional.of(addon));
        
        // Get started
        addon.onLoad();
        addon.createWorlds();
        addon.onEnable();

    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @AfterAll
    public static void cleanUp() throws Exception {
        deleteAll(new File("addon.jar"));
        deleteAll(new File("addons"));
        deleteAll(new File("config.yml"));
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#onEnable()}.
     */
    @Test
    void testOnEnable() {
        verify(plugin).logWarning("[StrangerRealms] StrangerRealms recommends the InvSwitcher addon.");
        verify(plugin).logWarning(
                "[StrangerRealms] StrangerRealms has its own Border, so do not use Border in the Crowdbound world."
                );
        verify(plugin).logError(
                "[StrangerRealms] Could not make a spawn claim. You will have to set one manually in the world."
                );
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#onReload()}.
     */
    @Test
    void testOnReload() {
        addon.onReload();
        verify(plugin).log("[StrangerRealms] Reloaded settings");
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#isUsesNewChunkGeneration()}.
     */
    @Test
    void testIsUsesNewChunkGeneration() {
        assertTrue(addon.isUsesNewChunkGeneration());
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#isFixIslandCenter()}.
     */
    @Test
    void testIsFixIslandCenter() {
        assertFalse(addon.isFixIslandCenter());
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#isEnforceEqualRanges()}.
     */
    @Test
    void testIsEnforceEqualRanges() {
        assertFalse(addon.isEnforceEqualRanges());
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getSettings()}.
     */
    @Test
    void testGetSettings() {
        Settings settings = addon.getSettings();
        assertNotNull(settings);
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getWorldSettings()}.
     */
    @Test
    void testGetWorldSettings() {
        assertNotNull(addon.getWorldSettings());
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getDefaultWorldGenerator(java.lang.String, java.lang.String)}.
     */
    @Test
    void testGetDefaultWorldGeneratorStringString() {
       assertNotNull(addon.getDefaultWorldGenerator("stranger-realms", ""));
        
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getAvailableBorderTypesView()}.
     */
    @Test
    void testGetAvailableBorderTypesView() {
        Set<BorderType> types = addon.getAvailableBorderTypesView();
        assertTrue(types.contains(BorderType.BARRIER));
        assertTrue(types.contains(BorderType.VANILLA));
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getBorderShower()}.
     */
    @Test
    void testGetBorderShower() {
       assertTrue(addon.getBorderShower() instanceof PerPlayerBorderProxy);
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getBorderSize()}.
     */
    @Test
    void testGetBorderSize() {
       assertEquals(320D, addon.getBorderSize(), 0.1D);
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#setBorderSize(int)}.
     */
    @Test
    void testSetBorderSize() {
        assertEquals(320D, addon.getBorderSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#createWarpedCompassItem()}.
     */
    @Test
    void testCreateWarpedCompassItem() {
       ItemStack item = StrangerRealms.createWarpedCompassItem();
       assertTrue(StrangerRealms.isWarpedCompass(item));
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#isWarpedCompass(org.bukkit.inventory.ItemStack)}.
     */
    @Test
    void testIsWarpedCompass() {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.ACACIA_BOAT);
        assertFalse(StrangerRealms.isWarpedCompass(item));
    }

    /**
     * Test method for {@link world.bentobox.stranger.StrangerRealms#getPlayerListener()}.
     */
    @Test
    void testGetPlayerListener() {
        assertNotNull(addon.getPlayerListener());
    }

}
