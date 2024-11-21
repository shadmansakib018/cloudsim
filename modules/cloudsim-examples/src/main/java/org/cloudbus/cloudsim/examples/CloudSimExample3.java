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
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
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
public class CloudSimExample3 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.println("Starting CloudSimExample3...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			Datacenter datacenter1 = createDatacenter("Datacenter_1");


			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			vmlist = new ArrayList<>();

			CreateVmCharacteristics CreateVmCharacteristics = new CreateVmCharacteristics();
			List<Vm> vmListVersionOne = CreateVmCharacteristics.createVmsVersionOne(3, brokerId);
			List<Vm> highPerformanceVmList = CreateVmCharacteristics.createVmsVersionTwo(2, brokerId);
			vmlist.addAll(vmListVersionOne);
			vmlist.addAll(highPerformanceVmList);
			
			//submit vm list to the broker
			broker.submitGuestList(vmlist);


			cloudletList = new ArrayList<>();
			Random random = new Random();
			//Cloudlet properties
			int id = 1;
			long length = 400;
			long fileSize = 300;
			long outputSize = 300;
			int originalMin = 10000;    // Lower bound of the range
	        int originalMax = 30000;    // Upper bound of the range
			UtilizationModel utilizationModel = new UtilizationModelFull();
			int[] numbers = {};
			
			String filePath = "C:\\Users\\ss4587s\\Desktop\\CloudSimCSVs\\Resources\\taskLength.txt";

			// Use BufferedReader for line-by-line reading
	         BufferedReader reader = new BufferedReader(new FileReader(filePath));
	            
	            // List to temporarily hold the numbers
	            List<Integer> numberList = new ArrayList<>();
	            
	            String line;
	            while ((line = reader.readLine()) != null) {
	                // Parse each line as an integer and add to list
	                numberList.add(Integer.parseInt(line.trim()));
	            }
	            reader.close();
	            
	            // Convert List to Array
	            numbers = numberList.stream().mapToInt(i -> i).toArray();
	            int pesNumber = 1; 
	        for (int i = 0; i < numbers.length; i++) {
//	        	 int randomNumber = random.nextInt(originalMax - originalMin + 1) + originalMin;
	            Cloudlet cloudlet = new Cloudlet(i, numbers[i], pesNumber, fileSize, outputSize,
	            									utilizationModel, utilizationModel, utilizationModel);
	            
	            // Set the user ID of the cloudlet to associate it with the broker
	            cloudlet.setUserId(brokerId);

	            cloudletList.add(cloudlet);
	        }

			//submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

//			CloudSim.stopSimulation();

			ShowResults.printCloudletList(newList, vmlist);
//			ShowResults.writeCloudletDataToCsv(newList, vmlist, broker.loadBalancer.getName());

			Log.println("CloudSimExample3 finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 4096; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerTimeShared(peList)
    			)
    		); // This is our first machine

		//create another machine in the Data center
		List<Pe> peList2 = new ArrayList<>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips*4)));

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // This is our second machine



		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
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
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		Map<Integer, Integer> guestIdCountMap = new HashMap<>();

		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID"
					+ indent + indent + "STATUS"
					+ indent + indent + "Task Length"
//					+ indent + indent + "Datacenter ID"
					+ indent + indent + "VM ID"
					+ indent + indent + "RAM"
					+ indent + indent + "Storage"
					+ indent + indent + "Bandwidth"
					+ indent + indent + "MIPS"
					+ indent + indent + "Processing Time"
					+ indent + indent + "Start Time"
					+ indent + indent + "Finish Time");
		
		Log.println("");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
			guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {

				Log.println(
						indent + cloudlet.getCloudletId()
						+ indent + indent + indent + "SUCCESS"
						+ indent + indent + indent + cloudlet.getCloudletLength() 
//						+ indent + indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getGuestId()
						+ indent + indent + indent + vmlist.get(cloudlet.getGuestId()).getRam()
						+ indent + indent +  vmlist.get(cloudlet.getGuestId()).getSize()
						+ indent + indent + indent +  vmlist.get(cloudlet.getGuestId()).getBw()
						+ indent + indent + indent + vmlist.get(cloudlet.getGuestId()).getMips()
						+ indent + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + indent + indent  + indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent +  dft.format(cloudlet.getExecFinishTime()));
			}
		}
		
		for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
            System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
        }

	}
}
