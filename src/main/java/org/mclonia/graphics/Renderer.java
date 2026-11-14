package org.mclonia.graphics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL31.*;

public class Renderer {
    float[] vertices = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f
    };

    int vbo, vao;
    int vertexShader, fragmentShader;
    int program;

    public Renderer() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        String vertexSource = "";
        String fragmentSource = "";
        try {
            Path vertexSourcePath = Path.of("src/main/resources/shaders/normal.vert");
            vertexSource = Files.readString(vertexSourcePath);

            Path fragmentSourcePath = Path.of("src/main/resources/shaders/normal.frag");
            fragmentSource = Files.readString(fragmentSourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);

        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);

        program = glCreateProgram();

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void render() {
        glUseProgram(program);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    public void cleanup() {
        glDeleteProgram(program);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}
