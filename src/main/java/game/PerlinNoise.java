package game;

import java.util.Random;

public class PerlinNoise {
    private final int[] permutation;

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        permutation = new int[256];
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
    }

    private int hash(int x, int y, int z) {
        int h = permutation[(x + permutation[(y + permutation[z]) % 256]) % 256];
        return h;
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int A = permutation[X] + Y;
        int AA = (permutation[A & 255] + Z) & 255;
        int AB = (permutation[(A + 1) & 255] + Z) & 255;
        int B = permutation[(X + 1) & 255] + Y;
        int BA = (permutation[B & 255] + Z) & 255;
        int BB = (permutation[(B + 1) & 255] + Z) & 255;

        double result = lerp(
            lerp(
                lerp(grad(permutation[AA], x, y, z),
                     grad(permutation[BA], x - 1, y, z), u),
                lerp(grad(permutation[AB], x, y - 1, z),
                     grad(permutation[BB], x - 1, y - 1, z), u),
                v),
            lerp(
                lerp(grad(permutation[(AA + 1) & 255], x, y, z - 1),
                     grad(permutation[(BA + 1) & 255], x - 1, y, z - 1), u),
                lerp(grad(permutation[(AB + 1) & 255], x, y - 1, z - 1),
                     grad(permutation[(BB + 1) & 255], x - 1, y - 1, z - 1), u),
                v),
            w);

        return (result + 1) / 2;
    }
}
