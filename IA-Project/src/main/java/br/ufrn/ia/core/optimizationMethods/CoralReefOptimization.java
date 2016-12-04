package br.ufrn.ia.core.optimizationMethods;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import br.ufrn.ia.core.ARFF;
import br.ufrn.ia.core.Main;
import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.metrics.CorrectRand;

public class CoralReefOptimization extends OptimizationAlgorithm {

	public static void main(String[] args) throws Exception {
		int numK = 2;
		Problem problem = new Problem(ARFF.Breast_Cancer_Wisconsin_Original, new CorrectRand(), numK);
		Solve.problem = problem;

		int numAnts = 10;
		int[][] clusterings = Main.getClusterings(ARFF.Breast_Cancer_Wisconsin_Original, numK);
		Solve[] start = new Solve[numAnts];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(numK, clusterings, Solve.pPartitions, Solve.pEquals);
			start[i].evaluate();
		}

		//cro1
		double time = System.currentTimeMillis();
		CoralReefOptimization cro = new CoralReefOptimization(start, 10, 25, false, 0.7, 0.1, 0.8, 0.05, 1);
		cro.run();
		System.out.println((System.currentTimeMillis() - time) / 1000);

		// cro2
		//CoralReefOptimization cro = new CoralReefOptimization(start, 10, 25, true, 0.7, 0.1, 0.8, 0.1, 5);
		//cro.run();

		// cro3
		//CoralReefOptimization cro = new CoralReefOptimization(start, 10, 25, false, 0.7, 0.1, 0.8, 0.1, 5);
		//cro.run();

		System.out.println(cro.getBestSolve());
	}

	public static final int k = 5; // tryes to insert

	private int dimension;

	private int epochs;

	private double rho; // The rate between free/occupied squares 0 < rho < 1

	private double Fb; // fraction of broadcast spawners 0 < Fb < 1

	private double Fa; // fraction of duplicates 0 < Fa < 1

	private double Fd; // fraction of the worse health corals 0 < Fd < 1

	private boolean insertRandOrRank; // true for rand and false for ranked

	private int stepsDepredation;

	private Solve[] reef;

	private Solve[] start;

	private Solve bestSolve;

	/**
	 * Executa o algoritmo CRO para os parâmetros informados.
	 * Observe que o CRO tem três tipos que podem ser configurados da seguinte forma:
	 * 		CRO1 insertRandOrRank = false; stepsDepredation = 1; Fd = 0.05
	 * 		CRO2 insertRandOrRank = true; stepsDepredation = 5; Fd = 0.1
	 * 		CRO3 insertRandOrRank = false; stepsDepredation = 5; Fd = 0.1
	 * @see CoralReefOptimization#getBestSolve() para recuperar a solução após a execução de {@link CoralReefOptimization#run()}
	 * 
	 * @param dimension Dimensão do grid indicada como o produto da largura pela altura. Valor maior que 1. Geralmente 25 (5x5).
	 * @param insertRandOrRank Parâmetro de controle do método de inserção de larvas no grid. True para inserção randômica e False para inserção e lugar vazio ou substituição da solução de maior custo.
	 * @param epochs Quantidade de iteração do algoritmo. Valor entre 0 e infinito. Geralmente 100.
	 * @param rho Taxa de ocupação inicial do grid.
	 * @param fa Taxa de replicação dos corais. 0 < Fa < 1. Geralmente valor próximo a 1.
	 * @param fb Taxa de broadcast das larvas, razão entre cruzamento e mutação. 0 < Fb < 1. Geralmente valor próximo a 1.
	 * @param fd Taxa de depredação dos corais. 0 < Fd < 1. Geralmente valor próximo a 0.
	 * @param stepsDepredation Parâmetro de controle de quando será aplicadda a depredação no coral. Geralmente 1.
	 * @param start Vetor de soluções iniciais.
	 */

	public CoralReefOptimization(Solve[] start, int epochs, int dimension, boolean insertRandOrRank, double rho, double fa, double fb, double fd, int stepsDepredation) {
		this.dimension = dimension;
		this.epochs = epochs;
		this.rho = rho;
		Fa = fa;
		Fb = fb;
		Fd = fd;
		this.insertRandOrRank = insertRandOrRank;
		this.stepsDepredation = stepsDepredation;
		this.start = start.clone();
	}

	public void run() {

		reef = new Solve[dimension];

		bestSolve = null;
		for (int i = 0; i < reef.length; i++) {
			if (Problem.rand.nextDouble() < rho) {
				reef[i] = new Solve(start[Problem.rand.nextInt(start.length)]);
				if (bestSolve == null || reef[i].cost < bestSolve.cost) {
					bestSolve = new Solve(reef[i]);
				}
			}
		}

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {
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

			if (validSolves[0].cost < bestSolve.cost) {
				bestSolve = new Solve(validSolves[0]);
				stepsUpdate = 0;
			}

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
		return new Solve(bestSolve);
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "CRO dimension(%d) InsertRand(%s) InsertRank(%s) Rho(%f) Fa-repication(%f) Fb-broadcast(%f) Fd-depredation(%f) StepsUntilDepredation(%d)", dimension, Boolean.toString(insertRandOrRank), Boolean.toString(!insertRandOrRank), rho, Fa, Fb, Fd, stepsDepredation);
	}
}
