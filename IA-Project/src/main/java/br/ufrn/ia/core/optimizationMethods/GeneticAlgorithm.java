package br.ufrn.ia.core.optimizationMethods;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;

import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Solve;

public class GeneticAlgorithm extends OptimizationAlgorithm {

	private int epochs;
	private double mutate;
	private double crossover;
	private Solve bestSolve;

	/**
	 * Executa o algoritmo gen�tico para os par�metros informados.
	 * @see GeneticAlgorithm#getBestSolve() para recuperar a solu��o ap�s a execu��o de {@link GeneticAlgorithm#run()}
	 * 
	 * @param start Vetor de solu��es iniciais. O tamanho do vetor determina o tamanho da popula��o.
	 * @param epochs Quantidade de itera��es do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param mutate Taxa de muta��o. 0<=mutate<=1. Geralmente 0.1.
	 * @param crossover Taxa de cruzamento. 0<=crossover<=1. Geralmente 0.9.
	 */
	public GeneticAlgorithm(int epochs, double mutate, double crossover) {
		this.epochs = epochs;
		this.mutate = mutate;
		this.crossover = crossover;
	}

	public void run() {

		Solve[] population = new Solve[this.population.length * 2];
		for (int i = 0; i < population.length / 2; i++) {
			population[i] = new Solve(this.population[i]);
		}
		bestSolve = new Solve(population[0]);

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = population.length / 2; i < population.length; i++) {
				Solve parentA = roulette(population, population.length / 2);
				Solve parentB = roulette(population, population.length / 2);
				
				Random r = new Random();
				if (r.nextDouble() < crossover)
					population[i] = this.crossover(parentA, parentB);
				else
					population[i] = new Solve(r.nextBoolean() ? parentA : parentB);

				population[i] = mutation(population[i], mutate);
			}

			Hashtable<Solve, Double> count = new Hashtable<Solve, Double>();
			for (Solve s : population)
				count.put(s, s.cost);

			quicksort(population, 0, population.length - 1, createHashtableComparator(count));

			if (population[0].cost < bestSolve.cost) {
				bestSolve = new Solve(population[0]);
				stepsUpdate = 0;
			}
		}
	}

	public Solve getBestSolve() {
		return new Solve(bestSolve);
	}

	public Solve roulette(Solve[] population, int popSize) {
		double sum = 0;
		double maxValue = population[0].cost;
		for (int i = 0; i < popSize; i++) {
			sum += population[i].cost + 1;
			maxValue = maxValue < population[i].cost ? population[i].cost : maxValue;
		}
		Random rand = new Random();
		double r = rand.nextDouble();
		double current = 0;
		for (int i = 0; i < popSize; i++) {
			current += ((maxValue - population[i].cost) + 1) / sum; // menor valor = maior probabilidade
			if (r <= current)
				return population[i];
		}
		return population[0];
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "AG Crossover(%f) Mutation(%f)", crossover, mutate);
	}
}
