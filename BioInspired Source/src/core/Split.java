package core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

public class Split {

	public static final int size = 10;
	
	public static final double percentIncrease = 0.025;
	
	public static final double minimumPercent = 0.05;

	public static final File directory = new File("dat");

	public static Database[] split(ARFF arff) throws IOException {

		FileReader fileReader = new FileReader(new File(arff.getLocation()));
		Instances instances = new Instances(fileReader);
		fileReader.close();
		
		assert instances.numInstances() > 20; // else percentIncrease don't add instances

		List<List<Instance>> classInInstance = getInstancesByClass(instances);
		double[] percent = new double[classInInstance.size()];
		for (int i = 0; i < classInInstance.size(); i++) {
			percent[i] = classInInstance.get(i).size() / (double) instances.numInstances();
		}

		List<double[]> increments = percents(percent);
		
		assert increments.size() == size : "Insufficient Data!";
		
		Database[] bases = new Database[size];
		File arffFile = new File(arff.getLocation());
		for (int i = 0; i < increments.size(); i++) {
			String name = arffFile.getName().substring(0, arffFile.getName().length() - 5) + "-" + i;
			File output = new File(directory, name + ".arff");
			bases[i] = new SampleDataBase(output, new Random(0), name, instances, increments.get(i), classInInstance);
		}

		return bases;
	}

	private static List<double[]> percents(double[] percent) {
		List<Integer> elements = new ArrayList<>();
		for (int i = 0; i < percent.length; i++) {
			elements.add(i);
		}

		List<List<Integer>> increment = new ArrayList<>();
		List<List<Integer>> decrement = new ArrayList<>();

		pairsIncrementDecrement(elements, increment, decrement);

		List<double[]> percents = new ArrayList<>();
		double currentPercent = percentIncrease;
		while (percents.size() < size && percentIncrease + currentPercent < 1.0 - minimumPercent) {
			for (int i = 0; i < increment.size() && percents.size() < size; i++) {
				boolean isValid = true;
				double[] p = percent.clone();
				for (int index : increment.get(i)) {
					p[index] += currentPercent;
					isValid = isValid && p[index] <= 1.0 - minimumPercent;
				}
				for (int index : decrement.get(i)) {
					p[index] -= currentPercent;
					isValid = isValid && p[index] >= minimumPercent;
				}
				if(isValid){
					percents.add(p);
				}
			}
			currentPercent += percentIncrease;
		}
		
		return percents;
	}

	private static void pairsIncrementDecrement(List<Integer> elements, List<List<Integer>> pairIncrement, List<List<Integer>> pairDecrement) {
		assert elements.size() > 1;
		int maxGroup = elements.size() / 2;

		for (int i = maxGroup; i > 0; i--) {
			List<List<Integer>> increment = combination(elements, i);

			for (List<Integer> inc : increment) {
				List<Integer> comb = new ArrayList<>(elements);
				comb.removeAll(inc);

				List<List<Integer>> decrement = combination(comb, i);

				for (List<Integer> dec : decrement) {
					pairIncrement.add(inc);
					pairDecrement.add(dec);
				}
			}
		}
	}

	private static List<List<Integer>> combination(List<Integer> elements, int groupSize) {
		elements = new ArrayList<>(elements);

		List<List<Integer>> results = new ArrayList<>();
		while (!elements.isEmpty()) {
			int element = elements.remove(0);
			if (groupSize == 1) {
				List<Integer> e = new ArrayList<>();
				e.add(element);
				results.add(e);
			} else {
				List<List<Integer>> sub = combination(elements, groupSize - 1);
				for (List<Integer> list : sub) {
					list.add(0, element);
					results.add(list);
				}
			}
		}
		return results;
	}

	public static List<List<Instance>> getInstancesByClass(Instances instances) {
		instances.setClassIndex(instances.numAttributes() - 1);

		final List<List<Instance>> instanceByClass = new ArrayList<>();
		for (int i = 0; i < instances.numClasses(); i++) {
			instanceByClass.add(new ArrayList<Instance>());
		}

		for (int i = 0; i < instances.numInstances(); i++) {
			final Instance instance = instances.get(i);
			instanceByClass.get((int) instance.classValue()).add(instance);
		}
		return instanceByClass;
	}

	private static class SampleDataBase implements Database {

		private String name;

		private String location;

		public SampleDataBase(File file, Random rand, String name, Instances model, double[] percent, List<List<Instance>> classInInstances) throws IOException {
			this.name = name;
			this.location = file.getPath();

			Instances instances = new Instances(model, model.numInstances());

			for (int i = 0; i < percent.length; i++) {
				int size = (int) (percent[i] * model.numInstances());
				for (int j = 0; j < size; j++) {
					instances.add(classInInstances.get(i).get(rand.nextInt(classInInstances.get(i).size())));
				}
			}

			while (instances.numInstances() < model.numInstances()) { // correct double round
				instances.add(model.get(rand.nextInt(model.numInstances())));
			}

			PrintStream output = new PrintStream(file);
			output.println(instances);
			output.close();
		}

		public String getLocation() {
			return location;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
