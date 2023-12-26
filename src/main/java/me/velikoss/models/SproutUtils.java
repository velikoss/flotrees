package me.velikoss.models;

import me.velikoss.models.cell.Cell;
import me.velikoss.models.cell.Leaf;
import me.velikoss.models.cell.Sprout;

import static me.velikoss.Main.*;

public class SproutUtils {

    public final static Condition[] conditions = new Condition[] {
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> (MAX_HEIGHT - i) > parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> (MAX_HEIGHT - i) < parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> (MAX_HEIGHT - i) == parameter,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight > parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight < parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight == parameter,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> liveLeft > parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> liveLeft < parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> liveLeft == parameter,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> energyLeft > parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> energyLeft < parameter,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> energyLeft == parameter,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight - parameter > (MAX_HEIGHT - i),
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight - parameter < (MAX_HEIGHT - i),
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> treeHeight - parameter == (MAX_HEIGHT - i),

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,

            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
            (i, j, parameter, treeHeight, liveLeft, energyLeft) -> true,
    };

    public final static Command[] commands = new Command[] {
            (i, j, parameter, availableEnergy) -> (Diff) () -> {
                if (cells[i][j] != null && idTree.containsKey(cells[i][j].id)) idTree.get(cells[i][j].id).age += 2;
            },
            (i, j, parameter, availableEnergy) -> (Diff) () -> {
                Cell cell = cells[i][j];
                if (cell == null || !idTree.containsKey(cells[i][j].id)) return;
                TreeInfo info = idTree.get(cells[i][j].id);
                forceSetCell(i, j, Cell.generate(
                    cell.id,
                    info.getGenome(),
                    parameter,
                    availableEnergy
                ));
            },
            (i, j, parameter, availableEnergy) -> (Diff) () -> {
                Sprout sprout = (Sprout) cells[i][j];
                if (sprout == null || !idTree.containsKey(cells[i][j].id)) return;
                TreeInfo info = idTree.get(cells[i][j].id);
                int[] activeGene = info.genome.getA()[sprout.getActiveGene()];

                setCell(i - 1, j, Cell.generate(
                        sprout.id,
                        info.getGenome(),
                        activeGene[1],
                        availableEnergy
                ));

                setCell(i, j - 1, Cell.generate(
                        sprout.id,
                        info.getGenome(),
                        activeGene[2],
                        availableEnergy
                ));

                setCell(i, j + 1, Cell.generate(
                        sprout.id,
                        info.getGenome(),
                        activeGene[3],
                        availableEnergy
                ));

                setCell(i + 1, j, Cell.generate(
                        sprout.id,
                        info.getGenome(),
                        activeGene[4],
                        availableEnergy
                ));

                forceSetCell(i, j, new Leaf(sprout.id));
            },
    };

}
