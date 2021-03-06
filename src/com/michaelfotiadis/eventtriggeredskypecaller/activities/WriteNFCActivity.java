package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.DataUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class WriteNFCActivity extends Activity {

	private class WriteResponse {
		int status;
		String message;

		WriteResponse(int Status, String Message) {
			this.status = Status;
			this.message = Message;
		}

		public String getMessage() {
			return message;
		}

		public int getStatus() {
			return status;
		}
	}

	private String mSkype_Name;

	private String mMD5 = "Message";

	private static final String TAG = "NFC_WRITER";

	public static boolean supportedTechs(String[] techs) {
		boolean ultralight = false;
		boolean nfcA = false;
		boolean ndef = false;
		for (String tech : techs) {
			if (tech.equals("android.nfc.tech.MifareUltralight")) {
				ultralight = true;
			} else if (tech.equals("android.nfc.tech.NfcA")) {
				nfcA = true;
			} else if (tech.equals("android.nfc.tech.Ndef")
					|| tech.equals("android.nfc.tech.NdefFormatable")) {
				ndef = true;
			} else {
			}
		}
		if (ultralight && nfcA && ndef) {
			return true;
		} else {
			return false;
		}
	}

	private NfcAdapter mNfcAdapter;

	private PendingIntent mNfcPendingIntent;

	private boolean writeProtect = false;

	protected TextView mReportTextView;

	private SuperActivityToast mSuperActivityToast;

	private final String TOAST_STRING_1 = "Please Touch an NFC Tag to the Device";

	private final long VIBRATION_DURATION = 500;

	private long THREAD_SLEEP_TIME = 2000;

	protected void checkForNFCAdapter() {
		if (mNfcAdapter != null) {

			if (!mNfcAdapter.isEnabled()) {

				LayoutInflater inflater = getLayoutInflater();
				View dialoglayout = inflater.inflate(R.layout.activity_write_nfc,
						(ViewGroup) findViewById(R.layout.activity_write_nfc));
				new AlertDialog.Builder(this)
				.setView(dialoglayout)
				.setPositiveButton(
						"Please Enable the Wireless Network",
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

	public void disableForegroundMode() {
		Logger.d(TAG, "disableForegroundMode");
		SuperActivityToast.cancelAllSuperActivityToasts();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	public void enableForegroundMode() {
		Logger.d(TAG, "enableForegroundMode");

		// foreground mode gives the current active application priority for
		// reading scanned tags
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
		IntentFilter[] writeTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				writeTagFilters, null);
	}

	private NdefMessage getTagAsNdef() {
		boolean addAAR = false;
		String uniqueId = mMD5;
		byte[] uriField = uniqueId.getBytes(Charset.forName("US-ASCII"));
		byte[] payload = new byte[uriField.length + 1];

		System.arraycopy(uriField, 0, payload, 1, uriField.length);

		NdefRecord rtdTextRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);

		if (addAAR) {
			// note: returns AAR for different app (nfcreadtag)
			return new NdefMessage(
					new NdefRecord[] {
							rtdTextRecord,
							NdefRecord
							.createApplicationRecord("com.eratosthenes.eventtriggeredskypecaller") });
		} else {
			return new NdefMessage(new NdefRecord[] { rtdTextRecord });
		}
	}

	private void isTagSupported(Intent intent) {
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		String message;

		if (supportedTechs(detectedTag.getTechList())) {

			message = "Tag is Writable";
			Logger.d(TAG, message);

			// check if tag is writable (to the extent that we can
			if (writableTag(detectedTag)) {
				// writeTag here
				WriteResponse wr = writeTag(getTagAsNdef(), detectedTag);
				message = (wr.getStatus() == 1 ? "Success: " : "Failed: ")
						+ wr.getMessage();
				Logger.d(TAG, message);
				showProgressDialog(message);
				

			} else {
				message = "This tag is not writable";
				Logger.d(TAG, message);
				mReportTextView.append("\n" + message);
			}
		} else {
			message = "This tag type is not supported";
			Logger.d(TAG, message);
			mReportTextView.append("\n" + message);
		}

	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_nfc);

		// Set the textview
		mReportTextView = (TextView) findViewById(R.id.textViewNFCWriterTitle);

		// initialize NFC
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mSkype_Name = extras.getString(CustomConstants.EXTRA_PAYLOAD);
			mMD5 = new DataUtils().getMd5(mSkype_Name);
			mReportTextView.setText("Registering NFC Tag for : " + mSkype_Name);
			ToastUtils.makeProgressToast(this, mSuperActivityToast, TOAST_STRING_1 );
		}

	}

	@Override
	public void onNewIntent(Intent intent) { // this method is called when an
		// NFC tag is scanned
		Logger.d(TAG, "onNewIntent");

		// check for NFC related actions
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			// Call the intent to read the tag
			readTag(intent);

			isTagSupported(intent);

		} else {
		}
	}

	@Override
	protected void onPause() {
		super.onResume();

		Logger.d(TAG, "onPause");

		disableForegroundMode();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Logger.d(TAG, "onResume");

		checkForNFCAdapter();

		enableForegroundMode();
	}

	private void readTag(Intent intent) {
		ToastUtils.makeInfoToast(this, "Tag Detected");

		String message;

		Parcelable[] messages = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (messages != null) {
			Logger.d(TAG, "Found " + messages.length + " NDEF messages");

			vibrate(); // signal found messages :-)

			// parse to records
			for (int i = 0; i < messages.length; i++) {
				try {
					List<Record> records = new org.ndeftools.Message(
							(NdefMessage) messages[i]);

					message = "Found " + records.size()
							+ " records in message " + i;

					Logger.d(TAG, message);

					for (int k = 0; k < records.size(); k++) {
						message = " Record #" + k + " is of class "
								+ records.get(k).getClass().getSimpleName();
						Logger.d(TAG, message);

						Record record = records.get(k);

						Logger.d(TAG, record.toString());

						if (record instanceof AndroidApplicationRecord) {
							AndroidApplicationRecord aar = (AndroidApplicationRecord) record;

							message = "Package is " + aar.getPackageName();
							Logger.d(TAG, message);
						}
					}
				} catch (Exception e) {
					message = "Problem parsing message";
					Logger.e(TAG, "\n" + message, e);
				}
			}
		}
	}

	private void sendResult(String result) {
		Logger.d(TAG, "Sending Result: " + result);
		Intent returnIntent = new Intent();
		returnIntent.putExtra(CustomConstants.EXTRA_RESULT, result);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void showProgressDialog(final String message) {
		// Cancel all SuperCardToasts already showing
		SuperCardToast.cancelAllSuperCardToasts();

		// Create a progress dialogue
		final ProgressDialog progressDialogue = ProgressDialog.show(WriteNFCActivity.this, "NFC Tag Found", 
				message);

		// start a new thread to process job
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
					progressDialogue.cancel();
					sendResult(mMD5);
				} catch (InterruptedException e) {
					progressDialogue.cancel();
					Logger.e(TAG, e.getLocalizedMessage());
				}
			}
		});
		t.start();
	}

	private void vibrate() {
		Logger.d(TAG, "vibrate");

		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(VIBRATION_DURATION);
	}

	private boolean writableTag(Tag tag) {

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					ToastUtils.makeWarningToast(this, "Tag is Read-Only.");

					ndef.close();
					return false;
				}
				ndef.close();
				return true;
			}
		} catch (Exception e) {
			Logger.e(TAG, e.getLocalizedMessage());
			ToastUtils.makeWarningToast(this, "Failed to Read Tag");
		}

		return false;
	}

	public WriteResponse writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		String messsage = "";

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					return new WriteResponse(0, "Tag is read-only");
				}
				if (ndef.getMaxSize() < size) {
					messsage = "Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.";
					return new WriteResponse(0, messsage);
				}

				ndef.writeNdefMessage(message);
				if (writeProtect)
					ndef.makeReadOnly();
				messsage = "Tag Written Successfully";
				return new WriteResponse(1, messsage);
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						messsage = "Tag Formatted and Written Successfully";
						return new WriteResponse(1, messsage);
					} catch (IOException e) {
						messsage = "Failed to Format Tag";
						Logger.e(TAG, e.getLocalizedMessage());
						return new WriteResponse(0, messsage);
					}
				} else {
					messsage = "Tag does not support NDEF";
					return new WriteResponse(0, messsage);
				}
			}
		} catch (Exception e) {
			messsage = "Failed to write tag";
			Logger.e(TAG, e.getLocalizedMessage());
			return new WriteResponse(0, messsage);
		}
	}

}
