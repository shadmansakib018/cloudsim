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

	private static DecimalFormat dft = new DecimalFormat("###.##");
	private static final String DESKTOP_PATH = System.getProperty("user.home") + "/Desktop/CloudSimCSVs/csv/";
	private static final String FILE_EXTENSION = ".csv";
    
    public static void writeCloudletDataToCsv(List<Cloudlet> list, List<? extends GuestEntity> vmlist, String lb) {
    	Map<Integer, Integer> guestIdCountMap = new HashMap<>();
    	double totalResponseTime = 0;
//    	double totalWaitingTime = 0;
//    	double totalExecTime = 0;
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        
    	String FILE_PREFIX = lb+"_";

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = FILE_PREFIX + timestamp + FILE_EXTENSION; 
//        
      try {
            File file = new File(DESKTOP_PATH + filename);
            
           fileWriter = new FileWriter(file);
           bufferedWriter = new BufferedWriter(fileWriter);
            
           String header = "Cloudlet ID,Task Length,VM ID,Submit Time,Start Time,Processing Time,Finish Time";
           bufferedWriter.write(header);
           bufferedWriter.newLine();
            
            for (Cloudlet cloudlet : list) {
                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                	guestIdCountMap.put(cloudlet.getGuestId(), guestIdCountMap.getOrDefault(cloudlet.getGuestId(), 0) + 1);
                	totalResponseTime = totalResponseTime + 
                					(cloudlet.getActualCPUTime() + (cloudlet.getExecStartTime()- cloudlet.getSubmissionTimeTwo()));
                	                	
                    StringBuilder row = new StringBuilder();

                    row.append(cloudlet.getCloudletId())
//                    .append(",").append("SUCCESS")
                    .append(",").append(cloudlet.getCloudletLength())
//                    .append(",").append(cloudlet.getResourceId())
                    .append(",").append(cloudlet.getGuestId())
//                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getRam())
//                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getSize())
//                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getBw())
//                    .append(",").append(vmlist.get(cloudlet.getGuestId()).getMips())
                    .append(",").append(dft.format(cloudlet.getSubmissionTimeTwo()))
                    .append(",").append(dft.format(cloudlet.getExecStartTime()))
                    .append(",").append(dft.format(cloudlet.getActualCPUTime()))
                    .append(",").append(dft.format(cloudlet.getExecFinishTime()));
                	
                   bufferedWriter.write(row.toString());
                    bufferedWriter.newLine();
                }
            }
            
//            for (Map.Entry<Integer, Integer> entry : guestIdCountMap.entrySet()) {
//                System.out.println("VM ID: " + entry.getKey() + " ==> " + entry.getValue() + " Tasks");
//            	System.out.println(entry.getKey());
//            	System.out.println(entry.getValue());
//            }
            
            
            System.out.println("Average Response Time: " + dft.format(totalResponseTime /(list.size())));
//            System.out.println("Average Waiting Time: " + dft.format(totalWaitingTime /(list.size())));
//            System.out.println("Average Execution Time: " + dft.format(totalExecTime /(list.size())));
//            System.out.println("CSV file created successfully at: " + DESKTOP_PATH + filename);
//            OpenFileExample(DESKTOP_PATH + filename);

        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
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
	        File file = new File(pathToFile);

	        if (Desktop.isDesktopSupported()) {
	            Desktop desktop = Desktop.getDesktop();
	            try {
	                desktop.open(file);
	            } catch (IOException e) {
	                System.err.println("Error opening the file: " + e.getMessage());
	            }
	        } else {
	            System.err.println("Desktop is not supported on this platform.");
	        }
	    
	}
	
	
	public static void writeResultsDataToCsv(
			List<Double> AvgResponseTimeList,
//			List<Double> AvgWaitingTimeList,
//			List<Double> AvgExecutionTimeList,
			List<Double> DcRunCostList,
//			List<Double> DcSetupCostList,
			List<Double> AvgDcProcessingTime,
			String lb
			) {
    	
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        
    	String FILE_PREFIX = lb+"_AllRuns"+"_"; 
    	String timestamp = new SimpleDateFormat("HHmmss").format(new Date());
        String filename = FILE_PREFIX + timestamp + FILE_EXTENSION; 
        
        try {
            
            File file = new File(DESKTOP_PATH + filename);
            
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            
            String header = "Avgerage Response Time, Average Waiting Time, Average Execution Time, VM Cost, Dc setup Cost, Avg DC Processing Time";
            
            bufferedWriter.write(header);
            bufferedWriter.newLine();  // Move to the next line
            
            for (int i = 0; i < AvgResponseTimeList.size(); i++) {
            	StringBuilder row = new StringBuilder();
                row.append(dft.format(AvgResponseTimeList.get(i)))
                .append(",").append("-")
                .append(",").append("-")
                .append(",").append(dft.format(DcRunCostList.get(i)))
                .append(",").append("-")
                .append(",").append(dft.format(AvgDcProcessingTime.get(i)));
                
                bufferedWriter.write(row.toString());
                bufferedWriter.newLine();
                
            }
            
//            System.out.println("CSV file created successfully at: " + DESKTOP_PATH + filename);
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
	
	
	public static void writeResultsRL(List<Double> AvgResponseTimeList,String lb, String name) {
//		String timestamp = new SimpleDateFormat("MMdd_HHmm").format(new Date());
		String folderName = System.getProperty("user.home") + "/Desktop/CloudSimCSVs/RL/EXP1";
		File folder = new File(folderName);
		
		// Create the folder if it doesn't exist
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
//        String timestamp2 = new SimpleDateFormat("mmss").format(new Date());
//        File file = new File(folder, lb+ "_" + timestamp2 + ".txt");
        File file = new File(folder, name + ".txt");

        double totalAvgResponseTimes = 0.0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Double value : AvgResponseTimeList) {
            	totalAvgResponseTimes = totalAvgResponseTimes + value;
                writer.write(String.format("%.3f", value));
                writer.newLine();
            }
            writer.write(String.format("%.3f", totalAvgResponseTimes/AvgResponseTimeList.size()));
            writer.newLine();
            System.out.println("File written to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write file: " + e.getMessage());
        }
        
        
    }
}
