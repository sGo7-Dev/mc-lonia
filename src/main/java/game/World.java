package game;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<String, Chunk> chunks;
    private final PerlinNoise noise;
    private final int seaLevel;

    public World() {
        this.chunks = new HashMap<>();
        this.noise = new PerlinNoise(42);
        this.seaLevel = 8;
        GameLogger.log("Mundo creado");
    }

    public Chunk generateChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (chunks.containsKey(key)) {
            return chunks.get(key);
        }

        GameLogger.log("Generando chunk [" + chunkX + "," + chunkZ + "]...");

        Chunk chunk = new Chunk(chunkX, chunkZ);

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int wx = chunkX * Chunk.SIZE + x;
                int wz = chunkZ * Chunk.SIZE + z;

                double noiseValue = noise.noise(wx * 0.05, wz * 0.05, 0);
                int height = (int) (noiseValue * 8) + 16;

                for (int y = 0; y < Chunk.SIZE; y++) {
                    if (y < height - 2) {
                        chunk.setBlock(x, y, z, BlockType.STONE);
                    } else if (y < height) {
                        chunk.setBlock(x, y, z, BlockType.DIRT);
                    } else if (y == height) {
                        chunk.setBlock(x, y, z, BlockType.GRASS);
                    } else if (y > seaLevel && y <= height) {
                        chunk.setBlock(x, y, z, BlockType.DIRT);
                    } else {
                        chunk.setBlock(x, y, z, BlockType.AIR);
                    }
                }
            }
        }

        chunks.put(key, chunk);
        GameLogger.log("Chunk [" + chunkX + "," + chunkZ + "] generado");
        return chunk;
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        return chunks.get(key);
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public Block getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = generateChunk(chunkX, chunkZ);
        }
        int localX = x - chunkX * Chunk.SIZE;
        int localZ = z - chunkZ * Chunk.SIZE;
        return chunk.getBlock(localX, y, localZ);
    }
}
