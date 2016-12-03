package core;

import java.util.Arrays;
import java.util.HashSet;

public class MinimumWeightBipartiteMatching {

	public static void main(String[] args) {

		int[][] matrix = new int[][] { { 1, 1, 2, 2, 3, 3 }, { 3, 3, 1, 1, 2, 2 }, { 2, 2, 2, 3, 1, 1 } };

		matrix = new int[][] { { 1, 2, 2, 2, 3, 3 }, { 1, 1, 2, 2, 3, 3 }, { 1, 1, 2, 2, 3, 3 }
		};

		//int[][] matrix = new int[][] { { 1, 3, 2 }, { 1, 3, 2 }, { 2, 1, 2 }, { 2, 1, 3 }, { 3, 2, 1 }, { 3, 2, 1 } };

		//int [][] matrix = new int [][]{{3,4,8,7,8},{2,5,3,2,6},{7,9,1,8,3},{5,3,4,6,6},{8,9,7,5,8}};

		//int [][] matrix = new int [][]{{3,2,7,5,8},{4,5,9,3,9},{8,3,1,4,7},{7,2,8,6,5},{8,6,3,6,8}};

		//int [][] matrix = new int [] [] {{90,35,125,45},{75,85,95,110},{75,55,90,95},{80,65,105,115}};

		//int [][] matrix = new int [][]{{73,4,80,95,60},{79,42,33,99,41},{84,30,12,33,57},{36,30,66,29,23},{33,27,40,67,83}};

		//matrix = new int[][] { { 58, 0, 42, 93, 38 }, { 57, 27, 24, 11, 19 }, { 16, 42, 97, 84, 24 }, {4,14,13,40,23}, {6,38,17,89,31}};

		int[] consensus = evaluate(new int[] { 0, 0, 1, 2, 2, 3, 3, 3, 1 }, new int[] { 1, 1, 2, 0, 0, 3, 3, 3, 2 });

		//[2, 0, 1, 3]

		//2 3 1 4
		
		//[1, 2, 0, 3]
		//{0, 0, 1, 2, 2, 3, 3, 3, 1}
		//{1, 1, 2, 0, 0, 3, 3, 3, 2}

		System.out.println(Arrays.toString(consensus));
	}

	public static int[] evaluate(int[] clusteringA, int[] clusteringB) {

		HashSet<Integer> set;

		set = new HashSet<Integer>();
		for (int i = 0; i < clusteringA.length; i++)
			set.add(clusteringA[i]);
		int numKa = set.size();

		set = new HashSet<Integer>();
		for (int i = 0; i < clusteringA.length; i++)
			set.add(clusteringA[i]);
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
			for (int j = 0; j < setI.length; j++)
				setI[j] = clusteringA[j] == i;

			for (int j = 0; j < numKa; j++) {
				assignmentMatrix[i][j] = colsValues(j, clusteringB, setI);
			}
		}
		int[] consensus = HungarianMethod.evaluate(assignmentMatrix);

		return consensus;
	}

	private static int colsValues(int cluster, int[] clustering, boolean[] merge) {

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
