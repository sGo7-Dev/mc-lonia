package game;

public class Block {
    private final BlockType type;
    private final int x;
    private final int y;
    private final int z;

    public Block(BlockType type, int x, int y, int z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isAir() {
        return type == BlockType.AIR;
    }
}
