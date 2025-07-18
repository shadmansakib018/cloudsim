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
 * 24 hour simulation
 */
public class TwentyFourHourSimulation {
	public static String LoadBalancerName;

	private static List<Cloudlet> cloudletList;
	private static List<Datacenter> DatacenterList;
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		 Random rand = new Random(100);
		 List<Double> AvgResponseTimeList = new ArrayList<>();
	     List<Double> AvgWaitingTimeList = new ArrayList<>();
	     List<Double> AvgExecutionTimeList = new ArrayList<>();
	     List<Double> DcRunCostList = new ArrayList<>();
	     List<Double> DcSetupCostList = new ArrayList<>();
	     List<Double> AvgDcProcessingTime = new ArrayList<>();

		Log.println("Starting 24Hour simulation...");

		try {
			for(int k =0; k < 24; k++) {
			System.out.println("Thread ID: " + Thread.currentThread().getId());
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);
			Constants.seed = Constants.seed+k;
			if(k>=17 && k<=22 || k>=8 && k<=10 || k>=13 && k<=14) {
				int[] batchsizeOptions = {5000, 5500, 6000}; // 4000 - 5.5k
				int randomIndex = rand.nextInt(batchsizeOptions.length);
				int randomBatchSize = batchsizeOptions[randomIndex];
				Constants.batchSize = randomBatchSize;
				
				int[] totalBatchesOptions = {18, 19, 20}; // 15-18
				int randomIndex2 = rand.nextInt(totalBatchesOptions.length);
				int randomTotalBatch = totalBatchesOptions[randomIndex2];
				Constants.totalBatches = randomTotalBatch;
			}else {
				int[] batchsizeOptions = {3000, 3500, 4000}; // 1.5k - 3k
				int randomIndex = rand.nextInt(batchsizeOptions.length);
				int randomBatchSize = batchsizeOptions[randomIndex];
				Constants.batchSize = randomBatchSize;
				
				int[] totalBatchesOptions = {9,10,11}; // 6-9
				int randomIndex2 = rand.nextInt(totalBatchesOptions.length);
				int randomTotalBatch = totalBatchesOptions[randomIndex2];
				Constants.totalBatches = randomTotalBatch;
			}


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
			
			DatacenterList = new ArrayList<>();

			for (int j = 0; j < Constants.numberOfDcs; j++) {
			    Datacenter datacenter = createDatacenter("Datacenter_" + j, gmtOffsets[j]);
			    DatacenterList.add(datacenter);
			}
			
			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			System.out.println("broker ID: " +brokerId);

			vmlist = new ArrayList<>();

			CreateVmCharacteristics CreateVmCharacteristics = new CreateVmCharacteristics();
			
			// Loop for 8 times (assuming 8 data centers)
			for (int i = 0; i < Constants.numberOfDcs; i++) {
			    
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
	        System.out.println("Number of Finsihed Cloudlets " + newList.size());
	        Double totalDCCost = broker.getDataCenterCost();
	        DcSetupCostList.add(totalDCCost);
//	        broker.getVmCost();
//			ShowResults.printCloudletList(newList, vmlist);
//			ShowResults.writeCloudletDataToCsv(newList, vmlist, broker.loadBalancer.getName());
	        
	        Map<Integer, Integer> guestIdCountMap = new HashMap<>();
	    	double totalResponseTime = 0;
//	    	double totalWaitingTime = 0;
//	    	double totalExecTime = 0;
			for (Cloudlet cloudlet : newList) {
                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                	guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);
//                	totalWaitingTime = totalWaitingTime + (cloudlet.getExecStartTime()- cloudlet.getSubmissionTimeTwo());
                	totalResponseTime = totalResponseTime + (cloudlet.getActualCPUTime() + (cloudlet.getExecStartTime() - cloudlet.getSubmissionTimeTwo()));
//                	totalExecTime = totalExecTime + cloudlet.getActualCPUTime();
                }
            }
			
			AvgResponseTimeList.add(totalResponseTime /(newList.size()));
//			AvgWaitingTimeList.add(totalWaitingTime /(newList.size()));
//			AvgExecutionTimeList.add(totalExecTime /(newList.size()));
			LoadBalancerName = broker.loadBalancer.getName();
			
//			for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
////                System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
//                System.out.println(entry.getValue());
//
//            }
			
			double CostToRunDC = 0.0;
			double totalDcProcessingTime = 0.0;
			for(Datacenter dc: DatacenterList) {
				totalDcProcessingTime += (dc.lastProcessTime/1000);
				CostToRunDC += (dc.lastProcessTime/1000) * dc.getCharacteristics().getCostPerSecond();
//				System.out.println("Cost to run " + dc.getName()+ " for this many seconds: " + (dc.lastProcessTime/1000)+ ": $"+ (dc.lastProcessTime/1000) * dc.getCharacteristics().getCostPerSecond());
			}
			AvgDcProcessingTime.add(totalDcProcessingTime/DatacenterList.size());
//			System.out.println("Total Cost to run DC (DC operating cost): "+ CostToRunDC);
//			System.out.println(totalDcProcessingTime/DatacenterList.size());
			DcRunCostList.add(CostToRunDC);
			Log.println("**************************************************************");
					
			Log.println("CloudSimExample3 finished! run Number: "+k);
		}
			ShowResults.writeResultsDataToCsv
			(AvgResponseTimeList,DcRunCostList,AvgDcProcessingTime, LoadBalancerName);
			
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