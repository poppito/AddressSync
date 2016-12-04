package com.noni.embryio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.noni.embryio.FirstTab.HttpMethodTask;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

public class ThirdTab  extends Fragment implements OnClickListener , UpdateableFragment {

	public ListView embryioContacts;
	public Button selectall, deselectall, unsync;
	public ListView unsyncStatusList;
	public Button retrieveContacts, updatesAvailable;
	public static final String TAG = "ThirdTab";
	public static String testURL1 = Constants.SERVERURL + "getsyncedcontacts";
	public static String testURL2 = Constants.SERVERURL + "unsynccontacts";
	public int TIMEOUT_MILLSEC = 10000;
	public  ArrayAdapter<String> mArrayAdapter = null;
	public ArrayList<String> listViewContents = new ArrayList<String>();
	public JSONObject syncStatus = null;
	public JSONArray values = null;
	private ArrayList<String> removePhoneContacts = new ArrayList<String>();
	public HttpMethodTask1 HTM1;
	public HttpMethodTask2 HTM2;
	public ProgressDialog mProgressDialog1;
	public ProgressDialog mProgressDialog2;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	public JSONArray createJsonString(ArrayList<String> listofdetails)
	{
		
		JSONArray innerArray = new JSONArray();
		
			for (int i = 0; i<listofdetails.size(); i++)
			{
				
				try 
				{
						
					JSONObject contact = new JSONObject();
					contact.put("contact_name", listofdetails.get(i));
					innerArray.put(contact);
					
				} 
				catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					innerArray = null;
					e.printStackTrace();
				}
				
			}
	Log.e(TAG, "this is the JSON array" + innerArray.toString());
	return innerArray;
	}
	
	public JSONObject getObject(JSONArray jsonArray)
	{
		String usernameString = "haha";
		JSONObject outerJSONObject = new JSONObject();
		try {
			outerJSONObject.put("contacts", jsonArray);
			outerJSONObject.put("username", usernameString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outerJSONObject;
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		            Bundle savedInstanceState) {
		 
				Context context = getActivity().getApplicationContext();
				HTM1 = new HttpMethodTask1(context);
				if ((HTM1.getStatus() != AsyncTask.Status.RUNNING))
				{
					HTM1.execute(testURL1);
					Log.e(TAG, "HTM1 called from on create view");
				}
		        View rootView = inflater.inflate(R.layout.third_tab, container, false);
		        unsyncStatusList = (ListView)rootView.findViewById(R.id.embryiocontacts);
		        selectall = (Button) rootView.findViewById(R.id.eselectall);
		        selectall.setOnClickListener(this);
		        deselectall = (Button) rootView.findViewById(R.id.edeselectall);
		        deselectall.setOnClickListener(this);
		        unsync = (Button) rootView.findViewById(R.id.unsyncme);
		        unsync.setOnClickListener(this);
		        return rootView;
		
		 }
	 
	 
		public class HttpMethodTask1 extends AsyncTask<String, Void, String> {

			private Context context;
			private JSONObject jsonObject;
			public HttpMethodTask1(Context context) {
				// TODO Auto-generated constructor stub
				this.context = context;
			}
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				mProgressDialog1 = new ProgressDialog(getActivity());
				mProgressDialog1.setProgress(ProgressDialog.STYLE_SPINNER);
				mProgressDialog1.setTitle("Processing...");
				mProgressDialog1.setMessage("Please wait.");
				mProgressDialog1.setCancelable(false);
				mProgressDialog1.setIndeterminate(true);
				mProgressDialog1.show();
			}
			
			@Override
			protected String doInBackground(String...url) {
				
				String resp = "";
				org.apache.http.Header[] responseheaders = null;
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
				HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLSEC);
				HttpGet request = new HttpGet(url[0]);
				MyHttpClient Client = LogonClass.Client;
				Client.putContext(context);
				try
					{
						request.setHeader("syncstatus", "True");
						HttpResponse response = Client.execute(request);
						resp = EntityUtils.toString(response.getEntity());
						responseheaders = response.getAllHeaders();
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
			protected void onPostExecute(String s)
			{
				if (mProgressDialog1.isShowing())
				{
					Log.e(TAG, "did this get reached? fucking cancel progress dialogs please!");
					mProgressDialog1.dismiss();
				}
				listViewContents.clear();
				try {
					JSONTokener tokener = new JSONTokener(s);
					values = (JSONArray) tokener.nextValue();
					Log.e(TAG, "json array is " + values.toString());
					for (int i=0; i<values.length(); i++)
					{
						JSONObject obj = values.getJSONObject(i);
						if (obj.getString("contact_name") != null)
						{
							listViewContents.add(obj.getString("contact_name"));
						}
					}
				}
				catch (JSONException e )
				{
					e.printStackTrace();
				}
				catch (ClassCastException e)
				{
					e.printStackTrace();
				}
				ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_multiple_choice, listViewContents);
				Collections.sort(listViewContents);
				unsyncStatusList.setAdapter(mArrayAdapter);
				unsyncStatusList.setChoiceMode(unsyncStatusList.CHOICE_MODE_MULTIPLE);
			}
		}


		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
			case R.id.eselectall:
				for ( int i=0; i <unsyncStatusList.getCount(); i++) {
					unsyncStatusList.setItemChecked(i, true);
					}
				break;
			case (R.id.edeselectall):
				for ( int i=0; i <unsyncStatusList.getCount(); i++) {
					unsyncStatusList.setItemChecked(i, false);
					}
				break;
			case (R.id.unsyncme):
				SparseBooleanArray checked = unsyncStatusList.getCheckedItemPositions();
				for (int i = 0; i < checked.size(); i++){
		    
					int key = checked.keyAt(i);
					boolean value  = checked.get(key);
					if (value)
					{
						
						removePhoneContacts.add((String) unsyncStatusList.getItemAtPosition(key));
					}
					else
					{
			    
					}
					
			}
				JSONArray jsonArray = createJsonString(removePhoneContacts);
				Log.e(TAG, "json array is " + jsonArray.toString());
				JSONObject sendObject = getObject(jsonArray);
				Log.e(TAG, "send object is " + sendObject.toString());
				HTM2 = new HttpMethodTask2(getActivity(), sendObject);
				if ((HTM2.getStatus() != AsyncTask.Status.RUNNING))
				{
					Log.e(TAG, "HTM2 called from onclick of the button");
					HTM2.execute(testURL2);
				}
				break;
		}

		}
		
		
		public class HttpMethodTask2 extends AsyncTask<String, String, String> {

		private Context context = getActivity().getApplicationContext();
		private JSONObject jsonObject;
		
		public HttpMethodTask2(Context applicationContext, JSONObject jsonObject) {
			// TODO Auto-generated constructor stub
				this.context = context;
				this.jsonObject = jsonObject;
				
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			mProgressDialog2 = new ProgressDialog(getActivity());
			mProgressDialog2.setProgress(ProgressDialog.STYLE_SPINNER);
			mProgressDialog2.setTitle("Deleting contacts on embry.io");
			mProgressDialog2.setMessage("Please wait.");
			mProgressDialog2.setCancelable(false);
			mProgressDialog2.setIndeterminate(true);
			mProgressDialog2.show();
		}


		@Override
		protected String doInBackground(String... url)  {
			// TODO Auto-generated method stub
			String resp = "";
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
			
			Log.e(TAG, "response is " + resp.toString());
			return resp;
		}
		
		
		@Override
		protected void onPostExecute(String s)
		{
				if (mProgressDialog2 != null)
				{
					mProgressDialog2.dismiss();
				}
			update();
		}
		
		}

		@Override
		public void update() {
			listViewContents.clear();
			removePhoneContacts.clear();
			Context context = getActivity();
			if ((HTM1 != null) && (HTM1.getStatus() == AsyncTask.Status.FINISHED))
			{
				HTM1 = new HttpMethodTask1(getActivity().getApplicationContext());
				Log.e(TAG, "HTM1 called from update");
				HTM1.execute(testURL1);
			}
		}
}


