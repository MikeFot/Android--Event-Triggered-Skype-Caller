package com.michaelfotiadis.eventtriggeredskypecaller.containers;

public enum DeviceType {

	NFC("NFC"),
	IBEACON("IBEACON");
	
	private String text;
	DeviceType(String description) {
		text = description;
	}
	
	public String getString() {
		return text;
	}
	
}
