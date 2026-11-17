package game.mainmenu;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    private final long window;
    private boolean fullscreen;

    private final List<MenuButton> buttons;
    private ShaderUI shaderUI;
    private int vao, vbo;
private boolean visible;
    private MenuResult result;

    private float btnW, btnH, btnSpacing, titleYOffset;
    private float textScale;
    private int lastWidth, lastHeight;
    private boolean leftMouseWasPressed;

    public enum MenuResult {
        PLAY, QUIT, NONE
    }

    public MainMenu(long window) {
        this.window = window;
        this.fullscreen = false;
        this.buttons = new ArrayList<MenuButton>();
        this.result = MenuResult.NONE;
        this.visible = true;
        this.leftMouseWasPressed = false;
        // Valores por defecto (se recalculan en recalculateLayout)
        this.btnW = 320;
        this.btnH = 70;
        this.btnSpacing = 110;
        this.titleYOffset = 240;
        int[] initial = getWindowSize();
        this.lastWidth = initial[0];
        this.lastHeight = initial[1];
    }

    public void init() {
        recalculateLayout();

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
    }

public void recalculateLayout() {
        int[] size = getWindowSize();
        float width = size[0];
        float height = size[1];

        // Dimensiones proporcionales - 50% del tamaño original
        // Original: 18% ancho, 8% alto, 13% spacing
        // Nuevo: 9% ancho, 4% alto, 6.5% spacing
        btnW = width * 0.09f;       // 9% del ancho (50% reducido)
        btnH = height * 0.04f;      // 4% del alto (50% reducido)
        btnSpacing = height * 0.065f; // 6.5% del alto entre botones (50% reducido)
        titleYOffset = height * 0.18f; // Título más arriba

// Calcular escala de texto proporcional a la altura del botón
        // Glyph original: 8px bitmap. Queremos que el texto ocupe ~60% de la altura del botón
        float targetTextHeight = btnH * 0.6f;
textScale = targetTextHeight / 8f; // 8px = altura original del glyph

        float centerX = width / 2f - btnW / 2f;
        // Los botones van DEBAJO del título (Y aumenta hacia arriba en OpenGL)
        float startY = height / 2f + titleYOffset - btnSpacing;

        // Recrear botones con nuevas posiciones
        buttons.clear();
        buttons.add(new MenuButton("Jugar", "PLAY", centerX, startY, btnW, btnH));
        buttons.add(new MenuButton("Opciones", "OPTIONS", centerX, startY - btnSpacing, btnW, btnH));
        buttons.add(new MenuButton("Salir", "QUIT", centerX, startY - btnSpacing * 2, btnW, btnH));
    }

    private int[] getWindowSize() {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(window, w, h);
        return new int[]{w[0], h[0]};
    }

    public void updateInput() {
        if (!visible) {
            return;
        }

        if (glfwGetKey(window, GLFW_KEY_F11) == GLFW_PRESS) {
            toggleFullscreen();
        }

        boolean leftMousePressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
        boolean leftMouseClicked = leftMousePressed && !leftMouseWasPressed;
        leftMouseWasPressed = leftMousePressed;

        if (leftMouseClicked) {
            int[] size = getWindowSize();
            float height = size[1];
            double[] mx = new double[1];
            double[] my = new double[1];
            glfwGetCursorPos(window, mx, my);
            float fx = (float) mx[0];
            float fy = (float) (height - my[0]);
            for (int i = 0; i < buttons.size(); i++) {
                MenuButton btn = buttons.get(i);
                if (btn.contains(fx, fy)) {
                    btn.press();
                }
            }
        }

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            if (btn.isPressed()) {
                String action = btn.getActionId();
                if ("PLAY".equals(action)) {
                    result = MenuResult.PLAY;
                    visible = false;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                } else if ("QUIT".equals(action)) {
                    result = MenuResult.QUIT;
                    visible = false;
                }
                btn.reset();
            }
        }
    }

    public MenuResult consumeResult() {
        MenuResult r = result;
        result = MenuResult.NONE;
        return r;
    }

    private void toggleFullscreen() {
        if (fullscreen) {
            glfwSetWindowMonitor(window, 0, 100, 100, 1280, 720, 0);
            fullscreen = false;
        } else {
            long monitor = glfwGetPrimaryMonitor();
            if (monitor != 0) {
                var mode = glfwGetVideoMode(monitor);
                glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
                fullscreen = true;
            }
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void render() {
        if (!visible) {
            return;
        }

        int[] size = getWindowSize();
        float width = size[0];
        float height = size[1];

        // Recalcular layout si la ventana cambio de tamaño (maximizar, restaurar, redimensionar)
        if (width != lastWidth || height != lastHeight) {
            recalculateLayout();
            lastWidth = (int) width;
            lastHeight = (int) height;
        }

        Matrix4f projection = new Matrix4f().ortho(0, width, 0, height, -1, 1);

        shaderUI.bind();
        int projLoc = glGetUniformLocation(shaderUI.getProgram(), "projection");
        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));

        // Titulo centrado
        float titleWidth = textWidth("MC-LONIA");
        drawText("MC-LONIA", width / 2f - titleWidth / 2f, height / 2f + titleYOffset, 1.0f, 1.0f, 1.0f);
// Dibujar botones
        for (MenuButton btn : buttons) {
            drawButton(btn);
        }

        drawText("F11: Pantalla completa", 20, 30, 0.6f, 0.6f, 0.6f);

shaderUI.unbind();
    }

    private void drawButton(MenuButton btn) {
        int[] size = getWindowSize();
        float height = size[1];

        float x = btn.getX();
        float y = btn.getY();
        float w = btn.getWidth();
        float h = btn.getHeight();

        double[] mx = new double[1];
        double[] my = new double[1];
        glfwGetCursorPos(window, mx, my);
        float fx = (float) mx[0];
        float fy = (float) (height - my[0]);
        boolean hovered = btn.contains(fx, fy);

        float c = hovered ? 0.5f : 0.25f;

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
        glUniform4f(colorLoc, c, c, c, 1.0f);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glUniform4f(colorLoc, 0.9f, 0.9f, 0.9f, 1.0f);
        glDrawArrays(GL_LINE_LOOP, 0, 4);

        glBindVertexArray(0);

// Dibujar el texto centrado dentro del botón
        String label = btn.getText();
        float labelScale = textScale;

        // Ancho real del texto
        int tw = 0;
        for (int i = 0; i < label.length(); i++) {
            if (getGlyph(label.charAt(i)) != null) tw += 8;
        }
        tw = (int) (tw * labelScale);

// Centrar horizontalmente
        float textX = x + (w - tw) / 2f;
        // Centrado vertical correcto: el glyph tiene 8 filas, la fila 0 es la superior
        // Para centrar: y + (h - 8*scale)/2
        float textY = y + (h - 8 * labelScale) / 2f;
        // Color blanco puro
        glColor3f(1.0f, 1.0f, 1.0f);
        drawText(label, textX, textY, 1.0f, 1.0f, 1.0f);
    }


    private int textWidth(String text) {
        // Ancho = número de caracteres * 8px (ancho glyph) * textScale
        // Glyph original: 8px de ancho, escalado por textScale
        return (int) (text.length() * 8 * textScale);
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
                cx += 8 * textScale;
                continue;
            }

            for (int row = 0; row < 8; row++) {
                byte bits = glyph[row];
                for (int col = 0; col < 8; col++) {
                    if ((bits & (1 << (7 - col))) != 0) {
                        float ry = y + (7 - row) * textScale;
                        float[] vertices = {
                            cx + col * textScale, ry,
                            cx + col * textScale + textScale, ry,
                            cx + col * textScale + textScale, ry + textScale,
                            cx + col * textScale, ry + textScale
                        };
                        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
                        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
                    }
                }
            }
            cx += 8 * textScale;
        }

        glBindVertexArray(0);
    }

private byte[] getGlyph(char c) {
        switch (c) {
            case 'M': return new byte[]{
                0b01000010, 0b01100110, 0b01100110, 0b01010101,
                0b01010101, 0b01000010, 0b01000010, 0b01000010
            };
            case 'C': return new byte[]{
                0b00111100, 0b01000010, 0b01000000, 0b01000000,
                0b01000000, 0b01000010, 0b00111100, 0b00000000
            };
            case 'L': return new byte[]{
                0b01000000, 0b01000000, 0b01000000, 0b01000000,
                0b01000000, 0b01000000, 0b01111110, 0b00000000
            };
            case 'O': return new byte[]{
                0b00111100, 0b01000010, 0b01000010, 0b01000010,
                0b01000010, 0b01000010, 0b00111100, 0b00000000
            };
            case 'N': return new byte[]{
                0b01000010, 0b01100010, 0b01010010, 0b01001010,
                0b01000110, 0b01000010, 0b01000010, 0b00000000
            };
            case 'I': return new byte[]{
                0b01111110, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b01111110, 0b00000000
            };
            case 'A': return new byte[]{
                0b00011000, 0b00100100, 0b00100100, 0b00100100,
                0b00111100, 0b00100100, 0b00100100, 0b00000000
            };
            case 'J': return new byte[]{
                0b01111110, 0b00000100, 0b00000100, 0b00000100,
                0b00000100, 0b00100100, 0b00100100, 0b00011000
            };
            case 'U': return new byte[]{
                0b00100010, 0b00100010, 0b00100010, 0b00100010,
                0b00100010, 0b00100010, 0b00111110, 0b00000000
            };
            case 'G': return new byte[]{
                0b00011100, 0b00100010, 0b00100010, 0b00100010,
                0b00111110, 0b00000010, 0b00100010, 0b00011100
            };
            case 'R': return new byte[]{
                0b00111100, 0b00100010, 0b00100010, 0b00111100,
                0b00101000, 0b00100100, 0b00100010, 0b00000000
            };
            case 'E': return new byte[]{
                0b00011100, 0b01000000, 0b01000000, 0b00111100,
                0b01000010, 0b01000010, 0b00011100, 0b00000000
            };
            case 'S': return new byte[]{
                0b00011110, 0b01000000, 0b01000000, 0b00011100,
                0b00000010, 0b01000010, 0b00011100, 0b00000000
            };
            case 'P': return new byte[]{
                0b00111100, 0b01000010, 0b01000010, 0b00111100,
                0b01000000, 0b01000000, 0b01000000, 0b00000000
            };
            case 'T': return new byte[]{
                0b01111110, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00010000, 0b00000000
            };
            // Minúsculas para textos de botones
            case 'a': return new byte[]{
                0b00000000, 0b00000000, 0b00011100, 0b00100100,
                0b00111100, 0b00100100, 0b00100100, 0b00000000
            };
            case 'c': return new byte[]{
                0b00000000, 0b00000000, 0b00011100, 0b00100000,
                0b00100000, 0b00100000, 0b00011100, 0b00000000
            };
            case 'e': return new byte[]{
                0b00000000, 0b00000000, 0b00011100, 0b00100000,
                0b00111100, 0b00100000, 0b00011100, 0b00000000
            };
            case 'i': return new byte[]{
                0b00010000, 0b00000000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00010000, 0b00000000
            };
            case 'l': return new byte[]{
                0b00010000, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00010000, 0b00000000
            };
            case 'n': return new byte[]{
                0b00000000, 0b00000000, 0b00111100, 0b00100100,
                0b00100100, 0b00100100, 0b00100100, 0b00000000
            };
            case 'o': return new byte[]{
                0b00000000, 0b00000000, 0b00011100, 0b00100010,
                0b00100010, 0b00100010, 0b00011100, 0b00000000
            };
            case 'p': return new byte[]{
                0b00000000, 0b00000000, 0b00111100, 0b00100010,
                0b00111100, 0b00100000, 0b00100000, 0b00000000
            };
            case 'r': return new byte[]{
                0b00000000, 0b00000000, 0b00111100, 0b00100010,
                0b00100000, 0b00100000, 0b00100000, 0b00000000
            };
            case 's': return new byte[]{
                0b00000000, 0b00000000, 0b00011100, 0b00100000,
                0b00011100, 0b00000010, 0b00111100, 0b00000000
            };
            case 'u': return new byte[]{
                0b00000000, 0b00000000, 0b00100010, 0b00100010,
                0b00100010, 0b00100010, 0b00011100, 0b00000000
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
        if (visible) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
    }

    public void cleanup() {
        if (shaderUI != null) {
            shaderUI.cleanup();
        }
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
