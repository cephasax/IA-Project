package br.ufrn.ia.main;

import java.io.File;
import java.util.ArrayList;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.metrics.CorrectRand;
import br.ufrn.ia.metrics.DaviesBouldin;
import br.ufrn.ia.metrics.MX;

public class Configuration {

	// set on constructor
	private ArrayList<Fitness> metrics;
	
	private final int numberOfRepetitions = 10;
	private final int minK = 2;
	private final int maxK = 10;
	private final int sizeOfPupulation = 10;
	private final double pPartitions = 0.9;
	private final double pEquals = 0.9;
	private File outputFile;
	
	
	//Constructor
	public Configuration() throws Exception{
		
		//set metrics
		metrics = new ArrayList<Fitness>();	
		metrics.add(new CorrectRand());
		//metrics.add(new DaviesBouldin());
		//metrics.add(new MX());
		
	}

	//GETTERS AND SETTERS
	public double getpPartitions() {
		return pPartitions;
	}

	public double getpEquals() {
		return pEquals;
	}

	public int getSizeOfPupulation() {
		return sizeOfPupulation;
	}

	public ArrayList<Fitness> getMetrics() {
		return metrics;
	}

	public void setMetrics(ArrayList<Fitness> metrics) {
		this.metrics = metrics;
	}

	public int getMinK() {
		return minK;
	}

	public int getMaxK() {
		return maxK;
	}

	public int getNumberOfRepetitions() {
		return numberOfRepetitions;
	}
	
	public void setFileOutput(File file) {
		this.outputFile = file;
	}
	
	public File getFileOutput(){
		return this.outputFile;
	}
}
