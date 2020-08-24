package com.advikonsignagekeyboardplayer.mediamanager;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.advikonsignagekeyboardplayer.activities.HomeActivity;
import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.database.PlayerStatusDataSource;
import com.advikonsignagekeyboardplayer.models.PlayerStatus;
import com.advikonsignagekeyboardplayer.models.Playlist;
import com.advikonsignagekeyboardplayer.models.Songs;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.Utilities;
import com.advikonsignagekeyboardplayer.BuildConfig;
import com.advikonsignagekeyboardplayer.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by love on 4/6/17.
 */
public class PlayerStatusManager implements OkHttpUtil.OkHttpResponse {

    private PlayerStatusDataSource playerStatusDataSource;

    private Context context;

    public String artist_id = "";
    public String title_id = "";
    public String spl_plalist_id = "";
    public String songsDownloaded = "";


    public PlayerStatusManager(Context context){
        this.context = context;
        playerStatusDataSource = new PlayerStatusDataSource(this.context);
    }

    public void insertSongPlayedStatus(){

        Calendar calendar;
        calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss a");

        Date date = calendar.getTime();

        String played_date_Time = simpleDateFormat.format(date);

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setArtistIdStatusSong(artist_id);
        playerStatus.setTitleIdSong(title_id);
        playerStatus.setSplPlaylistIdSong(spl_plalist_id);
        playerStatus.setPlayerDateTimeSong(played_date_Time);
        playerStatus.setPlayerStatusAll("song");

        try {
            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAdvPlayerStatus(PlayerStatus playerStatus){

        try {
            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* This method is for login status where we will insert and send data to server that we have saved locally  */

    public void updateLoginStatus(){

        String currentDate = Utilities.currentDate();
        String currenttime = Utilities.currentTime();

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setLoginDate(currentDate);
        playerStatus.setLoginTime(currenttime);
        playerStatus.setPlayerStatusAll("login");

        try {
            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*Update the player logout time in database.*/
    public void updateLogoutStatus(){

        String currentDate = Utilities.currentDate();
        String currenttime = Utilities.currentTime();

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setLogoutDate(currentDate);
        playerStatus.setLogoutTime(currenttime);
        playerStatus.setPlayerStatusAll("logout");

        try {
            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            updateLogoutStatusOnServer();
        }
    }

    private void updateLogoutStatusOnServer(){

        try {
            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("logout");

            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("LogoutDate", arrayList.get(i).getLogoutDate());
                    jsonObject.put("LogoutTime", arrayList.get(i).getLogoutTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

//            return jsonArray.toString();
//            Toast.makeText(this.context, "Sending login status", Toast.LENGTH_SHORT).show();

            new OkHttpUtil(this.context, Constants.PLAYER_LOGOUT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGOUT_STATUS_STREAM_TAG).
                    callRequest();

        }catch (Exception e){
            e.printStackTrace();
        }
//        return null;
    }

    public void updateHeartBeatStatus(){

        Calendar calendar;
        calendar =Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa");
        String played_date_time = simpleDateFormat.format(calendar.getTime());

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setHeartbeatDateTimeStatus(played_date_time);
        playerStatus.setPlayerStatusAll("heartbeat");

        try {

            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecordsPlayerSatus(String type){
        try {
            playerStatusDataSource.open();
            playerStatusDataSource.deletePlayedStatus(type);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDataOnServer(){

        try {
            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("login");

            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("LoginDate", arrayList.get(i).getLoginDate());
                    jsonObject.put("LoginTime", arrayList.get(i).getLoginTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

//            return jsonArray.toString();
//            Toast.makeText(this.context, "Sending login status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(this.context,Constants.PLAYER_LOGIN_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGIN_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(this.context, Constants.PLAYER_LOGIN_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGIN_STATUS_STREAM_TAG).
                    callRequest();

        }catch (Exception e){
            e.printStackTrace();
        }
//        return null;
    }

    public void sendPlayedSongsStatusOnServer(){

        try{

            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongsNew("song");

            Collections.sort(arrayList, new Comparator<PlayerStatus>() {
                @Override
                public int compare(PlayerStatus playerStatus, PlayerStatus t1) {

                    Calendar calendar;

                    calendar = Calendar.getInstance();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa");

                    try {
                        Date played_date_Time1 = simpleDateFormat.parse(playerStatus.getPlayerDateTimeSong());
                        Date played_date_Time2 = simpleDateFormat.parse(t1.getPlayerDateTimeSong());

                        return played_date_Time2.compareTo(played_date_Time1);

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
                    jsonObject.put("ArtistId", arrayList.get(i).getArtistIdStatusSong());
                    jsonObject.put("PlayedDateTime", arrayList.get(i).getPlayerDateTimeSong());
                    jsonObject.put("splPlaylistId", arrayList.get(i).getSplPlaylistIdSong());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonObject.put("TitleId", arrayList.get(i).getTitleIdSong());
                    jsonArray.put(jsonObject);

                    //TODO: Here delete the song status one by one from database table
                    //TODO: Here delete the song status one by one from database table
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

           // Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();

            new OkHttpUtil(context, Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();


     /*       new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    callRequest();*/

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateDownloadedSongsCountOnServer(){

        if (!songsDownloaded.equalsIgnoreCase("")) {

            JSONObject jsonObject = new JSONObject();
            String pathToUsb="";
            File[] pathsss = ContextCompat.getExternalFilesDirs(context,null);
            long bytesAvailable;
            long bytesTotal;
            long megAvailable;
            long megTotAvailable;

            if (pathsss.length > 1) {
                File usbDrive = pathsss[1];
                if (usbDrive == null){
                    usbDrive = pathsss[0];
                }

                pathToUsb = usbDrive.getAbsolutePath();
                StatFs stat = new StatFs(pathToUsb);
                bytesTotal = stat.getBlockSizeLong() * stat.getBlockCountLong();
                megTotAvailable=bytesTotal / (1024 * 1024);
                bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
                megAvailable = bytesAvailable / (1024 * 1024);

            } else {
                pathToUsb =Environment.getDataDirectory().getAbsolutePath();
                StatFs stat = new StatFs(pathToUsb);
                bytesTotal = stat.getBlockSizeLong() * stat.getBlockCountLong();
                megTotAvailable=bytesTotal / (1024 * 1024);
                bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
                megAvailable = bytesAvailable / (1024 * 1024);
            }

            try {

                jsonObject.put("totalSong",songsDownloaded);
                jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(PlayerStatusManager.this.context, Constants.TOKEN_ID));
                jsonObject.put("FreeSpace",megAvailable);
                jsonObject.put("TotalSpace",megTotAvailable);
                jsonObject.put("verNo", BuildConfig.VERSION_NAME);

            }catch (Exception e){
                e.printStackTrace();
            }

            new OkHttpUtil(this.context, Constants.DOWNLOADINGPROCESS,jsonObject.toString(),
                    PlayerStatusManager.this,false,
                    Constants.DOWNLOADINGPROCESS_TAG).
                    callRequest();
        }

    }

    public void updateDownloadedSongsPlaylistWise(){

        try{

            PlaylistManager playlistManager = new PlaylistManager(this.context,null);

            ArrayList<Playlist> arrayList = playlistManager.getAllPlaylistInPlayingOrder();

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                for (int i = 0; i < arrayList.size(); i++) {

                    JSONObject jsonObject = new JSONObject();

                    Playlist playlist = arrayList.get(i);

                    ArrayList<Songs> arrSongs = playlistManager.getDownloadedSongsForPlaylist(playlist.getsplPlaylist_Id());

                    if (arrSongs == null) {
                        jsonObject.put("totalSong", "0");
                    }
                    else {
                        jsonObject.put("totalSong", arrSongs.size());
                    }

                    jsonObject.put("splPlaylistId",playlist.getsplPlaylist_Id());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }



            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(context, Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS_TAG).
                    execute();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void updateDownloadedSongsPlaylistDetails(){

        try{

            PlaylistManager playlistManager = new PlaylistManager(this.context,null);

            ArrayList<Playlist> arrayListRepeated = playlistManager.getAllPlaylistInPlayingOrder();

            List<Playlist> arrayList = new ArrayList<Playlist>();

            for (Playlist event : arrayListRepeated) {

                boolean isFound = false;
                // check if the event name exists in noRepeat
                for (Playlist e : arrayList) {
                    if (e.getsplPlaylist_Id().equalsIgnoreCase(event.getsplPlaylist_Id()))
                        isFound = true;
                }

                if (!isFound) {
                    arrayList.add(event);
                }
            }

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                for (int i = 0; i < arrayList.size(); i++) {

                    JSONObject jsonObject = new JSONObject();

                    Playlist playlist = arrayList.get(i);

                    ArrayList<Songs> arrSongs = playlistManager.getDownloadedSongsForPlaylist(playlist.getsplPlaylist_Id());

                    if (arrSongs == null) {
                        jsonObject.put("titleIDArray", new JSONArray());
                    }
                    else {

                        JSONArray titleIDArray = new JSONArray();

                        for (int j = 0; j < arrSongs.size(); j++){

                            Songs songs = arrSongs.get(j);
                            titleIDArray.put(songs.getTitle_Id());

                        }

                        jsonObject.put("titleIDArray", titleIDArray);
                    }

                    jsonObject.put("splPlaylistId",playlist.getsplPlaylist_Id());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));

                    jsonArray.put(jsonObject);
                }

            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(context, Constants.UPDATE_PLAYLIST_SONGS_DETAILS,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.UPDATE_PLAYLIST_SONGS_DETAILS_TAG).
                    execute();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendPlayedAdsStatusOnServer(){

        try{

            playerStatusDataSource.open();
            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("adv");

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("AdvtId", arrayList.get(i).getAdvIdStatus());
                    jsonObject.put("PlayedDate", arrayList.get(i).getAdvPlayedDate());
                    jsonObject.put("PlayedTime", arrayList.get(i).getAdvPlayedTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                    //TODO: Here we delete advertisement status data from database table one by one
                    String advid = arrayList.get(i).getAdvIdStatus();
                    playerStatusDataSource.open();
                    playerStatusDataSource.deletePlayedAdvStatus("adv", advid);
                    playerStatusDataSource.close();
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();
*/
            Log.e("jsonArray => ",jsonArray.toString());

            new OkHttpUtil(context, Constants.PLAYED_ADVERTISEMENT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_ADVERTISEMENT_TAG).
                    callRequest();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendHeartBeatStatusOnServer(){
        try{

            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("heartbeat");
            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("HeartbeatDateTime", arrayList.get(i).getHeartbeatDateTimeStatus());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
            /*new OkHttpUtil(context,Constants.PLAYER_HEARTBEAT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG).
                    execute();*/

                        new OkHttpUtil(context, Constants.PLAYER_HEARTBEAT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null || response.equals("") || response.length() < 1){
            Toast.makeText(this.context, "Empty response for player statuses", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (tag){
            case Constants.PLAYER_LOGIN_STATUS_STREAM_TAG:{
                handleUpdatedDataResponse(response);
            }break;

            case Constants.PLAYED_SONG_STATUS_STREAM_TAG:{
                handleUpdatedSongsResponse(response);
            }break;

            case Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG:{
                handleUpdatedHeartBeatStatusResponse(response);
            }break;
            case Constants.PLAYED_ADVERTISEMENT_TAG:{
                handleUpdatedAdvertisementStatusResponse(response);
            }break;
            case Constants.PLAYER_LOGOUT_STATUS_STREAM_TAG:{
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }break;
            case Constants.DOWNLOADINGPROCESS_TAG:{
                Log.e("Updated download status",response);
            }break;
            case Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS_TAG:{
                handleUpdatedPlaylistWiseDownloadedSongs(response);
            }break;
            case Constants.UPDATE_PLAYLIST_SONGS_DETAILS_TAG:{
                Log.e("Updated download status",response);
            }break;
        }
    }

    private void handleUpdatedPlaylistWiseDownloadedSongs(String response) {


        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){

//                Toast.makeText(this.context, "All status updated", Toast.LENGTH_SHORT).show();
            }

            //updateDownloadedSongsPlaylistDetails();
            sendPlayedAdsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception e, int tag) {
        e.printStackTrace();
    }

    private void handleUpdatedDataResponse(String response){
        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("login");

            }
            /*Update played songs status*/
//            sendPlayedSongsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdatedSongsResponse(String response){

        try {

            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("login");
                HomeActivity.Imgmarker.setImageResource(R.drawable.green);
            }
            else
            {
                HomeActivity.Imgmarker.setImageResource(R.drawable.red);

            }

            JSONArray songsArray = new JSONArray(response).getJSONObject(0).getJSONArray("SongArray");

                    if (songsArray != null && songsArray.length() > 0) {

                        playerStatusDataSource.open();

                        ArrayList<PlayerStatus> playedSongsArrayList = playerStatusDataSource.getPlayedSongsNew("song");

                        for (int count = 0; count < songsArray.length(); count++) {

                            JSONObject song = songsArray.getJSONObject(count);

                            if (song != null){

                                String songTitleId = song.getString("returnTitleId");
                                String songPlayedTime = song.getString("returnPlayedDateTime");
                                String songUpdated = song.getString("Response");

                                if (songTitleId != null && songPlayedTime != null && songUpdated != null){

                                    if (playedSongsArrayList != null && playedSongsArrayList.size() > 0) {

                                        for (PlayerStatus playerStatus : playedSongsArrayList) {

                                            if (songTitleId.equalsIgnoreCase(playerStatus.getTitleIdSong())
                                                    && songPlayedTime.equalsIgnoreCase(playerStatus.getPlayerDateTimeSong())
                                                    && songUpdated.equalsIgnoreCase("1")){

                                                playerStatusDataSource.deletePlayedSongStatusForTime("song", songTitleId,songPlayedTime);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        playerStatusDataSource.close();
                    }


        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            /*Update heartbeat status*/
            updateDownloadedSongsPlaylistWise();
        }

    }

    private void handleUpdatedHeartBeatStatusResponse(String response){

        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("heartbeat");
//                Toast.makeText(this.context, "All status updated", Toast.LENGTH_SHORT).show();
            }
            sendPlayedAdsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleUpdatedAdvertisementStatusResponse(String response){

        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){

//                Toast.makeText(this.context, "All status updated", Toast.LENGTH_SHORT).show();
            }
            /* TODO Update advertisement status*/
            updateDownloadedSongsPlaylistDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
