package br.ufrn.ia.metrics;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.Util;
import weka.core.Instance;
import weka.core.Instances;

public class Dom implements Fitness {

	public double evaluate(Instances instances, int[] consensus) {
		Instances y = new Instances(instances);
		Util util = new Util();
		util.replaceClassByConsensus(y, consensus);
		
		Instances[] clustersOriginal = new Instances[instances.numClasses()];
		Instances[] clustersModified = new Instances[y.numClasses()];

		for (int i = 0; i < clustersOriginal.length; i++)
			clustersOriginal[i] = new Instances(instances, 0);
		for (int i = 0; i < clustersModified.length; i++)
			clustersModified[i] = new Instances(y, 0);
		for (int i = 0; i < instances.numInstances(); i++)
			clustersOriginal[((Double) instances.instance(i).classValue()).intValue()].add(instances.instance(i));
		for (int i = 0; i < y.numInstances(); i++) 
			clustersModified[((Double) y.instance(i).classValue()).intValue()].add(y.instance(i));

		// ====================================================================
		double H = 0;
		double n = instances.numInstances();
		double nij, niDot;

		for (int i = 0; i < clustersOriginal.length; i++) {
			for (int j = 0; j < clustersModified.length; j++) {
				nij = commonObjectsBetween(clustersModified[j], clustersOriginal[i]);
				niDot = clustersModified[j].numInstances();
				double log = (nij == 0) ? 1 : Math.log(nij / niDot);
				H += ((nij / n) * log);
			}
		}

		// ===================================================================
		double sum = 0;
		for (int i = 0; i < clustersModified.length; i++) {
			sum += Math.log(combinationOf(clustersModified[i].numInstances() + clustersModified.length - 1, clustersModified.length - 1));
		}
		// ===================================================================

		double dom = H + sum * (1 / n);
		return dom;
	}

	public static int commonObjectsBetween(Instances clusterA, Instances clusterB) {
		int common = 0;
		int numInstsA = clusterA.numInstances();
		int numInstsB = clusterB.numInstances();

		for (int i = 0; i < numInstsA; i++) {
			for (int j = 0; j < numInstsB; j++) {
				if (compare(clusterA.instance(i), clusterB.instance(j))) {
					common++;
				}
			}
		}

		return common;
	}

	public static boolean compare(Instance A, Instance B) {
		int numAttr = A.numAttributes();
		for (int i = 0; i < numAttr; i++) {
			if (A.value(i) != B.value(i)) {
				return false;
			}
		}
		return true;
	}

	public static long combinationOf(long a, long b) {
		if ((a == b) || (b == 0))
			return 1;
		else if (b == (a - 1))
			return a;
		if (b > (a - b))
			b = a - b;
		return arrange(a, b) / factorial(b);
	}

	public static long factorial(long n) {
		if (n < 3) 
			return n;
		long x = 2;
		for (long i = 3; i <= n; i++)
			x *= i;
		return x;
	}

	public static long arrange(long a, long b) {
		if ((a == b) || (b == 0))
			return 1;
		long x = a;
		for (long i = x - 1; i >= (a - b + 1); i--)
			x *= i;
		return x;
	}
}
