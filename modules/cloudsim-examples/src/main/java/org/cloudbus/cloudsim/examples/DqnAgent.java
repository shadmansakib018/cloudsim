package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Constants;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimpler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


public class DqnAgent {
	
	public static String LoadBalancerName;
	
	// command line args ==> port number, batchsize, Load balancer, epochs
	

	public static void main(String[] args) {
		Constants.commandLineArgs = args;
		Constants.batchSize = Integer.parseInt(Constants.commandLineArgs[1]);
		Constants.epochs = Integer.parseInt(Constants.commandLineArgs[3]);
		List<Double> AvgResponseTimeList = new ArrayList<>();
	    try {
	    	for (int i=1; i <= Constants.epochs; i++) {
//	    		long start = System.currentTimeMillis();
	    		Constants.seed = Constants.seed + i;
				List<Cloudlet> cloudletList;
				List<Vm> vmlist;
					
				int num_user = 1;
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false; 
				CloudSim.init(num_user, calendar, trace_flag);
	
				List<Datacenter> datacenterList = new ArrayList<>();
	
				for (int j = 0; j < 1; j++) {
				    Datacenter datacenter = createDatacenter("Datacenter_" + j);
				    datacenterList.add(datacenter);
				}
				
				DatacenterBroker broker = createBroker();
				int brokerId = broker.getId();
	
				vmlist = new ArrayList<>();
	
				CreateVmCharacteristics CreateVmCharacteristics = new CreateVmCharacteristics();
				int numberofVmsHalf = Constants.numberOfVmsPerDC/2;
				
				for (int k = 0; k < 1; k++) {
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
	
				LoadBalancerName = broker.loadBalancer.getName();
		        List<Cloudlet> newList = broker.getCloudletReceivedList();
		        System.out.println("Number of Finsihed Cloudlets " + newList.size());
		        
		        Map<Integer, Integer> guestIdCountMap = new HashMap<>();
		    	double totalResponseTime = 0;
		        for (Cloudlet cloudlet : newList) {
	                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
	                	guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);
	                	totalResponseTime = totalResponseTime + (cloudlet.getActualCPUTime() + (cloudlet.getExecStartTime() - cloudlet.getSubmissionTimeTwo()));
	                }
		        }
		        double avgRT = totalResponseTime /newList.size();
		        AvgResponseTimeList.add(avgRT);
		        if(broker.loadBalancer.lbname.equals("Reinforcement_Learning")) {
		        	broker.loadBalancer.callTrain();
		        	broker.loadBalancer.sendLongTermReward(avgRT);
		        }
	            for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
	                System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
	            }
//	            long finish = System.currentTimeMillis();
//	            long timeElapsed = finish - start;
		        System.out.println("simulation number: "+(i)+ "  Average Response Time: " + avgRT);
		        Thread.sleep(3000);					
//				ShowResults.writeCloudletDataToCsv(newList, vmlist, LoadBalancerName);
		        
		}

	    	ShowResults.writeResultsRL(AvgResponseTimeList, LoadBalancerName, Constants.commandLineArgs[4]);

	}
		catch (Exception e) {
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

	
	private static Datacenter createDatacenter(String name){

		List<Host> hostList = new ArrayList<>();
		List<Pe> peList1 = new ArrayList<>();

		int mips = 1000;
		int numberOfVmsPerDc = Constants.numberOfVmsPerDC;
		double halfVms = (double) numberOfVmsPerDc / 2;
		int halfVmsRoundedUp = (int) Math.ceil(halfVms);
		
		double result = (double) numberOfVmsPerDc / 4;
		int roundedUp = (int) Math.ceil(result);
		

		//
		for (int i = 0; i < roundedUp; i++) {
		    peList1.add(new Pe(i, new PeProvisionerSimple(mips)));
		}


		List<Pe> peList2 = new ArrayList<>();

		for (int i = 0; i < numberOfVmsPerDc; i++) {
		    peList2.add(new Pe(i, new PeProvisionerSimple(mips)));
		}


				int hostId=0;
				int ram = 1024*halfVmsRoundedUp; 
				long storage = 10000*halfVmsRoundedUp;
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
		    		);

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
		    		); 
		String arch = "x86";  
		String os = "Linux";  
		String vmm = "Xen";
		double time_zone = 10.0;        
		double cost = 3.0;          
		double costPerMem = 0.004;		
		double costPerStorage = 0.0001;
		double costPerBw = 0.01;
		LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimpler(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
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
