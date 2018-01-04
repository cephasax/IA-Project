package metric;

import core.Fitness;
import core.Util;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

public class Dunn implements Fitness {

	@Override
	public boolean isMinimization() { // quanto menor melhor
		return false;
	}

	@Override
	public double evaluate(Instances instances, int[] consensus) {
		Instances dataset = new Instances(instances);
		Util.replaceClassByConsensus(dataset, consensus);
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
		double distance = measure.distance(getAveragePoint(clusterA), getAveragePoint(clusterB));
		return distance;
	}

	public static Instance getAveragePoint(Instances instances) {
		double[] average = new double[instances.numAttributes()];
		for (int i = 0; i < instances.numInstances(); i++) {
			for (int j = 0; j < instances.numAttributes() - 1; j++) {
				average[j] += instances.get(i).value(j);
			}
		}
		for (int i = 0; i < average.length; i++)
			average[i] /= (double) instances.numInstances();
		return new DenseInstance(1.0, average);
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
