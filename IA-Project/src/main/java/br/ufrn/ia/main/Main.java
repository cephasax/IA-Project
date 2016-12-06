package br.ufrn.ia.main;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.Problem;
import br.ufrn.ia.core.RelabelAndConsensus;
import br.ufrn.ia.core.Solve;
import br.ufrn.ia.core.Util;
import br.ufrn.ia.view.FileManager;
import weka.clusterers.AbstractClusterer;
import weka.core.Instances;

public class Main {
	
	public static File[]  files;
	public static ArrayList<Log> logs;
	public static ArrayList<Solve> initialPopulation;
	public static FileManager fileManager = new FileManager();
	public static Configuration conf;
	public static ConfOptimization confOptimization;
	public static ArrayList<Integer> epochs;
	public static String logFileName = "log.txt";
	
	public static void main(String[] args) throws Exception {
		
		Util util = new Util();
		
		logs = new ArrayList<Log>();
		initialPopulation = new ArrayList<Solve>();
		
		//Selecionar arquivos .arff
		files = fileManager.selecionarArquivos();
		
		int fileIndex = 0;		
		
		//Para cada arquivo
		for (File file: files){
			
			//repetir "repetitions" vezes
			for(int repetitions = 1; repetitions <= conf.getNumberOfRepetitions(); repetitions++){
				
				//com k variando de "minK" ate "maxK"
				for(int tempK = conf.getMinK(); tempK <= conf.getMaxK(); tempK++){
					
					conf = new Configuration(tempK);
					
					//Guarda nome do arquivo
					logs.get(fileIndex).setFileName(file.getName() + " - k. " + tempK);
					fileIndex++;
					
					Instances instances = new Instances(new FileReader(file));
					instances.deleteAttributeAt(instances.numAttributes() - 1);
		
					conf.getKmeans1().buildClusterer(instances);		
					conf.getEm1().buildClusterer(instances);
					conf.getHc1().buildClusterer(instances);
					
					
					//criar vetores de representacao de agrupamentos
					int[][] clustering = new int[3][instances.numInstances()];
					AbstractClusterer[] c = new AbstractClusterer[] { conf.getKmeans1(), conf.getEm1(), conf.getHc1() };
					
					for (int i = 0; i < c.length; i++) {
						for (int j = 0; j < clustering[0].length; j++) {
							clustering[i][j] = c[i].clusterInstance(instances.get(j));
						}
					}
					
					//Resultado dos agrupamentos na classe de pedacos
					logs.get(fileIndex).setClusteringVectors(clustering);
					
					RelabelAndConsensus rAc = new RelabelAndConsensus();
					
					//relabel
					int[][] newClusters = rAc.relabel(clustering);
					
					//Resultado do relabel na classe de pedacos
					logs.get(fileIndex).setRelabeledClusters(newClusters);
					
					for(Fitness fitness: conf.getMetrics()){
											
						Problem problem = new Problem(file, fitness, tempK);
						ArrayList<Solve> solves = new ArrayList<Solve>();						
						for(int numberOfsolves = 1; numberOfsolves <= conf.getNumberOfSolves(); numberOfsolves++){
							//Construção das soluções
							Solve solve = new Solve(tempK, newClusters, conf.getpPartitions(), conf.getpEquals());
							solve.setProblem(problem);
							solves.add(solve);
						}
						
						double[][] distances= util.buildHeuristic2(tempK, clustering);
						confOptimization = new ConfOptimization(solves.toArray(new Solve[]{}), distances);
						confOptimization.buildAlgs();
						
						for(OptimizationAlgorithm oa: confOptimization.getOptimizationAlgorithms()){
							util.evaluate(logFileName, problem, logs.get(fileIndex).getFileName(), tempK, oa);
						}
						
					}	
				}
			}
		}
	}	
}
