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
import java.util.Collections;
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
public class CloudSimExample3 {
	public static String LoadBalancerName;

	public static void main(String[] args) {
		List<Double> AvgResponseTimeList = new ArrayList<>();
	     List<Double> AvgWaitingTimeList = new ArrayList<>();
	     List<Double> AvgExecutionTimeList = new ArrayList<>();
	     List<Double> DcRunCostList = new ArrayList<>();
	     List<Double> DcSetupCostList = new ArrayList<>();
	     List<Double> AvgDcProcessingTime = new ArrayList<>();

		try {
			for(int i =0; i < Constants.numberOfDcs; i++) {
			List<Cloudlet> cloudletList;
			List<Vm> vmlist;
				
			Log.println("Starting CloudSimExample3... RUN NUMBER: " + i);
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 
			CloudSim.init(num_user, calendar, trace_flag);
			
//			double[] gmtOffsets = {-5.0, -8.0, 9.0, 8.0, 1.0, 10.0, -3.0, 2.0};
//			double[] gmtOffsets = {
//			-5.0,  // Eastern Standard Time (EST)
//            -6.0,  // Central Standard Time (CST)
//            -7.0,  // Mountain Standard Time (MST)
//            -8.0,  // Pacific Standard Time (PST)
//            -9.0,  // Alaska Standard Time (AKST)
//            -10.0, // Hawaii-Aleutian Standard Time (HST)
//            -4.0,  // Atlantic Standard Time (AST)
//            1.0   // Central European Time (CET)
//			};
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

			List<Datacenter> datacenterList = new ArrayList<>();

			for (int j = 0; j <= i; j++) {
			    Datacenter datacenter = createDatacenter("Datacenter_" + j, gmtOffsets[j]);
			    datacenterList.add(datacenter);
			}
			
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			System.out.println("broker ID: " +brokerId);

			vmlist = new ArrayList<>();

			CreateVmCharacteristics CreateVmCharacteristics = new CreateVmCharacteristics();
			int numberofVmsHalf = Constants.numberOfVmsPerDC/2;
			
			for (int k = 0; k <= i; k++) {
			    List<Vm> vmListVersionOne = CreateVmCharacteristics.createVmsVersionOne(numberofVmsHalf, brokerId);
			    List<Vm> highPerformanceVmList = CreateVmCharacteristics.createVmsVersionTwo(numberofVmsHalf, brokerId);
			    vmlist.addAll(vmListVersionOne);
			    vmlist.addAll(highPerformanceVmList);
			}
			
			broker.submitGuestList(vmlist);

			cloudletList = new ArrayList<>();
			createCloudlets createCloudlets = new createCloudlets();
			cloudletList = createCloudlets.createTasks(brokerId);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();


	        List<Cloudlet> newList = broker.getCloudletReceivedList();
	        System.out.println("Number of Finsihed Cloudlets " + newList.size());
//			ShowResults.printCloudletList(newList, vmlist);
	        Double totalDCCost = broker.getDataCenterCost();
	        DcSetupCostList.add(totalDCCost);
//			double vmCost = broker.getVmCost();
			Map<Integer, Integer> guestIdCountMap = new HashMap<>();
	    	double totalResponseTime = 0;
	    	double totalWaitingTime = 0;
	    	double totalExecTime = 0;
			for (Cloudlet cloudlet : newList) {
                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                	guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);
                	totalWaitingTime = totalWaitingTime + (cloudlet.getExecStartTime()- cloudlet.getSubmissionTimeTwo());
                	totalResponseTime = totalResponseTime + (cloudlet.getActualCPUTime() + (cloudlet.getExecStartTime() - cloudlet.getSubmissionTimeTwo()));
                	totalExecTime = totalExecTime + cloudlet.getActualCPUTime();
                }
            }
			
			AvgResponseTimeList.add(totalResponseTime /(newList.size()));
			AvgWaitingTimeList.add(totalWaitingTime /(newList.size()));
			AvgExecutionTimeList.add(totalExecTime /(newList.size()));
			LoadBalancerName = broker.loadBalancer.getName();
			for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
                System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
            }
			double CostToRunDC = 0.0;
			double totalDcProcessingTime = 0.0;
			for(Datacenter dc: datacenterList) {
				totalDcProcessingTime += (dc.lastProcessTime/1000);
				CostToRunDC += (dc.lastProcessTime/1000) * dc.getCharacteristics().getCostPerSecond();
				System.out.println("Cost to run " + dc.getName()+ " for this many seconds: " + (dc.lastProcessTime/1000)+ ": $"+ (dc.lastProcessTime/1000) * dc.getCharacteristics().getCostPerSecond());
			}
			AvgDcProcessingTime.add(totalDcProcessingTime/datacenterList.size());
			System.out.println("Total Cost to run DC(Vm cost): "+ CostToRunDC);
			DcRunCostList.add(CostToRunDC);
			Log.println("**************************************************************");
			
			}
			
			ShowResults.writeResultsDataToCsv
			(AvgResponseTimeList,AvgWaitingTimeList,AvgExecutionTimeList, DcRunCostList, DcSetupCostList,AvgDcProcessingTime, LoadBalancerName);
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
//		double time_zone = 10.0;         // time zone this resource located
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
