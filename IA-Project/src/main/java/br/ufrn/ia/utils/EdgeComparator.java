package br.ufrn.ia.utils;

import java.util.Comparator;

import br.ufrn.ia.graph.Edge;

public class EdgeComparator implements Comparator<Edge> {

	private double[][] distance;

	public EdgeComparator(double[][] distance) {
		this.distance = distance;
	}

	public int compare(Edge e1, Edge e2) {
		double d1 = distance[e1.v1][e1.v2];
		double d2 = distance[e2.v1][e2.v2];
		int cmp = d1 < d2 ? -1 : d1 > d2 ? 1 : 0;
		return cmp;
	}
}
