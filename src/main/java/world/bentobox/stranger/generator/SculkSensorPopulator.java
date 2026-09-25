package world.bentobox.stranger.generator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Scatters sculk sensors over the Deep Dark floor of the Upside Down.
 * <p>
 * Sensors have block entities, so they are placed by a populator rather than in
 * {@link NetherChunks#generateSurface}. Populators run after vanilla carvers and
 * decorations; from Minecraft 26.3 those steps replaced sensors placed earlier
 * (e.g. with lava), orphaning their block entities and logging a
 * ServerInternalException for each one.
 */
public class SculkSensorPopulator extends BlockPopulator {

    /**
     * Chance that an exposed floor block becomes a sculk sensor
     */
    static final double SENSOR_CHANCE = 0.1;

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ,
            @NotNull LimitedRegion region) {
        final int minY = worldInfo.getMinHeight() + 5;
        final int maxY = minY + NetherChunkMaker.NETHER_FLOOR;
        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;
        for (int y = minY; y < maxY; y++) {
            for (int x = baseX; x < baseX + 16; x++) {
                for (int z = baseZ; z < baseZ + 16; z++) {
                    if (isExposedFloor(region, x, y, z) && random.nextDouble() < SENSOR_CHANCE) {
                        region.setType(x, y, z, Material.SCULK_SENSOR);
                    }
                }
            }
        }
    }

    /**
     * @return true if the block is floor with open air above it
     */
    private boolean isExposedFloor(LimitedRegion region, int x, int y, int z) {
        Material type = region.getType(x, y, z);
        return (type.isSolid() || type == Material.SCULK_VEIN) && region.getType(x, y + 1, z) == Material.AIR;
    }
}
