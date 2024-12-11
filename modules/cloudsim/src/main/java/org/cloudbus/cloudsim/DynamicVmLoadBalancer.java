package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CustomVm;
import org.cloudbus.cloudsim.core.GuestEntity;

public class DynamicVmLoadBalancer extends VmLoadBalancer {
    private Map<Integer, VirtualMachineState> vmStatesList;
    private List<? extends GuestEntity> vmList;
    private List<CustomVm> customVmList;
    private boolean once = true;
    private int originalMin = Constants.originalMin;    
    private int originalMax = Constants.originalMax;    
    private int targetMin = 1;         
    private boolean useProportion = true;
    private double allocAmount = 0.5;

    public DynamicVmLoadBalancer(DatacenterBroker dcb) {
        setName("DynamicVmLoadBalancer");
        this.vmList = dcb.getGuestsCreatedList(); 
        this.customVmList = new ArrayList<>();
        System.out.println(originalMin + " " + originalMax);
    }

    public void createCustomVm(List<? extends GuestEntity> vmList) {
        for (GuestEntity vm : vmList) {
            CustomVm customVm = new CustomVm(
            		 vm.getId(),
            	        vm.getUserId(),
            	        vm.getMips(),
            	        (double) vm.getRam(),      // Convert to double
            	        (double) vm.getBw(),       // Convert to double
            	        (double) vm.getSize()      // Convert to double
            );
            customVmList.add(customVm);
        }
    }

    public int getNextAvailableVm(Cloudlet cl) {
        if (once) {
            createCustomVm(vmList);
            once = false;
        }
        int bestVmId = -1;
        double bestScore = -1;

        for (CustomVm vm : customVmList) {
            double score = getCurrentScore(vm, cl);
            if (score > bestScore) {
                bestScore = score;
                bestVmId = vm.getId();
            }
        }
        if (bestVmId != -1) {
            allocateResourcesToVm(bestVmId, cl);
        }
        return bestVmId;
    }

    private double getCurrentScore(CustomVm vm, Cloudlet cl) {
        double reqMIPS = 0;
        double reqRAM = 0;
        double reqBW = 0;

        double availableMips = vm.getMips() - vm.getCurrentAllocatedMips();
        double availableRam = vm.getRam() - vm.getCurrentAllocatedRam();
        double availableBw = vm.getBw() - vm.getCurrentAllocatedBw();

        if (useProportion) {
            long cloudletLength = cl.getCloudletLength();
            reqMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getMips());
            reqRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getRam());
            reqBW = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getBw());
        }

        if (availableMips < reqMIPS || availableRam < reqRAM || availableBw < reqBW) {
            return -1; 
        }

        return availableMips + availableRam + availableBw;
    }

    private void allocateResourcesToVm(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);

        if (selectedVm != null) {         
            if (useProportion) {
                long cloudletLength = cl.getCloudletLength();
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getMips());
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());
                
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + normalizedMIPS);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + normalizedRAM);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + normalizedBW);
            } else {
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + selectedVm.getMips() * allocAmount);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + selectedVm.getRam() * allocAmount);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + selectedVm.getBw() * allocAmount);
            }
        }
    }

    public void releaseResources(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);        
        if (selectedVm != null) {        
            if (useProportion) {
                long cloudletLength = cl.getCloudletLength();
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getMips());
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());

                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - normalizedMIPS);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - normalizedRAM);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - normalizedBW);
            } else {
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - selectedVm.getMips() * allocAmount);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - selectedVm.getRam() * allocAmount);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - selectedVm.getBw() * allocAmount);
            }
        }
    }

    public static double normalize(long x, int min_x, int max_x, int min_y, double max_y) {
        return ((double)(x - min_x) / (max_x - min_x) * (max_y - min_y) + min_y);
    }
} 
