package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class SensorBaseActivity extends FragmentActivity {

	private final String TAG = "Sensor Base Activity";

	
	/**
	 * Method which checks for the existence of an NFC Adapter
	 */
	protected void checkForNFCAdapter() {
		if (NfcAdapter.getDefaultAdapter(this) != null) {

			if (!NfcAdapter.getDefaultAdapter(this).isEnabled()) {

				LayoutInflater inflater = getLayoutInflater();
				View dialoglayout = inflater.inflate(
						R.layout.activity_main,
						(ViewGroup) findViewById(R.layout.activity_main));
				new AlertDialog.Builder(this)
				.setView(dialoglayout)
				.setPositiveButton("Please Enable the Wireless Network",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0,
							int arg1) {
						Intent setnfc = new Intent(
								Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(setnfc);
					}
				})
				.setOnCancelListener(
						new DialogInterface.OnCancelListener() {

							public void onCancel(DialogInterface dialog) {
								finish(); // exit application if user
								// cancels
							}
						}).create().show();

			} else {
				Logger.d(TAG, "NFC Scanning is Enabled");
			}

		} else {
			ToastUtils.makeWarningToast(this, "Sorry, No NFC Adapter found.");
		}
	}
}
