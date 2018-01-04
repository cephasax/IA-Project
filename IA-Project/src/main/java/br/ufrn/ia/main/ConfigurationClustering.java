package br.ufrn.ia.main;

import br.ufrn.ia.core.clustering.EMIaProject;
import br.ufrn.ia.core.clustering.HierarchicalClustererIaProject;
import br.ufrn.ia.core.clustering.SimpleKMeansIaProject;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instances;

public class ConfigurationClustering {

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
	private int m_NumClusters = 2;
	
	//K-MEANS
	private final boolean m_displayStdDevs = false;											
	private final boolean m_dontReplaceMissing = false;
	private final int m_MaxIterations = 500;
	private final DistanceFunction m_DistanceFunction = new EuclideanDistance();
	private final boolean m_PreserveOrder = false;
	private final int m_Seed = 1;
	private SimpleKMeansIaProject kmeans1;
	
	//EM	
	private final boolean m_verbose = false;													
	private final int m_max_iterations = 100;
	private final double m_minStdDev = 1e-6;
	private final boolean m_displayModelInOldFormat = false;
	private final int m_Seed_EM = 100;
	private EMIaProject em1;
	

	//HIERARCHICAL
	private final boolean m_bPrintNewick = true;
	private final boolean m_bDistanceIsBranchLength = false;
	private final boolean m_bDebug = false;													
	private final DistanceFunction m_DistanceFunction_Hierarchical = new EuclideanDistance();
	private final int m_nLinkType_Hierarchical = AVERAGE;
	private HierarchicalClustererIaProject hc1;
	
	
	public void buildAlgs(Instances instances) throws Exception{
		kmeans1 = new SimpleKMeansIaProject();
		kmeans1.setOptions(	m_displayStdDevs, m_dontReplaceMissing, m_NumClusters, 
				 			m_MaxIterations, m_DistanceFunction, m_PreserveOrder, m_Seed);
		kmeans1.setSeed((int)(Math.random()*Integer.MAX_VALUE));
		
		em1 = new EMIaProject();
		em1.setOptions(	m_verbose, m_max_iterations, m_NumClusters, 
						m_minStdDev, m_displayModelInOldFormat, m_Seed_EM);
		em1.setSeed((int)(Math.random()*Integer.MAX_VALUE));
		
		hc1 = new HierarchicalClustererIaProject();
		hc1.setOptions(m_bPrintNewick, m_NumClusters, m_bDistanceIsBranchLength, 
						m_bDebug, m_DistanceFunction_Hierarchical, m_nLinkType_Hierarchical);
		
		this.kmeans1.buildClusterer(instances);		
		this.em1.buildClusterer(instances);
		this.hc1.buildClusterer(instances);
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

	public int getM_NumClusters() {
		return m_NumClusters;
	}

	public void setM_NumClusters(int m_NumClusters) {
		this.m_NumClusters = m_NumClusters;
	}	
}

