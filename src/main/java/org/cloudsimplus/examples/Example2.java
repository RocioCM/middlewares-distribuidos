/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2021 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.examples;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.SimulatedAnnealingHeuristic;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing;

import java.util.ArrayList;
import java.util.List;

/**
 * An example that runs 2 Cloudlets into a VM where its number
 * of PEs is just half of the total PEs required by all Cloudlets.
 * The Vm uses a {@link CloudletSchedulerTimeShared}.
 * Since there are enough PEs for all Cloudlets, this scheduler
 * shares existing PEs among all Cloudlets, so that there aren't
 * waiting Cloudlets.
 * They start at the same time and also considering they have the same length,
 * they finish together too.
 * However, each Cloudlet will take the double of the time
 * it would take if there were enough PEs for all of them.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.2.1
 */
public class Example2 {
    private static final int PE_MIPS = 1000; // in Million of Instructions per Second

    private static final int VM_GROUPS = 3;
    private static final int VMS_PER_GROUP = 5;
    private static final int VMS = VM_GROUPS * VMS_PER_GROUP;
    private static final int VM_PES = 2;
    private static final int VM_MIPS = 1000;
    private static final int VM_BW = 1000;
    private static final int VM_RAM = 512;
    private static final int VM_STORAGE = 1000;

    private static final int HOSTS = 1;
    private static final int HOST_PES = VM_PES * VMS * 5; // Number of processing units
    private static final long HOST_RAM = VM_RAM * VMS * 4; // in Megabytes
    private static final long HOST_BW = VM_BW * VMS * 4; // in Megabits/s
    private static final long HOST_STORAGE = VM_STORAGE * VMS * 4; // in Megabytes

    private static final int CLOUDLET_GROUPS = 5;
    private static final int CLOUDLETS_PER_GROUP = 14;
    private static final int CLOUDLETS = CLOUDLET_GROUPS * CLOUDLETS_PER_GROUP;
    private static final int CLOUDLET_PES = 2;
    private static final int[] CLOUDLET_LENGTH = { 10000, 500, 5000, 2000, 1300 };
    private static final int[] CLOUDLET_FILE_SIZE = { 1024, 2048, 512, 3036, 4096 };
    private static final int[] CLOUDLET_OUTPUT_SIZE = CLOUDLET_FILE_SIZE;

    private CloudSim simulation;
    private DatacenterBrokerHeuristic broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;

    public static void main(String[] args) {
        new Example2();
    }

    private Example2() {
        /*
         * Enables just some level of log messages.
         * Make sure to import org.cloudsimplus.util.Log;
         */
        // Log.setLevel(ch.qos.logback.classic.Level.WARN);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();

        // Creates a broker that is a software acting on behalf a cloud customer to
        // manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerHeuristic(simulation);
        broker0.setHeuristic(new SimulatedAnnealingHeuristic());

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();

        simulation2();
    }

    private void simulation2() {
        simulation = new CloudSim();
        datacenter0 = createDatacenter();

        // Creates a broker that is a software acting on behalf a cloud customer to
        // manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerHeuristic(simulation);
        broker0.setHeuristic(new CloudletToVmMappingSimulatedAnnealing(1, new UniformDistr(0, 1)));

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();
    }

    /**
     * Creates a Datacenter and its Hosts.
     */
    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for (int h = 0; h < HOSTS; h++) {
            Host host = createHost();
            hostList.add(host);
        }

        final Datacenter dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        return dc;
    }

    private Host createHost() {
        List<Pe> peList = new ArrayList<>(HOST_PES);
        // List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(PE_MIPS, new PeProvisionerSimple()));
        }

        ResourceProvisioner ramProvisioner = new ResourceProvisionerSimple();
        ResourceProvisioner bwProvisioner = new ResourceProvisionerSimple();
        VmScheduler vmScheduler = new VmSchedulerTimeShared();
        Host host = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
        host
                .setRamProvisioner(ramProvisioner)
                .setBwProvisioner(bwProvisioner)
                .setVmScheduler(vmScheduler);
        return host;
    }

    /**
     * Creates a list of VMs.
     */
    private List<Vm> createVms() {
        final List<Vm> list = new ArrayList<>(VMS);

        for (int g = 0; g < VM_GROUPS; g++) {
            int factor = g + 1;
            for (int v = 0; v < VMS_PER_GROUP; v++) {
                Vm vm = new VmSimple(g * VMS_PER_GROUP + v, VM_MIPS * factor, VM_PES * factor)
                        .setRam(VM_RAM * factor).setBw(VM_BW * factor).setSize(VM_STORAGE * factor)
                        .setCloudletScheduler(new CloudletSchedulerSpaceShared());

                list.add(vm);
            }
        }

        return list;
    }

    /**
     * Creates a list of Cloudlets.
     */
    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
        UtilizationModel utilization = new UtilizationModelStochastic(30);
        // UtilizationModel utilization = new UtilizationModelDynamic(30);
        for (int g = 0; g < CLOUDLET_GROUPS; g++) {

            for (int c = 0; c < CLOUDLETS_PER_GROUP; c++) {
                Cloudlet cloudlet = new CloudletSimple(g * CLOUDLETS_PER_GROUP + c, CLOUDLET_LENGTH[g], CLOUDLET_PES)
                        .setFileSize(CLOUDLET_FILE_SIZE[g])
                        .setOutputSize(CLOUDLET_OUTPUT_SIZE[g])
                        .setUtilizationModel(utilization);
                list.add(cloudlet);
            }
        }
        return list;
    }
}
