package me.velikoss.models.cell;

import me.velikoss.Main;

import java.util.UUID;

public class Leaf extends Cell {

    public Leaf(UUID id) {
        super(id);
    }

    @Override
    public void tick() {
        if (Main.idTree.containsKey(id)) Main.idTree.get(id).setEnergy(Main.idTree.get(id).getEnergy() - 11);
    }
}