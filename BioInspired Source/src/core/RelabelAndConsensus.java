package core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class RelabelAndConsensus {

	public static void main(String[] args) {

		int[][] matrix = new int[][] { { 1, 1, 2, 2, 0, 0 }, { 2, 2, 0, 0, 1, 1 }, { 2, 2, 2, 0, 1, 1 } };

		int[] consensus = RelabelAndConsensus.consensus(matrix);

		System.out.println(Arrays.toString(consensus));
	}
	
	public static void relabel (int [][]clusterings){
		remapToStartWithZero(clusterings);
		for (int i = 1; i < clusterings.length; i++) {
			int[] assignment = MinimumWeightBipartiteMatching.evaluate(clusterings[0], clusterings[i]);
			relabel(assignment, clusterings[i]);
		}
		remapToStartWithZero(clusterings);
	}

	public static int[] consensus(int[][] clusterings) {
		relabel(clusterings);

		HashSet<Integer> set = new HashSet<Integer>();
		for(int i=0;i<clusterings[0].length;i++)
			set.add(clusterings[0][i]);

		int [] consensus = new int [clusterings[0].length];
		for(int i=0;i<clusterings[0].length;i++){
			int [] voting = new int [set.size()];
			for(int j=0;j<clusterings.length;j++)
				voting[clusterings[j][i]]++;
			consensus[i] = maxIndex(voting);
		}
		
		return consensus;
	}
	
	/**
	 * Remapeia cada clustering para iniciar com o cluster 0 até o cluster k-1
	 * @param clusterings
	 */
	private static void remapToStartWithZero(int [][]clusterings){
		for (int i = 0; i < clusterings.length; i++) {
			remapToStartWithZero(clusterings[i]);
		}
	}
	
	public static void remapToStartWithZero(int [] cluster){
		Hashtable<Integer, Integer> remap = new Hashtable<Integer, Integer>();
		for (int i = 0; i < cluster.length; i++)
			if (!remap.containsKey(cluster[i]))
				remap.put(cluster[i], remap.size());
		for (int i = 0; i < cluster.length; i++)
			cluster[i] = remap.get(cluster[i]);
	}
	
	private static int maxIndex (int [] votes){
		int max = 0;
		for(int i=0;i<votes.length;i++){
			if(votes[i]>votes[max])
				max = i;
		}
		return max;
	}

	private static void relabel(int[] assignment, int[] clustering) {
		for (int i = 0; i < clustering.length; i++)
			clustering[i] = assignment[clustering[i]];
	}
}
