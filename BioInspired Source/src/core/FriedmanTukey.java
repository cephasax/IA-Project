package core;

import java.util.Arrays;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.TDistribution;

public class FriedmanTukey {

	public static void main(String[] args) {
		double[][] values = new double[][] {
			{0.225453195,	0.023222222,	0.081850534,	0.000810373},
				{0.215453195,	0.022222222,	0.081850534,	0.000810373},
				{0.215453195,	0.022222222,	0.081850534,	0.000810373},
				{0.215453195,	0.022222222,	0.081850534,	0.000810373},
				{0.215453195,	0.022222222,	0.081850534,	0.000810373},
				{0.215453195,	0.022222222,	0.081850534,	0.000810373},

		};
		FriedmanTukey friedman = new FriedmanTukey();
		System.out.println(friedman.evaluate(values));

		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				System.out.print(String.format("%.4f", friedman.posthoc(i, j)));
				System.out.print(" ");
			}
			System.out.println();
		}

	}

	private double[] R;

	private double posthoc;

	private double df;

	// n > 15 or k > 4
	public double evaluate(double[]... samples) {
		if (samples.length < 2 || samples[0].length < 2) {
			return 1;
		}

		double[][] t = new double[samples[0].length][samples.length];
		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[0].length; j++) {
				t[i][j] = samples[j][i];
			}
		}

		double[][] ranks = new double[t.length][];
		for (int i = 0; i < ranks.length; i++) {
			ranks[i] = RankSample.rankSample(t[i]);
		}

		t = new double[ranks[0].length][ranks.length];
		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[0].length; j++) {
				t[i][j] = ranks[j][i];
			}
		}
		ranks = t;

		int k = ranks.length;
		int b = ranks[0].length;

		double A1 = 0;
		for (double[] rank : ranks) {
			for (int j = 0; j < ranks[0].length; j++) {
				A1 += Math.pow(rank[j], 2);
			}
		}

		double C1 = (b * k * Math.pow(k + 1, 2)) / 4.0;

		double[] rj = new double[k];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < b; j++) {
				rj[i] += ranks[i][j];
			}
		}
		R = rj.clone();
		for (int i = 0; i < k; i++)
			R[i] /= b;

		double sum = 0;
		for (int i = 0; i < k; i++) {
			sum += Math.pow(rj[i] - (b * (k + 1)) / 2.0, 2);
		}

		double T1 = ((k - 1) * sum) / (A1 - C1);

		double T2 = ((b - 1) * T1) / (b * (k - 1) - T1);

		sum = 0;
		for (int i = 0; i < rj.length; i++) {
			sum += Math.pow(rj[i], 2.0);
		}

		t = new double[ranks[0].length][ranks.length];
		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[0].length; j++) {
				t[i][j] = ranks[j][i];
			}
		}

		double tieadj = 0;
		for (int i = 0; i < t.length; i++) {
			int count = 0;
			boolean[] flag = new boolean[t[0].length];
			for (int m = 0; m < t[0].length; m++) {
				for (int n = m + 1; n < t[0].length; n++) {
					if (t[i][m] == t[i][n]) {
						if (!flag[m]) {
							count++;
							flag[m] = true;
						}
						if (!flag[n]) {
							count++;
							flag[n] = true;
						}
					}
				}
			}
			tieadj += 2*(count*(count-1)*(count+1)/2);
		}
		
		double sse = ranks.length * (ranks.length + 1) / 12.0;
		if(tieadj>0){
			sse = sse - tieadj / (12.0 * ranks[0].length* (ranks.length-1));
		}
		sse = Math.sqrt((sse*sse)/(double)ranks[0].length);

		posthoc = sse;
		df = (b - 1) * (k - 1);

		return 1.0 - new FDistribution(k - 1, (b - 1) * (k - 1)).cumulativeProbability(T2);
	}

	public double posthoc(int a, int b) {
		double pvalue = Math.abs(R[a] - R[b]) / posthoc;
		TDistribution tdist = new TDistribution(df);
		pvalue = 2.0 * (1.0 - tdist.cumulativeProbability(pvalue));
		return pvalue;
	}
}
