package br.ufrn.ia.core;
import java.util.Arrays;
import java.util.HashSet;

public class Solve implements Cloneable {
	
	public Problem problem;
	public int[][] clusterings;
	public double cost;
	
	public Solve(int [][] clusterings){
		this.clusterings = new int [clusterings.length][];
		for(int i=0;i<this.clusterings.length;i++)
			this.clusterings[i] = clusterings[i].clone();
		evaluate();
	}

	public Solve(Solve solve) {
		clusterings = new int[solve.clusterings.length][];
		for (int i = 0; i < solve.clusterings.length; i++)
			clusterings[i] = solve.clusterings[i].clone();
		cost = solve.cost;
	}
	
	public int getNumClusteres(){
		return problem.getNumClusterers();
	}
	
	public int [] getConsensus(){
		problem.evaluate(clusterings);
		return problem.getConsensus();
	}
	
	public void randomize (){
		for(int i=0;i<clusterings.length;i++){
			int numClusteres = 2+Problem.rand.nextInt(clusterings[i].length-2); // minimum 2 groups
			for(int j=0;j<clusterings[i].length;j++)
				clusterings[i][j] = Problem.rand.nextInt(numClusteres);
		}
	}
	
	public int numClusteres (int clustering){
		HashSet <Integer> set = new HashSet <Integer> ();
		for(int i=0;i<clusterings[clustering].length;i++)
			set.add(clusterings[clustering][i]);
		return set.size();
	}

	public void evaluate() {
		cost = problem.evaluate(clusterings);
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder(Arrays.toString(getConsensus())+" | ");
		for(int i=0;i<clusterings.length;i++)
			str.append(Arrays.toString(clusterings[i]));
		return String.format("(%6.3f,%s) | ", cost, str.toString());
	}
}
