package br.ufrn.ia.core;
import weka.core.Instances;

public interface Fitness {

	public double evaluate(Instances instances, int[] consensus);
}
