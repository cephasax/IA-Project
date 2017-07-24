package core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import metric.CalinskiHarabasz;
import metric.Dom;
import metric.Dunn;
import metric.Jaccard;
import metric.MX;
import weka.core.Instances;

public class Compare {

	public static void main(String[] args) throws IOException {
		Locale.setDefault(Locale.ENGLISH);

		File dir = new File("C:\\Users\\Antonino\\Dropbox\\Experiments Clustering\\Resultados");
		File[] files = { new File(dir, "P30I100.txt") };

		String[] algorithms = {
				"ACO MST(false) AFN(true) Alpha(0.500000) Beta(0.500000) Ro(0.200000)",
				"AG Crossover(0.900000) Mutation(0.100000)",
				"BCO TurnsMaxNotImproved(10)",
				"CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.050000) StepsUntilDepredation(1)",
				"CRO dimension(100) InsertRand(true) InsertRank(false) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)",
				"CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)",
				"PSO P-OwnWay(0.950000) P-PreviousPosition(0.050000) P-BestPosition(0.000000)"
		};

		String[] labels = new String[] { "ACO", "AG", "BCO", "CRO1", "CRO2", "CRO3", "PSO" };

		Fitness[] evals = { new CalinskiHarabasz(), new Jaccard(), new Dunn(), new Dom() };

		Fitness metric = new MX();

		for (Fitness eval : evals) {
			double[][] data = new double[4][];

			for (int i = 0; i < data.length; i++) {
				data[i] = getValues(files[0], metric, eval, algorithms[i]);
			}

			FriedmanTukey friedman = new FriedmanTukey();
			System.out.println("Friedman: " + friedman.evaluate(data));

			System.out.print(metric.getClass().getSimpleName() + "\t");
			for (int i = 0; i < labels.length; i++) {
				System.out.print(labels[i] + "\t");
			}
			System.out.println();

			for (int i = 0; i < data.length; i++) {
				System.out.print(labels[i] + "\t");
				for (int j = 0; j < i; j++) {
					System.out.print(String.format("%.4f\t", friedman.posthoc(i, j)));
				}
				System.out.println();
			}
			System.out.println();

			System.out.print(metric.getClass().getSimpleName() + "\t");
			for (int i = 0; i < labels.length; i++) {
				System.out.print(labels[i] + "\t");
			}
			System.out.println();
			for (int i = 0; i < data.length; i++) {
				System.out.print(labels[i] + "\t");
				for (int j = 0; j < i; j++) {
					if (friedman.posthoc(i, j) < 0.05) {
						boolean Ibetter = metric.isMinimization() ? average(data[i]) > average(data[j]) : average(data[i]) > average(data[j]);
						String label = Ibetter ? labels[i] : labels[j];
						System.out.print(String.format("%s\t", label));
					} else {
						System.out.print("-\t");
					}
				}
				System.out.println();
			}
			System.out.println();

		}
		/*
		double[][] data = new double[6][];
		
		for (int i = 0; i < data.length; i++) {
			data[i] = getValues(files[i], new CorrectRand(), algorithms[0]);
		}
		
		// mesmo algoritmo diferente base de dados
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				System.out.print(data[j][i] + " ");
			}
			System.out.println();
		}
		//*/
	}

	private static double[] getValues(File file, Fitness metrics, Fitness eval, String algorithm) throws IOException {
		Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>>> data = Analysis.readData(file);

		int sizeK = Main.numMaxK - 1;
		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology, ARFF.Congressional_Voting_Records, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.Connectionist_Bench_Vowel, ARFF.Balance, ARFF.Pima_Indians_Diabetes };
		//bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Hepatitis, ARFF.Wine, ARFF.Automobile, ARFF.Glass_Identification, ARFF.Statlog_Heart };

		double[] valuesInBases = new double[bases.length * sizeK * Main.numRepetitions];
		int count = 0;
		for (ARFF arff : bases) {
			Instances instances = new Instances(new FileReader(new File(arff.location)));
			instances.setClassIndex(instances.numAttributes() - 1);

			double[] values = new double[sizeK * Main.numRepetitions];
			for (int k = 0; k < sizeK; k++) {
				Hashtable<String, Hashtable<String, Hashtable<String, Vector<int[]>>>> f = data.get(arff.toString());
				Hashtable<String, Hashtable<String, Vector<int[]>>> me = f.get(metrics.getClass().getSimpleName());
				Hashtable<String, Vector<int[]>> a = me.get(algorithm);
				Vector<int[]> numk = a.get(Integer.toString(k + 2));
				int[][] consensus = numk.toArray(new int[][] {});
				Problem p = new Problem(arff, eval, k + 2);
				Solve.problem = p;
				for (int i = 0; i < consensus.length; i++) {
					Solve solve = new Solve(consensus[i]);
					solve.evaluate();
					values[(k * consensus.length) + i] = Double.isInfinite(solve.cost) ? 10000 : solve.cost;
				}
			}
			System.arraycopy(values, 0, valuesInBases, count * values.length, values.length);
			count++;
		}
		return valuesInBases;
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
}
