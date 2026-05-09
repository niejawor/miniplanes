package view;

import org.lwjgl.opengl.GL;
import viewmodel.GamePresenter;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public void open(GamePresenter presenter) {
        if (!glfwInit()) throw new IllegalStateException("Błąd GLFW");

        long win = glfwCreateWindow(800, 600, presenter.getTitle(), NULL, NULL);
        glfwMakeContextCurrent(win);
        GL.createCapabilities();

        float[] c = presenter.getColor();
        glClearColor(c[0], c[1], c[2], c[3]);

        while (!glfwWindowShouldClose(win)) {
            glClear(GL_COLOR_BUFFER_BIT);
            glfwSwapBuffers(win);
            glfwPollEvents();
        }
        glfwTerminate();
    }
}