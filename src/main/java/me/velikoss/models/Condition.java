package me.velikoss.models;

import java.util.UUID;

public interface Condition {
    boolean apply(int i, int j, int parameter, int treeHeight, int liveLeft, int energyLeft);
}
