/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.predicates.Predicate;

/**
 * This class represents a simulation entity. An entity handles events and can send events to other
 * entities. When this class is extended, there are a few methods that need to be implemented:
 * <ul>
 * <li> {@link #startEntity()} is invoked by the {@link CloudSim} class when the simulation is
 * started. This method should be responsible for starting the entity up.
 * <li> {@link #processEvent(SimEvent)} is invoked by the {@link CloudSim} class whenever there is
 * an event in the deferred queue, which needs to be processed by the entity.
 * <li> {@link #shutdownEntity()} is invoked by the {@link CloudSim} before the simulation
 * finishes. If you want to save data in log files this is the method in which the corresponding
 * code would be placed.
 * </ul>
 * 
 * //TODO the list above is redundant once all mentioned methods are abstract.
 * The documentation duplication may lead to have some of them
 * out-of-date and future confusion.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public abstract class SimEntity implements Cloneable {

	/** The entity name. */
	private String name;

	/** The entity id. */
	private int id;

	/** The buffer for selected incoming events. */
	private SimEvent evbuf;

	/** The entity's current state. */
	private int state;

	/**
	 * Creates a new entity.
	 * 
	 * @param name the name to be associated with the entity
	 */
	public SimEntity(String name) {
		if (name.contains(" ")) {
			throw new IllegalArgumentException("Entity names can't contain spaces.");
		}
		this.name = name;
		id = -1;
		state = RUNNABLE;
		CloudSim.addEntity(this);
	}

	/**
	 * Gets the name of this entity.
	 * 
	 * @return The entity's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the unique id number assigned to this entity.
	 * 
	 * @return The id number
	 */
	public int getId() {
		return id;
	}

	// The schedule functions

	/**
	 * Sends an event to another entity by id number, with data. Note that the tag <code>9999</code>
	 * is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(int dest, double delay, CloudSimTags tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.send(id, dest, delay, tag, data);
	}

	/**
	 * Sends an event to another entity by id number and with <b>no</b> data. Note that the tag
	 * <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void schedule(int dest, double delay, CloudSimTags tag) {
		schedule(dest, delay, tag, null);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with data. Note that the
	 * tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(String dest, double delay, CloudSimTags tag, Object data) {
		schedule(CloudSim.getEntityId(dest), delay, tag, data);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with <b>no</b> data. Note
	 * that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void schedule(String dest, double delay, CloudSimTags tag) {
		schedule(dest, delay, tag, null);
	}

	/**
	 * Sends an event to another entity by id number, with data
         * but no delay. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(int dest, CloudSimTags tag, Object data) {
		schedule(dest, 0, tag, data);
	}

	/**
	 * Sends an event to another entity by id number and with <b>no</b> data
         * and no delay. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag event type.
	 */
	public void scheduleNow(int dest, CloudSimTags tag) {
		schedule(dest, 0, tag, null);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with data
         * but no delay. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(String dest, CloudSimTags tag, Object data) {
		schedule(CloudSim.getEntityId(dest), 0, tag, data);
	}

	/**
	 * Send an event to another entity through a port with a given name, with <b>no</b> data
         * and no delay. 
         * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag event type.
	 */
	public void scheduleNow(String dest, CloudSimTags tag) {
		schedule(dest, 0, tag, null);
	}

	/**
	 * Sends a high priority event to another entity by id number, with data. 
         * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(int dest, double delay, CloudSimTags tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.sendFirst(id, dest, delay, tag, data);
	}

	/**
	 * Sends a high priority event to another entity by id number and with <b>no</b> data. 
         * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void scheduleFirst(int dest, double delay, CloudSimTags tag) {
		scheduleFirst(dest, delay, tag, null);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with data.
	 * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(String dest, double delay, CloudSimTags tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dest), delay, tag, data);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void scheduleFirst(String dest, double delay, CloudSimTags tag) {
		scheduleFirst(dest, delay, tag, null);
	}

	/**
	 * Sends a high priority event to another entity by id number, with data
         * and no delay. 
         * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(int dest, CloudSimTags tag, Object data) {
		scheduleFirst(dest, 0, tag, data);
	}

	/**
	 * Sends a high priority event to another entity by id number and with <b>no</b> data
         * and no delay. 
         * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag event type.
	 */
	public void scheduleFirstNow(int dest, CloudSimTags tag) {
		scheduleFirst(dest, 0, tag, null);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with data
         * and no delay.
	 * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(String dest, CloudSimTags tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dest), 0, tag, data);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data and no delay. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag event type.
	 */
	public void scheduleFirstNow(String dest, CloudSimTags tag) {
		scheduleFirst(dest, 0, tag, null);
	}

	/**
	 * Sets the entity to be inactive for a time period.
	 * 
	 * @param delay the time period for which the entity will be inactive
	 */
	public void pause(double delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("Negative delay supplied.");
		}
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.pause(id, delay);
	}

	/**
	 * Counts how many events matching a predicate are waiting in the entity's deferred queue.
	 * 
	 * @param p The event selection predicate
	 * @return The count of matching events
	 */
	public int numEventsWaiting(Predicate p) {
		return CloudSim.waiting(id, p);
	}

	/**
	 * Counts how many events are waiting in the entity's deferred queue.
	 * 
	 * @return The count of events
	 */
	public int numEventsWaiting() {
		return CloudSim.waiting(id, CloudSim.SIM_ANY);
	}

	/**
	 * Extracts the first event matching a predicate waiting in the entity's deferred queue.
	 * 
	 * @param p The event selection predicate
	 * @return the simulation event
	 */
	public SimEvent selectEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}

		return CloudSim.select(id, p);
	}

	/**
	 * Cancels the first event matching a predicate waiting in the entity's future queue.
	 * 
	 * @param p The event selection predicate
	 * @return The number of events cancelled (0 or 1)
	 */
	public SimEvent cancelEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}

		return CloudSim.cancel(id, p);
	}

	/**
	 * Gets the first event matching a predicate from the deferred queue, or if none match, wait for
	 * a matching event to arrive.
	 * 
	 * @param p The predicate to match
	 * @return the simulation event
	 */
	public SimEvent getNextEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}
		if (numEventsWaiting(p) > 0) {
			return selectEvent(p);
		}
		return null;
	}

	/**
	 * Waits for an event matching a specific predicate. This method does not check the entity's
	 * deferred queue.
	 * 
	 * @param p The predicate to match
	 */
	public void waitForEvent(Predicate p) {
		if (!CloudSim.running()) {
			return;
		}

		CloudSim.wait(id, p);
		state = WAITING;
	}

	/**
	 * Gets the first event waiting in the entity's deferred queue, or if there are none, wait for an
	 * event to arrive.
	 * 
	 * @return the simulation event
	 */
	public SimEvent getNextEvent() {
		return getNextEvent(CloudSim.SIM_ANY);
	}

	/**
	 * This method is invoked by the {@link CloudSim} class when the simulation is started. 
	 * It should be responsible for starting the entity up.
	 */
	public void startEntity() {
//		Log.printlnConcat(CloudSim.clock(), " SimEntity line 395: ", getName(), " is starting...");
	}

	/**
	 * Processes events or services that are available for the entity.
	 * This method is invoked by the {@link CloudSim} class whenever there is an event in the
	 * deferred queue, which needs to be processed by the entity.
	 * 
	 * @param ev information about the event just happened
	 */
	public abstract void processEvent(SimEvent ev);

	/**
         * Shuts down the entity.
	 * This method is invoked by the {@link CloudSim} before the simulation finishes. If you want
	 * to save data in log files this is the method in which the corresponding code would be placed.
	 */
	public void shutdownEntity() {
//		Log.printlnConcat(CloudSim.clock(), ": ", getName(), " is shutting down...");
	}

        /**
         * The run loop to process events fired during the simulation.
         * The events that will be processed are defined
         * in the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * 
         * @see #processEvent(org.cloudbus.cloudsim.core.SimEvent) 
         */
	public void run() {
		SimEvent ev = evbuf != null ? evbuf : getNextEvent();

		while (ev != null) {
			processEvent(ev);
			if (state != RUNNABLE) {
				break;
			}

			ev = getNextEvent();
		}

		evbuf = null;
	}

	/**
	 * Gets a clone of the entity. This is used when independent replications have been specified as
	 * an output analysis method. Clones or backups of the entities are made in the beginning of the
	 * simulation in order to reset the entities for each subsequent replication. This method should
	 * not be called by the user.
	 * 
	 * @return A clone of the entity
	 * @throws CloneNotSupportedException when the entity doesn't support cloning
	 */
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		SimEntity copy = (SimEntity) super.clone();
		copy.setName(name);
		copy.setEventBuffer(null);
		return copy;
	}

	// Used to set a cloned entity's name
	/**
	 * Sets the name.
	 * 
	 * @param new_name the new name
	 */
	private void setName(String new_name) {
		name = new_name;
	}

	// --------------- PACKAGE LEVEL METHODS ------------------

	/**
	 * Gets the entity state.
	 * 
	 * @return the state
	 */
	protected int getState() {
		return state;
	}

	/**
	 * Gets the event buffer.
	 * 
	 * @return the event buffer
	 */
	protected SimEvent getEventBuffer() {
		return evbuf;
	}

	// The entity states
        ////TODO The states should be an enum.
	/** The Constant RUNNABLE. */
	public static final int RUNNABLE = 0;

	/** The Constant WAITING. */
	public static final int WAITING = 1;

	/** The Constant HOLDING. */
	public static final int HOLDING = 2;

	/** The Constant FINISHED. */
	public static final int FINISHED = 3;

	/**
	 * Sets the entity state.
	 * 
	 * @param state the new state
	 */
	protected void setState(int state) {
		this.state = state;
	}

	/**
	 * Sets the entity id.
	 * 
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the event buffer.
	 * 
	 * @param e the new event buffer
	 */
	protected void setEventBuffer(SimEvent e) {
		evbuf = e;
	}

	// --------------- EVENT / MESSAGE SEND WITH NETWORK DELAY METHODS ------------------

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(int entityId, double delay, CloudSimTags cloudSimTag, Object data) {
		if (entityId < 0) {
			return;
		}

		// if delay is -ve, then it doesn't make sense. So resets to 0.0
		if (delay < 0) {
			delay = 0;
		}

		if (Double.isInfinite(delay)) {
			throw new IllegalArgumentException("The specified delay is infinite value");
		}

		if (entityId < 0) {
			Log.printlnConcat(getName(), ".send(): Error - invalid entity id ", entityId);
			return;
		}

		int srcId = getId();
		if (entityId != srcId) {// only delay messages between different entities
			delay += getNetworkDelay(srcId, entityId);
		}

		schedule(entityId, delay, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param entityId the id number of the destination entity
	 * @param delay    how long from the current simulation time the event should be sent. If delay is
	 *                 a negative number, then it will be changed to 0
	 * @param tag      an user-defined number representing the type of an event/message
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(int entityId, double delay, CloudSimTags tag) {
		send(entityId, delay, tag, null);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param entityName the name of the destination entity
	 * @param delay      how long from the current simulation time the event should be sent. If delay is
	 *                   a negative number, then it will be changed to 0
	 * @param tag        an user-defined number representing the type of an event/message
	 * @param data       A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(String entityName, double delay, CloudSimTags tag, Object data) {
		send(CloudSim.getEntityId(entityName), delay, tag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param entityName the name of the destination entity
	 * @param delay      how long from the current simulation time the event should be sent. If delay is
	 *                   a negative number, then it will be changed to 0
	 * @param tag        an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(String entityName, double delay, CloudSimTags tag) {
		send(entityName, delay, tag, null);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param entityId the id number of the destination entity
	 * @param tag      an user-defined number representing the type of an event/message
	 * @param data     A reference to data to be sent with the event
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(int entityId, CloudSimTags tag, Object data) {
		send(entityId, 0, tag, data);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param entityId the id number of the destination entity
	 * @param tag      an user-defined number representing the type of an event/message
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(int entityId, CloudSimTags tag) {
		send(entityId, 0, tag, null);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param entityName the name of the destination entity
	 * @param tag        an user-defined number representing the type of an event/message
	 * @param data       A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(String entityName, CloudSimTags tag, Object data) {
		send(CloudSim.getEntityId(entityName), 0, tag, data);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param entityName the name of the destination entity
	 * @param tag        an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(String entityName, CloudSimTags tag) {
		send(entityName, 0, tag, null);
	}

	/**
	 * Gets the network delay associated to the sent of a message from a given source to a given
	 * destination.
	 * 
	 * @param src source of the message
	 * @param dst destination of the message
	 * @return delay to send a message from src to dst
	 * @pre src >= 0
	 * @pre dst >= 0
	 */
	private double getNetworkDelay(int src, int dst) {
		if (NetworkTopology.isNetworkEnabled()) {
			return NetworkTopology.getDelay(src, dst);
		}
		return 0.0;
	}

}
