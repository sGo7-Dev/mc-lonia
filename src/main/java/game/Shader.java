package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;

public class Shader {
    private final int program;

    public Shader(String vertexPath, String fragmentPath) {
        int vertexShader = compileShader(loadSource(vertexPath), GL_VERTEX_SHADER);
        int fragmentShader = compileShader(loadSource(fragmentPath), GL_FRAGMENT_SHADER);

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        int linked = glGetProgrami(program, GL_LINK_STATUS);
        if (linked == 0) {
            String log = glGetProgramInfoLog(program);
            GameLogger.logError("Error vinculando shader: " + log, null);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private String loadSource(String path) {
        try (InputStream is = Shader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                GameLogger.logError("No se encontro el shader en classpath: " + path, null);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            GameLogger.logError("No se pudo cargar el shader: " + path, e);
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
            GameLogger.logError("Error compilando shader: " + log, null);
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
