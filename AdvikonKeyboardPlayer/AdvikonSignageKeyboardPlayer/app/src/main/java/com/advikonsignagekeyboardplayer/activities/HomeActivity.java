package com.advikonsignagekeyboardplayer.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.advikonsignagekeyboardplayer.adapters.PlaylistAdapter;
import com.advikonsignagekeyboardplayer.adapters.SongAdapter;
import com.advikonsignagekeyboardplayer.database.MySQLiteHelper;
import com.advikonsignagekeyboardplayer.models.Advertisements;
import com.advikonsignagekeyboardplayer.models.Announcement;
import com.advikonsignagekeyboardplayer.models.PlayerStatus;
import com.advikonsignagekeyboardplayer.models.Playlist;
import com.advikonsignagekeyboardplayer.models.Songs;
import com.advikonsignagekeyboardplayer.utils.AlenkaMediaPreferences;
import com.advikonsignagekeyboardplayer.utils.ConnectivityReceiver;
import com.advikonsignagekeyboardplayer.utils.Constants;
import com.advikonsignagekeyboardplayer.utils.ExternalStorage;
import com.advikonsignagekeyboardplayer.utils.FileUtil;
import com.advikonsignagekeyboardplayer.utils.MyNotificationManager;
import com.advikonsignagekeyboardplayer.utils.NetworkUtil;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.StorageUtils;
import com.advikonsignagekeyboardplayer.utils.Utilities;
import com.advikonsignagekeyboardplayer.R;
import com.crashlytics.android.Crashlytics;
import com.developer.kalert.KAlertDialog;
import com.github.rongi.rotate_layout.layout.RotateLayout;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.advikonsignagekeyboardplayer.alarm_manager.ApplicationChecker;
import com.advikonsignagekeyboardplayer.alarm_manager.PlaylistWatcher;
import com.advikonsignagekeyboardplayer.api_manager.DownloadService;
import com.advikonsignagekeyboardplayer.api_manager.OkHttpUtil;
import com.advikonsignagekeyboardplayer.application.AlenkaMedia;
import com.advikonsignagekeyboardplayer.custom_views.MyClaudVideoView;
import com.advikonsignagekeyboardplayer.interfaces.DownloadListener;
import com.advikonsignagekeyboardplayer.interfaces.PlaylistLoaderListener;
import com.advikonsignagekeyboardplayer.interfaces.SerialPortManagerListener;
import com.advikonsignagekeyboardplayer.interfaces.WirelessKeyboardManagerListener;
import com.advikonsignagekeyboardplayer.mediamanager.AdvertisementsManager;
import com.advikonsignagekeyboardplayer.mediamanager.AnnouncementManager;
import com.advikonsignagekeyboardplayer.mediamanager.PlayerStatusManager;
import com.advikonsignagekeyboardplayer.mediamanager.PlaylistManager;
import com.advikonsignagekeyboardplayer.serialport.KeyboardManager;
import com.advikonsignagekeyboardplayer.serialport.SerialPortManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.advikonsignagekeyboardplayer.alarm_manager.PlaylistWatcher.NO_PLAYLIST;
import static com.advikonsignagekeyboardplayer.alarm_manager.PlaylistWatcher.currentPlaylistID;

/**
 * Created by love on 30/5/17.
 */
public class HomeActivity extends Activity implements DownloadListener,
        OkHttpUtil.OkHttpResponse, PlaylistWatcher.PlaylistStatusListener,
        SerialPortManagerListener, WirelessKeyboardManagerListener {

    public static final String TAG = "HomeActivity";

    private final int VIDEO_VIEW_TAG = 1;
    public int y=1;

    private static final int VIDEO_AD_VIEW_TAG = 2;

    TextView txtTokenId;
    public  File gblStorage;

    private DownloadService mDownloadService;

    private boolean mIsBound;

    private ProgressBar circularProgressBar;
    private int currentApiVersion = Build.VERSION.SDK_INT;

    private ListView lvPlaylist;
    private String gblSongid="";
    RelativeLayout layout;
    LinearLayout portraitmp3layout;
    RotateLayout mp3layout;
    RotateLayout imglayout;
    RotateLayout videolayout;

    private int ctrplaylistchg=0;

    KAlertDialog tankEmptyDialog;

    private ListView lvSongs;

    private PlaylistAdapter playlistAdapter;
    private RotateLayout layout4;
    private SongAdapter songAdapter;
    public static ImageView Imgmarker;
    private ArrayList<Playlist> arrPlaylists = new ArrayList<Playlist>();
    private ArrayList<Playlist> arrPlaylistsweb = new ArrayList<Playlist>();
    private ArrayList<Songs> arrSongsDownloadAll = new ArrayList<Songs>();

    private ArrayList<Songs> arrSongs = new ArrayList<Songs>();
    private ArrayList<Songs> arrSongsweb = new ArrayList<Songs>();
    private ArrayList<Advertisements> arrAdvweb = new ArrayList<Advertisements>();
    ArrayList<Songs> songsArrayList;
    private ArrayList<Advertisements> arrAdvertisements = new ArrayList<Advertisements>();

    private int currentlyPlayingSongAtIndex = 0;

    public static int currentlyPlayingAdAtIndex = -1;

    private MyClaudVideoView mPreview;

//    private //VideoView//123videoViewAds;

    private PlaylistWatcher alarm;

//    Handler checkForPlaylistStatus   = new Handler();

    int delay = 1000; //milliseconds

    private int currentPlaylistStatus = -2;

    private boolean doubleBackToExitPressedOnce;
    public MySQLiteHelper songsrc;

    IntentFilter intentFilter = new IntentFilter(Constants.ALARM_ACTION);
    IntentFilter intentConnectivity = new IntentFilter(Constants.CONNECTIVITY_CHANGED);

    BroadcastReceiver broadcastReceiver;

    BroadcastReceiver networkChangeReceiver;

    boolean shouldPlaySoftStopAd = false;
    private int playNextSongIex = -1;
    private int playNextAdvIndex = -1;
    public String titledownload="";
    public String artistdownload="";
    private int countblack=0;
    private int playlistcounter=0;

    private String sdCardLocation = "";
    public static HomeActivity hm;
    private ArrayList<String> arrVideoFiles = new ArrayList<>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    SerialPortManager serialPortManager;

    KeyboardManager keyboardManager;

    /********countOfTotalSongsToBeDownloaded********
     This variable is used for updating the count of
     songs which are downloaded every 15 minutes. It stores
     the total number of songs which are to be downloaded.
     /********countOfTotalSongsToBeDownloaded********/

    private int countOfTotalSongsToBeDownloaded = 0;

    private int REQUEST_CODE_STORAGE_FOLDER_SELECTOR = 43;

    /*********************Broadcast Receiver Starts**************************/



    /*********************Broadcast Receiver Ends**************************/

    /***********************Download Videos Service Methods Start****************************************/

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDownloadService = ((DownloadService.LocalBinder)service).getService();
            mDownloadService.registerListener(HomeActivity.this);
        }
        public void onServiceDisconnected(ComponentName className) {
            mDownloadService = null;
        }
    };
    private String targetFileName;
    private String sourceFileLocation;
    private int imgcounter=0;
    private int EXPORT_VIDEO_INDEX = 0;
    private TextView txtFileWriter;
    private ImageView Imgicon;
    private ImageView myImage;
    // private ImageView Imgicon1;
    private TextView txtSong;
    private TextView txtArtist;
    private CountDownTimer imgCountdowntimer,imgCountdowntimer1,imgCountdowntimer2;
    private CountDownTimer announcementTimer;
    private CountDownTimer mCountDownTimer;
    boolean isCountDownTimerRunning = false;
    private long downloadId;

    AlertDialog.Builder dialogBuilder = null;

    AlertDialog dialog;

    public com.lztek.toolkit.Lztek mLztek;

    void doBindService() {
        bindService(new Intent(HomeActivity.this,
                DownloadService.class), mConnection, 0);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            if (mDownloadService != null) {
                mDownloadService.unregisterListener(this);
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onUpdate(long value) {
        /*When a song is being downloaded its progress is shown here.*/
        Log.e("Download Status","" + value);
        circularProgressBar.setProgress((int) value);
    }



    public void playfoundsong(boolean shouldPlay, Songs song)
    {
        if (shouldPlay){
            mPreview = findViewById(R.id.video_view);

            /*If video view is not playing then only we start the player*/
            if (!mPreview.isPlaying()) {
                getPlaylistsForCurrentTime();
                return;
            }
        }


        if (arrSongs.size() > 0){

            String downloadedSongPlaylistId = song.getSpl_PlaylistId();
            String currentPlayingPlaylistId = arrSongs.get(0).getSpl_PlaylistId();

            if (downloadedSongPlaylistId.equals(PlaylistWatcher.currentPlaylistID)){
                //Toast.makeText(HomeActivity.this, "Downloaded and added song" + song.getTitle(), Toast.LENGTH_SHORT).show();
                arrSongs.add(song);
            }
        }
    }



    @Override
    public void downloadCompleted(boolean shouldPlay, Songs song) {

        /*A case where current time has no playlist but after one hour or so there is playlist
         * and the songs are being downloaded. After the first song finishes download shouldPlay will be true
         * and also the videoView will not be playing. Prevent video view from playing in this case.*/

        if (shouldPlay){

            /*If video view is not playing then only we start the player*/

            if (imgcounter==0) {
                getPlaylistsForCurrentTime();
                return;
            }
        }

        if (arrSongs.size() > 0){

            String downloadedSongPlaylistId = song.getSpl_PlaylistId();
            String currentPlayingPlaylistId = arrSongs.get(0).getSpl_PlaylistId();

            if (downloadedSongPlaylistId.equals(PlaylistWatcher.currentPlaylistID)){
                // Toast.makeText(HomeActivity.this, "Downloaded and added song" + song.getTitle(), Toast.LENGTH_SHORT).show();
                arrSongs.add(song);

            }

            if (!(DownloadService.songsToBeDownloaded.size() - 1 > DownloadService.downloadingFileAtIndex)) {

                if (arrSongs.size() > 0) arrSongs.clear();

                String schtype = SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.SchType);
                if (schtype.equals("Normal")) {

                    songsArrayList = new PlaylistManager(HomeActivity.this, null).getSongsForPlaylist(arrPlaylists.get(0).getsplPlaylist_Id());
                } else {
                    songsArrayList = new PlaylistManager(HomeActivity.this, null).getSongsForPlaylistRandom(arrPlaylists.get(0).getsplPlaylist_Id());

                }


                if (songsArrayList != null && songsArrayList.size() > 0) {
                    arrSongs.addAll(songsArrayList);
                }

                if (arrSongs.size() > 0) {

                    if (arrPlaylists.get(0).getIsSeparatinActive() == 1) {
                        sort(arrSongs);

                    }

                    songAdapter = new SongAdapter(HomeActivity.this, arrSongs);
                    lvSongs.setAdapter(songAdapter);
                    lvSongs.deferNotifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void advertisementDownloaded(Advertisements advertisements) {

        if (advertisements != null){
            alarm.setAdvertisements();
            PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER=0;
            String advType = SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.is_Time_Adv);

            if (advType.equals("1")){

                if (advertisements.getStart_Adv_Time_Millis() >= System.currentTimeMillis()){

                    arrAdvertisements.add(advertisements);
                }

            }
            else {
                arrAdvertisements.add(advertisements);
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(mPreview.isPlaying())
        {
            mPreview.stopPlayback();
        }
        if(imgCountdowntimer!=null) {
            imgCountdowntimer.cancel();
        }
        if(imgCountdowntimer1!=null) {
            imgCountdowntimer1.cancel();
        }
        if(imgCountdowntimer2!=null) {
            imgCountdowntimer2.cancel();
        }
        myImage.setVisibility(View.INVISIBLE);
        myImage.setImageDrawable(null);
        System.exit(1);

    }

    @Override
    public void refreshPlayerControls() {

        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {


            }
        });
    }
    @Override
    public void startedCopyingSongs(final int currentSong,final int totalSongs,final boolean isFinished) {

        Handler  handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                freeMemory();
                if (isFinished){
                    txtFileWriter.setText("Copy successful");
                    alarm = new PlaylistWatcher();
                    alarm.setContext(HomeActivity.this);
                    alarm.setPlaylistStatusListener(HomeActivity.this);
                    alarm.setWatcher();
                    getPlaylistsForCurrentTime();
                    getAdvertisements();

                    /*PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
                    playerStatusManager.songsDownloaded = "" + songsThatHaveBeenDownloaded;
                    playerStatusManager.updateDownloadedSongsCountOnServer();*/

                    return;
                }
                txtFileWriter.setText("Copying song " + currentSong + " of " + totalSongs);
            }
        });


        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isFinished){
                    txtFileWriter.setText("Copy successful");
                    return;
                }
                txtFileWriter.setText("Copying song " + currentSong + " of " + totalSongs);
            }
        });*/
    }

    @Override
    public void finishedDownloadingSongs(int totalSongs) {

        if (totalSongs > 0){

            PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
            playerStatusManager.songsDownloaded = "" + totalSongs;
//            playerStatusManager.updateDownloadedSongsCountOnServer();

        } else {

            if (AlenkaMedia.getInstance().isUpdateInProgress){

                Log.e(TAG,"New songs downloaded now restarting");

                PlaylistManager playlistManager = new PlaylistManager(HomeActivity.this,playlistLoaderListener);
                playlistManager.publishTokenForUpdatedData();

            }
        }

        if (serialPortManager != null) {
            serialPortManager.reloadAnnouncements();
        }
    }

    private void showDialogForExportDone(){

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        builder.setTitle("Songs copied from external storage.");

        builder.setCancelable(false);
// Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    @Override
    public void showOfflineDownloadingAlert() {

    }


    /***********************Download Videos Service Methods Ends****************************************/

    private void mockSoftDeleteAnnouncements(){
        AnnouncementManager manager = new AnnouncementManager(this);
        ArrayList<Announcement> announcements = manager.getAnnouncementsThatAreDownloaded();

        for (Announcement announcement : announcements) {
            announcement.setStatus_Download(0);
            manager.announcementDownloaded(announcement);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        AppCenter.start(getApplication(), "6047b043-4c2d-4508-af87-56412d83e64e",
                Analytics.class, Crashes.class);
        Crashes.setEnabled(true);
        setContentView(R.layout.activity_player);

        String deviceType = SharedPreferenceUtil.getStringPreference(this, AlenkaMediaPreferences.deviceType);

        if (deviceType.equals("Screen")){

            try {
                keyboardManager = new KeyboardManager(this);
            } catch(Exception e){
                e.printStackTrace();
            }

        } else if (deviceType.equals("Sanitizer")){

            try {
                mLztek = com.lztek.toolkit.Lztek.create(this);
            } catch(Exception e){
                e.printStackTrace();
            }
            serialPortManager = new SerialPortManager(this);

        } else {

        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        layout = findViewById(R.id.mainContainer);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        portraitmp3layout = (LinearLayout) findViewById(R.id.mp3layout);
        mp3layout = (RotateLayout) findViewById(R.id.mp3rotate);
        imglayout = (RotateLayout) findViewById(R.id.rotateimg);
        videolayout = (RotateLayout) findViewById(R.id.rotatevideo);

        circularProgressBar = findViewById(R.id.circularProgress);

        lvPlaylist =  findViewById(R.id.listViewPlaylists);

        lvSongs = findViewById(R.id.listViewSongs);
        Imgmarker=(ImageView)findViewById(R.id.marker);
        mPreview = findViewById(R.id.video_view);
        mPreview.setOnMediaCompletionListener(mediaPlayerCompletionListener);
        mPreview.setTag(VIDEO_VIEW_TAG);

        txtFileWriter = findViewById(R.id.txtWritingFile);
        txtSong = findViewById(R.id.songtitle);
        txtArtist = findViewById(R.id.Artist);
        myImage = (ImageView) findViewById(R.id.previmg);
        Imgicon = findViewById(R.id.imgID);
        txtTokenId = findViewById(R.id.txtTokenId);
        songsrc = new MySQLiteHelper(HomeActivity.this);
        hm=this;
        MyNotificationManager.getInstance(this);
        txtTokenId.setTypeface(Utilities.getApplicationTypeface(HomeActivity.this));
        txtFileWriter.setTypeface(Utilities.getApplicationTypeface(HomeActivity.this));
        String token = SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.TOKEN_ID);

        if (token.length() > 0){
            txtTokenId.setText("Token ID : " + token);
        } else {
            txtTokenId.setText("");
        }
        String inditype= SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.Indicatorimg);
        if(inditype.equals("1"))
        {
            Imgmarker.setVisibility(View.VISIBLE);
        }
        else
        {
            Imgmarker.setVisibility(View.INVISIBLE);

        }
        String imgtype= SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.Imgtype);

        if(!imgtype.equals("0"))
        {
            downloadimg(imgtype);
        }
        String rotation= SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.Rotation);
        // rotation="90";
        mp3layout.setAngle(Integer.parseInt(rotation));
        imglayout.setAngle(Integer.parseInt(rotation));
        videolayout.setAngle(Integer.parseInt(rotation));
        //  registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        startService(new Intent(HomeActivity.this, ApplicationChecker.class));

        //TODO: Handle the new custom view crash and send crash log.

        ArrayList<Songs> songs = getSongsToBeDownloaded();
        ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();

        if (songs != null && songs.size() > 0 ||
                ads != null && ads.size() > 0){

            boolean isStorageAlertShownOnce = SharedPreferenceUtil.getBooleanPreference(HomeActivity.this, Constants.STORAGE_ALERT_SHOWN_ONCE,false);

            if (!isStorageAlertShownOnce){

                SharedPreferenceUtil.setBooleanPreference(HomeActivity.this, Constants.STORAGE_ALERT_SHOWN_ONCE,true);
                // showAlertDialogForStorageSelection();
                alarm = new PlaylistWatcher();
                alarm.setContext(HomeActivity.this);
                alarm.setPlaylistStatusListener(HomeActivity.this);
                alarm.setWatcher();

                // Toast.makeText(HomeActivity.this, "Auto downloading songs in 1 minute.", Toast.LENGTH_LONG).show();

                mCountDownTimer = new CountDownTimer(60,1000) {
                    @Override
                    public void onTick(long l) {
                        isCountDownTimerRunning = true;
                        Log.e(TAG,"seconds remaining: " + l / 1000);
                    }

                    @Override
                    public void onFinish() {
                        isCountDownTimerRunning = false;

                    }
                };
                mCountDownTimer.start();

            } else {

                if (!AlenkaMedia.getInstance().isDownloadServiceRunning){
                    doBindService();
                }

                alarm = new PlaylistWatcher();
                alarm.setContext(HomeActivity.this);
                alarm.setPlaylistStatusListener(HomeActivity.this);
                alarm.setWatcher();

                getPlaylistsForCurrentTime();
                getAdvertisements();

            }
            if (songs != null)
                countOfTotalSongsToBeDownloaded = songs.size();

        } else {

            alarm = new PlaylistWatcher();
            alarm.setContext(HomeActivity.this);
            alarm.setPlaylistStatusListener(HomeActivity.this);
            alarm.setWatcher();
            getPlaylistsForCurrentTime();
            getAdvertisements();
        }

//
//        saveLogcatToFile(HomeActivity.this);
    }





    public static HomeActivity getInstance(){
        return hm;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


    public void playadvnow(String id)
    {
        ArrayList<Advertisements> advertisementsdownloadall = new AdvertisementsManager(HomeActivity.this).
                getAdvertisementsThatAreDownloaded();

        if (advertisementsdownloadall != null && advertisementsdownloadall.size() > 0){
            arrAdvweb.addAll(advertisementsdownloadall);
            for(int i=0;i<arrAdvweb.size();i++)
            {
                String t=arrAdvweb.get(i).getAdvtID();
                String p=arrAdvweb.get(i).getAdvFileUrl();
                final String h = p.substring(p.length() - 3);
                if(t.equals(id)) {
                    currentlyPlayingAdAtIndex= i;
                    Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(h.equals("mp3")) {
                                //123videoView.reset();
                                //123videoView.setVideoPath(arrAdvweb.get(currentlyPlayingAdAtIndex).getAdvtFilePath());
                                //123videoView.setVisibility(View.INVISIBLE);
                                txtTokenId.setVisibility(View.INVISIBLE);
                                circularProgressBar.setVisibility(View.INVISIBLE);
                                Imgicon.setVisibility(View.INVISIBLE);
                                txtSong.setText(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtName());
                                txtSong.setVisibility(View.VISIBLE);
                                //123videoView.start();

                            }
                            else {
                                Log.d(TAG,"Here");
                                //123videoView.reset();
                                //123videoView.setVisibility(View.VISIBLE);
                                //123videoView.setVideoPath(arrAdvweb.get(currentlyPlayingAdAtIndex).getAdvtFilePath());
                                //123videoView.start();
                            }
                        }
                    });
                }

            }

        }


    }

    public void playplaylistfromwebnow(String id)
    {
        ArrayList<Playlist> playlistArrayList = new PlaylistManager(HomeActivity.this,null).getAllPlaylistInPlayingOrder();

        if (playlistArrayList != null && playlistArrayList.size()>0){
            arrPlaylistsweb.addAll(playlistArrayList);
            for(int i=0;i<arrPlaylistsweb.size();i++)
            {
                String t=arrPlaylistsweb.get(i).getsplPlaylist_Id();
                if(t.equals(id)) {
                    playSelectedPlaylist(t);
                    break;
                }

            }

        }
    }

    private void downloadimg(String img)
    {
        String url= "http://api.nusign.eu/api/"+img+".jpg";
        String pathToUsb = "";
        long bytesAvailable;
        long megAvailable;
        File[] pathsss =  ContextCompat.getExternalFilesDirs(getApplicationContext(), null);

        if (pathsss.length > 1) {
            File intDrive = pathsss[0];
            pathToUsb = intDrive.getAbsolutePath();
            File usbDrive = pathsss[1];
            StatFs stat = new StatFs(pathToUsb);
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
            megAvailable = bytesAvailable / (1024 * 1024);
            if (megAvailable > 1200) {
                pathToUsb = intDrive.getAbsolutePath();
                gblStorage=usbDrive;

            }
            else
            {
                File usbDrive1 = pathsss[1];
                pathToUsb = usbDrive1.getAbsolutePath();
                gblStorage=intDrive;
            }

        }
        else {
            File usbDrive2 = pathsss[0];
            gblStorage=usbDrive2;
            pathToUsb = getApplicationInfo().dataDir;
        }

        String applicationDirectory =pathToUsb;
        final String filePath=applicationDirectory+"/"+img+".jpg";
        File imgpath=new File(filePath);
        if(imgpath.exists())
        {
            Imgicon.setImageURI(Uri.parse(filePath));

            // Drawable drawable = Drawable.createFromPath(filePath);
            // Imgicon.setImageDrawable(drawable);
        }
        else {
            Ion.with(HomeActivity.this)
                    .load(url)
                    .progress(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {

                            int percentage = (int) (downloaded * 100.0 / total + 0.5);

                            if (percentage == 0) {
                                percentage = 1;
                            }

                            Log.e("Song downloaded", percentage + "%");

                            sendUpdate(percentage);
                        }
                    })
                    .write(new File(filePath)).setCallback(new FutureCallback<File>() {
                @Override
                public void onCompleted(Exception e, File result) {

                    if (e != null) {

                        Handler handler = new Handler(Looper.getMainLooper());

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HomeActivity.this, "Downloading failed for img ", Toast.LENGTH_SHORT).show();
                            }
                        }, 1000);
                        return;
                    }

                    if (result != null) {

                        if (result.exists()) {
                            Imgicon.setImageURI(Uri.parse(filePath));
                            //Drawable drawable = Drawable.createFromPath(filePath);
                            //Imgicon.setImageDrawable(drawable);
                        }

                    }
                }

            });
        }


    }

    private ActivityManager.MemoryInfo getAvailableMemory()
    {
        ActivityManager am=(ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo=new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public void playSelectedPlaylist(String playlistid)
    {
        ArrayList<Songs> arrSongsForSelectedPlaylist = new PlaylistManager(HomeActivity.this,null).
                getSongsForPlaylist(playlistid);

        if (arrSongsForSelectedPlaylist != null && arrSongsForSelectedPlaylist.size() > 0){

            arrSongs.clear();
            arrSongs.addAll(arrSongsForSelectedPlaylist);
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPreview.setTag(VIDEO_VIEW_TAG);
                    currentlyPlayingSongAtIndex = 0;
                    songAdapter.notifyDataSetChanged();
                    if(mPreview.isPlaying()) {
                        mPreview.stopPlayback();
                        mPreview.start();
                    }

                    mPreview.setVisibility(View.VISIBLE);
                    mPreview.playMedia(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath());

                }
            });


        }


    }

    public void playsongfromweb(String songid, String url, String albumid, String artistid, final String title,final String artname)
    {

        gblSongid=songid;
        titledownload=title;
        artistdownload=artname;
        int h=0;
        arrSongsweb.clear();
        arrSongsDownloadAll.clear();

        ArrayList<Songs> arrSongsDownloadAll = new PlaylistManager(HomeActivity.this,null).getAllDownloadedSongs(songid);
        final int p=arrSongsDownloadAll.size();

         /*
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String str1 = mg;
              //  Toast.makeText(HomeActivity.this,"mg "+str1,Toast.LENGTH_SHORT).show();
            }
        });*/

        if (arrSongsDownloadAll.size() > 0){
            arrSongsweb.addAll(arrSongsDownloadAll);
            for(int i=0;i<arrSongsweb.size();i++)
            {
                String t=arrSongsweb.get(i).getTitle_Id();
                if(t.equals(songid)) {
                    h=1;
                    currentlyPlayingSongAtIndex = i;
                    String f = arrSongsweb.get(currentlyPlayingSongAtIndex).getTitle_Url();
                    String a = f.substring(f.length() - 3);
                    //String d=h;
                    if (a.equals("mp3")) {
                        Handler handler = new Handler(HomeActivity.this.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG,"Here");
                                //123videoView.reset();
                                //123videoView.setVideoPath(arrSongsweb.get(currentlyPlayingSongAtIndex).getSongPath());
                                //123videoView.setVisibility(View.INVISIBLE);
                                txtTokenId.setVisibility(View.INVISIBLE);
                                circularProgressBar.setVisibility(View.INVISIBLE);
                                Imgicon.setVisibility(View.INVISIBLE);
                                txtSong.setText(title);
                                txtArtist.setText(artname);
                                txtArtist.setVisibility(View.VISIBLE);
                                txtSong.setVisibility(View.VISIBLE);
                                //123videoView.start();
                            }
                        });
                    }
                    else {

                        Handler handler = new Handler(HomeActivity.this.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG,"123");
                                //123videoView.reset();
                                //123videoView.setVisibility(View.VISIBLE);
                                //123videoView.setVideoPath(arrSongsweb.get(currentlyPlayingSongAtIndex).getSongPath());
                                //123videoView.start();
                            }
                        });

                    }
                }
                break;

            }

        }
        if(h==0)
        {
            //songsrc.insertnewSongsfromweb(songid, url, artistid, albumid);
            // startDownloadingSongs(url,songid);
        }

    }

    public void startDownloadingSongs(String url,String title)
    {

        try {

            String fileURL =url;
            final String fileName = title;
            final String ext = "."+fileURL.substring(fileURL.length() - 3);
            Utilities.showToast(HomeActivity.this, "Downloading Start");

            //filePath = applicationDirectory;

            Uri uri = Uri.parse(fileURL); // Path where you want to download file.
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);// Tell on which network you want to download file.
            if (Utilities.isVersionLowerThanLollipop()) {

                File[] pathsss = getExternalFilesDirs(null);

                Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();

                if (externalLocations.size() > 1) {

                    File usbDrive = pathsss[0];

                    File name = new File(usbDrive, fileName + ext);
                    request.setDestinationUri(Uri.fromFile(name));

                } else {
                    request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath(),"Downloads");
                }
            }

            else
            {
                File[] pathsss =  ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
                File usbDrive;
                long bytesAvailable;
                long megAvailable;
                String pathToUsb="";
                if (pathsss.length > 1) {
                    usbDrive = pathsss[0];
                    pathToUsb = usbDrive.getAbsolutePath();
                    File name = new File(usbDrive, fileName + ext);
                    StatFs stat = new StatFs(pathToUsb);
                    bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
                    megAvailable = bytesAvailable / (1024 * 1024);
                    if (megAvailable > 1000) {
                        request.setDestinationUri(Uri.fromFile(name));
                    }
                    else
                    {
                        File sdcsrd=pathsss[1];
                        File name1 = new File(sdcsrd, fileName + ext);
                        request.setDestinationUri(Uri.fromFile(name1));
                    }

                }

            }
            downloadId = ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);


        }catch (Exception e1){

            e1.printStackTrace();
            Utilities.showToast(HomeActivity.this, " Error => "+e1.getMessage() );
            startDownloadingSongs(url,title);
        }

    }

    public void sendUpdate(long value) {
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).onUpdate(value);
        }
    }
    private final ArrayList<DownloadListener> mListeners
            = new ArrayList<DownloadListener>();

    /* private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             //Fetching the download id received with the broadcast
             long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
             DownloadManager.Query q = new DownloadManager.Query();

             if (downloadId == id) {
                 //Utilities.showToast(HomeActivity.this, "Id => "+ id);
                 q.setFilterById(id);
                 DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                 Cursor c = manager.query(q);
                 if (c.moveToFirst()) {
                     int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                     //Utilities.showToast(HomeActivity.this,  "Reason: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                     final String stu= Integer.toString(status);
                     final  String stu7= Integer.toString(DownloadManager.STATUS_SUCCESSFUL);

                    // Utilities.showToast(HomeActivity.this, "status => "+ stu+ " status7 => "+ stu7);

                     if (status == DownloadManager.STATUS_SUCCESSFUL) {
                         // do any thing here
                         Utilities.showToast(HomeActivity.this, "Download Completed ");
                        final String h =c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        String k="1";
                        songsrc.downloadupdate(k,h,gblSongid);
                         final String a = h.substring(h.length() - 3);
                         Handler  handler = new Handler(HomeActivity.this.getMainLooper());
                         handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 try {
                                     if (a.equals("mp3")) {
                                         //123videoView.reset();
                                         //123videoView.setVideoPath(h);
                                         //123videoView.setVisibility(View.INVISIBLE);
                                         circularProgressBar.setVisibility(View.INVISIBLE);
                                         txtSong.setVisibility(View.VISIBLE);
                                         txtArtist.setVisibility(View.VISIBLE);
                                         txtSong.setText(titledownload);
                                         txtArtist.setText(artistdownload);
                                         txtTokenId.setVisibility(View.INVISIBLE);
                                         Imgicon.setVisibility(View.INVISIBLE);
                                         //123videoView.start();
                                     }
                                     else {
                                         //123videoView.reset();
                                         //123videoView.setVisibility(View.VISIBLE);
                                         //123videoView.setVideoPath(h);
                                         //123videoView.start();
                                     }
                                 }
                                 catch (Exception e)
                                 {
                                     Utilities.showToast(HomeActivity.this, "Error 7 => " + e.getMessage());
                                     e.getCause();
                                 }
                             }
                         });
                     }
                 }
             }
         }
     };

 */
    private void sendLastCrashLog() {

        try{

            String json =  SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.CRASH_MESSAGE);

            if (json != null){

                JSONObject jsonObject = new JSONObject(json);
                new OkHttpUtil(HomeActivity.this, Constants.UPDATE_CRASH_LOG, jsonObject.toString(), new OkHttpUtil.OkHttpResponse() {
                    @Override
                    public void onResponse(String response, int tag) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.has("Response")){

                                String status = jsonObject.getString("Response");

                                if (status.equalsIgnoreCase("1")){

                                    SharedPreferenceUtil.removeStringPreference(HomeActivity.this, Constants.CRASH_MESSAGE);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e, int tag) {
                        e.printStackTrace();
                    }
                },false, Constants.UPDATE_CRASH_LOG_TAG).execute();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<Songs> getSongsToBeDownloaded(){

        ArrayList<Playlist> playlists = new PlaylistManager(HomeActivity.this, null).getPlaylistFromLocallyToBedDownload();
        ArrayList<Songs> songsToBeDownloaded = null;

        if (playlists != null && playlists.size() > 0) {

            PlaylistManager songsLoader = new PlaylistManager(HomeActivity.this, null);
            songsToBeDownloaded = new ArrayList<>();

            for (Playlist playlist : playlists) {

                ArrayList<Songs> songs = songsLoader.getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

                if (songs != null && songs.size() > 0) {

//                    if (playlist.getIsSeparatinActive() == 0){
                    // sort(songs);
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

    private ArrayList<Advertisements> getAdvertisementsToBeDownloaded(){
        return new AdvertisementsManager(this).
                getAdvertisementsToBeDownloaded();
    }

    private void getAdvertisements() {

        ArrayList<Advertisements> advertisements = new AdvertisementsManager(HomeActivity.this).
                getAdvertisementsThatAreDownloaded();

        String advType = SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.is_Time_Adv);

        if (advType.equals("1")){

            if (advertisements != null && advertisements.size() > 0) {

                for (Advertisements ad :advertisements) {

                    /*Add only those advertisements whose end time is greater than current time.*/

                    if (ad.getStart_Adv_Time_Millis() >= System.currentTimeMillis()) {
                        arrAdvertisements.add(ad);
                    }

                }
            }


        }
        else {
            if (advertisements != null && advertisements.size() > 0){
                arrAdvertisements.addAll(advertisements);
            }
        }
    }

    MyClaudVideoView.OnMediaCompletionListener mediaPlayerCompletionListener = new MyClaudVideoView.OnMediaCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            try {

                if(imgCountdowntimer!=null) {
                    imgCountdowntimer.cancel();
                }
                if(imgCountdowntimer1!=null) {
                    imgCountdowntimer1.cancel();
                }
                if(imgCountdowntimer2!=null) {
                    imgCountdowntimer2.cancel();
                }
                if(mPreview.isPlaying())
                {
                    return;
                }

                if (arrAdvertisements.size() > 0) {

                    if ((Integer) mPreview.getTag() == VIDEO_AD_VIEW_TAG) {
                        PlaylistWatcher.ADVERTISEMENT_TIME_COUNTER = 0;
                        shouldPlaySoftStopAd = false;
                        PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER = 0;
                    }

                    String advertisementTypeSong = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                            AlenkaMediaPreferences.is_song_Adv);

                    String advertisementTypeMinute = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                            AlenkaMediaPreferences.is_Minute_Adv);

                    String advertisementTypeTime = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                            AlenkaMediaPreferences.is_Time_Adv);


                    if (advertisementTypeSong.equals("1")) {


                        if (PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER == PlaylistWatcher.PLAY_AD_AFTER_SONGS) {

//                        Toast.makeText(HomeActivity.this, "Should play ad from Home", Toast.LENGTH_SHORT).show();
                            setVisibilityAndPlayAdvertisement();

                            return;
                        }
                    } else if (advertisementTypeMinute.equals("1")) {

                        if (shouldPlaySoftStopAd) {

//                        Toast.makeText(HomeActivity.this, "Should play soft stop ad", Toast.LENGTH_SHORT).show();

                            setVisibilityAndPlayAdvertisement();

                            return;
                        }
                    } else if (advertisementTypeTime.equals("1")) {

                        if (shouldPlaySoftStopAd) {

                            setVisibilityAndPlayAdvertisement();

                            return;
                        }
                    }
                }

                if (SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING) {
                    SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING = false;

                    //If there are no playlists for current time, do not play next song.
                    if (AlenkaMedia.playlistStatus == NO_PLAYLIST){
                        mPreview.setVisibility(View.INVISIBLE);
                        txtTokenId.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                /*If song played was at last index then restart the playlist.*/

                if (arrSongs.size() - 1 > currentlyPlayingSongAtIndex) {
                    currentlyPlayingSongAtIndex++;
                } else {
                    currentlyPlayingSongAtIndex = 0;
                }
                if((arrAdvertisements!=null) && (arrAdvertisements.size()>0)) {
                    PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;
                }

                mPreview.setTag(VIDEO_VIEW_TAG);
                insertsongStatus(currentlyPlayingSongAtIndex);
                //long v=Runtime.getRuntime().maxMemory();
                // Toast.makeText(HomeActivity.this,"Max heap"+v, Toast.LENGTH_LONG).show();
                String f = arrSongs.get(currentlyPlayingSongAtIndex).getTitle_Url();
                String h = f.substring(f.length() - 3);
                //String d=h;
                if(h.equals("jpg")||h.equals("jpeg")||h.equals("png"))
                {
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //123videoView.reset();
                            y=0;
                            //hidenavigation();
                            if(mPreview.isPlaying())
                            {
                                mPreview.stopPlayback();
                            }
                            // Utilities.showToast(HomeActivity.this,"OnComplete");

                            String k = arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            myImage.setVisibility(View.VISIBLE);
                            myImage.setImageURI(Uri.parse(k));
                            mPreview.setVisibility(View.INVISIBLE);
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            Imgicon.setVisibility(View.INVISIBLE);
                            txtArtist.setVisibility(View.INVISIBLE);
                            txtSong.setVisibility(View.INVISIBLE);
                            //123videoView.start();


                        }
                    });
                    imgCountdowntimer= new CountDownTimer(15000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            // mTaextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                        }

                        public void onFinish() {

                            if (imgCountdowntimer != null) {
                                imgCountdowntimer.cancel();
                                onCompletion(mediaPlayer);
                            }
                        }
                    }.start();
                }
                else if(h.equals("mp4")) {

                    Handler handler = new Handler(HomeActivity.this.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //   Toast.makeText(HomeActivity.this,String.valueOf(arrSongs.size()),Toast.LENGTH_LONG).show();
                            //videoView.setVisibility(View.VISIBLE);
                            if ((y == 0) && (myImage!=null)) {
                                myImage.setVisibility(View.INVISIBLE);
                                txtArtist.setVisibility(View.INVISIBLE);
                                txtSong.setVisibility(View.INVISIBLE);
                            }

                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            mPreview.setVisibility(View.VISIBLE);
                            Uri uri = Uri.fromFile(new File(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath()));
                            mPreview.playMedia(uri);
                            Imgicon.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                else
                {
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (y == 0) {
                                myImage.setVisibility(View.INVISIBLE);
                            }
                            Uri uri = Uri.fromFile(new File(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath()));
                            mPreview.playMedia(uri);
                            mPreview.setVisibility(View.INVISIBLE);
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            Imgicon.setVisibility(View.VISIBLE);
                            txtSong.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                            txtArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());
                            portraitmp3layout.setVisibility(View.VISIBLE);
                            txtArtist.setVisibility(View.VISIBLE);
                            txtSong.setVisibility(View.VISIBLE);
                        }
                    });

                }

            }
            catch(Exception e) {
                Toast.makeText(HomeActivity.this,"App Crashe", Toast.LENGTH_LONG).show();

                sendLastCrashLog();

            }


        }
    };


    private void checkimgSuccesive()
    {
        try {

            if (arrAdvertisements.size() > 0) {

                if ((Integer) mPreview.getTag() == VIDEO_AD_VIEW_TAG) {
                    PlaylistWatcher.ADVERTISEMENT_TIME_COUNTER = 0;
                    shouldPlaySoftStopAd = false;
                    PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER = 0;
                }

                String advertisementTypeSong = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_song_Adv);

                String advertisementTypeMinute = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_Minute_Adv);

                String advertisementTypeTime = SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_Time_Adv);


                if (advertisementTypeSong.equals("1")) {


                    if (PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER == PlaylistWatcher.PLAY_AD_AFTER_SONGS) {

//                        Toast.makeText(HomeActivity.this, "Should play ad from Home", Toast.LENGTH_SHORT).show();
                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                } else if (advertisementTypeMinute.equals("1")) {

                    if (shouldPlaySoftStopAd) {

//                        T
// Toast.makeText(HomeActivity.this, "Should play soft stop ad", Toast.LENGTH_SHORT).show();

                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                } else if (advertisementTypeTime.equals("1")) {

                    if (shouldPlaySoftStopAd) {

                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                }
            }

            /*If song played was at last index then restart the playlist.*/
            if (!mPreview.isPlaying()) {
                if (arrSongs.size() - 1 > currentlyPlayingSongAtIndex) {
                    currentlyPlayingSongAtIndex++;
                } else {
                    currentlyPlayingSongAtIndex = 0;
                }
                if ((arrAdvertisements != null) && (arrAdvertisements.size() > 0)) {
                    PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;
                }
                mPreview.setTag(VIDEO_VIEW_TAG);
                insertsongStatus(currentlyPlayingSongAtIndex);

                String f = arrSongs.get(currentlyPlayingSongAtIndex).getTitle_Url();
                String h = f.substring(f.length() - 3);

                if (h.equals("jpg") || h.equals("jpeg") || h.equals("png")) {

                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            y = 0;
                            if (mPreview.isPlaying()) {
                                mPreview.stopPlayback();
                            }
                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            String k = arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            myImage.setVisibility(View.VISIBLE);
                            Imgicon.setVisibility(View.INVISIBLE);
                            myImage.setImageURI(Uri.parse(k));
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            txtArtist.setVisibility(View.INVISIBLE);
                            txtSong.setVisibility(View.INVISIBLE);
                        }
                    });
                    imgCountdowntimer1 = new CountDownTimer(15000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {

                            if (imgCountdowntimer1 != null) {
                                imgCountdowntimer1.cancel();
                                checkimgSuccesive();
                            }
                        }
                    }.start();

                } else if (h.equals("mp4")) {

                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((y == 0) && (myImage != null)) {
                                myImage.setVisibility(View.INVISIBLE);
//                            playerView.setVisibility(View.VISIBLE);
                            }
                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "Here");
                            txtArtist.setVisibility(View.INVISIBLE);
                            txtSong.setVisibility(View.INVISIBLE);
                            Imgicon.setVisibility(View.INVISIBLE);
                            mPreview.refreshDrawableState();
                            String path = arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            Uri uri = Uri.fromFile(new File(path));
                            mPreview.setVisibility(View.VISIBLE);
                            mPreview.playMedia(uri);

                        }
                    });
                } else {
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (y == 0) {
                                myImage.setVisibility(View.INVISIBLE);

                            }
                            // mPreview.reset();
                            Uri uri = Uri.fromFile(new File(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath()));
                            mPreview.playMedia(uri);
                            mPreview.setVisibility(View.INVISIBLE);
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            portraitmp3layout.setVisibility(View.VISIBLE);
                            txtArtist.setVisibility(View.VISIBLE);
                            txtSong.setVisibility(View.VISIBLE);
                            Imgicon.setVisibility(View.VISIBLE);
                            txtSong.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                            txtArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());

                        }
                    });

                }
            }
        }
        catch(Exception e) {
            Toast.makeText(HomeActivity.this,"App Crashe", Toast.LENGTH_LONG).show();

            sendLastCrashLog();

        }


    }

    private void setVisibilityAndPlayAdvertisement(){

        if (currentlyPlayingAdAtIndex < 0){ // If as is playing for the first time.
            currentlyPlayingAdAtIndex = 0;

        } else if(currentlyPlayingAdAtIndex == arrAdvertisements.size() - 1){ // If ad playing is at the last index
            currentlyPlayingAdAtIndex = 0;
        } else { // If ad is between 0 and index of ads array.
            currentlyPlayingAdAtIndex++;
        }
        mPreview.setTag(VIDEO_AD_VIEW_TAG);
        insertAdvertisementStatus(currentlyPlayingAdAtIndex);
        String f=arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvFileUrl();
        String h = f.substring(f.length() - 3);
        if(h.equals("mp3"))
        {
            Handler handler = new Handler(HomeActivity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mPreview.refreshDrawableState();
                    myImage.setVisibility(View.INVISIBLE);
                    mPreview.playMedia(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtFilePath());
                    circularProgressBar.setVisibility(View.INVISIBLE);
                    txtTokenId.setVisibility(View.INVISIBLE);
                    Imgicon.setVisibility(View.VISIBLE);
                    txtSong.setText(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtName());
                    portraitmp3layout.setVisibility(View.VISIBLE);
                    mPreview.setVisibility(View.INVISIBLE);
                    txtArtist.setVisibility(View.INVISIBLE);
                    txtSong.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            Handler handler = new Handler(HomeActivity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    myImage.setVisibility(View.INVISIBLE);
                    Log.d(TAG,"Here");
                    mPreview.refreshDrawableState();
                    txtTokenId.setVisibility(View.INVISIBLE);
                    Imgicon.setVisibility(View.INVISIBLE);
                    portraitmp3layout.setVisibility(View.INVISIBLE);
                    txtArtist.setVisibility(View.INVISIBLE);
                    circularProgressBar.setVisibility(View.INVISIBLE);
                    txtSong.setVisibility(View.INVISIBLE);
                    mPreview.setVisibility(View.VISIBLE);
                    mPreview.playMedia(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtFilePath());

                }
            });



            //123videoView.setVisibility(View.VISIBLE);
            //123videoView.setVideoPath(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtFilePath());
        }
    }



    /*OnPreparedListener videoViewPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared() {
            freeMemory();
            txtFileWriter.setVisibility(View.INVISIBLE);
            if ((Integer)videoView.getTag() == VIDEO_VIEW_TAG){
                insertsongStatus(currentlyPlayingSongAtIndex);
            } else {
                //insert ad status
            }
            //123videoView.start();
        }
    };*/



    private void getPlaylistsForCurrentTime(){

        if (arrPlaylists.size() > 0) arrPlaylists.clear();

        ArrayList<Playlist> playlistArrayList = new PlaylistManager(HomeActivity.this, null).getPlaylistForCurrentTimeOnly();

        if ((playlistArrayList.size() > 0) && (playlistArrayList != null)) {

            arrPlaylists.addAll(playlistArrayList);

            String p = arrPlaylists.get(0).getsplPlaylistCategory();

            if (p.equals("1")) {
                AudioManager am =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC, 0, 0);


            } else {
                AudioManager am =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0);
            }
        }

        if (arrPlaylists.size() > 0) {

            /*If current time has a playlist then get the playlist for future times also*/

//            playlistAdapter = new PlaylistAdapter(HomeActivity.this, arrPlaylists,false);
//            lvPlaylist.setAdapter(playlistAdapter);

            getSongsForPlaylist(arrPlaylists.get(0));
        }

    }

    private void getSongsForPlaylist(Playlist playlist){

        /*If there is not valid playlist set then set the current playlist.*/
        if (AlenkaMedia.currentPlaylistId.equals("")){
            AlenkaMedia.currentPlaylistId = playlist.getsplPlaylist_Id();
        }

        /*If the AlenkaMedia.currentPlaylistId is not equal to current playing playlist then set
         * the current playlist as AlenkaMedia.currentPlaylistId*/

        if (!AlenkaMedia.currentPlaylistId.equals(playlist.getsplPlaylist_Id())){
            AlenkaMedia.currentPlaylistId = playlist.getsplPlaylist_Id();
        }

        currentlyPlayingSongAtIndex = 0;

        if (arrSongs.size() > 0) arrSongs.clear();

        String schtype= SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.SchType);
        if(schtype.equals("Normal")) {

            songsArrayList = new PlaylistManager(HomeActivity.this, null).getSongsForPlaylist(playlist.getsplPlaylist_Id());
        }
        else
        {
            songsArrayList = new PlaylistManager(HomeActivity.this, null).getSongsForPlaylistRandom(playlist.getsplPlaylist_Id());

        }



        // ArrayList<Songs> songsArrayList = new PlaylistManager(HomeActivity.this,null).getSongsForPlaylist(playlist.getsplPlaylist_Id());

        if (songsArrayList != null && songsArrayList.size() > 0){
            arrSongs.addAll(songsArrayList);
        }

        if (arrSongs.size() > 0) {

            if (playlist.getIsSeparatinActive() == 1){
                sort(arrSongs);

            }

            songAdapter = new SongAdapter(HomeActivity.this, arrSongs);
            lvSongs.setAdapter(songAdapter);
        }
        else {

            ArrayList<Songs> songNotDownloaded = new PlaylistManager(HomeActivity.this,null).
                    getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

            if (songNotDownloaded.size() > 0){

                if (!Utilities.isMyServiceRunning(DownloadService.class, HomeActivity.this)){
                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }

            }
            // If there are no songs to be played then hide the player and show logo.
            mPreview.setVisibility(View.INVISIBLE);
        }


        if (arrSongs.size() > 0){

            //123videoView.setVisibility(View.VISIBLE);
            //123videoView.setTag(VIDEO_VIEW_TAG);
            if((arrAdvertisements!=null) && (arrAdvertisements.size()>0)) {
                PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;
            }
            try {
                String f=arrSongs.get(currentlyPlayingSongAtIndex).getTitle_Url();

                String h=f.substring(f.length() - 3);

                insertsongStatus(currentlyPlayingSongAtIndex);

                if(h.equals("jpg")||h.equals("jpeg")||h.equals("png"))
                {
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imgcounter=1;
                            //123videoView.reset();
                            //   hidenavigation();
                            y=0;
                            if(mPreview.isPlaying())
                            {
                                mPreview.stopPlayback();
                            }
                            // Utilities.showToast(HomeActivity.this,"Firsttime");

                            String p=arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            //123videoView.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            myImage.setVisibility(View.VISIBLE);
                            myImage.setImageURI(Uri.parse(p));
                            Imgicon.setVisibility(View.INVISIBLE);
                            txtArtist.setVisibility(View.INVISIBLE);
                            txtSong.setVisibility(View.INVISIBLE);
                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            //123videoView.start();

                        }
                    });
                    imgCountdowntimer2= new CountDownTimer(15000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {

                            if (imgCountdowntimer2 != null){
                                imgCountdowntimer2.cancel();
                                checkimgSuccesive();
                            }
                        }
                    }.start();
                }
                else if(h.equals("mp4"))
                {
                    imgcounter=1;
                    if(mPreview.isPlaying())
                    {
                        return;
                    }
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((y == 0) && (myImage!=null)) {
                                myImage.setVisibility(View.INVISIBLE);
                            }
                            Log.d(TAG,"Here");
                            Imgicon.setVisibility(View.INVISIBLE);
                            portraitmp3layout.setVisibility(View.INVISIBLE);
                            txtArtist.setVisibility(View.INVISIBLE);
                            txtSong.setVisibility(View.INVISIBLE);
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            String path = arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            Uri uri = Uri.fromFile(new File(path));
                            mPreview.setVisibility(View.VISIBLE);
                            mPreview.playMedia(uri);

                        }
                    });
                }
                else
                {
                    Handler handler = new Handler(HomeActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (y == 0) {
                                myImage.setVisibility(View.INVISIBLE);
                            }
                            imgcounter=1;
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            txtTokenId.setVisibility(View.INVISIBLE);
                            Imgicon.setVisibility(View.VISIBLE);
                            txtSong.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                            txtArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());
                            portraitmp3layout.setVisibility(View.VISIBLE);
                            String path = arrSongs.get(currentlyPlayingSongAtIndex).getSongPath();
                            mPreview.setVisibility(View.INVISIBLE);
                            Uri uri = Uri.fromFile(new File(path));
                            mPreview.playMedia(uri);
                            txtArtist.setVisibility(View.VISIBLE);
                            txtSong.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }

            catch (Exception e)
            {
                e.getCause();
                Toast.makeText(HomeActivity.this,"App Crashe", Toast.LENGTH_LONG).show();
                sendLastCrashLog();
            }
        }
        else {
            //123videoView.setVisibility(View.INVISIBLE);
        }
    }

    protected void onDestroy() {

        doUnbindService();
        stopService(new Intent(HomeActivity.this, DownloadService.class));
        //sendLastCrashLog();

        //123videoView.stopPlayback();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendDatabaseToServer();

    }

    private void sendDatabaseToServer() {
        new SendDatabaseToServerAsyncTask().execute();
    }

    @Override
    public void tankEmpty(int status) {

        dismissTankEmptyDialog();

        tankEmptyDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE);
        tankEmptyDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        tankEmptyDialog.setContentText("! WARNING refill the dispenser WARNING !");
        tankEmptyDialog.setCustomImage(R.drawable.logonusign);
        tankEmptyDialog.setCanceledOnTouchOutside(true);

        tankEmptyDialog.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (tankEmptyDialog != null && tankEmptyDialog.isShowing()) {
                    dismissTankEmptyDialog();
                }
            }
        };

        tankEmptyDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 3 * 1000);// 3 seconds

        notifyTankStatus(status);
    }

    private void dismissTankEmptyDialog(){
        if (tankEmptyDialog != null) {
            tankEmptyDialog.dismissWithAnimation();
            tankEmptyDialog = null;
        }
    }

    private void notifyTankStatus(int status){
        String token = SharedPreferenceUtil.getStringPreference(this, AlenkaMediaPreferences.TOKEN_ID);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("TokenId",token);
            jsonObject.put("TankStatusPercent",status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new OkHttpUtil(this, Constants.TANK_STATUS_REMINDER, jsonObject.toString(),
                this, false, Constants.TANK_STATUS_REMINDER_TAG).callRequest();
    }

    @Override
    public void tankInOut() {
        dismissTankEmptyDialog();
    }

    @Override
    public void playAnnouncement(Announcement announcement) {

        if (announcementTimer != null) {
            announcementTimer.cancel();
            announcementTimer = null;
        }

        if (imgCountdowntimer != null) {
            imgCountdowntimer.cancel();
            imgCountdowntimer = null;
        }
        if (imgCountdowntimer1 != null) {
            imgCountdowntimer1.cancel();
            imgCountdowntimer1 = null;
        }
        if (imgCountdowntimer2 != null) {
            imgCountdowntimer2.cancel();
            imgCountdowntimer2 = null;
        }

        if (announcement.getMediaType() == Announcement.MediaType.IMAGE) {

            String filePath = announcement.getAncFilePath();

            if (filePath == null){
                return;
            }

            File imgFile = new File(filePath);

            if (imgFile.exists()) {

                imgcounter = 1;
                y = 0;

                if (mPreview.isPlaying()) {
                    mPreview.stopPlayback();
                    mPreview.reset();
                    mPreview.clearSurfaceView();
                }

                mPreview.setVisibility(View.INVISIBLE);
                txtTokenId.setVisibility(View.INVISIBLE);
                circularProgressBar.setVisibility(View.INVISIBLE);
                myImage.setVisibility(View.VISIBLE);
                Imgicon.setVisibility(View.INVISIBLE);
                txtArtist.setVisibility(View.INVISIBLE);
                txtSong.setVisibility(View.INVISIBLE);
                portraitmp3layout.setVisibility(View.INVISIBLE);

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                myImage.setImageBitmap(myBitmap);
                Log.e(TAG,"Called image");
                SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING = true;
                announcementTimer = new CountDownTimer(5000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING = false;
                        announcementTimer.cancel();
                        announcementTimer = null;

                        myImage.setVisibility(View.INVISIBLE);

                        //If there are no playlists for current time, do not play next song.
                        if (AlenkaMedia.playlistStatus == NO_PLAYLIST){
                            txtTokenId.setVisibility(View.VISIBLE);
                            return;
                        }
                        checkimgSuccesive();

                    }
                }.start();
            }
        } else if (announcement.getMediaType() == Announcement.MediaType.VIDEO) {

            Handler handler = new Handler(HomeActivity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if ((y == 0) && (myImage != null)) {
                        myImage.setVisibility(View.INVISIBLE);
                        txtArtist.setVisibility(View.INVISIBLE);
                        txtSong.setVisibility(View.INVISIBLE);
                    }

                    if (mPreview.isPlaying()) {
                        mPreview.stopPlayback();
                        mPreview.reset();
                    }
                    portraitmp3layout.setVisibility(View.INVISIBLE);
                    Imgicon.setVisibility(View.INVISIBLE);
                    mPreview.setVisibility(View.VISIBLE);
                    mPreview.playMedia(announcement.getAncFilePath());
                    Log.e(TAG,"Called video");
                    SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING = true;
                }
            });
        }
    }

    View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {

            if (keyboardManager != null){
                keyboardManager.onKey(view, i, keyEvent);
            }

            return true;
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            return super.dispatchKeyEvent(event);
        }

        if (keyListener.onKey(null, event.getKeyCode(), event))
            return true;
        else
            return super.dispatchKeyEvent(event);
    }

    @Override
    public void keyboardInputReceived(Integer index) {

        Log.d(TAG,"Play media for index: " + index);

        /*If song played was at last index then restart the playlist.*/

        int selectedIndex = index;

        if (arrSongs.size() - 1 >= selectedIndex) {

        } else {

            PlaylistManager playlistManager = new PlaylistManager(this, null);

            int count = playlistManager.getSongsCountForPlaylistId(currentPlaylistID);

            if((count - 1) < selectedIndex ) {
                //1 is being added to show the count to user only
                Toast.makeText(this, "Media at this position does not exist.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(this, "Media download is in progress.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(imgCountdowntimer!=null) {
            imgCountdowntimer.cancel();
        }
        if(imgCountdowntimer1!=null) {
            imgCountdowntimer1.cancel();
        }
        if(imgCountdowntimer2!=null) {
            imgCountdowntimer2.cancel();
        }

        if((arrAdvertisements!=null) && (arrAdvertisements.size()>0)) {
            PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;
        }

        mPreview.setTag(VIDEO_VIEW_TAG);
        insertsongStatus(selectedIndex);

        try {
            mPreview.stopPlayback();
            mPreview.reset();
            mPreview.clearSurfaceView();
            mPreview.release();
        }catch (Exception e){
            e.printStackTrace();
        }

        String f = arrSongs.get(selectedIndex).getTitle_Url();
        String h = f.substring(f.length() - 3);

        if(h.equals("jpg")||h.equals("jpeg")||h.equals("png"))
        {
            Handler handler = new Handler(HomeActivity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    y=0;

                    String k = arrSongs.get(selectedIndex).getSongPath();
                    portraitmp3layout.setVisibility(View.INVISIBLE);
                    myImage.setVisibility(View.VISIBLE);
                    myImage.setImageURI(Uri.parse(k));
                    mPreview.setVisibility(View.INVISIBLE);
                    circularProgressBar.setVisibility(View.INVISIBLE);
                    txtTokenId.setVisibility(View.INVISIBLE);
                    Imgicon.setVisibility(View.INVISIBLE);
                    txtArtist.setVisibility(View.INVISIBLE);
                    txtSong.setVisibility(View.INVISIBLE);
                }
            });
            imgCountdowntimer= new CountDownTimer(15000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    if (imgCountdowntimer != null) {
                        imgCountdowntimer.cancel();
                        mediaPlayerCompletionListener.onCompletion(mPreview.mediaPlayer);
                    }

                }
            }.start();
        }
        else if(h.equals("mp4")) {

            Handler handler = new Handler(HomeActivity.this.getMainLooper());

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if ((y == 0) && (myImage!=null)) {
                        myImage.setVisibility(View.INVISIBLE);
                        txtArtist.setVisibility(View.INVISIBLE);
                        txtSong.setVisibility(View.INVISIBLE);
                    }

                    portraitmp3layout.setVisibility(View.INVISIBLE);
                    mPreview.setVisibility(View.VISIBLE);
                    Uri uri = Uri.fromFile(new File(arrSongs.get(selectedIndex).getSongPath()));
                    mPreview.playMedia(uri);
                    Imgicon.setVisibility(View.INVISIBLE);
                }
            });
        }
        else
        {
            Handler handler = new Handler(HomeActivity.this.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (y == 0) {
                        myImage.setVisibility(View.INVISIBLE);
                    }
                    Uri uri = Uri.fromFile(new File(arrSongs.get(selectedIndex).getSongPath()));
                    mPreview.playMedia(uri);
                    mPreview.setVisibility(View.INVISIBLE);
                    circularProgressBar.setVisibility(View.INVISIBLE);
                    txtTokenId.setVisibility(View.INVISIBLE);
                    Imgicon.setVisibility(View.VISIBLE);
                    txtSong.setText(arrSongs.get(selectedIndex).getTitle());
                    txtArtist.setText(arrSongs.get(selectedIndex).getAr_Name());
                    portraitmp3layout.setVisibility(View.VISIBLE);
                    txtArtist.setVisibility(View.VISIBLE);
                    txtSong.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void increaseCurrentPlaylistIndex(){
        if (arrSongs.size() - 1 > currentlyPlayingSongAtIndex) {
            currentlyPlayingSongAtIndex++;
        } else {
            currentlyPlayingSongAtIndex = 0;
        }
    }

    class SendDatabaseToServerAsyncTask extends AsyncTask<String, Void ,String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Utilities.showToast(HomeActivity.this,"Start sending database");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //  Utilities.showToast(HomeActivity.this,"Finished sending database");

        }

        @Override
        protected String doInBackground(String... strings) {

            final String error;

            String path = HomeActivity.this.getApplicationInfo().dataDir
                    + File.separator + Constants.ROOT_FOLDER
                    + File.separator + MySQLiteHelper.DATABASE_NAME;

            if (path != null) {

                File dbFile = new File(path);

                if (dbFile.exists()) {

                    MediaType mediaType = MediaType.parse("multipart/form-data;");

                    String token = SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.TOKEN_ID);

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM).addFormDataPart("",token + "-" + dbFile.getName(),RequestBody.create(mediaType, dbFile)                                    )
                            .build();

                    Request request = new Request.Builder()
                            .url("http://134.119.178.26/ReceiveUpload.aspx")
                            .post(requestBody)
                            .build();

                    OkHttpClient client =new OkHttpClient();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String responseString = response.body().toString();

                            if (responseString != null) {

                            }
                        }
                    });
                }
            }

            return null;
        }


    }




    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Remove below line
        if (serialPortManager != null) {
            serialPortManager.startConnection();
        }

        Log.e(TAG,"Starting timer");
        startRepeatingTimer(null);
        sendLastCrashLog();


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent != null){

                    boolean shouldPlaylistChange = intent.getBooleanExtra(Constants.ALARM_PLAYLIST_CHANGED,false);

                    if (shouldPlaylistChange){
                        // Stop the current playlist and start new playlist.
                        switchPlaylist();
                    } else {

                        int playlistStatus = intent.getIntExtra(Constants.ALARM_ACTION,-12);

                        if (playlistStatus == 0 || playlistStatus == 1){
                            onPlaylistTimeChanged(playlistStatus);
                        }
                    }
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = NetworkUtil.getConnectivityStatusString(context);

                boolean isConnected = haveNetworkConnection();

                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {

                    /*if(status== NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){

                        Toast.makeText(context, "Network Disconnected", Toast.LENGTH_SHORT).show();
                        HomeActivity.this.stopService(new Intent(HomeActivity.this, DownloadService.class));

                    }else{

                        Toast.makeText(context, "Network Connected", Toast.LENGTH_SHORT).show();

                        if (!AlenkaMedia.getInstance().isDownloadServiceRunning && !isCountDownTimerRunning){

                            HomeActivity.this.startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                            doBindService();
                        }
                    }*/

                    if (isConnected){

                        Toast.makeText(context, "Network Connected", Toast.LENGTH_SHORT).show();

                        if (!AlenkaMedia.getInstance().isDownloadServiceRunning && !isCountDownTimerRunning){

                            HomeActivity.this.startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                            doBindService();
                        }

                    } else {

                        Toast.makeText(context, "Network Disconnected", Toast.LENGTH_SHORT).show();
                        HomeActivity.this.stopService(new Intent(HomeActivity.this, DownloadService.class));
                    }
                }

            }
        };
        registerReceiver(networkChangeReceiver, intentConnectivity);

//        checkForPlaylistStatus.postDelayed(handlePlaylistStatus,delay);
        updatePlayerLoginStatus();

    }

    private boolean haveNetworkConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
     *TODO: On back press button code if user press back button the application will not destroy or stop
     * */
    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (Integer.parseInt(String.valueOf(Build.VERSION.SDK_INT)) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int shotCount = -1;

    private void simulateShotCount(){

        if (serialPortManager != null) {
            if (shotCount == -1) {
                shotCount = 1;
            }

            if (serialPortManager != null){
                serialPortManager.readShotCount("shots - " + shotCount);
            }
        }
    }

    @Override
    public void shouldUpdateTimeOnServer() {

        updatePlayerSongsStatus();
        updateAnnouncementLogsOnServer();
        updateDownloadedSongsStatusOnServer();
        checkForUpdateData();

    }

    @Override
    public void checkForPendingDownloads() {
//        checkForUnfinishedDownloads();
    }

    @Override
    public void playAdvertisement() {

        if (arrAdvertisements == null || arrAdvertisements.size() == 0){

            PlaylistWatcher.ADVERTISEMENT_TIME_COUNTER = 0;
            shouldPlaySoftStopAd = false;
            PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER = 0;
            return;
        }

        String adPlayType = SharedPreferenceUtil.getStringPreference(HomeActivity.this, AlenkaMediaPreferences.playing_Type);

        if (adPlayType.equals("Hard Stop")){

            // Stop current song and play song

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (arrAdvertisements.size() > 0){

                        if (mPreview.isPlaying()){
                            mPreview.release();
                            //mPreview.reset();
                            setVisibilityAndPlayAdvertisement();
                        }
                        else
                        {
                            //   mPreview.release();
                            setVisibilityAndPlayAdvertisement();
                        }
                    }
                }
            });

        } else if(adPlayType.equals("Soft Stop")){

            shouldPlaySoftStopAd = true;
        }

    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();

            shotCount = 0;

            if(mPreview.isPlaying())
            {
                mPreview.stopPlayback();
        }
            updateLogoutStatus();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    Runnable handlePlaylistStatus = new Runnable() {
        @Override
        public void run() {

            if (currentPlaylistStatus != AlenkaMedia.playlistStatus){
                currentPlaylistStatus = AlenkaMedia.playlistStatus;
                onPlaylistTimeChanged(currentPlaylistStatus);
            }

//            checkForPlaylistStatus.postDelayed(this, delay);
        }
    };

    @Override
    protected void onStop() {

        if (serialPortManager != null) {
            //TODO: Remove below line
            serialPortManager.closeConnection();
            serialPortManager.release();
        }

        Log.e(TAG,"Stopping timer");
        cancelRepeatingTimer(null);
        mPreview.stopPlayback();
        //123videoView.stopPlayback();
        stopService(new Intent(HomeActivity.this, DownloadService.class));
//        checkForPlaylistStatus.removeCallbacks(handlePlaylistStatus);
        super.onStop();
    }

    @Override
    protected void onPause() {

        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(networkChangeReceiver);
        super.onPause();

    }

    /*************************PlaylistWatcher Methods Starts****************************/

    public void startRepeatingTimer(View view) {
        Context context = HomeActivity.this;
        if(alarm != null){
            alarm.setWatcher();
        }else{
//            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelRepeatingTimer(View view){
        Context context = this.getApplicationContext();
        if(alarm != null){
            alarm.cancelWatcher();
        }else{
//            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPlaylistTimeChanged(int playlistCode) {

        switch (playlistCode){
            case NO_PLAYLIST:{

                /*123if (videoView.isPlaying()){
                    //123videoView.stopPlayback();
                    //123videoView.setVisibility(View.GONE);
                }*/
            }break;

            case PlaylistWatcher.PLAYLIST_PRESENT:{
                getPlaylistsForCurrentTime();
            }break;
        }
    }
    public void switchPlaylist() {

        /*if (videoView.isPlaying()){
            //123videoView.stopPlayback();
            getPlaylistsForCurrentTime();
        }*/
    }

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
    /*This method inserts the status of song as played in database.*/

    public void insertsongStatus(final int index){

        String artist_id = arrSongs.get(index).getArtist_ID();
        String title_id  = arrSongs.get(index).getTitle_Id();
        String spl_plalist_id = arrSongs.get(index).getSpl_PlaylistId();

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.artist_id = artist_id;
        playerStatusManager.title_id = title_id;
        playerStatusManager.spl_plalist_id = spl_plalist_id;
        playerStatusManager.insertSongPlayedStatus();

        playerStatusManager = null;
    }



    public void insertAdvertisementStatus(final int index){

        String currentDate = Utilities.currentDate();
        String currenttime = Utilities.currentTime();

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setAdvPlayedDate(currentDate);
        playerStatus.setAdvPlayedTime(currenttime);
        playerStatus.setAdvIdStatus(arrAdvertisements.get(index).getAdvtID());
        playerStatus.setPlayerStatusAll("adv");

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.insertAdvPlayerStatus(playerStatus);

    }


    private void updatePlayerLoginStatus(){

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.updateLoginStatus();
        playerStatusManager.updateHeartBeatStatus();
        playerStatusManager.updateDataOnServer();

    }

    private void updatePlayerSongsStatus(){
        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.sendPlayedSongsStatusOnServer();
    }

    private void updateAnnouncementLogsOnServer() {
        if (serialPortManager != null){
            serialPortManager.getAnnouncementLogsManager().sendPlayedSongsStatusOnServer();
        }
    }

    private void updateLogoutStatus(){
        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.updateLogoutStatus();
    }

    @Override
    public void onResponse(String response, int tag) {
        if (response == null || response.equals("") || response.length() < 1){
            Toast.makeText(HomeActivity.this, "Empty response for player statuses", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tag == Constants.TANK_STATUS_REMINDER_TAG){

            try {
                JSONObject jsonObject = new JSONObject(response);
                Integer status = jsonObject.getInt("Response");
                if (status == 1){
                    Log.d(TAG,"Notified");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Exception e, int tag) {

    }

    public static void saveLogcatToFile(Context context) {

        String fileName = "logcat_"+System.currentTimeMillis()+".txt";
        File outputFile = new File(Environment.getExternalStorageDirectory(),fileName);
        try {
            @SuppressWarnings("unused")
            Process process = Runtime.getRuntime().exec("logcat -e "+outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlaylistStatusChanged(int status) {

        switch (status){
            case NO_PLAYLIST:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mPreview.isPlaying())
                        {
                            mPreview.stopPlayback();
                        }
                        if(imgCountdowntimer!=null) {
                            imgCountdowntimer.cancel();
                        }
                        if(imgCountdowntimer1!=null) {
                            imgCountdowntimer1.cancel();
                        }
                        if(imgCountdowntimer2!=null) {
                            imgCountdowntimer2.cancel();
                        }

                        txtTokenId.setVisibility(View.VISIBLE);
                        myImage.setVisibility(View.INVISIBLE);
                        myImage.setImageDrawable(null);
                        ctrplaylistchg=1;
                        playlistcounter=1;
                    }
                });



            }break;

            case PlaylistWatcher.PLAYLIST_PRESENT:{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mPreview.isPlaying())
                        {
                            mPreview.stopPlayback();
                        }

                        playlistcounter=0;
                        PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER=0;
                        ctrplaylistchg=1;
                        getPlaylistsForCurrentTime();
                    }
                });


            }break;

            case PlaylistWatcher.PLAYLIST_CHANGE:{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //    Toast.makeText(HomeActivity.this,"Playlist Changing",Toast.LENGTH_LONG).show();
                        if(mPreview.isPlaying())
                        {
                            mPreview.stopPlayback();
                        }

                        playlistcounter=0;
                        PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER=0;
                        ctrplaylistchg=1;
                        getPlaylistsForCurrentTime();
                    }
                });


            }break;
        }

    }

    private void sort(ArrayList<Songs> songsArrayList){

        try {
            Collections.shuffle(songsArrayList);

        } catch (Exception e){

            Log.e("Sort exception","");
            e.printStackTrace();
        }
    }
    /*************************PlaylistWatcher Methods Ends*/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanForFiles(){

        String path = HomeActivity.this.getApplicationInfo().dataDir;

        File file = new File(path);

     /*   Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
        File sdCard = externalLocations.get(ExternalStorage.SD_CARD);

        if (sdCard != null){
            sdCardLocation = sdCard.getAbsolutePath();
        }
        File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
*/
       /* HashSet<String> externalLocations = ExternalStorage.getExternalMounts();

        if (externalLocations.size() > 0){

            Object[] aa = externalLocations.toArray();
            sdCardLocation = (String) aa[0];
        }*/

        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();

        File[] files = HomeActivity.this.getExternalMediaDirs();

        String[] sdsd = files[1].getAbsolutePath().split("/");

        if (sdsd.length > 3){
            String zeroComponent = sdsd[0];

            if (zeroComponent.equals(" ") || zeroComponent.equals("")){
                zeroComponent = "/";
            }
            String firstComponent = sdsd[1];
            String secondComponent = sdsd[2];

            String finalPath = zeroComponent + firstComponent + File.separator + secondComponent;

            sdCardLocation = finalPath;
        }

       /* if (files.length > 1){

            String storage = files[1].getAbsolutePath();
            sdCardLocation = storage;
        }*/

        if (arrVideoFiles.size() > 0){

            targetFileName = arrVideoFiles.get(13);

//            targetFileName = firstVideoFileLocation.substring(firstVideoFileLocation.lastIndexOf("/")+1);

            try {

                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 42);

               /* File target = new File(sdCardLocation.concat(File.separator + targetFileName));
                File source = new File(firstVideoFileLocation);
                copyDirectory(source,target);*/

            }catch (Exception e){
                Log.e("File Copy", "Failed");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_STORAGE_FOLDER_SELECTOR){

            Uri treeUri = resultData.getData();


            String path = FileUtil.getFullPathFromTreeUri(treeUri, HomeActivity.this);

            if (path != null){

                File sourceDirectory = new File(path);

                if (sourceDirectory != null){

                    Toast.makeText(HomeActivity.this, "" + path, Toast.LENGTH_SHORT).show();

                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

                    AlenkaMedia.globalDocumentFile = sourceDirectory;

                    String pickedD = pickedDir.toString();

                    if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                        startService(new Intent(this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,treeUri.toString()));
                        doBindService();
                    }
                }
            }


        }
    }

    void listRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                listRecursive(child);

        Log.e("List Files: ", fileOrDirectory.getName());

        if (fileOrDirectory.getName().endsWith(".mp4")) {

            arrVideoFiles.add(fileOrDirectory.getAbsolutePath());
        }
    }

    private void updateDownloadedSongsStatusOnServer(){

        int totalSongs = new PlaylistManager(HomeActivity.this,null).getTotalDownloadedSongs();

        if (totalSongs >= 0){


            PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
            playerStatusManager.songsDownloaded = "" + totalSongs;
            playerStatusManager.updateDownloadedSongsCountOnServer();
        }

        /*if (countOfTotalSongsToBeDownloaded > 0){

            if (getSongsToBeDownloaded() != null){

                int songsThatHaveBeenDownloaded =  countOfTotalSongsToBeDownloaded - getSongsToBeDownloaded().size();

                if (songsThatHaveBeenDownloaded > 0){

                    PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
                    playerStatusManager.songsDownloaded = "" + songsThatHaveBeenDownloaded;
                    playerStatusManager.updateDownloadedSongsCountOnServer();
                }
            }
        }*/
    }

    private void checkForUnfinishedDownloads(){

        Toast.makeText(HomeActivity.this, "Checking for unfinished downloads.", Toast.LENGTH_SHORT).show();

        Log.e(TAG, "Checking for unfinished downloads.");
        if (ConnectivityReceiver.isConnected()){

            Log.e(TAG, "Internet is connected.");
            if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                ArrayList<Songs> songs = getSongsToBeDownloaded();
                ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();

                if (songs != null && songs.size() > 0 ||
                        ads != null && ads.size() > 0) {
                    Log.e(TAG, "Starting download.");
                    Toast.makeText(HomeActivity.this, "Starting download for unfinished songs.", Toast.LENGTH_SHORT).show();
                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }
            }
        }
    }

    private void checkForUpdateData(){

        boolean shouldUpdateData = AlenkaMedia.getInstance().isUpdateInProgress;

        if (!shouldUpdateData)
            new PlaylistManager(HomeActivity.this, playlistLoaderListener).checkUpdatedPlaylistData();
    }

    PlaylistLoaderListener playlistLoaderListener = new PlaylistLoaderListener() {
        @Override
        public void startedGettingPlaylist() {

            Log.e(TAG,"Started getting playlist");
        }

        @Override
        public void finishedGettingPlaylist() {

            Log.e(TAG,"Finished getting playlist");

            ArrayList<Songs> songs = getSongsToBeDownloaded();
            ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();

            /*
            If new songs are present. Start downloading them, else restart the player for playlist time changes sync.
             */
            if (songs != null && songs.size() > 0 ||
                    ads != null && ads.size() > 0){

                if (!AlenkaMedia.getInstance().isDownloadServiceRunning){
                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }



            } else {

                PlaylistManager playlistManager = new PlaylistManager(HomeActivity.this,playlistLoaderListener);
                playlistManager.publishTokenForUpdatedData();
            }
        }

        @Override
        public void errorInGettingPlaylist() {

        }

        @Override
        public void recordSaved(boolean isSaved) {

        }

        @Override
        public void tokenUpdatedOnServer() {

            AlenkaMedia.getInstance().isUpdateInProgress = false;
            restartPlayer();
        }
    };

    private void restartPlayer(){

        AlenkaMedia.playlistStatus = -12;
        AlenkaMedia.currentPlaylistId = "";
        startActivity(new Intent(HomeActivity.this, Splash_Activity.class));
        HomeActivity.this.finish();
    }


    private void showAlertDialogForStorageSelection(){

        dialogBuilder = new AlertDialog.Builder(HomeActivity.this, R.style.AppTheme_MaterialDialogTheme);

        dialogBuilder.setTitle("Select source");
        dialogBuilder.setMessage("Please select source of songs.");
        dialogBuilder.setNegativeButton("Download from Internet",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mCountDownTimer != null){
                            mCountDownTimer.cancel();
                        }

                        alarm = new PlaylistWatcher();
                        alarm.setContext(HomeActivity.this);
                        alarm.setPlaylistStatusListener(HomeActivity.this);
                        alarm.setWatcher();

                        startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                        doBindService();
                        dialog.dismiss();
                    }
                }
        ).setPositiveButton("External Storage", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mCountDownTimer != null){
                    mCountDownTimer.cancel();
                }

                if (Utilities.isVersionLowerThanLollipop()){

                    List<StorageUtils.StorageInfo> list = StorageUtils.getStorageList();

                    if (list.size() > 1){

                        File root = new File(list.get(1).path, "AlenkaMedia");

                        if (root.exists()){

                            AlenkaMedia.globalDocumentFile = root;

                            startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,root.toString()));
                            doBindService();
                            alarm = new PlaylistWatcher();
                            alarm.setContext(HomeActivity.this);
                            alarm.setPlaylistStatusListener(HomeActivity.this);
                            alarm.setWatcher();

                        }else {
                            Toast.makeText(HomeActivity.this, "AlenkaMedia folder does not exist in external storage.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(HomeActivity.this, "No external storage found.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_FOLDER_SELECTOR);
                }

//                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_FOLDER_SELECTOR);
                dialogInterface.dismiss();
            }
        })
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (mCountDownTimer != null){
                            mCountDownTimer.cancel();
                        }

                        SharedPreferenceUtil.setBooleanPreference(HomeActivity.this, Constants.STORAGE_ALERT_SHOWN_ONCE,false);
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                });
        dialogBuilder.setCancelable(false);

        dialog = dialogBuilder.create();
        final Window dialogWindow = dialog.getWindow();
        final WindowManager.LayoutParams dialogWindowAttributes = dialogWindow.getAttributes();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
// Set fixed width (280dp) and WRAP_CONTENT height
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogWindowAttributes);
        lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);

// Set to TYPE_SYSTEM_ALERT so that the Service can display it
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_TOAST);
//            dialogWindowAttributes.windowAnimations = R.style.Dialo;
        dialog.show();
    }
}
