package br.ufrn.ia.core.optimizationMethods;

import java.util.Arrays;
import java.util.Locale;

import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.core.Util;
import br.ufrn.ia.naturalDomain.Ant;

public class AntColonyOptimization extends OptimizationAlgorithm {

	private int epochs;
	private double alpha;
	private double beta;
	private double ro;
	private double[][] distance;
	private Solve bestSolve;
	private boolean heuristic;

	public AntColonyOptimization(){
		
	}
	
	/**
	 * Executa o algoritmo gen�tico para os par�metros informados.
	 * @see AntColonyOptimization#getBestSolve() para recuperar a solu��o ap�s a execu��o de {@link AntColonyOptimization#run()}
	 * @see Util#distance(weka.core.Instance, weka.core.Instance) para calcular a dist�ncia entre as inst�ncias.
	 * 
	 * @param population Vetor de solu��es iniciais. O tamanho do vetor determina o tamanho da popula��o.
	 * @param epochs Quantidade de itera��es do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param heuristic Tipo de heuristics utilizada: true para hierarquical aglomerative e false para spanning tree divisive
	 * @param alpha Determina o quanto a solu��o considerar� os ferom�nios na hora de escolher um componente da solu��o. 1 para todos os ferom�nios. Valor entre 0 e 1.
	 * @param beta Determina o quanto a solu��o considerar� a heur�stica na hora de escolher um componente da solu��o. 1 para gerar uma solu��o construtiva. Valor entre 0 e 1.
	 * @param ro Determina quanto do ferom�nios ser� evaporada. 0 ele armazena todos os ferom�nios das itera��es passadas.
	 * @param distance Informa��o heur�stica que determina a dist�ncia entre as inst�ncias.
	 */
	public AntColonyOptimization(int epochs, boolean heuristic, double alpha, double beta, double ro, double[][] distance) {
		this.epochs = epochs;
		this.heuristic = heuristic;
		this.alpha = alpha;
		this.beta = beta;
		this.ro = ro;
		this.distance = distance;
	}

	@Override
	public void run() {

		double[][] pheromone = new double[distance.length][distance[0].length];
		for (int i = 0; i < pheromone.length; i++)
			Arrays.fill(pheromone[i], 1.0);

		Ant[] ants = new Ant[population.length];
		for (int i = 0; i < ants.length; i++)
			ants[i] = new Ant(population[i]);

		int stepsUpdate = 0;
		while (epochs-- > 0 && stepsUpdate++ < maxStepsWhitoutUpdate) {

			for (int i = 0; i < ants.length; i++) {
				if (heuristic) {
					ants[i].build(pheromone, alpha, beta, distance);
				} else {
					ants[i].build2(pheromone, alpha, beta, distance);
				}
			}

			for (int i = 0; i < pheromone.length; i++) {
				for (int j = 0; j < pheromone[i].length; j++) {
					double delta = 0;
					for (int k = 0; k < ants.length; k++)
						delta += ants[k].update[i][j];
					pheromone[i][j] = (1 - ro) * pheromone[i][j] + delta;
				}
			}
			for (int i = 0; i < ants.length; i++){
				if (bestSolve == null || ants[i].solve.cost < bestSolve.cost){
					bestSolve = new Solve(ants[i].solve);
					stepsUpdate = 0;
				}
			}

		}
	}

	@Override
	public Solve getBestSolve() {
		return new Solve(bestSolve);
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "ACO MST(%b) AFN(%b) Alpha(%f) Beta(%f) Ro(%f)", heuristic, !heuristic, alpha, beta, ro);
	}
	
	public double[][] getDistance() {
		return distance;
	}

	public void setDistance(double[][] distance) {
		this.distance = distance;
	}

	public int getEpochs() {
		return epochs;
	}

	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	public Solve[] getPopulation() {
		return population;
	}

	public void setPopulation(Solve[] population) {
		this.population = population.clone();
	}
	
	
	
	
}