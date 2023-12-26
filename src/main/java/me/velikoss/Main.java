package me.velikoss;

import lombok.SneakyThrows;
import me.velikoss.models.*;
import me.velikoss.models.cell.Cell;
import me.velikoss.models.cell.Leaf;
import me.velikoss.models.cell.Seed;
import me.velikoss.models.cell.Sprout;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.time.Instant;
import java.util.*;

import static com.raylib.Jaylib.*;
import static java.lang.Math.*;

public class Main {
    public static Main instance;

    public static final int MAX_WIDTH = 1000, MAX_HEIGHT = 75, LIGHT_LEVEL = 8;

    public static void main(String[] args) {
        new Main();
    }
    public static HashMap<UUID, TreeInfo> idTree = new HashMap<>();
    public static Cell[][] cells = new Cell[MAX_HEIGHT][MAX_WIDTH];
    public static int[][] energyMap = new int[MAX_HEIGHT][MAX_WIDTH];
    static Queue<Diff> diffs = new LinkedList<>();
    public static int xOffset = 0, diffsCount = 0;
    public static volatile boolean isPaused = false;

    public static void forceSetCell(int y, int x, Cell cell) {
        if(!(y >= 0 && y < MAX_HEIGHT) || !(x >= 0 && x < MAX_WIDTH)) return;

        diffs.offer(() -> {
            if (cells[y][x] != null && idTree.containsKey(cells[y][x].id))
                idTree.get(cells[y][x].id).typesCounter[cells[y][x].getType()]--;
            if (cell != null && idTree.containsKey(cell.id)) {
                if (cell instanceof Seed seed && idTree.containsKey(seed.old)) idTree.get(seed.old).seeds++;
                idTree.get(cell.id).typesCounter[cell.getType()]++;
            }

            cells[y][x] = cell;
        });
    }

    static void killCell(int y, int x) {
        if (cells[y][x] == null) return;
        forceSetCell(y, x, null);
    }

    public static void setCell(int y, int x, Cell cell) {
        if( !(x >= 0 && x < MAX_WIDTH)) {
            setCell(y,((x%MAX_WIDTH)+MAX_WIDTH)%MAX_WIDTH, cell);
            return;
        }
        if(!(y >= 0 && y < MAX_HEIGHT) || cells[y][x] != null) {
            if (cell instanceof Seed seed) idTree.remove(seed.id);
            return;
        }
        forceSetCell(y, x, cell);
    }

    static void moveCell(int y, int x, int dy, int dx) {
        setCell(y+dy, x+dx, cells[y][x]);
        killCell(y, x);
    }

    public static boolean isInitialised = false;
    public static Timer fps = new Timer();
    public static int targetTPS = 60;
    public static int tickCount = 0;
    public static int resetCount = -1;
    public static int tickCountLast = 0;

    MultiValuedMap<Integer, Genome> nextPeppers = new ArrayListValuedHashMap<>();

    final Task runWorld = () -> {
        initEnergyMap();

        if (idTree.values().isEmpty()) {
            resetCount++;
            for (int i = 0; i < 100; i++) {
                setCell(
                        GetRandomValue(MAX_HEIGHT - 20, MAX_HEIGHT - 10),
                        GetRandomValue(10, MAX_WIDTH - 10),
                        new Seed(null, 0,
                                nextPeppers.isEmpty()
                                        ? Genome.generate() :
                                        Genome.mutate(nextPeppers.values().stream().toList().get(i % nextPeppers.size())),
                                300)
                );
            }
            nextPeppers.clear();
        }


        for (int i = 0; i < MAX_HEIGHT; i++) {
            for (int j = 0; j < MAX_WIDTH; j++) {
                if (cells[i][j] == null) continue;

                cells[i][j].tick();

                if (!idTree.containsKey(cells[i][j].id)) {
                    forceSetCell(i, j, null);
                    continue;
                }

                idTree.get(cells[i][j].id).treeHeight = max(MAX_HEIGHT - i, idTree.get(cells[i][j].id).treeHeight);

                if (cells[i][j] instanceof Seed seed) {
                    if (i != MAX_HEIGHT - 1) {
                        idTree.get(seed.id).setAge(0);
                        moveCell(i, j, 1, 0);
                    } else {
                        UUID uuid = cells[i][j].id;

                        if (idTree.containsKey(seed.old)) idTree.get(seed.old).seeds += 3 * max(idTree.get(seed.old).generation, 10) * idTree.get(seed.old).age;

                        Cell newCell = Cell.generate(
                                uuid,
                                idTree.get(uuid).getGenome(),
                                idTree.get(uuid).getGenome().getA()[seed.activeGene][1],
                                0
                        );
                        forceSetCell(i, j, newCell);
                    }
                }
                else if (cells[i][j] instanceof Sprout sprout) {
                    UUID uuid = cells[i][j].id;
                    TreeInfo info = idTree.get(uuid);
                    Genome genome = info.getGenome();

                    int[] activeGene = genome.getA()[sprout.getActiveGene()];
                    int availableEnergy = min(activeGene[5]*10, info.getEnergy());

                    if (!SproutUtils.conditions[activeGene[6] % SproutUtils.conditions.length].apply(i, j, activeGene[7], info.treeHeight, 300 - info.age, info.energy)) continue;

                    SproutUtils.commands[activeGene[8] % SproutUtils.commands.length]
                            .apply(
                                    i,
                                    j,
                                    activeGene[7],
                                    availableEnergy
                            ).diff();
                }
                else if (cells[i][j] instanceof Leaf leaf) {
                    idTree.get(leaf.id).setEnergy(idTree.get(leaf.id).getEnergy() + energyMap[i][j]);
                }
            }
        }

        diffsCount = diffs.size();
        for (Diff diff : diffs) {
            diff.diff();
        }
        diffs.clear();

        Iterator<Map.Entry<UUID, TreeInfo>> a = idTree.entrySet().iterator();
        while (a.hasNext()) {
            Map.Entry<UUID, TreeInfo> entry = a.next();
            TreeInfo value = entry.getValue();
            value.setAge(value.getAge() + 1);

            if (value.isDead()) {
                nextPeppers.put(Arrays.stream(value.getTypesCounter()).sum() + value.seeds * 20, value.getGenome());
                if (nextPeppers.size() > 10) {
                    nextPeppers.remove(nextPeppers.keySet().stream().sorted().iterator().next());
                }
                a.remove();
            }
        }
        tickCount++;
    };
    public Main() {
        instance = this;
        new Thread(Renderer::new).start();
        fps.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tickCountLast = tickCount;
                tickCount = 0;
            }
        }, 1000, 1000);


        while (true) {
            while (isPaused) Thread.onSpinWait();

            if (IsWindowReady()) isInitialised = true;
            if (isInitialised && WindowShouldClose()) return;

            scheduleTaskAtRate(runWorld, (1000f / targetTPS));
        }
    }

    @SneakyThrows
    void scheduleTaskAtRate(Task task, double millis) {
        Instant before = Instant.now().plusMillis((long) ceil(millis));
        task.run();
        while (Instant.now().isBefore(before)) Thread.onSpinWait();
    }

    private static void initEnergyMap() {
        for (int x = 0; x < MAX_WIDTH; x++) {
            int lightLevel = LIGHT_LEVEL, lightPower = 3;
            for (int y = 0; y < MAX_HEIGHT; y++) {
                energyMap[y][x] = max(0,lightLevel*lightPower);

                if(cells[y][x] == null){
                    lightPower = min(lightPower + 1, 3);
                    continue;
                }

                lightPower--;
                lightLevel = max(0, lightLevel - 1);
            }
        }
    }
}