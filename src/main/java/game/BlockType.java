package game;

public enum BlockType {
    AIR(0, -1),
    GRASS(1, 0),
    DIRT(2, 1),
    STONE(3, 2);

    private final int id;
    private final int textureIndex;

    BlockType(int id, int textureIndex) {
        this.id = id;
        this.textureIndex = textureIndex;
    }

    public int getId() {
        return id;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public boolean isAir() {
        return this == AIR;
    }
}
