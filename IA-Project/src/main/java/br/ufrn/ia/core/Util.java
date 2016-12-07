package br.ufrn.ia.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import br.ufrn.ia.main.Configuration;
import weka.clusterers.AbstractClusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class Util {

	public void printMatrix(double[][] clustering) {
		for (int i = 0; i < clustering.length; i++) {
			for (int j = 0; j < clustering[0].length; j++) {
				System.out.print(String.format("%10.3f", clustering[i][j]));
			}
			System.out.println();
		}
	}

	public void printMatrix(int[][] clustering) {
		for (int i = 0; i < clustering.length; i++) {
			for (int j = 0; j < clustering[0].length; j++) {
				System.out.print(String.format("%5d", clustering[i][j]));
			}
			System.out.println();
		}
	}

	public void replaceClassByConsensus(Instances instances, int[] consensus) {
		RelabelAndConsensus relabelAndConsensus = new RelabelAndConsensus();
		relabelAndConsensus.remapToStartWithZero(consensus);
		instances.setClassIndex(0);
		instances.deleteAttributeAt(instances.numAttributes() - 1);
		ArrayList<String> newClass = new ArrayList<String>();
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < consensus.length; i++)
			set.add(consensus[i]);
		for (Integer key : set)
			newClass.add(key.toString());
		Attribute attribute = new Attribute("Consensus", newClass);
		instances.insertAttributeAt(attribute, instances.numAttributes());
		instances.setClassIndex(instances.numAttributes() - 1);
		for (int i = 0; i < instances.numInstances(); i++) {
			instances.get(i).setClassValue(consensus[i]);
		}
	}

	public double distance(Instance A, Instance B) {
		double diff = 0;
		for (int i = 1; i < A.numAttributes() - 1; i++) {
			if (A.attribute(i).isNominal())
				diff += A.value(i) == B.value(i) ? 0 : 1;
			else
				diff += Math.abs(A.value(i) - B.value(i));
		}
		return diff;
	}
	
	public double [][] buildHeuristic1(ARFF arff, int numK, int[][]clusterings) throws IOException {
		Instances instances = new Instances(new FileReader(new File(arff.location)));
		instances.setClassIndex(instances.numAttributes() - 1);
		double[][] distance = new double[instances.numInstances()][instances.numInstances()];
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				Util util = new Util();
				distance[i][j] = util.distance(instances.get(i), instances.get(j));
			}
		}
		return distance;
	}

	public double[][] buildHeuristic2(int numK, int[][] clusterings) {
		double[][] heuristic = new double[clusterings[0].length][];
		for (int i = 0; i < heuristic.length; i++) {
			double[] votes = new double[numK];
			Arrays.fill(votes, 1);
			for (int j = 0; j < clusterings.length; j++) {
				votes[clusterings[j][i]]++;
			}
			heuristic[i] = votes;
		}
		return heuristic;
	}
	
	 public void evaluate(File logFile, Problem problem, String baseName, int numK, OptimizationAlgorithm alg) throws IOException {
		String parans = alg.toString();
		double time = System.currentTimeMillis();
		alg.run();
		time = (System.currentTimeMillis() - time) / 1000.0;
		Solve bestSolve = alg.getBestSolve();
		PrintStream out = new PrintStream(new FileOutputStream(logFile, true));
		int[] cluster = bestSolve.cluster;
		
		RelabelAndConsensus relabelAndConsensus = new RelabelAndConsensus();
		relabelAndConsensus.remapToStartWithZero(cluster);
		out.println(String.format(Locale.ENGLISH, "%d\t%f\t%s\t%s\t%s\t%s\t%s\t%10.5f", numK, bestSolve.cost, problem.getFitness().getClass().getSimpleName(), alg.getClass().getSimpleName(), baseName, Arrays.toString(cluster), parans, time));
		out.close();
	}
	
	public ArrayList<Solve> buildSolves(Configuration conf, int tempK, int[][] newClusters, Problem problem){
		ArrayList<Solve> solves = new ArrayList<Solve>();						
		for(int numberOfsolves = 1; numberOfsolves <= conf.getSizeOfPupulation(); numberOfsolves++){
			//Construção das soluções
			Solve solve = new Solve(tempK, newClusters, conf.getpPartitions(), conf.getpEquals());
			solve.setProblem(problem);
			solves.add(solve);
		}
		return solves;
	}
}
