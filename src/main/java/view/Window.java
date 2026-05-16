package view;

import model.Airport;
import model.Shape;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import viewmodel.GamePresenter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int backgroundTexture;

    public void open(GamePresenter presenter) {
        if (!glfwInit()) throw new IllegalStateException("Błąd GLFW");

        long win = glfwCreateWindow(2304, 1296, presenter.getTitle(), NULL, NULL);
        glfwMakeContextCurrent(win);
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, 1.0, 1.0, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        backgroundTexture = loadTexture("src/assets/mapa.png");

        while (!glfwWindowShouldClose(win)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            drawBackground();

            drawAirports(presenter);

            glfwSwapBuffers(win);
            glfwPollEvents();
        }

        glfwTerminate();
    }

    private void drawBackground() {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, backgroundTexture);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(0, 0);
        glTexCoord2f(1, 0); glVertex2f(1, 0);
        glTexCoord2f(1, 1); glVertex2f(1, 1);
        glTexCoord2f(0, 1); glVertex2f(0, 1);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    private void drawAirports(GamePresenter presenter) {
        float size = 0.008f;

        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            Shape shape = airport.getShape();

            glColor3f(0.8f, 0.1f, 0.1f);

            glPushMatrix();
            glTranslatef(x, y, 0);

            glScalef(1.0f, 16.0f / 9.0f, 1.0f);

            switch (shape) {
                case Triangle:
                    glBegin(GL_TRIANGLES);
                    glVertex2f(0, -size);
                    glVertex2f(-size, size);
                    glVertex2f(size, size);
                    glEnd();
                    break;
                case Circle:
                    glBegin(GL_POLYGON);
                    for (int i = 0; i < 360; i += 20) {
                        float rad = (float) Math.toRadians(i);
                        glVertex2f((float) Math.cos(rad) * size, (float) Math.sin(rad) * size);
                    }
                    glEnd();
                    break;
                default:
                    glBegin(GL_QUADS);
                    glVertex2f(-size, -size);
                    glVertex2f(size, -size);
                    glVertex2f(size, size);
                    glVertex2f(-size, size);
                    glEnd();
                    break;
            }
            glPopMatrix();
        }
    }

    private int loadTexture(String filepath) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        stbi_set_flip_vertically_on_load(false);

        ByteBuffer image = stbi_load(filepath, width, height, channels, 4);
        if (image == null) {
            throw new RuntimeException("Nie udało się wczytać pliku: " + filepath + "\n" + stbi_failure_reason());
        }

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        stbi_image_free(image);
        return textureID;
    }
}