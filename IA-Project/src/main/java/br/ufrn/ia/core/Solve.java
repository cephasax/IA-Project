package br.ufrn.ia.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

public class Solve implements Cloneable {

	public final double pPartitions = 0.9;
	public final double pEquals = 0.9;
	public Problem problem;
	public int[] cluster;
	public double cost;

	public Solve(Problem problem){
		this.problem = problem;
	}
	
	/**
	 * Técnica de randomize de Anne
	 * 
	 * @param k
	 *            quantidade de clusters.
	 * @param clusterings
	 *            clusterings a ser formando o consensus, devem estar de acordo
	 *            com o rótulos.
	 * @param partitions
	 *            Probabilidade do valor ser escolhido entre as partições.
	 * @param equals
	 *            Se o valor for escolhido entre as partições elea probabilidade
	 *            equals de ser o mesmo valor.
	 */
	public Solve(int k, int[][] clusterings, double partitions, double equals) {
		cluster = new int[clusterings[0].length];
		for (int i = 0; i < cluster.length; i++) {
			Random r = new Random();
			if (r.nextDouble() < partitions) {
				HashSet<Integer> set = new HashSet<Integer>();
				for (int j = 0; j < clusterings.length; j++)
					set.add(clusterings[j][i]);
				if (set.size() == 1) {
					Vector<Integer> others = new Vector<Integer>();
					for (int j = 0; j < k; j++)
						if (j != clusterings[0][i])
							others.add(j);
					cluster[i] = r.nextDouble() < equals ? clusterings[0][i]
							: others.get(r.nextInt(others.size()));
				}
			} else {
				cluster[i] = r.nextInt(k);
			}
		}
	}

	public Solve(int[] cluster) {
		this.cluster = cluster.clone();
	}

	public Solve(Solve solve) {
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

	public void randomize() {
		for (int i = 0; i < cluster.length; i++){
			Random r = new Random();
			cluster[i] = r.nextInt(getNumClusteres());
		}
	}

	public int numClusteres(int clustering) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < cluster.length; i++)
			set.add(cluster[i]);
		return set.size();
	}

	public void evaluate() {
		cost = problem.evaluate(cluster);
	}

	public String toString() {
		return String.format(Locale.ENGLISH, "(%6.3f,%s)", cost, Arrays.toString(cluster));
	}
}
