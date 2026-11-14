package org.mclonia.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de chunks.
 * Administra los chunks del mundo, generando y manteniendo un registro.
 * Por ahora genera 5 chunks iniciales.
 */
public class Chunks {
    public static final int INITIAL_CHUNK_COUNT = 5;

    private final Map<String, Chunk> chunks;
    private final World world;

    public Chunks(World world) {
        this.world = world;
        this.chunks = new HashMap<>();
        // Generar 5 chunks iniciales en línea
        for (int i = 0; i < INITIAL_CHUNK_COUNT; i++) {
            Chunk chunk = world.generateChunk(i - 2, 0);
            chunks.put(i + ",0", chunk);
        }
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        return chunks.get(key);
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public int getChunkCount() {
        return chunks.size();
    }

    public void update() {
        // Actualizar lógica de chunks (preparado para futuras características)
    }

    public void render() {
        // El renderizado se maneja en el Game principal
    }
}