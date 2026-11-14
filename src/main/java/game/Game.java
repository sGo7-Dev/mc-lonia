package game;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import org.lwjgl.opengl.GL;

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

    private boolean running;
    private float lastTime;
    private float deltaTime;

    private int totalFacesBefore = 0;
    private int totalFacesAfter = 0;

    public Game(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
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

        textureAtlas = new TextureAtlas();
        world = new World();
        chunks = new Chunks(world);
        player = new Player(world);
        shader = new Shader("shaders/block.vert", "shaders/block.frag");

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

        GameLogger.log("Sistemas inicializados");
        GameLogger.log("Chunks iniciales: " + chunks.getChunkCount());
    }

    public void run() {
        init();
        running = true;

        GameLogger.log("Iniciando bucle principal...");

        while (running && !glfwWindowShouldClose(window)) {
            float currentTime = getTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            glClearColor(0.24f, 0.56f, 0.74f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);

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

        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));
        glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));

        int totalFaces = 0;
        int totalChunks = 0;

        for (Chunk chunk : chunks.getChunks().values()) {
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
        glfwSetCursorPosCallback(window, null);
        glfwSetKeyCallback(window, null);
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
