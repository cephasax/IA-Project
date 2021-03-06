package method;

import java.util.Locale;
import java.util.Random;

import core.OptimizationAlgorithm;
import core.Solve;

public class ParticleSwarmOptimization extends OptimizationAlgorithm {

	private Solve[] particle;

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

	public ParticleSwarmOptimization(Random rand, Solve[] start, int epochs, double pOwnWay, double pPreviousPosition, double pBestPosition) {
		super(rand);
		this.epochs = epochs;
		particle = start.clone();
		this.pOwnWay = pOwnWay;
		this.pPreviousPosition = pPreviousPosition;
		this.pBestPosition = pBestPosition;
	}

	@Override
	public void run() {
		double[] p = new double[] { pOwnWay, pPreviousPosition, pBestPosition };

		Solve[] pBest = new Solve[particle.length];
		for (int i = 0; i < particle.length; i++) {
			particle[i] = new Solve(particle[i]);
			pBest[i] = new Solve(particle[i]);
		}
		bestSolve = new Solve(particle[0]);

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			Solve gBest = particle[0];
			for (int i = 0; i < particle.length; i++) {
				if (particle[i].cost < gBest.cost)
					gBest = particle[i];
				if (particle[i].cost < pBest[i].cost)
					pBest[i] = particle[i];
			}
			if (gBest.cost < bestSolve.cost) {
				bestSolve = new Solve(gBest);
				stepsUpdate = 0;
			}

			for (int i = 0; i < particle.length; i++) {
				int v = roulette(p, rand);
				switch (v) {
				case 0:
					particle[i] = localSearch(particle[i], rand, Math.min(particle.length, particle[0].cluster.length));
					break;
				case 1:
					particle[i] = pathRelink(pBest[i], particle[i]);
					break;
				case 2:
					particle[i] = pathRelink(gBest, particle[i]);
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
