package me.velikoss.models.cell;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.velikoss.Main;
import me.velikoss.models.Genome;

import java.util.Map;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
public abstract class Cell {
    public UUID id;
    public abstract void tick();

    public final static Map<String, Integer> types = Map.of (
            "Seed",                             0,
            "Sprout",                           1,
            "Leaf",                             2
    );

    public static Cell generate(UUID id, Genome genome, int activeGene, int canGiveEnergy) {
        switch (genome.getA()[activeGene][0]) {
            case 0 -> { // SEED
                return new Seed(id, activeGene, genome, canGiveEnergy);
            }
            case 1 -> { // SPROUT
                return new Sprout(id, activeGene);
            }
            case 2 -> { // LEAF
                return new Leaf(id);
            }
        }
        return null;
    }

    public int getType() {
        return types.get(
                this.getClass().getSimpleName()
        );
    }
}




