package br.ufrn.ia.core.optimizationMethods;

import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;

public class BeeColonyOptimization extends OptimizationAlgorithm {
	
	private int epochs;
	private Solve[] bees;
	private int[][][] clusterings;
	private int maxNotImproved;
	private Solve bestSolve;

	public BeeColonyOptimization(int[][][] clusterings, int epochs, int maxNotImproved) {
		this.clusterings = clusterings;
		this.epochs = epochs;
		this.maxNotImproved = maxNotImproved;
	}

	@Override
	public void run() {
		bees = new Solve[clusterings.length];
		for (int i = 0; i < bees.length; i++)
			bees[i] = new Solve(clusterings[i]);
		bestSolve = bees[0];

		int[] improved = new int[bees.length];
		int step = 0;
		while (step++ < epochs) {

			for (int i = 0; i < bees.length; i++) {
				for (int j = 0; j < 5; j++) {
					Solve solve = new Solve(bees[i]);
					move(solve, Problem.rand);
					if (solve.cost < bees[j].cost) {
						bees[j] = solve;
						improved[j] = step;
					}
				}
			}

			for (int i = 0; i < bees.length; i++) {
				int index = wheelSelection(bees, Problem.rand);
				Solve solve = new Solve(bees[index]);
				move(solve, Problem.rand);
				solve = localSearch(solve, Problem.rand);
				if (solve.cost < bees[i].cost) {
					bees[i] = solve;
					improved[i] = step;
				}
			}
			
			for (int i = 0; i < bees.length; i++)
				if(bees[i].cost < bestSolve.cost)
					bestSolve = new Solve(bees[i]);

			for (int i = 0; i < bees.length; i++) {
				if (step - improved[i] > maxNotImproved) {
					bees[i] = new Solve(bees[i]);
					bees[i].randomize();
					bees[i].evaluate();
					improved[i] = step;
				}
			}
		}
	}

	@Override
	public Solve getBestSolve() {
		return bestSolve;
	}
}
