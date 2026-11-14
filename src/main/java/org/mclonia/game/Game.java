package org.mclonia.game;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.LWJGL.glfw.GLFWCursorPosCallback;
import org.LWJGL.glfw.GLFWKeyCallback;

import static org.LWJGL.glfw.GLFW.*;
import static org.LWJGL.OpenGL.GL11.*;
import static org.LWJGL.OpenGL.GL20.*;
import static org.LWJGL.OpenGL.GL30.*;
import static org.LWJGL.OpenGL.GL31.*;

/**
 * Punto de entrada y administrador principal del juego.
 * Inicializa los sistemas, ejecuta el game loop y gestiona update/render.
 */
public class Game {
    private long window;
    private int width;
    private int height;
    private String title;

    private World world;
    private Chunks chunks;
    private Player player;
    private TextureAtlas textureAtlas;

    private boolean running;
    private float lastTime;
    private float deltaTime;

    public Game(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        glfwWindowHint.GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint.GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint.GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Inicializar sistemas
        textureAtlas = new TextureAtlas();
        world = new World();
        chunks = new Chunks(world);
        player = new Player(world);

        // Callbacks de entrada
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            public void invoke(long win, double x, double y) {
                player.setMouseLook((float) x, (float) y);
            }
        });

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            public void invoke(long win, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    glfwSetWindowShouldClose(win, true);
                }
            }
        });

        lastTime = getTime();
    }

    public void run() {
        init();
        running = true;

        while (running && !glfwWindowShouldClose(window)) {
            float currentTime = getTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            glClearColor(0.24f, 0.56f, 0.74f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            update(deltaTime);
            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        cleanup();
    }

    private void update(float deltaTime) {
        player.update(deltaTime, window);
        chunks.update();
    }

    private void render() {
        textureAtlas.bind();

        // Configurar proyección y vista
        Matrix4f projection = new Matrix4f()
            .perspective((float) Math.toRadians(70.0f), (float) width / height, 0.1f, 1000.0f);

        Vector3f lookDir = new Vector3f(
            (float) (Math.cos(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch()))),
            (float) (Math.sin(Math.toRadians(player.getPitch()))),
            (float) (-Math.sin(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch())))
        );

        Matrix4f view = new Matrix4f()
            .lookAt(player.getPosition(),
                    player.getPosition().add(lookDir, new Vector3f()),
                    new Vector3f(0, 1, 0));

        // Renderizar cada chunk
        for (Chunk chunk : chunks.getChunks().values()) {
            renderChunk(chunk, projection, view);
        }
    }

    private void renderChunk(Chunk chunk, Matrix4f projection, Matrix4f view) {
        // Renderizado básico de los bloques
        // Aquí se renderizaría cada bloque visible
        // Por ahora es un placeholder para la estructura
    }

    private float getTime() {
        return (float) glfwGetTime();
    }

    private void cleanup() {
        glfwSetCursorPosCallback(window, null);
        glfwSetKeyCallback(window, null);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }
}