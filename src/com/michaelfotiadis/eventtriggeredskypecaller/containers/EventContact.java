package com.michaelfotiadis.eventtriggeredskypecaller.containers;

import java.io.Serializable;

public class EventContact implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7239448677353188173L;
	
	private String contactName;
	private String contactDevice;
	private String contactAction;
	private String deviceID;
	
	public EventContact() {
		
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactDevice() {
		return contactDevice;
	}

	public void setContactDevice(String contactDevice) {
		this.contactDevice = contactDevice;
	}

	public String getContactAction() {
		return contactAction;
	}

	public void setContactAction(String contactAction) {
		this.contactAction = contactAction;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Contact Name : ");
		sb.append(this.contactName);
		sb.append(" Using Device : ");
		sb.append(this.contactDevice);
		sb.append(" Action : ");
		sb.append(this.contactAction);
		sb.append(" Device ID : ");
		sb.append(this.deviceID);
		
		return sb.toString();
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getFileWritableString() {
		StringBuilder contents = new StringBuilder();
		contents.append(this.contactName);
		contents.append(',');
		contents.append(this.contactDevice);
		contents.append(',');
		contents.append(this.deviceID);
		contents.append(',');
		contents.append(this.contactAction);
		
		return contents.toString();
	}
	
}
