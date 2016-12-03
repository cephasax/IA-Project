package core;

import java.util.Arrays;
import java.util.PriorityQueue;

import core.graph.Edge;

public class MinimumSpanningTreeClustering {

	public static final int Kmax = 10;

	private double mstSum;

	public static void main(String[] args) {

		double[][] distance = new double[6][6];

		for (int i = 0; i < distance.length; i++)
			Arrays.fill(distance[i], 100);

		distance[0][1] = distance[1][0] = 1;
		distance[0][3] = distance[3][0] = 4;
		distance[0][4] = distance[4][0] = 3;
		distance[1][3] = distance[3][1] = 4;
		distance[1][4] = distance[4][1] = 2;
		distance[2][4] = distance[4][2] = 4;
		distance[2][5] = distance[5][2] = 5;
		distance[3][4] = distance[4][3] = 4;
		distance[4][5] = distance[5][4] = 7;

		new MinimumSpanningTreeClustering(distance);
	}

	private int maxK;
	private int[] maxClustering;

	public MinimumSpanningTreeClustering(double[][] distance) {
		PriorityQueue<Edge> edges = new PriorityQueue<Edge>(distance.length * distance.length, new EdgeComparator(distance));
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				if (i != j) {
					Edge e = new Edge(i, j);
					edges.add(e);
				}
			}
		}

		int numClusterings = distance.length;
		int[] sets = new int[numClusterings];
		for (int i = 0; i < sets.length; i++)
			sets[i] = i;
		PriorityQueue<Edge> mst = new PriorityQueue<Edge>(distance.length, new EdgeReverseComparator(distance));
		mstSum = 0;
		while (numClusterings > 1) {
			Edge e = edges.poll();
			if (sets[e.v1] != sets[e.v2]) {
				mstSum += distance[e.v1][e.v2];
				int value = sets[e.v2];
				mst.add(e);
				for (int i = 0; i < sets.length; i++) {
					if (sets[i] == value) {
						sets[i] = sets[e.v1];
					}
				}
				numClusterings--;
			}
		}

		maxK = 1;
		maxClustering = new int[mst.size() + 1];
		mstSum = evaluateCost(mst, mstSum, distance);
	}

	private void relabel(int vertice, int[] clustering, Edge[] mst, int label, int oldLabel) {
		clustering[vertice] = label;
		for (int i = 0; i < mst.length; i++) {
			Edge e = mst[i];
			if (e.v1 == vertice && clustering[e.v2] == oldLabel) {
				relabel(e.v2, clustering, mst, label, oldLabel);
			} else if (e.v2 == vertice && clustering[e.v1] == oldLabel) {
				relabel(e.v1, clustering, mst, label, oldLabel);
			}
		}
	}

	private double evaluateCost(PriorityQueue<Edge> mst, double mstSum, double[][] distance) {
		double maxP = 0;
		int[] clustering = new int[mst.size() + 1];
		for (int numClusterings = 2; numClusterings <= Math.min(clustering.length, Kmax); numClusterings++) {
			Edge e = mst.poll();
			relabel(e.v2, clustering, mst.toArray(new Edge[] {}), numClusterings - 1, clustering[e.v1]);

			int[] count = new int[numClusterings];
			for (int i = 0; i < clustering.length; i++)
				count[clustering[i]]++;

			double[] wc = new double[numClusterings];
			Arrays.fill(wc, 0);
			for (Edge edge : mst)
				wc[clustering[edge.v1]] += distance[edge.v1][edge.v2];

			double P = numClusterings;
			for (int i = 0; i < count.length; i++)
				P *= Math.pow(count[i], wc[i] / mstSum);

			if (P > maxP) {
				maxP = P;
				this.maxK = numClusterings;
				this.maxClustering = clustering.clone();
			}
		}

		return maxP;
	}

	public int getNumClusters() {
		return maxK;
	}

	public int[] getClustering() {
		return maxClustering;
	}

	public double getSumCost() {
		return mstSum;
	}
}
