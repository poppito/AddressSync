package com.noni.embryio;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class Register_New extends Activity implements OnClickListener {
	
	private EditText registerPasswordText, registerUsernameText, registerEmailText;
	private TextView registerStatus;
	private Button saveButton;
	String testURL = Constants.SERVERURL + "registermobile";
	int TIMEOUT_MILLSEC = 1000;
	public final String TAG = "RegisterNew";
	public HttpMethodTask HMT;
	private ProgressDialog mProgressDialog;
	private ActionBar actionBar;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.registernew);
		registerUsernameText = (EditText)findViewById(R.id.registerusername);
		registerPasswordText = (EditText)findViewById(R.id.registerpassword);
		registerEmailText = (EditText)findViewById(R.id.registeremail);
		registerStatus = (TextView)findViewById(R.id.register_status);
		saveButton = (Button)findViewById(R.id.submitbutton);
		saveButton.setOnClickListener(this);
	}
	
	
	@Override
	public void onBackPressed()
	{
		Intent i = new Intent(this, LogonClass.class);
		startActivity(i);
		finish();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if ((HMT != null) && (HMT.getStatus() == AsyncTask.Status.RUNNING))
		{
			HMT.cancel(true);
		}
	}
	
	@Override
	protected void onPause()
	{
		//Log.e(TAG, "on pause called");
		//Log.e(TAG, "HMT status is " + HMT.getStatus().toString());
		super.onPause();
		if ((HMT != null) && (HMT.getStatus() == AsyncTask.Status.RUNNING))
		{
			HMT.cancel(true);
		}
		//Log.e(TAG, "HMT status is " + HMT.getStatus().toString());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Button press!");
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		Editor mEditor = prefs.edit();
		String username = registerUsernameText.getText().toString();
		String password = registerPasswordText.getText().toString();
		String email = registerEmailText.getText().toString();
		mEditor.putString("username", registerUsernameText.getText().toString());
		Log.e(TAG, registerUsernameText.getText().toString());
		mEditor.putString("password", registerPasswordText.getText().toString());
		Log.e(TAG, registerPasswordText.getText().toString());
		if ((username != null)&&(password != null)&&(email != null))
		{
			mEditor.apply();
			HMT = new HttpMethodTask(getApplicationContext());
			HMT.execute(testURL);
				
		}
	}
	
	public class HttpMethodTask extends AsyncTask<String, Void, String> {
		
		public Context context;
		
		protected void onPreExecute()
		{
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(Register_New.this);
			mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setTitle("Processing...");
			mProgressDialog.setMessage("Please wait.");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
		}
		
		public HttpMethodTask(Context applicationContext) {
			// TODO Auto-generated constructor stub
			this.context = applicationContext;
		}
		

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			//first get u/p prefs, store and test;
			String fleh = null;
			//create a http post request
			Log.e(TAG, "HTTP Method task called!");
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLSEC);
			MyHttpClient Client = new MyHttpClient(context);
			HttpPost request = new HttpPost(url[0]);
			Log.e(TAG, "URL is " + url[0]);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("username", registerUsernameText.getText().toString()));
		    nameValuePairs.add(new BasicNameValuePair("password", registerPasswordText.getText().toString()));
		    nameValuePairs.add(new BasicNameValuePair("email", registerEmailText.getText().toString()));
			
			
			//here goes nothing
			try
			{
			   UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			   request.setEntity(entity);
			   HttpResponse response = Client.execute(request);
			   fleh = EntityUtils.toString(response.getEntity());
			   Log.e(TAG, "fleh returned "+ fleh);
			
			}
			catch (ClientProtocolException e)
				{
				Log.e(TAG, "client protocol exception!");				
				} 
			
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "encoding wasn't utf8?");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return fleh;
		}
		
		
		@Override
		protected void onPostExecute(String s)
		{
			if (mProgressDialog != null)
			{
				mProgressDialog.dismiss();
			}
			String status = null, error = null;			
			JSONTokener tokener = new JSONTokener(s);
			
			try {
				 JSONObject response = (JSONObject) tokener.nextValue();
				 status = response.getString("status");
				 error = response.getString("error");
				 registerStatus.setText(status);
				 registerStatus.setTextColor(getResources().getColor(R.color.white));
				 Log.e(TAG, "status " + status + " error " + error);
		
				 if (error.equals("none"))
				 {
						Intent i = new Intent(Register_New.this, LogonClass.class);
						startActivity(i);
						finish();
				 }
				 else
				 {
					 Toast.makeText(Register_New.this, error, Toast.LENGTH_SHORT).show();
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
