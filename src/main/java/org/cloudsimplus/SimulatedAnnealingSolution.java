package org.cloudsimplus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;

public class SimulatedAnnealingSolution extends CloudletToVmMappingSolution {

	public SimulatedAnnealingSolution(Heuristic heuristic) {
		super(heuristic);
	}

	public SimulatedAnnealingSolution(final CloudletToVmMappingSolution solution) {
		super(solution);
	}

	public double computeCostDiffOfAllVms() {
		Set<Entry<Vm, List<Entry<Cloudlet, Vm>>>> map = groupCloudletsByVm()
				.entrySet();

		double max = map
				.stream()
				.mapToDouble(this::getVmCost).max().orElse(100);

		double min = map
				.stream()
				.mapToDouble(this::getVmCost).min().orElse(0);

		return max - min;
	}

	private boolean swapVmsOfTwoSelectedMapEntries(final List<Map.Entry<Cloudlet, Vm>> entries) {
		if (entries == null || entries.size() != 2 || entries.get(0) == null || entries.get(1) == null) {
			return false;
		}

		final Vm vm0 = entries.get(0).getValue();
		final Vm vm1 = entries.get(1).getValue();
		final Cloudlet cloudlet0 = entries.get(0).getKey();
		final Cloudlet cloudlet1 = entries.get(1).getKey();
		bindCloudletToVm(cloudlet0, vm1);
		bindCloudletToVm(cloudlet1, vm0);
		entries.get(0).setValue(vm1);
		entries.get(1).setValue(vm0);

		return true;
	}

	public boolean swapVmsOfTwoRandomSelectedMapEntries() {
		return swapVmsOfTwoSelectedMapEntries(getRandomMapEntries());
	}
}
