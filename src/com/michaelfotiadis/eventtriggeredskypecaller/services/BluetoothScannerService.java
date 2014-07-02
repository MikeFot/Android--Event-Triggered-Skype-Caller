package com.michaelfotiadis.eventtriggeredskypecaller.services;
//package com.eratosthenes.eventtriggeredskypecaller.services;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
//import uk.co.alt236.bluetoothlelib.device.IBeaconDevice;
//import uk.co.alt236.bluetoothlelib.util.IBeaconUtils;
//import uk.co.alt236.bluetoothlelib.util.IBeaconUtils.IBeaconDistanceDescriptor;
//import android.app.IntentService;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
//
//import com.eratosthenes.eventtriggeredskypecaller.MyApp;
//import com.eratosthenes.eventtriggeredskypecaller.activities.MainActivity.ResponseReceiver;
//import com.eratosthenes.eventtriggeredskypecaller.containers.BluetoothLeDeviceStore;
//import com.eratosthenes.eventtriggeredskypecaller.containers.CustomConstants;
//import com.eratosthenes.eventtriggeredskypecaller.containers.EventContact;
//import com.eratosthenes.eventtriggeredskypecaller.containers.SkypeAction;
//import com.eratosthenes.eventtriggeredskypecaller.dialogs.SkypeActionDialogFragment;
//import com.eratosthenes.eventtriggeredskypecaller.util.BluetoothLeScanner;
//import com.eratosthenes.eventtriggeredskypecaller.util.BluetoothUtils;
//import com.eratosthenes.eventtriggeredskypecaller.util.Logger;
//
//public class BluetoothScannerService extends IntentService {
//	private final String TAG = "LE Scanner Service";
//
//	private BluetoothUtils mBluetoothUtils;
//	
//	// BlueTooth Fields
//	private BluetoothLeDeviceStore mDeviceStore;
//	private BluetoothLeScanner mScanner;
//	private Long mScanStartTime = null;
//	private final int TARGET_SCAN_DURATION = -1;
//	
//	protected ArrayList<EventContact> mContactList;
//	
//	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//		@Override
//		public void onLeScan(final BluetoothDevice device, int rssi,
//				byte[] scanRecord) {
//
//			final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device,
//					rssi, scanRecord, System.currentTimeMillis());
//
//			new Runnable() {
//				@Override
//				public void run() {
//					if (IBeaconUtils.isThisAnIBeacon(deviceLe)) {
//
//						IBeaconDevice device = new IBeaconDevice(deviceLe);
//						IBeaconDistanceDescriptor distanceDescriptor = device
//								.getDistanceDescriptor();
//
//						if (distanceDescriptor == IBeaconDistanceDescriptor.NEAR || distanceDescriptor == IBeaconDistanceDescriptor.IMMEDIATE) {
//
//							String result = deviceLe.getAddress();
//
//							for (EventContact contact : mContactList) {
//								if (contact.getDeviceID().equals(result)) {
//									Logger.d(TAG, "Device ID matches contact " + contact.getContactName());
//
//									notifyFinished(result);
//									break;
//								}
//							}
//							mDeviceStore.addDevice(deviceLe);
//							Logger.i(TAG, "Found an iBeacon");
//						}
//					}
//				}
//			};
//		}
//	};
//
//	private ResponseReceiver mReceiver;
//	
//	
//	public BluetoothScannerService() {
//		super("BlueTooth Scanner Service");
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void onHandleIntent(Intent intent) {
//		Logger.i(TAG, "Entered Service");
//		
//		mContactList =  (ArrayList<EventContact>) intent.getSerializableExtra(CustomConstants.EXTRA_PAYLOAD);
//		
//		mReceiver = new ResponseReceiver();
//		
//		IntentFilter mIntentFilter = new IntentFilter(
//				CustomConstants.Broadcasts.BROADCAST_1.getString());
//		
//		LocalBroadcastManager.getInstance(MyApp.getAppContext()).registerReceiver(mReceiver,
//				mIntentFilter);
//
//		
//		
//		mBluetoothUtils = new BluetoothUtils(MyApp.getAppContext());
//		mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
//		
//		Logger.i(TAG, "Starting Scan");
//		mScanner.scanLeDevice(TARGET_SCAN_DURATION, true);
//		
//	}
//
//	
//	private void notifyFinished(String result){
//		// Clear memory 
//
//		Intent broadcastIntent = new Intent(CustomConstants.LE_SCAN_ACTION);
//		broadcastIntent.putExtra(CustomConstants.EXTRA_PAYLOAD, result);
//
//		Logger.i(TAG, "Broadcasting Result");
//		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
//	}
//	
//}
