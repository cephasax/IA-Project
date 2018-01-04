package metric;
import java.util.HashSet;

import core.Fitness;
import core.Util;
import weka.core.Instances;

// maximization objective (0,1) range
public class MX implements Fitness {

	public static double eps = 0.8;
	
	@Override
	public boolean isMinimization (){ // quanto maior melhor
		return true; // inverse mx 1-mx
	}

	@Override
	public double evaluate(Instances instances, int[] consensus) {
		instances = new Instances(instances);
		Util.replaceClassByConsensus(instances, consensus);

		double[][] distance = new double[instances.numInstances()][instances.numInstances()];
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				distance[i][j] = Util.distance(instances.get(i), instances.get(j));
			}
		}
		int quantClusters = instances.numClasses();
		double result = 0;

		for (int i = 0; i < quantClusters; i++) {
			result += evaluate(instances, distance, eps, i);
		}

		double mx = (result / quantClusters);
		return 1.0 - mx; // (1.0 - mx) minimization objective
	}

	public static double evaluate(Instances instances, double[][] distance, double EPS, double clusterID) {
		int instancesInCluster;
		double[][] distanceMatrix;
		int[][] vizinhos;

		instancesInCluster = 0;

		for (int j = 0; j < instances.numInstances(); j++) {
			if (instances.instance(j).classValue() == clusterID) {
				instancesInCluster++;
			}
		}

		if (instancesInCluster > 0) {

			distanceMatrix = baseDistanceMatrix(instances, distance, (int) (clusterID - (clusterID % 1)));

			vizinhos = new int[distanceMatrix.length][distanceMatrix.length];
			HashSet<Integer> objetosNoCluster = new HashSet<Integer>();

			for (int j = 0; j < vizinhos.length; j++) {
				for (int k = j + 1; k < vizinhos.length; k++) {
					if (distanceMatrix[j][k] <= EPS) {
						vizinhos[j][k] = k;
						vizinhos[k][j] = j;
					} else {
						vizinhos[j][k] = -1;
						vizinhos[k][j] = -1;
					}
				}
			}

			for (int j = 0; j < vizinhos.length; j++) {
				for (int k = j + 1; k < vizinhos.length; k++) {
					if (vizinhos[j][k] > -1) {
						objetosNoCluster.add(k);
					}
				}
			}

			return (((double) objetosNoCluster.size()) / instancesInCluster);
		}

		return 0;
	}

	public static double[][] baseDistanceMatrix(Instances instances, double[][] distance, int clusterID) {
		int numInstances = 0;
		int aux = instances.numInstances();

		for (int i = 0; i < aux; i++) {
			if (instances.instance(i).classValue() == ((double) clusterID)) {
				numInstances++;
			}
		}

		double[][] distanceMatrix = new double[numInstances][numInstances];

		int ii = 0, jj = 0;
		for (int i = 0; i < aux; i++) {
			for (int j = i; j < aux; j++) {
				if (instances.instance(i).classValue() == instances.instance(j).classValue()) {
					distanceMatrix[ii][jj] = distance[i][j];
					distanceMatrix[jj][ii] = distanceMatrix[ii][jj];
					jj++;

					if (jj == numInstances) {
						jj = 0;
						ii++;
						if (ii == numInstances) {
							ii = 0;
						}
					}
				}
			}
		}

		return distanceMatrix;
	}
}
