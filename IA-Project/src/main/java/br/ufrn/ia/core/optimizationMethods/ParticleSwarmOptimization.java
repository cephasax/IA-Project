package br.ufrn.ia.core.optimizationMethods;

import java.util.Locale;
import java.util.Random;

import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Solve;

public class ParticleSwarmOptimization extends OptimizationAlgorithm {

	private double pOwnWay;
	private double pPreviousPosition;
	private double pBestPosition;
	private int epochs;
	private Solve bestSolve;

	/**
	 * Executa o algoritmo nuvem de part�culas para os par�metros informados.
	 * Observe que pOwnWay + pPreviousPosition + pBestPosition deve ser 1.0.
	 * @see ParticleSwarmOptimization#getBestSolve() para recuperar a solu��o ap�s a execu��o de {@link ParticleSwarmOptimization#run()}
	 * 
	 * @param clusterings Vetor de solu��es iniciais. O tamanho do vetor determina o tamanho da popula��o.
	 * @param epochs Quantidade de itera��es do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param pOwnWay Probabilidade inicial da part�cula seguir o pr�prio caminho. 0<=pOwnWay<=1. Geralmente 0.95.
	 * @param pPreviousPosition Probabilidade inicial da part�cula voltar para a melhor posi��o encontrada por ela. 0<=pPreviousPosition<=1. Geralmente 0.05.
	 * @param pBestPosition Probabilidade inicial de seguir a melhor posi��o encontrada entre todas as part�culas. 0<=pPreviousPosition<=1. Geralmente 0.
	 */
	public ParticleSwarmOptimization(int epochs, double pOwnWay, double pPreviousPosition, double pBestPosition) {
		this.epochs = epochs;
		this.pOwnWay = pOwnWay;
		this.pPreviousPosition = pPreviousPosition;
		this.pBestPosition = pBestPosition;
	}

	@Override
	public void run() {
		double[] p = new double[] { pOwnWay, pPreviousPosition, pBestPosition };

		Solve[] pBest = new Solve[population.length];
		for (int i = 0; i < population.length; i++) {
			population[i].evaluate();
			pBest[i] = new Solve(population[i]);
		}
		bestSolve = new Solve(population[0]);

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			Solve gBest = population[0];
			for (int i = 0; i < population.length; i++) {
				if (population[i].cost < gBest.cost)
					gBest = population[i];
				if (population[i].cost < pBest[i].cost)
					pBest[i] = population[i];
			}
			if (gBest.cost < bestSolve.cost) {
				bestSolve = new Solve(gBest);
				stepsUpdate = 0;
			}

			for (int i = 0; i < population.length; i++) {
				Random r = new Random();
				int v = roulette(p, r);
				switch (v) {
				case 0:
					population[i] = localSearch(population[i], r, Math.min(population.length, population[0].cluster.length));
					break;
				case 1:
					population[i] = pathRelink(pBest[i], population[i]);
					break;
				case 2:
					population[i] = pathRelink(gBest, population[i]);
				}
			}

			p[0] *= 0.95;
			p[1] *= 1.01;
			p[2] = 1.0 - (p[0] + p[1]);
		}
	}

	@Override
	public Solve getBestSolve() {
		return bestSolve;
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "PSO P-OwnWay(%f) P-PreviousPosition(%f) P-BestPosition(%f)", pOwnWay, pPreviousPosition, pBestPosition);
	}
}
