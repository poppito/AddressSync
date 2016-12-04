package com.noni.embryio;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class LogonClass extends FragmentActivity implements OnClickListener {

	private SharedPreferences prefs;
	private String TAG = "LogonClass.java";
	private EditText passwordText, usernameText;
	private String usernameString;
	private String passwordString;
	private Button logonButton, resetButton;
	private TextView logonstatus;
	private final String LOGON_URL   = Constants.SERVERURL+ "login_mobile";
	int TIMEOUT_MILLSEC = 10000;
	public static MyHttpClient Client = new MyHttpClient(null);
	public ProgressDialog mProgressDialog;
	private ActionBar actionBar;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.logonview);
		usernameText = (EditText)findViewById(R.id.username);
		passwordText = (EditText)findViewById(R.id.password);
		logonButton = (Button)findViewById(R.id.logonbutton);
		resetButton = (Button)findViewById(R.id.resetbutton);
		logonButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		
	}
	
	@Override
	protected void onDestroy()
	{
		Log.e(TAG, "On Destroy called");
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		Log.e(TAG, "On Pause called");
		super.onPause();
	}


	@Override
	public  void onBackPressed()
	{
		finish();
	}

	@Override
	public void onClick(View view) {
	// TODO Auto-generated method stub
		HttpMethodTask HMT = new HttpMethodTask(getApplicationContext());
		switch (view.getId())
		{	
			case R.id.logonbutton:
			{
				Log.e(TAG, "logon button pressed");
				usernameString = usernameText.getText().toString();
				passwordString = passwordText.getText().toString();
				Log.e(TAG, "username is " + usernameString + "password is " + passwordString);
				if (HMT.getStatus() != AsyncTask.Status.RUNNING)
				{
					try
					{
						HMT.execute(LOGON_URL);
						//new HttpMethodTask(getApplicationContext()).execute(testURL);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				break;
			}
				
			case R.id.resetbutton:
			{
				Intent i = new Intent(this, Register_New.class);
				startActivity(i);
				finish();
				break;
			}
		}
	}
	
public class HttpMethodTask extends AsyncTask<String, Void, String> {
		
		public Context context;

		public HttpMethodTask(Context ApplicationContext) {
			// TODO Auto-generated constructor stub
			this.context = ApplicationContext;
			
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(LogonClass.this);
			mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setTitle("Logging you in");
			mProgressDialog.setMessage("one sec, beautiful ;)");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			//first get u/p prefs, store and test;
			String fleh = "no response?";
			//create a http post request
			Header[] responseHeaders = null;
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
			HttpPost request = new HttpPost(url[0]);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("login", "True"));
			nameValuePairs.add(new BasicNameValuePair("username", usernameString));
		    nameValuePairs.add(new BasicNameValuePair("password", passwordString));
			try
			{
			   UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			   request.setEntity(entity);
			   Client.putContext(context);
			   HttpResponse response = Client.execute(request);
			   fleh = EntityUtils.toString(response.getEntity());
			   Log.e(TAG, "fleh returned "+ fleh);
			   responseHeaders = response.getAllHeaders();
			   for (int i=0; i<responseHeaders.length; i++)
				{
					Header h = responseHeaders[i];
					String name = h.getName();
					String value = h.getValue();
					Log.e(TAG, name + " " + value);
				}
			
			}
			catch (ClientProtocolException e)
				{
				Log.e(TAG, "client protocol exception!");				
				} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e(TAG, "fleh returned " + fleh);
			return fleh;
		}
		
		
		@Override
		protected void onPostExecute(String s)
		{
			if (mProgressDialog != null)
			{
				mProgressDialog.dismiss();
			}
			
			if (s.equals("no response?"))
			{
				Toast.makeText(LogonClass.this, "No connection to the internet detected :(", Toast.LENGTH_SHORT).show();
			}
			else
			{
				String status = null, error = null;	
				JSONTokener tokener = new JSONTokener(s);
			
				try {
					JSONObject response = (JSONObject) tokener.nextValue();
					status = response.getString("status");
					 error = response.getString("error");
					 Log.e(TAG, "status " + status + " error " + error);
					
					 if (error.equals("none"))
					 {
						Intent i = new Intent(LogonClass.this, MainActivity.class);
						startActivity(i);
						finish();
					 }
					 else
					 {
						 Toast.makeText(LogonClass.this, error, Toast.LENGTH_SHORT).show();
					 }
					 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (java.lang.ClassCastException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}

