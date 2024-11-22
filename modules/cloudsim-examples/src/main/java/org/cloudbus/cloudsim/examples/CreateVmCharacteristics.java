package org.cloudbus.cloudsim.examples;
import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;

import org.cloudbus.cloudsim.Vm;


public class CreateVmCharacteristics {
	
	int currentVmId = 0;

	// Method to create VMs with predefined characteristics
    public List<Vm> createVmsVersionOne(int numberOfVms, int brokerId) {
        int mips = 500;
        long size = 10000; // Image size (MB)
        int ram = 1024;    // VM memory (MB)
        long bw = 1000;    // Bandwidth (MB/s)
        int pesNumber = 1; // Number of CPUs
        String vmm = "Xen"; // VMM name
        
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            Vm vm = new Vm(currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
            currentVmId+=1;
        }

        return vmList;
    }
    
    public List<Vm> createVmsVersionTwo(int numberOfVms, int brokerId) {
        int mips = 1000;
        long size = 20000; // Image size (MB)
        int ram = 2048;    // VM memory (MB)
        long bw = 2000;    // Bandwidth (MB/s)
        int pesNumber = 2; // Number of CPUs
        String vmm = "Xen"; // VMM name
        
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            Vm vm = new Vm(currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
            currentVmId+=1;
        }

        return vmList;
    }
    
    public List<Vm> createVmsVersionThree(int numberOfVms, int brokerId) {
        int mips = 3000;
        long size = 40000;// Image size (MB)
        int ram = 4096;    // VM memory (MB)
        long bw = 3000;    // Bandwidth (MB/s)
        int pesNumber = 4; // Number of CPUs
        String vmm = "Xen"; // VMM name
        
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            Vm vm = new Vm(currentVmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
            currentVmId+=1;
        }

        return vmList;
    }

}
