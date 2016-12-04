package com.noni.embryio;

import java.util.ArrayList;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DuplicateDialogFragment extends DialogFragment implements OnClickListener {
	
	public static String TAG = "DuplicateDialogFragment";
	public ArrayList<String> mergeAllPhoneContacts, mergeSyncedContacts;
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		            Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialogfragment, container, false);
		View tv = v.findViewById(R.id.duplicateDialog);
		((TextView)tv).setText("Merge duplicates?");
		Button yesButton, noButton;
		yesButton = (Button)v.findViewById(R.id.yesButton);
		noButton = (Button)v.findViewById(R.id.noButton);
		yesButton.setOnClickListener(this);
		noButton.setOnClickListener(this);
		return v;
	
	}
	
	public void getDuplicates(ArrayList<String> duplicates)
	{
		mergeAllPhoneContacts = duplicates;
	}
	
	public void getSyncedContacts(ArrayList<String> syncedContacts)
	{
		mergeSyncedContacts = syncedContacts;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case  R.id.yesButton:
			{
				Log.v(TAG, "duplicates are " + mergeAllPhoneContacts.toString() + "synced " + mergeSyncedContacts.toString());
				Intent i = new Intent(getActivity(), DuplicateMerge.class);
				i.putExtra("duplicateContacts", mergeAllPhoneContacts);
				i.putExtra("syncedContacts", mergeSyncedContacts);
				startActivity(i);
				break;
			}
			
			case R.id.noButton:
			{
				dismiss();
				break;
			}
		}
		
	}
		
	
		public static DuplicateDialogFragment DuplicateDialogFragment()
		{
			return new DuplicateDialogFragment();
		}
}
