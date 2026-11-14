package org.mclonia.game;

/**
 * Tipos de bloques soportados por el mundo. separadon aun no hay atlas
 * Cada tipo tiene un identificador y un índice de textura.
 */
public enum BlockType {
    /** Aire (sin bloque). */
    AIR(0, -1),
    /** Hierba en la parte superior de un bloque de tierra. */
    GRASS(1, 0),
    /** Tierra. */
    DIRT(2, 1),
    /** Piedra. */
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