package br.ufrn.ia.core;

import java.util.Arrays;
import java.util.Vector;

public class HierarquicalDivisiveClustering {

	private int[] kClustering;

	public HierarquicalDivisiveClustering(double[][] matrix) {
		int[] clustering;

		clustering = new int[matrix.length];
		for (int i = 0; i < clustering.length; i++)
			clustering[i] = i + 1;

		Vector<Integer> setMap = new Vector<Integer>();
		for (int i = 0; i < clustering.length; i++)
			setMap.add(i);

		for (int numClustering = clustering.length; numClustering > 1; numClustering--) {
			boolean valid = false;
			int maxIndexI = 0;
			int maxIndexJ = 0;
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < i; j++) {
					if (matrix[i][j] < matrix[maxIndexI][maxIndexJ] || !valid) {
						maxIndexI = i;
						maxIndexJ = j;
						valid = true;
					}
				}
			}

			for (int i = 0; i < matrix.length; i++) {
				if (i != maxIndexI) {
					matrix[maxIndexJ][i] = Math.min(matrix[maxIndexJ][i], matrix[maxIndexI][i]);
					matrix[i][maxIndexJ] = matrix[maxIndexJ][i];
				}
				matrix[i][maxIndexJ] = 100;
				matrix[maxIndexJ][i] = 100;
			}
			Util util = new Util();
			util.printMatrix(matrix);

			if (clustering[maxIndexI] < clustering[maxIndexJ]) {
				int aux = maxIndexJ;
				maxIndexJ = maxIndexI;
				maxIndexI = aux;
			}

			System.out.println(String.format("Turn %5d in %5d", clustering[maxIndexI], clustering[maxIndexJ]) + " "
					+ Arrays.toString(clustering));

			int jvalue = clustering[maxIndexI];
			for (int i = 0; i < clustering.length; i++) {
				if (clustering[i] == jvalue) {
					clustering[i] = clustering[maxIndexJ];
				}
			}
		}
	}

	public int[] getClustering() {
		return kClustering;
	}
}
