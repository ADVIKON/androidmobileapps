package com.advikonsignagekeyboardplayer.mediamanager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.advikonsignagekeyboardplayer.activities.LoginActivity;
import com.advikonsignagekeyboardplayer.activities.Splash_Activity;
import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.application.AlenkaMedia;
import com.advikonsignagekeyboardplayer.database.AdvertisementDataSource;
import com.advikonsignagekeyboardplayer.database.AnnouncementDataSource;
import com.advikonsignagekeyboardplayer.database.PlaylistDataSource;
import com.advikonsignagekeyboardplayer.database.SongsDataSource;
import com.advikonsignagekeyboardplayer.eventbus.MessageEvent;
import com.advikonsignagekeyboardplayer.interfaces.PlaylistLoaderListener;
import com.advikonsignagekeyboardplayer.models.Advertisements;
import com.advikonsignagekeyboardplayer.models.Announcement;
import com.advikonsignagekeyboardplayer.models.Playlist;
import com.advikonsignagekeyboardplayer.models.Songs;
import com.advikonsignagekeyboardplayer.utils.AlenkaMediaPreferences;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by love on 29/5/17.
 */
public class PlaylistManager implements OkHttpUtil.OkHttpResponse {

    public static final String TAG = "PlaylistManager";

    Context context;

    PlaylistDataSource playlistDataSource = null;

    PlaylistLoaderListener playlistLoaderListener;

    ArrayList<String> schIdArrayList = new ArrayList<String>();

    SongsDataSource songsDataSource = null;

    AnnouncementDataSource announcementDataSource = null;

    int currentlyDownloadingSongsFromPlaylistAtIndex = 0;

    ArrayList<String> titleId = new ArrayList<>();
    ArrayList<String> advId = new ArrayList<>();
    ArrayList<String> announcementIds = new ArrayList<>();

    String splid;

    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Playlist> allplaylists = new ArrayList<>();

    private AdvertisementDataSource advertisementDataSource;

    public PlaylistManager(Context context, PlaylistLoaderListener playlistLoaderListener){
        this.context = context;
        playlistDataSource = new PlaylistDataSource(this.context);
        songsDataSource = new SongsDataSource(this.context);
        advertisementDataSource = new AdvertisementDataSource(this.context);
        announcementDataSource = new AnnouncementDataSource(this.context);
        this.playlistLoaderListener = playlistLoaderListener;
    }

    public void getPlaylistsFromServer(){

        if (this.playlistLoaderListener != null){
            this.playlistLoaderListener.startedGettingPlaylist();
        }

        try{
            JSONObject json = new JSONObject();

            json.put("DfClientId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID));
            json.put("TokenId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.TOKEN_ID));
            json.put("WeekNo", Utilities.getCurrentDayNumber());

/*
            new OkHttpUtil(context, Constants.GetSplPlaylist_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GetSplPlaylist_TAG).
                    execute();
*/

            new OkHttpUtil(context, Constants.GetSplPlaylist_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GetSplPlaylist_TAG).
                    callRequest();


        } catch (Exception e){
            e.printStackTrace();
        }
    }



    public void getAdvertisements(){

        try{
            JSONObject json = new JSONObject();

            json.put("Cityid", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.City_ID));
            json.put("CountryId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.Country_ID));
            json.put("CurrentDate", Utilities.currentFormattedDate());
            json.put("DfClientId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID));
            json.put("StateId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.State_Id));
            json.put("TokenId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.TOKEN_ID));
            json.put("WeekNo", Utilities.getDayNumberForAdv());

            new OkHttpUtil(context, Constants.ADVERTISEMENTS,json.toString(),
                    PlaylistManager.this,false,
                    Constants.ADVERTISEMENTS_TAG).
                    callRequest();


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getAnnouncements(){

        try{
            JSONObject json = new JSONObject();
            json.put("Tokenid", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.TOKEN_ID));

            new OkHttpUtil(context, Constants.ANNOUNCEMENTS,json.toString(),
                    PlaylistManager.this,false,
                    Constants.ANNOUNCEMENTS_TAG).
                    callRequest();


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Playlist> getCurrentPlaylist(){

        try {
            playlistDataSource.open();
            /*Commented method returns the playlist for current time only and not for the future times.*/
//            return playlistDataSource.getAllPlaylists();

            /*This method returns the playlist for current time and the future times.*/
            return playlistDataSource.getPlaylistsForCurrentAndComingTime();
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            playlistDataSource.close();
        }
        return null;
    }

    /*This method returns playlists for current time only and not for future times.*/

    public ArrayList<Playlist> getPlaylistForCurrentTimeOnly(){

        try {
            playlistDataSource.open();

            return playlistDataSource.getAllPlaylists();

        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            playlistDataSource.close();
        }
        return null;
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null){
            //Toast.makeText(this.context, "Empty response", Toast.LENGTH_SHORT).show();
            return;
        }

        switch(tag){

            case Constants.GetSplPlaylist_TAG :{
//                Toast.makeText(this.context, "GetSplPlaylist_TAG", Toast.LENGTH_SHORT).show();
                handleGettingPlaylistResponse(response);
            }break;

            case Constants.GET_SPL_PLAY_LIST_TITLES_TAG:{
//                Toast.makeText(this.context, "GET_SPL_PLAY_LIST_TITLES_TAG", Toast.LENGTH_SHORT).show();
                handleGetSongsResponse(response);
            }break;

            case Constants.ADVERTISEMENTS_TAG:{
//                Toast.makeText(this.context, "ADVERTISEMENTS_TAG", Toast.LENGTH_SHORT).show();
                handleResponseForAdvertisements(response);
            }break;

            case Constants.CHECK_TOKEN_PUBLISH_TAG:{
                handleResponseForTokenPublish(response);
            }break;

            case Constants.UPDATE_TOKEN_PUBLISH_TAG:{
                handleResponseForTokenUpdatedOnServer(response);
            }break;

            case Constants.ANNOUNCEMENTS_TAG:{
                handleAnnouncementsResponse(response);
            }break;
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.context.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void handleAnnouncementsResponse(String response) {

        if (response.equalsIgnoreCase("[]")){

            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.finishedGettingPlaylist();
            }

            try {
                Activity activity = (Activity)this.context;

                if (activity instanceof Splash_Activity || activity instanceof LoginActivity){
                    activity.finish();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }
        try {
            announcementIds.clear();
            announcementDataSource.open();
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String announcementId = jsonObject.getString("id");
                String announcementUrl = jsonObject.getString("url");
                Integer announcementSerialNumber = jsonObject.getInt("srno");


                if (announcementId != null && announcementUrl != null) {

                    announcementIds.add(announcementId);

                    Announcement announcement = new Announcement();

                    announcement.setSerialNo(announcementSerialNumber);
                    announcement.setAncID(announcementId);
                    announcement.setAncFileUrl(announcementUrl);
                    announcementDataSource.checkifExistAnc(announcement);
                }
            }

        }catch (Exception e)
        {
            e.getCause();
        }
        finally {
            deleteExtraAnnouncements();
            announcementDataSource.close();

            if (this.playlistLoaderListener != null) {
                this.playlistLoaderListener.finishedGettingPlaylist();
            }

            try {
                Activity activity = (Activity) this.context;

                if (activity instanceof Splash_Activity || activity instanceof LoginActivity) {
                    activity.finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
                final String h = e.getMessage();
            }
        }

    }

    private void handleResponseForTokenUpdatedOnServer(String response) {


        try {

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null){

                if (jsonArray.length() > 0){

                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    String isPublishUpdate = jsonObject.getString("IsPublishUpdate");

                    if (isPublishUpdate.equals("1")){

                        if (this.playlistLoaderListener != null){
                            this.playlistLoaderListener.tokenUpdatedOnServer();
                        }

                    }
                }
            }



        }catch (Exception e){

            e.printStackTrace();
        }
    }

    private void handleResponseForTokenPublish(String response) {

        try {

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null){

                if (jsonArray.length() > 0){

                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    checkAndUpdateNewSettings(jsonObject);
                    String isPublishUpdate = jsonObject.getString("IsPublishUpdate");

                    Log.e(TAG,"IsPublishUpdate value: " + isPublishUpdate);

                    if (isPublishUpdate.equals("1")){

                        Log.e(TAG,"Get new data.");
                        AlenkaMedia.getInstance().isUpdateInProgress = true;
                        getPlaylistsFromServer();
                        return;
                    }
                }
            }

            AlenkaMedia.getInstance().isUpdateInProgress = false;

        }catch (Exception e){

            e.printStackTrace();
        }
    }

    private void checkAndUpdateNewSettings(JSONObject jsonObject){

        if (jsonObject == null){
            return;
        }

        try {

            boolean isNewValuePresent = false;

            Integer newTotalShotsCount = jsonObject.getInt("TotalShot");
            String newTankAlerts = jsonObject.getString("DispenserAlert");
            Boolean newIsDemoToken = jsonObject.getBoolean("IsDemoToken");

            if (newIsDemoToken != null) {

                Boolean currentDemoToken =  SharedPreferenceUtil.getBooleanPreference(context, AlenkaMediaPreferences.isDemoToken, false);

                if (currentDemoToken != newIsDemoToken){
                    SharedPreferenceUtil.setBooleanPreference(context, AlenkaMediaPreferences.isDemoToken,newIsDemoToken);
                    isNewValuePresent = true;
                }
            }
            if (newTotalShotsCount != null) {

                Integer currentTotalShotsCount =  SharedPreferenceUtil.getIntegerPreference(context, AlenkaMediaPreferences.totalShotsCount, -1);

                if (currentTotalShotsCount != newTotalShotsCount){
                    SharedPreferenceUtil.setIntegerPreference(context, AlenkaMediaPreferences.totalShotsCount,newTotalShotsCount);
                    isNewValuePresent = true;
                }
            }
            if (newTankAlerts != null) {

                String currentTankAlerts =  SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.tankAlertPercentages);

                if (!currentTankAlerts.equals(newTankAlerts) ){
                    SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.tankAlertPercentages,newTankAlerts);
                    isNewValuePresent = true;
                }
            }

            if (isNewValuePresent){
                EventBus.getDefault().post(new MessageEvent(true));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Songs> getUnschdSongs(){
        try {
            songsDataSource.open();
            return songsDataSource.getUnschdSongsThoseAreNotDownloaded();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    @Override
    public void onError(Exception e, int tag) {
        if (this.playlistLoaderListener != null){
            this.playlistLoaderListener.errorInGettingPlaylist();
        }
    }

    private void handleGettingPlaylistResponse(String response) {

        try {

            playlistDataSource.open();
            schIdArrayList.clear();
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Playlist modal = new Playlist();
                String startTime = jsonObject.getString("StartTime");
                String endTime = jsonObject.getString("EndTime");

                //TODO: Here we change the format of date & Time
                String startTime1 = Utilities.changeDateFormat(startTime);
                String endTime1 = Utilities.changeDateFormat(endTime);

                //TODO: Here change the Date & Time in Milliseconds

                long startTimeInMilli = Utilities.getTimeInMilliSec(startTime1);
                long endTimeInMilli = Utilities.getTimeInMilliSec(endTime1);
                modal.setStart_Time_In_Milli(startTimeInMilli);
                modal.setEnd_Time_In_Milli(endTimeInMilli);
                modal.setStart_time(startTime);
                modal.setEnd_time(endTime);

                modal.setFormat_id(jsonObject.getString("FormatId"));
                modal.setdfclient_id(jsonObject.getString("dfclientid"));
                modal.setPlaylistCategory(jsonObject.getString("IsMute"));
                modal.setpSc_id(jsonObject.getString("pScid"));
                modal.setsplPlaylist_Id(jsonObject.getString("splPlaylistId"));
                modal.setsplPlaylist_Name(jsonObject.getString("splPlaylistName"));
                modal.setIsSeparatinActive(jsonObject.getLong("IsSeprationActive"));
                schIdArrayList.add(modal.getpSc_id());
                playlistDataSource.checkifPlaylistExist(modal);

            }

        } catch (SQLException e) {
            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.errorInGettingPlaylist();
            }

            e.printStackTrace();
        } catch (JSONException e) {
            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.errorInGettingPlaylist();
            }
            e.printStackTrace();
        } finally {
            playlistDataSource.close();
            if (this.playlistLoaderListener != null){
//                this.playlistLoaderListener.finishedGettingPlaylist();
            }
            deletExtraPlaylists();

            getSongsForAllPlaylists();
        }
    }

    private ArrayList<Songs> getSongsToBeDownloaded(){

        ArrayList<Playlist> playlists = new PlaylistManager(PlaylistManager.this.context, null).getPlaylistFromLocallyToBedDownload();
        ArrayList<Songs> songsToBeDownloaded = null;

        if (playlists.size() > 0) {

            PlaylistManager songsLoader = new PlaylistManager(PlaylistManager.this.context, null);
            songsToBeDownloaded = new ArrayList<>();
            for (Playlist playlist : playlists) {

                ArrayList<Songs> songs = songsLoader.getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

                if (songs != null && songs.size() > 0) {

//                    if (playlist.getIsSeparatinActive() == 0){
//                    }

                    songsToBeDownloaded.addAll(songs);
                }
            }
            songsLoader = null;

            if (songsToBeDownloaded.size() > 0) {
                return songsToBeDownloaded;
            }
        }
        return null;
    }

    private void handleGetSongsResponse(String response){

        if (response.equalsIgnoreCase("[]")){

            if (getSongsToBeDownloaded() != null && getSongsToBeDownloaded().size() > 0){

                if (this.playlistLoaderListener != null){
                    this.playlistLoaderListener.finishedGettingPlaylist();
                }

                try {
                    Activity activity = (Activity)this.context;

                    if (activity instanceof Splash_Activity || activity instanceof LoginActivity){
                        activity.finish();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return;
        }

        try {

            titleId.clear();
            songsDataSource.open();

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null && jsonArray.length() > 0){

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Songs modal = new Songs();
                    modal.setAlbum_ID(jsonObject.getString("AlbumID"));
                    modal.setArtist_ID(jsonObject.getString("ArtistID"));
                    modal.setTitle(jsonObject.getString("Title"));
                    modal.setTitle_Url(jsonObject.getString("TitleUrl"));
                    modal.setAl_Name(jsonObject.getString("alName"));
                    modal.setAr_Name(jsonObject.getString("arName"));
                    modal.setSpl_PlaylistId(jsonObject.getString("splPlaylistId"));
                    modal.setT_Time(jsonObject.getString("tTime"));
                    modal.setTitle_Id(jsonObject.getString("titleId"));
                    modal.setSerialNo(jsonObject.getLong("srno"));
                    modal.setFilesize(jsonObject.getString("FileSize"));
                    titleId.add(modal.getTitle_Id());
                    splid = modal.getSpl_PlaylistId();
                    modal.setIs_Downloaded(0);
                    // TODO: Check for song if song exist then skip else insert
                    songsDataSource.checkifSongExist(modal,this.context);
//                    modalSongList.add(modal);
                }
            } else {
                Toast.makeText(this.context, "No songs in playlist.", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {

            songsDataSource.close();

            deleteExtraSongs();

            //If there are more playlists whose songs are not retreived get them

            if (currentlyDownloadingSongsFromPlaylistAtIndex < playlists.size() - 1){
                currentlyDownloadingSongsFromPlaylistAtIndex++;
                startDownloadingSongsForPlaylistWithPlaylistID(currentlyDownloadingSongsFromPlaylistAtIndex);
            } else {

                songsDataSource.close();

                getAdvertisements();
            }
        }
    }

    private void handleResponseForAdvertisements(String response){
        if (response.equalsIgnoreCase("[]")){
            try {
                Activity activity = (Activity)this.context;

                if (activity instanceof Splash_Activity || activity instanceof LoginActivity){
                    activity.finish();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return;


        }
        try {
             advId.clear();
             advertisementDataSource.open();
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.getString("Response").equals("1")) {

                    Advertisements modal_Adv = new Advertisements();
                    //String Response = jsonObject.getString("Response");
                    modal_Adv.setAdvFileUrl(jsonObject.getString("AdvtFilePath"));
                    modal_Adv.setAdvtID(jsonObject.getString("AdvtId"));
                    modal_Adv.setAdvtName(jsonObject.getString("AdvtName"));
                    modal_Adv.setIsMinute(jsonObject.getString("IsMinute"));
                    modal_Adv.setIsSong(jsonObject.getString("IsSong"));
                    modal_Adv.setIsTime(jsonObject.getString("IsTime"));
                    modal_Adv.setPlayingType(jsonObject.getString("PlayingType"));
                    modal_Adv.setSRNo(jsonObject.getString("SrNo"));
                    modal_Adv.setTotalMinutes(jsonObject.getString("TotalMinutes"));
                    modal_Adv.setTotalSongs(jsonObject.getString("TotalSongs"));
                    modal_Adv.seteDate(jsonObject.getString("eDate"));
                    modal_Adv.setsDate(jsonObject.getString("sDate"));


                    modal_Adv.setsTime(jsonObject.getString("sTime"));
                    advId.add(modal_Adv.getAdvtID());

                    String edate = jsonObject.getString("eDate");
                    String sdate = jsonObject.getString("sDate");
                    String sTime = jsonObject.getString("sTime");

                       /* if (i == 0){
                            sTime = "12:15 AM";
                            modal_Adv.setsTime(sTime);
                        } else {
                            sTime = "12:18 AM";
                            modal_Adv.setsTime(sTime);
                        }*/

                    //TODO: Get sTime in milliseconds
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat mdformat = new SimpleDateFormat("dd-MMM-yyyy");
                    String strDate = mdformat.format(calendar.getTime());

                    String play_Adv_Time = strDate + " " + sTime;
                    //TODO : cahnge format using Prayer Method for Advertisement Type Is_Time
                    String formated_time = Utilities.changeDateFormatForPrayer(play_Adv_Time);
                    //TODO : get Time in milliseconds using Prayer Method for Advertisement Type Is_Time
                    long timeInMilli = Utilities.getTimeInMilliSecForPrayer(formated_time);
                    modal_Adv.setStart_Adv_Time_Millis(timeInMilli);
                    modal_Adv.setStatus_Download(0);
                    String playing_type = modal_Adv.getPlayingType(); // Hard Stop

                    SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.playing_Type, playing_type); //// TODO: 17/6/17 Change this


                    // String playing_type = modal_Adv.getPlayingType(); // Hard Stop

                    String is_Minute = modal_Adv.getIsMinute(); //1
                    String totalMinutes = modal_Adv.getTotalMinutes(); // 5
                    //                        is_Minute = "1"; //TODO Remove this value - for testing only
                    //                        totalMinutes = "3";

                    String is_Song = modal_Adv.getIsSong(); // 0
                    String totalSongs = modal_Adv.getTotalSongs();
                    //    is_Song = "0";
                    //   totalSongs = "0";

                    String isTime = modal_Adv.getIsTime();
                    SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_Time_Adv, isTime);

                    //    isTime = "1";

                    if (is_Minute.equals("1")) {
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_song_Adv, "");
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_Minute_Adv, is_Minute);
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.total_minute_after_adv_play, totalMinutes);

                    } else if (is_Song.equals("1")) {
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_Minute_Adv, "");
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_song_Adv, is_Song);
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.total_Songs, totalSongs);

                    } else if (isTime.equals("1")) {
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_song_Adv, "");
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_Minute_Adv, "");
                        SharedPreferenceUtil.setStringPreference(this.context, AlenkaMediaPreferences.is_Time_Adv, isTime);

                        //                            playing_type = "Soft Stop";
                    }


                    //stringArray[i]=jsonObject.getString("AdvtId");
                    advertisementDataSource.checkifExistAdv(modal_Adv);

                }
            }

        }catch (Exception e)
        {
            e.getCause();
        }
        finally {
            deletExtraAds();
            advertisementDataSource.close();

            getAnnouncements();
        }

    }


    public ArrayList<Songs> getDownloadedSongsForPlaylistID(String playlistID) {

        ArrayList<Songs> arrayList = new ArrayList<>();

        try {
            songsDataSource.open();

            arrayList = songsDataSource.getSongsThoseAreDownloaded(playlistID);

            songsDataSource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;

    }

    public ArrayList<Playlist> getAllPlaylistInPlayingOrder() {

        ArrayList<Playlist> arrayList = null;

        try {
            playlistDataSource.open();

            arrayList = playlistDataSource.getAllPlaylistsInPlayingOrder();

            playlistDataSource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public ArrayList<Songs> getAllDownloadedSongs(String songid){

        try {
            songsDataSource.open();

            return songsDataSource.getAllDownloadedSongs(songid);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteExtraSongs(){

        try {

            songsDataSource.open();

            ArrayList<Songs> arrayList = songsDataSource.getSongListNotAvailableinWebResponse
                    (Arrays.copyOf(titleId.toArray(), titleId.toArray().length, String[].class),splid);
            if (arrayList.size() > 0) {
                for (int k = 0; k < arrayList.size(); k++) {
                    String songpath = arrayList.get(k).getSongPath();
                    File file = new File(songpath);
//                    file.delete();
                    songsDataSource.deleteSongs(arrayList.get(k),false);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            songsDataSource.close();
        }
    }

    public void getSongsForAllPlaylists(){

       // allplaylists.addAll(getAllPlaylistInPlayingOrder());
        playlists.addAll(getAllPlaylistInPlayingOrder());

        if (playlists.size() > 0){

            startDownloadingSongsForPlaylistWithPlaylistID(currentlyDownloadingSongsFromPlaylistAtIndex);
        } else {

            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.finishedGettingPlaylist();
            }
          //  Toast.makeText(this.context, "No songs for current time.", Toast.LENGTH_SHORT).show();
        }

    }
    private void startDownloadingSongsForPlaylistWithPlaylistID(int index){

        getSongsForPlaylistId(playlists.get(index).getsplPlaylist_Id());


    }

    private void getSongsForPlaylistId(String playlistId){

        try {

            JSONObject json = new JSONObject();
            json.put("splPlaylistId", playlistId);
            Log.e(TAG, "json" + json);

/*
            new OkHttpUtil(context, Constants.GET_SPL_PLAY_LIST_TITLES_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GET_SPL_PLAY_LIST_TITLES_TAG).
                    execute();
*/

            new OkHttpUtil(context, Constants.GET_SPL_PLAY_LIST_TITLES_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GET_SPL_PLAY_LIST_TITLES_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deletExtraPlaylists(){
        try{
            playlistDataSource.open();

            ArrayList<Playlist> arrayList = playlistDataSource.
                    getListNotAvailableinWebResponse(
                            Arrays.copyOf(schIdArrayList.toArray(),
                                    schIdArrayList.toArray().length,
                                    String[].class));

            if (arrayList.size() > 0) {

                songsDataSource.open();
                //TODO: check if playlist id not refer in other schid record if not exist then delete all songs else dont
                for (int k = 0; k < arrayList.size(); k++) {
                    if (playlistDataSource.checkifPlaylistExist(arrayList.get(k).getpSc_id())) {
                       // songsDataSource.deleteSongsWithPlaylist(arrayList.get(k).getpSc_id());
                    }
                    playlistDataSource.deletePlaylist(arrayList.get(k));
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            playlistDataSource.close();
            songsDataSource.close();
        }
    }

    private void deletExtraAds(){
        try{
            advertisementDataSource.open();

            ArrayList<Advertisements> arrayList = advertisementDataSource.
                    getListNotAvailableinWebResponse(
                            Arrays.copyOf(advId.toArray(),
                                    advId.toArray().length,
                                    String[].class));
            if (arrayList.size() > 0) {
                for (int k = 0; k < arrayList.size(); k++) {
                    String songpath = arrayList.get(k).getAdvtFilePath();
                    File file = new File(songpath);
//                    file.delete();
                    advertisementDataSource.deleteAds(arrayList.get(k),false);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            advertisementDataSource.close();
        }
    }

    private void deleteExtraAnnouncements(){
        try{
            announcementDataSource.open();

            ArrayList<Announcement> arrayList = announcementDataSource.
                    getListNotAvailableinWebResponse(
                            Arrays.copyOf(announcementIds.toArray(),
                                    announcementIds.toArray().length,
                                    String[].class));
            if (arrayList.size() > 0) {
                for (int k = 0; k < arrayList.size(); k++) {
                    announcementDataSource.deleteAnnouncements(arrayList.get(k),true);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            announcementDataSource.close();
        }
    }


    private ArrayList<Playlist> getPlaylistFromLocally() {
        ArrayList<Playlist> arrayList = null;
        ArrayList<Playlist> remaingArrayist = null;
        try {
            playlistDataSource.open();
            arrayList = playlistDataSource.getAllPlaylists();
            remaingArrayist = playlistDataSource.getRemainingAllPlaylists();

            if (arrayList.size() > 0) {

//                 TODO Add the playlists whose time to play is gone.
            }
            arrayList.addAll(remaingArrayist);
            playlistDataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void songDownloaded(Songs songs, PlaylistLoaderListener songsLoaderListener){
        try {
            songsDataSource.open();
            songsDataSource.updateSongsListWithDownloadstatusandPath(songs);

        }catch (Exception e){
            e.printStackTrace();
            if (songsLoaderListener != null){
                songsLoaderListener.recordSaved(false);
            }
        }finally {
            songsDataSource.close();

            if (songsLoaderListener != null){
                songsLoaderListener.recordSaved(true);
            }
        }
    }

    public ArrayList<Songs> getSongsThatAreNotDownloaded(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreNotDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getAllSongss(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getSongsForPlaylistRandom(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getAllSongssRandom(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getDownloadedSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getNotDownloadedSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreNotDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    /*This method provides the playlists which will play after current time.*/
    public ArrayList<Playlist> getPlaylistFromLocallyToBedDownload() {

        ArrayList<Playlist> arrayList = null;
        ArrayList<Playlist> remaingArrayist = null;

        try {
            playlistDataSource.open();

            arrayList = playlistDataSource.getPlaylistsForCurrentAndComingTime();


            remaingArrayist = playlistDataSource.getRemainingAllPlaylists();

            ArrayList<Playlist> arrayListGoneTime = new ArrayList<>();

            if (arrayList.size() > 0) {
                if (arrayListGoneTime.size() > 0) {
                    remaingArrayist.addAll(arrayListGoneTime);
                }
            }
            /*We are not adding the playlists for the playlists whose time has gone.*/
//            arrayList.addAll(remaingArrayist);
            playlistDataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void checkUpdatedPlaylistData(){

        Log.e(TAG,"Checking for new data");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Tokenid", SharedPreferenceUtil.
                    getStringPreference(PlaylistManager.this.context, Constants.TOKEN_ID));

            new OkHttpUtil(context, Constants.CHECK_TOKEN_PUBLISH,jsonObject.toString(),
                    PlaylistManager.this,false,
                    Constants.CHECK_TOKEN_PUBLISH_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void publishTokenForUpdatedData(){

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Tokenid", SharedPreferenceUtil.
                    getStringPreference(PlaylistManager.this.context, Constants.TOKEN_ID));

            new OkHttpUtil(context, Constants.UPDATE_TOKEN_PUBLISH,jsonObject.toString(),
                    PlaylistManager.this,false,
                    Constants.UPDATE_TOKEN_PUBLISH_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getTotalDownloadedSongs(){

        try {
            songsDataSource.open();

            return songsDataSource.getCountForTotalSongsDownloaded();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSongsCountForPlaylistId(String playlistId){

        int count = 0;

        try {
            playlistDataSource.open();
            if (playlistDataSource != null){
                count = playlistDataSource.getSongsCountForPlaylistId(playlistId);
            }
            return count;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}