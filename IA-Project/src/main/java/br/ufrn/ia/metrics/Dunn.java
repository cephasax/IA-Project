package br.ufrn.ia.metrics;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.Util;
import weka.core.EuclideanDistance;
import weka.core.Instances;

public class Dunn implements Fitness {

	public double evaluate(Instances instances, int[] consensus) {
		Instances dataset = new Instances(instances);
		Util util = new Util();
		util.replaceClassByConsensus(dataset, consensus);
		Instances[] clusters = new Instances[dataset.numClasses()];
		double aux;

		for (int i = 0; i < clusters.length; i++) {
			clusters[i] = new Instances(dataset, 0);
		}

		for (int i = 0; i < dataset.numInstances(); i++) {
			clusters[((Double) dataset.instance(i).classValue()).intValue()].add(dataset.instance(i));
		}

		if (clusters.length == 1) {
			return distanceBetweenClusters(clusters[0], clusters[0]) / clusterDiameter(clusters[0]);
		}

		double maxDiameter = clusterDiameter(clusters[0]);
		for (int i = 1; i < clusters.length; i++) {
			aux = clusterDiameter(clusters[i]);
			maxDiameter = (aux > maxDiameter) ? aux : maxDiameter;
		}

		double dunn = distanceBetweenClusters(clusters[0], clusters[1]) / maxDiameter;

		for (int i = 0; i < clusters.length; i++) {
			for (int j = i + 1; j < clusters.length; j++) {
				aux = distanceBetweenClusters(clusters[i], clusters[j]) / maxDiameter;
				if (aux < dunn) {
					dunn = aux;
				}
			}
		}

		return dunn;
	}

	public static double distanceBetweenClusters(Instances clusterA, Instances clusterB) {
		EuclideanDistance measure = new EuclideanDistance(clusterA);

		int numInstancesA = clusterA.numInstances();
		int numInstancesB = clusterB.numInstances();

		double min = measure.distance(clusterA.firstInstance(), clusterB.firstInstance());
		double aux;

		for (int i = 0; i < numInstancesA; i++) {
			for (int j = 0; j < numInstancesB; j++) {
				aux = measure.distance(clusterA.instance(i), clusterB.instance(j));
				if (aux < min) {
					min = aux;
				}
			}
		}

		return min;
	}

	public static double clusterDiameter(Instances cluster) {
		EuclideanDistance measure = new EuclideanDistance(cluster);
		int numInstances = cluster.numInstances();

		double max = measure.distance(cluster.instance(0), cluster.instance(0));
		double aux;

		for (int i = 0; i < numInstances; i++) {
			for (int j = i + 1; j < numInstances; j++) {
				aux = measure.distance(cluster.instance(i), cluster.instance(j));
				if (aux > max) {
					max = aux;
				}
			}
		}
		return max;
	}
}
