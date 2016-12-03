package br.ufrn.ia.core;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import br.ufrn.ia.utils.ARFF;
import weka.core.Instances;

public class Problem {

	private int k;
	private int[] clustering;
	private Fitness fitness;
	private Instances instances;
	public static Random rand = new Random(0);
	
	public Problem(ARFF base, Fitness fitness, int k) {
		this.k = k;
		this.fitness = fitness;
		try {
			instances = new Instances(new FileReader(base.location));
			instances.setClassIndex(instances.numAttributes() - 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNumClusterers() {
		return k;
	}

	public int[] getConsensus() {
		return clustering;
	}

	public Instances getInstances() {
		return instances;
	}

	public double evaluate(int[][] clusterings) {
		boolean equals = true; // the operators can reduce k
		for (int i = 0; i < clusterings.length; i++)
			equals = equals && numClusters(clusterings[i]) == k;

		double fit = Double.MAX_VALUE;
		if (!equals) {
			clustering = new int[clusterings[0].length];
		} else {
			RelabelAndConsensus rc = new RelabelAndConsensus();
			clustering = rc.consensus(clusterings);
			int consensusK = numClusters(clustering);
			if (consensusK == k)
				fit = fitness.evaluate(instances, clustering);
		}

		return fit;
	}

	private int numClusters(int[] clustering) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < clustering.length; i++)
			set.add(clustering[i]);
		return set.size();
	}
}
