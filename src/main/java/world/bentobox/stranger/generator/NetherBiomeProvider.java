package world.bentobox.stranger.generator;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Makes the nether, different
 */
public class NetherBiomeProvider extends BiomeProvider {

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        if (y < worldInfo.getMinHeight() + 30) {
            return Biome.DEEP_DARK;
        }
        
        return worldInfo.vanillaBiomeProvider().getBiome(worldInfo, x, y, z);
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(Biome.BASALT_DELTAS, Biome.CRIMSON_FOREST, Biome.DEEP_DARK, 
                Biome.NETHER_WASTES, Biome.SOUL_SAND_VALLEY, Biome.WARPED_FOREST);
    }
}