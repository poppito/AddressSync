package com.noni.embryio;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class FirstTab extends Fragment implements OnClickListener, UpdateableFragment {
	
	private ListView syncStatusList;
	private static final String TAG = "First_Tab";
	private static String testURL1 = Constants.SERVERURL + "getsyncedcontacts";
	private static String testURL2 = Constants.SERVERURL + "getsyncstatus";
	private int TIMEOUT_MILLSEC = 10000;
	private ArrayList<String> listViewContents = new ArrayList<String>();
	private JSONArray values = new JSONArray();
	private ArrayList<String> allPhoneContacts, unsyncedphoneContacts;
	private Button uselectall, udeselectall, usyncme;
	private HttpMethodTask HMT;
	private HttpMethodTask2 HMT2;
	private InsertUnsyncedContacts IUC;
	private ProgressDialog mProgressDialog;
	
	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (listViewContents != null){ listViewContents.clear(); }
		if (unsyncedphoneContacts != null) {unsyncedphoneContacts.clear(); }
		if ((HMT != null)&&(HMT.getStatus() == AsyncTask.Status.FINISHED))
		{
			HMT = new HttpMethodTask(getActivity().getApplicationContext());
			HMT.execute(testURL1);
		}
	}
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	 
	        View rootView = inflater.inflate(R.layout.get_sync_status, container, false);
			HMT = new HttpMethodTask(getActivity().getApplicationContext());
			if ((HMT != null) && (HMT.getStatus() != AsyncTask.Status.RUNNING))
			{
				
				HMT.execute(testURL1);
			}
			syncStatusList = (ListView)rootView.findViewById(R.id.listcontacts1);
			uselectall = (Button)rootView.findViewById(R.id.uselectall);
			udeselectall = (Button)rootView.findViewById(R.id.udeselectall);
			usyncme = (Button)rootView.findViewById(R.id.usyncme);
			uselectall.setOnClickListener(this);
			usyncme.setOnClickListener(this);
			udeselectall.setOnClickListener(this);
	        return rootView;
	
	 }
	 
		public class HttpMethodTask extends AsyncTask<String, Void, String> {

			private int totalContactCount = 0, unmatchedContactCount = 0;
			private Context context = getActivity().getApplicationContext();
			public HttpMethodTask(Context context_) {
				// TODO Auto-generated constructor stub
				context = context_;
					
			}

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setTitle("Retrieving synced contacts from embry.io");
				mProgressDialog.setMessage("Just a second..");
				mProgressDialog.setCancelable(false);
				mProgressDialog.setIndeterminate(true);
				mProgressDialog.show();
			}
			
			@Override
			protected String doInBackground(String...url) {
				
				String resp = "";
				Header[] responseHeaders = null;
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
				HttpGet request = new HttpGet(url[0]);
				MyHttpClient Client = LogonClass.Client;
				Client.putContext(context);
				try
					{
						request.setHeader("syncstatus", "True");
						HttpResponse response = Client.execute(request);
						resp = EntityUtils.toString(response.getEntity());
						responseHeaders = response.getAllHeaders();
					}
				catch (Exception e)
					{
					//Log.e(TAG, "HTTP request didn't work!");
					resp = "none is righteous";
					}
				Log.e(TAG, "fleh returned "+ resp);
				return resp;
			}
			
			@Override
			public void onPostExecute(String s)
			{
				if (mProgressDialog != null)
				{
					mProgressDialog.dismiss();
				}
				try
				{
					JSONTokener tokener = new JSONTokener(s);
					values = (JSONArray) tokener.nextValue();
				//	Log.e(TAG, "JSON array response is " + values.toString());
					for (int i=0; i<values.length(); i++)
					{
						JSONObject obj = new JSONObject();
						obj = values.getJSONObject(i);
						if (obj.getString("contact_name") != null)
						{
							listViewContents.add(obj.getString("contact_name"));
						}
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (ClassCastException e)
				{
					e.printStackTrace();
				}
				
				allPhoneContacts = MainActivity.getPhoneContactNames(getActivity().getContentResolver());
				totalContactCount = allPhoneContacts.size();
			//	Log.e(TAG, "contacts on phone " + allPhoneContacts.size() + " and contacts on server are " + listViewContents.size());
				unsyncedphoneContacts = MainActivity.getUnsyncedList(listViewContents, allPhoneContacts);
				Collections.sort(unsyncedphoneContacts);
				ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_multiple_choice, unsyncedphoneContacts);
				syncStatusList.setAdapter(mArrayAdapter);
				syncStatusList.setChoiceMode(syncStatusList.CHOICE_MODE_MULTIPLE);
				//Log.e(TAG, "unmatched contacts are " + unsyncedphoneContacts.toString());
			}
		}


		@Override
		public void onClick(View v) {
			switch (v.getId())
			{				
			case R.id.uselectall:
				for ( int i=0; i <syncStatusList.getCount(); i++) {
					syncStatusList.setItemChecked(i, true);
					}
				break;
				
			case R.id.udeselectall:
				for ( int i=0; i <syncStatusList.getCount(); i++) {
					syncStatusList.setItemChecked(i, false);
					}
				break;
			case R.id.usyncme:
				Log.v(TAG, "sync me button pressed!");
				ArrayList <String> selectedItemList = new ArrayList<String>();
				SparseBooleanArray checked = syncStatusList.getCheckedItemPositions();
				for (int i = 0; i < checked.size(); i++){
			    
					int key = checked.keyAt(i);
					boolean value  = checked.get(key);
					if (value)
				    {
				    	Log.v(TAG, "adding " + (String)syncStatusList.getItemAtPosition(key));
				    	selectedItemList.add((String) syncStatusList.getItemAtPosition(key));
				    }
				}
				JSONArray sendArray = new JSONArray();
		    	try
		    	{
			    	for (int j=0; j<selectedItemList.size(); j++)
			    	{
			    		
			    		Log.v(TAG, "selected name is " + selectedItemList.get(j));
			    		JSONObject contactName = new JSONObject();
						contactName.put("contactName", selectedItemList.get(j));
						sendArray.put(contactName);
			    	}
			    	Log.v(TAG, "send array is " + sendArray.toString());
			    	JSONObject sendObj = new JSONObject();
			    	sendObj.put("contacts", sendArray.toString());
			   // 	sendObj.put("username", "haha");
			    	sendArray = null;
			    	HMT2 = new HttpMethodTask2(getActivity().getApplicationContext(), sendObj);
			    	if ((HMT2 != null) && (HMT2.getStatus() != AsyncTask.Status.RUNNING))
					{
				    	HMT2.execute(testURL2);
					}
			    	
		    	}
			    catch (JSONException e)
			    {
			    	e.printStackTrace();
			    }
		    }
	}
		
		
	public class HttpMethodTask2 extends AsyncTask<String, Void, String> {

		private Context context;
		private JSONObject jsonObject;
		
		public HttpMethodTask2(Context context_, JSONObject jsonObject) {
			// TODO Auto-generated constructor stub
				context = context_;
				this.jsonObject = jsonObject;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setTitle("Retrieving your contacts from embry.io");
			mProgressDialog.setMessage("Just a second..");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... url)  {
			// TODO Auto-generated method stub
			String resp = "";
			String username = "";
	    	//Log.e(TAG, "send object is " + jsonObject.toString());
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLSEC);
			MyHttpClient Client = LogonClass.Client;
			Client.putContext(context);
			HttpPost request = new HttpPost(url[0]);
			try 
			{	
				request.setEntity(new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8")));
				HttpResponse response = Client.execute(request);
				resp = EntityUtils.toString(response.getEntity());
			} 
			
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.v(TAG, "response is " + resp.toString());
			return resp;
		}
		
		public void onPostExecute(String s)
		{
			if (mProgressDialog != null)
			{
				mProgressDialog.dismiss();
			}
			try
			{
				JSONTokener tokener = new JSONTokener(s);
				JSONArray detailsValues = (JSONArray) tokener.nextValue();
				Log.v(TAG, "There are " + detailsValues.length() + " unsynced contacts");
				Log.v(TAG, "detail values is " + detailsValues.toString());
				Context context_ = getActivity().getApplicationContext();
				ContentResolver cr_ = getActivity().getContentResolver();
				IUC = new InsertUnsyncedContacts(context_, cr_, detailsValues);
				if ((IUC != null) && (IUC.getStatus() != AsyncTask.Status.RUNNING))
				{
					IUC.execute();
				}
			}
			
			catch (JSONException e)
			{
				e.printStackTrace();
				Log.v(TAG, "json exception");
			}
			catch (ClassCastException e)
			{
				e.printStackTrace();
				Log.v(TAG, "class cast exception");
			}
			
		}
		
	}
	
	
	public class InsertUnsyncedContacts extends AsyncTask<Void, String, String> {
		
		private  ContentResolver cr;
		private  Context context;
		private JSONArray values;
		private ProgressDialog progressDialog = new ProgressDialog(getActivity());
		private int prog;
		
				
		public InsertUnsyncedContacts (Context context, ContentResolver cr, JSONArray values)
		{
			
			Log.v(TAG, "constructor for IUC reached");
			this.context = context;
			this.cr = cr;
			this.values = values;
			prog = values.length();
		}
			
		
		@Override
		protected void onPreExecute()
		{
			progressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setIndeterminate(false);
			progressDialog.setMax(values.length());
			progressDialog.setTitle("inserting unsynced contact(s) into your phone from embry.io");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... Void) 
		{
			
			try 
			{
				int j = values.length();
				Log.v(TAG, "values length is " + values.length());
				for (int i=0; i<(values.length()); i++)
				{
					
					publishProgress("Inserting " + i + " of " + j + " contacts");
					int deleted = 0;
					JSONObject obj = new JSONObject();
					obj = values.getJSONObject(i);
					
						if (obj.getString("contact_name") != null)
						
						{
							{	
								String accountName = null;
								String accountType = null;
								ArrayList<ContentProviderOperation> Ops = new ArrayList<ContentProviderOperation>();
								
								ContentProviderOperation.Builder op;

								Log.v(TAG, "names list check passed");
								if (( obj.getString("accountName") != null) && (obj.getString("accountType") != null))
								{
									accountName = obj.getString("accountName");
									accountType = obj.getString("accountType");
									Log.v(TAG, "account name is " + accountName.toString() + " account type is " + accountType.toString());
									
									op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
									.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountName)
									.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountType)
									.withValue(ContactsContract.RawContacts.DELETED, 0);
								}
								
								else
								{
									op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
										.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
										.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
										.withValue(ContactsContract.RawContacts.DELETED, 0);
										
								}
			
								Ops.add(op.build());
													
								op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
								.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, obj.getString("contact_name"));
												
								Ops.add(op.build());
	
												
								if (obj.getString("emailAddresses") != null)
								{		
									JSONObject emailAddresses = obj.getJSONObject("emailAddresses");
									Iterator iter = emailAddresses.keys();
									
									while(iter.hasNext())
									{
										String key = (String)iter.next();
										String value = emailAddresses.getString(key);
										{
											op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
											.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
											.withValue(ContactsContract.CommonDataKinds.Email.TYPE, value)
											.withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, key);
										}
										
											Ops.add(op.build());
									}
													    
								}
		
								if (obj.getString("IMs") != null)
								{		
									JSONObject IMs = obj.getJSONObject("IMs");
									Iterator iter = IMs.keys();
									
									while(iter.hasNext())
									{
										String key = (String)iter.next();
										String value = IMs.getString(key);
										
										Log.v(TAG, "IM type " + key + " Protocol " + value);
										
										{
											op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
											.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
											.withValue(ContactsContract.CommonDataKinds.Im.DATA1, key)
											.withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, value);
											
										}
										
											Ops.add(op.build());
									}
													    
								}
								
								
								if (obj.getString("Notes") != null)
								{		
									JSONObject Notes = obj.getJSONObject("Notes");
									Iterator iter = Notes.keys();
									
									while(iter.hasNext())
									{
										String key = (String)iter.next();
										String value = Notes.getString(key);
										
										{
											
											Log.v(TAG, "Note is " + key);
											op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
											.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
											.withValue(ContactsContract.CommonDataKinds.Note.NOTE, key);
											
										}
										
											Ops.add(op.build());
									}
													    
								}
								
								
								if (obj.getString("Address") != null)
								{		
									JSONObject Address = obj.getJSONObject("Address");
									Iterator iter = Address.keys();
									
									while(iter.hasNext())
									{
										String key = (String)iter.next();
										String value = Address.getString(key);
										
										{
											
											Log.v(TAG, "Address " + key + " address type " + value);
											
											op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
											.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
											.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, value)
											.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, key);
		
										}
										
											Ops.add(op.build());
									}
													    
								}
								
								if (obj.getString("website") != null)
								{		
									JSONObject website = obj.getJSONObject("website");
									Iterator iter = website.keys();
									
									while(iter.hasNext())
									{
										String key = (String)iter.next();
										String value = website.getString(key);
										
										
										{
											Log.v(TAG, "website is " + key);
											
											op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
											.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
											.withValue(ContactsContract.CommonDataKinds.Website.URL, key);
										}
										
											Ops.add(op.build());
									}
								}
		
									if (obj.getString("Org") != null)
									{		
										JSONObject Org = obj.getJSONObject("Org");
										Iterator iter = Org.keys();
										
										while(iter.hasNext())
										{
											String key = (String)iter.next();
											String value = Org.getString(key);
											
											Log.v(TAG, "Org is " + key + " Title  " + value);
											{
												
												op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
												.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
												.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
												.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, value)
												.withValue(ContactsContract.CommonDataKinds.Organization.TITLE, key);
												
											}
												Ops.add(op.build());
										}			    
									}
									
								if (obj.getString("phoneNumbers") != null)
								{
									JSONObject phoneNumbers = obj.getJSONObject("phoneNumbers");
									Iterator iter = phoneNumbers.keys();
									while (iter.hasNext())
									{
										String key = (String) iter.next();
										String value = phoneNumbers.getString(key);
										op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
										.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, value)
										.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, key);
										op.withYieldAllowed(true);
										Ops.add(op.build());
									}
								}
												
								try 
									{
										if (Ops != null)			
										{
											ContentProviderResult[] result  = cr.applyBatch(ContactsContract.AUTHORITY, Ops);
											Log.v(TAG, "Content provider result is " + result[0]);
										}
									}
											
								catch (Exception e )
									{
										Log.v(TAG, "contact add didn't work! ");
										e.printStackTrace();
										
									}			
						}
					}
				}
			}
				 
					catch (JSONException e) 
					{
						// TODO Auto-generated catch block
						Log.v(TAG, "json exception before insert");
						e.printStackTrace();
					}
								
					//catch (NullPointerException e)
					{
					//	Log.v(TAG, "null pointer exception before insert");
					//	e.printStackTrace();
					}
				return null;
				}
		
		
		protected void onProgressUpdate(String... progress) 
		{   
			Log.v(TAG, "progress is at " + progress[0]);
			super.onProgressUpdate(progress[0]);
			progressDialog.setMessage(progress[0]);
			
		}
		
		public void onPostExecute(String s)
		{
			if (progressDialog != null)
			{
				progressDialog.dismiss();
			}
			update();
		}
		
				
	}
}
