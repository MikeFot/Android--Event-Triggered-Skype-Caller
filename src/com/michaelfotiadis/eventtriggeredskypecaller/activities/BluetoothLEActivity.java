package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.util.Calendar;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.IBeaconDevice;
import uk.co.alt236.bluetoothlelib.util.IBeaconUtils;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.BluetoothLeDeviceStore;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.BluetoothLeScanner;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.BluetoothUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class BluetoothLEActivity extends FragmentActivity {

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(TAG, "On Receiver Result");
			if (intent.getAction().equalsIgnoreCase(
					CustomConstants.Broadcasts.BROADCAST_1.getString())) {
				Logger.i(TAG, "Scan Timed Out");
				onPostScan();
			}
		}
	}

	private BluetoothLeDeviceStore mDeviceStore;
	private BluetoothLeScanner mScanner;
	private BluetoothUtils mBluetoothUtils;

	private TextView reportTextView;
	private IBeaconDevice mClosestDevice;
	private int mCountScans = 0;
	private Long mScanStartTime = null;

	private Long mScanDuration = null;
	private final int TARGET_SCAN_DURATION = 5000;

	private final int TARGET_COUNT_SCANS_FALLBACK = 200;

	private final String TAG = "IBEACON_BLUETOOTH";

	private SuperActivityToast mSuperActivityToast;

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {

			final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device,
					rssi, scanRecord, System.currentTimeMillis());

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (IBeaconUtils.isThisAnIBeacon(deviceLe)) {

						mDeviceStore.addDevice(deviceLe);
						mCountScans++;
						Logger.i(TAG, "Found an iBeacon");
					}

					mScanDuration = Calendar.getInstance().getTimeInMillis()
							- mScanStartTime;
					Logger.i(TAG, "Scan Duration : " + mScanDuration.toString());

					addTextToTextView(reportTextView, "Scan Time "
							+ mScanDuration.toString() + ". Performed  "
							+ mCountScans + " scans.");

					if ((mScanDuration >= TARGET_SCAN_DURATION || mCountScans >= TARGET_COUNT_SCANS_FALLBACK)
							&& mDeviceStore.getDeviceList().size() >= 1) {
						mScanner.scanLeDevice(-1, false);
						onPostScan();
					}
				}
			});
		}
	};

	private ResponseReceiver mReceiver;

	private void addTextToTextView(TextView tv, String text) {
		if (tv.getLineCount() > 20) {
			tv.setText("");
		} else {
			tv.append("\n");
		}
		tv.append(text);
	}

	/*
	public boolean canIDoSomethingWithDevice(BluetoothLeDevice evaluatedDevice) {
		if (!IBeaconUtils.isThisAnIBeacon(evaluatedDevice)) {
			return false;
		}
		IBeaconDevice device = new IBeaconDevice(evaluatedDevice);
		IBeaconDistanceDescriptor distanceDescriptor = device
				.getDistanceDescriptor();

		if (distanceDescriptor == IBeaconDistanceDescriptor.IMMEDIATE) {
			addTextToTextView(reportTextView, "Found Immediate Device "
					+ device.getUUID());
			Logger.i(TAG, "Found Immediate Device " + device.getUUID());
			mClosestDevice = device;
			return true;
		} else {
			addTextToTextView(reportTextView, "Found a distant Device "
					+ device.getUUID() + " which is " + distanceDescriptor);
			Logger.i(TAG, "Found a distant Device " + device.getUUID()
					+ " which is " + distanceDescriptor);
		}
		return false;
	}
	*/

	public void initialiseBluetoothScan() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
				mCountScans = 0;

				mScanStartTime = Calendar.getInstance().getTimeInMillis();
				Logger.i(TAG, "Start Time is " + mScanStartTime);
				mDeviceStore.clear();

				if (mBluetoothUtils.isBluetoothOn()
						&& mBluetoothUtils.isBluetoothLeSupported()) {
					Logger.i(TAG, "Starting Scan");
					mScanner.scanLeDevice(TARGET_SCAN_DURATION, true);
				}
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_le);
		reportTextView = (TextView) findViewById(R.id.textViewBluetoothReport);
		reportTextView.setText("Starting Scan");
		startScan();
	}

	/**
	 * Called when activity gets invisible
	 */
	@Override
	protected void onPause() {
		super.onPause();
		removeReceivers();
	}

	private void onPostScan() {
		Logger.i(TAG, "Dismissing Toast");
		ToastUtils.dismissToast(mSuperActivityToast);

		removeReceivers();

		if (mDeviceStore.getDeviceList() != null
				&& mDeviceStore.getDeviceList().size() > 0) {

			addTextToTextView(reportTextView, " Devices found "
					+ mDeviceStore.getDeviceList().size());
			for (BluetoothLeDevice d : mDeviceStore.getDeviceList()) {
				if (IBeaconUtils.isThisAnIBeacon(d)) {
					IBeaconDevice iDevice = new IBeaconDevice(d);
					addTextToTextView(
							reportTextView,
							iDevice.getDevice().getAddress() + " "
									+ iDevice.getRunningAverageRssi());
				}
			}
			mClosestDevice = new IBeaconDevice(mDeviceStore.getDeviceList()
					.get(0));
			addTextToTextView(reportTextView, "The closest beacon is "
					+ mClosestDevice.getDevice().getAddress());
			addTextToTextView(reportTextView, "Distance Descriptor is "
					+ mClosestDevice.getDistanceDescriptor());

			CustomConstants.CLOSEST_DEVICE = mClosestDevice.getDevice()
					.getAddress();

			ToastUtils.makeInfoToast(this, "Device Detected");
			showChoiceDialog();

		} else {
			addTextToTextView(reportTextView, "No LE Devices Found");
			Logger.d(TAG, "No LE Devices Found");

			ToastUtils.makeInfoToast(this, "No Devices Found");
		}
	}

	protected void removeReceivers() {
		try {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mReceiver);
			Logger.d(TAG, "Receiver Unregistered Successfully");
		} catch (Exception e) {
			Logger.d(
					TAG,
					"Receiver Already Unregistered. Exception : "
							+ e.getLocalizedMessage());
		}
	}

	/**
	 * Method which broadcasts the activity result
	 * @param result String value of the result
	 */
	private void sendResult(String result) {
		Logger.d(TAG, "Sending Result: " + result);
		Intent returnIntent = new Intent();
		returnIntent.putExtra(CustomConstants.EXTRA_RESULT, result);
		// if a device has not been selected, set result cancelled
		if (result == null || result.length() < 1) {
			setResult(RESULT_CANCELED);
		} else {
			setResult(RESULT_OK, returnIntent);
		}
		finish();
	}

	protected void showChoiceDialog() {

		String[] results = new String[mDeviceStore.getDeviceList().size()];

		int i = 0;
		IBeaconDevice iDevice;

		// Generate the selection string
		for (BluetoothLeDevice d : mDeviceStore.getDeviceList()) {
			iDevice = new IBeaconDevice(d);
			results[i] = "Device ID: " + iDevice.getDevice().getAddress()
					+ CustomConstants.LINE_SEPARATOR + "Signal Strength: "
					+ iDevice.getRunningAverageRssi()
					+ CustomConstants.LINE_SEPARATOR + "Distance Descriptor: "
					+ iDevice.getDistanceDescriptor()
					+ CustomConstants.LINE_SEPARATOR;
			i++;
		}

		// Show a dialog which allows for the selection of a device
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a Device");
		builder.setItems(results, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int position) {
				Logger.d(TAG, "Selected " + position);
				IBeaconDevice iDevice = new IBeaconDevice(mDeviceStore
						.getDeviceList().get(position));
				sendResult(iDevice.getDevice().getAddress());
			}

		});
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				sendResult(null);
			}
		});

		builder.show();
	}

	protected void startScan() {
		reportTextView.setText("Starting Scan");

		Logger.i(TAG, "Registering the Broadcast Receiver");

		IntentFilter mIntentFilter = new IntentFilter(
				CustomConstants.Broadcasts.BROADCAST_1.getString());

		mReceiver = new ResponseReceiver();
		Logger.d(TAG, "Registering Receiver");
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
				mIntentFilter);

		mDeviceStore = new BluetoothLeDeviceStore();
		mBluetoothUtils = new BluetoothUtils(this);
		mScanner = new BluetoothLeScanner(this, mLeScanCallback, mBluetoothUtils);
		mSuperActivityToast = ToastUtils.makeProgressToast(this,
				mSuperActivityToast, "Scanning...");
		initialiseBluetoothScan();
	}

}
