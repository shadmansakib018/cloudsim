package org.cloudbus.cloudsim.core;

public class CustomVm {
    private int id;
    private int userId;
    private double mips; // Maximum MIPS capacity
    private int ram;     // Maximum RAM capacity
    private long bw;     // Maximum Bandwidth capacity
    private long size;   // Storage size capacity
    
    private double currentAllocatedMips; // Currently allocated MIPS
    private double currentAllocatedRam;     // Currently allocated RAM
    private double currentAllocatedBw;     // Currently allocated Bandwidth

    public CustomVm(int id, int userId, double mips, int ram, long bw, long size) {
        this.id = id;
        this.userId = userId;
        this.mips = mips;
        this.ram = ram;
        this.bw = bw;
        this.size = size;
        
        this.currentAllocatedMips = 0;
        this.currentAllocatedRam = 0;
        this.currentAllocatedBw = 0;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getMips() {
        return mips;
    }

    public int getRam() {
        return ram;
    }

    public long getBw() {
        return bw;
    }

    public long getSize() {
        return size;
    }
    
    // Getters for current allocated resources
    public double getCurrentAllocatedMips() {
        return currentAllocatedMips;
    }

    public double getCurrentAllocatedRam() {
        return currentAllocatedRam;
    }

    public double getCurrentAllocatedBw() {
        return currentAllocatedBw;
    }

    // Setters for current allocated resources
    public void setCurrentAllocatedMips(double currentAllocatedMips) {
        this.currentAllocatedMips = currentAllocatedMips;
    }

    public void setCurrentAllocatedRam(double currentAllocatedRam) {
        this.currentAllocatedRam = currentAllocatedRam;
    }

    public void setCurrentAllocatedBw(double currentAllocatedBw) {
        this.currentAllocatedBw = currentAllocatedBw;
    }

    @Override
    public String toString() {
        return "CustomVm{" +
                "id=" + id +
                ", userId=" + userId +
                ", mips=" + mips +
                ", ram=" + ram +
                ", bw=" + bw +
                ", size=" + size +
                '}';
    }
}
