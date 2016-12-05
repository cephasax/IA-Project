package br.ufrn.ia.metrics;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.Util;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

public class CalinskiHarabasz implements Fitness {

	private static DistanceFunction m_DistanceFunction;

	private static boolean m_PreserveOrder = false;
	
	public double evaluate(Instances instances, int[] consensus) {
		Instances dataset = new Instances(instances);
		Util util = new Util();
		util.replaceClassByConsensus(dataset, consensus);

		Instance datasetCenter = findCentroid(dataset);

		Instances[] clusters = new Instances[dataset.numClasses()];
		Instance[] centroids = new Instance[dataset.numClasses()];

		for (int i = 0; i < clusters.length; i++) {
			clusters[i] = new Instances(dataset, 0);
		}

		for (int i = 0; i < dataset.numInstances(); i++) {
			clusters[((Double) dataset.instance(i).classValue()).intValue()].add(dataset.instance(i));
		}

		for (int i = 0; i < centroids.length; i++) {
			centroids[i] = findCentroid(clusters[i]);
		}

		double numerator = 0, denominator = 0;

		// Numerator
		// ===========================================================
		for (int i = 0; i < clusters.length; i++) {
			m_DistanceFunction = new EuclideanDistance(clusters[i]);

			numerator += (clusters[i].numInstances() * Math.pow(m_DistanceFunction.distance(centroids[i], datasetCenter), 2));
		}
		numerator /= (dataset.numClasses() - 1);
		// ===========================================================

		// Denominator
		// ===========================================================
		int numInstances;
		for (int i = 0; i < clusters.length; i++) {
			m_DistanceFunction = new EuclideanDistance(clusters[i]);
			numInstances = clusters[i].numInstances();

			for (int j = 0; j < numInstances; j++) {
				denominator += Math.pow(m_DistanceFunction.distance(clusters[i].instance(j), centroids[i]), 2);
			}
		}

		denominator /= (dataset.numInstances() - dataset.numClasses());
		// ===========================================================

		return (numerator / denominator);
	}

	/**
	 * Fonte:
	 * https://github.com/IFMO-ML/FSSARecSys/blob/master/src/main/java/ru/
	 * ifmo/ctddev/FSSARecSys/utils/ClusterCentroid.java
	 */
	public static Instance findCentroid(Instances members) {
		m_DistanceFunction = new EuclideanDistance(members);

		double[] vals = new double[members.numAttributes()];

		// used only for Manhattan Distance
		Instances sortedMembers = null;
		int middle = 0;
		boolean dataIsEven = false;

		if (m_DistanceFunction instanceof ManhattanDistance) {
			middle = (members.numInstances() - 1) / 2;
			dataIsEven = ((members.numInstances() % 2) == 0);
			if (m_PreserveOrder) {
				sortedMembers = members;
			} else {
				sortedMembers = new Instances(members);
			}
		}

		for (int j = 0; j < members.numAttributes(); j++) {

			// in case of Euclidian distance the centroid is the mean point
			// in case of Manhattan distance the centroid is the median point
			// in both cases, if the attribute is nominal, the centroid is the
			// mode
			if (m_DistanceFunction instanceof EuclideanDistance || members.attribute(j).isNominal()) {
				vals[j] = members.meanOrMode(j);
			} else if (m_DistanceFunction instanceof ManhattanDistance) {
				// singleton special case
				if (members.numInstances() == 1) {
					vals[j] = members.instance(0).value(j);
				} else {
					vals[j] = sortedMembers.kthSmallestValue(j, middle + 1);
					if (dataIsEven) {
						vals[j] = (vals[j] + sortedMembers.kthSmallestValue(j, middle + 2)) / 2;
					}
				}
			}
		}
		return new DenseInstance(1.0, vals);
	}
}
