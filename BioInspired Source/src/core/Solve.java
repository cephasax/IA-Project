package core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

public class Solve implements Cloneable {

	public static final double pPartitions = 0.9;

	public static final double pEquals = 0.9;

	public Problem problem;

	public int[] cluster;

	public double cost;

	/**
	 * Técnica de randomize de Anne
	 * @param k quantidade de clusters.
	 * @param clusterings clusterings a ser formando o consensus, devem estar de acordo com o rótulos.
	 * @param partitions Probabilidade do valor ser escolhido entre as partições.
	 * @param equals Se o valor for escolhido entre as partições elea probabilidade equals de ser o mesmo valor.
	 */
	public Solve(Problem problem, int k, int[][] clusterings, double partitions, double equals, Random rand) {
		this.problem = problem;
		cluster = new int[clusterings[0].length];
		for (int i = 0; i < cluster.length; i++) {
			if (rand.nextDouble() < partitions) {
				HashSet<Integer> set = new HashSet<Integer>();
				for (int j = 0; j < clusterings.length; j++)
					set.add(clusterings[j][i]);
				if (set.size() == 1) {
					Vector<Integer> others = new Vector<Integer>();
					for (int j = 0; j < k; j++)
						if (j != clusterings[0][i])
							others.add(j);
					cluster[i] = rand.nextDouble() < equals ? clusterings[0][i] : others.get(rand.nextInt(others.size()));
				}
			} else {
				cluster[i] = rand.nextInt(k);
			}
		}
	}
	
	public Solve(Problem problem, int [] cluster) {
		this.problem = problem;
		this.cluster = cluster.clone();
	}

	public Solve(Solve solve) {
		problem = solve.problem;
		cluster = solve.cluster.clone();
		cost = solve.cost;
	}

	public int getNumClusteres() {
		return problem.getNumClusterers();
	}

	public boolean isValid() {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < cluster.length; i++)
			set.add(cluster[i]);
		return set.size() == getNumClusteres();
	}

	public int numClusteres(int clustering) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < cluster.length; i++)
			set.add(cluster[i]);
		return set.size();
	}
	
	public void randomize(Random rand) {
		for (int i = 0; i < cluster.length; i++)
			cluster[i] = rand.nextInt(getNumClusteres());
	}

	public void evaluate() {
		cost = problem.evaluate(cluster);
	}

	public String toString() {
		return String.format(Locale.ENGLISH, "(%6.3f,%s)", cost, Arrays.toString(cluster));
	}
}
