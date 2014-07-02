package com.michaelfotiadis.eventtriggeredskypecaller.containers;


/**
 * Class for storing constants used throughout the app
 * 
 * @author Michael Fotiadis
 * 
 */
public class CustomConstants {
	
	public enum Broadcasts {
		BROADCAST_1("Brodacast_1");
		
		private String text;
		Broadcasts(String description) {
			text = description;
		}
		
		public String getString() {
			return text;
		}
		
	}; 
	
    public static final String EXTRA_PAYLOAD = "EXTRA_PAYLOAD";
    public static final String EXTRA_RESULT = "EXTRA_RESULT453";
    
    public static final int REQUEST_CODE_1 = 1;
    public static final int REQUEST_CODE_2 = 2;
    public static final int REQUEST_CODE_3 = 3;
    
	// Device identifier string
	public static final String BLUETOOTH_DEVICE = "Bluetooth LE Device";

	// Result for the BroadCast Receiver
	public static boolean SERVICE_RESULT; 


	// Action Identifier
	public static final String LE_SCAN_ACTION = "com.eratosthenes.eventtriggeredskypecaller.StartBlueToothScan";

	// Configuration File identifier
	public static final String CONFIG_FILENAME = "config.txt";
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	// Configuration File status boolean
	public static boolean IS_CONFIG_FILE_SET = false;

	public static String CLOSEST_DEVICE;
	
	public static final String DOCUMENT_HEADER = "SKYPE_ID, DEVICE_ID, ACTION";
}
