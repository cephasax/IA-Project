package br.ufrn.ia.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class RelabelAndConsensus {
	
	public int[][] relabel (int [][]clusterings){
		int [][] values = new int [clusterings.length][];
		for(int i=0;i<values.length;i++)
			values[i] = clusterings[i].clone();
		
		remapToStartWithZero(values);
		
		
		for (int i = 1; i < values.length; i++) {
			MinimumWeightBipartiteMatching minimumWeightBipartiteMatching = new MinimumWeightBipartiteMatching();
			int[] assignment = minimumWeightBipartiteMatching.evaluate(values[0], values[i]);
			relabel(assignment, values[i]);
		}
		remapToStartWithZero(values);
		return values;
	}

	public int[] consensus(int[][] clusterings) {
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
	private void remapToStartWithZero(int [][]clusterings){
		for (int i = 0; i < clusterings.length; i++) {
			remapToStartWithZero(clusterings[i]);
		}
	}
	

	public void remapToStartWithZero(int [] cluster){
		Hashtable<Integer, Integer> remap = new Hashtable<Integer, Integer>();
		for (int i = 0; i < cluster.length; i++)
			if (!remap.containsKey(cluster[i]))
				remap.put(cluster[i], remap.size());
		for (int i = 0; i < cluster.length; i++)
			cluster[i] = remap.get(cluster[i]);
	}
	
	private int maxIndex (int [] votes){
		int max = 0;
		for(int i=0;i<votes.length;i++){
			if(votes[i]>votes[max])
				max = i;
		}
		return max;
	}

	private void relabel(int[] assignment, int[] clustering) {
		for (int i = 0; i < clustering.length; i++)
			clustering[i] = assignment[clustering[i]];
	}
}
