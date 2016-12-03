package core.graph;

public class Edge {
	public int v1;
	public int v2;
	public double cost;

	public Edge(int v1, int v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public String toString() {
		return "(" + v1 + "-" + v2 + ")";
	}
}
