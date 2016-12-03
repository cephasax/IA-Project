package br.ufrn.ia.metrics;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.utils.Util;
import weka.core.Instances;

public class CorrectRand implements Fitness {

	public double evaluate(Instances instances, int[] consensus) {
		Instances instancesY = new Instances(instances);
		Util.replaceClassByConsensus(instancesY, consensus);

		int[][] table = new int[instances.numClasses() + 1][instancesY.numClasses() + 1];

		int numInstances = instances.numInstances();

		int xClass, yClass;
		for (int i = 0; i < numInstances; i++) {
			xClass = (int) instances.instance(i).classValue();
			yClass = (int) instancesY.instance(i).classValue();
			table[xClass][yClass]++;
		}

		for (int i = 0; i < table.length - 1; i++) {
			for (int j = 0; j < table[i].length - 1; j++) {
				table[table.length - 1][j] += table[i][j];
				table[i][table[i].length - 1] += table[i][j];
			}
		}

		double TERMO_A = 0;
		for (int i = 0; i < table.length - 1; i++) {
			for (int j = 0; j < table[i].length - 1; j++) {
				TERMO_A += combinationOf(table[i][j], 2);
			}
		}

		double TERMO_B = 0;
		double TERMO_C = 0;
		for (int i = 0; i < table.length - 1; i++) {
			TERMO_B += combinationOf(table[i][table[i].length - 1], 2);
		}

		for (int i = 0; i < table[table.length - 1].length - 1; i++) {
			TERMO_C += combinationOf(table[table.length - 1][i], 2);
		}

		double TERMO_D = combinationOf(numInstances, 2);

		double INDEX = TERMO_A;
		double EXP_INDEX = (TERMO_B * TERMO_C) / TERMO_D;
		double MAX_INDEX = 0.5 * (TERMO_B + TERMO_C);

		double cr = ((INDEX - EXP_INDEX) / (MAX_INDEX - EXP_INDEX));
		return 1.0 - cr; // (1.0 - cr) minimization objective
	}

	private long combinationOf(long a, long b) {
		if ((a == b) || (b == 0))
			return 1;
		else if (b == (a - 1))
			return a;
		if (b > (a - b))
			b = Math.abs(a - b);
		return arrange(a, b) / factorial(b);
	}

	private long factorial(long n) {
		if (n < 3)
			return n;
		long x = 2;
		for (long i = 3; i <= n; i++)
			x *= i;
		return x;
	}

	private long arrange(long a, long b) {
		if ((a == b) || (b == 0))
			return 1;
		long x = a;
		for (long i = x - 1; i >= (a - b + 1); i--)
			x *= i;
		return x;
	}
}
