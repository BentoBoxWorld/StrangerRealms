package world.bentobox.stranger.generator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Make the nether have Deep Dark
 */
public class NetherChunks extends ChunkGenerator {

    /**
     * Custom noise generator for caves.
     */
    private CustomCaveGenerator caveGenerator;

    /**
     * The noise value cutoff. Any noise value *below* this
     * will be carved out as a cave (air).
     * - Smaller value (e.g., 0.07) = Rarer, thinner caves.
     * - Larger value (e.g., 0.12) = More common, wider caves.
     */
    private static final double CAVE_THRESHOLD = 0.4;

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        // Initialize the cave generator if this is the first run
        if (this.caveGenerator == null) {
            this.caveGenerator = new CustomCaveGenerator(worldInfo.getSeed());
        }

        // Define the Y-range for cave generation
        final int minCaveY = worldInfo.getMinHeight() + 7;
        final int maxCaveY = minCaveY + 30;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                // Calculate absolute world X and Z once per column
                final int worldX = chunkX * 16 + x;
                final int worldZ = chunkZ * 16 + z;
                for (int y = minCaveY; y < maxCaveY; y++) {
                    // Check the noise value
                    final int worldY = y;
                    double noise = this.caveGenerator.noise(worldX, worldY, worldZ);

                    if (noise < CAVE_THRESHOLD + 0.1) {
                        // Coat with Sculk
                        chunkData.setBlock(x, y, z, Material.SCULK);
                    }
                    if (noise < CAVE_THRESHOLD) {
                        // Noise value is low enough, carve a cave block
                        chunkData.setBlock(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        //  Define the Y-range
        final int minY = worldInfo.getMinHeight() + 7;
        final int maxY = minY + 30;

        for (int y = minY; y < maxY;  y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockData block = chunkData.getBlockData(x, y, z);
                    BlockData upBlock = chunkData.getBlockData(x, y + 1, z);
                    // Ceiling blocks
                    if (upBlock.getMaterial() != Material.AIR && block.getMaterial() == Material.AIR) {
                        if (random.nextDouble() < 0.1) {
                            chunkData.setBlock(x, y, z, Material.SCULK_VEIN);
                        }
                        continue;
                    }
                    if (block.getMaterial() != Material.AIR && upBlock.getMaterial() == Material.AIR) {
                        if (random.nextDouble() < 0.1) {
                            chunkData.setBlock(x, y, z, Material.SCULK_SENSOR);
                        } else  if (random.nextDouble() < 0.2) {
                            chunkData.setBlock(x, y, z, Material.SCULK_VEIN);
                        }
                    }
                }
            }
        }
    }
}
