package game.panel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import org.lwjgl.opengl.GL;

import game.GameLogger;
import game.mainmenu.ShaderUI;

public class LoadPanel {
    private final long window;
    private final int width;
    private final int height;

    private ShaderUI shaderUI;
    private int vao, vbo;

    private boolean visible;
    private boolean loading;
    private boolean done;
    private float progress;

    private CompletableFuture<Void> loadFuture;
    private ExecutorService executor;

    public LoadPanel(long window, int width, int height) {
        this.window = window;
        this.width = width;
        this.height = height;
        this.visible = true;
        this.loading = false;
        this.done = false;
        this.progress = 0.0f;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void init() {
        shaderUI = new ShaderUI("shaders/ui.vert", "shaders/ui.frag");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 4 * 2 * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Habilitar face culling para optimizacion de renderizado
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        GameLogger.log("LoadPanel inicializado con face culling activado");
    }

    public void startLoading(Runnable onComplete) {
        if (loading) {
            return;
        }
        loading = true;
        progress = 0.0f;
        done = false;

        loadFuture = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i <= 100; i += 5) {
                    progress = i / 100.0f;
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                loading = false;
                done = true;
                progress = 1.0f;
            }
        }, executor).thenRunAsync(onComplete);
    }

    public void render() {
        if (!visible && !loading) {
            return;
        }

        Matrix4f projection = new Matrix4f().ortho(0, width, 0, height, -1, 1);

        shaderUI.bind();
        int projLoc = glGetUniformLocation(shaderUI.getProgram(), "projection");
        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));

        // Fondo negro
        drawQuad(0, 0, width, height, 0.0f, 0.0f, 0.0f, 1.0f);

        // Texto "Cargando..."
        drawText("CARGANDO MC-LONIA...", width / 2f - 130, height / 2f + 60, 1.0f, 1.0f, 1.0f);

        // Barra de carga (fondo)
        float barW = 400;
        float barH = 30;
        float barX = width / 2f - barW / 2f;
        float barY = height / 2f - 20;
        drawQuad(barX, barY, barW, barH, 0.2f, 0.2f, 0.2f, 1.0f);

        // Barra de carga (progreso)
        float fillW = barW * progress;
        drawQuad(barX, barY, fillW, barH, 0.2f, 0.6f, 1.0f, 1.0f);

        // Borde de la barra
        drawQuadOutline(barX, barY, barW, barH, 1.0f, 1.0f, 1.0f, 1.0f);

        // Texto de porcentaje
        int pct = (int) (progress * 100);
        drawText(pct + "%", width / 2f - 20, height / 2f - 50, 0.8f, 0.8f, 0.8f);

        shaderUI.unbind();
    }

    private void drawQuad(float x, float y, float w, float h, float r, float g, float b, float a) {
        float[] vertices = {
            x, y,
            x + w, y,
            x + w, y + h,
            x, y + h
        };

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        int colorLoc = glGetUniformLocation(shaderUI.getProgram(), "color");
        glUniform4f(colorLoc, r, g, b, a);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        glBindVertexArray(0);
    }

    private void drawQuadOutline(float x, float y, float w, float h, float r, float g, float b, float a) {
        float[] vertices = {
            x, y,
            x + w, y,
            x + w, y + h,
            x, y + h
        };

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        int colorLoc = glGetUniformLocation(shaderUI.getProgram(), "color");
        glUniform4f(colorLoc, r, g, b, a);

        glDrawArrays(GL_LINE_LOOP, 0, 4);
        glBindVertexArray(0);
    }

    private int textWidth(String text) {
        return text.length() * 16;
    }

    private void drawText(String text, float x, float y, float r, float g, float b) {
        int colorLoc = glGetUniformLocation(shaderUI.getProgram(), "color");
        glUniform4f(colorLoc, r, g, b, 1.0f);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        float cx = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            byte[] glyph = getGlyph(c);
            if (glyph == null) {
                cx += 16;
                continue;
            }

            for (int row = 0; row < 8; row++) {
                byte bits = glyph[row];
                for (int col = 0; col < 8; col++) {
                    if ((bits & (1 << (7 - col))) != 0) {
                        float ry = y + (7 - row) * 2;
                        float[] vertices = {
                            cx + col * 2, ry,
                            cx + col * 2 + 2, ry,
                            cx + col * 2 + 2, ry + 2,
                            cx + col * 2, ry + 2
                        };
                        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
                        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
                    }
                }
            }
            cx += 16;
        }

        glBindVertexArray(0);
    }

    private byte[] getGlyph(char c) {
        switch (c) {
            case 'C': return new byte[]{
                0b00111100, 0b01000010, 0b01000000, 0b01000000,
                0b01000000, 0b01000010, 0b00111100, 0b00000000
            };
            case 'A': return new byte[]{
                0b00011000, 0b00100100, 0b00100100, 0b00100100,
                0b00111100, 0b00100100, 0b00100100, 0b00000000
            };
            case 'R': return new byte[]{
                0b00111100, 0b00100010, 0b00100010, 0b00111100,
                0b00101000, 0b00100100, 0b00100010, 0b00000000
            };
            case 'G': return new byte[]{
                0b00011100, 0b00100010, 0b00100010, 0b00100010,
                0b00111110, 0b00000010, 0b00100010, 0b00011100
            };
            case 'O': return new byte[]{
                0b00111100, 0b01000010, 0b01000010, 0b01000010,
                0b01000010, 0b01000010, 0b00111100, 0b00000000
            };
            case 'N': return new byte[]{
                0b01000010, 0b01100010, 0b01010010, 0b01001010,
                0b01000110, 0b01000010, 0b01000010, 0b00000000
            };
            case 'D': return new byte[]{
                0b00111100, 0b00100010, 0b00100010, 0b00100010,
                0b00100010, 0b00100010, 0b00111100, 0b00000000
            };
            case 'I': return new byte[]{
                0b01111110, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b01111110, 0b00000000
            };
            case 'L': return new byte[]{
                0b01000000, 0b01000000, 0b01000000, 0b01000000,
                0b01000000, 0b01000000, 0b01111110, 0b00000000
            };
            case 'E': return new byte[]{
                0b00011100, 0b00100000, 0b00100000, 0b00111100,
                0b00100010, 0b00100010, 0b00011100, 0b00000000
            };
            case 'M': return new byte[]{
                0b01000010, 0b01100110, 0b01100110, 0b01010101,
                0b01010101, 0b01000010, 0b01000010, 0b01000010
            };
            case 'T': return new byte[]{
                0b01111110, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00010000, 0b00000000
            };
            case 'S': return new byte[]{
                0b00011110, 0b00100000, 0b00100000, 0b00011100,
                0b00000010, 0b00100010, 0b00011100, 0b00000000
            };
            case 'U': return new byte[]{
                0b00100010, 0b00100010, 0b00100010, 0b00100010,
                0b00100010, 0b00100010, 0b00111110, 0b00000000
            };
            case 'P': return new byte[]{
                0b00111100, 0b00100010, 0b00100010, 0b00111100,
                0b00100000, 0b00100000, 0b00100000, 0b00000000
            };
            case '1': return new byte[]{
                0b00010000, 0b00110000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00111110, 0b00000000
            };
            case '0': return new byte[]{
                0b00111100, 0b01000010, 0b01000010, 0b01000010,
                0b01000010, 0b01000010, 0b00111100, 0b00000000
            };
            case '.': return new byte[]{
                0b00000000, 0b00000000, 0b00000000, 0b00000000,
                0b00000000, 0b00000000, 0b00000100, 0b00000000
            };
            case '%': return new byte[]{
                0b00100010, 0b00100010, 0b00010100, 0b00001000,
                0b00010100, 0b00100010, 0b00100010, 0b00000000
            };
            case ' ': return new byte[]{
                0b00000000, 0b00000000, 0b00000000, 0b00000000,
                0b00000000, 0b00000000, 0b00000000, 0b00000000
            };
            default: return null;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isDone() {
        return done;
    }

    public float getProgress() {
        return progress;
    }

    public void shutdown() {
        if (loadFuture != null) {
            loadFuture.cancel(true);
        }
        executor.shutdownNow();
        if (shaderUI != null) {
            shaderUI.cleanup();
        }
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
