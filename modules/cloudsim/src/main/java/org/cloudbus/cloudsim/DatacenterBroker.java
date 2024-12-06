/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Iterator;

import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, submission of cloudlets to VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {
	
	public int queuedCount = 0;
	public Map<Integer, VirtualMachineState> vmStatesList;
	public List<Cloudlet> waitingQueue;
	public VmLoadBalancer loadBalancer;
	
	public int batch = 1;
	// Seed the Random object with a specific value (e.g., 12345)
    long seed = 111L;  // You can use any long value as a seed
    Random random = new Random(seed);

	/** The list of VMs submitted to be managed by the broker. */
	protected List<? extends GuestEntity> vmList;

	/** The list of VMs created by the broker. */
	protected List<? extends GuestEntity> vmsCreatedList;

	/** The list of cloudlet submitted to the broker. 
         * @see #submitCloudletList(java.util.List) 
         */
	protected List<? extends Cloudlet> cloudletList;

	/** The list of submitted cloudlets. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The list of received cloudlet. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The number of submitted cloudlets. */
	protected int cloudletsSubmitted;

	/** The number of requests to create VM. */
	protected int vmsRequested;

	/** The number of acknowledges (ACKs) sent in response to
         * VM creation requests. */
	protected int vmsAcks;

	/** The number of destroyed VMs. */
	protected int vmsDestroyed;

	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The list of datacenters where was requested to place VMs. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map, where each key is a VM id
         * and each value is the datacenter id whwere the VM is placed. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics map where each key
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/** Next guest to which send the cloudlet */
	private int guestIndex = 0;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by {@link SimEntity} class)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);
		System.out.println("DCB Thread ID: " + Thread.currentThread().getId());
		setGuestList(new ArrayList<>());
		setGuestsCreatedList(new ArrayList<>());
		setCloudletList(new ArrayList<>());
		setCloudletSubmittedList(new ArrayList<>());
		setCloudletReceivedList(new ArrayList<>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<>());
		setDatacenterRequestedIdsList(new ArrayList<>());
		setVmsToDatacentersMap(new HashMap<>());
		setDatacenterCharacteristicsList(new HashMap<>());
		
		vmStatesList = (new HashMap<Integer, VirtualMachineState>());
		waitingQueue = (new LinkedList<Cloudlet>());
		loadBalancer = new RoundRobinVmLoadBalancer(this);
		Scanner scanner = new Scanner(System.in);

        // Prompt the user for input
        System.out.println("Please select Load Balancing algorithm:");
        System.out.println("1. RoundRobin");
        System.out.println("2. Throttled");
        System.out.println("3. DynamicLB");

        // Read the user's input
//        int choice = scanner.nextInt();
        int choice =3;

        // Conditional logic based on user input
        switch (choice) {
            case 1:
                System.out.println("You selected RoundRobin.");
                loadBalancer = new RoundRobinVmLoadBalancer(this);
                break;
            case 2:
                System.out.println("You selected Throttled.");
                loadBalancer = new ThrottledVmLoadBalancer(this);
                break;
            case 3:
                System.out.println("You selected DynamicLB.");
                loadBalancer = new DynamicVmLoadBalancer(this);
                break;
            default:
                System.out.println("Invalid choice. Defaults to Round Robin");
                loadBalancer = new RoundRobinVmLoadBalancer(this);
        }

        // Close the scanner to prevent resource leaks
        scanner.close();
		
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitGuestList(List<? extends GuestEntity> list) {
		getGuestList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.bindCloudletToVm
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
         * 
         * //TODO The name of the method is confused with the {@link #submitCloudlets()},
         * that in fact submit cloudlets to VMs. The term "submit" is being used
         * ambiguously. The method {@link #submitCloudlets()} would be named "sendCloudletsToVMs"
         * The method {@link #submitGuestList(List)} may have
         * be checked too.
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
		// Add only the first 5 elements from the list
//	    int endIndex = Math.min(5, list.size()); // Ensure we don't exceed the size of the list
//	    getCloudletList().addAll(list.subList(0, endIndex));
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setGuestId(vmId);
	}

	@Override
	public void processEvent(SimEvent ev) {
		CloudSimTags tag = ev.getTag();
//		System.out.println("****************** "+tag+" ***************");
        // Resource characteristics request
        if (tag == CloudActionTags.RESOURCE_CHARACTERISTICS_REQUEST) {
            processResourceCharacteristicsRequest(ev);

            // Resource characteristics answer
        } else if (tag == CloudActionTags.RESOURCE_CHARACTERISTICS) {
            processResourceCharacteristics(ev);

            // VM Creation answer
        } else if (tag == CloudActionTags.VM_CREATE_ACK) {
            processVmCreateAck(ev);

            // A finished cloudlet returned
        } else if (tag == CloudActionTags.CLOUDLET_RETURN) {
            processCloudletReturn(ev);

            // if the simulation finishes
        } else if (tag == CloudActionTags.END_OF_SIMULATION) {
            shutdownEntity();

            // other unknown tags are processed by this method
        } else {
            processOtherEvent(ev);
        }
	}

	/**
	 * Process the return of a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			//TODO: Remo Andreoli: should I need this?
			// getDatacenterCharacteristicsList().clear();
			setDatacenterRequestedIdsList(new ArrayList<>());
			createVmsInDatacenter(getDatacenterIdsList().getFirst());
		}
	}

	/**
	 * Process a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<>());

		Log.printlnConcat(CloudSim.clock(), " DCBroker line:208 : ", getName(), ": Cloud Resource List received with ",
				getDatacenterIdsList().size(), " datacenter(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudActionTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreateAck(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		GuestEntity guest = VmList.getById(getGuestList(), vmId);

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getGuestsCreatedList().add(guest);
			Log.printlnConcat(CloudSim.clock(), " : ", getName(), ": ", guest.getClassName(), " #", vmId,
					" has been created in Datacenter #", datacenterId, ", ", guest.getHost().getClassName(), " #",
					guest.getHost().getId());
			vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
		} else {
			Log.printlnConcat(CloudSim.clock(), " : ", getName(), ": Creation of ", guest.getClassName(), " #", vmId,
					" failed in Datacenter #", datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getGuestsCreatedList().size() == getGuestList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (!getGuestsCreatedList().isEmpty()) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printlnConcat(CloudSim.clock(), ": ", getName(),
							": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		vmStatesList.put(cloudlet.getGuestId(), VirtualMachineState.AVAILABLE);
		if(loadBalancer.lbname == "DynamicVmLoadBalancer") {
			loadBalancer.releaseResources(cloudlet.getGuestId(), cloudlet);
		}
		getCloudletReceivedList().add(cloudlet);
		Log.printlnConcat(CloudSim.clock(), " : ", getName(), " : ", cloudlet.getClass().getSimpleName(), " #", cloudlet.getCloudletId(),
				" return received");
		Log.printlnConcat(getName(), ": The number of finished Cloudlets is:", getCloudletReceivedList().size());
		cloudletsSubmitted--;
		submitWaitingCloudlet();
		int randomNumberToSubmit = random.nextInt(46) + 45;
		if(getCloudletReceivedList().size() == randomNumberToSubmit && batch <= 3) {
			System.out.println("sending batch number: " + batch + " when this many c");
		List<Cloudlet> cloudletList = new ArrayList<>();
		int pesNumber = 1;
		int originalMin = 20000;
	    int originalMax = 50000;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int numCloudlets = 90;
        for (int i = 90*batch; i < numCloudlets+(90*batch); i++) {
        	int randomNumber = random.nextInt(originalMax - originalMin + 1) + originalMin;
            Cloudlet cloudletNew = new Cloudlet(i, randomNumber, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudletNew.setUserId(this.getId());
            cloudletList.add(cloudletNew);
        }
        this.cloudletList.clear();
        this.submitCloudletList(cloudletList);
        this.submitCloudlets();
        
        batch++;
		}
		
		if (getCloudletList().isEmpty() && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printlnConcat(CloudSim.clock(), " : ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (!getCloudletList().isEmpty() && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				// changed here
//				createVmsInDatacenter(0);
				System.out.println("edited line 295 in DCB");
				createVmsInDatacenter(getDatacenterIdsList().get(0)); // TODO: Remo Andreoli: why datacentedId = 0 ?? should iterate over all the datacenters
			}

		}
	}

	/**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
         * //TODO to ensure the method will be overridden, it should be defined
         * as abstract in a super class from where new brokers have to be extended.
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printlnConcat(getName(), ".processOtherEvent(): ", "Error - an event is null.");
			return;
		}

		Log.printlnConcat(getName(), ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the submitted virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen Datacenter
	 * @pre $none
	 * @post $none
         * @see #submitGuestList(List)
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		Log.printlnConcat("createVmsInDatacenter from DCB");
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		Log.printlnConcat("DCBroker line: 334; data center name ", datacenterName);
		for (GuestEntity vm : getGuestList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Trying to Create ", vm.getClassName(),
						" #", vm.getId(), " in ", datacenterName);
				sendNow(datacenterId, CloudActionTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
         * @see #submitCloudletList(java.util.List) 
	 */
	protected void submitCloudlets() {
		List<Cloudlet> successfullySubmitted = new ArrayList<>();

		for (Cloudlet cloudlet : getCloudletList()) {
			System.out.println("checking clooudlet submission for cloudlet id #" + cloudlet.getCloudletId());
			GuestEntity vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getGuestId() == -1) {
				int vmid = loadBalancer.getNextAvailableVm(cloudlet);
				if(vmid == -1) {
					// add cloudlet to waiting list
//					System.out.println("all vms are busy, send task to waiting queue");
					waitingQueue.add(cloudlet);	
					queuedCount++;
					continue;
				}else {
					vm = getGuestsCreatedList().get(vmid);
					vmStatesList.put(vmid, VirtualMachineState.BUSY);
				}	
			} else { // submit to the specific vm
				vm = VmList.getById(getGuestsCreatedList(), cloudlet.getGuestId());
				if (vm == null) { // vm was not created
					vm = VmList.getById(getGuestList(), cloudlet.getGuestId()); // check if exists in the submitted list

					if(!Log.isDisabled()) {
						if (vm != null) {
							Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
									cloudlet.getCloudletId(), ": bount "+vm.getClassName()+" #"+vm.getId()+" not available");
						} else {
							Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
									cloudlet.getCloudletId(), ": bount guest entity doesn't exist");
						}
					}
					continue;
				}
			}

			if (!Log.isDisabled()) {
				Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Sending ", cloudlet.getClass().getSimpleName(),
						" #", cloudlet.getCloudletId(), " to " + vm.getClassName() + " #", vm.getId());
			}
			
			cloudlet.setGuestId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudActionTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
//		for(Cloudlet cl: successfullySubmitted) {
//			System.out.println("remove List: " + cl.getCloudletId());
//		}
		getCloudletList().removeAll(successfullySubmitted);
	}
	
	
	/**
	 * Process waiting queue cloudlets when some cloudlet have been returned.
	 * 
	 * @pre $none
	 * @post $none
	 */
	
	private void submitWaitingCloudlet(){
		if(waitingQueue.size() > 0){
			Cloudlet cloudlet = waitingQueue.remove(0);
//			System.out.println("pop from waiting list: task ID:  " + cloudlet.getCloudletId());
			int vmId =  loadBalancer.getNextAvailableVm(cloudlet);
			if (vmId == -1){
				waitingQueue.add(cloudlet);
				queuedCount++;
			}else {
				List<Cloudlet> successfullySubmitted = new ArrayList<>();
				GuestEntity vm = getGuestsCreatedList().get(vmId);
				vmStatesList.put(vmId, VirtualMachineState.BUSY);
				if (!Log.isDisabled()) {
					Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Sending ", cloudlet.getClass().getSimpleName(),
							" #", cloudlet.getCloudletId(), " to " + vm.getClassName() + " #", vm.getId());
				}
				
				cloudlet.setGuestId(vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudActionTags.CLOUDLET_SUBMIT, cloudlet);
				cloudletsSubmitted++;
				getCloudletSubmittedList().add(cloudlet);
				successfullySubmitted.add(cloudlet);
				// remove submitted cloudlets from waiting list
//				for(Cloudlet cl: successfullySubmitted) {
//					System.out.println("remove List: " + cl.getCloudletId());
//				}
				getCloudletList().removeAll(successfullySubmitted);
			}
		}
		return;

	}

	/**
	 * Destroy all virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (GuestEntity vm : getGuestsCreatedList()) {
			Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Destroying ", vm.getClassName(), " #", vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudActionTags.VM_DESTROY, vm);
		}

//		getGuestsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudActionTags.END_OF_SIMULATION);
	}

	@Override
	public void startEntity() {
		super.startEntity();
		schedule(getId(), 0, CloudActionTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends GuestEntity> List<T> getGuestList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends GuestEntity> void setGuestList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends GuestEntity> List<T> getGuestsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends GuestEntity> void setGuestsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment the number of acknowledges (ACKs) sent in response
         * to requests of VM creation.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	public void getDataCenterCost() {
//		CloudSim.getEntityList().forEach(entinty -> System.out.println(entinty.getName()));
		
		for(int id : this.getDatacenterRequestedIdsList()) {// for each datacenter
			double totalHostCost = 0.0;
			DatacenterCharacteristics characteristics = this.datacenterCharacteristicsList.get(id);
			
			for (HostEntity host : characteristics.getHostList()) {				
	            double hostCost = 0.0;
	            hostCost += host.getRam() * characteristics.getCostPerMem();
	            hostCost += host.getStorage() * characteristics.getCostPerStorage(); 
	            hostCost += host.getBw() * characteristics.getCostPerBw();

	            totalHostCost += hostCost;
	        }
//			System.out.println("Total Cost to run datacenter #" + id + " $" + totalHostCost );
			System.out.println("Total Cost for datacenter #" + id + " $" + totalHostCost );
		}
	}
	
	public void getVmCost() {
//		CloudSim.getEntityList().forEach(entinty -> System.out.println(entinty.getName()));
		double totalHostCost = 0.0;
		for(GuestEntity vm : this.getGuestsCreatedList()) {
			
			DatacenterCharacteristics characteristics = 
		this.datacenterCharacteristicsList.get(this.getVmsToDatacentersMap().get(vm.getId()));
	            double hostCost = 0.0;
	            hostCost += vm.getRam() * characteristics.getCostPerMem();
	            hostCost += vm.getSize() * characteristics.getCostPerStorage(); 
	            hostCost += vm.getBw() * characteristics.getCostPerBw();
	            totalHostCost += hostCost;
			

		}
			System.out.println("Total Cost for " + this.getGuestsCreatedList().size() + " VMS $" + totalHostCost);
			
//			for(GuestEntity vm : this.getGuestsCreatedList()) {
//				System.out.println(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()));
//			}
	}
	
	

}
