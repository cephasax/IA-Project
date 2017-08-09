package core;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import metric.CalinskiHarabasz;
import metric.CorrectRand;
import metric.DaviesBouldin;
import metric.Dom;
import metric.Dunn;
import metric.Jaccard;
import metric.MX;

public class AnalysisMetrics {

	public static String logFile = "output.txt";

	enum EvaluationMethod {
		Value, Norm, NormNeg
	}

	enum ViewMethod {
		Rank, Value
	}

	private static EvaluationMethod method = EvaluationMethod.Value;

	private static ViewMethod view = ViewMethod.Value;

	public static void main(String[] args) throws IOException {

		//File output = new File("output.txt");
		//Analysis.mergeFiles(output, new File("log_normal.txt"));
		//Analysis.mergeFiles(output, new File("log_imd1.txt"));

		System.setOut(new PrintStream("Results2.txt"));

		ReadResults results = new ReadResults(new File(logFile));

		ARFF[] base1 = new ARFF[] { ARFF.Lung_Cancer, ARFF.Labor, ARFF.Pittsburgh_Bridges_V1, ARFF.Hepatitis, ARFF.Wine, ARFF.Planning_Relax, ARFF.Flags, ARFF.Automobile, ARFF.Connectionist_Bench_Vowel, ARFF.Glass_Identification };
		ARFF[] base2 = new ARFF[] { ARFF.Statlog_Heart, ARFF.Breast_Cancer_Wisconsin_Original, ARFF.SolarFlare1, ARFF.Ecoli, ARFF.Ionosphere, ARFF.Dermatology };
		ARFF[] base3 = new ARFF[] { ARFF.Horse_Colic, ARFF.Congressional_Voting_Records, ARFF.Balance, ARFF.Pima_Indians_Diabetes };

		ARFF[] bases = new ARFF[] { ARFF.Lung_Cancer, ARFF.Labor, ARFF.Pittsburgh_Bridges_V1, ARFF.Hepatitis, ARFF.Wine, ARFF.Planning_Relax, ARFF.Statlog_Heart};

		Fitness[] metrics = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX() };
		//metrics = new Fitness[] { new DaviesBouldin()};//, new MX() };
		Fitness[] evaluate = new Fitness[] { new CalinskiHarabasz(), new Jaccard(), new Dunn(), new Dom() };
		//evaluate = new Fitness[] { new CorrectRand(), new DaviesBouldin(), new MX()};//, new MX(), new CalinskiHarabasz(), new Jaccard(), new Dunn(), new Dom() };

		Algorithms[] algorithms = { Algorithms.ACO, Algorithms.AG, Algorithms.BCO, Algorithms.CRO1, Algorithms.CRO2, Algorithms.CRO3, Algorithms.PSO };

		String[] messages = { "Robustez", "Novidade(Distance)", "Novidade(Similarity)", "Estabilidade" };
		String[][] metricMessages = { { "Menor", "Média" }, { "Menor", "Média" }, { "Menor", "Média" }, { "Média", "Desvio" } };

		for (Fitness eval : evaluate) {
		for (Fitness metric : metrics) {
			//Fitness eval = metric;
			// (norm|rank|normNeg|rank|value|rank) -> metric -> base -> (m1|m2) -> algorithm -> (minimum|average)
			double[][][][][][] values = new double[6][messages.length][bases.length][][][];
			for (int m = 0; m < bases.length; m++) {
				ARFF model = bases[m];
				Data[][] data = new Data[algorithms.length][];
				for (int i = 0; i < algorithms.length; i++) {
					data[i] = getData(eval, metric, model, algorithms[i], results);
				}

				Database[] databases = Split.split(model);
				Data[][][] sub = new Data[databases.length + 1][algorithms.length][];
				sub[0] = data;
				for (int i = 1; i < sub.length; i++) {
					for (int j = 0; j < algorithms.length; j++) {
						sub[i][j] = getData(eval, metric, databases[i - 1], algorithms[j], results);
					}
				}


				EvaluationMethod[] evalMethod = new EvaluationMethod[] { EvaluationMethod.Norm, EvaluationMethod.Norm, EvaluationMethod.NormNeg, EvaluationMethod.NormNeg, EvaluationMethod.Value, EvaluationMethod.Value };
				ViewMethod[] viewMethod = new ViewMethod[] { ViewMethod.Value, ViewMethod.Rank, ViewMethod.Value, ViewMethod.Rank, ViewMethod.Value, ViewMethod.Rank };

				for (int j = 0; j < evalMethod.length; j++) {
					method = evalMethod[j];
					view = viewMethod[j];
					//System.out.println("-------------------------------------------------------------"); //TODO
					values[j][0][m] = sturdiness(eval, model, data);
					values[j][1][m] = distance(eval, model, data);
					values[j][2][m] = similarity(eval, model, data);
					values[j][3][m] = stability(eval, model, sub);
				}
			}

			System.out.println(eval.getClass().getSimpleName() + "\t" + metric.getClass().getSimpleName());

			for (int i = 0; i < values[0].length; i++) {
				for (int messageMetric = 0; messageMetric < metricMessages[i].length; messageMetric++) {
					System.out.println();
					System.out.println(messages[i]);
					System.out.println(metricMessages[i][messageMetric]);

					StringBuilder str = new StringBuilder(1000);
					str.append("Base");
					for (int k = 0; k < values.length; k++) { // norm, rank, value
						for (int j = 0; j < algorithms.length; j++) {
							str.append("\t" + algorithms[j]);
						}
						str.append("\t");
					}
					System.out.println(str);

					for (int m = 0; m < bases.length; m++) {
						str = new StringBuilder(1000);
						ARFF model = bases[m];
						str.append(model + "\t");

						for (int r = 0; r < values.length; r++) {
							for (int j = 0; j < algorithms.length; j++) {
								double value = values[r][i][m][messageMetric][j][0];
								double dev = values[r][i][m][messageMetric][j][1];
								str.append(String.format(Locale.ENGLISH, "%.2f±%.2f\t", value, dev));
							}
							str.append("\t");
						}

						for (int r = 0; r < values.length; r++) {
							for (int j = 0; j < algorithms.length; j++) {
								str.append(values[r][i][m][messageMetric][j][0] + "\t");
							}
							str.append("\t");
						}

						System.out.println(str);
					}
				}
			}
		}
		}
	}

	private static Data[] getData(Fitness eval, Fitness metric, Database model, Algorithms algorithm, ReadResults results) {
		Data[] data = new Data[(Main.numMaxK - 1) * Main.numRepetitions];
		for (int k = 0; k < Main.numMaxK - 1; k++) {
			Problem p = new Problem(model, eval, k + 2);
			for (int i = 0; i < Main.numRepetitions; i++) {

				int[] c = results.getConsensus(model.toString(), metric.getClass().getSimpleName(), algorithm.toString(), k + 2, i);
				int[][] cs = results.getStart(model.toString(), metric.getClass().getSimpleName(), algorithm.toString(), k + 2, i);

				int index = (k * Main.numRepetitions) + i;
				data[index] = new Data(c, cs, p);
			}
		}
		return data;
	}

	private static double normHammingDistance(int[] a, int[] b) {
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				count++;
			}
		}
		return count / (double) a.length;
	}

	private static double[][][] similarity(Fitness eval, ARFF model, Data[][] data) {
		double[][] minimum = new double[data.length][data[0].length];
		double[][] average = new double[data.length][data[0].length];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				List<Solve> valid = new ArrayList<>();
				for (Solve s : data[i][j].clusterings) {
					if (Double.isFinite(s.cost)) {
						valid.add(s);
					}
				}

				double max = valid.size() > 0 ? 0 : Double.POSITIVE_INFINITY;
				double avg = valid.size() > 0 ? 0 : Double.POSITIVE_INFINITY;
				for (Solve s : valid) {
					double value = 1.0 - normHammingDistance(s.cluster, data[i][j].consensus.cluster);
					if (value > max) {
						max = value;
					}
					avg += value;
				}

				minimum[i][j] = max;
				average[i][j] = valid.size() > 0 ? avg / (double) valid.size() : Double.POSITIVE_INFINITY;
			}
		}
		return new double[][][] { evaluate(minimum, eval.isMinimization()), evaluate(average, eval.isMinimization()) };
	}

	private static double[][][] distance(Fitness eval, ARFF model, Data[][] data) {
		double[][] minimum = new double[data.length][data[0].length];
		double[][] average = new double[data.length][data[0].length];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				List<Solve> valid = new ArrayList<>();
				for (Solve s : data[i][j].clusterings) {
					if (Double.isFinite(s.cost)) {
						valid.add(s);
					}
				}

				double min = Double.POSITIVE_INFINITY;
				double avg = valid.size() > 0 ? 0 : Double.POSITIVE_INFINITY;
				for (Solve s : valid) {
					double value = normHammingDistance(s.cluster, data[i][j].consensus.cluster);
					if (value < min) {
						min = value;
					}
					avg += value;
				}

				minimum[i][j] = min;
				average[i][j] = valid.size() > 0 ? avg / (double) valid.size() : Double.POSITIVE_INFINITY;
			}
		}
		return new double[][][] { evaluate(minimum, eval.isMinimization()), evaluate(average, eval.isMinimization()) };
	}

	private static double[][][] sturdiness(Fitness eval, ARFF model, Data[][] data) {
		double[][] minimum = new double[data.length][data[0].length];
		double[][] average = new double[data.length][data[0].length];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {

				Solve mp = null;
				List<Solve> valid = new ArrayList<>();
				for (int k = 0; k < data[i][j].clusterings.length; k++) {
					double value = data[i][j].clusterings[k].cost;
					if (Double.isFinite(value)) {
						valid.add(data[i][j].clusterings[k]);
						if (mp == null || (eval.isMinimization() && value < mp.cost) || (!eval.isMinimization() && value > mp.cost)) {
							mp = data[i][j].clusterings[k];
						}
					}
				}

				double opt = data[i][j].consensus.cost;

				double avg = 0;
				for (Solve s : valid) {
					assert mp != null;
					avg += evalDiff(s.cost, opt, eval.isMinimization());
				}
				avg = valid.size() > 0 ? avg / (double) valid.size() : Double.POSITIVE_INFINITY;

				minimum[i][j] = mp != null ? evalDiff(mp.cost, opt, eval.isMinimization()) : Double.POSITIVE_INFINITY;
				average[i][j] = avg;
			}
		}

		return new double[][][] { evaluate(minimum, eval.isMinimization()), evaluate(average, eval.isMinimization()) };
	}

	private static double evalDiff(double mp, double opt, boolean isMinimization) {
		double res = Double.POSITIVE_INFINITY;
		if (Double.isInfinite(mp) || Double.isInfinite(opt)) {
			res = Double.POSITIVE_INFINITY;
		} else {

			double big = isMinimization ? mp : opt;
			double sml = isMinimization ? opt : mp;

			switch (method) {
			case Norm:
				if (big == 0 && sml == 0) {
					res = 0;
				} else if (big == 0) {
					res = -1;
				} else {
					res = (big - sml) / big;
				}
				break;
			case NormNeg:
				if (big == 0 && sml == 0) {
					res = 0;
				} else if (big > sml) {
					res = big == 0 ? 0 : (big - sml) / big;
				} else if(sml > big){
					res = sml == 0 ? 0 : (big - sml) / sml;
				} else if(sml == big){
					res = 1;
				}
				break;
			case Value:
				res = big - sml;
				break;
			}
			//System.out.format("(%1$-10.3f-%2$10.3f)/%1$10.3f = %3$10.3f/%1$-10.3f = %4$10.3f\n", big, sml, big - sml, res); //TODO
		}
		return res;
	}

	private static double[][][] stability(Fitness eval, ARFF model, Data[][][] sub) {

		double[][] average = new double[sub[0].length][sub[0][0].length];
		double[][] desviation = new double[sub[0].length][sub[0][0].length];

		for (int i = 0; i < sub[0].length; i++) {
			for (int j = 0; j < sub[0][0].length; j++) {
				List<Double> values = new ArrayList<>();
				for (int k = 0; k < sub.length; k++) {
					double value = sub[k][i][j].consensus.cost;
					if (Double.isFinite(value)) {
						values.add(value);
					}
				}

				average[i][j] = values.isEmpty() ? Double.POSITIVE_INFINITY : Analysis.average(values.toArray(new Double[values.size()]));
				desviation[i][j] = values.size() <= 1 ? Double.POSITIVE_INFINITY : Analysis.desviation(values.toArray(new Double[values.size()]));
			}
		}

		return new double[][][] { evaluate(average, eval.isMinimization()), evaluate(desviation, eval.isMinimization()) };
	}

	private static double[][] evaluate(double[][] values, boolean minimization) {
		if (view == ViewMethod.Rank) {
			values = Analysis.getRanked(Main.numMaxK - 1, values, minimization);
		}

		double[][] eval = new double[values.length][2];
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

			eval[i][0] = Analysis.average(column);
			eval[i][1] = Analysis.desviation(column);
		}
		return eval;
	}

	private static class Data {
		Solve consensus;
		Solve[] clusterings;

		public Data(int[] consensus, int[][] clusterings, Problem problem) {
			this.consensus = new Solve(problem, consensus);
			this.consensus.evaluate();
			this.clusterings = new Solve[clusterings.length];
			for (int i = 0; i < clusterings.length; i++) {
				this.clusterings[i] = new Solve(problem, clusterings[i]);
				this.clusterings[i].evaluate();
			}
		}
	}
}
