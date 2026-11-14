package game;

import java.util.HashMap;
import java.util.Map;

public class Chunks {
    public static final int INITIAL_CHUNK_COUNT = 5;

    private final Map<String, Chunk> chunks;
    private final World world;

    public Chunks(World world) {
        this.world = world;
        this.chunks = new HashMap<>();
        GameLogger.log("Generando " + INITIAL_CHUNK_COUNT + " chunks iniciales...");
        for (int i = 0; i < INITIAL_CHUNK_COUNT; i++) {
            Chunk chunk = world.generateChunk(i - 2, 0);
            chunks.put(i + ",0", chunk);
        }
        GameLogger.log("Chunks iniciales generados: " + chunks.size());
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
    }

    public void render() {
    }
}
