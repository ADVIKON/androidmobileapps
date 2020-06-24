package com.NuSignSignagePlayer.api_manager;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import com.NuSignSignagePlayer.activities.HomeActivity;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import android.os.Message;
import android.os.StatFs;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.NuSignSignagePlayer.R;
import com.NuSignSignagePlayer.application.AlenkaMedia;
import com.NuSignSignagePlayer.database.SongsDataSource;
import com.NuSignSignagePlayer.interfaces.DownloadListener;
import com.NuSignSignagePlayer.interfaces.PlaylistLoaderListener;
import com.NuSignSignagePlayer.mediamanager.AdvertisementsManager;
import com.NuSignSignagePlayer.mediamanager.PlaylistManager;
import com.NuSignSignagePlayer.models.Advertisements;
import com.NuSignSignagePlayer.models.Playlist;
import com.NuSignSignagePlayer.models.Songs;
import com.NuSignSignagePlayer.utils.Constants;
import com.NuSignSignagePlayer.utils.ExternalStorage;
import com.NuSignSignagePlayer.utils.Utilities;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by love on 30/5/17.
 */
public class DownloadService extends Service {

    public static final String TAG = "DownloadService";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    public static boolean serviceState = false;

    public static ArrayList<Songs> songsToBeDownloaded = new ArrayList<>();
    ArrayList<Advertisements> adsToBeDownloaded = new ArrayList<>();
    public Context mContext;

    public static int downloadingFileAtIndex = 0;

    int downloadingAdsAtIndex = 0;
    private int Startdownload=0;
    public HomeActivity hmactivty=new HomeActivity();

    private final ArrayList<DownloadListener> mListeners
            = new ArrayList<DownloadListener>();

    private final Handler mHandler = new Handler();
    public File gblStorage;

    private final IBinder mBinder = new LocalBinder();

    private TimerTask timerTask;

    final Handler handlerForRestartingDownload = new Handler();
    private Timer timer;
    public long downloadId;
    public String filePath="";
    private String sdCardLocation;

//    private DocumentFile[] arrVideoFilesFromSDCard;

    private File[] arrVideoFilesFromSDCard;

    private int COPY_SONG_AT_INDEX = 0;

    public static DocumentFile pickedDirectory = null;

    public static File sourceFileDirectory = null;

    public static Uri fileUri = null;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {

            /*  downloadMedia();*/
//            stopSelf(msg.arg1);
        }
    }


    public void prepareForDownload(){

        if (arrVideoFilesFromSDCard == null){

            arrVideoFilesFromSDCard = new File[sourceFileDirectory.listFiles().length + 1];
        }

        for (int count = 0; count < sourceFileDirectory.listFiles().length; count++){

            File file = sourceFileDirectory.listFiles()[count];

            arrVideoFilesFromSDCard[count] = file;

            Toast.makeText(DownloadService.this,"Added : " + file.getName(),Toast.LENGTH_SHORT).show();

            Log.e(TAG,"Added File " + file.getName());
        }

        if (arrVideoFilesFromSDCard.length > 0) {
          //  ArrayList<Songs> UnschdSonglist = new SongsDataSource(this).getUnschdSongsThoseAreNotDownloaded();
            ArrayList<Songs> arrSongs = getSongsToBeDownloaded();
           if(arrSongs!=null ) {
               if (arrSongs != null && arrSongs.size()>0) {
                   songsToBeDownloaded.addAll(arrSongs);
               }

               startDownloadingSongs();
               downloadSongsLocally();
           }

            else {

                prepareForDownloadingAdsLocally();
            }
        }
    }


    private void downloadSongsLocally(){

       /* if (arrVideoFilesFromSDCard == null){
            arrVideoFilesFromSDCard = documentFile.listFiles();
        }*/

        if (songsToBeDownloaded != null && songsToBeDownloaded.size() > 0){

            Songs songs = songsToBeDownloaded.get(COPY_SONG_AT_INDEX);

            if (songs != null){

                if (arrVideoFilesFromSDCard == null || arrVideoFilesFromSDCard.length == 0){
                    Toast.makeText(DownloadService.this, "No songs found inside external storage.", Toast.LENGTH_SHORT).show();
                }

                for (File file : arrVideoFilesFromSDCard){

                    String fileNameWithoutExtension = Utilities.removeFileExtension(file.getName(), DownloadService.this);

//                    String decodedFileName = Utilities.decodeFromBase64(fileNameWithoutExtension)
//                            + DownloadService.this.getString(R.string.mp4_file_extension);
                    String fileName = fileNameWithoutExtension + DownloadService.this.getString(R.string.mp4_file_extension);

                    if (Utilities.removeSpecialCharacterFromFileName(songs.getTitle()).equals(fileName)){
                        Log.e(TAG,"Matched");
                        startCopyFromExternalStorageTask(file, songs,fileName);
                        break;
                    }
                }

            }
        } else {
            Toast.makeText(DownloadService.this, "No songs found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCopyFromExternalStorageTask(File file, Songs songs, String decodedFileName){

        CopyFromExternalStorage videoWriteTask = new CopyFromExternalStorage();
        videoWriteTask.storageFile = file;
        videoWriteTask.songs = songs;
        videoWriteTask.copyingFileName = decodedFileName;
        videoWriteTask.execute();
    }

    private void startCopyAdFromExternalStorageTask(File file, Advertisements ad, String decodedFileName){

        CopyAdsFromExternalStorage videoWriteTask = new CopyAdsFromExternalStorage();
        videoWriteTask.storageFile = file;
        videoWriteTask.ad = ad;
        videoWriteTask.copyingFileName = decodedFileName;
        videoWriteTask.execute();
    }

    private void sendCopyIntimation(int current, int total, boolean isFinished){
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).startedCopyingSongs(current, total, isFinished);
        }
    }

    private void sendCopyIntimationForSongsCopied(int totalSongsCopied){

        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).finishedDownloadingSongs(totalSongsCopied);
        }
    }

    public class CopyFromExternalStorage extends AsyncTask<String, String, String> {

        public File storageFile = null;
        public Songs songs = null;
        public String copyingFileName = null;
        Exception e = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Toast.makeText(DownloadService.this, "Start", Toast.LENGTH_SHORT).show();
            int songIndex = COPY_SONG_AT_INDEX + 1;
            sendCopyIntimation(songIndex, songsToBeDownloaded.size(), false);
//            txtFileWriter.setText("Copying song " + songIndex + " of " + arrVideoFiles.size());
        }

        @Override
        protected String doInBackground(String... params) {

            String success = "0";
            long total = 0;

            try {

                String username = "bob@google.org";
                String password = "Password1";
                String secretID = "BlahBlahBlah";
                String SALT2 = "deliciously salty";

                byte[] key = (SALT2 + username + password).getBytes("UTF-8");
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16); // use only first 128 bit


                SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
                Cipher decipher = Cipher.getInstance("AES");
                decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

                FileOutputStream out = openFileOutput(copyingFileName, Context.MODE_PRIVATE);
                CipherOutputStream cos = new CipherOutputStream(out,decipher);
//                FileInputStream encfis = new FileInputStream(outfile);



                /*KeyGenerator kgen = KeyGenerator.getInstance("AES");
                SecretKey skey = kgen.generateKey();*/

                Uri uri = Uri.fromFile(storageFile);
                InputStream in = getContentResolver().openInputStream(uri);
                Cursor cursor = getContentResolver().query(uri,
                        null, null, null, null);

//                cursor.moveToFirst();

//                long lenghtOfFile = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
//                File fileSize = new File(storageFile.getAbsolutePath());
//
//                long lenghtOfFile = out.length();

                long file_size = storageFile.length();

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    total += len;
                    int progress = (int) ((total * 100) / file_size);
                    publishProgress("" + progress);

                    cos.write(buf,0,len);
                    cos.flush();
//                    out.write(buf, 0, len);
                }
                cos.close();

                /*
                while((read=encfis.read())!=-1)
        {
            cos.write(read);
            cos.flush();
        }        cos.close();
                 */
                in.close();
                out.close();
                success = "1";
            }catch (Exception e){
                e.printStackTrace();
                this.e = e;
            }
            return success;
        }

        protected void onProgressUpdate(String... progress) {
            final int intProgress = Integer.parseInt(progress[0]);
            sendUpdate(intProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            circularProgressBar.setProgress(0);

            if (e != null){
                e.printStackTrace();
                Toast.makeText(DownloadService.this, e.getMessage(), Toast.LENGTH_LONG ).show();
            }
            if (result.equals("1")){

                if (COPY_SONG_AT_INDEX < arrVideoFilesFromSDCard.length - 1){

                    File downloadedFile = getFileStreamPath(this.copyingFileName);

                    songs.setIs_Downloaded(1);
                    songs.setSongPath(downloadedFile.getAbsolutePath());



                    new PlaylistManager(DownloadService.this,null).songDownloaded(songs, new PlaylistLoaderListener() {
                        @Override
                        public void startedGettingPlaylist() {

                        }

                        @Override
                        public void finishedGettingPlaylist() {

                        }

                        @Override
                        public void errorInGettingPlaylist() {

                        }

                        @Override
                        public void tokenUpdatedOnServer() {

                        }

                        @Override
                        public void recordSaved(boolean isSaved) {

                            if (isSaved){

                                if (arrVideoFilesFromSDCard.length - 1 > COPY_SONG_AT_INDEX &&
                                        COPY_SONG_AT_INDEX < songsToBeDownloaded.size() - 1){

                                    COPY_SONG_AT_INDEX++;
                                    downloadSongsLocally();

                                } else {
                                    Toast.makeText(DownloadService.this, "ALL SONGS FINISHED", Toast.LENGTH_SHORT).show();
                                    // All songs from external storage have been downloaded
                                    sendCopyIntimation(0,0,false);

                                    prepareForDownloadingAdsLocally();

                                }
                            }

                        }
                    });

                } else {

//                    txtFileWriter.setText("Copying finished");
//                    Toast.makeText(HomeActivity.this, "All files transferred.", Toast.LENGTH_SHORT).show();

                }
            } else {
                Log.e("Error", "In writing video songs file...restarting");
                downloadSongsLocally();
            }
        }
    }

    @Override
    public void onCreate() {
        serviceState = true;
        HandlerThread thread = new HandlerThread("ServiceStartArguments", 1);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlenkaMedia.getInstance().isDownloadServiceRunning = true;

        String string = intent.getStringExtra(Constants.TAG_START_DOWNLOAD_SERVICE);

        boolean shoudDownloadFromInternet = intent.getBooleanExtra(Constants.TAG_START_DOWNLOAD_SERVICE,false);

        if (shoudDownloadFromInternet && string == null){

            ArrayList<Songs> arrSongs = getSongsToBeDownloaded();
            ArrayList<Advertisements> arrAds = getAdvertisementsToBeDownloaded();
          //  ArrayList<Songs> UnschdSonglist = new SongsDataSource(this).getUnschdSongsThoseAreNotDownloaded();

            if(arrSongs!=null) {
                if (arrSongs != null && arrSongs.size()>0) {
                    songsToBeDownloaded.addAll(arrSongs);
                }

                if (songsToBeDownloaded.size() > 0) {
                    downloadMedia();
                }

            }

            else if (arrAds != null){
                prepareForDownloadingAds();
            }

        } else if (string != null){

            if (AlenkaMedia.globalDocumentFile != null){

                sourceFileDirectory = AlenkaMedia.globalDocumentFile;
            }

            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            prepareForDownload();

        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroyed");
        serviceState = false;
        mHandler.removeCallbacks(mTickRunnable);
        AlenkaMedia.getInstance().isDownloadServiceRunning = false;
        songsToBeDownloaded.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    public void downloadMedia() {

        if (songsToBeDownloaded.size() > 0) {
            startDownloadingSongs();

        } else {
            prepareForDownloadingAds();

        }
    }

    private ArrayList<Songs> getSongsToBeDownloaded(){

        ArrayList<Playlist> playlists = new PlaylistManager(DownloadService.this, null).getPlaylistFromLocallyToBedDownload();

        List<Playlist> noRepeat = new ArrayList<Playlist>();

        for (Playlist event : playlists) {

            boolean isFound = false;
            // check if the event name exists in noRepeat
            for (Playlist e : noRepeat) {
                if (e.getsplPlaylist_Id().equalsIgnoreCase(event.getsplPlaylist_Id()))
                    isFound = true;
            }

            if (!isFound) {
                noRepeat.add(event);
            }
        }

        ArrayList<Songs> songsToBeDownloaded = null;

        if (noRepeat.size() > 0) {

            PlaylistManager songsLoader = new PlaylistManager(DownloadService.this, null);
            songsToBeDownloaded = new ArrayList<>();
            for (Playlist playlist : noRepeat) {

                ArrayList<Songs> songs = songsLoader.getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

                if (songs != null && songs.size() > 0) {

                    if (playlist.getIsSeparatinActive() == 0){
                        sort(songs);
                    }

                    for (Songs song: songs){

                        if (!doesArrayContainsSongWithTitleId(song.getTitle_Id(), songsToBeDownloaded)){
                            songsToBeDownloaded.add(song);
                        }
                    }
                }
                else {
                    ArrayList<Songs> UnschdSonglist = songsLoader.getUnschdSongs();
                    if ((UnschdSonglist != null) && (UnschdSonglist.size() > 0)) {
                        songsToBeDownloaded.addAll(UnschdSonglist);
                    }
                }



            }
            songsLoader = null;

            if (songsToBeDownloaded.size() > 0) {
                return songsToBeDownloaded;
            }
        }
        return null;
    }

    private boolean doesArrayContainsSongWithTitleId(String titleId, ArrayList<Songs> songsArrayList){

        for (Songs song : songsArrayList){

            if (song.getTitle_Id().equalsIgnoreCase(titleId)){
                return true;
            }
        }

        return false;
    }

    private ArrayList<Advertisements> getAdvertisementsToBeDownloaded(){
        return new AdvertisementsManager(this).
                getAdvertisementsToBeDownloaded();
    }

    private void startDownloadingSongs() {
        String pathToUsb = "";

        try {

            if (Utilities.isVersionLowerThanLollipop()){

                File[] pathsss = getExternalFilesDirs(null);

                Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();

                if (externalLocations.size() > 1){

                    File usbDrive = pathsss[0];

                    pathToUsb = usbDrive.getAbsolutePath();

                } else {
                    pathToUsb = getApplicationInfo().dataDir;
                }

            } else {

                long bytesAvailable;
                long megAvailable;
                File[] pathsss =  ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
                //  Utilities.showToast(DownloadService.this, " upper ");
//                File[] pathsss = getExternalFilesDirs(getApplicationContext(),null);

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

            }
            String applicationDirectory =pathToUsb;

            String fileURL = songsToBeDownloaded.

                    get(downloadingFileAtIndex).getTitle_Url();

            final String fileName = Utilities.removeSpecialCharacterFromFileName(songsToBeDownloaded.
                    get(downloadingFileAtIndex).getTitle());

            final String filePath = applicationDirectory + File.separator+ songsToBeDownloaded.
                    get(downloadingFileAtIndex).getTitle_Id() + Constants.TAG_FILE_EXTENSION_MP3;
            String filePath1 = gblStorage.getAbsolutePath() + File.separator + songsToBeDownloaded.
                    get(downloadingFileAtIndex).getTitle_Id() + Constants.TAG_FILE_EXTENSION_MP3;
            File file=new File(filePath);
            File fileint=new File(filePath1);
            long filebytes=0000;
            if(file.exists()||fileint.exists())
            {
                if(file.exists())
                {
                    filebytes=file.length();
                }
                if(fileint.exists())
                {
                    filebytes=fileint.length();
                }

                final Songs song = songsToBeDownloaded.get(downloadingFileAtIndex);
                if(filebytes==Long.parseLong(song.getFilesize()))
                {
                    //Utilities.showToast(DownloadService.this, " File found " + songsToBeDownloaded.get(downloadingFileAtIndex).getTitle_Id());
                    song.setIs_Downloaded(1);
                    if (file.exists()) {
                        song.setSongPath(filePath);
                    }
                    if (fileint.exists()) {
                        song.setSongPath(filePath1);
                    }
                    new PlaylistManager(DownloadService.this, null).songDownloaded(song, new PlaylistLoaderListener() {
                        @Override
                        public void startedGettingPlaylist() {

                        }

                        @Override
                        public void finishedGettingPlaylist() {

                        }

                        @Override
                        public void errorInGettingPlaylist() {

                        }

                        @Override
                        public void tokenUpdatedOnServer() {

                        }

                        @Override
                        public void recordSaved(boolean isSaved) {

                            if (isSaved) {
                                // Tell all the listeners that song has been downloaded.

                                boolean shouldPlay = false;

                                if (downloadingFileAtIndex == 0) shouldPlay = true;
                                HomeActivity.getInstance().playfoundsong(shouldPlay,song);

                                // Start the next song in queue.

                                if (songsToBeDownloaded.size() - 1 > downloadingFileAtIndex) {
                                    downloadingFileAtIndex++;
                                    startDownloadingSongs();
                                }
                                else {
                                    PlaylistManager songsLoader = new PlaylistManager(DownloadService.this, null);

                                    ArrayList<Songs> UnschdSonglist = songsLoader.getUnschdSongs();
                                    if((UnschdSonglist!=null) && (UnschdSonglist.size()>0))
                                    {
                                        songsToBeDownloaded.clear();
                                        downloadingFileAtIndex=0;
                                        songsToBeDownloaded.addAll(UnschdSonglist);
                                        startDownloadingSongs();
                                    }
                                    else {
                                        prepareForDownloadingAds();
                                    }

                                }

                            }

                        }
                    });
                }
                else {
                   // Utilities.showToast(DownloadService.this,fileName+"Downloading path-->"+filePath);
                    Ion.with(DownloadService.this)
                            .load(fileURL)
                            .progress(new ProgressCallback() {
                                @Override
                                public void onProgress(long downloaded, long total) {

                                    int percentage = (int) (downloaded * 100.0 / total + 0.5);

                                    if (percentage == 0) {
                                        percentage = 1;
                                    }

                                    Log.e("Song downloaded", percentage + "%");

//                   int progress = (int) (downloaded / total * 100.0);
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
                                        Toast.makeText(DownloadService.this, "Downloading failed for " + fileName, Toast.LENGTH_SHORT).show();
                                    }
                                }, 1000);
                                startDownloadingSongs();
                                return;
                            }

                            if (result != null) {

                                if (result.exists()) {

                                    final Songs song = songsToBeDownloaded.get(downloadingFileAtIndex);
                                    File downloadsize=new File(filePath);
                                    long h=downloadsize.length();
                                    long p=Long.parseLong(song.getFilesize());
                                    if(Long.parseLong(song.getFilesize())==downloadsize.length())
                                    {
                                        song.setIs_Downloaded(1);
                                        song.setSongPath(result.getAbsolutePath());

                                        new PlaylistManager(DownloadService.this, null).songDownloaded(song, new PlaylistLoaderListener() {
                                            @Override
                                            public void startedGettingPlaylist() {

                                            }

                                            @Override
                                            public void finishedGettingPlaylist() {

                                            }

                                            @Override
                                            public void errorInGettingPlaylist() {

                                            }

                                            @Override
                                            public void tokenUpdatedOnServer() {

                                            }

                                            @Override
                                            public void recordSaved(boolean isSaved) {

                                                if (isSaved) {
                                                    // Tell all the listeners that song has been downloaded.

                                                    boolean shouldPlay = false;

                                                    if (downloadingFileAtIndex == 0) shouldPlay = true;

                                                    sendDownloadCompletionUpdate(shouldPlay, song);

                                                    // Start the next song in queue.

                                                    if (songsToBeDownloaded.size() - 1 > downloadingFileAtIndex) {
                                                        downloadingFileAtIndex++;
                                                        startDownloadingSongs();
                                                    }
                                                    else {
                                                        PlaylistManager songsLoader = new PlaylistManager(DownloadService.this, null);

                                                        ArrayList<Songs> UnschdSonglist = songsLoader.getUnschdSongs();
                                                          if((UnschdSonglist!=null) && (UnschdSonglist.size()>0))
                                                          {
                                                              songsToBeDownloaded.clear();
                                                              downloadingFileAtIndex=0;
                                                              songsToBeDownloaded.addAll(UnschdSonglist);
                                                              startDownloadingSongs();
                                                          }
                                                          else {
                                                              prepareForDownloadingAds();
                                                          }

                                                    }

                                                }

                                            }
                                        });
                                    }
                                    else
                                    {
                                        startDownloadingSongs();
                                    }

                                }

                            }
                        }

                    });
                }

            }
            else {
               // Utilities.showToast(DownloadService.this,fileName+"Downloading path-->"+filePath);
                Ion.with(DownloadService.this)
                        .load(fileURL)
                        .progress(new ProgressCallback() {
                            @Override
                            public void onProgress(long downloaded, long total) {

                                int percentage = (int) (downloaded * 100.0 / total + 0.5);

                                if (percentage == 0) {
                                    percentage = 1;
                                }

                                Log.e("Song downloaded", percentage + "%");

//                   int progress = (int) (downloaded / total * 100.0);
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
                                    Toast.makeText(DownloadService.this, "Downloading failed for " + fileName, Toast.LENGTH_SHORT).show();
                                }
                            }, 1000);
                            startDownloadingSongs();
                            return;
                        }

                        if (result != null) {

                            if (result.exists()) {

                                final Songs song = songsToBeDownloaded.get(downloadingFileAtIndex);
                                File downloadsize=new File(filePath);
                                long h=downloadsize.length();
                                long p=Long.parseLong(song.getFilesize());
                                if(Long.parseLong(song.getFilesize())==downloadsize.length())
                                {
                                    song.setIs_Downloaded(1);
                                    song.setSongPath(result.getAbsolutePath());

                                    new PlaylistManager(DownloadService.this, null).songDownloaded(song, new PlaylistLoaderListener() {
                                        @Override
                                        public void startedGettingPlaylist() {

                                        }

                                        @Override
                                        public void finishedGettingPlaylist() {

                                        }

                                        @Override
                                        public void errorInGettingPlaylist() {

                                        }

                                        @Override
                                        public void tokenUpdatedOnServer() {

                                        }

                                        @Override
                                        public void recordSaved(boolean isSaved) {

                                            if (isSaved) {
                                                // Tell all the listeners that song has been downloaded.

                                                boolean shouldPlay = false;

                                                if (downloadingFileAtIndex == 0) shouldPlay = true;

                                                sendDownloadCompletionUpdate(shouldPlay, song);

                                                // Start the next song in queue.

                                                if (songsToBeDownloaded.size() - 1 > downloadingFileAtIndex) {
                                                    downloadingFileAtIndex++;
                                                    startDownloadingSongs();
                                                }
                                                else {
                                                    PlaylistManager songsLoader = new PlaylistManager(DownloadService.this, null);

                                                    ArrayList<Songs> UnschdSonglist = songsLoader.getUnschdSongs();
                                                    if((UnschdSonglist!=null) && (UnschdSonglist.size()>0))
                                                    {
                                                        songsToBeDownloaded.clear();
                                                        downloadingFileAtIndex=0;
                                                        songsToBeDownloaded.addAll(UnschdSonglist);
                                                        startDownloadingSongs();
                                                    }
                                                    else {
                                                        prepareForDownloadingAds();
                                                    }

                                                }

                                            }

                                        }
                                    });
                                }
                                else
                                {
                                    startDownloadingSongs();
                                }

                            }

                        }
                    }

                });
            }
        }catch (Exception e1){
            startDownloadingSongs();
            e1.printStackTrace();
        }
    }



    private void prepareForDownloadingAds(){

        ArrayList<Advertisements> advertisementsArrayList = new AdvertisementsManager(this).
                getAdvertisementsToBeDownloaded();

        final String lt= Integer.toString(advertisementsArrayList.size());
        // Toast.makeText(DownloadService.this, "prepareForDownloadingAds array size => "+ lt, Toast.LENGTH_SHORT).show();



        if (advertisementsArrayList != null && advertisementsArrayList.size() > 0){
            adsToBeDownloaded.addAll(advertisementsArrayList);
            startDownloadingAds();
        } else {
            sendCopyIntimationForSongsCopied(0);

            this.stopSelf();
        }

    }

    private void prepareForDownloadingAdsLocally(){

        ArrayList<Advertisements> advertisementsArrayList = new AdvertisementsManager(this).
                getAdvertisementsToBeDownloaded();


        final String lt= Integer.toString(advertisementsArrayList.size());
        Toast.makeText(DownloadService.this, "prepareForDownloadingAdsLocally array size => "+ lt, Toast.LENGTH_SHORT).show();

        if (advertisementsArrayList != null && advertisementsArrayList.size() > 0){
            adsToBeDownloaded.addAll(advertisementsArrayList);
            COPY_SONG_AT_INDEX = 0;
            downloadAdsLocally();
        } else {
            /*
            1 is being subracted because array space was 1 more than actual number of files.
             */
            sendCopyIntimationForSongsCopied(arrVideoFilesFromSDCard.length -1);
            sendCopyIntimation(0,0,true);
            stopSelf();
        }
    }

    private void downloadAdsLocally(){

        if (adsToBeDownloaded != null){

            Advertisements advertisements = adsToBeDownloaded.get(COPY_SONG_AT_INDEX);

            if (advertisements != null){

                for (File file : arrVideoFilesFromSDCard){

                    String fileNameWithoutExtension = Utilities.removeFileExtension(file.getName(), DownloadService.this);
//                    String decodedFileName = Utilities.decodeFromBase64(fileNameWithoutExtension)
//                            + DownloadService.this.getString(R.string.mp4_file_extension);

                    String fileName =  fileNameWithoutExtension + DownloadService.this.getString(R.string.mp4_file_extension);

                    if (Utilities.removeSpecialCharacterFromFileName(advertisements.getAdvtName()).equals(fileName)){
                        Log.e(TAG,"Matched");
                        startCopyAdFromExternalStorageTask(file, advertisements,fileName);
                        break;
                    }
                }

            }
        }

    }

    private void startDownloadingAds(){

        try {

            if (adsToBeDownloaded.size() > 0){

                String applicationDirectory = getApplicationInfo().dataDir;

                String fileURL = adsToBeDownloaded.
                        get(downloadingAdsAtIndex).getAdvFileUrl();

                final String fileName = Utilities.removeSpecialCharacterFromFileName(adsToBeDownloaded.
                        get(downloadingAdsAtIndex).getAdvtName());

                File advertisementFolder = new File(applicationDirectory + File.separator + Constants.ADVERTISEMENT_FOLDER);

                if (!advertisementFolder.exists()){
                    advertisementFolder.mkdir();
                }


                String filePath = applicationDirectory + File.separator + Constants.ADVERTISEMENT_FOLDER + File.separator + fileName;

                Ion.with(DownloadService.this).load(fileURL)
                        .progress(new ProgressCallback() {
                            @Override
                            public void onProgress(long downloaded, long total) {

                                int percentage = (int)(downloaded * 100.0 / total + 0.5);

                                Log.e("Adv downloaded", percentage + "%");

//                   int progress = (int) (downloaded / total * 100.0);

                            }
                        })
                        .write(new File(filePath))
                        .setCallback(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File result) {

                                if (e != null){
                                    e.printStackTrace();

                                    return;
                                }

                                if (result != null && result.exists()){

                                    final  Advertisements advertisements = adsToBeDownloaded.get(downloadingAdsAtIndex);
                                    advertisements.setStatus_Download(1);
                                    advertisements.setAdvtFilePath(result.getAbsolutePath());
                                    new AdvertisementsManager(DownloadService.this).advertisementDownloaded(advertisements);
                                    sendAdvertisementDownloaded(advertisements);
                                    if (adsToBeDownloaded.size() - 1 > downloadingAdsAtIndex){
                                        downloadingAdsAtIndex++;
                                        startDownloadingAds();
                                    } else {
                                        sendCopyIntimationForSongsCopied(0);
                                        stopSelf();
                                    }
                                }
                            }
                        });
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private final Runnable mTickRunnable = new Runnable() {
        public void run() {
            sendUpdate(0);
            mHandler.postDelayed(mTickRunnable, 1000);
        }
    };

    public void registerListener(DownloadListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(DownloadListener listener) {
        mListeners.remove(listener);
    }

    public void sendUpdate(long value) {
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).onUpdate(value);
        }
    }

    private void sendDownloadCompletionUpdate(boolean shouldPlay, Songs songs) {
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).downloadCompleted(shouldPlay, songs);
        }
    }

    private void sendAdvertisementDownloaded(Advertisements advertisements){
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).advertisementDownloaded(advertisements);
        }
    }

    private void sendOfflineDownloadMessage(){
        for (int i=mListeners.size()-1; i>=0; i--) {
            mListeners.get(i).showOfflineDownloadingAlert();
        }
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }


    public void stoptimertask() {
        //TODO: stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void sort(ArrayList<Songs> songsArrayList){

        try {
            Collections.sort(songsArrayList, new Comparator<Songs>() {
                public int compare(Songs p1, Songs p2) {
                    return Long.valueOf(p1.getSerialNo()).compareTo(p2.getSerialNo());
                }
            });
        } catch (Exception e){

            Log.e("Sort exception","");
            e.printStackTrace();
        }

    }

    public class CopyAdsFromExternalStorage extends AsyncTask<String, String, String> {

        public File storageFile = null;
        public Advertisements ad = null;
        public String copyingFileName = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            int songIndex = COPY_SONG_AT_INDEX + 1;
            //Toast.makeText(DownloadService.this, "ADS DOWNLOAD STARTED", Toast.LENGTH_SHORT).show();
            sendCopyIntimation(songIndex, adsToBeDownloaded.size(), false);
//            txtFileWriter.setText("Copying song " + songIndex + " of " + arrVideoFiles.size());
        }


        @Override
        protected String doInBackground(String... params) {

            String success = "0";
            long total = 0;

            try {

                String username = "bob@google.org";
                String password = "Password1";
                String secretID = "BlahBlahBlah";
                String SALT2 = "deliciously salty";

                byte[] key = (SALT2 + username + password).getBytes("UTF-8");
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16); // use only first 128 bit


                SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
                Cipher decipher = Cipher.getInstance("AES");
                decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

                FileOutputStream out = openFileOutput(copyingFileName, Context.MODE_PRIVATE);
                CipherOutputStream cos = new CipherOutputStream(out,decipher);

                Uri uri = Uri.fromFile(storageFile);
                InputStream in = getContentResolver().openInputStream(uri);
                Cursor cursor = getContentResolver().query(uri,
                        null, null, null, null);

//                cursor.moveToFirst();

//                long lenghtOfFile = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
//                InputStream in = new FileInputStream(storageFile.getAbsolutePath());
//                File fileSize = new File(storageFile.getAbsolutePath());
//
//                long lenghtOfFile = fileSize.length();

                long file_size = storageFile.length();

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    total += len;
                    int progress = (int) ((total * 100) / file_size);
                    publishProgress("" + progress);
//                    out.write(buf, 0, len);
                    cos.write(buf,0,len);
                    cos.flush();
                }
                cos.close();
                in.close();
//                out.close();
                success = "1";
            }catch (Exception e){
                e.printStackTrace();
            }
            return success;
        }

        protected void onProgressUpdate(String... progress) {
            final int intProgress = Integer.parseInt(progress[0]);
            sendUpdate(intProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            circularProgressBar.setProgress(0);

            if (result.equals("1")){

                if (COPY_SONG_AT_INDEX < arrVideoFilesFromSDCard.length - 1){

                    File downloadedFile = getFileStreamPath(this.copyingFileName);

                    ad.setStatus_Download(1);
                    ad.setAdvtFilePath(downloadedFile.getAbsolutePath());

                    new AdvertisementsManager(DownloadService.this).advertisementDownloaded(ad);

                    if (arrVideoFilesFromSDCard.length - 1 > COPY_SONG_AT_INDEX &&
                            COPY_SONG_AT_INDEX < adsToBeDownloaded.size() - 1){

                        COPY_SONG_AT_INDEX++;
                        downloadAdsLocally();
                    } else {
                        //Toast.makeText(DownloadService.this, "ADS DOWNLOAD FINISHED", Toast.LENGTH_SHORT).show();
                        // All songs from external storage have been downloaded
                        sendCopyIntimation(0,0,true);

                        /*
                           1 is being subracted because array space was 1 more than actual number of files.
                        */

                        sendCopyIntimationForSongsCopied(arrVideoFilesFromSDCard.length - 1);
                        stopSelf();
//                                    prepareForDownloadingAds();
                    }
                } else {

//                    txtFileWriter.setText("Copying finished");
//                    Toast.makeText(HomeActivity.this, "All files transferred.", Toast.LENGTH_SHORT).show();

                }
            } else {
                Log.e("Error", "In writing video file...restarting");
                downloadAdsLocally();
            }
        }
    }
}