package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.core.CustomVm;
import org.cloudbus.cloudsim.core.GuestEntity;

public class DynamicVmLoadBalancer extends VmLoadBalancer {
	private Map<Integer, VirtualMachineState> vmStatesList;
//	private int currVm = -1;
	private List<? extends GuestEntity> vmList;
	public boolean once = true;
	private List<CustomVm> customVmList;
	int originalMin = 10000;    // Minimum value of the original range
    int originalMax = 30000;    // Maximum value of the original range
    int targetMin = 0;         // Minimum value of the target range
    int targetMax = 500;       // Maximum value of the target range


	public DynamicVmLoadBalancer(DatacenterBroker dcb) {
		setName("DynamicVmLoadBalancer");
//		this.vmStatesList = dcb.vmStatesList;
		this.vmList = dcb.getGuestsCreatedList(); 
		this.customVmList = new ArrayList<>();
		
	}
	
	public void createCustomVm(List<? extends GuestEntity> vmList) {
		for (GuestEntity vm : vmList) {
            CustomVm customVm = new CustomVm(
                vm.getId(),
                vm.getUserId(),
                vm.getMips(),
                vm.getRam(),
                vm.getBw(),
                vm.getSize()
            );
            customVmList.add(customVm);
        }
	}
	
    // Function to get the next available VM
    public int getNextAvailableVm(Cloudlet cl) {
    	if(once) {
    		createCustomVm(vmList);
    		once = false;
    	}
        int bestVmId = -1;
        double bestScore = -1;
        

        // Check each VM for the best available one
        for (CustomVm vm : customVmList) {
        	
            double score = getCurrentScore(vm, cl);
            if (score > bestScore) {
                bestScore = score;
                bestVmId = vm.getId();
            }
        }

        if (bestVmId == -1) {
            System.out.println("No suitable VM found........................");
        } else {
            System.out.println("Best VM ID selected: " + bestVmId);
            allocateResourcesToVm(bestVmId, cl);
        }

        return bestVmId;
    }

    // Function to calculate the score of a VM based on available resources
    private double getCurrentScore(CustomVm vm, Cloudlet cl) {
    	
        // Calculate the remaining available resources
        double availableMips = vm.getMips() - vm.getCurrentAllocatedMips();
        double availableRam = vm.getRam() - vm.getCurrentAllocatedRam();
        double availableBw = vm.getBw() - vm.getCurrentAllocatedBw();

//        System.out.println("Checking VM ID: " + vm.getId());
//        System.out.println("Available MIPS: " + availableMips);
//        System.out.println("Available RAM: " + availableRam);
//        System.out.println("Available BW: " + availableBw);

        // If any resource is insufficient, the VM is considered overloaded
        if (availableMips <= 0 || availableRam <= 0 || availableBw <= 0) {
            return -1; // VM is overloaded
        }

        // Score is a weighted sum of available resources, higher score = better VM
        double score = availableMips * 0.5 + availableRam * 0.3 + availableBw * 0.2;
//        System.out.println("VM ID: " + vm.getId() + " Score: " + score);
        return score;
    }

    // Function to allocate resources to the selected VM
    private void allocateResourcesToVm(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);

        if (selectedVm != null) {
            // Allocate resources to the selected VM based on Cloudlet requirements
        	
            selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + normalize(cl.getCloudletLength(), originalMin, originalMax, targetMin, (long)selectedVm.getMips()));
            selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + normalize(cl.getCloudletLength(), originalMin, originalMax, targetMin, selectedVm.getRam()));
            selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + normalize(cl.getCloudletLength(), originalMin, originalMax, targetMin, selectedVm.getBw()));

//            System.out.println("Resources allocated to VM ID: " + selectedVm.getId());
        }
    }
    
    // Function to release resources from a VM once the Cloudlet finishes
    public void releaseResources(int vmId, Cloudlet cl) {
    	CustomVm selectedVm = customVmList.get(vmId);

        if (selectedVm != null) {
            // Release resources allocated to the Cloudlet
            selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - normalize(cl.getCloudletLength(), originalMin, originalMax, targetMin, (long)selectedVm.getMips()));
            selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - normalize(cl.getCloudletLength(), 0, originalMax, targetMin, selectedVm.getRam()));
            selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - normalize(cl.getCloudletLength(), 0, originalMax, targetMin, selectedVm.getBw()));

//            System.out.println("Resources released from VM ID: " + selectedVm.getId());
        }
    }
    
    // Method to normalize a value from one range to another
    public static int normalize(long x, int min_x, int max_x, int min_y, long max_y) {
        // Apply the normalization formula
        return (int) ((double) (x - min_x) / (max_x - min_x) * (max_y - min_y) + min_y);
    }


}
