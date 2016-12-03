package br.ufrn.ia.core.optimizationMethods;

import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;

public class ParticleSwarmOptimization extends OptimizationAlgorithm {

	private Solve[] particle;
	private int[][][] clusterings;
	private double pOwnWay;
	private double pPreviousPosition;
	private double pBestPosition;
	private int epochs;
	private Solve bestSolve;

	public ParticleSwarmOptimization(int[][][] clusterings, int epochs, double pOwnWay, double pPreviousPosition,
			double pBestPosition) {
		this.epochs = epochs;
		this.clusterings = clusterings;
		this.pOwnWay = pOwnWay;
		this.pPreviousPosition = pPreviousPosition;
		this.pBestPosition = pBestPosition;
	}

	@Override
	public void run() {
		double[] p = new double[] { pOwnWay, pPreviousPosition, pBestPosition };

		particle = new Solve[clusterings.length];
		Solve[] pBest = new Solve[particle.length];
		for (int i = 0; i < particle.length; i++) {
			particle[i] = new Solve(clusterings[i]);
			pBest[i] = new Solve(particle[i]);
		}

		while (epochs-- > 0) {

			Solve gBest = particle[1];
			for (int i = 0; i < particle.length; i++) {
				if (particle[i].cost < gBest.cost)
					gBest = particle[i];
				if (particle[i].cost < pBest[i].cost)
					pBest[i] = particle[i];
			}
			bestSolve = gBest;

			for (int i = 0; i < particle.length; i++) {
				int v = roulette(p, Problem.rand);
				switch (v) {
				case 0:
					particle[i] = localSearch(particle[i], Problem.rand);
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
}
