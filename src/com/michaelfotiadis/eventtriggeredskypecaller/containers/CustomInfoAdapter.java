package com.michaelfotiadis.eventtriggeredskypecaller.containers;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomInfoAdapter extends BaseAdapter {

    private Context mContext;
    private List<Pair<String, String>> mData;

    public CustomInfoAdapter(Context context, List<Pair<String, String>> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
       final View twoLineListItem;

        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (View) inflater.inflate(android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = convertView;
        }
		
		final TextView name = (TextView) twoLineListItem.findViewById(android.R.id.text1);
		final TextView skype = (TextView) twoLineListItem.findViewById(android.R.id.text2);
		
		name.setText(getItem(position).first);
		skype.setText(getItem(position).second);
		
		return twoLineListItem;
	}
}
