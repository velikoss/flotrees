package me.velikoss.models;

public interface Command {
    Diff apply(int i, int j, int parameter, int availableEnergy);
}
