package game.mainmenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;

public class ShaderUI {
    private final int program;

    public ShaderUI(String vertexPath, String fragmentPath) {
        int vertexShader = compileShader(loadSource(vertexPath), GL_VERTEX_SHADER);
        int fragmentShader = compileShader(loadSource(fragmentPath), GL_FRAGMENT_SHADER);

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        int linked = glGetProgrami(program, GL_LINK_STATUS);
        if (linked == 0) {
            String log = glGetProgramInfoLog(program);
            game.GameLogger.logError("Error vinculando shader UI: " + log, null);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private String loadSource(String path) {
        try (InputStream is = ShaderUI.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                game.GameLogger.logError("No se encontro el shader UI en classpath: " + path, null);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            game.GameLogger.logError("No se pudo cargar el shader UI: " + path, e);
            return "";
        }
    }

    private int compileShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (compiled == 0) {
            String log = glGetShaderInfoLog(shader);
            game.GameLogger.logError("Error compilando shader UI: " + log, null);
        }

        return shader;
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgram() {
        return program;
    }

    public void cleanup() {
        glDeleteProgram(program);
    }
}
