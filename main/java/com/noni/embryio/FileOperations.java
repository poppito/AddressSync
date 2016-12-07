package com.noni.embryio;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class FileOperations  extends AsyncTask<String, Void, String> {

    private final String TAG = FileOperations.class.getSimpleName();
    public Context context;
    DropboxAPI emboDBApi;

    public FileOperations (Context c, DropboxAPI emboDBApi) {
        this.context = c;
        this.emboDBApi = emboDBApi;
    }


    public File writeTestFile(Context context, String fileContent, String fileName) {
        FileOutputStream FOS = null;

        try {
            FOS = context.openFileOutput(fileName, context.MODE_PRIVATE);
            OutputStreamWriter OSW = new OutputStreamWriter(FOS);
            OSW.write(fileContent);
            OSW.close();
        } catch (IOException e) {
            Log.v(TAG, "exception thrown whilst writing file output stream");
            e.printStackTrace();
        }


        File file = new File(context.getFilesDir().getPath() + "/" + fileName);
        return file;

    }

    @Override
    protected String doInBackground(String... params)  {
        String responseRead  = "";
        String filename = params[0];
        try {
            File file = writeTestFile(context, "test", "test");
            FileInputStream inputStream = new FileInputStream(file);
            DropboxAPI.Entry response = emboDBApi.putFile("/mo.txt", inputStream, file.length(), null, null);
            Log.v(TAG, "The uploaded file's revision number is " + response.rev.toString());
            responseRead = response.rev.toString();
        }

        catch (FileNotFoundException e)
        {
            Log.v(TAG, "file not found exception thrown");
            e.printStackTrace();
        }

        catch (DropboxException e)
        {
            Log.v(TAG, "Dropbox exception thrown");
            e.printStackTrace();
        }

        return responseRead;
    }

    @Override
    protected void onPostExecute(String response) {

        if (! response.equals(""))
        {
            Log.v(TAG, "response from file upload: " + response.toString());
        }
        else
        {
            Log.v(TAG, "file upload failed");
        }

    }


}
