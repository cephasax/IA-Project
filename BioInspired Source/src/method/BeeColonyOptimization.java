package method;

import java.util.Locale;

import core.ARFF;
import core.Main;
import core.OptimizationAlgorithm;
import core.Problem;
import core.Solve;
import metric.CalinskiHarabasz;
import metric.MX;

public class BeeColonyOptimization extends OptimizationAlgorithm {

	public static void main(String[] args) throws Exception {
		int numK = 3;
		Problem problem = new Problem(ARFF.Balance, new MX(), numK);
		Solve.problem = problem;

		int numAnts = 10;
		int[][] clusterings = Main.getClusterings(ARFF.Balance, numK);
		Solve[] start = new Solve[numAnts];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(numK, clusterings, Solve.pPartitions, Solve.pEquals);
			start[i].evaluate();
		}

		BeeColonyOptimization aco = new BeeColonyOptimization(start, 1, 5);
		aco.run();

		Solve solve = aco.getBestSolve();

		System.out.println(solve);
		problem = new Problem(ARFF.Balance, new CalinskiHarabasz(), numK);
		Solve.problem = problem;
		solve.evaluate();
		System.out.println(solve);
	}

	private int epochs;

	private Solve[] bees;

	private int maxNotImproved;

	private Solve bestSolve;

	/**
	 * Executa o algoritmo genético para os parâmetros informados.
	 * @see BeeColonyOptimization#getBestSolve() para recuperar a solução após a execução de {@link BeeColonyOptimization#run()}
	 * 
	 * @param start Vetor de soluções iniciais. O tamanho do vetor determina o tamanho da população.
	 * @param epochs Quantidade de iterações do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param maxNotImproved Máxima quantidade de iterações que uma solução pode permancer estagnada. Valor entre 0 e infinito. Geralmente 5.
	 */
	public BeeColonyOptimization(Solve[] start, int epochs, int maxNotImproved) {
		this.bees = start.clone();
		this.epochs = epochs;
		this.maxNotImproved = maxNotImproved;
	}

	@Override
	public void run() {
		bestSolve = bees[0];

		int[] improved = new int[bees.length];
		int step = 0;
		int stepsUpdate = 0;
		while (step++ < epochs && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = 0; i < bees.length; i++) {
				for (int j = 0; j < 5; j++) {
					Solve solve = new Solve(bees[i]);
					move(solve, Problem.rand);
					if (solve.cost < bees[i].cost) {
						bees[i] = solve;
						improved[i] = step;
					}
				}
			}

			for (int i = 0; i < bees.length; i++) {
				int index = wheelSelection(bees, Problem.rand);
				Solve solve = new Solve(bees[index]);
				move(solve, Problem.rand);
				solve = localSearch(solve, Problem.rand, Math.min(bees[0].cluster.length,bees.length));
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
					bees[i].randomize();
					bees[i].evaluate();
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
