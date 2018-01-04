package br.ufrn.ia.main;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufrn.ia.core.OptimizationAlgorithm;
import br.ufrn.ia.core.optimizationMethods.AntColonyOptimization;
import br.ufrn.ia.core.optimizationMethods.BeeColonyOptimization;
import br.ufrn.ia.core.optimizationMethods.CoralReefOptimization;
import br.ufrn.ia.core.optimizationMethods.GeneticAlgorithm;
import br.ufrn.ia.core.optimizationMethods.ParticleSwarmOptimization;

public class ConfigurationOptimization {

	//Optimization Configurations
	
	//Global
	private final int epoch = 50; 		//Use 50 or 100 or 200
	
	//AntColonyOptimization
	private final boolean heuristic = false;
	private final double alpha = 0.5;
	private final double beta = 0.5;
	private final double ro = 0.2;
	private double[][] distanceACO;
	private AntColonyOptimization ACO;
	
	//GeneticAlgorithm 
	private final double mutate = 0.1;
	private final double crossover = 0.9;
	private GeneticAlgorithm GA;
	
	//BeeColonyOptimization
	private final int maxNotImproved = 10;;
	private BeeColonyOptimization BCO;
	
	//CoralReefOptimization - GENERAL 
	private final int dimension = 100;
	private final double rho = 0.9; 
	private final double fa = 0.5; 
	private final double fb = 0.8; 
	
		//CoralReefOptimization - 1
		private final boolean insertRandOrRank1 = false; 
		private final double fd1 = 0.05;
		private final int stepsDepredation1 = 1;
		private CoralReefOptimization CRO1;
		
		//CoralReefOptimization - 2
		private final boolean insertRandOrRank2 = true; 
		private final double fd2 = 0.1;
		private final int stepsDepredation2 = 5;
		private CoralReefOptimization CRO2;

		//CoralReefOptimization - 3
		private final boolean insertRandOrRank3 = false; 
		private final double fd3 = 0.1;
		private final int stepsDepredation3 = 5;
		private CoralReefOptimization CRO3;

	//ParticleSwarmOptimization
	private final double pOwnWay = 0.95;
	private final double pPreviousPosition = 0.05;
	private final double pBestPosition = 0;
	private ParticleSwarmOptimization PSO;
	
	private ArrayList<OptimizationAlgorithm> optimizationAlgorithms;
	private HashMap <OptimizationAlgorithm,String> map;

	//Constructor
	public ConfigurationOptimization(){
		optimizationAlgorithms = new ArrayList<OptimizationAlgorithm>();
	}
	
	public void buildAlgs(){
		
		this.ACO = new AntColonyOptimization(epoch, heuristic, alpha, beta, ro, distanceACO);
		this.GA = new GeneticAlgorithm(epoch, mutate, crossover);
		this.BCO = new BeeColonyOptimization(epoch, maxNotImproved);
		
		this.CRO1 = new CoralReefOptimization(epoch, dimension, insertRandOrRank1, rho, fa, fb, fd1, stepsDepredation1);
		this.CRO2 = new CoralReefOptimization(epoch, dimension, insertRandOrRank2, rho, fa, fb, fd2, stepsDepredation2);
		this.CRO3 = new CoralReefOptimization(epoch, dimension, insertRandOrRank3, rho, fa, fb, fd3, stepsDepredation3);
		
		this.PSO = new ParticleSwarmOptimization(epoch, pOwnWay, pPreviousPosition, pBestPosition);
		
		optimizationAlgorithms.add(ACO);
		optimizationAlgorithms.add(GA);
		optimizationAlgorithms.add(BCO);
		
		optimizationAlgorithms.add(CRO1);
		optimizationAlgorithms.add(CRO2);
		optimizationAlgorithms.add(CRO3);
		
		optimizationAlgorithms.add(PSO);
		
		map = new HashMap<OptimizationAlgorithm,String> ();
		map.put(ACO, "ACO");
		map.put(GA, "GA");
		map.put(BCO, "BCO");
		map.put(CRO1, "CRO1");
		map.put(CRO2, "CRO2");
		map.put(CRO3, "CRO3");
		map.put(PSO, "PSO");
	}

	public double[][] getDistanceACO() {
		return distanceACO;
	}

	public void setDistanceACO(double[][] distance) {
		this.distanceACO = distance;
		ACO.setDistance(distance);
	}
	
	public void setAcoDistance(double[][] distance){
		ACO.setDistance(distance);
	}

	public AntColonyOptimization getACO() {
		return ACO;
	}

	public void setACO(AntColonyOptimization aCO) {
		ACO = aCO;
	}

	public GeneticAlgorithm getGA() {
		return GA;
	}

	public void setGA(GeneticAlgorithm gA) {
		GA = gA;
	}

	public BeeColonyOptimization getBCO() {
		return BCO;
	}

	public void setBCO(BeeColonyOptimization bCO) {
		BCO = bCO;
	}

	public CoralReefOptimization getCRO1() {
		return CRO1;
	}

	public void setCRO1(CoralReefOptimization cRO1) {
		CRO1 = cRO1;
	}

	public CoralReefOptimization getCRO2() {
		return CRO2;
	}

	public void setCRO2(CoralReefOptimization cRO2) {
		CRO2 = cRO2;
	}

	public CoralReefOptimization getCRO3() {
		return CRO3;
	}

	public void setCRO3(CoralReefOptimization cRO3) {
		CRO3 = cRO3;
	}

	public ParticleSwarmOptimization getPSO() {
		return PSO;
	}

	public void setPSO(ParticleSwarmOptimization pSO) {
		PSO = pSO;
	}

	public HashMap<OptimizationAlgorithm, String> getMap() {
		return map;
	}

	public void setMap(HashMap<OptimizationAlgorithm, String> map) {
		this.map = map;
	}

	public ArrayList<OptimizationAlgorithm> getOptimizationAlgorithms() {
		return optimizationAlgorithms;
	}

	public void setOptimizationAlgorithms(ArrayList<OptimizationAlgorithm> optimizationAlgorithms) {
		this.optimizationAlgorithms = optimizationAlgorithms;
	}
	
	
	
}
