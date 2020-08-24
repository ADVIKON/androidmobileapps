package com.advikonsignagekeyboardplayer.mediamanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.database.SongsDataSource;
import com.advikonsignagekeyboardplayer.models.Songs;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by love on 11/8/18.
 */

public class AdditionalSongsRemovalTask extends AsyncTask{

    final static String TAG = AdditionalSongsRemovalTask.class.getSimpleName();

    final static String DATE_PATTERN = "dd/MMM/yyyy hh:mm:ss aa";

    interface TitleIdsCallback {
        void onResponse(String[] titleIds);
    }

    private Context mContext;

    public AdditionalSongsRemovalTask(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //22/Aug/2018 09:54:45 PM
        String lastDeletedDate = SharedPreferenceUtil.getStringPreference(mContext, Constants.SONGS_LAST_REMOVED);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

        // First time running, insert date and continue with the task.
        if (lastDeletedDate == null || lastDeletedDate.equalsIgnoreCase("")){


            String lastRunDateTime = simpleDateFormat.format(Calendar.getInstance().getTime());
            SharedPreferenceUtil.setStringPreference(mContext, Constants.SONGS_LAST_REMOVED,lastRunDateTime);

        } else {

            // Get the last date on which the songs were removed.

            Date date = null;
            try {
                date = simpleDateFormat.parse(lastDeletedDate);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            if (date != null){

                long differenceInDays = Utilities.getDifferenceBetweenTwoDatesInDays(new Date(),date);

                if (differenceInDays < 30){

                    cancel(true);
                } else {

                    String lastRunDateTime = simpleDateFormat.format(Calendar.getInstance().getTime());
                    SharedPreferenceUtil.setStringPreference(mContext, Constants.SONGS_LAST_REMOVED,lastRunDateTime);
                }
            }
        }

    }

    @Override
    protected Object doInBackground(Object[] objects) {

        getScheduledSongs(new TitleIdsCallback() {
            @Override
            public void onResponse(String[] titleIds) {

                if (titleIds.length > 0){

                    SongsDataSource songsDataSource = new SongsDataSource(mContext);
                    ArrayList<Songs> songsArrayList = songsDataSource.getSongsToBeDeletedWithTitleIds(titleIds);

                    if (songsArrayList != null && songsArrayList.size() > 0){

                        for (Songs song:songsArrayList) {
                            songsDataSource.deleteSongs(song, true);
                        }
                    }
                }
            }
        });
        return null;
    }


    private void getScheduledSongs(final TitleIdsCallback titleIdsCallback){

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Tokenid", SharedPreferenceUtil.getStringPreference(mContext, Constants.TOKEN_ID));

            new OkHttpUtil(mContext, Constants.SCHEDULED_SONGS, jsonObject.toString(), new OkHttpUtil.OkHttpResponse() {
                @Override
                public void onResponse(String response, int tag) {

                    if (response != null){

                        try{

                            JSONArray titleIds = new JSONArray(response);

                            String[] stringArray = new String[titleIds.length()];
                            for(int i = 0, count = titleIds.length(); i< count; i++)
                            {
                                try {
                                    String jsonString = titleIds.getString(i);
                                    stringArray[i] = jsonString.toString();
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (titleIdsCallback != null){
                                titleIdsCallback.onResponse(stringArray);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onError(Exception e, int tag) {
                    Log.e(TAG,e.getLocalizedMessage());
                }

            },false, Constants.SCHEDULED_SONGS_TAG).execute();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
