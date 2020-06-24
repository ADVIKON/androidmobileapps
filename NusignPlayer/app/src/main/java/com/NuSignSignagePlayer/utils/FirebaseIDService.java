package com.NuSignSignagePlayer.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        writeToFile(refreshedToken);
        readFromFile(getApplicationContext());

    }
    public void writeToFile(String data) {
        // Get the directory for the user's public pictures directory.
        File mydir = getApplicationContext().getDir("mydir", Context.MODE_PRIVATE);
        if (!mydir.exists()) {
            mydir.mkdir();
            Log.d("App", " created directory");
        }
        File file = new File(mydir, "myfile.txt");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }
    public String readFromFile(Context context) {

        String ret = "";
        String yourFilePath = context.getApplicationInfo().dataDir+ "/app_mydir/" + "myfile.txt";
        File writefile = new File( yourFilePath );
        try {

            if ( writefile != null ) {
                FileReader inputStreamReader = new FileReader(writefile);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}