package game;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshBuilder {

    private static final float[] CUBE_VERTICES = {
        0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
    };

    private static final int[][] FACE_QUADS = {
        {3, 2, 6, 7},
        {0, 4, 5, 1},
        {4, 5, 6, 7},
        {0, 1, 2, 3},
        {1, 5, 6, 2},
        {0, 3, 7, 4},
    };

    private static final float[][] FACE_NORMALS = {
        {0.0f, 1.0f, 0.0f},
        {0.0f, -1.0f, 0.0f},
        {0.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, -1.0f},
        {1.0f, 0.0f, 0.0f},
        {-1.0f, 0.0f, 0.0f},
    };

    private static final float[][] FACE_UVS = {
        {0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f},
        {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f},
    };

    public static MeshData buildMesh(Chunk chunk, World world, TextureAtlas atlas) {
        List<Float> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        int chunkWorldX = chunk.getWorldX();
        int chunkWorldZ = chunk.getWorldZ();

        int facesGenerated = 0;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block.isAir()) {
                        continue;
                    }

                    int worldX = chunkWorldX * Chunk.SIZE + x;
                    int worldY = y;
                    int worldZ = chunkWorldZ * Chunk.SIZE + z;

                    BlockType type = block.getType();

                    for (int faceIndex = 0; faceIndex < BlockFace.values().length; faceIndex++) {
                        BlockFace face = BlockFace.values()[faceIndex];

                        int nx = worldX + face.dx;
                        int ny = worldY + face.dy;
                        int nz = worldZ + face.dz;

                        Block neighbor = world.getBlock(nx, ny, nz);

                        if (neighbor != null && !neighbor.isAir()) {
                            continue;
                        }

                        addFace(vertexList, indexList, x, y, z, faceIndex, type, atlas);
                        facesGenerated++;
                    }
                }
            }
        }

        GameLogger.log("Chunk [" + chunkWorldX + "," + chunkWorldZ + "] mesh: " + facesGenerated + " caras generadas");

        float[] vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }

        int[] indices = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }

        return new MeshData(vertices, indices, facesGenerated);
    }

    private static void addFace(List<Float> vertexList, List<Integer> indexList,
                                int bx, int by, int bz, int faceIndex, BlockType type, TextureAtlas atlas) {
        int[] quad = FACE_QUADS[faceIndex];
        float[] normals = FACE_NORMALS[faceIndex];
        float[] uvs = FACE_UVS[faceIndex].clone();

        float[] offset = atlas.getUVOffset(type);
        float colWidth = 1.0f / 3.0f;
        for (int i = 0; i < 4; i++) {
            uvs[i * 2] = uvs[i * 2] * colWidth + offset[0];
        }

        int baseVertex = vertexList.size() / 8;

        for (int i = 0; i < 4; i++) {
            int ci = quad[i];
            float vx = CUBE_VERTICES[ci * 8 + 0] + bx;
            float vy = CUBE_VERTICES[ci * 8 + 1] + by;
            float vz = CUBE_VERTICES[ci * 8 + 2] + bz;

            vertexList.add(vx);
            vertexList.add(vy);
            vertexList.add(vz);

            vertexList.add(uvs[i * 2 + 0]);
            vertexList.add(uvs[i * 2 + 1]);

            vertexList.add(normals[0]);
            vertexList.add(normals[1]);
            vertexList.add(normals[2]);
        }

        indexList.add(baseVertex + 0);
        indexList.add(baseVertex + 1);
        indexList.add(baseVertex + 2);
        indexList.add(baseVertex + 0);
        indexList.add(baseVertex + 2);
        indexList.add(baseVertex + 3);
    }

    public static class MeshData {
        public final float[] vertices;
        public final int[] indices;
        public final int faceCount;

        public MeshData(float[] vertices, int[] indices, int faceCount) {
            this.vertices = vertices;
            this.indices = indices;
            this.faceCount = faceCount;
        }
    }
}
