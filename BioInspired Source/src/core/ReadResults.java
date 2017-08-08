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
import java.util.Vector;

public class ReadResults {

	private Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>>> data;

	private File results;

	public ReadResults(final File input) throws IOException {
		Locale.setDefault(Locale.ENGLISH);
		this.results = input;
		// base -> ( fitness -> ( algorithm -> (k -> (consensus,start) )) )
		data = new Hashtable<>();
		
		if(!input.exists()){
			PrintStream out = new PrintStream(new FileOutputStream(input, true));
			out.println(String.format(Locale.ENGLISH, "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "K", "Fitness", "Objective", "Algorithm", "Base", "Cluster", "StartSize", "StartClusterings", "AlgorithmParameters", "Time(s)"));
			out.close();
			return;
		}

		BufferedReader in = new BufferedReader(new FileReader(input));

		int indexK = 0;
		int indexFitness = 2;
		int indexAlgorithm = 3;
		int indexBase = 5;
		int indexCluster = 6;
		int indexSizeStart = 7;
		int indexStart = 8;

		String line = in.readLine(); // labels
		line = in.readLine();
		while (line != null) {
			final String[] lineSplit = line.split("\t");
			if (lineSplit.length == 11 && !lineSplit[0].equals("K")) {
				String baseString = lineSplit[indexBase];
				Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>> fitness = data.get(baseString);
				if (fitness == null) {
					fitness = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>>();
					data.put(baseString, fitness);
				}

				String fitnessString = lineSplit[indexFitness];
				Hashtable<String, Hashtable<String, Vector<Result>>> algorithm = fitness.get(fitnessString);
				if (algorithm == null) {
					algorithm = new Hashtable<String, Hashtable<String, Vector<Result>>>();
					fitness.put(fitnessString, algorithm);
				}

				String algorithmString = lineSplit[indexAlgorithm];
				Hashtable<String, Vector<Result>> k = algorithm.get(algorithmString);
				if (k == null) {
					k = new Hashtable<String, Vector<Result>>();
					algorithm.put(algorithmString, k);
				}

				String kString = lineSplit[indexK];
				Vector<Result> consensus = k.get(kString);
				if (consensus == null) {
					consensus = new Vector<Result>();
					k.put(kString, consensus);
				}

				line = lineSplit[indexCluster].substring(1, lineSplit[indexCluster].length() - 1);
				String[] conString = line.split(", ");
				int[] con = new int[conString.length];
				for (int i = 0; i < con.length; i++)
					con[i] = Integer.parseInt(conString[i]);

				int[][] start = new int[Integer.parseInt(lineSplit[indexSizeStart])][];
				String[] clusteringStart = lineSplit[indexStart].substring(0,lineSplit[indexStart].length()-1).split("] ");
				
				for (int i = 0; i < clusteringStart.length; i++) {

					line = clusteringStart[i].substring(1);
					String[] cluString = line.split(", ");
					int[] clustering = new int[cluString.length];
					for (int j = 0; j < con.length; j++)
						clustering[j] = Integer.parseInt(cluString[j]);
					start[i] = clustering;
				}

				Result result = new Result(con, start);
				consensus.add(result);
			}
			line = in.readLine();
		}
		in.close();
	}

	public synchronized void writeEvaluation(String base, String fitness, String algorithm, int seed, String parans, int k, double cost, int[] consensus, int[][] clusterings, double time) throws IOException {
		StringBuilder res = new StringBuilder(1000);
		res.append(k + "\t");
		res.append(cost + "\t");
		res.append(fitness + "\t");
		res.append(algorithm + "\t");
		res.append(seed + "\t");
		res.append(base + "\t");
		res.append(Arrays.toString(consensus) + "\t");
		res.append(clusterings.length + "\t");

		for (int i = 0; i < clusterings.length; i++) {
			res.append(Arrays.toString(clusterings[i]) + " ");
		}
		res.replace(res.length() - 1, res.length(), "\t");

		res.append(parans + "\t");
		res.append(String.format("%10.5f", time));
		
		PrintStream out = new PrintStream(new FileOutputStream(results, true));
		out.println(res.toString());
		out.close();
	}

	public boolean isFinished(String base, String fitness, String algorithm, int k, int repetition) {
		if (data.containsKey(base)) {
			Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>> fits = data.get(base);
			if (fits.containsKey(fitness)) {
				Hashtable<String, Hashtable<String, Vector<Result>>> algs = fits.get(fitness);
				if (algs.containsKey(algorithm)) {
					Hashtable<String, Vector<Result>> ks = algs.get(algorithm);
					String kString = Integer.toString(k);
					if (ks.containsKey(kString)) {
						Vector<Result> reps = ks.get(kString);
						if (repetition < reps.size()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public int[] getConsensus(String base, String fitness, String algorithm, int k, int repetition) {
		assert data.containsKey(base) : "No Data";
		Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>> fits = data.get(base);
		assert fits.containsKey(fitness) : "No Data";
		Hashtable<String, Hashtable<String, Vector<Result>>> algs = fits.get(fitness);
		assert algs.containsKey(algorithm) : "No Data";
		Hashtable<String, Vector<Result>> ks = algs.get(algorithm);
		String kString = Integer.toString(k);
		assert ks.containsKey(kString) : "No Data";
		Vector<Result> reps = ks.get(kString);
		assert repetition < reps.size() : "No Data";
		return reps.get(repetition).consensus;

	}

	public int[][] getStart(String base, String fitness, String algorithm, int k, int repetition) {
		assert data.containsKey(base) : "No Data";
		Hashtable<String, Hashtable<String, Hashtable<String, Vector<Result>>>> fits = data.get(base);
		assert fits.containsKey(fitness) : "No Data";
		Hashtable<String, Hashtable<String, Vector<Result>>> algs = fits.get(fitness);
		assert algs.containsKey(algorithm) : "No Data";
		Hashtable<String, Vector<Result>> ks = algs.get(algorithm);
		String kString = Integer.toString(k);
		assert ks.containsKey(kString) : "No Data";
		Vector<Result> reps = ks.get(kString);
		assert repetition < reps.size() : "No Data";
		return reps.get(repetition).startClusterings;
	}

	private static class Result {
		public int[] consensus;
		public int[][] startClusterings;

		public Result(int[] consensus, int[][] start) {
			this.consensus = consensus;
			this.startClusterings = start;
		}
	}
}
