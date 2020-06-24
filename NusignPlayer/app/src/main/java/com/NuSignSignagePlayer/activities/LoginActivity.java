package com.NuSignSignagePlayer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.NuSignSignagePlayer.R;
import com.NuSignSignagePlayer.api_manager.OkHttpUtil;
import com.NuSignSignagePlayer.interfaces.PlaylistLoaderListener;
import com.NuSignSignagePlayer.mediamanager.PlaylistManager;
import com.NuSignSignagePlayer.utils.AlenkaMediaPreferences;
import com.NuSignSignagePlayer.utils.AlertDialogManager;
import com.NuSignSignagePlayer.utils.Constants;
import com.NuSignSignagePlayer.utils.FirebaseIDService;
import com.NuSignSignagePlayer.utils.SharedPreferenceUtil;
import com.NuSignSignagePlayer.utils.Utilities;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class LoginActivity extends Activity implements View.OnClickListener,
        OkHttpUtil.OkHttpResponse, PlaylistLoaderListener{

    public static  final String TAG = "LoginActivity";
    Context context = LoginActivity.this;
    private EditText edit_username, edit_token;
    Button btn_Submit, btn_Cancel;
    private ArrayList<String> arrdeviceid = new ArrayList<String>();
    private FirebaseIDService fb=new FirebaseIDService();
    private String fbidupd="";
    ProgressDialog progressDialog;

    @Override
    public void tokenUpdatedOnServer() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_loginportrait);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        edit_username = (EditText) findViewById(R.id.edit_username);
        edit_token = (EditText) findViewById(R.id.edit_tokenNo);

        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        btn_Cancel = (Button) findViewById(R.id.btn_Cancel);

        btn_Submit.setOnClickListener(this);
        btn_Cancel.setOnClickListener(this);

//        edit_username.setText("In-Mohali");

        deleteDatabase();
    }
        catch (Exception e)
        {
            e.getCause();
        }
}

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btn_Submit:{
                submitButtonClickHandler();
           }break;

            case R.id.btn_Cancel:{
                LoginActivity.this.finish();
            }break;
        }


    }

    private void submitButtonClickHandler(){

        if (!Utilities.isConnected()) {

            AlertDialogManager alertDialogManager = null;
            alertDialogManager.showAlertDialog(LoginActivity.this,
                    getString(R.string.login_internet_not_connected),
                    getString(R.string.login_connect_to_internet),
                    true);

            return;
        } else {

            String user_Name = edit_username.getText().toString();
            String token_ID = edit_token.getText().toString();

            if (user_Name.equals("")) {

                edit_username.setError(getString(R.string.login_enter_username));

            } else if (token_ID.equals("")) {

                edit_token.setError(getString(R.string.login_enter_token));
            }
            else {
                showDialogBox(true);
                loginUser();
            }

        }
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null){
        //    Toast.makeText(LoginActivity.this, "Response returned null", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (tag){

            case  Constants.CHECK_USER_LOGIN_TAG:{
                handleLoginResponse(response);
            }break;

            case Constants.CHECK_USER_RIGHTS_TAG:{
                handleCheckDeviceIdResponse(response);
            }
        }

    }

    @Override
    public void onError(Exception e, int tag) {

    }

    private void loginUser(){

        String user_Name = edit_username.getText().toString();
        String token_ID = edit_token.getText().toString();

        try {
            JSONObject json = new JSONObject();

            json.put("DeviceId", Utilities.getDeviceID(context));
            json.put("TokenNo", token_ID);
            json.put("UserName", user_Name);
            json.put("PlayerType", Constants.PLAYER_TYPE);

            /*new OkHttpUtil(context,Constants.CHECK_USER_LOGIN,json.toString(),
                    LoginActivity.this,false,
                    Constants.CHECK_USER_LOGIN_TAG).
                    execute();*/
            new OkHttpUtil(context,Constants.CHECK_USER_LOGIN,json.toString(),
                    LoginActivity.this,false,
                    Constants.CHECK_USER_LOGIN_TAG).
                    callRequest();



        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private void handleLoginResponse(String response){

        try {

            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            if (Response.equals("1")) {

                SharedPreferenceUtil.setStringPreference(context,AlenkaMediaPreferences.TOKEN_ID,edit_token.getText().toString());

                checkDeviceIdOnServer();

            } else {
                showDialogBox(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        LoginActivity.this);
                builder.setTitle("Invalid");
                builder.setMessage("Please Enter the valid UserName and TokenNo.");
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                dialog.dismiss();

                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            }

        } catch (Exception e) {

        }
    }


    private void checkDeviceIdOnServer(){

        try {

            JSONArray jsondeviceid = new JSONArray();
            arrdeviceid.add(Utilities.getDeviceID(context));
            arrdeviceid.add(Splash_Activity.getMacAddr());
            arrdeviceid.add(Splash_Activity.getSerialNumber());
            for(int i=0;i<arrdeviceid.size();i++)
            {
                JSONObject json = new JSONObject();
                json.put("DeviceId",arrdeviceid.get(i));
                jsondeviceid.put(json);
            }

            /*new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    LoginActivity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).
                    execute();*/

            new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,jsondeviceid.toString(),
                    LoginActivity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).
                    callRequest();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFirebaseid()
    {
        JSONObject json = new JSONObject();
        try {

            json.put("TokenId",SharedPreferenceUtil.
                    getStringPreference(LoginActivity.this.context,Constants.TOKEN_ID));
            json.put("fcmId",fbidupd);

            Log.e(TAG,Utilities.getDeviceID(context));

         /*   new OkHttpUtil(context,Constants.CHECK_USER_RIGHTS,json.toString(),
                    Splash_Activity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).callRequest();*/

            new OkHttpUtil(context,Constants.UpdateFcm,json.toString(),
                    LoginActivity.this,false,
                    Constants.CHECK_USER_RIGHTS_TAG).
                    execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }





    private void handleCheckDeviceIdResponse(String response){

        try{

            String Response = new JSONArray(response).getJSONObject(0).getString("Response");
            String Left_Days = new JSONArray(response).getJSONObject(0).getString("LeftDays");
            String TokenID = new JSONArray(response).getJSONObject(0).getString("TokenId");

            int left_days = Integer.parseInt(Left_Days);

            if (Response.equals("1")){

                String Cityid = new JSONArray(response).getJSONObject(0).getString("Cityid");
                String CountryId = new JSONArray(response).getJSONObject(0).getString("CountryId");

                String StateId = new JSONArray(response).getJSONObject(0).getString("StateId");
                String dfClientId = new JSONArray(response).getJSONObject(0).getString("dfClientId");
                String isStopControl = new JSONArray(response).getJSONObject(0).getString("IsStopControl");
                String Firebaseid=new JSONArray(response).getJSONObject(0).getString("FcmId");
                String schtype=new JSONArray(response).getJSONObject(0).getString("scheduleType");
                String indictype=new JSONArray(response).getJSONObject(0).getString("IsIndicatorActive");
                String imgtype=new JSONArray(response).getJSONObject(0).getString("LogoId");
                String rotation=new JSONArray(response).getJSONObject(0).getString("Rotation");

                SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.DEVICE_ID,Utilities.getDeviceID(context));
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

                                            new PlaylistManager(context,LoginActivity.this).getPlaylistsFromServer();                                        }
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

                                            new PlaylistManager(context,LoginActivity.this).getPlaylistsFromServer();                                        }
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

                   new PlaylistManager(context,LoginActivity.this).getPlaylistsFromServer();

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
                    SharedPreferenceUtil.setStringPreference(context,AlenkaMediaPreferences.TOKEN_ID,"");
                  //  Toast.makeText(LoginActivity.this, "Go to login", Toast.LENGTH_SHORT).show();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteDatabase (){

       String path = context.getApplicationInfo().dataDir
               + File.separator + Constants.ROOT_FOLDER;

        File file = new File(path);

        deleteRecursive(file);
    }

    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();


    }

    @Override
    protected void onStop() {
        super.onStop();
        showDialogBox(false);
    }

    @Override
    public void startedGettingPlaylist() {

        showDialogBox(true);

    }

    @Override
    public void finishedGettingPlaylist() {
        showDialogBox(false);
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void errorInGettingPlaylist() {
        showDialogBox(false);
    }

    @Override
    public void recordSaved(boolean isSaved) {

    }

    private void showDialogBox(final boolean shouldShow) {

        Handler mainHandler = new Handler(context.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (shouldShow)
                {
                    if (progressDialog == null){
                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("Syncing songs...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    } else {
                        progressDialog.show();
                    }
                } else {

                    if (progressDialog != null)
                        progressDialog.dismiss();
                }
            }
        };
        mainHandler.post(myRunnable);


    }
}