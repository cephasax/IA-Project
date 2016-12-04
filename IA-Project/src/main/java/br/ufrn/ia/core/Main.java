package br.ufrn.ia.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.PriorityQueue;

import br.ufrn.ia.core.optimizationMethods.AntColonyOptimization;
import br.ufrn.ia.core.optimizationMethods.BeeColonyOptimization;
import br.ufrn.ia.core.optimizationMethods.CoralReefOptimization;
import br.ufrn.ia.core.optimizationMethods.GeneticAlgorithm;
import br.ufrn.ia.core.optimizationMethods.ParticleSwarmOptimization;
import br.ufrn.ia.metrics.CalinskiHarabasz;
import br.ufrn.ia.metrics.CorrectRand;
import br.ufrn.ia.metrics.DaviesBouldin;
import br.ufrn.ia.metrics.Jaccard;
import br.ufrn.ia.metrics.MX;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.Utils;



public class Main {

	public static String logFile = "results.txt";

	public static final int epochs = 200;

	public static final int numMaxK = 10; // 2 - 10

	public static final int numRepetitions = 1;

	public static final int numPopulation = 50;

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			logFile = args[0];
		}

		PrintStream output = new PrintStream(new File(logFile + ".output"));
		System.setOut(output);

		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Congressional_Voting_Records, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Connectionist_Bench_Vowel, ARFF.Balance, ARFF.Pima_Indians_Diabetes };
		Fitness[] metrics = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX() };

		//bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Pima_Indians_Diabetes, ARFF.Balance, ARFF.Connectionist_Bench_Vowel, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Congressional_Voting_Records, ARFF.Dermatology, ARFF.Ionosphere, ARFF.Ecoli, ARFF.SolarFlare1, ARFF.Statlog_Heart, ARFF.Glass_Identification, ARFF.Automobile, ARFF.Wine, ARFF.Hepatitis, ARFF.Lung_Cancer };

		Hashtable<String, String> algorithmsParamenters = new Hashtable<String, String>();
		algorithmsParamenters.put("ACO", "ACO MST(false) AFN(true) Alpha(0.500000) Beta(0.500000) Ro(0.200000)");
		algorithmsParamenters.put("AG", "AG Crossover(0.900000) Mutation(0.100000)");
		algorithmsParamenters.put("BCO", "BCO TurnsMaxNotImproved(10)");
		algorithmsParamenters.put("CRO1", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.050000) StepsUntilDepredation(1)");
		algorithmsParamenters.put("CRO2", "CRO dimension(100) InsertRand(true) InsertRank(false) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("CRO3", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("PSO", "PSO P-OwnWay(0.950000) P-PreviousPosition(0.050000) P-BestPosition(0.000000)");

		PriorityQueue<String> queue = new PriorityQueue<String>();
		queue.addAll(algorithmsParamenters.keySet());
		System.out.print(String.format(Locale.ENGLISH, "%30s\t", ""));
		while (!queue.isEmpty())
			System.out.print(String.format(Locale.ENGLISH, "%10s\t", queue.poll()));
		System.out.println();

		PrintStream out = new PrintStream(logFile);
		out.println(String.format(Locale.ENGLISH, "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "K", "Fitness", "Objective", "Algorithm", "Base", "Cluster", "AlgorithmParameters", "Time(s)"));
		out.close();

		int numAlgorithms = algorithmsParamenters.size();
		for (ARFF arff : bases) {

			System.out.println(String.format("%30s\t", arff));
			for (Fitness fitness : metrics) {

				double[][] resultsCH = new double[numAlgorithms][(numMaxK - 1) * numRepetitions];
				double[][] resultsJC = new double[numAlgorithms][(numMaxK - 1) * numRepetitions];

				int rep = 0;
				for (int k = 2; k <= numMaxK; k++) {
					for (int current = 0; current < numRepetitions; current++) {
						Problem problem = new Problem(arff, fitness, k);
						Solve.problem = problem;

						int[][] clusterings = Main.getClusterings(arff, k);
						Solve[] start = new Solve[numPopulation];
						for (int i = 0; i < start.length; i++) {
							start[i] = new Solve(k, clusterings, Solve.pPartitions, Solve.pEquals);
							start[i].evaluate();
						}

						double[][] heuristicMachine = AntColonyOptimization.buildHeuristic2(k, clusterings);

						OptimizationAlgorithm[] optimizers = new OptimizationAlgorithm[] { new AntColonyOptimization(start.clone(), epochs, false, 0.5, 0.5, 0.2, heuristicMachine), new GeneticAlgorithm(start.clone(), epochs, 0.1, 0.9), new BeeColonyOptimization(start.clone(), epochs, 10), new CoralReefOptimization(start.clone(), epochs, 100, false, 0.9, 0.5, 0.8, 0.05, 1), new CoralReefOptimization(start.clone(), epochs, 100, true, 0.9, 0.5, 0.8, 0.1, 5), new CoralReefOptimization(start.clone(), epochs, 100, false, 0.9, 0.5, 0.8, 0.1, 5), new ParticleSwarmOptimization(start.clone(), epochs, 0.95, 0.05, 0) };

						for (int i = 0; i < optimizers.length; i++) {
							evaluate(problem, arff, k, optimizers[i]);
							Solve.problem = new Problem(arff, new CalinskiHarabasz(), k);
							Solve bestSolve = optimizers[i].getBestSolve();
							bestSolve.evaluate();
							resultsCH[i][rep] = bestSolve.cost;
							Solve.problem = new Problem(arff, new Jaccard(), k);
							bestSolve.evaluate();
							resultsJC[i][rep] = bestSolve.cost;

						}
						rep++;
					}
				}

				String[] message = new String[] { "(CH)", "(JC)" };
				double[][][] results = new double[][][] { resultsCH, resultsJC };
				for (int res = 0; res < results.length; res++) {
					double[][] avg = new double[results[res].length][numMaxK - 1];
					for (int i = 0; i < avg.length; i++) {
						double sum = 0;
						int count = 0;
						int index = 0;
						int numValids = 0;
						for (int j = 0; j < results[res][0].length; j++) {
							if (!Double.isInfinite(results[res][i][j])) {
								numValids++;
								sum += results[res][i][j];
							}
							count++;
							if (count % numRepetitions == 0) {
								avg[i][index] = sum / (double) numValids;
								count = 0;
								sum = 0;
								numValids = 0;
								index++;
							}
						}
					}

					double[][] t = transp(avg);
					int[][] rank = new int[t.length][t[0].length];
					for (int i = 0; i < rank.length; i++) {
						rank[i] = Analysis.getRank(t[i]);
					}
					int[][] tRank = transp(rank);
					System.out.print(String.format("%20s%10s\t", fitness.getClass().getSimpleName(), message[res]));
					for (int i = 0; i < tRank.length; i++) {
						System.out.print(String.format(Locale.ENGLISH, "%6.2f±%-6.2f\t", Analysis.average(tRank[i]), Analysis.desviation(tRank[i])));
					}
					System.out.println();

				}
			}
		}
	}

	public static Hashtable<ARFF, Double> evaluatePerformance(int k, ARFF[] arff) throws Exception {

		Hashtable<ARFF, Double> performance = new Hashtable<ARFF, Double>();
		for (ARFF base : arff) {
			Instances instances = new Instances(new FileReader(new File(base.location)));
			instances.setClassIndex(instances.numAttributes() - 1);
			double[][] distance = new double[instances.numInstances()][instances.numInstances()];
			for (int i = 0; i < distance.length; i++) {
				for (int j = 0; j < distance.length; j++) {
					distance[i][j] = Util.distance(instances.get(i), instances.get(j));
				}
			}

			Problem problem = new Problem(base, new MX(), k);
			Solve.problem = problem;

			int[][] clusterings = Main.getClusterings(base, k);
			Solve[] start = new Solve[1];
			for (int i = 0; i < start.length; i++) {
				start[i] = new Solve(k, clusterings, Solve.pPartitions, Solve.pEquals);
				start[i].evaluate();
			}
			double time = System.currentTimeMillis();
			OptimizationAlgorithm alg = new AntColonyOptimization(start, k, true, 0.5, 0.5, 0.2, distance);
			alg.run();
			time = (System.currentTimeMillis() - time) / 1000;
			performance.put(base, time);
			System.out.println(performance);
		}
		return performance;
	}

	public static void evaluate(Problem problem, ARFF arff, int numK, OptimizationAlgorithm alg) throws IOException {
		String parans = alg.toString();
		Solve.problem = problem;
		double time = System.currentTimeMillis();
		alg.run();
		time = (System.currentTimeMillis() - time) / 1000.0;
		Solve bestSolve = alg.getBestSolve();
		PrintStream out = new PrintStream(new FileOutputStream(new File(logFile), true));
		int[] cluster = bestSolve.cluster;
		RelabelAndConsensus.remapToStartWithZero(cluster);
		out.println(String.format(Locale.ENGLISH, "%d\t%f\t%s\t%s\t%s\t%s\t%s\t%10.5f", numK, bestSolve.cost, problem.getFitness().getClass().getSimpleName(), alg.getClass().getSimpleName(), arff.toString(), Arrays.toString(cluster), parans, time));
		out.close();
	}

	public static int[][] getClusterings(ARFF file, int k) throws Exception {
		Instances instances = new Instances(new FileReader(new File(file.location)));

		AbstractClusterer c1 = new HierarchicalClusterer();
		c1.setOptions(Utils.splitOptions("-N " + k + " -L SINGLE -P -A \"weka.core.EuclideanDistance -R first-last\""));

		AbstractClusterer c2 = new SimpleKMeans();
		c2.setOptions(Utils.splitOptions("-init 0 -max-candidates 100 -periodic-pruning 10000 -min-density 2.0 -t1 -1.25 -t2 -1.0 -N " + k + " -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -num-slots 1 -S 10"));

		AbstractClusterer c3 = new EM();
		c3.setOptions(Utils.splitOptions("-I 100 -N " + k + " -M 1.0E-6 -S 100"));

		int[][] clustering = new int[3][instances.numInstances()];
		AbstractClusterer[] c = new AbstractClusterer[] { c1, c2, c3 };
		for (int i = 0; i < c.length; i++) {
			c[i].buildClusterer(new Instances(instances));
			for (int j = 0; j < clustering[0].length; j++) {
				clustering[i][j] = c[i].clusterInstance(instances.get(j));
			}
		}

		RelabelAndConsensus.relabel(clustering);
		return clustering;
	}

	public static int[][] transp(int[][] values) {
		int[][] v = new int[values[0].length][values.length];
		for (int i = 0; i < v.length; i++)
			for (int j = 0; j < v[0].length; j++)
				v[i][j] = values[j][i];
		return v;
	}

	public static double[][] transp(double[][] values) {
		double[][] v = new double[values[0].length][values.length];
		for (int i = 0; i < v.length; i++)
			for (int j = 0; j < v[0].length; j++)
				v[i][j] = values[j][i];
		return v;
	}
}
