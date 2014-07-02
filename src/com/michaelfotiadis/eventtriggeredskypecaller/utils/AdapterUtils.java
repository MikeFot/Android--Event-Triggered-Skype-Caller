package com.michaelfotiadis.eventtriggeredskypecaller.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.michaelfotiadis.eventtriggeredskypecaller.containers.EventContact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Pair;

public class AdapterUtils {

	private final String TAG = "AdapterUtils";

	public List<Pair<String, String>> queryContacts(final ContentResolver cr) {

		Logger.i(TAG, "Generating Contact DataList");

		final Cursor skypeCursor = cr.query(
				ContactsContract.Data.CONTENT_URI, 
				null, 
				null, 
				null, 
				ContactsContract.Data.DISPLAY_NAME + " collate localized");      

		final List<Pair<String, String >> dataList = new ArrayList<Pair<String,String>>(); 
		final Set<String> addedSkypeNames = new HashSet<String>();

		while (skypeCursor.moveToNext()) {
			final int type = skypeCursor
					.getInt(skypeCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
			final String contactName = skypeCursor.getString(skypeCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			final String imName = skypeCursor.getString(skypeCursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));


			if(type == ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE){
				if(imName != null && imName.trim().length() > 0){
					final String skypeName = imName.trim();

					if(!addedSkypeNames.contains(skypeName)){
						dataList.add(new Pair<String, String>(contactName, skypeName));
						addedSkypeNames.add(skypeName);
					}
				}
			}        
		}

		skypeCursor.close();

		return dataList;
	}

	public List<Pair<String, String>> pairListFromObjectList(final ContentResolver cr, ArrayList<EventContact> contactList) {
		Logger.i(TAG, "Generating Config File DataList");

		final List<Pair<String, String >> dataList = new ArrayList<Pair<String,String>>(); 

		for (EventContact contact : contactList) {
			dataList.add(new Pair<String, String>(contact.getContactName(), contact.getContactDevice() + " / " + contact.getContactAction()));
		}
		
		return dataList;
	}

}
