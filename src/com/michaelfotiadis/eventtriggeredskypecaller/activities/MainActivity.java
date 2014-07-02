package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.util.ArrayList;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.IBeaconDevice;
import uk.co.alt236.bluetoothlelib.util.IBeaconUtils;
import uk.co.alt236.bluetoothlelib.util.IBeaconUtils.IBeaconDistanceDescriptor;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.DeviceType;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.EventContact;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.SkypeAction;
import com.michaelfotiadis.eventtriggeredskypecaller.dialogs.InterfaceDialogListener;
import com.michaelfotiadis.eventtriggeredskypecaller.dialogs.SkypeActionDialogFragment;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.BluetoothLeScanner;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.BluetoothUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.DataUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.FileUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.SkypeUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class MainActivity extends SensorBaseActivity implements
InterfaceDialogListener {

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(TAG, "On Receiver Result");
			if (intent.getAction().equalsIgnoreCase(
					CustomConstants.Broadcasts.BROADCAST_1.getString())) {
				Logger.i(TAG, "Scan Timed Out");
			}
		}
	}

	// Define Tags
	private final String TAG = "Main Activity";

	private final String TAG_ACTION_PICKER = "Skype Action Picker";

	// Activity Toast
	private SuperActivityToast mSuperActivityToast;
	// Instances of non-static classes
	private SkypeUtils mSkypeUtilities;
	private FileUtils mFileUtils;

	private BluetoothUtils mBluetoothUtils;

	private String mSkypeUserName;
	// Toast Strings
	private final String mToastString1 = "Checking for Skype Installation";
	private final String mToastString2 = "Loading Data from Config File";
	private final String mToastString3 = "Configuration not found! Please execute setup first.";
	private final String mToastString4 = "Application Started";

	// Data
	private ArrayList<EventContact> mContactList = new ArrayList<EventContact>();

	// Receivers
	private ResponseReceiver mReceiver;
	// Widgets
	private ProgressBar mNfcProgressBar;
	private ProgressBar mIBeaconProgressBar;
	private ToggleButton mNfcToggleButton;
	private SeekBar mSeekBar;

	private ToggleButton mIBeaconToggleButton;
	// NDEF Fields
	private NfcAdapter mNfcAdapter;
	private IntentFilter[] mNdefExchangeFilters;

	private PendingIntent mNfcPendingIntent;

	// BlueTooth Fields
	private BluetoothLeScanner mScanner;
	private final int TARGET_SCAN_DURATION = -1;

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

						IBeaconDevice device = new IBeaconDevice(deviceLe);

						// Get the distance descriptor as an integer
						int distanceDescriptorValue = new DataUtils()
						.getValueOfEnum(device.getDistanceDescriptor());

						// Compare this integer to the progress bar sensitivity
						if (distanceDescriptorValue <= mSeekBar.getProgress()) {

							String result = deviceLe.getAddress();

							for (EventContact contact : getContactList()) {
								if (contact.getDeviceID().equals(result)) {

									processScanResult(contact, DeviceType.IBEACON);
									break;
								}
							}
							Logger.i(TAG, "Found an iBeacon");
						}
					}
				}
			});
		}
	};




	private final int THREAD_SLEEP_TIME = 2000;

	protected void checkForConfigFile() {
		mSuperActivityToast = ToastUtils.makeProgressToast(this,
				mSuperActivityToast, mToastString2);
		mFileUtils = new FileUtils();

		// This is here for debugging
		// boolean isFileDeleted = mFileUtils.deleteConfigFile();
		// Logger.d(TAG, "Did I delete Config File? " + isFileDeleted);

		if (!mFileUtils.isConfigFileSet(this)) {
			Logger.d(TAG, "Config File not Found!");
			startSetupActivity(true);
		} else {
			Logger.d(TAG, "Attempting to Read File");
			mFileUtils.readFromConfigFile(this);

			ToastUtils.dismissToast(mSuperActivityToast);

			setContactList(new FileUtils().generateContactListFromConfig(this));

			for (EventContact contact : getContactList()) {
				Logger.i(TAG, contact.toString());
			}

		}
	}

	/**
	 * Method which checks for Skype installation on the local device and
	 * prompts to open the market if none is present
	 */
	protected void checkForSkypeInstallation() {

		mSuperActivityToast = ToastUtils.makeProgressToast(this,
				mSuperActivityToast, mToastString1);

		mSkypeUtilities = new SkypeUtils();
		Logger.d(TAG, "Checking for Skype Installation");
		// Make sure the Skype for Android client is installed
		if (!mSkypeUtilities.isSkypeClientInstalled(this)) {
			ToastUtils.dismissToast(mSuperActivityToast);
			Logger.i(TAG, "Skype Client Not Found. Going to Market.");
			ToastUtils.makeInfoToast(this,
					"Skype Client Not Found. Going to Market.");
			mSkypeUtilities.goToMarket(this);
			return;
		} else {
			ToastUtils.dismissToast(mSuperActivityToast);
			Logger.i(TAG, "Skype Installation Detected");
			return;
		}

	}

	protected void disableBluetoothScan() {
		if (mScanner != null) {
			mScanner.scanLeDevice(-1, false);
		}
	}

	public void disableNFCForegroundMode() {
		Logger.d(TAG, "disableForegroundMode");

		mNfcAdapter.disableForegroundDispatch(this);
	}

	public void enableBluetoothScan() {
		Logger.i(TAG, "Registering the Broadcast Receiver");

		IntentFilter mIntentFilter = new IntentFilter(
				CustomConstants.Broadcasts.BROADCAST_1.getString());

		mReceiver = new ResponseReceiver();
		Logger.d(TAG, "Registering Receiver");
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
				mIntentFilter);

		mBluetoothUtils = new BluetoothUtils(this);
		mScanner = new BluetoothLeScanner(this, mLeScanCallback, mBluetoothUtils);

		if (!mBluetoothUtils.isBluetoothLeSupported()) {
			ToastUtils.makeWarningToast(this, "BlueTooth LE is not supported by your device");
			return;
		}
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBluetoothUtils.askUserToEnableBluetoothIfNeeded();

				if (mBluetoothUtils.isBluetoothOn()
						&& mBluetoothUtils.isBluetoothLeSupported()) {
					Logger.i(TAG, "Starting Scan");
					mScanner.scanLeDevice(TARGET_SCAN_DURATION, true);
				} else {
					mIBeaconToggleButton.performClick();
				}
			}
		});
	}

	public void enableNFCForegroundMode() {
		Logger.d(TAG, "enableForegroundMode");

		// Activate the NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// make sure that NFC can be detected
		checkForNFCAdapter();

		// Create the Intent
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// foreground mode gives the current active application priority for
		// reading scanned tags
		mNdefExchangeFilters = new IntentFilter[] { new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED) };

		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mNdefExchangeFilters, null);
	}

	public ArrayList<EventContact> getContactList() {
		return mContactList;
	}

	@Override
	public void onClickCall(android.support.v4.app.DialogFragment dialog) {
		ToastUtils.makeInfoToast(MainActivity.this, "Starting Skype");
		new SkypeUtils().startSkypeAction(this, mSkypeUserName,
				SkypeAction.CALL.getString());
		

	}

	@Override
	public void onClickChat(android.support.v4.app.DialogFragment dialog) {
		ToastUtils.makeInfoToast(MainActivity.this, "Starting Skype");
		new SkypeUtils().startSkypeAction(this, mSkypeUserName,
				SkypeAction.CHAT.getString());
	}

	@Override
	public void onClickIBeacon(DialogFragment dialog) {
		// Do Nothing
	}

	@Override
	public void onClickNFC(DialogFragment dialog) {
		// Do Nothing
	}

	/**
	 * OnClick handler which starts the Register Contact Activity
	 * 
	 * @param view
	 */
	public void onClickRegisterContact(View view) {
		startSetupActivity(false);
	}

	public void onClickToggleIBeacon(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			mIBeaconProgressBar.setVisibility(View.VISIBLE);
			enableBluetoothScan();
		} else {
			Logger.d(TAG, "Stopping Scan");
			disableBluetoothScan();
			mIBeaconProgressBar.setVisibility(View.INVISIBLE);

		}
	}

	public void onClickToggleNFC(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			mNfcProgressBar.setVisibility(View.VISIBLE);

			Logger.d(TAG, "Giving NFC Priority to this App");
			// Initialise NFC
			enableNFCForegroundMode();

		} else {
			mNfcProgressBar.setVisibility(View.INVISIBLE);
			// Disable NFC
			disableNFCForegroundMode();
			Logger.d(TAG, "Disabling NFC");
		}
	}

	@Override
	public void onClickVideoCall(android.support.v4.app.DialogFragment dialog) {
		ToastUtils.makeInfoToast(MainActivity.this, "Starting Skype");
		new SkypeUtils().startSkypeAction(this, mSkypeUserName,
				SkypeAction.VIDEO_CALL.getString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.i(TAG, "Starting Main Activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// gets the activity's default ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.show();

		// Activate the NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// Set Widgets
		mNfcProgressBar = (ProgressBar) findViewById(R.id.progressBarNFC);
		mIBeaconProgressBar = (ProgressBar) findViewById(R.id.progressBarIBeacon);
		mNfcToggleButton = (ToggleButton) findViewById(R.id.toggleButtonNFC);
		mIBeaconToggleButton = (ToggleButton) findViewById(R.id.toggleButtonIBeacon);
		mSeekBar = (SeekBar) findViewById(R.id.seekBarSensitivity);

		setupSeekBar();

		// Look for Skype Installation
		checkForSkypeInstallation();
		// Look for Local File
		checkForConfigFile();

		mSuperActivityToast = ToastUtils.makeInfoToast(this, mToastString4);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_setup_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Logger.d(TAG, "Detected New NFC Intent");
			vibrate();
			NdefMessage[] messages = null;
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				messages = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					messages[i] = (NdefMessage) rawMsgs[i];
				}
			}
			if (messages[0] != null) {
				String result = "";
				byte[] payload = messages[0].getRecords()[0].getPayload();
				// this assumes that we get back am SOH followed by host/code
				for (int b = 1; b < payload.length; b++) { // skip SOH
					result += (char) payload[b];
				}

				for (EventContact contact : getContactList()) {
					if (contact.getDeviceID().equals(result)) {
						processScanResult(contact, DeviceType.NFC);
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// same as using a normal menu
		switch (item.getItemId()) {
		case R.id.item_list:
			mSuperActivityToast = ToastUtils.makeInfoToast(this,
					"Displaying List");
			// Display the configuration data in a new activity
			startListDisplayActivity();
			break;
		}
		return true;
	}

	/**
	 * Called when activity gets invisible
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Cancel all toasts currently showing
		SuperActivityToast.cancelAllSuperActivityToasts();
		disableBluetoothScan();
		removeReceivers();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkForConfigFile();

		// Cancel all toasts currently showing
		SuperActivityToast.cancelAllSuperActivityToasts();

		if (mNfcToggleButton.isChecked()) {
			enableNFCForegroundMode();
		}
		if (mIBeaconToggleButton.isChecked()) {
			enableBluetoothScan();
		}
	}

	public void onStart() {
		super.onStart();
		setContactList(new FileUtils().generateContactListFromConfig(this));
	}

	public void processScanResult(EventContact contact, DeviceType deviceType) {

		if (deviceType == DeviceType.IBEACON) {
			mIBeaconToggleButton.performClick();
		}

		Logger.d(TAG, "Device ID matches contact "
				+ contact.getContactName());
		Logger.d(TAG, "Action is "+ contact.getContactAction());
		mSkypeUserName = contact.getContactName();
		// Start the progress dialog
		showSkypeInitProgressDialog(deviceType, contact.getContactName(), contact.getContactAction());
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

	public void setContactList(ArrayList<EventContact> contactList) {
		this.mContactList = contactList;
	}

	/**
	 * Seek Bar which allows user to setup BlueTooth scanning sensitivity
	 */
	protected void setupSeekBar() {
		mSeekBar.setProgress(0);
		mSeekBar.incrementProgressBy(1);
		mSeekBar.setMax(2);
		mSeekBar.setProgress(1);

		final TextView mSeekBarValue = (TextView) findViewById(R.id.textViewBlueToothSensitivity);

		mSeekBarValue.setText(this.getString(
				R.string.label_bluetooth_sensitivity)
				+ CustomConstants.LINE_SEPARATOR
				+ String.valueOf(IBeaconDistanceDescriptor.NEAR));

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar mSeekBar, int progress,
					boolean fromUser) {

				IBeaconDistanceDescriptor descriptor = new DataUtils()
				.getEnumOfValue(progress);

				mSeekBarValue.setText(MainActivity.this.getString(
						R.string.label_bluetooth_sensitivity)
						+ CustomConstants.LINE_SEPARATOR 
						+ String.valueOf(descriptor.toString()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar mSeekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar mSeekBar) {

			}
		});
	}

	private void showSkypeInitProgressDialog(DeviceType deviceType, final String skypeUserName, final String action) {
		// Cancel all SuperCardToasts already showing
		SuperCardToast.cancelAllSuperCardToasts();

		// Create a progress dialogue
		final ProgressDialog progressDialogue = ProgressDialog.show(MainActivity.this, "Found: " + deviceType, 
				"User: " + skypeUserName + CustomConstants.LINE_SEPARATOR + 
				"Action: " + action);

		// start a new thread to process job
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
					
					if (action.equals(SkypeAction.PROMPT_USER
							.getString())) {
						Logger.d(TAG, "Prompting User");
						new SkypeActionDialogFragment().show(getSupportFragmentManager(), TAG_ACTION_PICKER);
					} else {
						ToastUtils.makeInfoToast(MainActivity.this, "Starting Skype");
						mSkypeUtilities.startSkypeAction(MainActivity.this, skypeUserName, action);
					}
					progressDialogue.cancel();
				} catch (InterruptedException e) {
					progressDialogue.cancel();
					Logger.e(TAG, e.getLocalizedMessage());
				}
			}

		});
		t.start();
	}

	@SuppressWarnings("unchecked")
	private void startListDisplayActivity() {
		if (mContactList != null && mContactList.size() > 0) {
			Intent intent = new Intent(this, ListDisplayActivity.class);
			intent.putParcelableArrayListExtra(CustomConstants.EXTRA_PAYLOAD,
					(ArrayList<? extends Parcelable>) getContactList());
			startActivity(intent);
		} else {
			mSuperActivityToast = ToastUtils.makeWarningToast(this,
					mToastString3);
		}
	}

	@SuppressWarnings("unchecked")
	private void startSetupActivity(boolean firstTimeSetup) {
		Logger.i(TAG, "Starting First Time Setup");
		Intent intent = new Intent(this, SetupActivity.class);
		intent.putParcelableArrayListExtra(CustomConstants.EXTRA_PAYLOAD,
				(ArrayList<? extends Parcelable>) getContactList());
		startActivity(intent);

		if (firstTimeSetup) {
			// Do not return here if the configuration file is not set yet
			finish();
		}

	}

	private void vibrate() {
		Logger.d(TAG, "vibrate");

		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(500);
	}

}
