package br.ufrn.ia.core.optimizationMethods;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.utils.Util;

public abstract class OptimizationAlgorithm {

	public abstract void run();
	public abstract Solve getBestSolve();
	
	public int wheelSelection(Solve[] solve, Random rand) {
		double sum = 0;
		for (int i = 0; i < solve.length; i++)
			sum += 1.0 -solve[i].cost;

		double[] p = new double[solve.length];
		for (int i = 0; i < solve.length; i++)
			p[i] = (1.0-solve[i].cost) / sum;

		sum = 0;
		double r = rand.nextDouble() * sum;
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
			if (sum >= r) {
				return i;
			}
		}
		return 0;
	}

	protected Solve mutation(Solve solve, double mutate) {
		Solve m = new Solve(solve);
		for (int i = 0; i < solve.clusterings.length; i++) {
			for (int j = 0; j < solve.clusterings[i].length; j++) {
				if (Problem.rand.nextDouble() < mutate) {
					int newSet = Problem.rand.nextInt(solve.numClusteres(i));
					m.clusterings[i][j] = newSet;
				}
			}
		}
		m.evaluate();
		return m;
	}

	protected Solve crossover(Solve a, Solve b) {
		Solve out = new Solve(a);

		int[] arrayA = Util.clusteringToArray(a.clusterings);
		int[] arrayB = Util.clusteringToArray(b.clusterings);

		int indexA = Problem.rand.nextInt(arrayA.length / 2);
		int indexB = arrayB.length / 2 + Problem.rand.nextInt(arrayB.length / 2);

		int[] arrayC = new int[arrayA.length];

		for (int i = 0; i < indexA; i++)
			arrayC[i] = arrayA[i];
		for (int i = indexA; i < indexB; i++)
			arrayC[i] = arrayA[i];
		for (int i = indexB; i < arrayC.length; i++)
			arrayC[i] = arrayA[i];

		out.clusterings = Util.arrayToClustering(arrayC, a.clusterings.length);
		out.evaluate();

		return out;
	}

	public int roulette(double[] p, Random rand) {
		double r = rand.nextDouble();
		double current = 0;
		for (int i = 0; i < p.length; i++) {
			current += p[i];
			if (r <= current) {
				return i;
			}
		}
		return 0;
	}

	public Solve localSearch(Solve start, Random rand) {
		Solve best = start;
		do {
			start = new Solve(best);
			best = null;
			Solve s = new Solve(start);
			for (int i = 0; i < s.clusterings.length; i++) {
				int numClusterings = s.numClusteres(i);
				int numSample = Problem.rand.nextInt(s.clusterings[i].length/10);
				numSample = Math.max(numSample, 1);
				for (int j = 0; j < numSample; j++) {
					int originalCluster = s.clusterings[i][j];
					int index = Problem.rand.nextInt(s.clusterings[0].length);
					s.clusterings[i][index] = rand.nextInt(numClusterings);
					s.evaluate();
					if (best == null || s.cost < best.cost) {
						best = new Solve(s);
					}

					s.clusterings[i][index] = originalCluster;
				}
			}
		} while (best.cost < start.cost);
		return start;
	}

	public Solve localSearchComplete(Solve start, Random rand) {
		Solve best = start;
		do {
			start = new Solve(best);
			best = null;
			Solve s = new Solve(start);
			for (int i = 0; i < s.clusterings.length; i++) {
				int numClusterings = s.numClusteres(i) + 1; // +1 criar novo cluster
				for (int j = 0; j < s.clusterings[i].length; j++) {
					int originalCluster = s.clusterings[i][j];
					// para cada posição modificar as posivéis posições
					for (int k = 0; k < numClusterings; k++) {
						s.clusterings[i][j] = k;
						s.evaluate();
						if (best == null || s.cost < best.cost) {
							best = new Solve(s);
						}
					}
					s.clusterings[i][j] = originalCluster;
				}
			}
		} while (best.cost < start.cost);
		return start;
	}

	public Solve pathRelink(Solve start, Solve guide) {
		Solve path = new Solve(start);
		path = new Solve(guide);
		Solve best = new Solve(start);
		while (true) {
			Vector<Solve> alterations = new Vector<Solve>();
			for (int i = 0; i < start.clusterings.length; i++) {
				for (int j = 0; j < start.clusterings[i].length; j++) {
					if (path.clusterings[i][j] != guide.clusterings[i][j]) {
						Solve solve = new Solve(path);
						solve.clusterings[i][j] = guide.clusterings[i][j];
						alterations.add(solve);
					}
				}
			}
			if (alterations.isEmpty())
				break;

			Solve minor = alterations.get(0);
			for (Solve solve : alterations) {
				solve.evaluate();
				if (solve.cost < minor.cost)
					minor = solve;
			}
			path = minor;
			if (path.cost < best.cost)
				best = new Solve(path);
		}
		return best;
	}
	
	public void move(Solve solve, Random rand) {
		for(int i=0;i<solve.clusterings.length;i++){
			int startIndex = rand.nextInt(solve.clusterings[i].length);
			int endIndex = startIndex + rand.nextInt(solve.clusterings[i].length - startIndex);
			int numClusteres = solve.numClusteres(i);
			for(int j=startIndex;j<endIndex;j++)
				solve.clusterings[i][j] = rand.nextInt(numClusteres);
		}
		solve.evaluate();
	}

	public <T> Comparator<T> createHashtableComparator(Hashtable<T, ? extends Number> table) {
		return new HashtableComparator<T>(table);
	}

	private class HashtableComparator<T> implements Comparator<T> {

		protected Hashtable<T, ? extends Number> table;

		public HashtableComparator(Hashtable<T, ? extends Number> table) {
			this.table = table;
		}

		public int compare(T o1, T o2) {
			Number v1 = table.get(o1);
			Number v2 = table.get(o2);
			int cmp = 0;
			if (v1.doubleValue() > v2.doubleValue())
				cmp = +1;
			else if (v1.doubleValue() < v2.doubleValue())
				cmp = -1;
			return cmp;
		}
	}

	public <T> void quicksort(T[] values, int inc, int end, Comparator<T> cmp) {
		int i = inc;
		int j = end;
		T p = values[(inc + end) / 2];
		while (i < j) {
			while (cmp.compare(values[i], p) < 0)
				i++;
			while (cmp.compare(values[j], p) > 0)
				j--;
			if (i <= j) {
				T aux = values[i];
				values[i] = values[j];
				values[j] = aux;
				i++;
				j--;
			}
		}
		if (j > inc)
			quicksort(values, inc, j, cmp);
		if (i < end)
			quicksort(values, i, end, cmp);
	}
}
