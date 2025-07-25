package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.core.CustomVm;
import org.cloudbus.cloudsim.core.GuestEntity;

public class DynamicLBV2 extends VmLoadBalancer {
//    private Map<Integer, VirtualMachineState> vmStatesList;
    private List<? extends GuestEntity> vmList;
    private List<CustomVm> customVmList;
    private boolean once = true;
    private int originalMin = 0;    
    private int originalMax = 0;    
    private int targetMin = 50;         
    private boolean useProportion = true;
    private double allocAmount = 0.5;

    public DynamicLBV2(DatacenterBroker dcb) {
        setName("DynamicVmLoadBalancer");
        this.vmList = dcb.getGuestsCreatedList(); 
        this.customVmList = new ArrayList<>();
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
//        System.out.println("cl length ===>" + cl.getCloudletLength());
        int bestVmId = -1;
        double bestScore = -1;
//        int VmCap = 4;
//        if(customVmList.size() <= 40 && Constants.totalBatches >= 200) {
//        	VmCap = 3;
//        }

        for (CustomVm vm : customVmList) {
//        	System.out.println(vm.getId() + "==> " + vmAllocationCounts.get(vm.getId()));
        	if (vmAllocationCounts.containsKey(vm.getId()) && vmAllocationCounts.get(vm.getId()) >= 4) {
        		continue;
        	}
            double score = getCurrentScore(vm, cl);
            if (score > bestScore) {
                bestScore = score;
                bestVmId = vm.getId();
            }
        }
//        System.out.println("****");
        if (bestVmId != -1) {
//        	System.out.println("[ALLOCATED] best Vm chosen is vm # " + bestVmId);
            allocateResourcesToVm(bestVmId, cl);
        }else {
//        	System.out.println("[NOT ENOUGH RESOURCES], Bestscore: " + bestScore + " bestVmId: " + bestVmId + " cl length " + cl.getCloudletLength());
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

//        if (useProportion) {
//        	long cloudletLength = cl.getCloudletLength();
//        	double alpha = 1.0;
//        	if (cloudletLength >= Constants.VideoMipsLowerBound && cloudletLength <= Constants.VideoMipsUpperBound) {
//        	    originalMin = Constants.VideoMipsLowerBound;
//        	    originalMax = Constants.VideoMipsUpperBound;
//        	    
//        	} else if (cloudletLength >= Constants.ImageMipsLowerBound && cloudletLength <= Constants.ImageMipsUpperBound) {
//        	    originalMin = Constants.ImageMipsLowerBound;
//        	    originalMax = Constants.ImageMipsUpperBound;
//        	    alpha = 0.8;
//        	    
//        	} else if (cloudletLength >= Constants.TextMipsLowerBound && cloudletLength <= Constants.TextMipsUpperBound) {
//        	    originalMin = Constants.TextMipsLowerBound;
//        	    originalMax = Constants.TextMipsUpperBound;
//        	    alpha = 0.5;
//        	    
//        	} else {
//        	    System.out.println("Cloudlet length is outside of defined ranges.");
//        	    originalMin = 0;
//        	    originalMax = 0;
//        	}
//            reqMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getMips()) * alpha;
//            reqRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getRam()) * alpha;
//            reqBW = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getBw()) * alpha;
//        }
        if (availableMips <= reqMIPS || availableRam <= reqRAM || availableBw <= reqBW) {
            return -1; 
        }

        return availableMips + availableRam + availableBw;
    }

    private void allocateResourcesToVm(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);
        double alpha = 1.0;
        
        if (selectedVm != null) {         
            if (useProportion) {
                long cloudletLength = cl.getCloudletLength();
            	if (cloudletLength >= Constants.VideoMipsLowerBound && cloudletLength <= Constants.VideoMipsUpperBound) {
            	    originalMin = Constants.VideoMipsLowerBound;
            	    originalMax = Constants.VideoMipsUpperBound;
            	    
            	} else if (cloudletLength >= Constants.ImageMipsLowerBound && cloudletLength <= Constants.ImageMipsUpperBound) {
            	    originalMin = Constants.ImageMipsLowerBound;
            	    originalMax = Constants.ImageMipsUpperBound;
            	    alpha = 0.8;
            	    
            	} else if (cloudletLength >= Constants.TextMipsLowerBound && cloudletLength <= Constants.TextMipsUpperBound) {
            	    originalMin = Constants.TextMipsLowerBound;
            	    originalMax = Constants.TextMipsUpperBound;
            	    alpha = 0.5;
            	    
            	} else {
            	    System.out.println("Cloudlet length is outside of defined ranges.");
            	    originalMin = 0;
            	    originalMax = 0;
            	}
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getMips());
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());
                
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + (normalizedMIPS*alpha));
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + (normalizedRAM*alpha));
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + (normalizedBW*alpha));
            } else {
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + selectedVm.getMips() * allocAmount);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + selectedVm.getRam() * allocAmount);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + selectedVm.getBw() * allocAmount);
            }
            allocateTask(vmId);
        }
    }

    public void releaseResources(int vmId, Cloudlet cl) {
        CustomVm selectedVm = customVmList.get(vmId);
        double alpha = 1.0;
        
        if (selectedVm != null) {        
            if (useProportion) {
                long cloudletLength = cl.getCloudletLength();
            	if (cloudletLength >= Constants.VideoMipsLowerBound && cloudletLength <= Constants.VideoMipsUpperBound) {
            	    originalMin = Constants.VideoMipsLowerBound;
            	    originalMax = Constants.VideoMipsUpperBound;
            	    
            	} else if (cloudletLength >= Constants.ImageMipsLowerBound && cloudletLength <= Constants.ImageMipsUpperBound) {
            	    originalMin = Constants.ImageMipsLowerBound;
            	    originalMax = Constants.ImageMipsUpperBound;
            	    alpha = 0.8;
            	    
            	} else if (cloudletLength >= Constants.TextMipsLowerBound && cloudletLength <= Constants.TextMipsUpperBound) {
            	    originalMin = Constants.TextMipsLowerBound;
            	    originalMax = Constants.TextMipsUpperBound;
            	    alpha = 0.5;
            	    
            	} else {
            	    System.out.println("Cloudlet length is outside of defined ranges.");
            	    originalMin = 0;
            	    originalMax = 0;
            	}
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getMips());
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getRam());
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, selectedVm.getBw());

                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - (normalizedMIPS*alpha));
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - (normalizedRAM*alpha));
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - (normalizedBW*alpha));
            } else {
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - selectedVm.getMips() * allocAmount);
                selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - selectedVm.getRam() * allocAmount);
                selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - selectedVm.getBw() * allocAmount);
            }
            finishTask(vmId);
        }
    }

    public static double normalize(long x, int min_x, int max_x, int min_y, double max_y) {
        return ((double)(x - min_x) / (max_x - min_x) * (max_y - min_y) + min_y);
    }

	@Override
	public void sendLongTermReward(double avgRT) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callTrain() {
		// TODO Auto-generated method stub
		
	}
} 
