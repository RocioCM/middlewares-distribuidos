package org.cloudsimplus;

import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;

public class SimulatedAnnealingHeuristic implements CloudletToVmMappingHeuristic {
	private final int maxTime = 200;
	private List<Cloudlet> cloudletList;
	private List<Vm> vmList;
	private double solveTime = 0;
	private int searchesByIteration = 1;
	private CloudletToVmMappingSolution bestSolution;
	private CloudletToVmMappingSolution latestNeighbor;

	@Override
	public double getAcceptanceProbability() {
		return 1 / this.solveTime;
	}

	@Override
	public int getRandomValue(int maxValue) {
		Random rand = new Random();
		int number = rand.nextInt(maxValue);
		return number;
	}

	@Override
	public boolean isToStopSearch() {
		Boolean stop = false;
		if (this.solveTime >= this.maxTime) {
			stop = true;
		} else if (true) {
			// TODO: if solution is perfect return true.
			// "Perfect" means the largest task time.
		}
		return stop;
	}

	@Override
	public CloudletToVmMappingSolution getInitialSolution() {
		CloudletToVmMappingSolution initialSolution = new CloudletToVmMappingSolution(this);
		// TODO: repartir parejo los cloudlets a las VMs.
		return initialSolution;
	}

	@Override
	public CloudletToVmMappingSolution getNeighborSolution() {
		return this.latestNeighbor;
	}

	@Override
	public CloudletToVmMappingSolution createNeighbor(CloudletToVmMappingSolution source) {
		CloudletToVmMappingSolution neighbor = new CloudletToVmMappingSolution(source);
		// TODO: generate the neighbor. Probar opciones:
		// 1. Tomar 1 random y moverlo a una vm random.
		// 2. Tomar del más congestionado y llevar al menos.
		this.latestNeighbor = neighbor;
		return neighbor;
	}

	public Boolean acceptSolution() {
		Boolean isNeighborBest = latestNeighbor.compareTo(bestSolution) >= 1;
		if (isNeighborBest) {
			return true;
		}
		double temperature = getAcceptanceProbability();
		double randomNumber = getRandomValue(101) / 100;
		if (randomNumber <= temperature) {
			return true;
		}
		return false;
	}

	@Override
	public CloudletToVmMappingSolution solve() {
		bestSolution = getInitialSolution();
		while (!isToStopSearch()) {
			solveTime++;
			createNeighbor(bestSolution);
			if (acceptSolution()) {
				bestSolution = latestNeighbor;
			}
		}
		return bestSolution;
	}

	@Override
	public CloudletToVmMappingSolution getBestSolutionSoFar() {
		return this.bestSolution;
	}

	@Override
	public int getSearchesByIteration() {
		return this.searchesByIteration;
	}

	@Override
	public void setSearchesByIteration(int numberOfNeighborhoodSearches) {
		this.searchesByIteration = numberOfNeighborhoodSearches;
	}

	@Override
	public double getSolveTime() {
		return this.solveTime;
	}

	@Override
	public List<Cloudlet> getCloudletList() {
		return this.cloudletList;
	}

	@Override
	public List<Vm> getVmList() {
		return this.vmList;
	}

	@Override
	public void setCloudletList(List<Cloudlet> cloudletList) {
		this.cloudletList = cloudletList;
	}

	@Override
	public void setVmList(List<Vm> vmList) {
		this.vmList = vmList;
	}
}
