package br.ufrn.ia.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.RelabelAndConsensus;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.core.Util;
import br.ufrn.ia.view.FileManager;
import core.ARFF;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.Utils;

public class Main {

	public static File[] files;
	public static ArrayList<Log> logs;
	public static ArrayList<Solve> initialPopulation;
	public static FileManager fileManager = new FileManager();

	public static Configuration conf;
	public static ConfigurationOptimization confOptimization;
	public static ConfigurationClustering confClustering;
	public static String logFileName = "log.txt";

	public static void main(String[] args) throws Exception {

		Util util = new Util();

		logs = new ArrayList<Log>();
		initialPopulation = new ArrayList<Solve>();

		//Selecionar arquivos .arff
		files = fileManager.selecionarArquivos();
		int fileIndex = 0;

		//Para cada arquivo
		for (File file : files) {
			conf = new Configuration();
			conf.setFileOutput(new File("IA-Project\\results\\" + file.getName().substring(0, file.getName().length() - 5) + ".txt"));

			//repetir "repetitions" vezes
			for (int repetitions = 1; repetitions <= conf.getNumberOfRepetitions(); repetitions++) {
				//com k variando de "minK" ate "maxK"
				for (int tempK = conf.getMinK(); tempK <= conf.getMaxK(); tempK++) {
					confClustering = new ConfigurationClustering();
					confClustering.setM_NumClusters(tempK);

					//Guarda nome do arquivo
					String logName = new String((file.getName() + " - k. " + tempK));
					logs.add(new Log(logName));

					for (Fitness fitness : conf.getMetrics()) {
						//criar vetores de representacao de agrupamentos
						Instances instances = new Instances(new FileReader(file));
						instances.deleteAttributeAt(instances.numAttributes() - 1);

						confClustering.buildAlgs(instances);
						
						//Configura e constroi os algoritmos de otimizacao
						double[][] distances = util.buildHeuristic2(tempK, clustering);
						confOptimization = new ConfigurationOptimization(solves.toArray(new Solve[] {}), distances);
						
						confOptimization.buildAlgs();
						
						for (OptimizationAlgorithm oa : confOptimization.getOptimizationAlgorithms()) {

						//criar vetores de representacao de agrupamentos
						int[][] clustering = new int[3][instances.numInstances()];

						AbstractClusterer[] c = new AbstractClusterer[] { confClustering.getHc1(), confClustering.getKmeans1(), confClustering.getEm1() };
						
						for (int i = 0; i < c.length; i++) {
							for (int j = 0; j < clustering[0].length; j++) {
								clustering[i][j] = c[i].clusterInstance(instances.get(j));
							}
						}
						
						
						//Resultado dos agrupamentos na classe de pedacos
						logs.get(fileIndex).setClusteringVectors(clustering);

						//relabel
						RelabelAndConsensus rAc = new RelabelAndConsensus();
						int[][] newClusters = rAc.relabel(clustering);

						//Resultado do relabel na classe de pedacos
						logs.get(fileIndex).setRelabeledClusters(newClusters);

						//criar populacoes iniciais
						Problem problem = new Problem(file, fitness, tempK);
						ArrayList<Solve> solves = util.buildSolves(conf, tempK, newClusters, problem);

						

						//roda os algoritmos de otimizacao e salva os resultados num arquivo
						
							System.out.println(file.getName().substring(0, file.getName().length() - 5) + " " + tempK + " rep " + repetitions + " " + fitness.getClass().getSimpleName());
							util.evaluate(conf.getFileOutput(), problem, file.getName().substring(0, file.getName().length() - 5).replace(" ", "_"), tempK, oa);
						}
					}
					fileIndex++;
				}
			}
		}
	}
	
	public static int[][] getClusterings(File file, int k) throws Exception {
		Instances instances = new Instances(new FileReader(file));

		AbstractClusterer c1 = new HierarchicalClusterer();
		c1.setOptions(Utils.splitOptions("-N " + k + " -L SINGLE -P -A \"weka.core.EuclideanDistance -R first-last\""));

		AbstractClusterer c2 = new SimpleKMeans();
		c2.setOptions(Utils.splitOptions("-init 0 -max-candidates 100 -periodic-pruning 10000 -min-density 2.0 -t1 -1.25 -t2 -1.0 -N " + k + " -A \"weka.core.EuclideanDistance -R first-last\" -I 500 -num-slots 1 -S 10"));

		AbstractClusterer c3 = new EM();
		c3.setOptions(Utils.splitOptions("-I 100 -N " + k + " -M 1.0E-6 -S 100"));

		int[][] clustering = new int[3][instances.numInstances()];
		AbstractClusterer[] c = new AbstractClusterer[] { c1, c2, c3 };
		for (int i = 0; i < c.length; i++) {
			c[i].buildClusterer(new Instances(instances));
			for (int j = 0; j < clustering[0].length; j++) {
				clustering[i][j] = c[i].clusterInstance(instances.get(j));
			}
		}

		clustering = new RelabelAndConsensus().relabel(clustering);
		return clustering;
	}
}
