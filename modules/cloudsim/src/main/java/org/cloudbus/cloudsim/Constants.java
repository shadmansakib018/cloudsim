package org.cloudbus.cloudsim;

public final class Constants {

	public Constants() {
		// TODO Auto-generated constructor stub
	}
	
	 // Define constants
	public static final int VideoMipsLowerBound = 100000;    
	public static final int VideoMipsUpperBound = 500000;
	public static final double VideoPercentage = 0.6;
	
	public static final int ImageMipsLowerBound = 500;  
	public static final int ImageMipsUpperBound = 3000;  
	public static final double ImagePercentage = 0.3;
	
	public static final int TextMipsLowerBound = 5;   
	public static final int TextMipsUpperBound = 50; 
	public static final double TextPercentage = 0.1;
	
	public static int totalBatches = 1;
	public static int batchSize = 20000;
	public static long seed = 88L;
		
	public static final int numberOfVmsPerDC = 60;
	public static final int numberOfDcs = 8;

}