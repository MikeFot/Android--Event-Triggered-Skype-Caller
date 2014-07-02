package com.michaelfotiadis.eventtriggeredskypecaller.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;

public class BluetoothLeScanner {
	private final Handler mHandler;
	private final BluetoothAdapter.LeScanCallback mLeScanCallback;
	private final BluetoothUtils mBluetoothUtils;
	private boolean mScanning;
	private Context mContext;
	private final String TAG = "IBEACON_BLUETOOTH_LESCANNER";
	
	
	public BluetoothLeScanner(Context context, BluetoothAdapter.LeScanCallback leScanCallback, BluetoothUtils bluetoothUtils){
		mHandler = new Handler();
		mLeScanCallback = leScanCallback;
		mBluetoothUtils = bluetoothUtils;
		mContext = context;
	}
	
	public boolean isScanning() {
		return mScanning;
	}

	public void scanLeDevice(final int duration, final boolean enable) {
        if (enable) {
        	if(mScanning){return;}
        	Logger.d(TAG, "~ Starting Scan");
            // Stops scanning after a pre-defined scan period.
        	if(duration > 0){
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                	Logger.d(TAG, "~ Stopping Scan (timeout)");
	                    mScanning = false;
	                    mBluetoothUtils.getBluetoothAdapter().stopLeScan(mLeScanCallback);
	                    notifyFinished(mScanning);
	                }
	            }, duration);
        	}
            mScanning = true;
            mBluetoothUtils.getBluetoothAdapter().startLeScan(mLeScanCallback);
        } else {
        	Logger.d(TAG, "~ Stopping Scan");
            mScanning = false;
            mBluetoothUtils.getBluetoothAdapter().stopLeScan(mLeScanCallback);
            notifyFinished(mScanning);
        }
    }
	
	private void notifyFinished(boolean isFileFound){

		Intent broadcastIntent = new Intent(CustomConstants.Broadcasts.BROADCAST_1.getString());
		broadcastIntent.putExtra(CustomConstants.EXTRA_PAYLOAD, mScanning);

		Logger.i(TAG, "Broadcasting Result");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
	}
	
}
