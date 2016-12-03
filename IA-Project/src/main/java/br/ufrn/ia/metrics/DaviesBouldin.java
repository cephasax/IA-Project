package br.ufrn.ia.metrics;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.utils.Util;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

// minimization objective (0,R) range
public class DaviesBouldin implements Fitness {

	public double evaluate(Instances instances, int[] clustering) {
		Util.replaceClassByConsensus(instances, clustering);

		/*
		 * try { PrintStream p = new PrintStream("IrisConsensus.arff");
		 * p.println(instances); p.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		Instances[] group = new Instances[instances.numClasses()];
		for (int i = 0; i < group.length; i++)
			group[i] = new Instances(instances, 0);

		for (int i = 0; i < instances.numInstances(); i++) {
			group[(int) instances.instance(i).classValue()].add(instances.instance(i));
		}

		double[] E = new double[group.length];
		Instance[] center = new Instance[group.length];
		for (int i = 0; i < group.length; i++) {
			center[i] = this.center(group[i]);
			center[i].setDataset(group[i]);
			E[i] = medianSquaredDistance(group[i], center[i]);
		}

		double db = 0;
		for (int i = 0; i < group.length; i++) {
			db += mrs(i, E, center);
		}
		db = db / (double) group.length;
		return db;
	}

	protected double mrs(int index, double[] E, Instance[] center) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < center.length; i++) {
			if (i != index) {
				double aux = rs(E[index], E[i], center[index], center[i]);
				if (aux > max) {
					max = aux;
				}
			}
		}
		return max;
	}

	protected double rs(double E1, double E2, Instance center1, Instance center2) {
		return (E1 + E2) / (distance(center1, center2));
	}

	protected double medianSquaredDistance(Instances base, Instance center) {
		double median = 0;
		for (int i = 0; i < base.numInstances(); i++)
			median += Math.pow(distance(base.instance(i), center), 2);
		return median / (double) base.numInstances();
	}

	private double distance(Instance a, Instance b) {
		double diff = 0;
		for (int i = 1; i < a.numAttributes() - 1; i++) {
			if (a.attribute(i).isNominal()) {
				diff += a.value(i) == b.value(i) ? 0 : 1;
			} else {
				diff += Math.abs(a.value(i) - b.value(i));
			}
		}
		return diff;
	}

	protected Instance center(Instances base) {
		Instance center = new SparseInstance(base.firstInstance());
		for (int i = 0; i < base.numAttributes(); i++) {
			if (base.attribute(i).isNominal())
				center.setValue(i, moda(base, base.attribute(i)));
			else if (base.attribute(i).isNumeric())
				center.setValue(i, median(base, base.attribute(i)));
			else
				throw new IllegalArgumentException(
						"Attribute " + base.attribute(i).name() + " not is numeric or nominal");
		}
		return center;
	}

	protected double moda(Instances base, Attribute att) {
		double[] count = new double[att.numValues()];
		for (int i = 0; i < base.numInstances(); i++)
			count[(int) base.instance(i).value(att)]++;
		return maxIndex(count);
	}

	protected double median(Instances base, Attribute att) {
		double median = 0;
		for (int i = 0; i < base.numInstances(); i++)
			median += base.instance(i).value(att);
		return median / (double) base.numInstances();
	}

	private int maxIndex(double... values) {
		int max = 0;
		for (int i = 1; i < values.length; i++)
			if (values[i] > values[max])
				max = i;
		return max;
	}
}
