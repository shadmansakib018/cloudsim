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

	protected abstract void releaseResources(int vmId, Cloudlet cl);
	
	 // Method to allocate a task to a VM
    public void allocateTask(int vmId) {
        vmAllocationCounts.put(vmId, vmAllocationCounts.getOrDefault(vmId, 0) + 1);
//        System.out.println("Task allocated to VM: " + vmId);
    }

    // Method to finish a task on a VM
    public void finishTask(int vmId) {
        if (vmAllocationCounts.containsKey(vmId) && vmAllocationCounts.get(vmId) > 0) {
            vmAllocationCounts.put(vmId, vmAllocationCounts.get(vmId) - 1);
//            System.out.println("Task finished on VM: " + vmId);
        } else {
//            System.out.println("No tasks running on VM: " + vmId);
        }
    }
}
