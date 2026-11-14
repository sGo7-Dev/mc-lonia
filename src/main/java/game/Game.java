package game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.lwjgl.opengl.GL;

import game.mainmenu.MainMenu;
import game.panel.LoadPanel;
import game.word.CicloDiaNoche;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

public class Game {
    private long window;
    private int width;
    private int height;
    private String title;

    private World world;
    private Chunks chunks;
    private Player player;
    private TextureAtlas textureAtlas;
    private Shader shader;
    private MainMenu menu;
    private LoadPanel loadPanel;
    private CicloDiaNoche cicloDiaNoche;

    private boolean running;
    private float lastTime;
    private float deltaTime;

    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouse = true;

    private enum GameState {
        LOADING, MENU, PLAYING
    }

    private GameState state;

    // Configuracion de generacion de chunks alrededor del jugador
    private static final int CHUNK_RENDER_RADIUS = 2;
    private static final int MAX_CHUNKS = 20;

    public Game(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.state = GameState.LOADING;
    }

    public void init() {
        GameLogger.log("Inicializando GLFW...");
        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // VSYNC para limitar FPS y evitar 100% CPU
        glfwSwapInterval(1);

        GameLogger.log("OpenGL version: " + glGetString(GL_VERSION));

        // Face culling habilitado desde el inicio
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        GameLogger.log("Face culling activado");

        textureAtlas = new TextureAtlas();
        world = new World();
        chunks = new Chunks(world);
        player = new Player(world);
        shader = new Shader("shaders/block.vert", "shaders/block.frag");

        loadPanel = new LoadPanel(window, width, height);
        loadPanel.init();

        menu = new MainMenu(window);
        menu.init();

        cicloDiaNoche = new CicloDiaNoche();

        // Callback de movimiento del mouse para rotar camara
        glfwSetCursorPosCallback(window, (win, mouseX, mouseY) -> {
            if (state == GameState.PLAYING) {
                if (firstMouse) {
                    lastMouseX = mouseX;
                    lastMouseY = mouseY;
                    firstMouse = false;
                }
                double offsetX = mouseX - lastMouseX;
                double offsetY = lastMouseY - mouseY; // Invertido porque Y crece hacia abajo
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                player.setMouseLook((float) offsetX, (float) offsetY);
            }
        });

        // Callback de resize para actualizar proyeccion
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            this.width = w;
            this.height = h;
            glViewport(0, 0, w, h);
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        // Iniciar carga en segundo plano
        loadPanel.startLoading(() -> {
            GameLogger.log("Carga completada. Iniciando juego...");
            state = GameState.MENU;
        });

        lastTime = getTime();

        GameLogger.log("Sistemas inicializados");
        GameLogger.log("Estado inicial: " + state);
    }

    public void run() {
        init();
        running = true;

        GameLogger.log("Iniciando bucle principal...");

        while (running && !glfwWindowShouldClose(window)) {
            float currentTime = getTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            // Limitar deltaTime para evitar saltos grandes si la ventana se congelo
            if (deltaTime > 0.1f) {
                deltaTime = 0.1f;
            }

            processInput();

            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            if (state == GameState.PLAYING && cicloDiaNoche != null) {
                Vector3f cielo = cicloDiaNoche.getColorCielo();
                glClearColor(cielo.x, cielo.y, cielo.z, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            }

            switch (state) {
                case LOADING:
                    loadPanel.render();
                    break;
                case MENU:
                    menu.render();
                    break;
                case PLAYING:
                    update(deltaTime);
                    renderWorld();
                    break;
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        cleanup();
    }

    private void processInput() {
        switch (state) {
            case LOADING:
                break;
            case MENU:
                menu.updateInput();
                MainMenu.MenuResult result = menu.consumeResult();
                if (result == MainMenu.MenuResult.PLAY) {
                    state = GameState.PLAYING;
                    menu.setVisible(false);
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    // Centrar cursor al entrar al juego
                    glfwSetCursorPos(window, width / 2.0, height / 2.0);
                    lastMouseX = width / 2.0;
                    lastMouseY = height / 2.0;
                    firstMouse = true;
                } else if (result == MainMenu.MenuResult.QUIT) {
                    running = false;
                }
                break;
            case PLAYING:
                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    state = GameState.MENU;
                    menu.setVisible(true);
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                }
                player.update(deltaTime, window);
                break;
        }
    }

    private void update(float deltaTime) {
        chunks.update(player);
        if (cicloDiaNoche != null) {
            cicloDiaNoche.update(deltaTime);
        }
    }

    private void renderWorld() {
        shader.bind();
        textureAtlas.bind();

        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(window, w, h);
        float renderWidth = w[0];
        float renderHeight = h[0];

        Matrix4f projection = new Matrix4f()
            .perspective((float) Math.toRadians(70.0f), renderWidth / renderHeight, 0.1f, 1000.0f);

        Vector3f lookDir = new Vector3f(
            (float) (Math.cos(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch()))),
            (float) (Math.sin(Math.toRadians(player.getPitch()))),
            (float) (-Math.sin(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch())))
        );

        Matrix4f view = new Matrix4f()
            .lookAt(player.getPosition(),
                    player.getPosition().add(lookDir, new Vector3f()),
                    new Vector3f(0, 1, 0));

        int projLoc = glGetUniformLocation(shader.getProgram(), "projection");
        int viewLoc = glGetUniformLocation(shader.getProgram(), "view");
        int modelLoc = glGetUniformLocation(shader.getProgram(), "model");
        int lightLoc = glGetUniformLocation(shader.getProgram(), "lightPos");

        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));
        glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));

        Vector3f luzPos = cicloDiaNoche != null ? cicloDiaNoche.getLuzPosicion() : new Vector3f(10.0f, 20.0f, 10.0f);
        glUniform3f(lightLoc, luzPos.x, luzPos.y, luzPos.z);

        int totalChunks = 0;

        List<Chunk> chunkList = new ArrayList<>(world.getChunks().values());
        for (Chunk chunk : chunkList) {
            ChunkMesh mesh = chunk.getMesh();
            if (mesh == null || mesh.isDirty()) {
                ChunkMeshBuilder.MeshData meshData = ChunkMeshBuilder.buildMesh(chunk, world);
                if (mesh == null) {
                    mesh = new ChunkMesh();
                    chunk.setMesh(mesh);
                }
                mesh.upload(meshData.vertices, meshData.indices);
            }

            Matrix4f model = new Matrix4f()
                .translation(chunk.getWorldX() * Chunk.SIZE, 0, chunk.getWorldZ() * Chunk.SIZE);
            int modelLoc2 = glGetUniformLocation(shader.getProgram(), "model");
            glUniformMatrix4fv(modelLoc2, false, model.get(new float[16]));

            mesh.render();
            totalChunks++;
        }

        shader.unbind();
    }

    private float getTime() {
        return (float) glfwGetTime();
    }

    public void cleanup() {
        GameLogger.log("Limpiando recursos...");
        if (loadPanel != null) {
            loadPanel.shutdown();
        }
        if (menu != null) {
            menu.cleanup();
        }
        if (shader != null) {
            shader.cleanup();
        }
        if (textureAtlas != null) {
            // TextureAtlas no tiene metodo cleanup propio, pero se libera con el contexto OpenGL
        }
        if (chunks != null) {
            for (Chunk chunk : chunks.getChunks().values()) {
                ChunkMesh mesh = chunk.getMesh();
                if (mesh != null) {
                    mesh.cleanup();
                }
            }
        }
        glfwDestroyWindow(window);
        glfwTerminate();
        GameLogger.log("Recursos limpiados");
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

    public Chunks getChunks() {
        return chunks;
    }
}
