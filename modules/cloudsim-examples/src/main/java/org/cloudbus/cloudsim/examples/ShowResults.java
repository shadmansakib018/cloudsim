package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;

public class ShowResults {

	private static final String DESKTOP_PATH = System.getProperty("user.home") + "/Desktop/CloudSimCSVs/100/"; // Desktop path
	private static final String FILE_EXTENSION = ".csv";  // File extension
    private static DecimalFormat dft = new DecimalFormat("###.##");
    
    public static void writeCloudletDataToCsv(List<Cloudlet> list, List<? extends GuestEntity> vmlist, String lb) {
    	Map<Integer, Integer> guestIdCountMap = new HashMap<>();
    	double totalResponseTime = 0;
    	double totalWaitingTime = 0;
    	double totalExecTime = 0;
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        
    	String FILE_PREFIX = lb+"_";  // File prefix (name)

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = FILE_PREFIX + timestamp + FILE_EXTENSION; 
        
        try {
            // Create a File object for the CSV file on the Desktop
            File file = new File(DESKTOP_PATH + filename);
            
            // Create FileWriter and BufferedWriter to write to the file
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            
//            bufferedWriter.write("===============================================");
//            bufferedWriter.newLine();
//            bufferedWriter.write(lb);
//            bufferedWriter.newLine();
//            bufferedWriter.write("===============================================");
//            bufferedWriter.newLine();
//            bufferedWriter.newLine();  // Add an extra line to separate from the CSV header
            
            // Write the header to the CSV file
//            String indent = "    ";
            String header = "Cloudlet ID,STATUS,Task Length,Datacenter ID,VM ID,RAM,Storage,Bandwidth,MIPS,Processing Time,Start Time,Finish Time, Submission Time";
            bufferedWriter.write(header);
            bufferedWriter.newLine();  // Move to the next line
            
            // Loop through the list of Cloudlets and write each one
            for (Cloudlet cloudlet : list) {
                // Assuming cloudlet.getStatus(), cloudlet.getCloudletId(), cloudlet.getGuestId(), etc. work as described
                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                	guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);
                	totalWaitingTime = totalWaitingTime + (cloudlet.getExecStartTime()- cloudlet.getSubmissionTimeTwo());
                	totalResponseTime += (cloudlet.getActualCPUTime() + (cloudlet.getExecStartTime()- cloudlet.getSubmissionTimeTwo()));
                	
                	totalExecTime += cloudlet.getActualCPUTime();
                    StringBuilder row = new StringBuilder();

                    // Construct each row by appending the values with the same indent format
                    row.append(cloudlet.getCloudletId())
                    .append(",").append("SUCCESS")
                    .append(",").append(cloudlet.getCloudletLength())
                    .append(",").append(cloudlet.getResourceId())
                    .append(",").append(cloudlet.getGuestId())
                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getRam())
                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getSize())
                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getBw())
                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getMips())
                    .append(",").append(dft.format(cloudlet.getActualCPUTime()))
                    .append(",").append(dft.format(cloudlet.getExecStartTime()))
                    .append(",").append(dft.format(cloudlet.getExecFinishTime()))
                    .append(",").append(dft.format(cloudlet.getSubmissionTimeTwo()));
                    // Write the row to the CSV
                    bufferedWriter.write(row.toString());
                    bufferedWriter.newLine();  // Move to the next line
                }
            }
            for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
                System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
            }
            System.out.println("Average Response Time: " + totalResponseTime /(list.size()));
            System.out.println("Average Waiting Time: " + totalWaitingTime /(list.size()));
            System.out.println("Average Execution Time: " + totalExecTime /(list.size()));
            System.out.println("CSV file created successfully at: " + DESKTOP_PATH + filename);
//            OpenFileExample(DESKTOP_PATH + filename);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	
	static void printCloudletList(List<Cloudlet> list, List<? extends GuestEntity> vmlist) {
		Cloudlet cloudlet;
		Map<Integer, Integer> guestIdCountMap = new HashMap<>();

		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID"
					+ indent + indent + "STATUS"
					+ indent + indent + "Task Length"
					+ indent + indent + "Datacenter ID"
					+ indent + indent + indent + "VM ID"
					+ indent + indent + "RAM"
					+ indent + indent + "Storage"
					+ indent + indent + "Bandwidth"
					+ indent + indent + "MIPS"
					+ indent + indent + "Processing Time"
					+ indent + indent + "Start Time"
					+ indent + indent + "Finish Time"
					+ indent + indent + "Submission Time");
		
		Log.println("");

		DecimalFormat dft = new DecimalFormat("###.##");
		int counter = 0;
		for (Cloudlet value : list) {
//			if (counter >= 40) {
//		        break;  // Exit the loop if 20 iterations are reached
//		    }
			counter++;
			cloudlet = value;
			guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {

				Log.println(
						indent + cloudlet.getCloudletId()
						+ indent + indent + indent + "SUCCESS"
						+ indent + indent + indent + cloudlet.getCloudletLength() 
						+ indent + indent + indent +indent + cloudlet.getResourceId()
						+ indent + indent + indent + indent + indent +cloudlet.getGuestId()
						+ indent + indent + indent + vmlist.get(cloudlet.getGuestId()).getRam()
						+ indent + indent +  vmlist.get(cloudlet.getGuestId()).getSize()
						+ indent + indent + indent +  vmlist.get(cloudlet.getGuestId()).getBw()
						+ indent + indent + indent + vmlist.get(cloudlet.getGuestId()).getMips()
						+ indent + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + indent  + indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent +  dft.format(cloudlet.getExecFinishTime())
				+ indent + indent + indent +  dft.format(cloudlet.getSubmissionTimeTwo()));
				
			}
		}
		
		for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
            System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
        }

	}
	
	public static void OpenFileExample(String pathToFile) {
	        // Specify the path to your file
	        File file = new File(pathToFile);

	        // Check if Desktop is supported on the current platform
	        if (Desktop.isDesktopSupported()) {
	            Desktop desktop = Desktop.getDesktop();
	            try {
	                // Open the file with the default program associated with the file type
	                desktop.open(file);
	            } catch (IOException e) {
	                System.err.println("Error opening the file: " + e.getMessage());
	            }
	        } else {
	            System.err.println("Desktop is not supported on this platform.");
	        }
	    
	}
	
}
