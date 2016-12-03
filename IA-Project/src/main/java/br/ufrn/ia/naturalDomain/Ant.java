package br.ufrn.ia.naturalDomain;

import java.util.Hashtable;

import br.ufrn.ia.core.MinimumSpanningTree;
import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.graph.Edge;

public class Ant {

	public Solve solve;
	public double[][] update;

	public Ant(int[][] clusterings) {
		this.solve = new Solve(clusterings);
	}

	public void build(double[][] pheromone, double alpha, double beta, double[][] distance) {
		update = new double[distance.length][distance.length];

		double[][] distanceRemovedEdges = new double[distance.length][];
		for (int i = 0; i < distanceRemovedEdges.length; i++)
			distanceRemovedEdges[i] = distance[i].clone();

		for (int clusterer = 0; clusterer < solve.clusterings.length; clusterer++) {
			for (int k = 1; k < solve.getNumClusteres(); k++) {
				MinimumSpanningTree mst = new MinimumSpanningTree(distanceRemovedEdges, k);
				Hashtable<Edge, Double> edges = getProbabilities(pheromone, mst, alpha, beta);

				for (int i = 0; i < k; i++) {
					Edge e = roulette(edges);
					mst.removeEdge(e);
					update[e.v1][e.v2] = 1.0 / distance[e.v1][e.v2];
					distanceRemovedEdges[e.v1][e.v2] = Double.MAX_VALUE;
				}
				solve.clusterings[clusterer] = mst.getClustering();
			}
		}
		solve.evaluate();
	}

	private Hashtable<Edge, Double> getProbabilities(double[][] pheromone, MinimumSpanningTree mst, double alpha,
			double beta) {
		double sum = 0;
		Hashtable<Edge, Double> edges = new Hashtable<Edge, Double>();
		for (Edge e : mst.getMST()) {
			double gamma = Math.pow(pheromone[e.v1][e.v2], alpha);
			double ni = Math.pow(e.cost, beta);
			double p = gamma * ni;
			sum += p;
			edges.put(e, p);
		}

		if (sum == 0)
			sum = 1;

		for (Edge e : edges.keySet()) {
			double value = edges.get(e);
			edges.put(e, value / sum);
		}
		return edges;
	}

	public <T> T roulette(Hashtable<T, Double> feromone) {
		double sum = 0;
		for (double d : feromone.values())
			sum += d + 1;
		double r = Problem.rand.nextDouble();
		double current = 0;
		for (T key : feromone.keySet()) {
			current += (feromone.get(key) + 1.0) / sum;
			if (r <= current) {
				return key;
			}
		}
		return feromone.keys().nextElement();
	}

	public String toString() {
		return solve.toString();
	}
}
