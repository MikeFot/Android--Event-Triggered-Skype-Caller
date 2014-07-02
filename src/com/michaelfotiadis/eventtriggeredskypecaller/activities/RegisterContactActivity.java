package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomInfoAdapter;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class RegisterContactActivity extends Activity implements
OnItemClickListener {

	private ListView mContactListView;
	private BaseAdapter mListAdapter;

	private String mDisplay_name = null;
	private String mUserName = null;

	// private String mAddressToRegister;

	private SuperActivityToast mSuperActivityToast;

	private final String TAG = "SKYPE_CONTACT_PICKER";
	private final String TOAST_STRING_1 = "Retrieving Skype Contacts. Please Wait...";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_contact);
	}

	public void onStart() {
		super.onStart();

		Logger.d(TAG, "Generating List View");
		mContactListView = (ListView) findViewById(R.id.contactSkypeListView);
		mContactListView.setOnItemClickListener(this);

		mListAdapter = new CustomInfoAdapter(this,
				new ArrayList<Pair<String, String>>());

		Logger.d(TAG, "Setting List View Adapter");
		mContactListView.setAdapter(mListAdapter);
		
		// Start a new toast while waiting for the ASyncTask to finish
		mSuperActivityToast = ToastUtils.makeProgressToast(this,
				mSuperActivityToast, TOAST_STRING_1);
		
		// Set up the query ASyncTask to retrieve skype contacts
		AsyncTaskRunner runner = new AsyncTaskRunner();
		runner.execute("");

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		mDisplay_name = ((Pair<String, String>) mListAdapter.getItem(position)).first;
		mUserName = ((Pair<String, String>) mListAdapter.getItem(position)).second;

		String message = "Selected: " + mDisplay_name + " with Skype Name: "
				+ mUserName;
		
		Logger.i(TAG, message);

		mSuperActivityToast = ToastUtils.makeInfoToast(this, message);

		sendResult(mUserName);
	}

	private void sendResult(String result) {
		Logger.d(TAG, "Sending Result: " + result);
		Intent returnIntent = new Intent();
		returnIntent.putExtra(CustomConstants.EXTRA_RESULT,result);
		setResult(RESULT_OK,returnIntent);
		finish();
	}
	
	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		final List<Pair<String, String >> dataList = new ArrayList<Pair<String,String>>(); 

		@Override
		protected String doInBackground(String... params) {
			Logger.d(TAG, "Starting ASyncTask in Background");
			final Cursor skypeCursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI, null, null, null,
					ContactsContract.Data.DISPLAY_NAME + " collate localized");

			final Set<String> addedSkypeNames = new HashSet<String>();

			while (skypeCursor.moveToNext()) {
				final int type = skypeCursor
						.getInt(skypeCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
				final String contactName = skypeCursor.getString(skypeCursor
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				final String imName = skypeCursor
						.getString(skypeCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

				if (type == ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE) {
					if (imName != null && imName.trim().length() > 0) {
						final String skypeName = imName.trim();

						if (!addedSkypeNames.contains(skypeName)) {
							dataList.add(new Pair<String, String>(contactName,
									skypeName));
							addedSkypeNames.add(skypeName);
						}
					}
				}
			}
			skypeCursor.close();
			Logger.d(TAG, "Cursor Closed");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Logger.d(TAG, "ASyncTask Post Executing");
			// execution of result of Long time consuming operation
			mListAdapter = new CustomInfoAdapter(RegisterContactActivity.this, dataList);

			mContactListView.setAdapter(mListAdapter);
			mSuperActivityToast.dismiss();
		}
	}

}
