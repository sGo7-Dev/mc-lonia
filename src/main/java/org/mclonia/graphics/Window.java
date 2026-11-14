package org.mclonia.graphics;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window {
    // El puntero de la ventana en si
    private long handle;

    public Window(int width, int height, String title) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("No se pudo iniciarlizar GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        handle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL)
            throw new RuntimeException("No se pudo crear la ventana de GLFW");

        glfwMakeContextCurrent(handle);
    }

    // Se ejecuta hasta que se cierra todo
    public void renderLoop() {
        GL.createCapabilities();

        glClearColor(0.24f, 0.56f, 0.74f, 1.0f);

        while (!glfwWindowShouldClose(handle)) {
            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT);
            glfwSwapBuffers(handle);
        }
    }

    public void cleanup() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
