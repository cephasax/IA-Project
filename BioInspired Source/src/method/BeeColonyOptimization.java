package method;

import java.util.Locale;
import java.util.Random;

import core.OptimizationAlgorithm;
import core.Solve;

public class BeeColonyOptimization extends OptimizationAlgorithm {

	private int epochs;

	private Solve[] bees;

	private int maxNotImproved;

	private Solve bestSolve;

	/**
	 * Executa o algoritmo gen�tico para os par�metros informados.
	 * @see BeeColonyOptimization#getBestSolve() para recuperar a solu��o ap�s a execu��o de {@link BeeColonyOptimization#run()}
	 * 
	 * @param start Vetor de solu��es iniciais. O tamanho do vetor determina o tamanho da popula��o.
	 * @param epochs Quantidade de itera��es do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param maxNotImproved M�xima quantidade de itera��es que uma solu��o pode permancer estagnada. Valor entre 0 e infinito. Geralmente 5.
	 */
	public BeeColonyOptimization(Random rand, Solve[] start, int epochs, int maxNotImproved) {
		super(rand);
		this.bees = start.clone();
		this.epochs = epochs;
		this.maxNotImproved = maxNotImproved;
	}

	@Override
	public void run() {
		bestSolve = bees[0];
		
		for (int i = 0; i < bees.length; i++) {
			if (bees[i].cost < bestSolve.cost) {
				bestSolve = new Solve(bees[i]);
			}
		}

		int[] improved = new int[bees.length];
		int step = 0;
		int stepsUpdate = 0;
		while (step++ < epochs && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = 0; i < bees.length; i++) {
				for (int j = 0; j < 5; j++) {
					Solve solve = new Solve(bees[i]);
					move(solve, rand);
					if (solve.cost < bees[i].cost) {
						bees[i] = solve;
						improved[i] = step;
					}
				}
			}

			for (int i = 0; i < bees.length; i++) {
				int index = wheelSelection(bees, rand);
				Solve solve = new Solve(bees[index]);
				move(solve, rand);
				solve = localSearch(solve, rand, Math.min(bees[0].cluster.length,bees.length));
				if (solve.cost < bees[i].cost) {
					bees[i] = solve;
					improved[i] = step;
				}
			}

			for (int i = 0; i < bees.length; i++) {
				if (bees[i].cost < bestSolve.cost) {
					bestSolve = new Solve(bees[i]);
					stepsUpdate = 0;
				}
			}

			for (int i = 0; i < bees.length; i++) {
				if (step - improved[i] > maxNotImproved) {
					bees[i] = new Solve(bees[i]);
					bees[i].randomize(rand);
					bees[i].evaluate();
					improved[i] = step;
				}
			}
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
