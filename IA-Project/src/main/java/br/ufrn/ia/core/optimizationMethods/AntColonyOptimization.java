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
	 * Executa o algoritmo genético para os parâmetros informados.
	 * @see AntColonyOptimization#getBestSolve() para recuperar a solução após a execução de {@link AntColonyOptimization#run()}
	 * @see Util#distance(weka.core.Instance, weka.core.Instance) para calcular a distância entre as instâncias.
	 * 
	 * @param population Vetor de soluções iniciais. O tamanho do vetor determina o tamanho da população.
	 * @param epochs Quantidade de iterações do algoritmo. Valor maior ou igual a zero. Geralmente 100.
	 * @param heuristic Tipo de heuristics utilizada: true para hierarquical aglomerative e false para spanning tree divisive
	 * @param alpha Determina o quanto a solução considerará os feromônios na hora de escolher um componente da solução. 1 para todos os feromônios. Valor entre 0 e 1.
	 * @param beta Determina o quanto a solução considerará a heurística na hora de escolher um componente da solução. 1 para gerar uma solução construtiva. Valor entre 0 e 1.
	 * @param ro Determina quanto do feromônios será evaporada. 0 ele armazena todos os feromônios das iterações passadas.
	 * @param distance Informação heurística que determina a distância entre as instâncias.
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