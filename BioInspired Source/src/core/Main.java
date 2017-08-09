package core;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import metric.CorrectRand;
import metric.DaviesBouldin;
import metric.MX;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.Utils;

public class Main {

	public static String logFile = "NewResults.txt";

	public static final int epochs = 100; // 50 100 200

	public static final int numMaxK = 10; // 2 - 10

	public static final int numRepetitions = 5; // 10

	public static final int numPopulation = 30; // 10 30 50

	//(10|50, 10|100, 10|200, 30|50, 30|100 e 30|200)

	public static void main(String[] args) throws Exception {
		//ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Congressional_Voting_Records, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Connectionist_Bench_Vowel, ARFF.Balance, ARFF.Pima_Indians_Diabetes, ARFF.Labor, ARFF.Pittsburgh_Bridges_V1, ARFF.Planning_Relax, ARFF.Flags, ARFF.Horse_Colic};
		
		ARFF[] base1 = new ARFF[] { ARFF.Connectionist_Bench_Vowel, ARFF.Glass_Identification, ARFF.Lung_Cancer, ARFF.Labor, ARFF.Pittsburgh_Bridges_V1, ARFF.Hepatitis, ARFF.Wine, ARFF.Planning_Relax, ARFF.Flags, ARFF.Automobile };
		ARFF[] base2 = new ARFF[] { ARFF.Breast_Cancer_Wisconsin_Original, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Statlog_Heart };
		ARFF[] base3 = new ARFF[] { ARFF.Balance, ARFF.Pima_Indians_Diabetes, ARFF.Horse_Colic, ARFF.Congressional_Voting_Records };

		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer };

		Fitness[] metrics = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX() };

		Algorithms[] algorithms = { Algorithms.ACO, Algorithms.AG, Algorithms.BCO, Algorithms.CRO1, Algorithms.CRO2, Algorithms.CRO3, Algorithms.PSO };

		int numThreads = 10;
		if (args.length > 1) {
			switch (args[1]) {
			case "NORMAL":
				bases = base1;
				numThreads = 10;
				break;
			case "IMD1":
				bases = base2;
				numThreads = 16;
				break;
			case "IMD2":
				bases = base3;
				numThreads = 16;
				break;
			}
		}

		if (args.length > 0) {
			logFile = args[0];
		}
		ReadResults writedResults = new ReadResults(new File(logFile));

		List<Runner> runners = new ArrayList<>();

		for (ARFF model : bases) {

			List<Database> all = new ArrayList<>();
			all.add(model);
			all.addAll(Arrays.asList(Split.split(model)));

			for (Database base : all) {
				for (Fitness fitness : metrics) {
					for (int k = 2; k <= numMaxK; k++) {
						for (int rep = 0; rep < numRepetitions; rep++) {
							int seed = k * numRepetitions + rep;
							Problem problem = new Problem(base, fitness, k);
							for (Algorithms algorithm : algorithms) {
								if (!writedResults.isFinished(base.toString(), fitness.getClass().getSimpleName(), algorithm.toString(), k, rep)) {
									runners.add(new Runner(algorithm, problem, base, seed, k, writedResults));
								}
							}
						}
					}
				}
			}
		}
		
		new ParallelProcessing(runners.toArray(new Runnable[runners.size()]), numThreads).start();
	}

	public static int[][] getClusterings(Database file, int k, int seed) throws Exception {
		FileReader fileReader = new FileReader(new File(file.getLocation()));
		Instances instances = new Instances(fileReader);
		fileReader.close();

		AbstractClusterer c1 = new HierarchicalClusterer();
		c1.setOptions(Utils.splitOptions("-N " + k + " -L SINGLE -P -A \"weka.core.EuclideanDistance -R first-last\""));

		AbstractClusterer c2 = new SimpleKMeans();
		c2.setOptions(Utils.splitOptions("-init 0 -max-candidates 100 -periodic-pruning 10000 -min-density 2.0 -t1 -1.25 -t2 -1.0 -N " + k + " -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -num-slots 1 -S " + seed));

		AbstractClusterer c3 = new EM();
		c3.setOptions(Utils.splitOptions("-I 100 -N " + k + " -M 1.0E-6 -S " + seed));

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
	
	private static class Runner implements Runnable {

		private Algorithms algorithm;

		private Problem problem;

		private Database arff;

		private int numK;

		private int seed;

		private ReadResults results;

		public Runner(Algorithms algorithm, Problem problem, Database arff, int seed, int numK, ReadResults results) {
			this.algorithm = algorithm;
			this.problem = problem;
			this.arff = arff;
			this.seed = seed;
			this.numK = numK;
			this.results = results;
		}

		public void run() {
			try {
				double time = System.currentTimeMillis();
				int[][] clusterings = getClusterings(arff, numK, seed);
				OptimizationAlgorithm alg = algorithm.newInstance(problem, seed, numK, clusterings);
				alg.run();
				time = (System.currentTimeMillis() - time) / 1000.0;
				Solve bestSolve = alg.getBestSolve();
				int[] cluster = bestSolve.cluster;
				RelabelAndConsensus.remapToStartWithZero(cluster);
				results.writeEvaluation(arff.toString(), problem.getFitness().getClass().getSimpleName(), algorithm.toString(), seed, algorithm.getDescription(), numK, bestSolve.cost, cluster, clusterings, time);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
}
