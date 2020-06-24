package com.NuSignSignagePlayer.alarm_manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import com.NuSignSignagePlayer.application.AlenkaMedia;
import com.NuSignSignagePlayer.mediamanager.AdvertisementsManager;
import com.NuSignSignagePlayer.mediamanager.PlaylistManager;
import com.NuSignSignagePlayer.models.Advertisements;
import com.NuSignSignagePlayer.models.Playlist;
import com.NuSignSignagePlayer.utils.AlenkaMediaPreferences;
import com.NuSignSignagePlayer.utils.SharedPreferenceUtil;
import com.NuSignSignagePlayer.utils.Utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by love on 3/6/17.
 */
public class PlaylistWatcher {


    /***********************************************************
     *
     * Constant for when no playlist is present at current time.
     *
     ***********************************************************/
    public static final int NO_PLAYLIST = 0;


    /***********************************************************
     *
     * Constant for when a playlist is present at current time.
     *
     ***********************************************************/
    public static final int PLAYLIST_PRESENT = 1;


    /************************PLAYLIST_CHANGE********************
     *
     * Constant for when a playlist is present at current time and
     * the next playlist is present without any gap.
     * For example end time of Playlist A is 4:00 pm and the start
     * time of Playlist B is also 4:00 pm. In this case we stop
     * current playback and start the new one.
     *
     ***********************************************************/

    public static final int PLAYLIST_CHANGE = 2;


    /*****************currentDayOfTheWeek************************
     * This variable indicates the current day of week as an integer.
     * For ex 1 for Monday 2 for Tuesday and so on.
     ***********************************************************/
    private static int currentDayOfTheWeek = -1;


    /******************currentPlaylistID***********************
     * ID of the playlist currently playling.
     **********************************************************/
    public static String currentPlaylistID = "";
    public long milliSec;
    public String formattedDate="";

    /**********************************************************
     * Handler that checks the playlists time. This runs every
     * 10 second
     ***********************************************************/
    private Handler mHandler = null;

    /***********************************************************
     * This handler is used to run the mHandler in background.
     ************************************************************/
    private HandlerThread mHandlerThread = null;

    Context context;

    private long IsPlaylistFind=0;
    private long playlistcounter=0;
    private String cTime="";
    String playlistId="";
    private String cDate="";
    private PlaylistStatusListener playlistStatusListener;

    private static int UPDATE_PLAYER_STATUS_TIME = 600 * 1000; //15 minutes 900

    private static long UPDATE_PLAYER_STATUS_TIMER = 0;

    private static int CHECK_PENDING_DOWNLOADS_TIME = 600 * 1000; //15 minutes 900

    private static long CHECK_PENDING_DOWNLOADS_TIMER = 0;

    private static String ADVERTISEMENT_TYPE = ""; // 1 for isMinute, 2 for isSong, 3 is for isTime

    private static int TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = -1;
    private ArrayList<Playlist> playlists;
    private ArrayList<Playlist> playlistsAll;

    /*******************ADVERTISEMENT_TIME_COUNTER*******************
     * ADVERTISEMENT_TIME_COUNTER is used to check the time for an ad
     * to play. Initial value is 0 and after every 10 seconds the value
     * is increased to 10 seconds(in milliseconds). When the value becomes
     * equal to TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY we play the advertisement.
     ****************************************************************/
    public static long ADVERTISEMENT_TIME_COUNTER = 0;

    /*************************ADVERTISEMENT_TIME********************
     * ADVERTISEMENT_TIME indicates the number of minutes(in milliseconds)
     * after which the ad will play. Default value is 1 and actual value we
     * get from server.
     ****************************************************************/
    private static int ADVERTISEMENT_TIME = 1;

    /******************PLAY_AD_AFTER_SONGS****************************
     * This variable is used when ADVERTISEMENT_TYPE is of type 2
     * that is when advertisement is to be played after a number
     * songs have played.
     *****************************************************************/
    public static int PLAY_AD_AFTER_SONGS = -1;

    /****************PLAY_AD_AFTER_SONGS_COUNTER************************
     * This variable is used to keep track the number of songs that have been
     * played. After value of this variable reaches PLAY_AD_AFTER_SONGS we play
     * an ad and this value is reset to 0.
     *
     *******************************************************************/
    public static long PLAY_AD_AFTER_SONGS_COUNTER = 0;

    /****************PLAYLIST_TIMER_CHECK_TIMER************************
     * This is the value in seconds in which the handler runs every mentioned
     * seconds.
     *******************************************************************/
    public static long PLAYLIST_TIMER_CHECK_TIMER = 1;

    /****************ADVERTISEMENT_PLAY_TIME************************
     * When advertisement is of type isTime. This variable will keep
     * track of the time on which the next song advertisement is to be
     * played.
     ******************************************************************/
    public static String ADVERTISEMENT_PLAY_TIME = "";

    ArrayList<Advertisements> advertisements;

    private static int currentlyPlayingAdAtIndex = 0;

    private boolean isPaused = false;

    public interface PlaylistStatusListener {
        void onPlaylistStatusChanged(int status);
        void shouldUpdateTimeOnServer();
        void playAdvertisement();
        void checkForPendingDownloads();
        void refreshPlayerControls();
    }

    public void setContext(Context context){

        this.context = context;
        setAdvertisements();
    }

    public void setPlaylistStatusListener(PlaylistStatusListener playlistStatusListener) {
        this.playlistStatusListener = playlistStatusListener;
    }

   /* public String datatableplaylistid()
    {
        String datatableplaylistid="";
        Date date = new Date();
        long currenttimeinmilli=date.getTime();

        int datatablecount=HomeActivity.playlistdatatable.getChildCount();
        for(int i=0;i<=datatablecount;i++)
        {
            TableRow mRow = (TableRow) HomeActivity.playlistdatatable.getChildAt(i);
            long stime=Long.parseLong(mRow.getChildAt(1).toString());
            long etime=Long.parseLong(mRow.getChildAt(2).toString());
            if((stime<=currenttimeinmilli) && (etime>=currenttimeinmilli))
            {
                datatableplaylistid=mRow.getChildAt(0).toString();
                break;
            }

        }
        return datatableplaylistid;
    }*/


    private String changeDateFormat(String starttime) {
        DateFormat readFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa",Locale.US);
        DateFormat writeFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",Locale.US);
        Date date = null;
        try {
            date = readFormat.parse(starttime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            formattedDate = writeFormat.format(date);
        }
        return formattedDate;
    }

    public long getTimeInMilliSec(String starttime) {


        SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",Locale.US);
        try {
            Date mDate = sdf1.parse(starttime);
            milliSec=mDate.getTime();
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(mDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return milliSec;
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (PlaylistWatcher.this.playlistStatusListener != null){
                PlaylistWatcher.this.playlistStatusListener.refreshPlayerControls();
            }

            if (ADVERTISEMENT_TYPE.equals("1")){ //isMinuteAdvertisement

                ADVERTISEMENT_TIME_COUNTER = ADVERTISEMENT_TIME_COUNTER + (500 * PLAYLIST_TIMER_CHECK_TIMER);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(ADVERTISEMENT_TIME_COUNTER);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(ADVERTISEMENT_TIME_COUNTER);

                long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(ADVERTISEMENT_TIME);

                //Logger.e("Total time = " + totalMinutes);
                //Logger.e("Current time in seconds = " + seconds);
                //Logger.e("Current time in minutes = " + minutes);

                if (ADVERTISEMENT_TIME_COUNTER == ADVERTISEMENT_TIME){
                    // Play advertisement;
//                    Toast.makeText(PlaylistWatcher.this.context, "Should play advertisement", Toast.LENGTH_SHORT).show();

                    if (PlaylistWatcher.this.playlistStatusListener != null){
                        PlaylistWatcher.this.playlistStatusListener.playAdvertisement();
                    }
                }
            }

            if (ADVERTISEMENT_TYPE.equals("3")){

                String timeStamp = new SimpleDateFormat("hh:mm aa", Locale.US).format(Calendar.getInstance().getTime());

                if (timeStamp.equals(ADVERTISEMENT_PLAY_TIME)){
                       Toast.makeText(PlaylistWatcher.this.context, "Play ad for isTime", Toast.LENGTH_SHORT).show();

                    if (PlaylistWatcher.this.playlistStatusListener != null){
                        PlaylistWatcher.this.playlistStatusListener.playAdvertisement();
                    }

                    currentlyPlayingAdAtIndex++;

                    if (advertisements.size() - 1 >= currentlyPlayingAdAtIndex){

                        String playAdAtTime = advertisements.get(currentlyPlayingAdAtIndex).getsTime();

                        if (playAdAtTime != null){
                            ADVERTISEMENT_PLAY_TIME = playAdAtTime;
                        }
                    } else {
                        ADVERTISEMENT_PLAY_TIME = "";
                    }
                }
            }

            UPDATE_PLAYER_STATUS_TIMER = UPDATE_PLAYER_STATUS_TIMER + (1000 * PLAYLIST_TIMER_CHECK_TIMER);

//            Log.e("Playlist Watcher", "Timer Value " + UPDATE_PLAYER_STATUS_TIMER);

            if (UPDATE_PLAYER_STATUS_TIMER == UPDATE_PLAYER_STATUS_TIME){

                if (PlaylistWatcher.this.playlistStatusListener != null){
                    PlaylistWatcher.this.playlistStatusListener.shouldUpdateTimeOnServer();
                }

                UPDATE_PLAYER_STATUS_TIMER = 0;
            }

            CHECK_PENDING_DOWNLOADS_TIMER = CHECK_PENDING_DOWNLOADS_TIMER + 1000 * PLAYLIST_TIMER_CHECK_TIMER;

            if (CHECK_PENDING_DOWNLOADS_TIME == CHECK_PENDING_DOWNLOADS_TIMER){

                if (PlaylistWatcher.this.playlistStatusListener != null){
                    PlaylistWatcher.this.playlistStatusListener.checkForPendingDownloads();
                }

                CHECK_PENDING_DOWNLOADS_TIMER = 0;
            }

            if (currentDayOfTheWeek == -1){
                currentDayOfTheWeek = Utilities.getCurrentDayNumber();
            }

            if (currentDayOfTheWeek != Utilities.getCurrentDayNumber()){
                currentDayOfTheWeek = Utilities.getCurrentDayNumber();
                new PlaylistManager(context,null).getPlaylistsFromServer();
                try {
                    TimeUnit.SECONDS.sleep(10);
                }
                catch (InterruptedException e)

                {
                    e.getCause();
                }


                //context.startActivity(new Intent(context, Splash_Activity.class));
                //Activity activity = (Activity)context;
                //activity.finish();
            }

            String currentDate=Utilities.currentDate();

            int playlistStatus = -1;
            boolean shouldPlaylistChange = false;
            if(!cDate.equals(currentDate)) {
                cDate=currentDate;
                playlistsAll = new PlaylistManager(context, null).getAllPlaylistInPlayingOrder();
            }

            String currentTime=Utilities.currentTimeHHMM();
            Calendar calander;
            SimpleDateFormat simpleDateFormat;
            String time;
            if(!cTime.equals(currentTime)) {
                cTime = currentTime;
                if ((playlistsAll == null) || (playlistsAll.size() == 0)) {
                    cDate = "";
                } else if ((playlistsAll != null) && (playlistsAll.size() > 0)) {

                    calander = Calendar.getInstance();
                    simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa", Locale.US);
                    time = simpleDateFormat.format(calander.getTime());

                    String array[]=time.split("\\s+");
                    time=array[1]+" "+array[2];

                    String plcurrentTime=changeDateFormat("1/1/1900"+" "+ time);

                    // Here change the Date & Time in Milliseconds

                    long currenttimeinmilli =getTimeInMilliSec(plcurrentTime);
                    for (int ipl = 0; ipl < playlistsAll.size(); ipl++) {
                        long h=playlistsAll.get(ipl).getStart_Time_In_Milli();
                        long k=playlistsAll.get(ipl).getStart_Time_In_Milli();

                        if ((currenttimeinmilli >= playlistsAll.get(ipl).getStart_Time_In_Milli() ) && (currenttimeinmilli < playlistsAll.get(ipl).getEnd_Time_In_Milli() )) {
                            playlistId = playlistsAll.get(ipl).getsplPlaylist_Id();
                            break;
                        }
                        else
                        {
                            playlistId="";
                        }
                    }

                }


                if (playlistId.equals("")) {
                    playlistStatus = NO_PLAYLIST;
                    IsPlaylistFind=0;
                    currentPlaylistID = "";
                    AlenkaMedia.playlistStatus = NO_PLAYLIST;
                    if (playlistStatusListener != null) {
                        playlistStatusListener.onPlaylistStatusChanged(NO_PLAYLIST);
                    }

                } else {

                    if (currentPlaylistID.equals("")) {
                        currentPlaylistID = playlistId;
                    }

                    if (!currentPlaylistID.equals(playlistId)) {

                        shouldPlaylistChange = true;
                        currentPlaylistID = playlistId;

                        if (playlistStatusListener != null) {
                          playlistStatusListener.onPlaylistStatusChanged(PLAYLIST_CHANGE);
                        }
                        playlistStatus = PLAYLIST_PRESENT;
                    }

                    if (IsPlaylistFind==0){
                        IsPlaylistFind=1;
                        playlistStatus = PLAYLIST_PRESENT;
                        AlenkaMedia.playlistStatus = playlistStatus;
                        if (playlistStatusListener != null) {
                           playlistStatusListener.onPlaylistStatusChanged(playlistStatus);
                        }
                    }


                    // Old work with love
                    // playlists = new PlaylistManager(context, null).getPlaylistForCurrentTimeOnly();
                }
                if (AlenkaMedia.playlistStatus == -12){
                    AlenkaMedia.playlistStatus = playlistStatus;
                }
            }

/*

            if ((playlists == null )||(playlists.size() == 0 ))
            {
                playlistcounter=playlistcounter+1;
                if(playlistcounter>=3000)
                {
                    playlistcounter=1;
                }
            }
            if((playlists != null) && (playlists.size() > 0))
            {
              playlistcounter=0;
            }

            if (playlistcounter>=300){
                playlistStatus = NO_PLAYLIST;
                currentPlaylistID = "";

            }
            else if ((playlists != null) && (playlists.size() > 0)){


                //String playlistId =datatableplaylistid();

                String playlistId = playlists.get(0).getsplPlaylist_Id();
                playlistcounter=0;

                if (currentPlaylistID.equals("")){
                    currentPlaylistID = playlistId;
                }


                if (!currentPlaylistID.equals(playlistId)){

                    shouldPlaylistChange = true;
                    currentPlaylistID = playlistId;

                    if (playlistStatusListener != null){
                        playlistStatusListener.onPlaylistStatusChanged(PLAYLIST_CHANGE);
                    }
                }

                playlistStatus = PLAYLIST_PRESENT;
            }

            if (AlenkaMedia.playlistStatus == -12){
                AlenkaMedia.playlistStatus = playlistStatus;
            }

             if((playlistcounter<300) && (playlistStatus==-1)) {
                 playlistStatus = PLAYLIST_PRESENT;
             }
            if (AlenkaMedia.playlistStatus != playlistStatus){
                AlenkaMedia.playlistStatus = playlistStatus;
                      if (playlistStatusListener != null){
                    playlistStatusListener.onPlaylistStatusChanged(playlistStatus);
                }

            }
*/
            if (!isPaused)
                mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
        }
    };

    public void setWatcher()
    {
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
    }

    public void pausePlaylistWatcher(){

        isPaused = true;
    }

    public void resumePlaylistWatcher(){
        isPaused = false;
        mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
    }

    public void cancelWatcher()
    {

        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
    }

    public void setAdvertisements(){

        String advertisementTypeMinute =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_Minute_Adv);

        String advertisementTypeSong =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_song_Adv);

        String advertisementTypeTime =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_Time_Adv);


        if (advertisementTypeMinute != null && !advertisementTypeMinute.equals("")){
            ADVERTISEMENT_TYPE = "1";

            String timeAfterAdvertisement =  SharedPreferenceUtil.getStringPreference(this.context,
                    AlenkaMediaPreferences.total_minute_after_adv_play);

            if (timeAfterAdvertisement != null && !timeAfterAdvertisement.equals("")){
                TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = Integer.valueOf(timeAfterAdvertisement);
//                TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = 1; // TODO Remove this
                ADVERTISEMENT_TIME = TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY * 60000;
            }

        } else if(advertisementTypeSong != null && !advertisementTypeSong.equals("")){
            ADVERTISEMENT_TYPE = "2";

            String playSongsAfterAdvertisement =  SharedPreferenceUtil.getStringPreference(this.context,
                    AlenkaMediaPreferences.total_Songs);

            PLAY_AD_AFTER_SONGS = Integer.valueOf(playSongsAfterAdvertisement);

        }
        else if(advertisementTypeTime != null && !advertisementTypeTime.equals("")){
            ADVERTISEMENT_TYPE = "3";
             ADVERTISEMENT_PLAY_TIME="";
             if(advertisements!=null) {
                 advertisements.clear();
             }

            if (ADVERTISEMENT_PLAY_TIME.equals("")){

                if (advertisements == null)
                    advertisements = new AdvertisementsManager(PlaylistWatcher.this.context).
                            getAdvertisementsForComingTime();

                if ((advertisements!=null) && (advertisements.size() > 0))
                {

                    String playAdAtTime = advertisements.get(currentlyPlayingAdAtIndex).getsTime();

                    if (playAdAtTime != null){
                        ADVERTISEMENT_PLAY_TIME = playAdAtTime;
                    }
                }

            }
        }

    }
}
