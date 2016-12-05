package br.ufrn.ia.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Vector;

import weka.core.Instances;

public class Analysis {

	public void generateRankReport(int sizeK, Instances instances, double[][] values, boolean showTime) {
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
				System.out.print(String.format(Locale.ENGLISH, "%6.2f±%-6.2f\t", average(tRank[i]), desviation(tRank[i])));
			}
			System.out.println();
		} else {
			for (int i = 0; i < values.length; i++) {
				double[] column = new double[values[0].length];
				for (int j = 0; j < values[0].length; j++)
					column[j] = values[i][j];
				System.out.print(String.format(Locale.ENGLISH, "%6.2f±%-6.2f\t", average(column), desviation(column)));
			}
			System.out.println();
		}
	}

	public int[][] getConsensus(int k, File input, String parans, String arff, String objective) throws IOException {
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

	public Double[] getFitness(File input, String parans, String arff, String objective) throws IOException {
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

	public Double[] getTime(File input, String parans, String arff, String objective) throws IOException {
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

	public int[] getRank(double[] values) {
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

	public double average(double[] values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += values[i];
		return sum / values.length;
	}

	public double desviation(double[] values) {
		double x = average(values);
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += Math.pow(x - values[i], 2);
		return Math.sqrt(1.0 / (values.length - 1) * sum);
	}

	public double average(int[] values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += values[i];
		return sum / values.length;
	}

	public double desviation(int[] values) {
		double x = average(values);
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += Math.pow(x - values[i], 2);
		return Math.sqrt(1.0 / (values.length - 1) * sum);
	}

	public void mergeFiles(File a, File b) throws IOException {
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
