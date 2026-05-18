package view;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import model.*;
import viewmodel.GamePresenter;

import java.util.ArrayList;
import java.util.List;

public class Window extends ApplicationAdapter {
    private final GamePresenter presenter;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Texture backgroundTexture;
    private Texture airplaneTexture;

    private final List<Airport> currentRoute = new ArrayList<>();
    private final Vector3 mousePos = new Vector3();

    public Window(GamePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(true, 1.0f, 1.0f);

        backgroundTexture = new Texture(Gdx.files.local("src/assets/mapa.png"));
        airplaneTexture = new Texture(Gdx.files.local("src/assets/airplane2.png"));

        Gdx.input.setInputProcessor(new InputAdapter() {
           @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
               camera.unproject(mousePos.set(screenX, screenY, 0));
               if (button == Input.Buttons.LEFT)
                   handleMouseClick(mousePos.x, mousePos.y);
               else if (button == Input.Buttons.RIGHT)
                   currentRoute.clear();
               return true;
           }

           @Override
           public boolean keyDown(int keycode) {
               if (keycode == Input.Keys.ENTER)
                   handleEnterPress();
               else if (keycode == Input.Keys.ESCAPE)
                   currentRoute.clear();
               return true;
           }

           @Override
           public boolean mouseMoved(int screenX, int screenY) {
               camera.unproject(mousePos.set(screenX, screenY, 0));
               return true;
           }
        });
    }

    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 1, 1, 0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(), false, true);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawTempRoute();
        drawLines();
        drawAirports();

        batch.begin();
        drawAirplanes();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawAirports() {
        float size = 0.008f;
        Color color = Color.Red;
        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            drawSingleShape(airport.getShape(), x, y, size, color);
            drawAirportDetails(airport);
        }
    }

    private void drawAirplanes() {
        if (presenter.getAirplanes() == null) return;

        for (Airplane plane : presenter.getAirplanes()) {
            float x = plane.getPosition().getX();
            float y = plane.getPosition().getY();
            float scale = plane.getType() == AirplaneType.SmallAirplane ? 0.015f : 0.01f;

            float angle = 0f;
            if (plane.isCurrentlyFlying()) {
                float destX = plane.getDestination().getPosition().getX();
                float destY = plane.getDestination().getPosition().getY();

                float dx = destX - x;
                float dy = destY - y;

                angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            }

            float width = scale * 2;
            float height = scale * 2;
            float originX = scale;
            float originY = scale;

            batch.draw(airplaneTexture, x - scale, y - scale, originX, originY, width, height, 1.0f, 1.0f, angle,0, 0, airplaneTexture.getWidth(), airplaneTexture.getHeight(),false, true);
        }
    }

    private void drawAirportDetails(Airport airport) {
        float x = airport.getPosition().getX();
        float y = airport.getPosition().getY();

        int passCount = 0;
        int maxPassengersToShow = 10;

        Color normalColor = Color.Blue;
        Color overloadColor = Color.Black;
        Color color = normalColor;

        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) break;
            if (passCount == airport.getAirportType().passengerCapacity)
                color = overloadColor;
            float px = x + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + ((passCount / 5) * 0.015f) + 0.028f;
            drawSingleShape(p.getDestination(), px, py, 0.003f, color);
            passCount++;
        }
    }

    private void drawTempRoute() {
        if (currentRoute.isEmpty()) return;

        Gdx.gl.glLineWidth(4.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.8f);

        for (int i = 0; i < currentRoute.size() - 1; i++) {
            Airport a1 = currentRoute.get(i);
            Airport a2 = currentRoute.get(i + 1);
            shapeRenderer.line(a1.getPosition().getX(), a1.getPosition().getY(), a2.getPosition().getX(), a2.getPosition().getY());
        }

        Airport last = currentRoute.get(currentRoute.size() - 1);
        shapeRenderer.line(last.getPosition().getX(), last.getPosition().getY(), mousePos.x, mousePos.y);

        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
    }

    private void drawLines() {
        if (presenter.getLines() == null || presenter.getLines().isEmpty()) return;

        Gdx.gl.glLineWidth(5.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Line line : presenter.getLines()) {
            if (line.size() < 2) continue;
            setShapeRendererColor(line.color);

            for (int i = 0; i < line.size() - 1; i++) {
                Airport a1 = line.get(i);
                Airport a2 = line.get(i + 1);
                shapeRenderer.line(a1.getPosition().getX(), a1.getPosition().getY(), a2.getPosition().getX(), a2.getPosition().getY());
            }

        }

        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
    }

    private void drawSingleShape(Shape shape, float x, float y, float size, Color color) {
        Matrix4 transform = new Matrix4();
        transform.setToTranslation(x, y, 0);
        transform.scale(1.0f, 16.0f / 9.0f, 1.0f);
        shapeRenderer.setTransformMatrix(transform);

        float s;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        setShapeRendererColor(color);

        switch (shape) {
            case Triangle:
                s = size * 1.1f;
                shapeRenderer.triangle(0, -s, -s, s, s, s);
                break;
            case Circle:
                s = size * 1.05f;
                shapeRenderer.circle(0, 0, s, 30);
                break;
            case Diamond:
                s = size * 1.2f;
                shapeRenderer.triangle(0, -s, s, 0, -s, 0);
                shapeRenderer.triangle(-s, 0, s, 0, 0, s);
                break;
            case Pentagon:
                s = size * 1.15f;
                drawRegularPolygonFilled(5, s);
                break;
            case Hexagon:
                s = size * 1.15f;
                drawRegularPolygonFilled(6, s);
                break;
            case Cross:
                s = size * 1.1f;
                shapeRenderer.rect(-s, -s / 3.0f, s * 2, (s / 3.0f) * 2);
                shapeRenderer.rect(-s / 3.0f, -s, (s / 3.0f) * 2, s * 2);
                break;
            case Star:
                s = size * 1.35f;
                drawStarFilled(10, s, s / 2.4f);
                break;
            case Square:
            default:
                s = size * 0.95f;
                shapeRenderer.rect(-s, -s, s * 2, s * 2);
                break;
        }
        shapeRenderer.end();

        Gdx.gl.glLineWidth(1.8f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 1);
        switch (shape) {
            case Triangle:
                s = size * 1.1f;
                shapeRenderer.polygon(new float[]{0, -s, -s, s, s, s});
                break;
            case Circle:
                s = size * 1.05f;
                shapeRenderer.circle(0, 0, s, 30);
                break;
            case Diamond:
                s = size * 1.2f;
                shapeRenderer.polygon(new float[]{0, -s, s, 0, 0, s, -s, 0});
                break;
            case Pentagon:
                s = size * 1.15f;
                drawRegularPolygonLine(5, s);
                break;
            case Hexagon:
                s = size * 1.15f;
                drawRegularPolygonLine(6, s);
                break;
            case Cross:
                s = size * 1.1f;
                shapeRenderer.polygon(new float[]{
                        -s/3.0f, -s, s/3.0f, -s, s/3.0f, -s/3.0f, s, -s/3.0f,
                        s, s/3.0f, s/3.0f, s/3.0f, s/3.0f, s, -s/3.0f, s,
                        -s/3.0f, s/3.0f, -s, s/3.0f, -s, -s/3.0f, -s/3.0f, -s/3.0f
                });
                break;
            case Star:
                s = size * 1.35f;
                drawStarLine(10, s, s / 2.4f);
                break;
            case Square:
            default:
                s = size * 0.95f;
                shapeRenderer.polygon(new float[]{-s, -s, s, -s, s, s, -s, s});
                break;
        }
        shapeRenderer.end();

        Gdx.gl.glLineWidth(1.0f);
        shapeRenderer.setTransformMatrix(new Matrix4());
    }

    private void drawRegularPolygonFilled(int sides, float radius) {
        float[] v = new float[sides * 2];
        for (int i = 0; i < sides; i++) {
            float rad = (float) Math.toRadians(i * (360.0f / sides) - 90);
            v[i * 2] = (float) Math.cos(rad) * radius;
            v[i * 2 + 1] = (float) Math.sin(rad) * radius;
        }
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            shapeRenderer.triangle(0, 0, v[i * 2], v[i * 2 + 1], v[next * 2], v[next * 2 + 1]);
        }
    }

    private void drawRegularPolygonLine(int sides, float radius) {
        float[] v = new float[sides * 2];
        for (int i = 0; i < sides; i++) {
            float rad = (float) Math.toRadians(i * (360.0f / sides) - 90);
            v[i * 2] = (float) Math.cos(rad) * radius;
            v[i * 2 + 1] = (float) Math.sin(rad) * radius;
        }
        shapeRenderer.polygon(v);
    }

    private void drawStarFilled(int points, float outerRadius, float innerRadius) {
        float[] v = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float rad = (float) Math.toRadians(i * (360.0f / points) - 90);
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            v[i * 2] = (float) Math.cos(rad) * r;
            v[i * 2 + 1] = (float) Math.sin(rad) * r;
        }
        for (int i = 0; i < points; i++) {
            int next = (i + 1) % points;
            shapeRenderer.triangle(0, 0, v[i * 2], v[i * 2 + 1], v[next * 2], v[next * 2 + 1]);
        }
    }

    private void drawStarLine(int points, float outerRadius, float innerRadius) {
        float[] v = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float rad = (float) Math.toRadians(i * (360.0f / points) - 90);
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            v[i * 2] = (float) Math.cos(rad) * r;
            v[i * 2 + 1] = (float) Math.sin(rad) * r;
        }
        shapeRenderer.polygon(v);
    }

    private void handleMouseClick(float x, float y) {
        Airport clicked = null;
        float minDistance = 0.02f;

        for (Airport airport : presenter.getAirports()) {
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
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

    private void setShapeRendererColor(Color color) {
        switch (color) {
            case Red:
                shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1.0f);
                break;
            case Green:
                shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1.0f);
                break;
            case Blue:
                shapeRenderer.setColor(0.2f, 0.2f, 0.8f, 1.0f);
                break;
            case Black:
            default:
                shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
                break;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        airplaneTexture.dispose();
    }
}