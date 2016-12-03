package br.ufrn.ia.utils;

import java.util.Random;

public class InitialPopulation {

	//Criacao da populacao inicial a partir do numero de k 
	public int[][][] createInitialPopulation(int numK, int numInstances){
		Random rand = new Random(0);
		int[][][] clustering = new int[10][3][numInstances];
		for (int i = 0; i < clustering.length; i++) {
			for (int j = 0; j < clustering[i].length; j++) {
				for (int k = 0; k < clustering[i][j].length; k++) {
					clustering[i][j][k] = rand.nextInt(numK);
				}
			}
		}
		return clustering;
	}
}
