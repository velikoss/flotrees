package me.velikoss.models;


import com.google.gson.Gson;
import lombok.*;

import java.util.Random;

import static com.raylib.Raylib.GetRandomValue;
import static java.lang.Math.abs;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Genome {
    public static Genome generate() {
        Genome ret = new Genome();
        for (int i = 0; i < ret.a.length; i++) {
            ret.a[i][0] = GetRandomValue(0, 2);
            for (int j = 1; j < ret.a[i].length; j++) {
                ret.a[i][j] = GetRandomValue(0, 31);
            }
        }
        return ret;
    }

    public static Genome mutate(Genome base) {
        if (GetRandomValue(0, 100) >= 10) return base;

        Genome newGenome = base.clone();

        int place = GetRandomValue(0, 31);
        int what = GetRandomValue(0, 8);
        newGenome.getA()[place][what] = GetRandomValue(0, 31);

        return newGenome;
    }

    int[][] a = new int[32][9];

    public Genome clone() {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(this), Genome.class);
    }
}
