package com.michaelfotiadis.eventtriggeredskypecaller.dialogs;

import android.support.v4.app.DialogFragment;

public interface InterfaceDialogListener {
	abstract public void onClickCall (DialogFragment dialog);
	abstract public void onClickVideoCall (DialogFragment dialog);
	abstract public void onClickChat (DialogFragment dialog);
	abstract public void onClickNFC(DialogFragment dialog);
	abstract public void onClickIBeacon(DialogFragment dialog);
}