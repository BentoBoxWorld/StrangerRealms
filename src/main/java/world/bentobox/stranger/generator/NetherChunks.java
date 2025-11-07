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
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        for (int y = worldInfo.getMinHeight(); y < worldInfo.getMinHeight() + 30; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockData block = chunkData.getBlockData(x, y, z);
                    BlockData upBlock = chunkData.getBlockData(x, y + 1, z);
                    if (block.getMaterial() != Material.AIR && upBlock.getMaterial() == Material.AIR) {
                        if (random.nextDouble() < 0.2) {
                            chunkData.setBlock(x, y, z, Material.SCULK_CATALYST);
                        } else  if (random.nextDouble() < 0.6) {
                            chunkData.setBlock(x, y, z, Material.SCULK);
                        }
                        if (random.nextDouble() < 0.3) {
                            chunkData.setBlock(x, y+1, z, Material.SCULK_SENSOR);
                        }
                    }
                }
            }
        }
    }
}
