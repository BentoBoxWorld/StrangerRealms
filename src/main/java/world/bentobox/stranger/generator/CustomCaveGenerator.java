package world.bentobox.stranger.generator;

import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.Random;

/**
 * A custom noise generator for creating 3D cave systems.
 *
 * This class extends NoiseGenerator and is designed to be used within
 * a custom ChunkGenerator (like in generateNoise(), generateSurface(), or
 * generateBedrock()).
 *
 * The algorithm works by combining two 3D Simplex noise fields. A "cave"
 * is formed in areas where the values of both noise fields are close to zero.
 * This creates intersecting "sheets" of noise, resulting in tunnel-like
 * structures.
 */
public class CustomCaveGenerator extends NoiseGenerator {

    // These are our two underlying noise generators.
    // Using two different generators and combining them creates
    // more complex and "tunnel-like" patterns than a single one.
    private final NoiseGenerator noiseGen1;
    private final NoiseGenerator noiseGen2;

    /**
     * Creates a new CustomCaveGenerator with a specific seed.
     * We use the main seed to create two new Random objects,
     * ensuring our two internal generators are different but
     * still deterministic based on the world seed.
     *
     * @param seed The world seed.
     */
    public CustomCaveGenerator(long seed) {
        // Create two separate Random objects based on the seed
        // to initialize two different noise generators.
        Random rand1 = new Random(seed);
        Random rand2 = new Random(seed * 2); // Or any other mutation

        this.noiseGen1 = new SimplexNoiseGenerator(rand1);
        this.noiseGen2 = new SimplexNoiseGenerator(rand2);
    }
    
    /**
     * Creates a new CustomCaveGenerator with a Random object.
     * @param rand The Random object to use.
     */
    public CustomCaveGenerator(Random rand) {
        // We can create new Random objects from the first one to ensure
        // our generators are different.
        this.noiseGen1 = new SimplexNoiseGenerator(new Random(rand.nextLong()));
        this.noiseGen2 = new SimplexNoiseGenerator(new Random(rand.nextLong()));
    }

    /**
     * Computes the 3D "cave" noise value at the given coordinates.
     *
     * This is the core abstract method from NoiseGenerator.
     * The value returned is typically in the range [-1, 1], but this
     * implementation's "cheese" logic may exceed that slightly.
     *
     * A lower value (closer to 0) means "more likely to be a cave".
     * A higher value (closer to 1 or -1) means "more likely to be solid".
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return The 3D noise value.
     */
    @Override
    public double noise(double x, double y, double z) {
        // ---
        // 1. Define noise parameters
        // ---
        
        // "Frequency" of the caves. Smaller values = larger caves.
        double frequency = 0.04;
        
        // "Cheese" caves (large holes). Smaller values = larger holes.
        double cheeseFrequency = 0.08;

        // Apply frequency to coordinates
        double xf = x * frequency;
        double yf = y * frequency;
        double zf = z * frequency;

        // Apply "cheese" frequency
        double xc = x * cheeseFrequency;
        double yc = y * cheeseFrequency;
        double zc = z * cheeseFrequency;

        // ---
        // 2. Sample the noise
        // ---

        // Get the base noise values. These will be in the range [-1, 1].
        // We use Math.abs() to "fold" the noise. This makes the "valleys"
        // (values near 0) into sharp "creases", and the peaks (near 1 and -1)
        // into broad "plateaus". This is key to forming tunnels.
        double noiseValue1 = Math.abs(noiseGen1.noise(xf, yf, zf));
        double noiseValue2 = Math.abs(noiseGen2.noise(xf, yf, zf));

        // Get a third, lower-frequency noise value for large "cheese" chambers.
        // We don't take the absolute value here.
        double cheeseValue = noiseGen1.noise(xc, yc / 2.0, zc); // Stretch Y for flatter chambers
        
        // ---
        // 3. Combine the noise
        // ---
        
        // Find the "ridge" where both noise values are low.
        // We take the maximum of the two folded values.
        // If noiseValue1 = 0.1 (cave) and noiseValue2 = 0.8 (solid),
        // the result is 0.8 (solid).
        // If noiseValue1 = 0.1 (cave) and noiseValue2 = 0.2 (cave),
        // the result is 0.2 (cave).
        // This effectively finds the *intersection* of the two noise fields.
        double finalNoise = Math.max(noiseValue1, noiseValue2);

        // ---
        // 4. Add "Cheese" Caves (Optional but recommended)
        // ---
        
        // Now, let's use the cheeseValue to carve out larger areas.
        // We subtract the cheese value from our tunnel noise.
        // If cheeseValue is high (e.g., 0.8), it will drastically lower
        // the finalNoise, carving a large hole.
        // We scale it down (e.g., * 0.3) so it doesn't overpower the tunnels.
        // The "0.3" is a "cheese threshold" - values above this create holes.
        double cheeseModifier = (cheeseValue - 0.3) * 0.3; // Tweak these numbers!
        
        if (cheeseModifier > 0) {
            finalNoise -= cheeseModifier;
        }

        // We can clamp the final value to a [0, 1] range if we want
        // finalNoise = Math.max(0, Math.min(1, finalNoise));

        // The finalNoise value is a double.
        // In a ChunkGenerator, you would check:
        // if (customCaveGenerator.noise(x, y, z) < 0.1) {
        //    chunkData.setBlock(x, y, z, Material.AIR);
        // }
        
        return finalNoise;
    }
}