package game;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.lwjgl.opengl.GL;

import game.mainmenu.MainMenu;
import game.panel.LoadPanel;

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

    private boolean running;
    private float lastTime;
    private float deltaTime;

    private int totalFacesBefore = 0;
    private int totalFacesAfter = 0;

    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouse = true;

    private enum GameState {
        LOADING, MENU, PLAYING
    }

    private GameState state;

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

        menu = new MainMenu(window, width, height);
        menu.init();

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

            processInput();

            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

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
                    menu.render();
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
                if (menu.getResult() == MainMenu.MenuResult.PLAY) {
                    state = GameState.PLAYING;
                    menu.setVisible(false);
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                } else if (menu.getResult() == MainMenu.MenuResult.QUIT) {
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
        chunks.update();
    }

    private void renderWorld() {
        shader.bind();
        textureAtlas.bind();

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

        int projLoc = glGetUniformLocation(shader.getProgram(), "projection");
        int viewLoc = glGetUniformLocation(shader.getProgram(), "view");
        int modelLoc = glGetUniformLocation(shader.getProgram(), "model");
        int lightLoc = glGetUniformLocation(shader.getProgram(), "lightPos");

        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));
        glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));
        glUniform3f(lightLoc, 10.0f, 20.0f, 10.0f);

        int totalFaces = 0;
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
                totalFacesAfter += meshData.faceCount;
            } else {
                totalFacesAfter += mesh.getIndexCount() / 6;
            }

            Matrix4f model = new Matrix4f()
                .translation(chunk.getWorldX() * Chunk.SIZE, 0, chunk.getWorldZ() * Chunk.SIZE);
            glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));

            mesh.render();
            totalFaces += mesh.getIndexCount();
            totalChunks++;
        }

        shader.unbind();

        if (totalFaces > 0) {
            totalFacesBefore = totalFaces;
        }
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
}
