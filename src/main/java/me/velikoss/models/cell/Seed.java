package me.velikoss.models.cell;

import me.velikoss.Main;
import me.velikoss.models.Genome;
import me.velikoss.models.TreeInfo;

import java.awt.*;
import java.util.UUID;

import static java.lang.Math.max;


public class Seed extends Cell {

    public UUID old;

    public Seed(UUID id, int activeGene) {
        super(id);
        this.activeGene = activeGene;
    }

    public final int activeGene;

    public static UUID getID() {
        UUID id = UUID.randomUUID();
        while (id.hashCode() < Color.LIGHT_GRAY.getRGB()) id = UUID.randomUUID();
        return id;
    }

    public Seed(UUID old, int activeGene, Genome generate, int canGiveEnergy) {
        super(getID());

        this.old = old;
        int generation = 1;

        if (old != null && Main.idTree.containsKey(old)) {
            Main.idTree.get(old).setEnergy(Main.idTree.get(old).getEnergy() - canGiveEnergy);
            generation = Main.idTree.get(old).generation + 1;
        }


        this.activeGene = activeGene;
        Main.idTree.put(id, new TreeInfo(canGiveEnergy, 0, generation, 0, 0, Genome.mutate(generate)));
    }

    @Override
    public void tick() {
        if (Main.idTree.containsKey(id)) Main.idTree.get(id).setEnergy(Main.idTree.get(id).getEnergy() - 1);
    }
}
