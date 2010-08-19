package edu.stanford.junction.sample.partyware.widgets;


public class PlaylistMediaView{

	public PlaylistMediaView(){
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)(getContext().getSystemService(
													 Context.LAYOUT_INFLATER_SERVICE));
			v = vi.inflate(R.layout.media_item, null);
		}
		JSONObject o = getItem(position);
		if (o != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			String caption = o.optString("caption");
			tt.setText(caption);

			Date d = new Date(o.optLong("time") * 1000);
			String time = dateFormat.format(d); 
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			bt.setText(" " + time);

			final ImageView icon = (ImageView)v.findViewById(R.id.icon);
			icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
			final String url = o.optString("thumbUrl");

			icon.setImageResource(R.drawable.ellipsis);
			mgr.getBitmap(url, new BitmapHandler(icon));
		}
		return v;
	}


}