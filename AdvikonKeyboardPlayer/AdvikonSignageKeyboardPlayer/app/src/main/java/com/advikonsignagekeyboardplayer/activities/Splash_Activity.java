package com.advikonsignagekeyboardplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.advikonsignagekeyboardplayer.utils.AlenkaMediaPreferences;
import com.advikonsignagekeyboardplayer.utils.ConnectivityReceiver;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.FirebaseIDService;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.Utilities;
import com.advikonsignagekeyboardplayer.R;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.advikonsignagekeyboardplayer.api_manager.DownloadService;
import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.interfaces.PlaylistLoaderListener;
import com.advikonsignagekeyboardplayer.mediamanager.PlaylistManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by love on 29/5/17.
 */
public class Splash_Activity extends Activity implements ConnectivityReceiver.ConnectivityReceiverListener,
        OkHttpUtil.OkHttpResponse, PlaylistLoaderListener {

    private static final String TAG = "Splash Activity";

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    private LinearLayout rootLayout;
    Context context = Splash_Activity.this;
    private int currentApiVersion = Build.VERSION.SDK_INT;
    TextView txtTokenId, txtCurrentTask;
    private FirebaseIDService fb=new FirebaseIDService();
    private String fbidupd="";

    ArrayList<String> permissions = new ArrayList<String>();

    CircularProgressView progressView;

    private boolean isActivityVisible;
    private ArrayList<String> arrdeviceid = new ArrayList<String>();

    private String m_chosenDir = "";
    private boolean m_newFolderEnabled = true;

    // to find usb/otg
    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;

    @Override
    public void tokenUpdatedOnServer() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_);

//        Fabric.with(this, new Crashlytics());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        writeToFile(token);
                        // Log and toast
                    }
                });

        rootLayout = findViewById(R.id.mainContainer);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        txtTokenId = (TextView) findViewById(R.id.txtTokenId);
        txtTokenId.setTypeface(Utilities.getApplicationTypeface(context));

        txtCurrentTask = (TextView) findViewById(R.id.txtCurrentProgress);

        txtCurrentTask.setTypeface(Utilities.getApplicationTypeface(context));

        progressView = (CircularProgressView) findViewById(R.id.progress_view);

        String token = SharedPreferenceUtil.getStringPreference(context, Constants.TOKEN_ID);

        if (token.length() > 0){
            txtTokenId.setText("Token ID : " + token);
        } else {
            txtTokenId.setText("");
        }


//        SharedPreferenceUtil.setBooleanPreference(this.context,Constants.IS_UPDATE_IN_PROGRESS,false);
//        Toast.makeText(Splash_Activity.this, getAndroidVersion(), Toast.LENGTH_LONG).show()
    }

    @Override
    protected void onStart() {
        super.onStart();

      //  new AdditionalSongsRemovalTask(Splash_Activity.this).execute();

        stopService(new Intent(Splash_Activity.this, DownloadService.class));

        if (checkPermissions().size() > 0){
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]),100);
        } else {
            checkUserRights();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111) {

            String folderLocation = data.getExtras().getString("data");
            Log.i( "folderLocation", folderLocation );
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    private ArrayList<String> checkPermissions(){

        boolean hasWritePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasWritePermission){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!hasReadPermission){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return permissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions1, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions1, grantResults);

        permissions.clear();

        if (checkPermissions().size() > 0){

            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]),100);

        }else {
            checkUserRights();
        }

    }

    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            // (?) Lenovo Tab (https://stackoverflow.com/a/34819027/1276306)
            serialNumber = (String) get.invoke(c, "gsm.sn1");

            if (serialNumber.equals(""))
                // Samsung Galaxy S5 (SM-G900F) : 6.0.1
                // Samsung Galaxy S6 (SM-G920F) : 7.0
                // Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
                // (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
                serialNumber = (String) get.invoke(c, "ril.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Google Nexus 5 : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 5C (NEM-L51) : 7.0
                // Honor 5X (KIW-L21) : 6.0.1
                // Huawei M2 (M2-801w) : 5.1.1
                // (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
                serialNumber = (String) get.invoke(c, "ro.serialno");

            if (serialNumber.equals(""))
                // (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
                serialNumber = (String) get.invoke(c, "sys.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 9 Lite (LLD-L31) : 8.0
                // Xiaomi Mi 8 (M1803E1A) : 8.1.0
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = "";
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }


    public static String getMacAddr() {
        try {

            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }
    private void checkUserRights(){

        if (Utilities.isConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    checkDeviceIdOnServer();

                }

            }, SPLASH_DISPLAY_LENGTH);


        } else {

            String deviceID = SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DEVICE_ID);
            if (deviceID.equals("")) {

                showDialogBox(false);

            } else {

                /*Start the app in offline mode.*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);

            }
        }
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

    private void updateFirebaseid()
    {
        JSONObject json = new JSONObject();
        try {

            json.put("TokenId", SharedPreferenceUtil.
                    getStringPreference(Splash_Activity.this.context, Constants.TOKEN_ID));
            json.put("fcmId",fbidupd);

            Log.e(TAG, Utilities.getDeviceID(context));

         /*   new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).callRequest();*/

            new OkHttpUtil(context, Constants.UPDATE_FCM,json.toString(),
                    Splash_Activity.this,false,
                    Constants.UPDATE_FCM_TAG).
                    execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void checkDeviceIdOnServer(){
        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();
        txtCurrentTask.setText("Checking device ID");
        try {

            JSONArray jsondeviceid = new JSONArray();
            arrdeviceid.add(Utilities.getDeviceID(context));
            arrdeviceid.add(getMacAddr());
            arrdeviceid.add(getSerialNumber());

            String macAddress = Utilities.getLztekMacAddress(this);
            if (!macAddress.equals("")){
                arrdeviceid.add(macAddress);
            }

            for(int i=0;i<arrdeviceid.size();i++)
            {
                JSONObject json = new JSONObject();
                json.put("DeviceId",arrdeviceid.get(i));
                jsondeviceid.put(json);
            }
           // Toast.makeText(Splash_Activity.this, "DeviceId => "+ Utilities.getDeviceID(context) , Toast.LENGTH_SHORT).show();
            Log.e(TAG, Utilities.getDeviceID(context));

            new OkHttpUtil(context, Constants.CHECK_USER_RIGHTS,jsondeviceid.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).callRequest();

           /* new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).
                    execute();*/

        } catch (JSONException e) {
            e.printStackTrace();
          //  Toast.makeText(Splash_Activity.this, "Error => "+ e.getMessage() , Toast.LENGTH_SHORT).show();
            checkDeviceIdOnServer();
        }
    }

    public void hidenavigation()
    {

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                            else
                            {


                            }
                        }
                    });
        }

    }



    @Override
    public void onResponse(String response,int tag) {

        if (response == null){
          //  Toast.makeText(Splash_Activity.this, "Response returned null", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (tag){

            case Constants.CHECK_USER_RIGHTS_TAG:{
                handleCheckDeviceIdResponse(response);
            }break;

            case Constants.UPDATE_FCM_TAG:{
                handleUpdateFCMResponse(response);
            }break;
        }

    }

    @Override
    public void onError(Exception e,int tag) {

        if (tag == Constants.CHECK_USER_RIGHTS_TAG){

            String deviceID = SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DEVICE_ID);
            if (deviceID.equals("")) {

                showDialogBox(false);

            }
            else {

                Thread thread = new Thread(){
                    public void run(){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                };
                thread.start();



                /*Start the app in offline mode.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);*/

            }

//            checkDeviceIdOnServer();
        }
        e.printStackTrace();
    }

    private void handleUpdateFCMResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);

        } catch(Exception e){
            e.printStackTrace();
        }

    }

    private void handleCheckDeviceIdResponse(String response){

        try{
            //Toast.makeText(Splash_Activity.this, "response => "+ response , Toast.LENGTH_LONG).show();
            if(response.equals("[]")){

                Intent intent = new Intent(Splash_Activity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            JSONArray jsonArray = new JSONArray(response);

            String Response = jsonArray.getJSONObject(0).getString("Response");
            String Left_Days = jsonArray.getJSONObject(0).getString("LeftDays");
            String TokenID = jsonArray.getJSONObject(0).getString("TokenId");

            int left_days = Integer.parseInt(Left_Days);

            if (Response.equals("1")){

                String Cityid = jsonArray.getJSONObject(0).getString("Cityid");
                String CountryId = jsonArray.getJSONObject(0).getString("CountryId");

                String StateId = jsonArray.getJSONObject(0).getString("StateId");
                String dfClientId = jsonArray.getJSONObject(0).getString("dfClientId");
                String isStopControl = jsonArray.getJSONObject(0).getString("IsStopControl");
                String Firebaseid=jsonArray.getJSONObject(0).getString("FcmId");
                String schtype=jsonArray.getJSONObject(0).getString("scheduleType");
                String indictype=jsonArray.getJSONObject(0).getString("IsIndicatorActive");
                String imgtype=jsonArray.getJSONObject(0).getString("LogoId");
                String rotation=jsonArray.getJSONObject(0).getString("Rotation");

                Boolean isDemoToken = jsonArray.getJSONObject(0).getBoolean("IsDemoToken");
                Integer totalShotsCount = jsonArray.getJSONObject(0).getInt("TotalShot");
                String tankAlerts = jsonArray.getJSONObject(0).getString("DispenserAlert");
                String deviceType = jsonArray.getJSONObject(0).getString("DeviceType");

                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.DEVICE_ID, Utilities.getDeviceID(context));
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID,dfClientId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.TOKEN_ID,TokenID);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.City_ID,Cityid);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Country_ID,CountryId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.State_Id,StateId);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Is_Stop_Control,isStopControl);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Indicatorimg,indictype);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.SchType,schtype);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Imgtype,imgtype);
                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.Rotation,rotation);

                if (isDemoToken != null) {
                    SharedPreferenceUtil.setBooleanPreference(context, AlenkaMediaPreferences.isDemoToken,isDemoToken);
                }
                if (totalShotsCount != null) {
                    SharedPreferenceUtil.setIntegerPreference(context, AlenkaMediaPreferences.totalShotsCount,totalShotsCount);
                }
                if (tankAlerts != null) {
                    SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.tankAlertPercentages,tankAlerts);
                }

                deviceType = "Screen";

                if (deviceType != null) {
                    SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.deviceType,deviceType);
                } else {
                    SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.deviceType,"");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Tokenid", SharedPreferenceUtil.
                            getStringPreference(Splash_Activity.this.context, Constants.TOKEN_ID));

                    new OkHttpUtil(context, Constants.CHECK_TOKEN_PUBLISH,jsonObject.toString(),
                            Splash_Activity.this,false,
                            Constants.CHECK_TOKEN_PUBLISH_TAG).
                            callRequest();


                }catch (Exception e){
                    e.printStackTrace();
                }
                fbidupd=fb.readFromFile(getApplicationContext());
                if(!Firebaseid.equals(fbidupd))
                {
                    updateFirebaseid();
                }

                if (left_days >= 2 && left_days <= 7) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(left_days + " days left to renewal of subscription.Pay immediately  to keep your Music Online.")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                } else if (left_days == 1) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(left_days + " day left to renewal of subscription.Pay immediately  to keep your Music Online.")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                } else if (left_days == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Last day left to renewal of subscription.Pay immediately  to keep your Music Online.")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                         /*   dialog.dismiss();
                                            Intent intent_Main2 = new Intent(context,
                                                    PlayerActivity.class);
                                            startActivity(intent_Main2);
                                            finish();*/
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                } else {

                    /*After device id verified, we fetch the playlist and songs and upon completion
                     * it calls finishedGettingSongs() in this activity which takes us to Home screen */

                    new PlaylistManager(context, Splash_Activity.this).getPlaylistsFromServer();
                }
            } else if (Response.equals("0")) {
                if (left_days < 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(" Music Player is Expired. Please connect your vendor !!. Your player id: " + TokenID)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                } else {

                    SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.TOKEN_ID,"");
                    progressView.setVisibility(View.GONE);
                    progressView.stopAnimation();
                    txtTokenId.setText("");
                    Intent login = new Intent(context,
                            LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(Splash_Activity.this, "Error2 => "+ e.getMessage() , Toast.LENGTH_SHORT).show();
            checkDeviceIdOnServer();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActivityVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
    }

    private void showDialogBox(boolean isConnected) {

        if (!isConnected) {
            AlertDialog alertDialog = new AlertDialog.Builder(
                    Splash_Activity.this).create();

            // Setting Dialog Title
            alertDialog.setTitle("Internet Connection Error");

            // Setting Dialog Message
            alertDialog.setMessage("Please connect to working Internet connection!");

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to execute after dialog closed
                    finish();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }

    }

    @Override
    public void startedGettingPlaylist() {

        try {


            Handler mainHandler = new Handler(context.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    progressView.setVisibility(View.VISIBLE);
                    progressView.startAnimation();
                    txtCurrentTask.setText("Syncing content...");
                }
            };
            mainHandler.post(myRunnable);
        }
        catch (Exception e) {
            startedGettingPlaylist();
        }

    }

    @Override
    public void finishedGettingPlaylist() {

        Handler mainHandler = new Handler(context.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.INVISIBLE);
                progressView.stopAnimation();
                txtCurrentTask.setText("");

                startActivity(new Intent(Splash_Activity.this, HomeActivity.class));
            }
        };
        mainHandler.post(myRunnable);

    }

    @Override
    public void errorInGettingPlaylist() {

    }

    @Override
    public void recordSaved(boolean isSaved) {

    }

}
