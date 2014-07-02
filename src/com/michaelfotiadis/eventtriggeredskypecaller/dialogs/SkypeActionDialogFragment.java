package com.michaelfotiadis.eventtriggeredskypecaller.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;

public class SkypeActionDialogFragment extends DialogFragment {

	private final String TAG = "DIALOG_FRAGMENT";
	InterfaceDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
			mListener = (InterfaceDialogListener) activity;

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.pick_action)
		.setItems(R.array.array_dialog_actions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
					
				Logger.d(TAG,  "Clicked : " + which);
				if (which == 0) {
					mListener.onClickCall(SkypeActionDialogFragment.this);
				} else if (which == 1) {
					mListener.onClickVideoCall(SkypeActionDialogFragment.this);
				}  else if (which == 2) {
					mListener.onClickChat(SkypeActionDialogFragment.this);
				}
			}
		});
		return builder.create();
	}
}