package method;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;

import core.ARFF;
import core.Main;
import core.MinimumSpanningTree;
import core.OptimizationAlgorithm;
import core.Problem;
import core.Solve;
import core.Util;
import core.graph.Edge;
import metric.CalinskiHarabasz;
import metric.MX;
import weka.core.Instances;

public class AntColonyOptimization extends OptimizationAlgorithm {

	public static void main(String[] args) throws Exception {

		int numK = 2;
		ARFF base = ARFF.Lung_Cancer;
		Problem problem = new Problem(base, new MX(), numK);
		Solve.problem = problem;

		int numAnts = 1;
		int[][] clusterings = Main.getClusterings(base, numK);

		Solve[] start = new Solve[numAnts];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(numK, clusterings, Solve.pPartitions, Solve.pEquals);
			start[i].evaluate();
		}

		double [][] distance = AntColonyOptimization.buildHeuristic1(base, numK, clusterings);

		AntColonyOptimization aco = new AntColonyOptimization(start, 5, true, 0.3, 0.7, 0.2, distance);
		aco.run();

		Solve solve = aco.getBestSolve();

		System.out.println(solve);

		problem = new Problem(base, new CalinskiHarabasz(), numK);
		Solve.problem = problem;
		solve.evaluate();
		System.out.println(solve);
	}

	private int epochs;

	private double alpha;

	private double beta;

	private double ro;

	private Solve[] population;

	private double[][] distance;

	private Solve bestSolve;

	private boolean heuristic;

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
	public AntColonyOptimization(Solve[] population, int epochs, boolean heuristic, double alpha, double beta, double ro, double[][] distance) {
		this.epochs = epochs;
		this.population = population.clone();
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
	
	public static double [][] buildHeuristic1(ARFF arff, int numK, int[][]clusterings) throws IOException {
		Instances instances = new Instances(new FileReader(new File(arff.location)));
		instances.setClassIndex(instances.numAttributes() - 1);
		double[][] distance = new double[instances.numInstances()][instances.numInstances()];
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				distance[i][j] = Util.distance(instances.get(i), instances.get(j));
			}
		}
		return distance;
	}

	public static double[][] buildHeuristic2(int numK, int[][] clusterings) {
		double[][] heuristic = new double[clusterings[0].length][];
		for (int i = 0; i < heuristic.length; i++) {
			double[] votes = new double[numK];
			Arrays.fill(votes, 1);
			for (int j = 0; j < clusterings.length; j++) {
				votes[clusterings[j][i]]++;
			}
			heuristic[i] = votes;
		}
		return heuristic;
	}
}

class Ant {

	public Solve solve;

	public double[][] update;

	public Ant(Solve solve) {
		this.solve = new Solve(solve);
	}

	public void build(double[][] pheromone, double alpha, double beta, double[][] distance) {
		update = new double[distance.length][distance.length];

		double[][] distanceRemovedEdges = new double[distance.length][];
		for (int i = 0; i < distanceRemovedEdges.length; i++)
			distanceRemovedEdges[i] = distance[i].clone();

		int k = 1;
		while (k < solve.getNumClusteres()) {
			MinimumSpanningTree mst = new MinimumSpanningTree(distanceRemovedEdges, k);
			Hashtable<Edge, Double> edges = getProbabilities(pheromone, mst, alpha, beta);

			Edge e = roulette(edges);
			mst.removeEdge(e);
			update[e.v1][e.v2] = 1.0 / distance[e.v1][e.v2];
			distanceRemovedEdges[e.v1][e.v2] = Double.MAX_VALUE;

			solve.cluster = mst.getClustering();

			HashSet<Integer> numK = new HashSet<Integer>();
			for (int i = 0; i < solve.cluster.length; i++)
				numK.add(solve.cluster[i]);
			k = numK.size();
		}
		solve.evaluate();
	}

	private Hashtable<Edge, Double> getProbabilities(double[][] pheromone, MinimumSpanningTree mst, double alpha, double beta) {
		double sum = 0;
		Hashtable<Edge, Double> edges = new Hashtable<Edge, Double>();
		for (Edge e : mst.getMST()) {
			double gamma = Math.pow(pheromone[e.v1][e.v2], alpha);
			double ni = Math.pow(e.cost, beta);
			double p = gamma * ni;
			sum += p;
			edges.put(e, p);
		}

		if (sum == 0)
			sum = 1;

		for (Edge e : edges.keySet()) {
			double value = edges.get(e);
			edges.put(e, value / sum);
		}
		return edges;
	}

	public void build2(double[][] pheromone, double alpha, double beta, double[][] distance) {
		update = new double[distance.length][distance[0].length];
		for (int i = 0; i < distance.length; i++) {
			Hashtable<Edge, Double> edges = getProbabilities2(pheromone, i, distance[i], alpha, beta);
			Edge e = roulette(edges);
			update[e.v1][e.v2] = 1.0 / distance[e.v1][e.v2];
			solve.cluster[i] = e.v2;
		}
		solve.evaluate();
	}

	private Hashtable<Edge, Double> getProbabilities2(double[][] pheromone, int currentNode, double[] votes, double alpha, double beta) {
		double sum = 0;

		Hashtable<Edge, Double> edges = new Hashtable<Edge, Double>();
		for (int j = 0; j < votes.length; j++) {
			Edge e = new Edge(currentNode, j);
			e.cost = votes[j];
			double gamma = Math.pow(pheromone[e.v1][e.v2], alpha);
			double ni = Math.pow(e.cost, beta);
			double p = gamma * ni;
			sum += p;
			edges.put(e, p);
		}

		if (sum == 0)
			sum = 1;

		for (Edge e : edges.keySet()) {
			double value = edges.get(e);
			edges.put(e, value / sum);
		}
		return edges;
	}

	public <T> T roulette(Hashtable<T, Double> feromone) {
		double sum = 0;
		for (double d : feromone.values())
			sum += d + 1;
		double r = sum * Problem.rand.nextDouble();
		double current = 0;
		for (T key : feromone.keySet()) {
			current += (feromone.get(key) + 1.0) / sum;
			if (r <= current) {
				return key;
			}
		}
		return feromone.keys().nextElement();
	}

	public String toString() {
		return solve.toString();
	}
}