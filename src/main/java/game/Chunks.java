package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Chunks {
    public static final int INITIAL_CHUNK_COUNT = 5;
    public static final int MAX_CHUNKS = 200;

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

    public void update(Player player) {
        // Calcular chunk actual del jugador
        int playerChunkX = (int) Math.floor(player.getPosition().x / Chunk.SIZE);
        int playerChunkZ = (int) Math.floor(player.getPosition().z / Chunk.SIZE);

        // Generar chunks en circulo alrededor del jugador dentro de LOAD_DISTANCE
        Set<String> chunksToKeep = new HashSet<>();
        int radius = GameConfig.LOAD_DISTANCE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Solo generar si esta dentro del radio circular
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius) {
                    continue;
                }

                int cx = playerChunkX + dx;
                int cz = playerChunkZ + dz;
                String key = cx + "," + cz;

                Chunk chunk = chunks.get(key);
                if (chunk == null) {
                    if (chunks.size() >= MAX_CHUNKS) {
                        continue;
                    }
                    chunk = world.generateChunk(cx, cz);
                    chunks.put(key, chunk);
                    GameLogger.log("Chunk generado alrededor del jugador: [" + cx + "," + cz + "]");
                }
                chunksToKeep.add(key);
            }
        }

        // Limpiar chunks muy lejanos para liberar memoria y evitar fuga
        List<String> toRemove = new ArrayList<>();
        for (String key : chunks.keySet()) {
            if (!chunksToKeep.contains(key)) {
                toRemove.add(key);
            }
        }
        for (String key : toRemove) {
            Chunk chunk = chunks.remove(key);
            ChunkMesh mesh = chunk.getMesh();
            if (mesh != null) {
                mesh.cleanup();
            }
            GameLogger.log("Chunk eliminado (lejos del jugador): " + key);
        }
    }

    public void render() {
    }
}
