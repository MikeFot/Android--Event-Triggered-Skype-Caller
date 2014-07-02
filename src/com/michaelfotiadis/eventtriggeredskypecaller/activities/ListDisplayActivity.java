package com.michaelfotiadis.eventtriggeredskypecaller.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.michaelfotiadis.eventtriggeredskypecaller.R;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomConstants;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.CustomInfoAdapter;
import com.michaelfotiadis.eventtriggeredskypecaller.containers.EventContact;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.AdapterUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.FileUtils;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.Logger;
import com.michaelfotiadis.eventtriggeredskypecaller.utils.ToastUtils;

public class ListDisplayActivity extends Activity implements
		OnItemClickListener, OnItemLongClickListener {

	private final String TAG = "ListDisplayActivity";

	private ArrayList<EventContact> contactList;

	private ListView mContactListView;
	private BaseAdapter mListAdapter;

	private final String mToastString1 = "Displaying Configuration";

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		contactList = ((ArrayList<EventContact>) getIntent()
				.getSerializableExtra(CustomConstants.EXTRA_PAYLOAD));

		if (contactList == null || contactList.size() < 1) {
			finish();
		}
		
		Logger.d(TAG, "Received the List with size of " + contactList.size());

		mContactListView = (ListView) findViewById(R.id.listViewConfigContacts);
		mContactListView.setOnItemClickListener(this);
		mContactListView.setOnItemLongClickListener(this);

	}
	private int mSelectionPosition;
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		StringBuilder sb = new StringBuilder();
		sb.append(contactList.get(position).getContactDevice());
		sb.append(CustomConstants.LINE_SEPARATOR);
		sb.append(contactList.get(position).getDeviceID());

		mSelectionPosition = position;
		
		SuperActivityToast.cancelAllSuperActivityToasts();
		
		SuperActivityToast superActivityToast = new SuperActivityToast(this,  SuperToast.Type.BUTTON);
		superActivityToast.setDuration(SuperToast.Duration.LONG);
		superActivityToast.setText(sb.toString());
		superActivityToast.setButtonIcon(SuperToast.Icon.Dark.EXIT, "DELETE");
		superActivityToast.setOnClickWrapper(onClickWrapper);
		superActivityToast.show();
		
	}
	
	OnClickWrapper onClickWrapper = new OnClickWrapper("SuperActivityToast", new SuperToast.OnClickListener() {

	    @Override
	    public void onClick(View view, Parcelable token) {

	    	AlertDialog.Builder adb = new AlertDialog.Builder(ListDisplayActivity.this);
			adb.setTitle("Delete?");
			adb.setMessage("Are you sure you want to delete this entry?");
			adb.setNegativeButton("Cancel", null);
			adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					// drop it from our list
					contactList.remove(mSelectionPosition);
					// update the contents of the configuration file
					new FileUtils().updateConfigFile(ListDisplayActivity.this, contactList);

					// let the adapter know that the contents have changed
					mListAdapter.notifyDataSetChanged();
					// re-populate it
					populateAdapter();
				}
			});
			adb.show();
	    }
	});
	
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position,
			long id) {

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Delete?");
		adb.setMessage("Are you sure you want to delete this entry?");
		final int positionToRemove = position;
		adb.setNegativeButton("Cancel", null);
		adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// drop it from our list
				contactList.remove(positionToRemove);
				// update the contents of the configuration file
				new FileUtils().updateConfigFile(ListDisplayActivity.this, contactList);

				// let the adapter know that the contents have changed
				mListAdapter.notifyDataSetChanged();
				// re-populate it
				populateAdapter();
			}
		});
		adb.show();

		return false;
	}

	public void onStart() {
		super.onStart();

		Logger.d(TAG, "Generating List View");

		// Populate the adapter as soon as the activity starts
		populateAdapter();

	}

	/**
	 * Method which populates the list adapter using a list
	 */
	private void populateAdapter() {
		List<Pair<String, String>> dataList = new AdapterUtils()
				.pairListFromObjectList(getContentResolver(), contactList);

		mListAdapter = new CustomInfoAdapter(this, dataList);

		Logger.d(TAG, "Setting List View Adapter");
		mContactListView.setAdapter(mListAdapter);

		// Start a new toast while waiting for the ASyncTask to finish
		ToastUtils.makeInfoToast(this, mToastString1);
	}
}
