package br.ufrn.ia.core.optimizationMethods;

import java.util.Locale;
import java.util.Random;

import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Solve;

public class BeeColonyOptimization extends OptimizationAlgorithm {

	private int epochs;
	private int maxNotImproved;
	private Solve bestSolve;

	/**
	 * Executa o algoritmo gen�tico para os par�metros informados.
	 * 
	 * @see BeeColonyOptimization#getBestSolve() para recuperar a solu��o ap�s a
	 *      execu��o de {@link BeeColonyOptimization#run()}
	 * 
	 * @param start
	 *            Vetor de solu��es iniciais. O tamanho do vetor determina o
	 *            tamanho da popula��o.
	 * @param epochs
	 *            Quantidade de itera��es do algoritmo. Valor maior ou igual a
	 *            zero. Geralmente 100.
	 * @param maxNotImproved
	 *            M�xima quantidade de itera��es que uma solu��o pode permancer
	 *            estagnada. Valor entre 0 e infinito. Geralmente 5.
	 */
	public BeeColonyOptimization( int epochs, int maxNotImproved) {
		this.epochs = epochs;
		this.maxNotImproved = maxNotImproved;
	}

	@Override
	public void run() {
		bestSolve = population[0];

		int[] improved = new int[population.length];
		int step = 0;
		int stepsUpdate = 0;
		while (step++ < epochs && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = 0; i < population.length; i++) {
				for (int j = 0; j < 5; j++) {
					Solve solve = new Solve(population[i]);
					Random r = new Random();
					move(solve, r);
					if (solve.cost < population[i].cost) {
						population[i] = solve;
						improved[i] = step;
					}
				}
			}

			for (int i = 0; i < population.length; i++) {
				Random r = new Random();
				int index = wheelSelection(population, r);
				Solve solve = new Solve(population[index]);
				move(solve, r);
				solve = localSearch(solve, r, Math.min(population[0].cluster.length, population.length));
				if (solve.cost < population[i].cost) {
					population[i] = solve;
					improved[i] = step;
				}
			}

			for (int i = 0; i < population.length; i++) {
				if (population[i].cost < bestSolve.cost) {
					bestSolve = new Solve(population[i]);
					stepsUpdate = 0;
				}
			}

			for (int i = 0; i < population.length; i++) {
				if (step - improved[i] > maxNotImproved) {
					population[i] = new Solve(population[i]);
					population[i].randomize();
					population[i].evaluate();
					improved[i] = step;
				}
			}

			bestSolve.evaluate();
		}
	}

	@Override
	public Solve getBestSolve() {
		return new Solve(bestSolve);
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "BCO TurnsMaxNotImproved(%d)", maxNotImproved);
	}
}
