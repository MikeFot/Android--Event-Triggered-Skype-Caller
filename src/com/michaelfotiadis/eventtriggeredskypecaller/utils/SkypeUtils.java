package com.michaelfotiadis.eventtriggeredskypecaller.utils;

import com.michaelfotiadis.eventtriggeredskypecaller.containers.SkypeAction;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class SkypeUtils {
	
	private final String TAG = "SKYPE_UTILS";

	public String getSkypeID(Context mContext, String contactID) {
		Log.i(TAG, "getContactNumber");

		String returnID = null;

		ContentResolver cr = mContext.getContentResolver();
		Cursor skype = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.CONTACT_ID
				+ " = " + contactID, null, null);

		while (skype.moveToNext()) {

			int type = skype
					.getInt(skype
							.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
			String imName = skype.getString(skype
					.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

			switch (type) {
			case ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE:
				Log.d(TAG, "contactID: " + contactID + " type: " + type
						+ " imName: " + imName);

				returnID = imName;

				break;

			default:
				Log.v(TAG, "Other numbers: " + imName);
				break;

			}

		}

		return returnID;
	}

	// Install the Skype client through the market: URI scheme.

	public void goToMarket(Context myContext) {
		Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
		Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myContext.startActivity(myIntent);

		return;
	}
	
	/**
	 * Determine whether the Skype for Android client is installed on this device
	 * @param myContext
	 * @return true if installed, false if not
	 */
	public boolean isSkypeClientInstalled(Context myContext) {
		PackageManager myPackageMgr = myContext.getPackageManager();
		try {
			myPackageMgr.getPackageInfo("com.skype.raider",
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			Logger.e(TAG, e.getLocalizedMessage());
			return (false);
		}
		return (true);
	}
	
	
	public void startSkypeAction(Context context, String userName, String skypeAction) {
		
		if (skypeAction.equals(SkypeAction.CALL.getString())) {
			Logger.d(TAG, "Starting Call");
			startSkypeSimpleCall(context, userName);
		} 	else 	if (skypeAction.equals(SkypeAction.VIDEO_CALL.getString())) {
			Logger.d(TAG, "Starting Video Call");
			startSkypeVideoCall(context, userName);
		}  	else 	if (skypeAction.equals(SkypeAction.CHAT.getString())) {
			Logger.d(TAG, "Starting Chat");
			startSkypeChat(context, userName);
		}
		
	}
	
	public void startSkypeChat(Context context, String userName) {
		if (userName == null) {return;}
		Intent skypeVideo = new Intent("android.intent.action.VIEW");
		skypeVideo.setData(Uri.parse("skype:" + userName + "?chat"));
		context.startActivity(skypeVideo);	
	}

	public void startSkypeSimpleCall(Context context, String userName) {
		if (userName == null) {return;}
		Intent skypeCall = new Intent("android.intent.action.VIEW");
		skypeCall.setData(Uri.parse("skype:" + userName + "?call"));
		context.startActivity(skypeCall);
	}

	public void startSkypeVideoCall (Context context, String username) {
		if (username == null) {return;}
		Intent skypeVideo = new Intent("android.intent.action.VIEW");
		skypeVideo.setData(Uri.parse("skype:" + username + "?call&video=true"));
		context.startActivity(skypeVideo);
	}
	
}
