/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Constants;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmAllocationPolicySimpler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import java.util.Random;

/**
 * A simple example showing how to create
 * a datacenter with two hosts and run two
 * cloudlets on it. The cloudlets run in
 * VMs with different MIPS requirements.
 * The cloudlets will take different time
 * to complete the execution depending on
 * the requested VM performance.
 */
public class OriginalExample3 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	
	private static List<Datacenter> DatacenterList;
	private static List<Datacenter> CopyDatacenterList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.println("Starting CloudSimExample3...");

		try {
			System.out.println("Thread ID: " + Thread.currentThread().getId());
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
//			double[] gmtOffsets = {-5.0, -8.0, 9.0, 8.0, 1.0, 10.0, -3.0, 2.0};
//			double[] gmtOffsets = {
//					-5.0,  // Eastern Standard Time (EST)
//		            -6.0,  // Central Standard Time (CST)
//		            -7.0,  // Mountain Standard Time (MST)
//		            -8.0,  // Pacific Standard Time (PST)
//		            -9.0,  // Alaska Standard Time (AKST)
//		            -10.0, // Hawaii-Aleutian Standard Time (HST)
//		            -4.0,  // Atlantic Standard Time (AST)
//		            1.0   // Central European Time (CET)
//					};
			double[] gmtOffsets = {
					-5.0,
					-5.0,
					-6.0,
					7.0,
					8.0,
					9.0,
					10.0,
					-4.0
					};
			
			//east coast e.g new york
//			Datacenter datacenter0 = createDatacenter("Datacenter_0", -5.0);
			//west coast e.g california
//			Datacenter datacenter1 = createDatacenter("Datacenter_1", -8.0);
//			// asia: east asia eg japan
//			Datacenter datacenter2 = createDatacenter("Datacenter_2", 9.0);
//			// asia: south east asia eg singapore
//			Datacenter datacenter3 = createDatacenter("Datacenter_3", 8.0);
//			// europe central europe eg germany
//			Datacenter datacenter4 = createDatacenter("Datacenter_4", 1.0);
//			// australia e.g sydney
//			Datacenter datacenter5 = createDatacenter("Datacenter_5", 10.0);
//			// south america eg brazil
//			Datacenter datacenter6 = createDatacenter("Datacenter_6", -3.0);
//			// africa eg soouth afirca
//			Datacenter datacenter7 = createDatacenter("Datacenter_7", 2.0);
	
//			DatacenterList = new ArrayList<>();
//			DatacenterList.add(datacenter0);
//			DatacenterList.add(datacenter1);
//			DatacenterList.add(datacenter2);
//			DatacenterList.add(datacenter3);
//			DatacenterList.add(datacenter4);
//			DatacenterList.add(datacenter5);
//			DatacenterList.add(datacenter6);
//			DatacenterList.add(datacenter7);
			
			DatacenterList = new ArrayList<>();

			for (int j = 0; j < 3; j++) {
			    Datacenter datacenter = createDatacenter("Datacenter_" + j, gmtOffsets[j]);
			    DatacenterList.add(datacenter);
			}
//			CopyDatacenterList = new ArrayList<>(DatacenterList);
			



			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			System.out.println("broker ID: " +brokerId);

			vmlist = new ArrayList<>();

			CreateVmCharacteristics CreateVmCharacteristics = new CreateVmCharacteristics();
			
			// Loop for 8 times (assuming 8 data centers)
			for (int i = 0; i < 3; i++) {
			    // Create VMs for version one and version two
				int numberOfVmsPerDc = Constants.numberOfVmsPerDC;
			    List<Vm> vmListVersionOne = CreateVmCharacteristics.createVmsVersionOne(numberOfVmsPerDc/2, brokerId);
			    List<Vm> highPerformanceVmList = CreateVmCharacteristics.createVmsVersionTwo(numberOfVmsPerDc/2, brokerId);

			    // Add to the main list
			    vmlist.addAll(vmListVersionOne);
			    vmlist.addAll(highPerformanceVmList);
			}
			
			//submit vm list to the broker
			broker.submitGuestList(vmlist);

			cloudletList = new ArrayList<>();
			createCloudlets createCloudlets = new createCloudlets();
			cloudletList = createCloudlets.createTasks(brokerId);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();


	        List<Cloudlet> newList = broker.getCloudletReceivedList();
//			ShowResults.printCloudletList(newList, vmlist);
			ShowResults.writeCloudletDataToCsv(newList, vmlist, broker.loadBalancer.getName());
			double totalDcProcessingTime = 0.0;
			for(Datacenter dc: DatacenterList) {
				totalDcProcessingTime += (dc.lastProcessTime/1000);
				System.out.println("Cost to run " + dc.getName() + " for "+ dc.lastProcessTime/1000 + " seconds : $"+ (dc.lastProcessTime/1000) * dc.getCharacteristics().getCostPerSecond());
			}
			System.out.println(totalDcProcessingTime/DatacenterList.size());
			broker.getDataCenterCost();
//			broker.getVmCost();

			Log.println("CloudSimExample3 finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name, double time_zone){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<>();

		int mips = 1000;
		int numberOfVmsPerDc = Constants.numberOfVmsPerDC;
		double halfVms = (double) numberOfVmsPerDc / 2;
		int halfVmsRoundedUp = (int) Math.ceil(halfVms);
		
		double result = (double) numberOfVmsPerDc / 4;
		int roundedUp = (int) Math.ceil(result);
		

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		for (int i = 0; i < roundedUp; i++) {
		    peList1.add(new Pe(i, new PeProvisionerSimple(mips)));  // Store Pe ID and MIPS rating
		}


		List<Pe> peList2 = new ArrayList<>();

		for (int i = 0; i < numberOfVmsPerDc; i++) {
		    peList2.add(new Pe(i, new PeProvisionerSimple(mips)));
		}


		//4. Create Hosts with its id and list of PEs and add them to the list of machines
				int hostId=0;
				int ram = 1024*halfVmsRoundedUp; //host memory (MB)
				long storage = 10000*halfVmsRoundedUp; //host storage
				int bw = 1000*halfVmsRoundedUp;

				hostList.add(
		    			new Host(
		    				hostId,
		    				new RamProvisionerSimple(ram),
		    				new BwProvisionerSimple(bw),
		    				storage,
		    				peList1,
		    				new VmSchedulerTimeShared(peList1)
		    			)
		    		); // This is our first machine

				hostId++;

				hostList.add(
		    			new Host(
		    				hostId,
		    				new RamProvisionerSimple(ram*2*halfVmsRoundedUp),
		    				new BwProvisionerSimple(bw*2*halfVmsRoundedUp),
		    				storage*2,
		    				peList2,
		    				new VmSchedulerTimeShared(peList2)
		    			)
		    		); // Second machine



		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.004;		// the cost of using memory in this resource
		double costPerStorage = 0.0001;	// the cost of using storage in this resource
		double costPerBw = 0.01;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimpler(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("BrokerNum0");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	


}