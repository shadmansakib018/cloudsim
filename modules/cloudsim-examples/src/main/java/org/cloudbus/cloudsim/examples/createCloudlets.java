package org.cloudbus.cloudsim.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Constants;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class createCloudlets {
//    long seed = 111L; 
    Random random = new Random(Constants.seed);
    int currentTaskId = 0;
	
	//Cloudlet properties
	int fileSize = 300;
	int outputSize = 300;
    int pesNumber = 1;
	UtilizationModel utilizationModel = new UtilizationModelFull();
	
	 public List<Cloudlet> createTasks(int brokerId) {
	        List<Cloudlet> cloudletList = new ArrayList<>();

	        // Calculate the number of cloudlets per category
	        int videoCloudlets = (int) (Constants.batchSize * Constants.VideoPercentage);
	        int imageCloudlets = (int) (Constants.batchSize * Constants.ImagePercentage);
	        int textCloudlets = Constants.batchSize - videoCloudlets - imageCloudlets;

	        // Temporary lists for each type of cloudlet
	        List<Cloudlet> videoCloudletsList = new ArrayList<>();
	        List<Cloudlet> imageCloudletsList = new ArrayList<>();
	        List<Cloudlet> textCloudletsList = new ArrayList<>();

	        // Create Video Cloudlets
	        for (int i = 0; i < videoCloudlets; i++) {
	            int length = getRandomMips(Constants.VideoMipsLowerBound, Constants.VideoMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            videoCloudletsList.add(cloudlet);
	        }

	        // Create Image Cloudlets
	        for (int i = 0; i < imageCloudlets; i++) {
	            int length = getRandomMips(Constants.ImageMipsLowerBound, Constants.ImageMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            imageCloudletsList.add(cloudlet);
	        }

	        // Create Text Cloudlets
	        for (int i = 0; i < textCloudlets; i++) {
	            int length = getRandomMips(Constants.TextMipsLowerBound, Constants.TextMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            textCloudletsList.add(cloudlet);
	        }

	        // Combine all the cloudlets into one list
	        cloudletList.addAll(videoCloudletsList);
	        cloudletList.addAll(imageCloudletsList);
	        cloudletList.addAll(textCloudletsList);

	        // Shuffle the list to mix the cloudlets randomly
	        Collections.shuffle(cloudletList, random);

	        return cloudletList;
	    }

	    // Helper method to generate a random MIPS value within a given range
	    private int getRandomMips(int lowerBound, int upperBound) {
	        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
	    }
	    
	    public List<Cloudlet> createTasks2(int brokerId, int currentTaskNumber) {
	    	currentTaskId = currentTaskNumber;
	        List<Cloudlet> cloudletList = new ArrayList<>();

	        // Calculate the number of cloudlets per category
	        int videoCloudlets = (int) (Constants.batchSize * Constants.VideoPercentage);
	        int imageCloudlets = (int) (Constants.batchSize * Constants.ImagePercentage);
	        int textCloudlets = Constants.batchSize - videoCloudlets - imageCloudlets;

	        // Temporary lists for each type of cloudlet
	        List<Cloudlet> videoCloudletsList = new ArrayList<>();
	        List<Cloudlet> imageCloudletsList = new ArrayList<>();
	        List<Cloudlet> textCloudletsList = new ArrayList<>();

	        // Create Video Cloudlets
	        for (int i = 0; i < videoCloudlets; i++) {
	            int length = getRandomMips(Constants.VideoMipsLowerBound, Constants.VideoMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            videoCloudletsList.add(cloudlet);
	        }

	        // Create Image Cloudlets
	        for (int i = 0; i < imageCloudlets; i++) {
	            int length = getRandomMips(Constants.ImageMipsLowerBound, Constants.ImageMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            imageCloudletsList.add(cloudlet);
	        }

	        // Create Text Cloudlets
	        for (int i = 0; i < textCloudlets; i++) {
	            int length = getRandomMips(Constants.TextMipsLowerBound, Constants.TextMipsUpperBound);
	            Cloudlet cloudlet = new Cloudlet(currentTaskId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            textCloudletsList.add(cloudlet);
	        }

	        // Combine all the cloudlets into one list
	        cloudletList.addAll(videoCloudletsList);
	        cloudletList.addAll(imageCloudletsList);
	        cloudletList.addAll(textCloudletsList);

	        // Shuffle the list to mix the cloudlets randomly
	        Collections.shuffle(cloudletList, random);

	        return cloudletList;
	    }

	

}
