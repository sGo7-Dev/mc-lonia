package org.mclonia.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Sistema del mundo.
 * Genera el terreno mediante Perlin Noise y gestiona los chunks.
 */
public class World {
    private final Map<String, Chunk> chunks;
    private final PerlinNoise noise;
    private final int seaLevel;

    public World() {
        this.chunks = new HashMap<>();
        this.noise = new PerlinNoise(42);
        this.seaLevel = 8;
    }

    /**
     * Genera un chunk en la posición dada.
     */
    public Chunk generateChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (chunks.containsKey(key)) {
            return chunks.get(key);
        }

        Chunk chunk = new Chunk(chunkX, chunkZ);

        // Generar terreno con Perlin Noise
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                // Coordenadas mundiales
                int wx = chunkX * Chunk.SIZE + x;
                int wz = chunkZ * Chunk.SIZE + z;

                // Altura del terreno usando Perlin Noise
                double noiseValue = noise.noise(wx * 0.05, wz * 0.05);
                int height = (int) (noiseValue * 8) + 16;

                // Llenar de tierra hasta la altura, piedra debajo
                for (int y = 0; y < Chunk.SIZE; y++) {
                    if (y < height - 2) {
                        chunk.setBlock(x, y, z, BlockType.STONE);
                    } else if (y < height) {
                        chunk.setBlock(x, y, z, BlockType.DIRT);
                    } else if (y == height) {
                        chunk.setBlock(x, y, z, BlockType.GRASS);
                    } else if (y > seaLevel && y <= height) {
                        // Nieve encima de la hierba en alturas altas
                        chunk.setBlock(x, y, z, BlockType.DIRT);
                    } else {
                        chunk.setBlock(x, y, z, BlockType.AIR);
                    }
                }
            }
        }

        chunks.put(key, chunk);
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

    /**
     * Obtiene el bloque en coordenadas mundiales.
     */
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