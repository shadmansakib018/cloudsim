package org.cloudbus.cloudsim;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

public class DynamicVmLoadBalancer extends VmLoadBalancer {
//	private Map<Integer, VirtualMachineState> vmStatesList;
	private int currVm = -1;
	private List<? extends GuestEntity> vmList;



	public DynamicVmLoadBalancer(DatacenterBroker dcb) {
		setName("DynamicVmLoadBalancer");
//		this.vmStatesList = dcb.vmStatesList;
		this.vmList = dcb.getGuestsCreatedList(); 
	}
	
	public int getNextAvailableVm(Cloudlet cl){

	    double minUtilization = Double.MAX_VALUE;  // Start with the highest possible utilization
	    int selectedVmId = -1;

	    for (GuestEntity vm : vmList) {
	        double utilization = vm.getTotalUtilizationOfCpu(cl.getSubmissionTime());
	        System.out.println("utilization of vmID# : " + vm.getId()+ " "+ utilization);

	        // Assuming a threshold of 80% utilization as overloaded
	        if (utilization < minUtilization && utilization < 0.8) {
	            minUtilization = utilization;
	            selectedVmId = vm.getId();  // Get the ID of the least utilized VM
	        }
	    }

	    return selectedVmId;
		
	}

}
