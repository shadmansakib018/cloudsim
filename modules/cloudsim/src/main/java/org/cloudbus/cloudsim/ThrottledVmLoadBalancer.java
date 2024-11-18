package org.cloudbus.cloudsim;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;

public class ThrottledVmLoadBalancer  extends VmLoadBalancer {

	private Map<Integer, VirtualMachineState> vmStatesList;
	private List<? extends GuestEntity> vmList;
//	private DatacenterBroker dcbLocal;
	boolean once = true;
	/** 
	 * Constructor
	 * 
	 * @param dcb The {@link DatacenterController} using the load balancer.
	 */
	public ThrottledVmLoadBalancer(DatacenterBroker dcb){
		setName("ThrottledVmLoadBalancer");
		this.vmStatesList = dcb.vmStatesList;
		this.vmList = dcb.getGuestsCreatedList();
		
//		this.dcbLocal=dcb;
//		dcb.addCloudSimEventListener(this);
		
	} 
 
	/**
	 * @return VM id of the first available VM from the vmStatesList in the calling
	 * 			{@link DatacenterController}
	 */
	@Override
	public int getNextAvailableVm(Cloudlet cl){
		
//		double TotalUtilizationOfCpu = 0.0;
//		for(GuestEntity vm : vmList) {
//			double CPUutilization = vm.getTotalUtilizationOfCpu(CloudSim.clock());
//			System.out.println(CloudSim.clock() + " Vm ID: #"+ vm.getId() + " CPU utilization: "
//			+ CPUutilization + " ram"+ vm.getRam() + " ram req "+ vm.getCurrentRequestedRam());
////			if(CPUutilization < TotalUtilizationOfCpu) {
////				chosenVm = vm;
////				TotalUtilizationOfCpu = CPUutilization;
////			}
//		}
		
		int vmId = -1;		
		if (vmStatesList.size() > 0){
			int temp;
			for (Iterator<Integer> itr = vmStatesList.keySet().iterator(); itr.hasNext();){
				temp = itr.next();
				VirtualMachineState state = vmStatesList.get(temp); 
				if (state.equals(VirtualMachineState.AVAILABLE)){
					vmId = temp;
					break;
				}
			}
		}
		
		allocatedVm(vmId);
		
		return vmId;
		
	}

	@Override
	protected void releaseResources(int vmId, Cloudlet cl) {
		// TODO Auto-generated method stub
		
	}
	
}
