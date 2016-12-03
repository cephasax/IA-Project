package br.ufrn.ia.core.optimizationMethods;

import java.util.Arrays;

import br.ufrn.ia.core.Solve;
import br.ufrn.ia.naturalDomain.Ant;

public class AntColonyOptimization extends OptimizationAlgorithm {

	private int epochs;
	private double alpha;
	private double beta;
	private double ro;
	private int[][][] clusterings;
	private double[][] distance;
	private Solve bestSolve;

	public AntColonyOptimization(int[][][] clusterings, int epochs, double alpha, double beta, double ro,
			double[][] distance) {
		this.epochs = epochs;
		this.clusterings = clusterings;
		this.alpha = alpha;
		this.beta = beta;
		this.ro = ro;
		this.distance = distance;
	}

	public void run() {

		double[][] pheromone = new double[distance.length][distance.length];
		for (int i = 0; i < pheromone.length; i++)
			Arrays.fill(pheromone[i], 1.0);

		Ant[] ants = new Ant[clusterings.length];
		for (int i = 0; i < ants.length; i++)
			ants[i] = new Ant(clusterings[i]);

		while (epochs-- > 0) {

			for (int i = 0; i < ants.length; i++)
				ants[i].build(pheromone, alpha, beta, distance);

			for (int i = 0; i < pheromone.length; i++) {
				for (int j = 0; j < pheromone.length; j++) {
					double delta = 0;
					for (int k = 0; k < ants.length; k++)
						delta += ants[k].update[i][j];
					pheromone[i][j] = (1 - ro) * pheromone[i][j] + delta;
				}
			}
			for (int i = 0; i < ants.length; i++)
				if (bestSolve == null || ants[i].solve.cost < bestSolve.cost)
					bestSolve = new Solve(ants[i].solve);
		}
	}

	public Solve getBestSolve() {
		return bestSolve;
	}
}
