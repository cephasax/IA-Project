package br.ufrn.ia.naturalDomain;

import java.util.HashSet;
import java.util.Hashtable;

import br.ufrn.ia.core.MinimumSpanningTree;
import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.graph.Edge;

public class Ant {

	public Solve solve;

	public double[][] update;

	public Ant(Solve solve) {
		this.solve = new Solve(solve);
	}

	public void build(double[][] pheromone, double alpha, double beta, double[][] distance) {
		update = new double[distance.length][distance.length];

		double[][] distanceRemovedEdges = new double[distance.length][];
		for (int i = 0; i < distanceRemovedEdges.length; i++)
			distanceRemovedEdges[i] = distance[i].clone();

		int k = 1;
		while (k < solve.getNumClusteres()) {
			MinimumSpanningTree mst = new MinimumSpanningTree(distanceRemovedEdges, k);
			Hashtable<Edge, Double> edges = getProbabilities(pheromone, mst, alpha, beta);

			Edge e = roulette(edges);
			mst.removeEdge(e);
			update[e.v1][e.v2] = 1.0 / distance[e.v1][e.v2];
			distanceRemovedEdges[e.v1][e.v2] = Double.MAX_VALUE;

			solve.cluster = mst.getClustering();

			HashSet<Integer> numK = new HashSet<Integer>();
			for (int i = 0; i < solve.cluster.length; i++)
				numK.add(solve.cluster[i]);
			k = numK.size();
		}
		solve.evaluate();
	}

	private Hashtable<Edge, Double> getProbabilities(double[][] pheromone, MinimumSpanningTree mst, double alpha, double beta) {
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

	public void build2(double[][] pheromone, double alpha, double beta, double[][] distance) {
		update = new double[distance.length][distance[0].length];
		for (int i = 0; i < distance.length; i++) {
			Hashtable<Edge, Double> edges = getProbabilities2(pheromone, i, distance[i], alpha, beta);
			Edge e = roulette(edges);
			update[e.v1][e.v2] = 1.0 / distance[e.v1][e.v2];
			solve.cluster[i] = e.v2;
		}
		solve.evaluate();
	}

	private Hashtable<Edge, Double> getProbabilities2(double[][] pheromone, int currentNode, double[] votes, double alpha, double beta) {
		double sum = 0;

		Hashtable<Edge, Double> edges = new Hashtable<Edge, Double>();
		for (int j = 0; j < votes.length; j++) {
			Edge e = new Edge(currentNode, j);
			e.cost = votes[j];
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
		double r = sum * Problem.rand.nextDouble();
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
