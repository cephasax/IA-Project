package method;

import java.util.Locale;

import core.ARFF;
import core.Main;
import core.OptimizationAlgorithm;
import core.Problem;
import core.Solve;
import metric.MX;

public class ParticleSwarmOptimization extends OptimizationAlgorithm {

	public static void main(String[] args) throws Exception {
		int numK = 2;
		Problem problem = new Problem(ARFF.Breast_Cancer_Wisconsin_Original, new MX(), numK);
		Solve.problem = problem;

		int numAnts = 10;
		int[][] clusterings = Main.getClusterings(ARFF.Breast_Cancer_Wisconsin_Original, numK);
		Solve[] start = new Solve[numAnts];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(numK, clusterings, Solve.pPartitions, Solve.pEquals);
			start[i].evaluate();
		}

		double time = System.currentTimeMillis();
		ParticleSwarmOptimization pso = new ParticleSwarmOptimization(start, 10, 0.95, 0.5, 0);
		pso.run();
		time = (System.currentTimeMillis() - time) / 1000;

		System.out.println(time);

		System.out.println(pso.getBestSolve());
	}

	private Solve[] particle;

	private double pOwnWay;

	private double pPreviousPosition;

	private double pBestPosition;

	private int epochs;

	private Solve bestSolve;

	/**
	 * Executa o algoritmo nuvem de partículas para os parâmetros informados.
	 * Observe que pOwnWay + pPreviousPosition + pBestPosition deve ser 1.0.
	 * @see ParticleSwarmOptimization#getBestSolve() para recuperar a solução após a execução de {@link ParticleSwarmOptimization#run()}
	 * 
	 * @param clusterings Vetor de soluções iniciais. O tamanho do vetor determina o tamanho da população.
	 * @param epochs Quantidade de iterações do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param pOwnWay Probabilidade inicial da partícula seguir o próprio caminho. 0<=pOwnWay<=1. Geralmente 0.95.
	 * @param pPreviousPosition Probabilidade inicial da partícula voltar para a melhor posição encontrada por ela. 0<=pPreviousPosition<=1. Geralmente 0.05.
	 * @param pBestPosition Probabilidade inicial de seguir a melhor posição encontrada entre todas as partículas. 0<=pPreviousPosition<=1. Geralmente 0.
	 */

	public ParticleSwarmOptimization(Solve[] start, int epochs, double pOwnWay, double pPreviousPosition, double pBestPosition) {
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
				int v = roulette(p, Problem.rand);
				switch (v) {
				case 0:
					particle[i] = localSearch(particle[i], Problem.rand, Math.min(particle.length, particle[0].cluster.length));
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
