package method;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;

import core.OptimizationAlgorithm;
import core.Solve;

public class GeneticAlgorithm extends OptimizationAlgorithm {

	private int epochs;

	private double mutate;

	private double crossover;

	private Solve[] start;

	private Solve bestSolve;

	/**
	 * Executa o algoritmo genético para os parâmetros informados.
	 * @see GeneticAlgorithm#getBestSolve() para recuperar a solução após a execução de {@link GeneticAlgorithm#run()}
	 * 
	 * @param start Vetor de soluções iniciais. O tamanho do vetor determina o tamanho da população.
	 * @param epochs Quantidade de iterações do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param mutate Taxa de mutação. 0<=mutate<=1. Geralmente 0.1.
	 * @param crossover Taxa de cruzamento. 0<=crossover<=1. Geralmente 0.9.
	 */

	public GeneticAlgorithm(Random rand, Solve[] start, int epochs, double mutate, double crossover) {
		super(rand);
		this.epochs = epochs;
		this.mutate = mutate;
		this.crossover = crossover;
		this.start = start.clone();
	}

	public void run() {
		bestSolve = new Solve(start[0]);
		Solve[] population = new Solve[start.length * 2];
		for (int i = 0; i < population.length / 2; i++) {
			population[i] = new Solve(start[i]);
			if (population[0].cost < bestSolve.cost) {
				bestSolve = new Solve(population[0]);
			}
		}

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = population.length / 2; i < population.length; i++) {
				Solve parentA = roulette(population, population.length / 2);
				Solve parentB = roulette(population, population.length / 2);

				if (rand.nextDouble() < crossover)
					population[i] = this.crossover(parentA, parentB);
				else
					population[i] = new Solve(rand.nextBoolean() ? parentA : parentB);

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
