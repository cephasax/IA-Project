package metric;

import core.Fitness;
import core.Util;
import weka.core.Instances;

public class DunnAntonino implements Fitness {

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
		
		
		
		return 0;
	}

	@Override
	public boolean isMinimization() {
		return true;
	}
}
