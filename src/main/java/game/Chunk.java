package game;

public class Chunk {
    public static final int SIZE = 16;
    public static final int SIZE_SQ = SIZE * SIZE;

    private final int worldX;
    private final int worldZ;
    private final Block[] blocks;
    private ChunkMesh mesh;

    public Chunk(int worldX, int worldZ) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.blocks = new Block[SIZE * SIZE * SIZE];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(BlockType.AIR, 0, 0, 0);
        }
        this.mesh = null;
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
        if (mesh != null) {
            mesh.setDirty(true);
        }
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public ChunkMesh getMesh() {
        return mesh;
    }

    public void setMesh(ChunkMesh mesh) {
        this.mesh = mesh;
    }
}
