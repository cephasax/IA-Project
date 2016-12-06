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
	public static ConfigurationOptimization confOptimization;
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
			
			conf = new Configuration();
			
			//repetir "repetitions" vezes
			for(int repetitions = 1; repetitions <= conf.getNumberOfRepetitions(); repetitions++){
				
				//com k variando de "minK" ate "maxK"
				for(int tempK = conf.getMinK(); tempK <= conf.getMaxK(); tempK++){
					
					conf.setM_NumClusters(tempK);
										
					//Guarda nome do arquivo
					String logName = new String((file.getName() + " - k. " + tempK));
					logs.add(new Log(logName));					

					//criar vetores de representacao de agrupamentos
					Instances instances = new Instances(new FileReader(file));
					instances.deleteAttributeAt(instances.numAttributes() - 1);

					conf.buildAlgs(instances);
					
					//criar vetores de representacao de agrupamentos
					int[][] clustering = new int[2][instances.numInstances()];
					AbstractClusterer[] c = new AbstractClusterer[] { conf.getKmeans1(), conf.getEm1()/*, conf.getHc1()*/ };
					
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
					for(Fitness fitness: conf.getMetrics()){	
						
						Problem problem = new Problem(file, fitness, tempK);
						ArrayList<Solve> solves = util.buildSolves(conf, tempK, newClusters, problem);
						
						//Configura e constroi os algoritmos de otimizacao
						double[][] distances= util.buildHeuristic2(tempK, clustering);
						confOptimization = new ConfigurationOptimization(solves.toArray(new Solve[]{}), distances);
						confOptimization.buildAlgs();
						
						//roda os algoritmos de otimizacao e salva os resultados num arquivo
						for(OptimizationAlgorithm oa: confOptimization.getOptimizationAlgorithms()){
							util.evaluate(logFileName, problem, logs.get(fileIndex).getFileName(), tempK, oa);
						}
						
					}
					fileIndex++;
				}
			}
		}
	}	
}
