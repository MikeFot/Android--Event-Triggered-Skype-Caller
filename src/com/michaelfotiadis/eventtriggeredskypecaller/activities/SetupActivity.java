package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.DeviceType;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.EventContact;
import com.michaelfotiadis.eventtriggeredskypecaller.dialogs.DeviceDialogFragment;
import com.michaelfotiadis.eventtriggeredskypecaller.dialogs.InterfaceDialogListener;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.DataUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.FileUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class SetupActivity extends SensorBaseActivity implements OnItemSelectedListener, InterfaceDialogListener {

	private final String TAG = "SETUP_ACTIVITY";
	private final String TAG_DEVICE_PICKER = "DEVICE_PICKER";

	// UI Elements
	private Button mSetupContactButton;
	private Button mSetupDeviceButton;
	private Spinner mActionSpinner;

	private String mNameResult;
	private String mDeviceID;
	private String mDeviceType;
	private String mActionType;
	private String mResultDeviceID;

	private final String mToastString3 = "Configuration not found! Please register a contact first.";

	private ArrayList<EventContact> mContactList = new ArrayList<EventContact>();

	private boolean isDeviceSetUp = false;
	private boolean isUserNameSetUp = false;

	@SuppressWarnings("unchecked")
	private void startListDisplayActivity() {
		if (mContactList.size() > 0) {
			Intent intent = new Intent(this, ListDisplayActivity.class);
			intent.putParcelableArrayListExtra(CustomConstants.EXTRA_PAYLOAD, (ArrayList<? extends Parcelable>) mContactList);
			startActivity(intent);
		} else {
			ToastUtils.makeWarningToast(this, mToastString3);
		}
	}

	/**
	 * Handles Results from other Activities
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.d(TAG, "Request Code = " + requestCode);
		Logger.d(TAG, "Result Code = " + resultCode);

		if (requestCode == CustomConstants.REQUEST_CODE_1) {
			if (resultCode == RESULT_OK) {
				mNameResult = data.getStringExtra(CustomConstants.EXTRA_RESULT);
				mResultDeviceID = new DataUtils().getMd5(mNameResult);

				Logger.d(TAG, "Result returned : " + mNameResult);
				ToastUtils.makeInfoToast(this, "Result returned : "
						+ mNameResult);

				// Populate the TextView
				mSetupContactButton.setText("Skype Name: " + mNameResult);
				isDeviceSetUp = false;
				isUserNameSetUp = true;
				mSetupDeviceButton.setText(R.string.label_button_setup_device);
			}
			if (resultCode == RESULT_CANCELED) {
				Logger.d(TAG, "Operation cancelled. No result returned.");
			}
		} else if (requestCode == CustomConstants.REQUEST_CODE_2) {
			if (resultCode == RESULT_OK) {

				mDeviceID = data.getStringExtra(CustomConstants.EXTRA_RESULT);

				mDeviceType = DeviceType.NFC.getString();
				mSetupDeviceButton.setText("Using NFC Tag");

				Logger.d(TAG, "Result returned : " + mDeviceID);
				ToastUtils.makeInfoToast(this, "Result returned : " + mDeviceID);
				isDeviceSetUp = true;
			}
			if (resultCode == RESULT_CANCELED) {
				// consider device not set up if you get a cancelled code
				Logger.d(TAG, "Operation cancelled. No result returned.");
				isDeviceSetUp = false;
				mSetupDeviceButton.setText(R.string.label_button_setup_device);
			}
		} else if (requestCode == CustomConstants.REQUEST_CODE_3) {

			if (resultCode == RESULT_OK) {
				Logger.d(TAG, "Result from Bluetooth LE Setup");
				mDeviceID = data.getStringExtra(CustomConstants.EXTRA_RESULT);

				mDeviceType = DeviceType.IBEACON.getString();
				mSetupDeviceButton.setText("Using IBeacon Tag");

				Logger.d(TAG, "Result returned : " + mDeviceID);
				ToastUtils.makeInfoToast(this, "Result returned : " + mDeviceID);
				isDeviceSetUp = true;
			}
			if (resultCode == RESULT_CANCELED) {
				// consider device not set up if you get a cancelled code
				Logger.d(TAG, "Operation cancelled. No result returned.");
				ToastUtils.makeWarningToast(this, "No result returned");
				isDeviceSetUp = false;
				mSetupDeviceButton.setText(R.string.label_button_setup_device);
			}
		}
	}

	@Override
	public void onClickCall(android.support.v4.app.DialogFragment dialog) {
		// Unused

	}

	@Override
	public void onClickChat(android.support.v4.app.DialogFragment dialog) {
		// Unused

	}

	public void onClickCommitSetup(View view) {

		if (!isDeviceSetUp || !isUserNameSetUp) {
			ToastUtils.makeWarningToast(this, "Setup Not Complete. Please Select a Username and a Device");
			return;
		}

		// Call method to validate the stored variables
		validateResults();

		boolean wasSetupSuccessful = false;
		boolean isContactExisting = false;

		Logger.d(TAG, "Commiting Results");
		for (EventContact contact : mContactList) {

			if (contact.getContactName().equals(mNameResult)) {
				contact.setContactDevice(mDeviceType);
				contact.setDeviceID(mDeviceID);
				contact.setContactAction(mActionType);
				isContactExisting = true;

				break;
			}

		}

		if (isContactExisting) {
			Logger.d(TAG, "Contact already existing in setup file. Updating");
			wasSetupSuccessful = new FileUtils().updateConfigFile(this, mContactList);
		} else {
			StringBuilder contents = new StringBuilder();
			contents.append(mNameResult);
			contents.append(',');
			contents.append(mDeviceType);
			contents.append(',');
			contents.append(mDeviceID);
			contents.append(',');
			contents.append(mActionType);

			wasSetupSuccessful = new FileUtils()
			.writeToSettingsFile(this, contents.toString());
		}

		if (wasSetupSuccessful) {
			ToastUtils.makeInfoToast(this, "Setup Successful");
			startMainActivity();
		} else {
			ToastUtils.makeWarningToast(this, "Error While Setting Up");
		}
	}

	@Override
	public void onClickIBeacon(android.support.v4.app.DialogFragment dialog) {
		Logger.d(TAG, "Click on IBeacon Setup");
		Intent i = new Intent(this, BluetoothLEActivity.class);
		i.putExtra(CustomConstants.EXTRA_PAYLOAD, mNameResult);
		startActivityForResult(i, CustomConstants.REQUEST_CODE_3);
	}

	@Override
	public void onClickNFC(android.support.v4.app.DialogFragment dialog) {
		Logger.d(TAG, "Click on NFC Setup");
		Intent i = new Intent(this, WriteNFCActivity.class);
		i.putExtra(CustomConstants.EXTRA_PAYLOAD, mNameResult);
		startActivityForResult(i, CustomConstants.REQUEST_CODE_2);
	}

	/**
	 * Starts the Activity for picking a contact and returns the result
	 * 
	 * @param view
	 */
	public void onClickSetupContact(View view) {
		Logger.d(TAG, "Starting Register Contact Activity For Result");
		Intent i = new Intent(this, RegisterContactActivity.class);
		startActivityForResult(i, CustomConstants.REQUEST_CODE_1);
	}

	/**
	 * Starts a Dialog Fragment for selecting a Device Type. A Listener will
	 * handle the selection (see implemented methods)
	 * 
	 * @param view
	 */
	public void onClickSetupDevice(View view) {

		if (isUserNameSetUp) {
			Logger.d(TAG, "Starting Dialog");
			new DeviceDialogFragment()
			.show(getSupportFragmentManager(), TAG_DEVICE_PICKER);
		} else {
			ToastUtils.makeWarningToast(this, "Please Select a Contact First");
		}
	}

	@Override
	public void onClickVideoCall(android.support.v4.app.DialogFragment dialog) {
		// Unused
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		// gets the activity's default ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.show();


		mContactList = ((ArrayList<EventContact>) getIntent()
				.getSerializableExtra(CustomConstants.EXTRA_PAYLOAD));

		// Assign the UI Widgets by ID
		mSetupContactButton = (Button) findViewById(R.id.buttonSetupContact);
		mSetupDeviceButton = (Button) findViewById(R.id.buttonStartDeviceSync);

		// Set Up the Spinner
		mActionSpinner = (Spinner) findViewById(R.id.spinnerSelectAction);
		ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter
				.createFromResource(this, R.array.array_actions,
						android.R.layout.simple_spinner_item);
		dataAdapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mActionSpinner.setAdapter(dataAdapter);
		// Set up the listener
		mActionSpinner.setOnItemSelectedListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_setup_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Logger.d(TAG, "Spinner Selection at position " + pos);
		mActionType = parent.getItemAtPosition(pos).toString();
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Logger.d(TAG, "No Item Selected");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// same as using a normal menu
		switch (item.getItemId()) {
		case R.id.item_list:
			ToastUtils.makeInfoToast(this, "Displaying List");
			startListDisplayActivity();
			break;
		}
		return true;
	}

	private void startMainActivity() {
		Logger.d(TAG, "Starting Main Activity");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		// Finish this one
		finish();
	}

	public boolean validateResults() {

		if (!isDeviceSetUp) {
			ToastUtils.makeWarningToast(this, "Please Setup a Device First");
			return false;
		}

		Logger.d(TAG, "Validating Results");
		if (!mDeviceID.equals(mResultDeviceID)) {
			Logger.d(TAG, "MD5s do not match! Please try again");
			ToastUtils.makeWarningToast(this,
					"MD5s do not match! Please try again");
			return false;
		}
		if (mNameResult == null || mNameResult.length() < 1) {
			Logger.d(TAG, "Skype User Name not set up");
			ToastUtils.makeWarningToast(this,
					"Skype User Name not selected. Please try again.");
			return false;
		}
		if (mDeviceType == null) {
			Logger.d(TAG, "Device Type Error");
			ToastUtils.makeWarningToast(this, "Device Type Error");
			return false;
		}
		return true;
	}


}
