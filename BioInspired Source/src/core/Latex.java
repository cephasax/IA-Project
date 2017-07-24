package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;

public class Latex {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> baseShortName = new HashMap<String, String>();

		baseShortName.put("Lung_Cancer", "Lung");
		baseShortName.put("Hepatitis", "Hepatitis");
		baseShortName.put("Wine", "Wine");
		baseShortName.put("Automobile", "Automobile");
		baseShortName.put("Glass_Identification", "Glass");
		baseShortName.put("Statlog_Heart", "Heart");
		baseShortName.put("SolarFlare1", "Solar");
		baseShortName.put("Ecoli", "Ecoli");
		baseShortName.put("Ionosphere", "Ionosphere");
		baseShortName.put("Dermatology", "Dermatology");
		baseShortName.put("Congressional_Voting_Records", "Votes");
		baseShortName.put("Breast_Cancer_Wisconsin_Original", "Breast");
		baseShortName.put("Connectionist_Bench_Vowel", "Vowel");
		baseShortName.put("Balance", "Balance");
		baseShortName.put("Pima_Indians_Diabetes", "Diabetes");
		baseShortName.put("Labor", "Labor");
		baseShortName.put("Pittsburgh_Bridges_V1", "Pittsburgh");
		baseShortName.put("Planning_Relax", "Planning");
		baseShortName.put("Flags", "Flags");
		baseShortName.put("Horse_Colic", "Horse");
		baseShortName.put("AVG", "AVG");

		BufferedReader in = new BufferedReader(new FileReader("Input.txt"));

		String line = in.readLine();
		while (line != null) {

			String[] split = line.split("\t");

			double[] min = new double[7];
			double[] dev = new double[7];

			if (split[0].startsWith("AVG")) {

				for (int i = 1; i < split.length; i++)
					min[i - 1] = Double.parseDouble(split[i]);

				double minValue = min[0];
				for (int i = 0; i < min.length; i++)
					if (min[i] < minValue)
						minValue = min[i];
				
				System.out.print(baseShortName.get(split[0]));
				for (int i = 0; i < min.length; i++) {

					if (min[i] == minValue)
						System.out.print(String.format(Locale.ENGLISH, "\t&\t\\cellcolor{gray!25}\t%.2f",
								min[i]));
					else
						System.out.print(String.format(Locale.ENGLISH, "\t&\t%.2f", min[i]));
				}
				System.out.println("\t\\\\ \\hline");
				System.out.println();

			} else {

				for (int i = 1; i < split.length; i++) {
					String[] s = split[i].split("±");
					min[i - 1] = Double.parseDouble(s[0]);
					dev[i - 1] = Double.parseDouble(s[1]);
				}

				double minValue = min[0];
				for (int i = 0; i < min.length; i++)
					if (min[i] < minValue)
						minValue = min[i];

				System.out.print(baseShortName.get(split[0]));
				for (int i = 0; i < min.length; i++) {

					if (min[i] == minValue)
						System.out.print(String.format(Locale.ENGLISH, "\t&\t\\cellcolor{gray!25}\t%.2f$\\pm$%.2f",
								min[i], dev[i]));
					else
						System.out.print(String.format(Locale.ENGLISH, "\t&\t%.2f$\\pm$%.2f", min[i], dev[i]));
				}
				System.out.println("\t\\\\ \\hline");

			}

			line = in.readLine();
		}

		in.close();
	}
}
