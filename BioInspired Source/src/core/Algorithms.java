package core;

import java.util.Random;

import method.AntColonyOptimization;
import method.BeeColonyOptimization;
import method.CoralReefOptimization;
import method.GeneticAlgorithm;
import method.ParticleSwarmOptimization;

public enum Algorithms {
	ACO, AG, BCO, CRO1, CRO2, CRO3, PSO;

	public OptimizationAlgorithm newInstance(Problem problem, int seed, int k, int[][] clusterings) throws Exception {
		Solve[] start = new Solve[Main.numPopulation];
		for (int i = 0; i < start.length; i++) {
			start[i] = new Solve(problem, k, clusterings, Solve.pPartitions, Solve.pEquals, new Random(seed));
			start[i].evaluate();
		}

		switch (this) {
		case ACO:
			double[][] heuristicMachine = AntColonyOptimization.buildHeuristic2(k, clusterings);
			return new AntColonyOptimization(new Random(seed), start, Main.epochs, false, 0.5, 0.5, 0.2, heuristicMachine);
		case AG:
			return new GeneticAlgorithm(new Random(seed), start, Main.epochs, 0.1, 0.9);
		case BCO:
			return new BeeColonyOptimization(new Random(seed), start, Main.epochs, 10);
		case CRO1:
			return new CoralReefOptimization(new Random(seed), start, Main.epochs, 100, false, 0.9, 0.5, 0.8, 0.05, 1);
		case CRO2:
			return new CoralReefOptimization(new Random(seed), start, Main.epochs, 100, true, 0.9, 0.5, 0.8, 0.1, 5);
		case CRO3:
			return new CoralReefOptimization(new Random(seed), start, Main.epochs, 100, false, 0.9, 0.5, 0.8, 0.1, 5);
		case PSO:
			return new ParticleSwarmOptimization(new Random(seed), start, Main.epochs, 0.95, 0.05, 0);
		}
		throw new RuntimeException();
	}

	public String getDescription() {
		switch (this) {
		case ACO:
			return "ACO MST(false) AFN(true) Alpha(0.500000) Beta(0.500000) Ro(0.200000)";
		case AG:
			return "AG Crossover(0.900000) Mutation(0.100000)";
		case BCO:
			return "BCO TurnsMaxNotImproved(10)";
		case CRO1:
			return "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.050000) StepsUntilDepredation(1)";
		case CRO2:
			return "CRO dimension(100) InsertRand(true) InsertRank(false) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)";
		case CRO3:
			return "CRO dimension(100) InsertRand(false) InsertRank(true) Rho(0.900000) Fa-repication(0.500000) Fb-broadcast(0.800000) Fd-depredation(0.100000) StepsUntilDepredation(5)";
		case PSO:
			return "PSO P-OwnWay(0.950000) P-PreviousPosition(0.050000) P-BestPosition(0.000000)";
		}
		throw new RuntimeException();
	}
}