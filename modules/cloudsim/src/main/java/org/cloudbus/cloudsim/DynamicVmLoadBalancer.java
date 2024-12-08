package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CustomVm;
import org.cloudbus.cloudsim.core.GuestEntity;

public class DynamicVmLoadBalancer extends VmLoadBalancer {
	private Map<Integer, VirtualMachineState> vmStatesList;
//	private int currVm = -1;
	private List<? extends GuestEntity> vmList;
	public boolean once = true;
	private List<CustomVm> customVmList;
	int originalMin = 400;    // Minimum value of the original range
    int originalMax = 4000;    // Maximum value of the original range
    int targetMin = 1;         // Minimum value of the target range
//    int targetMax = 500;       // Maximum value of the target range
    double allocAmount = 0.09;

    int lastVmIdAssigned = -1;

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
//        	if(lastVmIdAssigned == vm.getId()) {
//        		continue;
//        	}
//        	System.out.println("getTotalUtilizationOfCpuMips for vm id #" + vm.getId() +" "+vmList.get(vm.getId()).getTotalUtilizationOfCpuMips(CloudSim.clock()));
            double score = getCurrentScore(vm, cl);
            if (score > bestScore) {
                bestScore = score;
                bestVmId = vm.getId();
            }
        }

        if (bestVmId == -1) {
            System.out.println("No suitable VM found........................");
        } else {
        	
//            System.out.println("Best VM ID selected: " + bestVmId);
            allocateResourcesToVm(bestVmId, cl);
        }
//        lastVmIdAssigned = bestVmId;
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
        double score = availableMips + availableRam + availableBw;
//        System.out.println("VM ID: " + vm.getId() + " Score: " + score);
        return score;
    }

    // Function to allocate resources to the selected VM
    private void allocateResourcesToVm(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);
        long cloudletLength = cl.getCloudletLength();

        if (selectedVm != null) {
            // Allocate resources to the selected VM based on Cloudlet requirements
        	
//        	System.out.println("total MIPS: "+ selectedVm.getMips() + " cloudlet length: " + cloudletLength);
        	int normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, (long)selectedVm.getMips());
        	int normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
        	int normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());
//            System.out.println("Amount to be used up for MIPS: " + normalizedMIPS + " this amount of allocation: " + allocAmount);

//            System.out.println("Amount to be used up for RAM: " + selectedVm.getRam()*allocAmount);
//
//            System.out.println("Amount to be used up for BW: " + selectedVm.getBw()*allocAmount);

        	
            selectedVm.setCurrentAllocatedMips
            (selectedVm.getCurrentAllocatedMips() + normalizedMIPS);
//            (selectedVm.getCurrentAllocatedMips() + selectedVm.getMips()*allocAmount);
            
            selectedVm.setCurrentAllocatedRam
            (selectedVm.getCurrentAllocatedRam() + normalizedRAM);
//            (selectedVm.getCurrentAllocatedRam() + selectedVm.getRam()*allocAmount);
            
            selectedVm.setCurrentAllocatedBw
            (selectedVm.getCurrentAllocatedBw() + normalizedBW);
//            (selectedVm.getCurrentAllocatedBw() + selectedVm.getBw()*allocAmount);

        }
    }
    
    // Function to release resources from a VM once the Cloudlet finishes
    public void releaseResources(int vmId, Cloudlet cl) {
    	CustomVm selectedVm = customVmList.get(vmId);
        long cloudletLength = cl.getCloudletLength();
        

        if (selectedVm != null) {
            // Release resources allocated to the Cloudlet
        	
        	int normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, (long)selectedVm.getMips());
        	int normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
        	int normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());
        	
            selectedVm.setCurrentAllocatedMips
            (selectedVm.getCurrentAllocatedMips() - normalizedMIPS);
//            (selectedVm.getCurrentAllocatedMips() - selectedVm.getMips()*allocAmount);
            
            selectedVm.setCurrentAllocatedRam
            (selectedVm.getCurrentAllocatedRam() - normalizedRAM);
//            (selectedVm.getCurrentAllocatedRam() - selectedVm.getRam()*allocAmount);
            
            selectedVm.setCurrentAllocatedBw
            (selectedVm.getCurrentAllocatedBw() - normalizedBW);
//            (selectedVm.getCurrentAllocatedBw() - selectedVm.getBw()*allocAmount);

        }
    }
    
    // Method to normalize a value from one range to another
    public static int normalize(long x, int min_x, int max_x, int min_y, long max_y) {
        // Apply the normalization formula
        return (int) ((double) (x - min_x) / (max_x - min_x) * (max_y - min_y) + min_y);
    }


}
