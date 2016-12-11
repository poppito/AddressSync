package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DownloadFile extends AsyncTask<Void, Integer, String> {
    private final String TAG = this.getClass().getSimpleName();
    public Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private String fileName;
    private DropboxAPI.DropboxFileInfo mFileInfo;
    private ProgressDialog mProgressDialog;
    private int totalCount, currentCount;

    public DownloadFile(Context c, String fileName, int totalCount, int currentCount) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileName = fileName;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            publishProgress(totalCount, currentCount);
            File file = new File(context.getFilesDir() + "/" + fileName);
            FileOutputStream mOutputStream = new FileOutputStream(file);
            mFileInfo = emboDBApi.getFile(fileName, null, mOutputStream, null);
        } catch (DropboxException e) {
            Log.v(TAG, e.getMessage());
        } catch (FileNotFoundException e) {
            Log.v(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Send contacts for sync");
        mProgressDialog.setMessage("Just a second.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setMessage("Downloading " + String.valueOf(values[1]) + " of " + String.valueOf(values[0]) + " contacts");
    }


    /*			try
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
	*/

}
