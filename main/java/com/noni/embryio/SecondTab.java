package com.noni.embryio;
import android.support.v4.app.Fragment;

//import android.app.ActionBar;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.noni.embryio.R.string;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SecondTab extends Fragment implements OnClickListener, UpdateableFragment {
	
	private static String TAG = "SecondTab";
	private Button selectall, deselectall, syncme;
	private ListView listContacts;
	private ArrayList<String> selectedItemList = new ArrayList<String>();
	private ArrayList<String> displayList = new ArrayList<String>();
	private ArrayList<String> allPhoneContacts = new ArrayList<String>();
	private ArrayList<String> syncedContacts = new ArrayList<String>();
	private final String testURL = Constants.SERVERURL + "getcontacts";
	private final String testURL2 = Constants.SERVERURL+ "getsyncedcontacts";
	private final static int TIMEOUT_MILLSEC = 1000;
	private CreateContactsContent CCC;
	private HttpMethodTask HMT;
	private HttpMethodTask2 HMT2;
	private ArrayAdapter mArrayAdapter;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		            Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_get_contacts, container, false);
		listContacts = (ListView)rootView.findViewById(R.id.listcontacts);
		Button selectall = (Button)rootView.findViewById(R.id.selectall);
		Button deselectall = (Button)rootView.findViewById(R.id.deselectall);
		Button syncme = (Button)rootView.findViewById(R.id.syncme);
		selectall.setOnClickListener(this);
		deselectall.setOnClickListener(this);
		syncme.setOnClickListener(this);
		//selectall.setEnabled(false);
		//deselectall.setEnabled(false);
		//syncme.setEnabled(false);
		return rootView;
		
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		HMT2 = new HttpMethodTask2(getActivity().getApplicationContext());
		if (HMT2.getStatus() != AsyncTask.Status.RUNNING)
		{
			HMT2.execute(testURL2);
		}
	}
	
	@Override
	public void update() {
		// TODO Auto-generated method stub
		allPhoneContacts = MainActivity.getPhoneContactNames(getActivity().getContentResolver());
		if ((HMT2 != null) && (HMT2.getStatus() == AsyncTask.Status.FINISHED))
		{
			HMT2 = new HttpMethodTask2(getActivity().getApplicationContext());
			HMT2.execute(testURL2);
		}
	}

		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case (R.id.selectall):
				for ( int i=0; i <listContacts.getCount(); i++) {
					listContacts.setItemChecked(i, true);
					}
				break;
			case(R.id.deselectall):
				for ( int i=0; i <listContacts.getCount(); i++) {
					listContacts.setItemChecked(i, false);
					}
				break;
				
			case (R.id.syncme):
			
				SparseBooleanArray checked = listContacts.getCheckedItemPositions();
				for (int i = 0; i < checked.size(); i++)
				{
					int key = checked.keyAt(i);
					boolean value  = checked.get(key);
					if (value)
				    {
						if (selectedItemList.contains(listContacts.getItemAtPosition(key)))
						{
							Log.v(TAG, listContacts.getItemAtPosition(key).toString() + " is a duplicate!!!!");
							
						}	
						selectedItemList.add((String) listContacts.getItemAtPosition(key));
				    }
					else
				    {
				    
				    }
				}
				//(TAG, "added " + selectedItemList.size() + " contacts to sync queue");
				CCC = new CreateContactsContent(getActivity().getApplicationContext(), getActivity().getContentResolver(), selectedItemList);
				if ((CCC != null)&&(CCC.getStatus() != AsyncTask.Status.RUNNING))
				{
					CCC.execute();
				}
			}
		}

			
		
		public class HttpMethodTask2 extends AsyncTask<String, Void, String> {

			private int totalContactCount = 0, unmatchedContactCount = 0;
			private ProgressDialog mProgressDialog1;
			private Context context;
			public HttpMethodTask2(Context context_) {
				// TODO Auto-generated constructor stub
				context = context_;
			}

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				mProgressDialog1 = new ProgressDialog(getActivity());
				mProgressDialog1.setProgress(ProgressDialog.STYLE_SPINNER);
				mProgressDialog1.setTitle("Retrieving contacts from your Phone");
				mProgressDialog1.setMessage("Just a second.");
				mProgressDialog1.setCancelable(false);
				mProgressDialog1.setIndeterminate(true);
				mProgressDialog1.show();
			}
			
			@Override
			protected String doInBackground(String...url) {
				
				if (allPhoneContacts != null)
				{
					allPhoneContacts.clear();
				}
				
				if (syncedContacts != null)
				{
					syncedContacts.clear();
				}
				
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
					
					resp = "none is righteous";
					}
				
				Log.v(TAG, "response is " + resp);
				return resp;
			}
			
			@Override
			public void onPostExecute(String s)
			{
				if (mProgressDialog1 != null)
				{
					mProgressDialog1.dismiss();
				}
				try
				{
					JSONArray values = new JSONArray();
					JSONTokener tokener = new JSONTokener(s);
					values = (JSONArray) tokener.nextValue();
				
					Log.v(TAG, "synced contacts count is " + values.length());
					
					for (int i=0; i<values.length(); i++)
					{
						JSONObject obj = new JSONObject();
						obj = values.getJSONObject(i);
		
						if (obj.getString("contact_name") != null)
						{
							syncedContacts.add(obj.getString("contact_name"));
							
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
				
				allPhoneContacts = MainActivity.getPhoneContactNames(getActivity().getContentResolver()); //gets all contacts except ones marked for deletion
				Log.v(TAG, allPhoneContacts.size() + " all contacts " );
				Log.v(TAG, syncedContacts.size() + " synced contacts");
				displayList = MainActivity.getSyncedList(syncedContacts, allPhoneContacts); //compares synced contacts with unsynced ones to only show unsynced contacts
				int duplicateCount = MainActivity.getDuplicates(syncedContacts, displayList);
				Log.v(TAG, "there are " + syncedContacts.size() + " synced contacts " + displayList.size() + " unsynced contacts");
				if (duplicateCount > 0)
				{
					{
						Toast t = Toast.makeText(context, "There are " + duplicateCount + " duplicate contacts", Toast.LENGTH_SHORT);
						t.show();
					}
					DuplicateDialogFragment newFragment = new DuplicateDialogFragment().DuplicateDialogFragment();
					newFragment.getDuplicates(displayList);
					newFragment.getSyncedContacts(syncedContacts);
					newFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
					newFragment.show(getActivity().getFragmentManager(), "dialog");
				}
				mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, displayList);
				Collections.sort(displayList);
				listContacts.setAdapter(mArrayAdapter);
				listContacts.setChoiceMode(listContacts.CHOICE_MODE_MULTIPLE);
		}
	}
		
		public class HttpMethodTask extends AsyncTask<String, Void, String> {

			private Context context;
			private JSONObject jsonObject = new JSONObject();
			ArrayList<String> selectedItemList;
			private ProgressDialog mProgressDialog2;
			
			public HttpMethodTask(Context context, ArrayList<String> selectedItemList) {
				// TODO Auto-generated constructor stub
					this.context = context;
					this.selectedItemList = selectedItemList;
			}	
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				mProgressDialog2 = new ProgressDialog(getActivity());
				mProgressDialog2.setProgress(ProgressDialog.STYLE_SPINNER);
				mProgressDialog2.setTitle("Sending your contacts to embry.io");
				mProgressDialog2.setMessage("Won't take long.");
				mProgressDialog2.setCancelable(false);
				mProgressDialog2.setIndeterminate(true);
				mProgressDialog2.show();
			}
			
			@Override
			protected String doInBackground(String...url) {
				
				String resp = "";
				String username = "";
				
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
				HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLSEC);
				HttpPost request = new HttpPost(url[0]);
				
				MultipartEntityBuilder MEB = MultipartEntityBuilder.create();
				Charset chars = Charset.forName("UTF-8");
				MEB.setCharset(chars);
				MEB.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				try {
					
					JSONArray contactsArray = new JSONArray();
				
				
					for (int i=0; i<selectedItemList.size(); i++)
					{
						JSONObject contactObj = new JSONObject();
						File getFile = new File(context.getFilesDir().getPath() + "/", selectedItemList.get(i));
						FileBody fb = new FileBody(getFile);
						contactObj.put("contact", selectedItemList.get(i));
						contactsArray.put(contactObj);
						MEB.addPart(selectedItemList.get(i), fb);
						fb = null;
					}

				selectedItemList.clear();
				byte[] contacts = contactsArray.toString().getBytes();
				MEB.addBinaryBody("contactsArray", contacts);
				contactsArray = null;
				
				HttpEntity ent = MEB.build();
				request.setEntity(ent);
				//request.setEntity(new ByteArrayEntity(file.toString().getBytes("UTF8")));
				MyHttpClient Client = LogonClass.Client;
				Client.putContext(context);
				HttpResponse response = Client.execute(request);
				contactsArray = null;
				resp = EntityUtils.toString(response.getEntity());
				}
				catch (Exception e)
					{
						e.printStackTrace();
				
					resp = "none is righteous";
					}
				Log.e(TAG, "response is " + resp);
				return resp;
			}
			
			@Override
			protected void onPostExecute(String s)
			{

				if (mProgressDialog2 != null)
				{
					mProgressDialog2.dismiss();
				}
				String status ="", message = "";
				try {
					JSONObject respObject = new JSONObject(s);
					status = respObject.getString("status");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				update();
				
			}
		}
		
	public class CreateContactsContent extends AsyncTask<String, String, String> {
			
		ArrayList<String> inputArrayList;
		public  ContentResolver cr;
		public  Context context;
		private int countingContacts = 1;
		private ProgressDialog progressDialog;
		private String prog;

	public CreateContactsContent(Context context, ContentResolver cr,  ArrayList<String> inputArrayList)
	{
		
		this.context = context;
		this.cr = cr;
		this.inputArrayList = inputArrayList;
		
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("Preparing contacts data to send to embry.io");
		progressDialog.setMax(inputArrayList.size());
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(false);
		progressDialog.show();
	}
					
	@Override
	protected String doInBackground(String ... String)
	{
	
	try
	{
		
		File file = null;
		String name, accountName = null; String accountType = null;
		
		String[] proj = {RawContacts.DISPLAY_NAME_PRIMARY, RawContacts.CONTACT_ID, RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE, RawContacts.DELETED};
		Cursor C = cr.query(RawContacts.CONTENT_URI, proj, null, null, null);

		while (C.moveToNext())
		{
			name = C.getString(C.getColumnIndex(RawContacts.DISPLAY_NAME_PRIMARY));
			int deleted = C.getInt(C.getColumnIndex(RawContacts.DELETED));
			if (deleted != 1)
			{
				Log.v(TAG, "name is " + name);
			
			
				if ( (C.getString(C.getColumnIndex(RawContacts.ACCOUNT_NAME)) != null) 
						&& (C.getString(C.getColumnIndex(RawContacts.ACCOUNT_TYPE)) != null) )
				{
						accountName = C.getString(C.getColumnIndex(RawContacts.ACCOUNT_NAME));
						accountType = C.getString(C.getColumnIndex(RawContacts.ACCOUNT_TYPE));
				}
			
				JSONObject numObj = new JSONObject();
				JSONObject emObj = new JSONObject();
				JSONObject detailType = new JSONObject();
				JSONObject IMobj = new JSONObject();
				JSONObject orgObj = new JSONObject();
				JSONObject addressObj = new JSONObject();
				JSONObject websiteObj = new JSONObject();
				JSONObject noteObj = new JSONObject();

				if (inputArrayList.contains(name))
				{
					
					prog = "processing contact " + (countingContacts - 1) + " of "  + (inputArrayList.size());
					publishProgress(prog);
					countingContacts++;
					String contactID = C.getString(C.getColumnIndex(RawContacts.CONTACT_ID));
					String[] filter = {contactID};
					
					Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
							null, Phone.CONTACT_ID + "=?", filter, null);
					
					
					while (phoneCursor.moveToNext())
					{
						int type = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));
						
						String numType = ""+type;
						String num = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
						if ((num != null) && (numType != null))
						{
							numObj.put(num, numType);
						}
					}
					detailType.put("phoneNumbers", numObj.toString());
			        Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, Phone.CONTACT_ID + "=?", filter, null);
			       
			        while(emailCursor.moveToNext())
			        {
			        	int type = emailCursor.getInt(emailCursor.getColumnIndex(CommonDataKinds.Email.TYPE));
			        	String emailType = "" + type;
			            String email = emailCursor.getString(emailCursor.getColumnIndex(CommonDataKinds.Email.ADDRESS));
			          
			            if ((email != null) && (emailType != null))
			            {
			            	
			            	emObj.put(email, emailType);
			            }
			        }
			        
			        detailType.put("emailAddresses", emObj.toString());
			        
			        Cursor addressCursor = cr.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,  
							null, CommonDataKinds.StructuredPostal.CONTACT_ID + "=?", filter, null);
			        
			        while(addressCursor.moveToNext())
			        {
			        	String type = addressCursor.getString(addressCursor.getColumnIndex(CommonDataKinds.StructuredPostal.TYPE));
			            String address = addressCursor.getString(addressCursor.getColumnIndex(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
			          
			            if ((address != null) && (type != null))
			            {
			            	
			            	addressObj.put(address, type);
			            }
			        }
		        
		  
			        Cursor genericCursor = cr.query(Data.CONTENT_URI,  
							null, Data.CONTACT_ID + "=?", filter, null);
			        
			        
			        while (genericCursor.moveToNext())
			        {
			        	String organisation = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Organization.DATA1));
			            String title = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Organization.DATA4));
			            String MIMETYPE_ORG = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Organization.MIMETYPE));
			            
			            
			            if ((organisation != null) && (title != null)&&(MIMETYPE_ORG.equals("vnd.android.cursor.item/organization")))
			           {
			            	orgObj.put(organisation, title);
			           }
			        
			       
			            String IMtype = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Im.PROTOCOL));
			            String IMvalue = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Im.DATA1));
			            String MIMETYPE_IM = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Im.MIMETYPE));
			            
				            if ((IMtype != null) && (IMvalue != null) && (MIMETYPE_IM.equals("vnd.android.cursor.item/im")))
				            {
				            	IMobj.put(IMvalue, IMtype);
				            }
			        
				            String websiteVal = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Website.URL));
				            String MIMETYPE_URL = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Website.MIMETYPE));
				            
				            if ((websiteVal != null)&&(MIMETYPE_URL.equals("vnd.android.cursor.item/website")))
				            {
				            	websiteObj.put(websiteVal, "website");
				            }
			            
			        
				            String notesVal = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Note.NOTE));
				            String MIMETYPE_NOTE = genericCursor.getString(genericCursor.getColumnIndex(CommonDataKinds.Note.MIMETYPE));
				            
				            if ((notesVal != null) && (MIMETYPE_NOTE.equals("vnd.android.cursor.item/note")))
				            {
				            	noteObj.put(notesVal, "Note");
				            }
				        }
		            
		        
			       detailType.put("Address", addressObj.toString());
			       detailType.put("Note", noteObj.toString()); 
			       detailType.put("website", websiteObj.toString());
			       detailType.put("Organisation", orgObj.toString());
			       detailType.put("IM", IMobj.toString());
			       detailType.put("accountName", accountName);
			       detailType.put("accountType", accountType);
			       genericCursor.close();
			       addressCursor.close();
			       emailCursor.close();
			       phoneCursor.close();
		        
			    	String fileContent = detailType.toString();
				
					FileOutputStream FOS = null;
						try 
						{
							FOS = context.openFileOutput(name, context.MODE_PRIVATE);
						
							try
							{
								OutputStreamWriter OSW = new OutputStreamWriter(FOS);
								OSW.write(fileContent);
								OSW.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
						catch (FileNotFoundException e1) 
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
				} 
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	
		return prog;
				
	}
	
	
	protected void onProgressUpdate(String ... progress)
	{
		Log.e(TAG, "progress is being called? " + progress[0]);
		super.onProgressUpdate(progress);
		progressDialog.setMessage(progress[0]);
	}
	
	
	public void onPostExecute(String s)
	{
		if (progressDialog != null)
		{
			progressDialog.dismiss();
		}
		
		HMT = new HttpMethodTask(getActivity().getApplicationContext(), selectedItemList);
		if (HMT.getStatus() != AsyncTask.Status.RUNNING)
		{
			HMT.execute(testURL);
		}
		
	}
}
}