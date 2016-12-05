package br.ufrn.ia.main;

import java.io.File;
import java.io.FileReader;

import br.ufrn.ia.view.FileManager;
import weka.clusterers.AbstractClusterer;
import weka.core.Instances;

public class Main {
	
	public static File  file;
	public static String out="";
	public static int [] qtd;
	public static int indice; 
	public static double [][] matDist;
	//public static Instances base;
	public static int colunas;
	public static double [] menor;
	public static double [] silhouette;

	public static FileManager fileManager = new FileManager();
	
	public static void main(String[] args) throws Exception {
		
		//Selecionar arquivos .arff
		file = fileManager.selecionarArquivo();
		
		Instances instances = new Instances(new FileReader(file));
		instances.deleteAttributeAt(instances.numAttributes() - 1);
		
		Configuration conf = new Configuration();

		conf.getKmeans1().buildClusterer(instances);		
		conf.getEm1().buildClusterer(instances);
		conf.getHc1().buildClusterer(instances);

		System.out.print(instances.attribute(instances.numAttributes() - 1));
		
		int[][] clustering = new int[3][instances.numInstances()];
		AbstractClusterer[] c = new AbstractClusterer[] { conf.getKmeans1(), conf.getEm1(), conf.getHc1() };
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < clustering[0].length; j++) {
				clustering[i][j] = c[i].clusterInstance(instances.get(j));
				
			}
		}
		
	}
}