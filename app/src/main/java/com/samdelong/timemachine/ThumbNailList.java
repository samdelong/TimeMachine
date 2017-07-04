package com.samdelong.timemachine;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
		import android.view.ViewGroup;
		import android.widget.ArrayAdapter;
		import android.widget.ImageView;
		import android.widget.TextView;

import java.util.List;

public class ThumbNailList extends ArrayAdapter<String>{

	private final Activity context;
	private final String web[];
	private final ImageView imageId[];
	public ThumbNailList(Activity context,
	                  String web[], ImageView imageId[]) {
		super(context, R.layout.list_single, web);
		this.context = context;
		this.web = web;
		this.imageId = imageId;

	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.list_single, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
		imageId[position] = (ImageView) rowView.findViewById(R.id.img);
		txtTitle.setText(web[position]);
		return rowView;
	}
}
