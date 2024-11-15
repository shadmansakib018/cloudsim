package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.GuestEntity;

public class RoundRobinVmLoadBalancer extends VmLoadBalancer {
	
	private int currVm = -1;
	private List<? extends GuestEntity> vmList;


	public RoundRobinVmLoadBalancer(DatacenterBroker dcb) {
		setName("RoundRobinVmLoadBalancer");
		this.vmList = dcb.getGuestsCreatedList();
	}
	
	public int getNextAvailableVm(Cloudlet cl){

		currVm++;
		
		if (currVm >= vmList.size()){
			currVm = 0;
		}
		
		allocatedVm(currVm);
		
		return currVm;
		
	}

}