package org.mclonia.game;

import org.joml.Vector3i;

/**
 * Representa un chunk del mundo (16x16x16 bloques).
 * Contiene los bloques y su posición dentro del mundo.
 */
public class Chunk {
    public static final int SIZE = 16;
    public static final int SIZE_SQ = SIZE * SIZE;

    private final int worldX;
    private final int worldZ;
    private final Block[] blocks;

    public Chunk(int worldX, int worldZ) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.blocks = new Block[SIZE * SIZE * SIZE];
        // Inicializar todo como aire
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(BlockType.AIR, 0, 0, 0);
        }
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldZ() {
        return worldZ;
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return new Block(BlockType.AIR, x, y, z);
        }
        return blocks[y * SIZE_SQ + z * SIZE + x];
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return;
        }
        blocks[y * SIZE_SQ + z * SIZE + x] = new Block(type, x, y, z);
    }

    public Block[] getBlocks() {
        return blocks;
    }
}