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

    private boolean leftMouseWasPressed;

    // Configuracion de diseno proporcional
    private float btnW;
    private float btnH;
    private float btnSpacing;
    private float titleYOffset;

    // Ultimo tamaño conocido para detectar redimensionamiento
    private int lastWidth;
    private int lastHeight;

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

        // Dimensiones proporcionales a la altura de la ventana
        btnW = width * 0.18f;     // 18% del ancho (botones mas compactos)
        btnH = height * 0.08f;    // 8% del alto (botones mas compactos)
        btnSpacing = height * 0.13f; // 13% del alto entre botones
        titleYOffset = height * 0.28f; // Titulo un poco mas arriba

        float centerX = width / 2f - btnW / 2f;
        float startY = height / 2f + titleYOffset;

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

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            drawButton(btn);
            double[] mx = new double[1];
            double[] my = new double[1];
            glfwGetCursorPos(window, mx, my);
            float fx = (float) mx[0];
            float fy = (float) (height - my[0]);
            boolean hovered = btn.contains(fx, fy);

            // Texto centrado horizontal y verticalmente dentro del boton
            float textW = textWidth(btn.getText());
            float textX = btn.getX() + btn.getWidth() / 2f - textW / 2f;
            // Glyph height es 8 filas * 2px escala = 16px. Centro vertical: mitad de boton - mitad de texto
            float textY = btn.getY() + btn.getHeight() / 2f - 8;

            drawText(btn.getText(), textX, textY,
                hovered ? 1.0f : 0.8f,
                hovered ? 1.0f : 0.8f,
                hovered ? 1.0f : 0.8f);
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
    }

    private int textWidth(String text) {
        return text.length() * 12;
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
                cx += 18;
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
            cx += 18;
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
                0b00011100, 0b00100000, 0b00100000, 0b00111100,
                0b00100010, 0b00100010, 0b00011100, 0b00000000
            };
            case 'S': return new byte[]{
                0b00011110, 0b00100000, 0b00100000, 0b00011100,
                0b00000010, 0b00100010, 0b00011100, 0b00000000
            };
            case 'P': return new byte[]{
                0b00111100, 0b00100010, 0b00100010, 0b00111100,
                0b00100000, 0b00100000, 0b00100000, 0b00000000
            };
            case 'T': return new byte[]{
                0b01111110, 0b00010000, 0b00010000, 0b00010000,
                0b00010000, 0b00010000, 0b00010000, 0b00000000
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
