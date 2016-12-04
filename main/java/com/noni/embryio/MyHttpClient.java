package com.noni.embryio;

import java.io.InputStream;
import java.security.KeyStore;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;
import android.util.Log;

public class MyHttpClient extends DefaultHttpClient {
	
	public static final String TAG = "MyHttpClient";
	public Context context;
	public MyHttpClient Client;
	
	public MyHttpClient(Context context)
	{
		this.context = context;
	}
	
	
	public void putContext(Context ApplicationContext)
	{
		this.context = ApplicationContext;
	}

	
	  @Override 
	  protected ClientConnectionManager createClientConnectionManager() {
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(
	        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    registry.register(new Scheme("https", newSslSocketFactory(), 443));
	    return new SingleClientConnManager(getParams(), registry);
	  }

	  private SSLSocketFactory newSslSocketFactory() {
	    try {
	      KeyStore trusted = KeyStore.getInstance("BKS");
	      InputStream in = context.getResources().openRawResource(R.raw.mystore);
	      Log.e(TAG, "context works fine?");
	      try {
	        trusted.load(in, "ez24get".toCharArray());
	      } finally {
	        in.close();
	      }
	      
	  	return new SSLSocketFactory(trusted);
	    } catch (Exception e) {
	    e.printStackTrace();
	      throw new AssertionError(e);
	    }
	  }
	}
