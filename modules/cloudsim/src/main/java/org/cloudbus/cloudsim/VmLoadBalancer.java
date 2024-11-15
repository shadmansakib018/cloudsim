package org.cloudbus.cloudsim;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

abstract public class VmLoadBalancer {
	/** Holds the count of allocations for each VM */
	protected Map<Integer, Integer> vmAllocationCounts;
	public String lbname = "hhhh";
	
	/** No args contructor */
	public VmLoadBalancer(){
		vmAllocationCounts = new HashMap<Integer, Integer>();
	}
	
	/**
	 * The main contract of {@link VmLoadBalancer}. All load balancers should implement
	 * this method according to their specific load balancing policy.
	 * 
	 * @return id of the next available Virtual Machine to which the next task should be
	 * 			allocated 
	 */
	abstract public int getNextAvailableVm(Cloudlet cl);
	
	/**
	 * Used internally to update VM allocation statistics. Should be called by all impelementing
	 * classes to notify when a new VM is allocated.
	 * 
	 * @param currVm
	 */
	protected void allocatedVm(int currVm){
		
		Integer currCount = vmAllocationCounts.get(currVm);
		if (currCount == null){
			currCount = 0;
		}
		vmAllocationCounts.put(currVm, currCount + 1);		
	}
		
	/**
	 * Returns a {@link Map} indexed by VM id and having the number of allocations for each VM.
	 * @return
	 */
	public Map<Integer, Integer> getVmAllocationCounts(){
		return vmAllocationCounts;
	}
	
	public void setName(String name) {
		this.lbname = name;
	}
	
	public String getName() {
		return this.lbname;
	}
}
