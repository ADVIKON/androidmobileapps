package com.advikonsignagekeyboardplayer.mediamanager;

import android.content.Context;
import android.widget.Toast;

import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.database.AnnouncementLogsDataSource;
import com.advikonsignagekeyboardplayer.models.AnnouncementLog;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class AnnouncementLogsManager implements OkHttpUtil.OkHttpResponse {

    private AnnouncementLogsDataSource announcementLogsDataSource;

    private Context context;

    public AnnouncementLogsManager(Context context){
        this.context = context;
        announcementLogsDataSource = new AnnouncementLogsDataSource(this.context);
    }

    public void insertSongPlayedStatus(int event, String command, String announcementId){

        Calendar calendar;
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss a");
        Date date = calendar.getTime();
        String played_date_Time = simpleDateFormat.format(date);

        AnnouncementLog log = new AnnouncementLog();
        log.setLog(event);
        log.setLogDateTimeString(played_date_Time);
        log.setCommand(command);
        log.setAnnouncementId(announcementId);

        try {
            announcementLogsDataSource.open();
            announcementLogsDataSource.createLog(log);
            announcementLogsDataSource.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public void deleteRecordsPlayerSatus(String type){
        try {
            announcementDataSource.open();
            announcementDataSource.deletePlayedStatus(type);
            announcementDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    public void sendPlayedSongsStatusOnServer(){

        try{

            announcementLogsDataSource.open();

            ArrayList<AnnouncementLog> arrayList = announcementLogsDataSource.getAnnouncementLogsNew();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa");

            Collections.sort(arrayList, new Comparator<AnnouncementLog>() {
                @Override
                public int compare(AnnouncementLog playerStatus, AnnouncementLog t1) {

                    try {
                        Date played_date_Time1 = simpleDateFormat.parse(playerStatus.getLogDateTimeString());
                        Date played_date_Time2 = simpleDateFormat.parse(t1.getLogDateTimeString());

                        return played_date_Time1.compareTo(played_date_Time2);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    };

                    return 0;
                }
            });

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                int run = arrayList.size() < 20 ? arrayList.size() : 20;

                for (int i = 0; i < run; i++) {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Logs", arrayList.get(i).getLog());
                    jsonObject.put("PlayedDateTime", arrayList.get(i).getLogDateTimeString());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonObject.put("titleId", arrayList.get(i).getAnnouncementId());

                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

            new OkHttpUtil(context, Constants.ANNOUNCEMENT_LOGS,jsonArray.toString(),
                     AnnouncementLogsManager.this,false,
                    Constants.ANNOUNCEMENT_LOGS_TAG).
                    execute();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null || response.equals("") || response.length() < 1){
            Toast.makeText(this.context, "Empty response for announcement log statuses", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (tag){
            case Constants.ANNOUNCEMENT_LOGS_TAG:{
                handleUpdatedDataResponse(response);
            }break;
        }
    }

    private void handleUpdatedDataResponse(String response) {

        try {

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray == null || jsonArray.length() == 0){
                return;
            }

            announcementLogsDataSource.open();

            JSONArray responseArray = jsonArray.getJSONObject(0).getJSONArray("EventArray");

            for (int count = 0; count < responseArray.length(); count++) {

                JSONObject jsonObject = responseArray.getJSONObject(count);

                String responseStatus = jsonObject.getString("Response");
                String playedDateTime = jsonObject.getString("returnEventDateTime");

                if (responseStatus.equals("1")){
                    announcementLogsDataSource.deleteAnnouncements(playedDateTime);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            announcementLogsDataSource.close();
        }

    }

    @Override
    public void onError(Exception e, int tag) {
        e.printStackTrace();
    }
}
