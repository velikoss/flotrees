package me.velikoss.models.cell;

import lombok.Getter;
import me.velikoss.Main;

import java.util.UUID;

@Getter
public class Sprout extends Cell {

    public final int activeGene;

    public Sprout(UUID id, int activeGene) {
        super(id);
        this.activeGene = activeGene;
    }

    @Override
    public void tick() {
        if (Main.idTree.containsKey(id)) Main.idTree.get(id).setEnergy(Main.idTree.get(id).getEnergy() - 7);
    }
}
