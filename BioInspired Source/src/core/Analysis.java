package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Vector;

import metric.CalinskiHarabasz;
import metric.CorrectRand;
import metric.DaviesBouldin;
import metric.Dom;
import metric.Dunn;
import metric.Jaccard;
import metric.MX;
import weka.core.Instances;

public class Analysis {

	enum Method {
		Time, Rank, Results, Raw
	}

	private static final Method method = Method.Raw;

	public static void main(String[] args) throws IOException {

		Hashtable<String, String> algorithmsParamenters = new Hashtable<String, String>();
		algorithmsParamenters.put("ACO", "ACO MST(false) AFN(true) Alpha(0.500000) Beta(0.500000) Ro(0.200000)");
		algorithmsParamenters.put("AG", "AG Crossover(0.900000) Mutation(0.100000)");
		algorithmsParamenters.put("BCO", "BCO TurnsMaxNotImproved(10)");
		algorithmsParamenters.put("CRO1", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.050000) StepsUntilDepredation(1)");
		algorithmsParamenters.put("CRO2", "CRO dimension(100) InsertRand(true) InsertRank(false) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("CRO3", "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)");
		algorithmsParamenters.put("PSO", "PSO P-OwnWay(0.950000) P-PreviousPosition(0.050000) P-BestPosition(0.000000)");

		/*
		File results = new File("C:\\Users\\Antonino\\Dropbox\\Experiments Clustering\\Resultados 5+\\P30I200.txt");
		results.delete();
		for (int i = 0; i < 10; i++) {
			mergeFiles(results, new File("C:\\Users\\Antonino\\Dropbox\\Experiments Clustering\\Resultados 5+\\Results.part" + (i + 1)));
		}
		System.exit(0);
		//*/

		String fileName = "P30I100.txt";

		File results = new File("C:\\Users\\Antonino\\Dropbox\\Experiments Clustering\\All Results\\" + fileName);

		int sizeK = Main.numMaxK - 1;
		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Congressional_Voting_Records, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Connectionist_Bench_Vowel, ARFF.Balance, ARFF.Pima_Indians_Diabetes, ARFF.Labor, ARFF.Pittsburgh_Bridges_V1, ARFF.Planning_Relax, ARFF.Flags, ARFF.Horse_Colic };
		
		Fitness[] metrics = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX() };

		//Fitness[] evaluate = new Fitness[] {new CalinskiHarabasz(), new Jaccard(), new Dunn(), new Dom() };
		Fitness[] evaluate = new Fitness[] {new Dom()};

		Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>> data = readData(results);

		//System.out.println(String.format(Locale.ENGLISH, "%40s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s", "", "ACO", "AG", "BCO", "CRO1", "CRO2", "CRO3", "PSO"));
		for (Fitness eval : evaluate) {
			
			//System.out.println(String.format("%s", eval.getClass().getSimpleName()));
			System.setOut(new PrintStream(fileName.substring(0, fileName.length()-4)+" " + eval.getClass().getSimpleName() + " " + method.toString() + ".txt"));
			
			for (int m = 0; m < metrics.length; m++) {
				System.out.println(String.format("%s", metrics[m].getClass().getSimpleName()));
				for (ARFF arff : bases) {
					Instances instances = new Instances(new FileReader(new File(arff.location)));
					instances.setClassIndex(instances.numAttributes() - 1);

					double[][] values = new double[algorithmsParamenters.size()][sizeK * Main.numRepetitions];
					if (method == Method.Rank) {
						System.out.print(String.format("%s\t", arff.toString(), instances.numInstances()));
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							for (int k = 0; k < sizeK; k++) {
								Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>> f = data.get(arff.toString());
								Hashtable<String, Hashtable<String, Vector<int[]>>> me = f.get(metrics[m].getClass().getSimpleName());
								Hashtable<String, Vector<int[]>> a = me.get(algorithmsParamenters.get(algorithm));
								Vector<int[]> numk = a.get(Integer.toString(k + 2));
								int[][] consensus = numk.toArray(new int[][] {});
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
						values = getRanked(sizeK, instances, values, eval.isMinimization());
						report(values);
					} else if (method == Method.Time) {
						System.out.print(String.format("%40s\t", arff.toString(), instances.numInstances()));
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							Double[] v = Analysis.getTime(results, algorithmsParamenters.get(algorithm), arff.toString(), metrics[m].getClass().getSimpleName());
							values[count] = new double[v.length];
							for (int i = 0; i < v.length; i++)
								values[count][i] = v[i];
							if (v.length == 0)
								System.out.println(algorithm);
							count++;
						}
						report(values);
					} else if (method == Method.Results) {
						System.out.print(String.format("%40s\t", arff.toString(), instances.numInstances()));
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							for (int k = 0; k < sizeK; k++) {
								Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>> f = data.get(arff.toString());
								Hashtable<String, Hashtable<String, Vector<int[]>>> me = f.get(metrics[m].getClass().getSimpleName());
								Hashtable<String, Vector<int[]>> a = me.get(algorithmsParamenters.get(algorithm));
								Vector<int[]> numk = a.get(Integer.toString(k + 2));
								int[][] consensus = numk.toArray(new int[][] {});
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
						report(values);
					} else if (method == Method.Raw) {
						PriorityQueue<String> queue = new PriorityQueue<String>();
						queue.addAll(algorithmsParamenters.keySet());
						int count = 0;
						while (!queue.isEmpty()) {
							String algorithm = queue.poll();
							for (int k = 0; k < sizeK; k++) {
								Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>> f = data.get(arff.toString());
								Hashtable<String, Hashtable<String, Vector<int[]>>> me = f.get(metrics[m].getClass().getSimpleName());
								Hashtable<String, Vector<int[]>> a = me.get(algorithmsParamenters.get(algorithm));
								Vector<int[]> numk = a.get(Integer.toString(k + 2));
								int[][] consensus = numk.toArray(new int[][] {});
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
						reportRaw(values);
					}
				}
			}
		}

	}

	private static Double[] getTime(File results, String algorithm, String arff, String metric) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(results));

		int indexFitness = 2;
		int indexBase = 4;
		int indexParans = 6;
		int indexTime = 7;

		Vector<Double> values = new Vector<Double>();
		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			String[] lineSplit = line.split("\t");
			if (lineSplit.length == 8 && !lineSplit[0].equals("K") && lineSplit[indexBase].equals(arff) && lineSplit[indexParans].equals(algorithm) && lineSplit[indexFitness].equals(metric)) {
				double v = Double.parseDouble(lineSplit[indexTime]);
				values.add(v);
			}
			line = in.readLine();
		}
		in.close();

		return values.toArray(new Double[] {});
	}

	public static Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>> readData(File input) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(input));

		// base -> ( fitness -> ( algorithm -> (k -> consensus )) )
		Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>> data = new Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>>();

		int indexK = 0;
		int indexFitness = 2;
		int indexBase = 4;
		int indexCluster = 5;
		int indexParans = 6;

		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			String[] lineSplit = line.split("\t");
			if (lineSplit.length > 1)
				lineSplit[indexBase] = lineSplit[indexBase].replace(" ", "_");
			if (lineSplit.length == 8 && !lineSplit[0].equals("K")) {
				String baseString = lineSplit[indexBase];
				Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>> fitness = data.get(baseString);
				if (fitness == null) {
					fitness = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>();
					data.put(baseString, fitness);
				}

				String fitnessString = lineSplit[indexFitness];
				Hashtable<String, Hashtable<String, Vector<int[]>>> algorithm = fitness.get(fitnessString);
				if (algorithm == null) {
					algorithm = new Hashtable<String, Hashtable<String, Vector<int[]>>>();
					fitness.put(fitnessString, algorithm);
				}

				String algorithmString = lineSplit[indexParans];
				Hashtable<String, Vector<int[]>> k = algorithm.get(algorithmString);
				if (k == null) {
					k = new Hashtable<String, Vector<int[]>>();
					algorithm.put(algorithmString, k);
				}

				String kString = lineSplit[indexK];
				Vector<int[]> consensus = k.get(kString);
				if (consensus == null) {
					consensus = new Vector<int[]>();
					k.put(kString, consensus);
				}

				line = lineSplit[indexCluster].substring(1, lineSplit[indexCluster].length() - 1);
				lineSplit = line.split(", ");
				int[] con = new int[lineSplit.length];
				for (int i = 0; i < con.length; i++)
					con[i] = Integer.parseInt(lineSplit[i]);

				consensus.add(con);
			}
			line = in.readLine();
		}
		in.close();

		return data;
	}

	public static double[][] getRanked(int sizeK, Instances instances, double[][] values, boolean minimum) {

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
		double[][] rank = new double[tValues.length][];
		for (int i = 0; i < rank.length; i++) {
			double[] r = minimum ? getMinimumRank(tValues[i]) : getMaximumRank(tValues[i]);
			rank[i] = new double[r.length];
			for (int j = 0; j < r.length; j++)
				rank[i][j] = r[j];
		}

		double[][] tRank = Main.transp(rank);
		return tRank;
	}

	private static void report(double[][] values) {
		for (int i = 0; i < values.length; i++) {
			double[] column = new double[values[0].length];
			for (int j = 0; j < values[0].length; j++)
				column[j] = values[i][j];

			Vector<Double> v = new Vector<Double>();
			for (int j = 0; j < column.length; j++)
				if (!Double.isInfinite(column[j]))
					v.add(column[j]);

			column = new double[v.size()];
			for (int j = 0; j < column.length; j++)
				column[j] = v.get(j);

			System.out.print(String.format(Locale.ENGLISH, "%.2f±%.2f\t", average(column), desviation(column)));
			//System.out.print(String.format(Locale.ENGLISH, "%6.2f\t", average(column), desviation(column)));
		}
		System.out.println();
	}

	private static void reportRaw(double[][] values) {
		for (int i = 0; i < values[0].length; i++) {
			for (int j = 0; j < values.length; j++)
				System.out.print(String.format(Locale.ENGLISH, "%6.4f\t", Double.isInfinite(values[j][i]) ? Double.MAX_VALUE : values[j][i]));
			System.out.println();
		}
	}

	public static double[] getMaximumRank(double[] values) {
		int count = 1;
		double previous = -1;
		double previousRank = 0;
		for (int i = 0; i < values.length; i++) {
			if (Double.isInfinite(values[i]) || Double.isNaN(values[i]))
				values[i] = -1;
		}
		double[] rank = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			int max = -1;
			for (int j = 0; j < values.length; j++) {
				if (max == -1 || values[j] > values[max])
					max = j;
			}
			rank[max] = values[max] == previous ? previousRank : count;
			previousRank = rank[max];
			previous = values[max];
			values[max] = -1;
			count++;
		}
		return rank;
	}

	public static double[] getMinimumRank(double[] values) {
		int count = 1;
		double previous = -1;
		double previousRank = 0;
		for (int i = 0; i < values.length; i++) {
			if (Double.isInfinite(values[i]) || Double.isNaN(values[i]))
				values[i] = Double.MAX_VALUE / 2;
		}
		double[] rank = new double[values.length];
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

	public static void mergeFiles(File a, File b) throws IOException {
		PrintStream out = new PrintStream(new FileOutputStream(a.getAbsolutePath(), true));
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
