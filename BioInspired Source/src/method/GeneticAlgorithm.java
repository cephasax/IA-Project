package method;

import java.util.Hashtable;
import java.util.Locale;

import core.ARFF;
import core.Main;
import core.OptimizationAlgorithm;
import core.Problem;
import core.Solve;
import metric.MX;

public class GeneticAlgorithm extends OptimizationAlgorithm {

	public static void main(String[] args) throws Exception {
		int numK = 4;
		Problem problem = new Problem(ARFF.Breast_Cancer_Wisconsin_Original, new MX(), numK);
		Solve.problem = problem;

		int numAnts = 1;
		int[][] clusterings = Main.getClusterings(ARFF.Balance, numK);
		Solve[] start = new Solve[numAnts];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(numK, clusterings, Solve.pPartitions, Solve.pEquals);
			start[i].evaluate();
		}

		double time = System.currentTimeMillis();
		GeneticAlgorithm ga = new GeneticAlgorithm(start, 100, 0.4, 0.9);
		ga.run();
		System.out.println((System.currentTimeMillis() - time) / 1000);

		System.out.println(ga.getBestSolve());
	}

	private int epochs;

	private double mutate;

	private double crossover;

	private Solve[] start;

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

	public GeneticAlgorithm(Solve[] start, int epochs, double mutate, double crossover) {
		this.epochs = epochs;
		this.mutate = mutate;
		this.crossover = crossover;
		this.start = start.clone();
	}

	public void run() {

		Solve[] population = new Solve[start.length * 2];
		for (int i = 0; i < population.length / 2; i++) {
			population[i] = new Solve(start[i]);
		}
		bestSolve = new Solve(population[0]);

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = population.length / 2; i < population.length; i++) {
				Solve parentA = roulette(population, population.length / 2);
				Solve parentB = roulette(population, population.length / 2);

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
		double r = Problem.rand.nextDouble();
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
