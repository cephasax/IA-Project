package br.ufrn.ia.main;

import java.util.ArrayList;

import br.ufrn.ia.core.Fitness;
import br.ufrn.ia.core.clustering.EMIaProject;
import br.ufrn.ia.core.clustering.HierarchicalClustererIaProject;
import br.ufrn.ia.core.clustering.SimpleKMeansIaProject;
import br.ufrn.ia.metrics.CorrectRand;
import br.ufrn.ia.metrics.DaviesBouldin;
import br.ufrn.ia.metrics.MX;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;

public class Configuration {

	//Dist
	final int SINGLE = 0;
	final int COMPLETE = 1;
	final int AVERAGE = 2;
	final int MEAN = 3;
	final int CENTROID = 4;
	final int WARD = 5;
	final int ADJCOMLPETE = 6;
	final int NEIGHBOR_JOINING = 7;
	
	//GENERAL K
	private int m_NumClusters;
	
	//K-MEANS
	private final boolean m_displayStdDevs = false;											//
	private final boolean m_dontReplaceMissing = false;
	private final int m_MaxIterations = 500;
	private final DistanceFunction m_DistanceFunction = new EuclideanDistance();
	private final boolean m_PreserveOrder = false;
	private final int m_Seed = 1;
	private SimpleKMeansIaProject kmeans1;
	
	//EM	
	private final boolean m_verbose = false;													//
	private final int m_max_iterations = 100;
	private final double m_minStdDev = 1e-6;
	private final boolean m_displayModelInOldFormat = false;
	private final int m_Seed_EM = 100;
	private EMIaProject em1;
	

	//HIERARCHICAL
	private final boolean m_bPrintNewick = true;
	private final boolean m_bDistanceIsBranchLength = false;
	private final boolean m_bDebug = true;													//
	private final DistanceFunction m_DistanceFunction_Hierarchical = new EuclideanDistance();
	private final int m_nLinkType_Hierarchical = AVERAGE;
	private HierarchicalClustererIaProject hc1;
	
	//others configurations
	
		// set on constructor
	private ArrayList<Fitness> metrics;
	
	private final int numberOfRepetitions = 10;
	private final int numberOfSolves = 10;
	private final int minK = 2;
	private final int maxK = 10;
	private final int sizeOfPupulation = 50;
	private final double pPartitions = 0.9;
	private final double pEquals = 0.9;
	
	
	//Constructor
	public Configuration(int k) throws Exception{
		
		this.m_NumClusters = k;
		
		kmeans1 = new SimpleKMeansIaProject();
		kmeans1.setOptions(	m_displayStdDevs, m_dontReplaceMissing, m_NumClusters, 
				 			m_MaxIterations, m_DistanceFunction, m_PreserveOrder, m_Seed);
		
		em1 = new EMIaProject();
		em1.setOptions(	m_verbose, m_max_iterations, m_NumClusters, 
						m_minStdDev, m_displayModelInOldFormat, m_Seed_EM);
		
		hc1 = new HierarchicalClustererIaProject();
		hc1.setOptions(m_bPrintNewick, m_NumClusters, m_bDistanceIsBranchLength, 
						m_bDebug, m_DistanceFunction_Hierarchical, m_nLinkType_Hierarchical);
		
		//set metrics
		
		metrics = new ArrayList<Fitness>();	
		metrics.add(new CorrectRand());
		metrics.add(new DaviesBouldin());
		metrics.add(new MX());
		
	}

	//GETTERS AND SETTERS
	
	public SimpleKMeansIaProject getKmeans1() {
		return kmeans1;
	}

	public void setKmeans1(SimpleKMeansIaProject kmeans1) {
		this.kmeans1 = kmeans1;
	}

	public EMIaProject getEm1() {
		return em1;
	}

	public void setEm1(EMIaProject em1) {
		this.em1 = em1;
	}

	public HierarchicalClustererIaProject getHc1() {
		return hc1;
	}

	public void setHc1(HierarchicalClustererIaProject hc1) {
		this.hc1 = hc1;
	}

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

	public int getM_NumClusters() {
		return m_NumClusters;
	}

	public void setM_NumClusters(int m_NumClusters) {
		this.m_NumClusters = m_NumClusters;
	}

	public int getNumberOfSolves() {
		return numberOfSolves;
	}


	
	
	
	
	
	
	
}
