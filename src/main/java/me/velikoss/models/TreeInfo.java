package me.velikoss.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeInfo {
    public int energy, age, generation;
    public final int[] typesCounter = new int[3];
    public int treeHeight, seeds;
    Genome genome;
    public boolean isDead() {
        return energy <= 0 || age > 300 || (typesCounter[0] + typesCounter[1]) <= 0;
    }
}
