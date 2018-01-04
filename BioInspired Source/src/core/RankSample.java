package core;

import java.util.Arrays;
import java.util.HashMap;

public class RankSample {

    public static double[][] alignedRankSample(double[][] var) {
        double[] vector = new double[var.length * var[0].length];
        for (int i = 0; i < var.length; i++) {
            System.arraycopy(var[i], 0, vector, i * var[0].length, var[0].length);
        }
        double[][] finalRank = new double[var.length][var[0].length];
        double[] rank = rankSample(vector);
        for (int i = 0; i < finalRank.length; i++) {
            for (int j = 0; j < finalRank[0].length; j++) {
                finalRank[i][j] = rank[i * finalRank[0].length + j];
            }
        }
        return finalRank;
    }

    public static double[] rankSample(double[] var) {
        double[] values = var.clone();
        Arrays.sort(values);

        HashMap<Double, Integer> ties = new HashMap<Double, Integer>();
        for (int i = 0; i < values.length; i++) {
            int count = 0;
            for (int j = 0; j < values.length; j++) {
                if (values[i] == values[j]) {
                    count++;
                }
            }
            ties.put(values[i], count);
        }

        int currentRank = 1;
        HashMap<Double, Double> ranks = new HashMap<Double, Double>();
        for (int i = 0; i < values.length; i++) {
            if (!ranks.containsKey(values[i])) {
                int sum = 0;
                for (int j = 0; j < ties.get(values[i]); j++) {
                    sum += currentRank + j;
                }
                ranks.put(values[i], sum / (double) ties.get(values[i]));
            }
            currentRank++;
        }

        double[] rank = new double[values.length];
        for (int i = 0; i < var.length; i++) {
            rank[i] = ranks.get(var[i]);
        }
        return rank;
    }
}
