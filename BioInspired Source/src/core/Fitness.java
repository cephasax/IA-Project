package core;
import weka.core.Instances;

public interface Fitness {

	public double evaluate(Instances instances, int[] consensus);
	
	public boolean isMinimization ();
}
