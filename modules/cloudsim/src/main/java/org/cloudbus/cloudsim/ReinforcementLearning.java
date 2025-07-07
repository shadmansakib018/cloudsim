package org.cloudbus.cloudsim;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.cloudbus.cloudsim.core.CustomVm;
import org.cloudbus.cloudsim.core.GuestEntity;

public class ReinforcementLearning extends VmLoadBalancer  {
	  String webserver;
	  public static final int GlobalMipsLowerBound = 1;
	  public static final int GlobalMipsUpperBound = 10000;
	  private boolean once = true;
	  private int originalMin = 0;    
	  private int originalMax = 0;    
	  private int targetMin = 50; 
	  private List<? extends GuestEntity> vmList;
	  private List<CustomVm> customVmList;
	  private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();
  
  
	public ReinforcementLearning(DatacenterBroker dcb) {
      if (Constants.commandLineArgs != null) {
    	  webserver = "http://localhost:" + Constants.commandLineArgs[0];
      }else {
    	  webserver = "http://localhost:" + 5999;
      }
		setName("Reinforcement_Learning");
        this.vmList = dcb.getGuestsCreatedList(); 
        this.customVmList = new ArrayList<>();
	}
	
    public void createCustomVm(List<? extends GuestEntity> vmList) {
        for (GuestEntity vm : vmList) {
            CustomVm customVm = new CustomVm(
            		 	vm.getId(),
            	        vm.getUserId(),
            	        vm.getMips(),
            	        (double) vm.getRam(),
            	        (double) vm.getBw(), 
            	        (double) vm.getSize()
            );
            customVmList.add(customVm);
        }
    }
    
    
	@Override
	public int getNextAvailableVm(Cloudlet cl) {
		if (once) {
            createCustomVm(vmList);
            once = false;

        }
	    
	    double[] currentState = getVmStateVector(cl); 

	    int selectedVmId = getActionFromFlask(currentState);
	    try {
			Thread.sleep(18);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    if(vmAllocationCounts.getOrDefault(selectedVmId, 0) >= 5) {
//	    	System.out.println("[MORE THAN 5 TASKS] vmId: " + selectedVmId);
		    sendTrainingDataToFlask(currentState, selectedVmId, -1, currentState);
		    return -1;
	    }

	    boolean available = checkIfResourcesEnough(selectedVmId, cl);
	    if(!available) {
//	    	System.out.println("[NOT ENOUGH RESOURCES] vmId: " + selectedVmId);
		    sendTrainingDataToFlask(currentState, selectedVmId, -1, currentState);
	    	return -1;
	    }
//	    System.out.println("[CHOSEN VM] selected VM ID: "+selectedVmId + " Cloudlet ID# " + cl.getCloudletId() + " TASK LENGTH: " + cl.getCloudletLength());
	    cl.setCurrentState(currentState);
	    allocateResourcesToVm(selectedVmId, cl);
	    double[] newState = getVmStateVector(cl);
	    cl.setNewState(newState);
	    return selectedVmId;
	}
	
	public boolean checkIfResourcesEnough(int vmId, Cloudlet cl) {
		CustomVm vm = customVmList.get(vmId);
		
		double availableMips = vm.getMips() - vm.getCurrentAllocatedMips();
		double availableRam  = vm.getRam() - vm.getCurrentAllocatedRam();
		double availableBw   = vm.getBw() - vm.getCurrentAllocatedBw();

        
//        long cloudletLength = cl.getCloudletLength();
//        double alpha = 1.0;
//    	if (cloudletLength >= Constants.VideoMipsLowerBound && cloudletLength <= Constants.VideoMipsUpperBound) {
//    	    originalMin = Constants.VideoMipsLowerBound;
//    	    originalMax = Constants.VideoMipsUpperBound;
//    	    
//    	} else if (cloudletLength >= Constants.ImageMipsLowerBound && cloudletLength <= Constants.ImageMipsUpperBound) {
//    	    originalMin = Constants.ImageMipsLowerBound;
//    	    originalMax = Constants.ImageMipsUpperBound;
//    	    alpha = 0.6;
//    	    
//    	} else if (cloudletLength >= Constants.TextMipsLowerBound && cloudletLength <= Constants.TextMipsUpperBound) {
//    	    originalMin = Constants.TextMipsLowerBound;
//    	    originalMax = Constants.TextMipsUpperBound;
//    	    alpha = 0.2;
//    	    
//    	} else {
//    	    System.out.println("Cloudlet length is outside of defined ranges.");
//    	    originalMin = 0; 
//    	    originalMax = 0;
//    	}
//        double reqMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getMips()) * alpha;
//        double reqRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getRam()) * alpha;
//        double reqBW = normalize(cloudletLength, originalMin, originalMax, targetMin, vm.getBw()) * alpha;
//        System.out.println(cloudletLength);
//        System.out.println("available MIPS: " + availableMips + "==> required Mips: " + reqMIPS);
//        System.out.println("available Ram: " + availableRam + "==> required Ram: " + reqRAM);
//        System.out.println("available Bw: " + availableBw + "==> required BW: " + reqBW);

        if (availableMips <= 0 || availableRam <= 0 || availableBw <= 0) {
            return false; 
        }
		
		return true;
		
	}
	
	private double[] getVmStateVector(Cloudlet cl) {
	    double[] state = new double[21];
	    int i = 0;
	    for (CustomVm vm : customVmList) {
	    	
	    	state[i++] = normalization((vm.getMips() - vm.getCurrentAllocatedMips()), 1000);
//	    	state[i++] = normalization((vm.getRam() - vm.getCurrentAllocatedRam()), 2048);
//	    	state[i++] = normalization((vm.getBw() - vm.getCurrentAllocatedBw()), 2000);

	        int activeTasks = vmAllocationCounts.getOrDefault(vm.getId(), 0);
	        state[i++] = (double) activeTasks / 5;  
	    }
	    state[i++] = normalizeCloudletLength(cl.getCloudletLength());
	    return state;
	}
	
	
	private int getActionFromFlask(double[] state) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        Map<String, Object> requestBody = new HashMap<>();
	        requestBody.put("state", state);

	        String json = mapper.writeValueAsString(requestBody);

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(new URI(webserver + "/select_action"))
	                .header("Content-Type", "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString(json))
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        JsonNode res = mapper.readTree(response.body());
	        return res.get("action").asInt();

	    } catch (Exception e) {
//	        e.printStackTrace();
	    	System.out.println("ERROR IN GET ACTION " + webserver);
	        return new Random().nextInt(vmList.size());
	    }
	}
	
	 void sendTrainingDataToFlask(double[] state, int action, double reward, double[] nextState) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();

	        Map<String, Object> payload = new HashMap<>();
	        payload.put("state", state);
	        payload.put("action", action);
	        payload.put("reward", reward);
	        payload.put("next_state", nextState);

	        String json = mapper.writeValueAsString(payload);

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(new URI( webserver + "/store_states"))
	                .header("Content-Type", "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString(json))
	                .build();

	        client.send(request, HttpResponse.BodyHandlers.ofString());

	    } catch (Exception e) {
	    	System.out.println("ERROR IN SENDING DATA "+ webserver);
	    }
	}
	 
	 
	 public void callTrain() {
		 try {
		 HttpRequest request = HttpRequest.newBuilder()
				    .uri(URI.create(webserver + "/train_now"))
				    .header("Content-Type", "application/json")
				    .POST(HttpRequest.BodyPublishers.noBody())  // no payload
				    .build();

				client.send(request, HttpResponse.BodyHandlers.ofString());
		 }catch (Exception e) {
//		        e.printStackTrace();
			 System.out.println("ERROR IN TRAIN");
		    }
	 }

    
    private void allocateResourcesToVm(int vmId, Cloudlet cl) {
    	CustomVm selectedVm = customVmList.get(vmId);
    	double alpha = 1.0;

        if (selectedVm != null) {         
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
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, 1000*0.3);
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, 2048*0.3);
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, 2000*0.3);
//                System.out.println("allocated amount based on length: " + cl.getCloudletLength());
//                System.out.println(normalizedMIPS);
//                System.out.println(normalizedRAM);
//                System.out.println(normalizedBW);
                
                
                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() + (normalizedMIPS*alpha));

                	selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() + (normalizedRAM*alpha));

                	selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() + (normalizedBW*alpha));

            
            allocateTask(vmId);
        }
    }

    public void releaseResources(int vmId, Cloudlet cl) {
    	CustomVm selectedVm = customVmList.get(vmId);   
    	double alpha = 1.0;
        if (selectedVm != null) {        
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
                double normalizedMIPS = normalize(cloudletLength, originalMin, originalMax, targetMin, 1000*0.3);
                double normalizedRAM = normalize(cloudletLength, originalMin, originalMax, targetMin, 2048*0.3);
                double normalizedBW = normalize(cloudletLength, originalMin, originalMax, targetMin, 2000*0.3);

                selectedVm.setCurrentAllocatedMips(selectedVm.getCurrentAllocatedMips() - (normalizedMIPS*alpha));

                	selectedVm.setCurrentAllocatedRam(selectedVm.getCurrentAllocatedRam() - (normalizedRAM*alpha));

                	selectedVm.setCurrentAllocatedBw(selectedVm.getCurrentAllocatedBw() - (normalizedBW*alpha));

            
            finishTask(vmId);
        }
    }

    private double estimateReward(int vmId, double[] oldState, double[] newState) {
    	if(vmId == -1) {
    		return -11;
    	}
    	if(vmId == -2) {
    		return -22;
    	}
        int idx = vmId * 4;
        CustomVm selectedVm = customVmList.get(vmId);
        double configuredMips = selectedVm.getMips();

        double oldAvailMips = oldState[idx];
        double oldAvailRam = oldState[idx + 1];
        double oldAvailBw  = oldState[idx + 2];
//        double oldTasks    = oldState[idx + 3];

        double newAvailMips = newState[idx];
        double newAvailRam  = newState[idx + 1];
        double newAvailBw   = newState[idx + 2];
        double newTasks     = newState[idx + 3];

        // 1. Proportional penalty for active tasks (stronger)
        double activeTaskPenalty = -0.8 * newTasks * newTasks;  

        // 2. Resource usage reward (based on difference)
        double usedMips = oldAvailMips - newAvailMips;
        double usedRam  = oldAvailRam - newAvailRam;
        double usedBw   = oldAvailBw  - newAvailBw;
        double usageReward = 0.002 * (usedMips + usedRam + usedBw);

        // 3. Bonus for choosing a VM with a lot of resources left
        double residualBonus = 0;
        if (newAvailMips > 0.4 * configuredMips) residualBonus += 0.2;
        if (newAvailRam  > 0.4 * selectedVm.getRam()) residualBonus += 0.2;
        if (newAvailBw   > 0.4 * selectedVm.getBw()) residualBonus += 0.2;

        
        double configuredMipsBonus = 3 * (configuredMips / 1000.0);  


        double reward = usageReward + activeTaskPenalty + residualBonus + configuredMipsBonus;

//        System.out.println(reward + " Usage: " + usageReward + " activeTaksPenalty: " + activeTaskPenalty + " residualBonus: " + residualBonus + 
//        		" configuredMips: " + configuredMipsBonus);
        return reward;
    }


    
    public static double normalize(long x, int min_x, int max_x, int min_y, double max_y) {
        return ((double)(x - min_x) / (max_x - min_x) * (max_y - min_y) + min_y);
    }

	private double normalization(double value, double max) {
	    if (max == 0) return 0; // prevent divide-by-zero
	    return Math.max(0.0, Math.min(1.0, value / max));
	}
	
	public void sendLongTermReward(double avgResponseTime) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();

	        // Prepare the JSON payload
	        Map<String, Object> payload = new HashMap<>();
	        payload.put("avg_response_time", avgResponseTime);

	        String json = mapper.writeValueAsString(payload);

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(new URI(webserver + "/long_term_reward"))
	                .header("Content-Type", "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString(json))
	                .build();

	        // Use the static shared HttpClient
	        client.send(request, HttpResponse.BodyHandlers.ofString());
	    } catch (Exception e) {
//	        e.printStackTrace();
	    	System.out.println("ERROR IN GET LONG TERM REWARD");
	    }
	}
	
	public static double normalizeCloudletLength(long length) {
	    double minOut = 0.1;  // Minimum normalized value
	    double normalized;
	    double weight;

	    if (length >= Constants.VideoMipsLowerBound && length <= Constants.VideoMipsUpperBound) {
	        normalized = minOut + (1 - minOut) * 
	            (double)(length - Constants.VideoMipsLowerBound) /
	            (Constants.VideoMipsUpperBound - Constants.VideoMipsLowerBound);
	        weight = 3;

	    } else if (length >= Constants.ImageMipsLowerBound && length <= Constants.ImageMipsUpperBound) {
	        normalized = minOut + (1 - minOut) * 
	            (double)(length - Constants.ImageMipsLowerBound) /
	            (Constants.ImageMipsUpperBound - Constants.ImageMipsLowerBound);
	        weight = 2;

	    } else if (length >= Constants.TextMipsLowerBound && length <= Constants.TextMipsUpperBound) {
	        normalized = minOut + (1 - minOut) * 
	            (double)(length - Constants.TextMipsLowerBound) /
	            (Constants.TextMipsUpperBound - Constants.TextMipsLowerBound);
	        weight = 1;

	    } else {
	        normalized = minOut;
	        weight = 1.0; // Neutral weight fallback
	    }

	    double weightedScore = normalized * weight;
	    return Math.round(weightedScore * 100.0) / 100.0;  // round to 2 decimal places
	}
	
}
