package br.ufrn.ia.core;

import java.util.HashSet;
import java.util.Hashtable;

public class RelabelAndConsensus {

	public int[] consensus(int[][] clusterings) {
		// clusters start with 0
		for (int i = 0; i < clusterings.length; i++) {
			Hashtable<Integer, Integer> remap = new Hashtable<Integer, Integer>();
			for (int j = 0; j < clusterings[i].length; j++)
				if (!remap.containsKey(clusterings[i][j]))
					remap.put(clusterings[i][j], remap.size());
			for (int j = 0; j < clusterings[i].length; j++)
				clusterings[i][j] = remap.get(clusterings[i][j]);
		}

		for (int i = 1; i < clusterings.length; i++) {
			
			MinimumWeightBipartiteMatching mwbm = new MinimumWeightBipartiteMatching();
			
			int[] assignment = mwbm.evaluate(clusterings[0], clusterings[i]);
			relabel(assignment, clusterings[i]);
		}

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
