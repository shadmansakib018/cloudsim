package org.cloudbus.cloudsim.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Constants;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class createCloudlets {
	// Seed the Random object with a specific value (e.g., 12345)
    long seed = 111L;  // You can use any long value as a seed
    Random random = new Random(seed);
    int currentTaskId = 0;
	
	//Cloudlet properties
	long length = 400;
	int fileSize = 300;
	int outputSize = 300;
//	int originalMin = 400;    // Lower bound of the range
//    int originalMax = 4000;    // Upper bound of the range
    int pesNumber = 1;
	UtilizationModel utilizationModel = new UtilizationModelFull();
	
	public List<Cloudlet> createTasks(int brokerId, double submissionTime){
		 List<Cloudlet> cloudletList =new ArrayList<>();
		 for(int i = 0; i < Constants.batchSize; i++) {
			 	int randomNumber = random.nextInt(Constants.originalMax - Constants.originalMin + 1) + Constants.originalMin;
	            Cloudlet cloudlet = new Cloudlet(currentTaskId, randomNumber, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            cloudletList.add(cloudlet);
	            currentTaskId++;
		 }
		 return cloudletList;
	}
	
	
	public List<Integer> getListFromTxtFile() {
	    List<Integer> numberList = new ArrayList<>();  // List to store the numbers
	    try {
	        String filePath = "C:\\Users\\ss4587s\\Desktop\\CloudSimCSVs\\Resources\\taskLength.txt";

	        // Try-with-resources ensures that the BufferedReader is closed automatically
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            // Read the entire line (since the numbers are space-separated on a single line)
	            String line = reader.readLine();

	            if (line != null) {
	                // Split the line by spaces and parse each number
	                String[] parts = line.split("\\s+");
	                
	                // Add each parsed number to the list
	                for (String part : parts) {
	                    try {
	                        numberList.add(Integer.parseInt(part.trim()));  // Add to list after parsing
	                    } catch (NumberFormatException e) {
	                        // If parsing fails, log the error (you can choose to handle this differently)
	                        System.err.println("Error parsing number: " + part);
	                    }
	                }
	            }
	        }

	    } catch (IOException e) {
	        // Handle file read errors
	        System.err.println("Error reading the file: " + e.getMessage());
	        e.printStackTrace();  // Optional: to print stack trace for debugging
	    }

	    return numberList;  // Return the List directly
	}

}
