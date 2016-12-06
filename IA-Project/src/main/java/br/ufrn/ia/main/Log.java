package br.ufrn.ia.main;

public class Log {

	private String fileName;
	private int[][] clusteringVectors;
	private int[][] relabeledClusters;
	
	public Log(String fileName){
		this.fileName = new String(fileName);
	}
	
	public int[][] getClusteringVectors() {
		return clusteringVectors;
	}

	public void setClusteringVectors(int[][] clusteringVectors) {
		this.clusteringVectors = clusteringVectors;
	}

	public int[][] getRelabeledClusters() {
		return relabeledClusters;
	}

	public void setRelabeledClusters(int[][] relabeledClusters) {
		this.relabeledClusters = relabeledClusters;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
	
}
