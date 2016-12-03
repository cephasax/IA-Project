package core;

import java.util.ArrayList;
import java.util.HashSet;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class Util {

	public static void printMatrix(double[][] clustering) {
		for (int i = 0; i < clustering.length; i++) {
			for (int j = 0; j < clustering[0].length; j++) {
				System.out.print(String.format("%10.3f", clustering[i][j]));
			}
			System.out.println();
		}
	}

	public static void printMatrix(int[][] clustering) {
		for (int i = 0; i < clustering.length; i++) {
			for (int j = 0; j < clustering[0].length; j++) {
				System.out.print(String.format("%5d", clustering[i][j]));
			}
			System.out.println();
		}
	}

	public static void replaceClassByConsensus(Instances instances, int[] consensus) {
		RelabelAndConsensus.remapToStartWithZero(consensus);
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

	public static double distance(Instance A, Instance B) {
		double diff = 0;
		for (int i = 1; i < A.numAttributes() - 1; i++) {
			if (A.attribute(i).isNominal())
				diff += A.value(i) == B.value(i) ? 0 : 1;
			else
				diff += Math.abs(A.value(i) - B.value(i));
		}
		return diff;
	}
}
