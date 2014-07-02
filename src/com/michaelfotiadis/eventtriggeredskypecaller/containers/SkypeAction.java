package com.michaelfotiadis.eventtriggeredskypecaller.containers;

public enum SkypeAction {
	
	CALL("Call"),
	VIDEO_CALL("Video Call"),
	CHAT("Chat"),
	PROMPT_USER("Prompt User");
	
	private String text;
	SkypeAction(String description) {
		text = description;
	}
	
	public String getString() {
		return text;
	}
	

	
}
