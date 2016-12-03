package br.ufrn.ia.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;

import br.ufrn.ia.graph.Edge;
import br.ufrn.ia.utils.EdgeComparator;
import br.ufrn.ia.utils.EdgeReverseComparator;

public class MinimumSpanningTree {

	private int[] clustering;
	
	private Collection <Edge> mst;

	public MinimumSpanningTree(double[][] distance, int k) {		
		PriorityQueue<Edge> edges = new PriorityQueue<Edge>(distance.length * distance.length, new EdgeComparator(distance));
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				if (i != j) {
					Edge e = new Edge(i, j);
					e.cost = distance[i][j];
					edges.add(e);
				}
			}
		}

		int numClusterings = distance.length;
		int[] sets = new int[numClusterings];
		for (int i = 0; i < sets.length; i++)
			sets[i] = i;
		PriorityQueue<Edge> mst = new PriorityQueue<Edge>(new EdgeReverseComparator(distance));
		while (numClusterings > 1) {
			Edge e = edges.poll();
			if (sets[e.v1] != sets[e.v2]) {
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

		clustering = new int[mst.size() + 1];
		
		for(int i=2;i<=k;i++){
			Edge e = mst.poll();
			relabel(e.v2, clustering, mst.toArray(new Edge[] {}), i - 1, clustering[e.v1]);
		}
		this.mst = mst;
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
	
	public void removeEdge(Edge e) {
		int k = 0;
		HashSet <Integer> set = new HashSet <Integer> ();
		for(int i=0;i<clustering.length;i++)
			set.add(clustering[i]);
		k = set.size();
		
		if(mst.remove(e))
			relabel(e.v2, clustering, mst.toArray(new Edge[] {}), k, clustering[e.v1]);
	}
	
	public Collection<Edge> getMST (){
		return mst;
	}

	public int[] getClustering() {
		return clustering;
	}
}

