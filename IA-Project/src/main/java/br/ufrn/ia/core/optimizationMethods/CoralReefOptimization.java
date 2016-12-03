package br.ufrn.ia.core.optimizationMethods;
import java.util.Hashtable;
import java.util.Vector;

import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;

public class CoralReefOptimization extends OptimizationAlgorithm {

	public final int k = 5; // tryes to insert
	private int dimension;
	private int epochs;
	private double rho; // The rate between free/occupied squares 0 < rho < 1
	private double Fb; // fraction of broadcast spawners 0 < Fb < 1
	private double Fa; // fraction of duplicates 0 < Fa < 1
	private double Fd; // fraction of the worse health corals 0 < Fd < 1
	private boolean insertRandOrRank; // true for rand and false for ranked
	private int stepsDepredation;
	private Solve[] reef;
	private int[][][] clusterings;

	public CoralReefOptimization(int dimension, boolean insertRandOrRank, int epochs, double rho, double fa, double fb, double fd, int stepsDepredation, int[][][] clusterings) {
		this.dimension = dimension;
		this.epochs = epochs;
		this.rho = rho;
		Fa = fa;
		Fb = fb;
		Fd = fd;
		this.insertRandOrRank = insertRandOrRank;
		this.stepsDepredation = stepsDepredation;
		this.clusterings = clusterings;
	}

	public void run() {

		reef = new Solve[dimension];

		for (int i = 0; i < reef.length; i++)
			if (Problem.rand.nextDouble() < rho)
				reef[i] = new Solve(clusterings[Problem.rand.nextInt(clusterings.length)]);

		while (epochs-- > 0) {

			Vector<Solve> broadcastSpawing = new Vector<Solve>();
			Vector<Solve> brooding = new Vector<Solve>();
			for (int i = 0; i < reef.length; i++) {
				if (reef[i] != null) {
					if (Problem.rand.nextDouble() < Fb) {
						broadcastSpawing.add(reef[i]);
					} else {
						brooding.add(reef[i]);
					}
				}
			}

			Vector<Solve> larvaes = new Vector<Solve>();
			while (broadcastSpawing.size() > 2) {
				Solve a = broadcastSpawing.remove(Problem.rand.nextInt(broadcastSpawing.size()));
				Solve b = broadcastSpawing.remove(Problem.rand.nextInt(broadcastSpawing.size()));
				larvaes.add(crossover(a, b));
				larvaes.add(crossover(b, a));
			}
			while (!brooding.isEmpty()) {
				Solve solve = brooding.remove(0);
				larvaes.add(mutation(solve, 0.1));
			}

			while (!larvaes.isEmpty()) {
				Solve solve = larvaes.remove(0);
				insertLarvae(new Solve(solve));
			}

			Hashtable<Solve, Double> count = new Hashtable<Solve, Double>();
			for (int i = 0; i < reef.length; i++)
				if (reef[i] != null)
					count.put(reef[i], reef[i].cost);
			Solve[] validSolves = count.keySet().toArray(new Solve[] {});
			quicksort(validSolves, 0, validSolves.length - 1, createHashtableComparator(count));

			for (int i = 0; i < validSolves.length * Fa; i++) {
				Solve solve = new Solve(validSolves[i]);
				boolean insert = false;
				for (int j = 0; j < reef.length && !insert; j++) {
					if (reef[j] == null || reef[j].cost > solve.cost) {
						reef[j] = solve;
						insert = true;
					}
				}
			}

			count = new Hashtable<Solve, Double>();
			for (int i = 0; i < reef.length; i++)
				if (reef[i] != null)
					count.put(reef[i], reef[i].cost);
			validSolves = count.keySet().toArray(new Solve[] {});
			quicksort(validSolves, 0, validSolves.length - 1, createHashtableComparator(count));

			if (validSolves.length > 0 && epochs % stepsDepredation == 0) {
				int numRemove = (int) (validSolves.length * (1.0 - Fd));
				numRemove = validSolves.length - numRemove;

				for (int i = 0; i < numRemove; i++) {
					double numberFd = validSolves[validSolves.length - i - 1].cost;

					boolean removed = false;
					for (int j = 0; j < reef.length && !removed; j++) {
						if (reef[j] != null && reef[j].cost >= numberFd) {
							reef[j] = null;
							removed = true;
						}
					}
				}
			}
		}
	}

	private void insertLarvae(Solve solve) {
		if (insertRandOrRank) {
			for (int i = 0; i < k; i++) {
				int index = Problem.rand.nextInt(reef.length);
				if (reef[index] == null || solve.cost < reef[index].cost) {
					reef[index] = solve;
					return;
				}
			}
		} else {
			double maxCost = 0;
			for (int i = 0; i < reef.length; i++) {
				if (reef[i] == null) {
					reef[i] = solve;
					return;
				} else if (reef[i].cost > maxCost) {
					maxCost = reef[i].cost;
				}
			}
			for (int i = 0; i < reef.length; i++) {
				if (reef[i] != null && reef[i].cost == maxCost) {
					reef[i] = solve;
					return;
				}
			}
		}
	}

	@Override
	public Solve getBestSolve() {
		Solve min = null;
		for (int i = 0; i < reef.length; i++)
			if (reef[i] != null && (min == null || reef[i].cost < min.cost))
				min = reef[i];
		return min;
	}
}
