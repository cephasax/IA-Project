package br.ufrn.ia.core;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public abstract class OptimizationAlgorithm {
	
	protected Solve [] population;
	
	protected final int maxStepsWhitoutUpdate = 20;
	public abstract void run();
	public abstract Solve getBestSolve();
	
	public void setPopulation (Solve [] solves){
		this.population = solves;
		for(int i=0;i<population.length;i++)
			population[i].evaluate();
	}

	public int wheelSelection(Solve[] solve, Random rand) {
		double sum = 0;
		for (int i = 0; i < solve.length; i++)
			sum += 1.0 - solve[i].cost;

		double[] p = new double[solve.length];
		for (int i = 0; i < solve.length; i++)
			p[i] = (1.0 - solve[i].cost) / sum;

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
		for (int j = 0; j < solve.cluster.length; j++) {
			Random r = new Random();
			if (r.nextDouble() < mutate) {
				int newSet =r.nextInt(solve.getNumClusteres());
				m.cluster[j] = newSet;
			}
		}
		m.evaluate();
		return m;
	}

	protected Solve crossover(Solve a, Solve b) {
		Solve out = new Solve(a);
		Random r = new Random();
		int indexA = r.nextInt(a.cluster.length / 2);
		int indexB = b.cluster.length / 2 + r.nextInt(b.cluster.length / 2);
		for (int i = indexA; i < indexB; i++)
			out.cluster[i] = b.cluster[i];
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

	public Solve localSearch(Solve start, Random rand, int size) {
		Solve best = start;
		do {
			start = new Solve(best);
			best = null;
			Solve s = new Solve(start);
			for (int i = 0; i < size; i++) {
				int originalCluster = s.cluster[i];
				double oldCost = s.cost;
				Random r  = new Random();
				int index = r.nextInt(s.cluster.length);
				s.cluster[index] = rand.nextInt(start.getNumClusteres());
				s.evaluate();
				if (best == null || s.cost < best.cost) {
					best = new Solve(s);
				}
				s.cluster[index] = originalCluster;
				s.cost = oldCost;
			}
		} while (best != null && best.cost < start.cost);
		return start;
	}

	public Solve pathRelink(Solve start, Solve guide) {
		Solve path = new Solve(start);
		path = new Solve(guide);
		Solve best = new Solve(start);
		while (true) {
			Vector<Solve> alterations = new Vector<Solve>();
			for (int j = 0; j < path.cluster.length; j++) {
				if (path.cluster[j] != guide.cluster[j]) {
					Solve solve = new Solve(path);
					solve.cluster[j] = guide.cluster[j];
					alterations.add(solve);
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
		int startIndex = rand.nextInt(solve.cluster.length);
		int endIndex = startIndex + rand.nextInt(solve.cluster.length - startIndex);
		for (int i = startIndex; i < endIndex; i++)
			solve.cluster[i] = rand.nextInt(solve.getNumClusteres());
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
