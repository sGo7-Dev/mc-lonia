package game;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_image_free;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL11.*;

public class TextureAtlas {
    private final int textureId;
    private final int width;
    private final int height;

    private final Vector2i[] uvCoords;

    public TextureAtlas() {
        int[] tex = {-1};
        int[] w = {0};
        int[] h = {0};

        tex[0] = loadTexture("/assets/textures/testgrafics_texture.png", w, h);
        if (tex[0] == -1) {
            GameLogger.log("Usando textura faltante como respaldo.");
            tex[0] = loadTexture("/assets/textures/missing_texture.png", w, h);
        }
        if (tex[0] == -1) {
            tex[0] = createFallbackTexture();
            w[0] = 16;
            h[0] = 16;
            GameLogger.log("Usando textura de fallback generada.");
        }

        this.textureId = tex[0];
        this.width = w[0] == 0 ? 16 : w[0];
        this.height = h[0] == 0 ? 16 : h[0];

        this.uvCoords = new Vector2i[BlockType.values().length];
        uvCoords[BlockType.AIR.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.GRASS.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.DIRT.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.STONE.ordinal()] = new Vector2i(0, 0);

        GameLogger.log("TextureAtlas cargado: " + width + "x" + height);
    }

    private int loadTexture(String path, int[] w, int[] h) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                GameLogger.log("No se encontro " + path);
                return -1;
            }
            byte[] data = is.readAllBytes();
            ByteBuffer buf = MemoryUtil.memAlloc(data.length + 1);
            buf.put(data).flip();
            buf.put((byte) 0);
            buf.flip();
            try {
                IntBuffer width = IntBuffer.allocate(1);
                IntBuffer height = IntBuffer.allocate(1);
                IntBuffer comp = IntBuffer.allocate(1);
                ByteBuffer img = stbi_load(buf, width, height, comp, 4);
                if (img == null) {
                    GameLogger.log("stbi_load fallo para " + path);
                    return -1;
                }
                int tex = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, tex);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                        0, GL_RGBA, GL_UNSIGNED_BYTE, img);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                stbi_image_free(img);
                w[0] = width.get(0);
                h[0] = height.get(0);
                GameLogger.log("Textura cargada: " + path + " (" + width.get(0) + "x" + height.get(0) + ")");
                return tex;
            } finally {
                MemoryUtil.memFree(buf);
            }
        } catch (IOException e) {
            GameLogger.logError("Error cargando " + path, e);
            return -1;
        }
    }

    private int createFallbackTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        int[] pixels = {0xFFFF00FF};
        ByteBuffer buf = MemoryUtil.memAlloc(4);
        buf.putInt(pixels[0]);
        buf.flip();
        try {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        } finally {
            MemoryUtil.memFree(buf);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        return tex;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Vector2i getUV(BlockType type) {
        return uvCoords[type.ordinal()];
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }
}
