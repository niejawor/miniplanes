package view;

import model.Airport;
import model.Airplane;
import model.Passenger;
import model.Shape;
import model.Line;
import model.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import viewmodel.GamePresenter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final long winHandle;
    private final int backgroundTexture;
    private final int airplaneTexture;
    GamePresenter presenter;

    private final List<Airport> currentRoute = new ArrayList<>();
    private double mouseX = 0.0, mouseY = 0.0;
    private final int windowWidth = 2304;
    private final int windowHeight = 1296;

    public Window(GamePresenter presenter) {
        this.presenter = presenter;

        if (!glfwInit()) throw new IllegalStateException("Błąd GLFW");

        glfwWindowHint(GLFW_SAMPLES, 4);

        winHandle = glfwCreateWindow(windowWidth, windowHeight, presenter.getTitle(), NULL, NULL);
        if (winHandle == NULL) throw new RuntimeException("Nie udało się utworzyć okna GLFW");

        glfwMakeContextCurrent(winHandle);
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, 1.0, 1.0, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_MULTISAMPLE);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glfwSetCursorPosCallback(winHandle, (window, xpos, ypos) -> {
            mouseX = xpos / windowWidth;
            mouseY = ypos / windowHeight;
        });

        glfwSetMouseButtonCallback(winHandle, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                handleMouseClick();
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
                currentRoute.clear();
            }
        });

        glfwSetKeyCallback(winHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                handleEnterPress();
            } else if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                currentRoute.clear();
            }
        });

        backgroundTexture = loadTexture("src/assets/mapa.png");
        airplaneTexture = loadTexture("src/assets/BigAirplane.png");
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(winHandle);
    }

    public void terminate() {
        glfwDestroyWindow(winHandle);
        glfwTerminate();
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawBackground();
        drawTempRoute();
        drawLines();
        drawAirports();
        drawAirplanes();

        glfwSwapBuffers(winHandle);
        glfwPollEvents();
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

    private void drawAirports() {
        float size = 0.008f;
        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            glColor3f(0.8f, 0.1f, 0.1f);
            drawSingleShape(airport.getShape(), x, y, size);
            drawAirportDetails(airport);
        }
    }

    private void drawAirplanes() {
        if (presenter.getAirplanes() == null) return;

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, airplaneTexture);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        for (Airplane plane : presenter.getAirplanes()) {
            float x = plane.getPosition().getX();
            float y = plane.getPosition().getY();

            float scale = plane.getPassengersOnBoard().size() > 5 ? 0.015f : 0.01f;

            glPushMatrix();
            glTranslatef(x, y, 0);

            glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(-scale, -scale);
            glTexCoord2f(1, 0); glVertex2f(scale, -scale);
            glTexCoord2f(1, 1); glVertex2f(scale, scale);
            glTexCoord2f(0, 1); glVertex2f(-scale, scale);
            glEnd();

            glPopMatrix();
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    private void drawAirportDetails(Airport airport) {
        float x = airport.getPosition().getX();
        float y = airport.getPosition().getY();

        int passCount = 0;
        int maxPassengersToShow = 10;
        glColor3f(0.2f, 0.2f, 0.8f);
        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) break;
            if (passCount >= airport.getAirportType().passengerCapacity)
                glColor3f(0f, 0f, 0f);
            float px = x + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + ((passCount / 5) * 0.015f) + 0.028f;
            drawSingleShape(p.getDestination(), px, py, 0.003f);
            passCount++;
        }
    }

    private void drawSingleShape(Shape shape, float x, float y, float size) {
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScalef(1.0f, 16.0f / 9.0f, 1.0f);

        float s;

        float[] fillColor = new float[4];
        glGetFloatv(GL_CURRENT_COLOR, fillColor);

        switch (shape) {
            case Triangle:
                s = size * 1.1f;
                glBegin(GL_TRIANGLES);
                glVertex2f(0, -s);
                glVertex2f(-s, s);
                glVertex2f(s, s);
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                glVertex2f(0, -s); glVertex2f(-s, s); glVertex2f(s, s);
                glEnd();
                break;

            case Circle:
                s = size * 1.05f;
                glBegin(GL_POLYGON);
                for (int i = 0; i < 360; i += 20) {
                    float rad = (float) Math.toRadians(i);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                for (int i = 0; i < 360; i += 20) {
                    float rad = (float) Math.toRadians(i);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();
                break;

            case Diamond:
                s = size * 1.2f;
                glBegin(GL_QUADS);
                glVertex2f(0, -s);
                glVertex2f(s, 0);
                glVertex2f(0, s);
                glVertex2f(-s, 0);
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                glVertex2f(0, -s); glVertex2f(s, 0); glVertex2f(0, s); glVertex2f(-s, 0);
                glEnd();
                break;

            case Pentagon:
                s = size * 1.15f;
                glBegin(GL_POLYGON);
                for (int i = 0; i < 5; i++) {
                    float rad = (float) Math.toRadians(i * 72 - 90);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                for (int i = 0; i < 5; i++) {
                    float rad = (float) Math.toRadians(i * 72 - 90);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();
                break;

            case Hexagon:
                s = size * 1.15f;
                glBegin(GL_POLYGON);
                for (int i = 0; i < 6; i++) {
                    float rad = (float) Math.toRadians(i * 60 - 90);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                for (int i = 0; i < 6; i++) {
                    float rad = (float) Math.toRadians(i * 60 - 90);
                    glVertex2f((float) Math.cos(rad) * s, (float) Math.sin(rad) * s);
                }
                glEnd();
                break;

            case Cross:
                s = size * 1.1f;
                glBegin(GL_QUADS);

                glVertex2f(-s, -s / 3.0f);
                glVertex2f(s, -s / 3.0f);
                glVertex2f(s, s / 3.0f);
                glVertex2f(-s, s / 3.0f);

                glVertex2f(-s / 3.0f, -s);
                glVertex2f(s / 3.0f, -s);
                glVertex2f(s / 3.0f, s);
                glVertex2f(-s / 3.0f, s);
                glEnd();

                float[][] crossVertices = {
                        {-s/3.0f, -s}, {s/3.0f, -s}, {s/3.0f, -s/3.0f}, {s, -s/3.0f},
                        {s, s/3.0f}, {s/3.0f, s/3.0f}, {s/3.0f, s}, {-s/3.0f, s},
                        {-s/3.0f, s/3.0f}, {-s, s/3.0f}, {-s, -s/3.0f}, {-s/3.0f, -s/3.0f}
                };

                glColor3f(0.0f, 0.0f, 0.0f);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                for (float[] v : crossVertices)
                    glVertex2f(v[0], v[1]);
                glEnd();
                break;

            case Star:
                s = size * 1.35f;
                glBegin(GL_TRIANGLE_FAN);
                glVertex2f(0, 0);
                for (int i = 0; i <= 10; i++) {
                    float rad = (float) Math.toRadians(i * 36 - 90);
                    float r = (i % 2 == 0) ? s : s / 2.4f;
                    glVertex2f((float) Math.cos(rad) * r, (float) Math.sin(rad) * r);
                }
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                for (int i = 0; i < 10; i++) {
                    float rad = (float) Math.toRadians(i * 36 - 90);
                    float r = (i % 2 == 0) ? s : s / 2.4f;
                    glVertex2f((float) Math.cos(rad) * r, (float) Math.sin(rad) * r);
                }
                glEnd();
                break;

            case Square:
            default:
                s = size * 0.95f;
                glBegin(GL_QUADS);
                glVertex2f(-s, -s);
                glVertex2f(s, -s);
                glVertex2f(s, s);
                glVertex2f(-s, s);
                glEnd();

                glColor4f(0.0f, 0.0f, 0.0f, fillColor[3]);
                glLineWidth(1.8f);
                glBegin(GL_LINE_LOOP);
                glVertex2f(-s, -s); glVertex2f(s, -s); glVertex2f(s, s); glVertex2f(-s, s);
                glEnd();
                break;
        }

        glColor4f(fillColor[0], fillColor[1], fillColor[2], fillColor[3]);
        glLineWidth(1.0f);
        glPopMatrix();
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

    private void handleMouseClick() {
        Airport clicked = null;
        float minDistance = 0.02f;

        for (Airport airport : presenter.getAirports()) {
            float dx = airport.getPosition().getX() - (float) mouseX;
            float dy = airport.getPosition().getY() - (float) mouseY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < minDistance) {
                clicked = airport;
                break;
            }
        }

        if (clicked != null && (currentRoute.isEmpty() || currentRoute.get(currentRoute.size() - 1) != clicked))
            currentRoute.add(clicked);
    }

    private void handleEnterPress() {
        if (currentRoute.size() >= 2)
            presenter.createConfirmedRoute(new ArrayList<>(currentRoute));
        currentRoute.clear();
    }

    private void drawTempRoute() {
        if (currentRoute.isEmpty()) return;

        glColor4f(0.3f, 0.3f, 0.3f, 0.8f);
        glLineWidth(4.0f);

        glBegin(GL_LINE_STRIP);

        for (Airport a : currentRoute)
            glVertex2f(a.getPosition().getX(), a.getPosition().getY());

        glVertex2d(mouseX, mouseY);

        glEnd();
        glLineWidth(1.0f);
    }

    private void drawLines() {
        if (presenter.getLines() == null || presenter.getLines().isEmpty()) return;

        glLineWidth(5.0f);

        for (Line line : presenter.getLines()) {
            if (line.size() < 2) continue;

            setOpenGLColor(line.color);

            glBegin(GL_LINE_STRIP);
            for (int i = 0; i < line.size(); i++) {
                Airport airport = line.get(i);
                glVertex2f(airport.getPosition().getX(), airport.getPosition().getY());
            }
            glEnd();
        }

        glLineWidth(1.0f);
    }

    private void setOpenGLColor(Color color) {
        switch (color) {
            case Red:
                glColor3f(0.8f, 0.2f, 0.2f);
                break;
            case Green:
                glColor3f(0.2f, 0.8f, 0.2f);
                break;
            case Blue:
                glColor3f(0.2f, 0.2f, 0.8f);
                break;
            default:
                glColor3f(0.0f, 0.0f, 0.0f);
                break;
        }
    }
}