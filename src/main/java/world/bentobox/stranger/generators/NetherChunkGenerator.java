package world.bentobox.stranger.generators;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import world.bentobox.stranger.StrangerRealms;

/**
 * @author tastybento
 * Generates a mirror nether world
 */
public class NetherChunkGenerator extends ChunkGenerator {

    private StrangerRealms addon;
    
    
    public NetherChunkGenerator(StrangerRealms addon) {
        super();
        this.addon = addon;
    }


    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
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
        return this.addon.getSettings().isAllowStructures();
    }
    
    @Override
    public void generateNoise(WorldInfo worldInfo, Random r, int chunkX, int chunkZ, ChunkData cd) {
        if (worldInfo.getEnvironment() != Environment.NETHER) {
            return;
        }
    }
    
}
