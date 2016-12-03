package br.ufrn.ia.core.optimizationMethods;
import java.util.Hashtable;

import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;

public class GeneticAlgorithm extends OptimizationAlgorithm {

	private int epochs;
	private double mutate;
	private double crossover;
	private int[][][] clusterings;
	private Solve bestSolve;

	public GeneticAlgorithm(int[][][] clusterings, int epochs, double mutate, double crossover) {
		this.epochs = epochs;
		this.mutate = mutate;
		this.crossover = crossover;
		this.clusterings = clusterings;
	}

	public void run() {

		Solve[] population = new Solve[clusterings.length * 2];
		for (int i = 0; i < population.length / 2; i++) {
			population[i] = new Solve(clusterings[i]);
		}

		while (epochs-- > 0) {

			for (int i = population.length / 2; i < population.length; i++) {
				Solve parentA = population[Problem.rand.nextInt(population.length / 2)];
				Solve parentB = population[Problem.rand.nextInt(population.length / 2)];

				if (Problem.rand.nextDouble() < crossover)
					population[i] = this.crossover(parentA, parentB);
				else
					population[i] = new Solve(Problem.rand.nextBoolean() ? parentA : parentB);

				population[i] = mutation(population[i], mutate);
			}

			Hashtable<Solve, Double> count = new Hashtable<Solve, Double>();
			for (Solve s : population)
				count.put(s, s.cost);

			quicksort(population, 0, population.length - 1, createHashtableComparator(count));
		}

		bestSolve = population[0];
	}

	public Solve getBestSolve() {
		return bestSolve;
	}
}
