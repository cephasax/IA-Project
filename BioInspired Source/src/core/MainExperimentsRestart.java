package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

public class MainExperimentsRestart {

	public static void restartAtBase(File fileExperiment, File outputSummary, ARFF currentBase, ARFF[] allBases) throws IOException {
		Hashtable<ARFF, Integer> baseIndex = new Hashtable<ARFF, Integer>();
		for (int i = 0; i < allBases.length; i++)
			baseIndex.put(allBases[i], i);

		int currentIndex = baseIndex.get(currentBase);

		File outputTmp = new File(fileExperiment.getParent(), fileExperiment.getName() + ".tmp");
		outputTmp.deleteOnExit();
		PrintStream tmp = new PrintStream(outputTmp);
		for (ARFF base : allBases) {
			BufferedReader reader = new BufferedReader(new FileReader(fileExperiment));
			String line = reader.readLine();
			int indexBase = 4;
			while (line != null) {
				String[] lineSplit = line.split("\t");
				if (lineSplit.length > 4 && lineSplit[indexBase].equals(base.toString()) && baseIndex.get(base) < currentIndex)
					tmp.println(line);
				line = reader.readLine();
			}
			reader.close();
		}
		tmp.close();
		copyFile(outputTmp, fileExperiment);

		String match = String.format("%30s\t", currentBase.toString());
		outputTmp = new File(outputSummary.getParent(), outputSummary.getName() + ".tmp");
		tmp = new PrintStream(outputTmp);
		BufferedReader reader = new BufferedReader(new FileReader(outputSummary));
		String line = reader.readLine();
		while (line != null) {
			if (line.equals(match)) {
				break;
			}
			tmp.println(line);
			line = reader.readLine();
		}
		reader.close();
		tmp.close();
		copyFile(outputTmp, outputSummary);
	}

	private static void copyFile(File origin, File target) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(origin));
		PrintStream out = new PrintStream(target);
		String line = in.readLine();
		while (line != null) {
			out.println(line);
			line = in.readLine();
		}
		in.close();
		out.close();
	}
}
