package core;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import weka.core.Instances;

public class Problem {

	private int k;

	private Fitness fitness;

	private Instances instances;

	public Problem(Database base, Fitness fitness, int k) {
		this.k = k;
		this.fitness = fitness;
		try {
			instances = new Instances(new FileReader(base.getLocation()));
			instances.setClassIndex(instances.numAttributes() - 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNumClusterers() {
		return k;
	}

	public Instances getInstances() {
		return instances;
	}

	public double evaluate(int[] cluster) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < cluster.length; i++)
			set.add(cluster[i]);

		double fit = set.size() != k ? Double.POSITIVE_INFINITY : fitness.evaluate(instances, cluster);
		return fit;
	}

	public Object getFitness() {
		return fitness;
	}
}
