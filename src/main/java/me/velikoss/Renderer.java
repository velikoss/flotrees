package me.velikoss;

import com.raylib.Raylib;
import me.velikoss.models.Diff;
import me.velikoss.models.Genome;
import me.velikoss.models.TreeInfo;
import me.velikoss.models.cell.Cell;
import me.velikoss.models.cell.Leaf;
import me.velikoss.models.cell.Seed;
import me.velikoss.models.cell.Sprout;

import java.time.Instant;
import java.util.*;

import static com.raylib.Jaylib.*;
import static com.raylib.Jaylib.RED;
import static com.raylib.Raylib.*;
import static java.lang.Math.*;
import static java.lang.System.*;
import static me.velikoss.Main.*;

public class Renderer {

    int WINDOW_WIDTH = 1920, WINDOW_HEIGHT = MAX_HEIGHT * 8;
    UUID selected = new UUID(0,0);
    public Renderer() {
        InitWindow(WINDOW_WIDTH, WINDOW_HEIGHT + 80, "Trees");

        int FPS = 60;
        SetTargetFPS(FPS);

        int fps = 0, lastFPS = -1;

        Instant gcT = Instant.now().plusSeconds(1);

        boolean willRender = true;

        while (!WindowShouldClose()) {
            switch (GetKeyPressed()) {
                case KEY_SPACE -> {
                    isPaused = !isPaused;
                }
                case KEY_R -> {
                    willRender = !willRender;
                }
            }
            if (IsKeyDown(KEY_DOWN)) targetTPS = max(1, targetTPS - (IsKeyDown(KEY_LEFT_SHIFT) ? 10 : 1));
            if (IsKeyDown(KEY_UP)) targetTPS += (IsKeyDown(KEY_LEFT_SHIFT) ? 10 : 1);
            if (IsKeyDown(KEY_LEFT)) xOffset = floorMod(xOffset - 2 * (IsKeyDown(KEY_LEFT_SHIFT) ? 10 : 1), (MAX_WIDTH - WINDOW_WIDTH / 8));
            if (IsKeyDown(KEY_RIGHT)) xOffset = (xOffset + 2 * (IsKeyDown(KEY_LEFT_SHIFT) ? 10 : 1)) % (MAX_WIDTH - WINDOW_WIDTH / 8);
            if (IsKeyDown(KEY_RIGHT)) xOffset = (xOffset + 2 * (IsKeyDown(KEY_LEFT_SHIFT) ? 10 : 1)) % (MAX_WIDTH - WINDOW_WIDTH / 8);
            if (IsKeyDown(KEY_BACKSPACE)) {
                idTree.clear();
                diffs.clear();
            }
            if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                Raylib.Vector2 vector2 = GetMousePosition();
                int x = (int) vector2.x() / 8, y = (int) vector2.y() / 8;
                Cell cell = cells[y][x + xOffset];
                if (cell != null) {
                    if (isPaused) selected = cell.id;
                    TreeInfo info = idTree.get(cell.id);
                    int activeGene = -1;
                    if (cell instanceof Seed seed) activeGene = seed.activeGene;
                    if (cell instanceof Sprout seed) activeGene = seed.activeGene;
                    if (info != null) TraceLog(LOG_INFO,
                            String.format(
                                    "%d %s %d",
                                    info.getEnergy(), cell.getClass().getSimpleName(), activeGene
                            )
                    );
                }
            }

            BeginDrawing();

            ClearBackground(BLACK);

            List<Pair<Integer, Integer>> selectedRender = new ArrayList<>();
            int left = Integer.MAX_VALUE, up = 0, right = 0;

            if (willRender) {
                for (int i = 0; i < MAX_HEIGHT; i++) {
                    for (int j = xOffset; j < min(Main.instance.xOffset + (WINDOW_WIDTH / 8), MAX_WIDTH); j++) {
                        Cell cell = cells[i][j];

                        if (cell == null) continue;

                        if (isPaused && cell.id == selected) {
                            if (i + 1 < MAX_HEIGHT && cells[i + 1][j] == null) {
                                DrawRectangle(
                                        j * 8,
                                        (i + 1) * 8,
                                        8,
                                        8,
                                        WHITE
                                );
                            }
                            if (j - 1 >= 0 && cells[i][j - 1] == null) {
                                DrawRectangle(
                                        (j - 1 - xOffset) * 8,
                                        i * 8,
                                        8,
                                        8,
                                        WHITE
                                );
                            }
                            if (i - 1 >= 0 && cells[i - 1][j] == null) {
                                DrawRectangle(
                                        (j - xOffset) * 8,
                                        (i - 1) * 8,
                                        8,
                                        8,
                                        WHITE
                                );
                            }
                            if (j + 1 < MAX_WIDTH && cells[i][j + 1] == null) {
                                DrawRectangle(
                                        (j - xOffset + 1) * 8,
                                        i * 8,
                                        8,
                                        8,
                                        WHITE
                                );
                            }
                        }

                        if (cell instanceof Sprout) {
                            DrawRectangle(
                                    (j - xOffset) * 8,
                                    i * 8,
                                    8,
                                    8,
                                    WHITE
                            );
                        }
                        else if (cell instanceof Seed && isPaused) {
                            DrawRectangle(
                                    (j - xOffset) * 8,
                                    i * 8,
                                    8,
                                    8,
                                    fps / 6 % 2 == 0 ? BLACK : ColorAlpha(GetColor((int) cell.id.getLeastSignificantBits()), 1.0f)
                            );
                        } else {
                            DrawRectangle(
                                    (j - xOffset) * 8,
                                    i * 8,
                                    8,
                                    8,
                                    ColorAlpha(GetColor((int) cell.id.getLeastSignificantBits()), 1.0f)
                            );
                        }
                        // DrawText(String.valueOf(energyMap[i][j]), j * 8, i * 8, 4, WHITE);
                    }
                }

                DrawRectangle((WINDOW_WIDTH - MAX_WIDTH) / 2, 0, MAX_WIDTH, MAX_HEIGHT, ColorAlpha(GRAY, 0.45f));
                DrawRectangle((WINDOW_WIDTH - MAX_WIDTH) / 2 + xOffset, 0, WINDOW_WIDTH / 8, MAX_HEIGHT, ColorAlpha(GRAY, 0.45f));

                int xRenderOffset = (WINDOW_WIDTH - MAX_WIDTH) / 2;

                for (int i = 0; i < MAX_HEIGHT; i++) {
                    for (int j = 0; j < MAX_WIDTH; j++) {
                        Cell cell = cells[i][j];
                        if (cell == null) continue;

                        if (cell.id == selected && isPaused) {

                            left = min (j, left);
                            up = max (MAX_HEIGHT - i, up);
                            right = max (j, right);

                            selectedRender.add(new Pair<>(MAX_HEIGHT - i, j));

                            if (i + 1 < MAX_HEIGHT && cells[i + 1][j] == null) {
                                DrawPixel(
                                        xRenderOffset + j,
                                        i+1,
                                        WHITE
                                );
                            }
                            if (j - 1 >= 0 && cells[i][j - 1] == null) {
                                DrawPixel(
                                        xRenderOffset + j - 1,
                                        i,
                                        WHITE
                                );
                            }
                            if (i - 1 >= 0 && cells[i - 1][j] == null) {
                                DrawPixel(
                                        xRenderOffset + j,
                                        i - 1,
                                        WHITE
                                );
                            }
                            if (j + 1 < MAX_WIDTH && cells[i][j + 1] == null) {
                                DrawPixel(
                                        xRenderOffset + j + 1,
                                        i,
                                        WHITE
                                );
                            }
                        }

                        if (cell instanceof Sprout) {
                            DrawPixel(
                                    xRenderOffset + j,
                                    i,
                                    WHITE
                            );
                        }
                        else if (cell instanceof Seed && isPaused) {
                            DrawPixel(
                                    xRenderOffset + j,
                                    i,
                                    fps / 6 % 2 == 0 ? BLACK : ColorAlpha(GetColor((int) cell.id.getLeastSignificantBits()), 1.0f)
                            );
                        } else {
                            DrawPixel(
                                    xRenderOffset + j,
                                    i,
                                    ColorAlpha(GetColor((int) cell.id.getLeastSignificantBits()), 1.0f)
                            );
                        }
                    }
                }
            }

            if (isPaused) {
                int previewWindowWidth = 360, previewWindowHeight = 420;
                int outsideOffsetLeft = 20, outsideOffsetTop = (WINDOW_HEIGHT - previewWindowHeight) / 2;
                int insideOffsetHorizontal = 20, insideOffsetTop = 20;

                DrawRectangle(outsideOffsetLeft, outsideOffsetTop, previewWindowWidth, previewWindowHeight, ColorAlpha(GRAY, 0.75f));
                int treeWidth = (right - left);

                int size = (int) ((min((float) (previewWindowWidth - insideOffsetHorizontal * 2) / treeWidth,
                        ((float) (previewWindowHeight / 2 - insideOffsetTop * 2) / up)))); // nobody thinks you'll understand that

                int previewWidth = size * treeWidth;
                int previewHeight = size * up;

                int treeHorizontalOffset = (previewWindowWidth - previewWidth) / 2;
                int treeVerticalOffset = outsideOffsetTop + previewWindowHeight / 2;

                for (Pair<Integer, Integer> cords: selectedRender) {
                    DrawRectangle(
                            (outsideOffsetLeft + treeHorizontalOffset) + (cords.b - left) * size - size / 2,
                            (treeVerticalOffset) - (cords.a) * size - insideOffsetTop,
                            size,
                            size,
                            ColorAlpha(GetColor((int) selected.getLeastSignificantBits()), 1.0f)
                    );

                    String id = selected.toString();

                    TreeInfo info = idTree.get(selected);

                    float ratio = (float) (previewWindowWidth - insideOffsetHorizontal * 2) / MeasureText(id, 14); // nobody thinks you'll understand that
                    int fontSize = (int) (14 * ratio);
                    fontSize += fontSize % 2;

                    DrawText(id, outsideOffsetLeft + previewWindowWidth / 2 - MeasureText(id, fontSize) / 2, treeVerticalOffset, fontSize, WHITE);

                    String age = String.format("Age: %d", info == null ? -1 : info.age);

                    int lastFontSize = fontSize;
                    ratio = (float) (previewWindowWidth - insideOffsetHorizontal * 2) / MeasureText(age, 14); // nobody thinks you'll understand that
                    fontSize = (int) (14 * ratio);
                    fontSize += fontSize % 2;

                    DrawText(age, outsideOffsetLeft + previewWindowWidth / 2 - MeasureText(age, fontSize) / 2, treeVerticalOffset + lastFontSize + 10, fontSize, WHITE);

                    String energy = String.format("Energy: %d", info == null ? -1 : info.energy);

                    lastFontSize += fontSize;
                    ratio = (float) (previewWindowWidth - insideOffsetHorizontal * 2) / MeasureText(energy, 14); // nobody thinks you'll understand that
                    fontSize = (int) (14 * ratio);
                    fontSize += fontSize % 2;

                    DrawText(energy, outsideOffsetLeft + previewWindowWidth / 2 - MeasureText(energy, fontSize) / 2, treeVerticalOffset + lastFontSize + 10, fontSize, WHITE);
                }
            }

            if (isPaused) DrawText("PAUSED", (WINDOW_WIDTH - MeasureText("PAUSED", 40)) / 2, WINDOW_HEIGHT / 2 - 20, 40, ColorAlpha(RED, 0.5f));

            DrawText(String.format("Trees count: %d", idTree.values().size()), 0, 0, 16, GREEN);
            DrawText(String.format("Diffs per tick: %d", diffsCount), 0, 20, 16, GREEN);
            DrawText(String.format("Reset count: %d", resetCount), 0, 40, 16, GREEN);

            DrawText("Renderer FPS (R/T): ", WINDOW_WIDTH - MeasureText("Renderer FPS (R/T): 9999/9999", 16), 0, 16, RED);
            String targetFpsCounter = String.format("%d/%d", GetFPS(), FPS);
            DrawText(targetFpsCounter, WINDOW_WIDTH - MeasureText(targetFpsCounter, 16), 0, 16, RED);

            DrawText("Simulation TPS (R/T): ", WINDOW_WIDTH - MeasureText("Simulation TPS (R/T): 9999/9999", 16), 20, 16, RED);
            String tpsCounter = String.format("%d/%d", tickCountLast, targetTPS);
            DrawText(tpsCounter, WINDOW_WIDTH - MeasureText(tpsCounter, 16), 20, 16, RED);

            DrawRectangleGradientH(0,WINDOW_HEIGHT, WINDOW_WIDTH,80, ColorFromHSV(0f,0f,0f), ColorFromHSV(0f,0f,0.27f));

            EndDrawing();

            fps ++;
            Instant now = Instant.now();
            if (gcT.isBefore(now)) {
                gc();
                gcT = now.plusSeconds(1);
            }
        }

        CloseWindow();
    }

}
