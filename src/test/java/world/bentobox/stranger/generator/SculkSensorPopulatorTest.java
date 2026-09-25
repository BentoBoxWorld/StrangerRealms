package world.bentobox.stranger.generator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests for {@link SculkSensorPopulator}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SculkSensorPopulatorTest {

    private static final int FLOOR_Y = 10;

    @Mock
    private WorldInfo worldInfo;
    @Mock
    private LimitedRegion region;
    @Mock
    private Random random;

    private SculkSensorPopulator populator;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        when(worldInfo.getMinHeight()).thenReturn(0);
        // Solid sculk up to the floor, air above it
        when(region.getType(anyInt(), anyInt(), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(1) <= FLOOR_Y ? Material.SCULK : Material.AIR);
        populator = new SculkSensorPopulator();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSensorsPlacedOnExposedFloorOnly() {
        when(random.nextDouble()).thenReturn(0D);
        populator.populate(worldInfo, random, 1, 2, region);
        // Every floor column gets a sensor at the top solid block, in world coordinates
        verify(region, atLeastOnce()).setType(16, FLOOR_Y, 32, Material.SCULK_SENSOR);
        verify(region).setType(31, FLOOR_Y, 47, Material.SCULK_SENSOR);
        // Never below the surface or in the air
        verify(region, never()).setType(anyInt(), intThat(y -> y != FLOOR_Y), anyInt(), any(Material.class));
    }

    @Test
    void testNoSensorsWhenChanceMisses() {
        when(random.nextDouble()).thenReturn(SculkSensorPopulator.SENSOR_CHANCE);
        populator.populate(worldInfo, random, 0, 0, region);
        verify(region, never()).setType(anyInt(), anyInt(), anyInt(), any(Material.class));
    }
}
