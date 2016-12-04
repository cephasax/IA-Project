package br.ufrn.ia.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Vector;

import br.ufrn.ia.metrics.CalinskiHarabasz;
import br.ufrn.ia.metrics.CorrectRand;
import br.ufrn.ia.metrics.DaviesBouldin;
import br.ufrn.ia.metrics.Dom;
import br.ufrn.ia.metrics.Dunn;
import br.ufrn.ia.metrics.Jaccard;
import br.ufrn.ia.metrics.MX;
import weka.core.Instances;

public class Analysis {

	public static void main(String[] args) throws IOException {

		Hashtable<String, String> algorithmsParamenters = new Hashtable<String, String>();
		algorithmsParamenters.put("ACO", "ACO MST(false) AFN(true) Alpha(0.500000) Beta(0.500000) Ro(0.200000)");
		algorithmsParamenters.put("AG", "AG Crossover(0.900000) Mutation(0.100000)");
		algorithmsParamenters.put("BCO", "BCO TurnsMaxNotImproved(10)");
		algorithmsParamenters.put("CRO1", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.050000) StepsUntilDepredation(1)");
		algorithmsParamenters.put("CRO2", "CRO dimension(100) InsertRand(true) InsertRank(false) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("CRO3", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("PSO", "PSO P-OwnWay(0.950000) P-PreviousPosition(0.050000) P-BestPosition(0.000000)");

		//System.out.println(Arrays.toString(getValues(new File("results.txt"), algorithmsParamenters.get("ACO1"), "Lung_Cancer", "CorrectRand")));

		/*
		File results = new File("resultsAll.txt");
		results.delete();
		for (int i = 0; i < 5; i++) {
			mergeFiles(results, new File("resultsS" + (i + 1) + ".txt"));
		} //*/
			File results = new File("results.txt");

		int sizeK = Main.numMaxK - 1;
		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Congressional_Voting_Records, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Connectionist_Bench_Vowel, ARFF.Balance, ARFF.Pima_Indians_Diabetes };
		bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis };

		Fitness[] metrics = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX() };

		Fitness[] evaluate = new Fitness[] { new CalinskiHarabasz(), new Jaccard(), new Dunn(), new Dom() };

		System.out.println(String.format(Locale.ENGLISH, "%40s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s", "", "ACO", "AG", "BCO", "CRO1", "CRO2", "CRO3", "PSO"));
		for (Fitness eval : evaluate) {
			System.out.println(String.format("%-40s", eval.getClass().getSimpleName()));
			for (int m = 0; m < metrics.length; m++) {
				System.out.println(String.format("%-40s", metrics[m].getClass().getSimpleName()));
				for (ARFF arff : bases) {
					Instances instances = new Instances(new FileReader(new File(arff.location)));
					instances.setClassIndex(instances.numAttributes() - 1);
					System.out.print(String.format("%40s\t", arff.toString(), instances.numInstances()));

					boolean showTime = false;
					double[][] values = new double[algorithmsParamenters.size()][sizeK * Main.numRepetitions];
					if (!showTime) {
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							for (int k = 0; k < sizeK; k++) {
								int[][] consensus = getConsensus(k + 2, results, algorithmsParamenters.get(algorithm), arff.toString(), metrics[m].getClass().getSimpleName());
								Problem p = new Problem(arff, eval, k + 2);
								Solve.problem = p;
								for (int i = 0; i < consensus.length; i++) {
									Solve solve = new Solve(consensus[i]);
									solve.evaluate();
									values[count][(k * consensus.length) + i] = solve.cost;
								}
							}
							count++;
						}
					} else {
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							Double[] v = getTime(results, algorithmsParamenters.get(algorithm), arff.toString(), metrics[m].getClass().getSimpleName());
							values[count] = new double[v.length];
							for (int i = 0; i < v.length; i++)
								values[count][i] = v[i];
							if (v.length == 0)
								System.out.println(algorithm);
							count++;
						}
					}

					generateRankReport(sizeK, instances, values, showTime);
				}
			}
		}

	}

	public static void generateRankReport(int sizeK, Instances instances, double[][] values, boolean showTime) {
		if (!showTime) {
			int repetitions = values[0].length / sizeK;
			double[][] avgRepetitions = new double[values.length][sizeK];
			for (int i = 0; i < avgRepetitions.length; i++) {
				double sum = 0;
				int count = 0;
				int index = 0;
				int numValids = 0;
				for (int j = 0; j < values[0].length; j++) {
					if (!Double.isInfinite(values[i][j])) {
						numValids++;
						sum += values[i][j];
					}
					count++;
					if (count % repetitions == 0) {
						avgRepetitions[i][index] = sum / (double) numValids;
						count = 0;
						sum = 0;
						numValids = 0;
						index++;
					}
				}
			}

			double[][] tValues = Main.transp(avgRepetitions);
			int[][] rank = new int[tValues.length][];
			for (int i = 0; i < rank.length; i++) {
				rank[i] = getRank(tValues[i]);
			}

			int[][] tRank = Main.transp(rank);
			for (int i = 0; i < tRank.length; i++) {
				System.out.print(String.format(Locale.ENGLISH, "%6.2f±%-6.2f\t", Analysis.average(tRank[i]), Analysis.desviation(tRank[i])));
			}
			System.out.println();
		} else {
			for (int i = 0; i < values.length; i++) {
				double[] column = new double[values[0].length];
				for (int j = 0; j < values[0].length; j++)
					column[j] = values[i][j];
				System.out.print(String.format(Locale.ENGLISH, "%6.2f±%-6.2f\t", Analysis.average(column), Analysis.desviation(column)));
			}
			System.out.println();
		}
	}

	public static int[][] getConsensus(int k, File input, String parans, String arff, String objective) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(input));

		Vector<int[]> values = new Vector<int[]>();

		int indexK = 0;
		int indexValue = 1;
		int indexFitness = 2;
		int indexAlgorithm = 3;
		int indexBase = 4;
		int indexCluster = 5;
		int indexParans = 6;

		String stringK = Integer.toString(k);
		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			String[] lineSplit = line.split("\t");
			if (lineSplit[indexK].equals(stringK) && lineSplit[indexFitness].equals(objective) && lineSplit[indexBase].equals(arff) && lineSplit[indexParans].equals(parans)) {
				line = lineSplit[indexCluster].substring(1, lineSplit[indexCluster].length() - 1);
				lineSplit = line.split(", ");
				int[] consensus = new int[lineSplit.length];
				for (int i = 0; i < consensus.length; i++)
					consensus[i] = Integer.parseInt(lineSplit[i]);
				values.add(consensus);
			}
			line = in.readLine();
		}
		in.close();

		return values.toArray(new int[][] {});
	}

	public static Double[] getFitness(File input, String parans, String arff, String objective) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(input));

		Vector<Double> values = new Vector<Double>();

		int indexK = 0;
		int indexValue = 1;
		int indexFitness = 2;
		int indexAlgorithm = 3;
		int indexBase = 4;
		int indexCluster = 5;
		int indexParans = 6;

		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			String[] lineSplit = line.split("\t");
			if (lineSplit[indexFitness].equals(objective) && lineSplit[indexBase].equals(arff) && lineSplit[indexParans].equals(parans))
				values.add(Double.parseDouble(lineSplit[indexValue]));
			line = in.readLine();
		}
		in.close();

		return values.toArray(new Double[] {});
	}

	public static Double[] getTime(File input, String parans, String arff, String objective) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(input));

		Vector<Double> values = new Vector<Double>();

		int indexK = 0;
		int indexValue = 1;
		int indexFitness = 2;
		int indexAlgorithm = 3;
		int indexBase = 4;
		int indexCluster = 5;
		int indexParans = 6;
		int indexTime = 7;

		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			String[] lineSplit = line.split("\t");
			if (lineSplit[indexFitness].equals(objective) && lineSplit[indexBase].equals(arff) && lineSplit[indexParans].equals(parans))
				values.add(Double.parseDouble(lineSplit[indexTime]));
			line = in.readLine();
		}
		in.close();

		return values.toArray(new Double[] {});
	}

	public static int[] getRank(double[] values) {
		int count = 1;
		double previous = -1;
		int previousRank = 0;
		for (int i = 0; i < values.length; i++) {
			if (Double.isInfinite(values[i]) || Double.isNaN(values[i]))
				values[i] = Double.MAX_VALUE / 2;
		}
		int[] rank = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			int min = -1;
			for (int j = 0; j < values.length; j++) {
				if (min == -1 || values[j] < values[min])
					min = j;
			}
			rank[min] = values[min] == previous ? previousRank : count;
			previousRank = rank[min];
			previous = values[min];
			values[min] = Double.MAX_VALUE;
			count++;
		}
		return rank;
	}

	public static double average(double[] values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += values[i];
		return sum / values.length;
	}

	public static double desviation(double[] values) {
		double x = average(values);
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += Math.pow(x - values[i], 2);
		return Math.sqrt(1.0 / (values.length - 1) * sum);
	}

	public static double average(int[] values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += values[i];
		return sum / values.length;
	}

	public static double desviation(int[] values) {
		double x = average(values);
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += Math.pow(x - values[i], 2);
		return Math.sqrt(1.0 / (values.length - 1) * sum);
	}

	public static void mergeFiles(File a, File b) throws IOException {
		PrintStream out = new PrintStream(new FileOutputStream(a.getName(), true));
		BufferedReader in = new BufferedReader(new FileReader(b));
		String line = in.readLine();
		while (line != null) {
			out.println(line);
			line = in.readLine();
		}
		out.close();
		in.close();
	}
}
