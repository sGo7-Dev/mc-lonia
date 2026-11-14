package org.mclonia.game;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2i;

import static org.LWJGL.stb.STBImage.stbi_load;
import static org.LWJGL.stb.STBImage.stbi_image_free;

import static org.LWJGL.OpenGL.GL30.*;
import static org.LWJGL.OpenGL.GL31.*;
import static org.LWJGL.OpenGL.GL11.*;

/**
 * Atlas de texturas simple.
 * Carga las texturas del juego y permite obtener la coordenada de un bloque.
 */
public class TextureAtlas {
    private static final String MISSING_TEXTURE_PATH = "/assets/textures/missing_texture.png";
    private static final String TEST_TEXTURE_PATH = "/assets/textures/testgrafics_texture.png";

    private final int textureId;
    private final int width;
    private final int height;

    private final Vector2i[] uvCoords;

    public TextureAtlas() {
        int[] tex = {-1};
        int[] w = {0};
        int[] h = {0};

        tex[0] = loadTexture(TEST_TEXTURE_PATH, w, h);
        if (tex[0] == -1) {
            System.err.println("Usando textura faltante como respaldo.");
            tex[0] = loadTexture(MISSING_TEXTURE_PATH, w, h);
        }
        if (tex[0] == -1) {
            tex[0] = createFallbackTexture();
            w[0] = 16;
            h[0] = 16;
        }

        this.textureId = tex[0];
        this.width = w[0] == 0 ? 16 : w[0];
        this.height = h[0] == 0 ? 16 : h[0];

        this.uvCoords = new Vector2i[BlockType.values().length];
        uvCoords[BlockType.AIR.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.GRASS.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.DIRT.ordinal()] = new Vector2i(0, 0);
        uvCoords[BlockType.STONE.ordinal()] = new Vector2i(0, 0);
    }

    private int loadTexture(String path, int[] w, int[] h) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("No se encontro " + path);
                return -1;
            }
            byte[] data = is.readAllBytes();
            ByteBuffer buf = org.LWJGL.system.LWJGL.util.ByteBufferUtil
                    .ioByteBuffer(data, 0, data.length);
            IntBuffer width = IntBuffer.allocate(1);
            IntBuffer height = IntBuffer.allocate(1);
            IntBuffer comp = IntBuffer.allocate(1);
            ByteBuffer img = stbi_load(buf, width, height, comp, 4);
            if (img == null) {
                System.err.println("stbi_load fallo para " + path);
                return -1;
            }
            int tex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                    0, GL_RGBA, GL_UNSIGNED_BYTE, img);
            glParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            stbi_image_free(img);
            w[0] = width.get(0);
            h[0] = height.get(0);
            return tex;
        } catch (IOException e) {
            System.err.println("Error cargando " + path + ": " + e.getMessage());
            return -1;
        }
    }

    private int createFallbackTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        int[] pixels = {0xFFFF00FF};
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(pixels[0]);
        buf.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
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