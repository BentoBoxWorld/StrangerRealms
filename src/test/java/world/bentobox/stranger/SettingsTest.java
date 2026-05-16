package world.bentobox.stranger;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.stranger.border.BorderType;

class SettingsTest {
    
    private Settings s;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        s = new Settings();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getFriendlyName()}.
     */
    @Test
    void testGetFriendlyName() {
        assertEquals("StrangerRealms", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getWorldName()}.
     */
    @Test
    void testGetWorldName() {
        assertEquals("stranger-world", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDifficulty()}.
     */
    @Test
    void testGetDifficulty() {
        assertEquals(Difficulty.NORMAL, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandDistance()}.
     */
    @Test
    void testGetIslandDistance() {
        assertEquals(32, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandProtectionRange()}.
     */
    @Test
    void testGetIslandProtectionRange() {
        assertEquals(32, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandStartX()}.
     */
    @Test
    void testGetIslandStartX() {
        assertEquals(0, s.getIslandStartX());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandStartZ()}.
     */
    @Test
    void testGetIslandStartZ() {
        assertEquals(0, s.getIslandStartZ());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandXOffset()}.
     */
    @Test
    void testGetIslandXOffset() {
        assertEquals(0, s.getIslandXOffset());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandZOffset()}.
     */
    @Test
    void testGetIslandZOffset() {
        assertEquals(0, s.getIslandZOffset());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIslandHeight()}.
     */
    @Test
    void testGetIslandHeight() {
        assertEquals(-64, s.getIslandHeight());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isUseOwnGenerator()}.
     */
    @Test
    void testIsUseOwnGenerator() {
        assertFalse(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSeaHeight()}.
     */
    @Test
    void testGetSeaHeight() {
        assertEquals(0, s.getSeaHeight());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMaxIslands()}.
     */
    @Test
    void testGetMaxIslands() {
        assertEquals(-1, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDefaultGameMode()}.
     */
    @Test
    void testGetDefaultGameMode() {
        assertEquals(GameMode.SURVIVAL, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isNetherGenerate()}.
     */
    @Test
    void testIsNetherGenerate() {
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isNetherIslands()}.
     */
    @Test
    void testIsNetherIslands() {
        assertTrue(s.isNetherIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getNetherSpawnRadius()}.
     */
    @Test
    void testGetNetherSpawnRadius() {
        assertEquals(32, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isEndGenerate()}.
     */
    @Test
    void testIsEndGenerate() {
        assertTrue(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isEndIslands()}.
     */
    @Test
    void testIsEndIslands() {
        assertFalse(s.isEndIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isDragonSpawn()}.
     */
    @Test
    void testIsDragonSpawn() {
        assertFalse(s.isDragonSpawn());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getRemoveMobsWhitelist()}.
     */
    @Test
    void testGetRemoveMobsWhitelist() {
        assertTrue(s.getRemoveMobsWhitelist().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getWorldFlags()}.
     */
    @Test
    void testGetWorldFlags() {
        assertTrue(s.getWorldFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDefaultIslandFlags()}.
     */
    @Test
    void testGetDefaultIslandFlags() {
        assertTrue(s.getDefaultIslandFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDefaultIslandSettings()}.
     */
    @Test
    void testGetDefaultIslandSettings() {
        assertTrue(s.getDefaultIslandSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getHiddenFlags()}.
     */
    @Test
    void testGetHiddenFlags() {
        assertTrue(s.getHiddenFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getVisitorBannedCommands()}.
     */
    @Test
    void testGetVisitorBannedCommands() {
        assertTrue(s.getVisitorBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getFallingBannedCommands()}.
     */
    @Test
    void testGetFallingBannedCommands() {
        assertTrue(s.getFallingBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMaxTeamSize()}.
     */
    @Test
    void testGetMaxTeamSize() {
        assertEquals(4, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMaxHomes()}.
     */
    @Test
    void testGetMaxHomes() {
        assertEquals(5, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getResetLimit()}.
     */
    @Test
    void testGetResetLimit() {
        assertEquals(-1, s.getResetLimit());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isLeaversLoseReset()}.
     */
    @Test
    void testIsLeaversLoseReset() {
        assertFalse(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isKickedKeepInventory()}.
     */
    @Test
    void testIsKickedKeepInventory() {
        assertTrue(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isCreateIslandOnFirstLoginEnabled()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginEnabled() {
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getCreateIslandOnFirstLoginDelay()}.
     */
    @Test
    void testGetCreateIslandOnFirstLoginDelay() {
        assertEquals(0, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isCreateIslandOnFirstLoginAbortOnLogout()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginAbortOnLogout() {
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetMoney()}.
     */
    @Test
    void testIsOnJoinResetMoney() {
        assertFalse(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetInventory()}.
     */
    @Test
    void testIsOnJoinResetInventory() {
        assertFalse(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetEnderChest()}.
     */
    @Test
    void testIsOnJoinResetEnderChest() {
        assertFalse(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetMoney()}.
     */
    @Test
    void testIsOnLeaveResetMoney() {
        assertFalse(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetInventory()}.
     */
    @Test
    void testIsOnLeaveResetInventory() {
        assertFalse(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetEnderChest()}.
     */
    @Test
    void testIsOnLeaveResetEnderChest() {
        assertFalse(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isDeathsCounted()}.
     */
    @Test
    void testIsDeathsCounted() {
        assertFalse(s.isDeathsCounted());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isAllowSetHomeInNether()}.
     */
    @Test
    void testIsAllowSetHomeInNether() {
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isAllowSetHomeInTheEnd()}.
     */
    @Test
    void testIsAllowSetHomeInTheEnd() {
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isRequireConfirmationToSetHomeInNether()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInNether() {
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isRequireConfirmationToSetHomeInTheEnd()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInTheEnd() {
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDeathsMax()}.
     */
    @Test
    void testGetDeathsMax() {
        assertEquals(0, s.getDeathsMax());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isTeamJoinDeathReset()}.
     */
    @Test
    void testIsTeamJoinDeathReset() {
        assertFalse(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getGeoLimitSettings()}.
     */
    @Test
    void testGetGeoLimitSettings() {
        assertTrue(s.getGeoLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getIvSettings()}.
     */
    @Test
    void testGetIvSettings() {
        assertTrue(s.getIvSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getResetEpoch()}.
     */
    @Test
    void testGetResetEpoch() {
        assertEquals(0L, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setFriendlyName(java.lang.String)}.
     */
    @Test
    void testSetFriendlyName() {
        s.setFriendlyName("X");
        assertEquals("X", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setWorldName(java.lang.String)}.
     */
    @Test
    void testSetWorldName() {
        s.setWorldName("world2");
        assertEquals("world2", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDifficulty(org.bukkit.Difficulty)}.
     */
    @Test
    void testSetDifficulty() {
        s.setDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setIslandDistance(int)}.
     */
    @Test
    void testSetIslandDistance() {
        s.setIslandDistance(10);
        assertEquals(10, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setIslandProtectionRange(int)}.
     */
    @Test
    void testSetIslandProtectionRange() {
        s.setIslandProtectionRange(15);
        assertEquals(15, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMaxIslands(int)}.
     */
    @Test
    void testSetMaxIslands() {
        s.setMaxIslands(42);
        assertEquals(42, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setNetherGenerate(boolean)}.
     */
    @Test
    void testSetNetherGenerate() {
        s.setNetherGenerate(false);
        assertFalse(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setEndGenerate(boolean)}.
     */
    @Test
    void testSetEndGenerate() {
        s.setEndGenerate(false);
        assertFalse(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setRemoveMobsWhitelist(java.util.Set)}.
     */
    @Test
    void testSetRemoveMobsWhitelist() {
        Set<EntityType> set = new HashSet<>();
        set.add(EntityType.ZOMBIE);
        s.setRemoveMobsWhitelist(set);
        assertEquals(set, s.getRemoveMobsWhitelist());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setWorldFlags(java.util.Map)}.
     */
    @Test
    void testSetWorldFlags() {
        Map<String, Boolean> m = new HashMap<>();
        m.put("flag", true);
        s.setWorldFlags(m);
        assertEquals(m, s.getWorldFlags());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDefaultIslandFlags(java.util.Map)}.
     */
    @Test
    void testSetDefaultIslandFlags() {
        Map<world.bentobox.bentobox.api.flags.Flag,Integer> m = new HashMap<>();
        s.setDefaultIslandFlags(m);
        assertEquals(m, s.getDefaultIslandFlags());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDefaultIslandSettings(java.util.Map)}.
     */
    @Test
    void testSetDefaultIslandSettings() {
        Map<world.bentobox.bentobox.api.flags.Flag,Integer> m = new HashMap<>();
        s.setDefaultIslandSettings(m);
        assertEquals(m, s.getDefaultIslandSettings());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setHiddenFlags(java.util.List)}.
     */
    @Test
    void testSetHiddenFlags() {
        List<String> l = Arrays.asList("a","b");
        s.setHiddenFlags(l);
        assertEquals(l, s.getHiddenFlags());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setVisitorBannedCommands(java.util.List)}.
     */
    @Test
    void testSetVisitorBannedCommands() {
        List<String> l = Arrays.asList("cmd");
        s.setVisitorBannedCommands(l);
        assertEquals(l, s.getVisitorBannedCommands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMaxTeamSize(int)}.
     */
    @Test
    void testSetMaxTeamSize() {
        s.setMaxTeamSize(8);
        assertEquals(8, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMaxHomes(int)}.
     */
    @Test
    void testSetMaxHomes() {
        s.setMaxHomes(7);
        assertEquals(7, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setIvSettings(java.util.List)}.
     */
    @Test
    void testSetIvSettings() {
        List<String> l = Arrays.asList("dmg");
        s.setIvSettings(l);
        assertEquals(l, s.getIvSettings());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setAllowSetHomeInNether(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInNether() {
        s.setAllowSetHomeInNether(false);
        assertFalse(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setAllowSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInTheEnd() {
        s.setAllowSetHomeInTheEnd(false);
        assertFalse(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setRequireConfirmationToSetHomeInNether(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInNether() {
        s.setRequireConfirmationToSetHomeInNether(false);
        assertFalse(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setRequireConfirmationToSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInTheEnd() {
        s.setRequireConfirmationToSetHomeInTheEnd(false);
        assertFalse(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setResetEpoch(long)}.
     */
    @Test
    void testSetResetEpoch() {
        s.setResetEpoch(12345L);
        assertEquals(12345L, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getPermissionPrefix()}.
     */
    @Test
    void testGetPermissionPrefix() {
        assertEquals("stranger", s.getPermissionPrefix());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isWaterUnsafe()}.
     */
    @Test
    void testIsWaterUnsafe() {
        assertFalse(s.isWaterUnsafe());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getBanLimit()}.
     */
    @Test
    void testGetBanLimit() {
        assertEquals(-1, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setBanLimit(int)}.
     */
    @Test
    void testSetBanLimit() {
        s.setBanLimit(10);
        assertEquals(10, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getPlayerCommandAliases()}.
     */
    @Test
    void testGetPlayerCommandAliases() {
        assertEquals("st strange", s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setPlayerCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetPlayerCommandAliases() {
        s.setPlayerCommandAliases("p1 p2");
        assertEquals("p1 p2", s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getAdminCommandAliases()}.
     */
    @Test
    void testGetAdminCommandAliases() {
        assertEquals("stranger", s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setAdminCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetAdminCommandAliases() {
        s.setAdminCommandAliases("adm");
        assertEquals("adm", s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isDeathsResetOnNewIsland()}.
     */
    @Test
    void testIsDeathsResetOnNewIsland() {
        assertFalse(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getOnJoinCommands()}.
     */
    @Test
    void testGetOnJoinCommands() {
        assertNotNull(s.getOnJoinCommands());
        assertTrue(s.getOnJoinCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setOnJoinCommands(java.util.List)}.
     */
    @Test
    void testSetOnJoinCommands() {
        List<String> cmds = Arrays.asList("[SUDO] echo hi");
        s.setOnJoinCommands(cmds);
        assertEquals(cmds, s.getOnJoinCommands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getOnLeaveCommands()}.
     */
    @Test
    void testGetOnLeaveCommands() {
        assertNotNull(s.getOnLeaveCommands());
        assertTrue(s.getOnLeaveCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setOnLeaveCommands(java.util.List)}.
     */
    @Test
    void testSetOnLeaveCommands() {
        List<String> cmds = Arrays.asList("say bye");
        s.setOnLeaveCommands(cmds);
        assertEquals(cmds, s.getOnLeaveCommands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getOnRespawnCommands()}.
     */
    @Test
    void testGetOnRespawnCommands() {
        assertNotNull(s.getOnRespawnCommands());
        assertTrue(s.getOnRespawnCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setOnRespawnCommands(java.util.List)}.
     */
    @Test
    void testSetOnRespawnCommands() {
        List<String> cmds = Arrays.asList("tp [player] 0 64 0");
        s.setOnRespawnCommands(cmds);
        assertEquals(cmds, s.getOnRespawnCommands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetHealth()}.
     */
    @Test
    void testIsOnJoinResetHealth() {
        assertFalse(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetHunger()}.
     */
    @Test
    void testIsOnJoinResetHunger() {
        assertFalse(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnJoinResetXP()}.
     */
    @Test
    void testIsOnJoinResetXP() {
        assertFalse(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetHealth()}.
     */
    @Test
    void testIsOnLeaveResetHealth() {
        assertFalse(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetHunger()}.
     */
    @Test
    void testIsOnLeaveResetHunger() {
        assertFalse(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isOnLeaveResetXP()}.
     */
    @Test
    void testIsOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isPasteMissingIslands()}.
     */
    @Test
    void testIsPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isTeleportPlayerToIslandUponIslandCreation()}.
     */
    @Test
    void testIsTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSpawnLimitMonsters()}.
     */
    @Test
    void testGetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setSpawnLimitMonsters(int)}.
     */
    @Test
    void testSetSpawnLimitMonsters() {
        s.setSpawnLimitMonsters(99);
        assertEquals(99, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSpawnLimitAnimals()}.
     */
    @Test
    void testGetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setSpawnLimitAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitAnimals() {
        s.setSpawnLimitAnimals(50);
        assertEquals(50, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSpawnLimitWaterAnimals()}.
     */
    @Test
    void testGetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setSpawnLimitWaterAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitWaterAnimals() {
        s.setSpawnLimitWaterAnimals(20);
        assertEquals(20, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSpawnLimitAmbient()}.
     */
    @Test
    void testGetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setSpawnLimitAmbient(int)}.
     */
    @Test
    void testSetSpawnLimitAmbient() {
        s.setSpawnLimitAmbient(5);
        assertEquals(5, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getTicksPerAnimalSpawns()}.
     */
    @Test
    void testGetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setTicksPerAnimalSpawns(int)}.
     */
    @Test
    void testSetTicksPerAnimalSpawns() {
        s.setTicksPerAnimalSpawns(400);
        assertEquals(400, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getTicksPerMonsterSpawns()}.
     */
    @Test
    void testGetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setTicksPerMonsterSpawns(int)}.
     */
    @Test
    void testSetTicksPerMonsterSpawns() {
        s.setTicksPerMonsterSpawns(300);
        assertEquals(300, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMaxCoopSize()}.
     */
    @Test
    void testGetMaxCoopSize() {
        assertEquals(4, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMaxCoopSize(int)}.
     */
    @Test
    void testSetMaxCoopSize() {
        s.setMaxCoopSize(6);
        assertEquals(6, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMaxTrustSize()}.
     */
    @Test
    void testGetMaxTrustSize() {
        assertEquals(4, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMaxTrustSize(int)}.
     */
    @Test
    void testSetMaxTrustSize() {
        s.setMaxTrustSize(7);
        assertEquals(7, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDefaultNewPlayerAction()}.
     */
    @Test
    void testGetDefaultNewPlayerAction() {
        assertEquals("spawn", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDefaultNewPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultNewPlayerAction() {
        s.setDefaultNewPlayerAction("create");
        assertEquals("create", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getDefaultPlayerAction()}.
     */
    @Test
    void testGetDefaultPlayerAction() {
        assertEquals("go", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDefaultPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultPlayerAction() {
        s.setDefaultPlayerAction("teleport");
        assertEquals("teleport", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMobLimitSettings()}.
     */
    @Test
    void testGetMobLimitSettings() {
        assertTrue(s.getMobLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMobLimitSettings(java.util.List)}.
     */
    @Test
    void testSetMobLimitSettings() {
        List<String> l = Arrays.asList("ZOMBIE");
        s.setMobLimitSettings(l);
        assertEquals(l, s.getMobLimitSettings());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSeed()}.
     */
    @Test
    void testGetSeed() {
        assertEquals(602103456450L, s.getSeed());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setSeed(long)}.
     */
    @Test
    void testSetSeed() {
        s.setSeed(9999L);
        assertEquals(9999L, s.getSeed());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isCheckForBlocks()}.
     */
    @Test
    void testIsCheckForBlocks() {
        assertFalse(s.isCheckForBlocks());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isMakeNetherPortals()}.
     */
    @Test
    void testIsMakeNetherPortals() {
        assertTrue(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSeedX()}.
     */
    @Test
    void testGetSeedX() {
        assertEquals(0, s.getSeedX());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getSeedZ()}.
     */
    @Test
    void testGetSeedZ() {
        assertEquals(0, s.getSeedZ());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getNetherSeedX()}.
     */
    @Test
    void testGetNetherSeedX() {
        assertEquals(0, s.getNetherSeedX());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getNetherSeedZ()}.
     */
    @Test
    void testGetNetherSeedZ() {
        assertEquals(0, s.getNetherSeedZ());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getEndSeedX()}.
     */
    @Test
    void testGetEndSeedX() {
        assertEquals(0, s.getEndSeedX());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getEndSeedZ()}.
     */
    @Test
    void testGetEndSeedZ() {
        assertEquals(0, s.getEndSeedZ());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getConcurrentIslands()}.
     */
    @Test
    void testGetConcurrentIslands() {
        assertEquals(3, s.getConcurrentIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setConcurrentIslands(int)}.
     */
    @Test
    void testSetConcurrentIslands() {
        s.setConcurrentIslands(6);
        assertEquals(6, s.getConcurrentIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isDisallowTeamMemberIslands()}.
     */
    @Test
    void testIsDisallowTeamMemberIslands() {
        assertTrue(s.isDisallowTeamMemberIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDisallowTeamMemberIslands(boolean)}.
     */
    @Test
    void testSetDisallowTeamMemberIslands() {
        s.setDisallowTeamMemberIslands(false);
        assertFalse(s.isDisallowTeamMemberIslands());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isAllowStructures()}.
     */
    @Test
    void testIsAllowStructures() {
        assertTrue(s.isAllowStructures());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setAllowStructures(boolean)}.
     */
    @Test
    void testSetAllowStructures() {
        s.setAllowStructures(false);
        assertFalse(s.isAllowStructures());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getType()}.
     */
    @Test
    void testGetType() {
        assertEquals(BorderType.VANILLA, s.getType());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isUseBarrierBlocks()}.
     */
    @Test
    void testIsUseBarrierBlocks() {
        assertFalse(s.isUseBarrierBlocks());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isShowParticles()}.
     */
    @Test
    void testIsShowParticles() {
        assertTrue(s.isShowParticles());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getBarrierReductionSpeed()}.
     */
    @Test
    void testGetBarrierReductionSpeed() {
        assertEquals(10, s.getBarrierReductionSpeed());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setBarrierReductionSpeed(int)}.
     */
    @Test
    void testSetBarrierReductionSpeed() {
        s.setBarrierReductionSpeed(5);
        assertEquals(5, s.getBarrierReductionSpeed());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getBarrierIncreaseBlocks()}.
     */
    @Test
    void testGetBarrierIncreaseBlocks() {
        assertEquals(320, s.getBarrierIncreaseBlocks());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setBarrierIncreaseBlocks(int)}.
     */
    @Test
    void testSetBarrierIncreaseBlocks() {
        s.setBarrierIncreaseBlocks(100);
        assertEquals(100, s.getBarrierIncreaseBlocks());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getMemberBonus()}.
     */
    @Test
    void testGetMemberBonus() {
        assertEquals(32, s.getMemberBonus());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setMemberBonus(int)}.
     */
    @Test
    void testSetMemberBonus() {
        s.setMemberBonus(10);
        assertEquals(10, s.getMemberBonus());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isManualBorderSize()}.
     */
    @Test
    void testIsManualBorderSize() {
        assertFalse(s.isManualBorderSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setManualBorderSize(boolean)}.
     */
    @Test
    void testSetManualBorderSize() {
        s.setManualBorderSize(true);
        assertTrue(s.isManualBorderSize());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isDisableWorldBorder()}.
     */
    @Test
    void testIsDisableWorldBorder() {
        assertFalse(s.isDisableWorldBorder());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setDisableWorldBorder(boolean)}.
     */
    @Test
    void testSetDisableWorldBorder() {
        s.setDisableWorldBorder(true);
        assertTrue(s.isDisableWorldBorder());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#isUseUpsideDown()}.
     */
    @Test
    void testIsUseUpsideDown() {
        assertTrue(s.isUseUpsideDown());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setUseUpsideDown(boolean)}.
     */
    @Test
    void testSetUseUpsideDown() {
        s.setUseUpsideDown(false);
        assertFalse(s.isUseUpsideDown());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getAttrition()}.
     */
    @Test
    void testGetAttrition() {
        assertEquals(5, s.getAttrition());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setAttrition(int)}.
     */
    @Test
    void testSetAttrition() {
        s.setAttrition(2);
        assertEquals(2, s.getAttrition());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getChestFills()}.
     */
    @Test
    void testGetChestFills() {
        assertEquals(3, s.getChestFills());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setChestFills(int)}.
     */
    @Test
    void testSetChestFills() {
        s.setChestFills(6);
        assertEquals(6, s.getChestFills());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#getRedstoneChance()}.
     */
    @Test
    void testGetRedstoneChance() {
        assertEquals(100, s.getRedstoneChance());
    }

    /**
     * Test method for {@link world.bentobox.stranger.Settings#setRedstoneChance(int)}.
     */
    @Test
    void testSetRedstoneChance() {
        s.setRedstoneChance(50);
        assertEquals(50, s.getRedstoneChance());
        s.setRedstoneChance(150);
        assertEquals(100, s.getRedstoneChance()); // clamped to 100
        s.setRedstoneChance(-10);
        assertEquals(0, s.getRedstoneChance()); // clamped to 0
    }

}
