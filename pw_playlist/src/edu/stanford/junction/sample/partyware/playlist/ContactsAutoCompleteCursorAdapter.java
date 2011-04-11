package edu.stanford.junction.sample.partyware.playlist;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ImageView;

import android.widget.LinearLayout;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;


public class ContactsAutoCompleteCursorAdapter extends CursorAdapter implements Filterable {

    protected TextView mName;
	protected ContentResolver mContent;

	static final String[] NAME_PROJECTION = new String[] {
        Contacts._ID,
        Contacts.DISPLAY_NAME
    };


    public ContactsAutoCompleteCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContent = context.getContentResolver();
    }

    public ContactsAutoCompleteCursorAdapter(Context context) {
		this(context, context.getContentResolver().query(
				 Contacts.CONTENT_URI, NAME_PROJECTION,
				 Contacts.DISPLAY_NAME + " IS NOT NULL", 
				 null,
				 Contacts.DISPLAY_NAME));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LinearLayout ret = new LinearLayout(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        mName = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        ret.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontal = new LinearLayout(context);
        horizontal.setOrientation(LinearLayout.HORIZONTAL);

        // you can even add images to each entry of your autocomplete fields
        // this example does it programmatically using JAVA, but the XML analog is very similar
        ImageView icon = new ImageView(context);

        int nameIdx = cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME);

        String name = cursor.getString(nameIdx);

        mName.setText(name);

        horizontal.addView(icon, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        ret.addView(mName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ret.addView(horizontal, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        return ret;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameIdx = cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME);
        String name = cursor.getString(nameIdx);
		// notice views have already been inflated and layout has already been set so all you need to do is set the data
		((TextView) ((LinearLayout) view).getChildAt(0)).setText(name);
	}

	@Override
	public String convertToString(Cursor cursor) {
		// this method dictates what is shown when the user clicks each entry in your autocomplete list
		int col = cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME);
		return cursor.getString(col);
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		// this is how you query for suggestions
		// notice it is just a StringBuilder building the WHERE clause of a cursor which is the used to query for results
		if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
		StringBuilder buf = null;
		String[] args = null;
		if (constraint != null) {
			buf = new StringBuilder();
			buf.append(Contacts.DISPLAY_NAME + " IS NOT NULL AND ");
			buf.append("UPPER(");
			buf.append(Contacts.DISPLAY_NAME);
			buf.append(") GLOB ?");
			args = new String[] { constraint.toString().toUpperCase() + "*" };
		}
		return mContent.query(Contacts.CONTENT_URI, NAME_PROJECTION, 
							  buf == null ? null : buf.toString(), args, 
							  Contacts.DISPLAY_NAME);
	}



	public static class EmailAdapter extends ContactsAutoCompleteCursorAdapter{
		public EmailAdapter(Context context) {
			super(context, context.getContentResolver().query(
					  Contacts.CONTENT_URI, 
					  NAME_PROJECTION,
					  Contacts.DISPLAY_NAME + " IS NOT NULL AND " + 
					  Contacts.DISPLAY_NAME + " LIKE '%@%' ",
					  null,
					  Contacts.DISPLAY_NAME));
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint){
			// this is how you query for suggestions
			// notice it is just a StringBuilder building the WHERE clause of a cursor which is the used to query for results
			if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
			StringBuilder buf = null;
			String[] args = null;
			if (constraint != null) {
				buf = new StringBuilder();
				buf.append(Contacts.DISPLAY_NAME + " IS NOT NULL AND ");
				buf.append(Contacts.DISPLAY_NAME + " LIKE '%@%' AND ");
				buf.append("UPPER(");
				buf.append(Contacts.DISPLAY_NAME);
				buf.append(") GLOB ?");
				args = new String[] { constraint.toString().toUpperCase() + "*" };
			}
			return mContent.query(Contacts.CONTENT_URI, NAME_PROJECTION, 
								  buf == null ? null : buf.toString(), args, 
								  Contacts.DISPLAY_NAME);
		}
	}



	public static class NameAdapter extends ContactsAutoCompleteCursorAdapter{
		public NameAdapter(Context context) {
			super(context, context.getContentResolver().query(
					  Contacts.CONTENT_URI, 
					  NAME_PROJECTION,
					  Contacts.DISPLAY_NAME + " IS NOT NULL AND " + 
					  Contacts.DISPLAY_NAME + " NOT LIKE '%@%'",
					  null,
					  Contacts.DISPLAY_NAME));
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint){
			// this is how you query for suggestions
			// notice it is just a StringBuilder building the WHERE clause of a cursor which is the used to query for results
			if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
			StringBuilder buf = null;
			String[] args = null;
			if (constraint != null) {
				buf = new StringBuilder();
				buf.append(Contacts.DISPLAY_NAME + " IS NOT NULL AND ");
				buf.append(Contacts.DISPLAY_NAME + " NOT LIKE '%@%' AND ");
				buf.append("UPPER(");
				buf.append(Contacts.DISPLAY_NAME);
				buf.append(") GLOB ?");
				args = new String[] { constraint.toString().toUpperCase() + "*" };
			}
			return mContent.query(Contacts.CONTENT_URI, NAME_PROJECTION, 
								  buf == null ? null : buf.toString(), args, 
								  Contacts.DISPLAY_NAME);
		}
	}

}
