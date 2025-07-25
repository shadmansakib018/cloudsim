package org.cloudbus.cloudsim;

public final class Constants {

	public Constants() {
		// TODO Auto-generated constructor stub
	}
	
	 // Define constants
	public static final int VideoMipsLowerBound = 3000;    
	public static final int VideoMipsUpperBound = 10000;
	public static final double VideoPercentage = 0.3;
	
	public static final int ImageMipsLowerBound = 20;  
	public static final int ImageMipsUpperBound = 100;  
	public static final double ImagePercentage = 0.5;
	
	public static final int TextMipsLowerBound = 5;   
	public static final int TextMipsUpperBound = 10; 
	public static final double TextPercentage = 0.2;
		
	public static int epochs = 5;
	public static int totalBatches = 20;
	public static int batchSize = 700;
	public static long seed = 62L;
		
	public static final int numberOfVmsPerDC = 60;
	public static final int numberOfDcs = 8;
	
	public static String[] commandLineArgs;

}