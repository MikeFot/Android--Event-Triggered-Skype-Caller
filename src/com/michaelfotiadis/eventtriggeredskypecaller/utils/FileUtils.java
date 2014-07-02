package com.michaelfotiadis.eventtriggeredskypecaller.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;

import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.EventContact;

public class FileUtils {

	private final String TAG = "FILE_UTILS";
	
	private final String separator = System.getProperty("line.separator");
	
	public boolean deleteConfigFile(Context context) {
		Logger.d(TAG, "Deleting File");
		File dir = context.getFilesDir();
		File file = new File(dir, CustomConstants.CONFIG_FILENAME);
		return file.delete();
	}
	
	public ArrayList<EventContact> generateContactListFromConfig(Context context) {
		
		Logger.d(TAG, "Generating Contact Objects from Config File");
		
		ArrayList<EventContact> contactList = new ArrayList<EventContact>();
		
		try {
			Logger.d(TAG, "Opening Config File " + CustomConstants.CONFIG_FILENAME);

			// Open the file
			FileInputStream fosIn = context.openFileInput(CustomConstants.CONFIG_FILENAME);
			// Create an InputStream
			InputStreamReader inputStreamReader = new InputStreamReader(fosIn);
			
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String receiveString = "";
			
			while ((receiveString = bufferedReader.readLine()) != null) {
				
				EventContact contact = new EventContact();
				
				String[] data = receiveString.split(",");

				if (data.length >=3) {
					contact.setContactName(data[0]);
					contact.setContactDevice(data[1]);
					contact.setDeviceID(data[2]);
					contact.setContactAction(data[3]);
					contactList.add(contact);
				}
			}
			
			bufferedReader.close();
			inputStreamReader.close();
			
			Logger.d(TAG, "Contact List Generated with size =  " + contactList.size());
			return contactList;
			
		} catch (FileNotFoundException e) {
			Logger.e(TAG, "Exception : " + e.getLocalizedMessage());
			return null;
		} catch (IOException e) {
			Logger.e(TAG, "Exception : " + e.getLocalizedMessage());
			return null;
		}
	}

	public boolean isConfigFileSet (Context context) {
		boolean didOperationSucceed = false;
		// Check if Config File exists
		String result = readFromConfigFile(context);
		
		if (result == null) {
			Logger.d(TAG, "Error while accessing Config File");
		} else if (result.equals("") || result.length() < 2) {
			Logger.d(TAG, "Config File is empty");
		} else {
			Logger.d(TAG, "Retrieved Contents of Config File: "  + result);

			didOperationSucceed = true;
		}
		return didOperationSucceed;
	}
	
	public String readFromConfigFile(Context context) {
		try {
			Logger.d(TAG, "Opening Config File " + CustomConstants.CONFIG_FILENAME);

			// Open the file
			FileInputStream fosIn = context.openFileInput(CustomConstants.CONFIG_FILENAME);
			// Create an InputStream
			InputStreamReader inputStreamReader = new InputStreamReader(fosIn);
			
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String receiveString = "";
			StringBuilder stringBuilder = new StringBuilder();
			while ((receiveString = bufferedReader.readLine()) != null) {
				stringBuilder.append(receiveString);
				Logger.d(TAG, "File InputStream : " + receiveString);
			}
			
			bufferedReader.close();
			inputStreamReader.close();
			Logger.d(TAG, "This is what I read from the file : " + stringBuilder.toString());
			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			Logger.e(TAG, "Exception : " + e.getLocalizedMessage());
			return null;
		} catch (IOException e) {
			Logger.e(TAG, "Exception : " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public boolean writeToSettingsFile(Context context, String contents) {
		try {
			FileOutputStream fosOut = context.openFileOutput(CustomConstants.CONFIG_FILENAME,
					Context.MODE_APPEND);
			
			OutputStreamWriter osw = new OutputStreamWriter(fosOut);
			
			osw.append(contents);
			osw.append(separator);
			
			osw.flush();
			osw.close();
			
			fosOut.close();
			
			Logger.i(TAG, "Successfully Written Config File");
			return true;
		} catch (FileNotFoundException e) {
			Logger.i(TAG, "Exception : " + e.getLocalizedMessage());
			return false;
		} catch (IOException e) {
			Logger.i(TAG, "Exception : " + e.getLocalizedMessage());
			return false;
		}
	}
	
	public boolean updateConfigFile(Context context, ArrayList<EventContact> contactList) {

		// clear the configuration file first
		deleteConfigFile(context);
		
		// iterate through the list and append the contacts to the file
		for (EventContact contact : contactList) {
			Logger.d(TAG, "Appending " + contact.getFileWritableString());
			writeToSettingsFile(context, contact.getFileWritableString());
		}
		return true;
	}
	
}
