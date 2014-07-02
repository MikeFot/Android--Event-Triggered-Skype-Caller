package com.michaelfotiadis.eventtriggeredskypecaller.utils;

import org.apache.commons.codec_1_9.binary.Hex;
import org.apache.commons.codec_1_9.digest.DigestUtils;

import uk.co.alt236.bluetoothlelib.util.IBeaconUtils.IBeaconDistanceDescriptor;

public class DataUtils {

	private final String TAG = "HASH_UTILS";
	
	/**
	 * Generates md5 hex hash from a String
	 * @param data String parameter to be converted
	 * @return md5 hash
	 */
	public String getMd5 (String data) {
		Logger.d(TAG, "Generating MD5");
		return new String(Hex.encodeHex(DigestUtils.md5(data)));
	}
	
	public int getValueOfEnum (IBeaconDistanceDescriptor desc) {
		
		if (desc == IBeaconDistanceDescriptor.IMMEDIATE) {
			return 0;
		} else if (desc == IBeaconDistanceDescriptor.NEAR) {
			return 1;
		} else if (desc == IBeaconDistanceDescriptor.FAR) {
			return 2;
		} else if (desc == IBeaconDistanceDescriptor.UNKNOWN) {
			return 3;
		} else {
			return Integer.MIN_VALUE;
		}
	}
	
	public IBeaconDistanceDescriptor getEnumOfValue (int value) {
		
		if (value == 0) {
			return  IBeaconDistanceDescriptor.IMMEDIATE;
		} else if (value == 1) {
			return  IBeaconDistanceDescriptor.NEAR;
		} else if (value == 2) {
			return IBeaconDistanceDescriptor.FAR;
		} else if (value == 3) {
			return IBeaconDistanceDescriptor.UNKNOWN;
		} else {
			return null;
		}
	}
	
}
