package metric;

import core.Fitness;
import weka.core.Instances;

public class Jaccard implements Fitness {
	
	public static void main (String [] args){
		new Jaccard().evaluate(null, null);
	}

	@Override
	public boolean isMinimization() { // quanto menor melhor
		return false;
	}
	
	@Override
	public double evaluate(Instances instances, int[] consensus) {
		int numInstances = instances.numInstances();
		int[] y = new int[numInstances];
		for (int i = 0; i < numInstances; i++)
			y[i] = ((Double) instances.instance(i).classValue()).intValue();
		
		double a1 = 0, a2 = 0, a3 = 0;
		for (int i = 0; i < numInstances; i++) {
			for (int j = 0; j < numInstances; j++) {
				if ((consensus[i] == consensus[j]) && (y[i] == y[j])) {
					a1++;
				} else if ((consensus[i] == consensus[j]) && (y[i] != y[j])) {
					a2++;
				} else if ((consensus[i] != consensus[j]) && (y[i] == y[j])) {
					a3++;
				}
			}
		}
		return a1 / (a1 + a2 + a3);
	}

}
