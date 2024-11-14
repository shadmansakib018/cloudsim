package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.EX.DatacenterBrokerEX;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.distributions.ExponentialDistr;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TandemAppExample5 {

	private static List<GuestEntity> guestList;

	private static List<NetworkHost> hostList;

	private static NetworkDatacenter datacenter;

	private static List<AppCloudlet> appCloudletList;

	private static List<Cloudlet> noisyCloudletList;

	private static DatacenterBrokerEX broker;

	private static final int numberOfHosts = 4;
	private static final int numberOfVms = 8;

	private static final int numberOfPeriodicActivations = 4;
	private static final int numberOfNoisyCloudlets = 100;

	// Realistic cloudlet arrival
	private static ExponentialDistr distr;

	private static int deadline = 3100;
	private static int period = deadline;
	/**
	 * Example of periodic tandem DAG: A ---> B
	 * with noisy cloudlets (with realistic arrival sampled from exponential distribution).
	 * with Datacenter configuration:
*                                                Agg. Switch
* 										         /        \
* 										ToR switch        ToR switch
* 								   		 /	 \  		   /	 \
* 									 Host0    Host1     Host2    Host3
	 * 						   		VM0 VM4  VM1 VM5   VM2 VM6  VM3 VM7
	 *
	 *
	 * Depending on the cloudlet placement, the network may be used or not.
	 * 
	 * @param args the args
	 * @author Remo Andreoli
	 */
	public static void main(String[] args) {
		distr = new ExponentialDistr(5, 1000);

		Log.println("Starting TandemAppExample5...");

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			datacenter = createDatacenter("Datacenter_0");
			
			// Third step: Create Broker (Make sure the broker stays alive the whole time)
			broker = new DatacenterBrokerEX("Broker", 1000000);

			guestList = CreateVMs(datacenter.getId());

			appCloudletList = new ArrayList<>();

			for (int i = 0; i < numberOfPeriodicActivations; i++) {
				AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, i, deadline, broker.getId());
				createTaskList(app);
				appCloudletList.add(app);

				broker.submitCloudletList(app.cList, 0.02 + i*period);
			}

			noisyCloudletList = new ArrayList<>();
			UtilizationModel u = new UtilizationModelFull();
			for (int i = 0; i < numberOfNoisyCloudlets; i++) {
				Cloudlet cloudlet = new Cloudlet(1000+i, 1000, 1, 1, 1, u, u, u);
				cloudlet.setUserId(broker.getId());
				//cloudlet.setGuestId(0);

				noisyCloudletList.add(cloudlet);
				broker.submitCloudletList(List.of(cloudlet), distr.sample());
			}

			// submit vm list to the broker
			broker.submitGuestList(guestList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ datacenter.totalDataTransfer);

			Log.println("TandemAppExample5 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the datacenter
	 */
	private static NetworkDatacenter createDatacenter(String name) {
		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 10;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

		hostList = new ArrayList<>();
		for (int i=0; i<numberOfHosts; i++) {
			List<Pe> peList = new ArrayList<>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));
			hostList.add(new NetworkHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList)));
		}



		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // no SAN devices as of now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);

		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					new VmAllocationPolicySimple(hostList),
					storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create Internal Datacenter network
		CreateNetwork(datacenter);
		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet cloudlet : list) {
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				Log.print("SUCCESS");
				Log.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getGuestId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getExecFinishTime()));
			}
		}

	}

	/**
	 * Creates virtual machines in a datacenter
	 * @param datacenterId The id of the datacenter where to create the VMs.
	 */
	private static ArrayList<GuestEntity> CreateVMs(int datacenterId) {
		ArrayList<GuestEntity> vmList = new ArrayList<>();

		int mips = 1;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		int pesNumber = 1;
		String vmm = "Xen";

		for (int i=0; i<numberOfVms; i++) {
			vmList.add(new Vm(
					i,
					broker.getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					vmm,
					new CloudletSchedulerTimeShared()));
		}

		return vmList;
	}

	static private void createTaskList(AppCloudlet appCloudlet) {
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		// Define cloudlets
		NetworkCloudlet cla = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		cla.setUserId(broker.getId());
		cla.setGuestId(guestList.get(0).getId());
		appCloudlet.cList.add(cla);

		NetworkCloudlet clb = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		clb.setUserId(broker.getId());
		clb.setGuestId(guestList.get(2).getId());
		appCloudlet.cList.add(clb);

		// Configure task stages within the cloudlets
		//
		cla.addExecutionStage(1000);
		cla.addSendStage(1000, clb);

		//
		clb.addRecvStage(cla);
		clb.addExecutionStage(1000);
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		Switch ToRSwitch1 = new Switch("Edge0", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
					0, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);
		Switch ToRSwitch2 = new Switch("Edge1", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
				0, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);

		Switch AggrSwitch = new Switch("Aggr0", NetworkConstants.AggSwitchPort, Switch.SwitchLevel.AGGR_LEVEL,
				0, NetworkConstants.BandWidthEdgeAgg, NetworkConstants.BandWidthEdgeAgg, dc);

		dc.registerSwitch(ToRSwitch1);
		dc.registerSwitch(ToRSwitch2);
		dc.registerSwitch(AggrSwitch);

		dc.attachSwitchToSwitch(ToRSwitch1, AggrSwitch);
		dc.attachSwitchToSwitch(ToRSwitch2, AggrSwitch);

		// Attach to hosts
		List<NetworkHost> netHosts = dc.getHostList();
		dc.attachSwitchToHost(ToRSwitch1, netHosts.get(0));
		dc.attachSwitchToHost(ToRSwitch1, netHosts.get(1));

		dc.attachSwitchToHost(ToRSwitch2, netHosts.get(2));
		dc.attachSwitchToHost(ToRSwitch2, netHosts.get(3));
	}
}