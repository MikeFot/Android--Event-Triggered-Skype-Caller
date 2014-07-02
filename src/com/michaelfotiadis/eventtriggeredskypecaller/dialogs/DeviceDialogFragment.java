package com.michaelfotiadis.eventtriggeredskypecaller.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;

public class DeviceDialogFragment extends DialogFragment {

	private final String TAG = "DIALOG_FRAGMENT";
	InterfaceDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (InterfaceDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.pick_device)
		.setItems(R.array.array_devices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
					
				Logger.d(TAG,  "Clicked : " + which);
				if (which == 0) {
					mListener.onClickNFC(DeviceDialogFragment.this);
				} else if (which == 1) {
					mListener.onClickIBeacon(DeviceDialogFragment.this);
				}
			}
		});
		return builder.create();
	}
}