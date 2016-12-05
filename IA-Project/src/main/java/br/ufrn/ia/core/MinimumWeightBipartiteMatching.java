package br.ufrn.ia.core;

import java.util.HashSet;

public class MinimumWeightBipartiteMatching {

	public int[] evaluate(int[] clusteringA, int[] clusteringB) {

		HashSet<Integer> set;
		set = new HashSet<Integer>();

		for (int i = 0; i < clusteringA.length; i++) {
			set.add(clusteringA[i]);
		}

		int numKa = set.size();
		set = new HashSet<Integer>();

		for (int i = 0; i < clusteringA.length; i++) {
			set.add(clusteringA[i]);
		}

		int numKb = set.size();
		if (numKa != numKb)
			return null;
		int[][] assignmentMatrix = new int[numKa][numKa];

		for (int i = 0; i < numKa; i++) {
			for (int j = 0; j < numKa; j++) {
				assignmentMatrix[i][j] = -1;
			}
		}

		for (int i = 0; i < numKa; i++) {
			boolean[] setI = new boolean[clusteringA.length];

			for (int j = 0; j < setI.length; j++) {
				setI[j] = clusteringA[j] == i;
			}

			for (int j = 0; j < numKa; j++) {
				assignmentMatrix[i][j] = colsValues(j, clusteringB, setI);
			}
		}
		HungarianMethod hungarianMethod = new HungarianMethod();
		int[] consensus = hungarianMethod.evaluate(assignmentMatrix);
		return consensus;
	}

	private int colsValues(int cluster, int[] clustering, boolean[] merge) {

		int sizeMerge = 0;
		for (int i = 0; i < merge.length; i++)
			if (merge[i])
				sizeMerge++;

		int sizeClustering = 0;
		boolean[] setI = new boolean[clustering.length];
		for (int i = 0; i < setI.length; i++) {
			setI[i] = clustering[i] == cluster;
			if (setI[i])
				sizeClustering++;
		}

		int numIntersect = 0;
		for (int i = 0; i < clustering.length; i++)
			if (merge[i] == true && setI[i] == true)
				numIntersect++;

		return (sizeMerge - numIntersect) + (sizeClustering - numIntersect);
	}
}
