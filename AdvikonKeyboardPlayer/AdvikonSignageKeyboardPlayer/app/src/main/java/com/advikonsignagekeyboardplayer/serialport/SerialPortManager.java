package com.advikonsignagekeyboardplayer.serialport;


import android.util.Log;
import android.widget.Toast;

import com.advikonsignagekeyboardplayer.activities.HomeActivity;
import com.advikonsignagekeyboardplayer.eventbus.MessageEvent;
import com.advikonsignagekeyboardplayer.interfaces.SerialPortManagerListener;
import com.advikonsignagekeyboardplayer.models.Announcement;
import com.advikonsignagekeyboardplayer.utils.AlenkaMediaPreferences;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;
import com.advikonsignagekeyboardplayer.utils.Utilities;
import com.lztek.toolkit.SerialPort;
import com.microsoft.appcenter.analytics.Analytics;
import com.advikonsignagekeyboardplayer.mediamanager.AnnouncementLogsManager;
import com.advikonsignagekeyboardplayer.mediamanager.AnnouncementManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class SerialPortManager extends Thread {

    private static final String PATH = "/dev/ttyS2";
    
    private static final Integer BAUDRATE = 38400;

    private volatile boolean mThreadRun = false;

    private volatile SerialPort mSerialPort;

    private HomeActivity mActivity;

    private Integer currentshotsCount = -1;

    public SerialPortManagerListener serialPortManagerListener;

    public static Boolean IS_ANNOUNCEMENT_DISPLAYING = false;

    private AnnouncementLogsManager announcementLogsManager;

    private Boolean isDebugMode = false;

    private Integer totalShotCount = 0;

    private ArrayList<Integer> percentages = new ArrayList<>();

    private Boolean settingsLocked = false;

    public SerialPortManager(HomeActivity homeActivity) {
        mActivity = homeActivity;
        serialPortManagerListener = homeActivity;
        announcementLogsManager = new AnnouncementLogsManager(homeActivity);

        loadShotsCountAndPercentages();
    }

    private void loadShotsCountAndPercentages(){
        settingsLocked = true;
        isDebugMode = SharedPreferenceUtil.getBooleanPreference(this.mActivity, AlenkaMediaPreferences.isDemoToken,false);
        totalShotCount = SharedPreferenceUtil.getIntegerPreference(this.mActivity, AlenkaMediaPreferences.totalShotsCount, -1);
        percentages = Utilities.getTankPercentages(this.mActivity);
        settingsLocked = false;
    }

    public AnnouncementLogsManager getAnnouncementLogsManager() {
        return announcementLogsManager;
    }

    public void reloadAnnouncements(){

        if (announcementList != null){
            announcementList.clear();
        }
        announcementList = new AnnouncementManager(this.mActivity).getAnnouncementsThatAreDownloaded();
    }

    public void startConnection() {
        onBtOpen();
    }

    public void closeConnection(){
        onBtClose();
    }

    private ArrayList<Announcement> announcementList;

    private Integer lastPlayedAnnouncement = -1;

    @Override
    public void run() {

        while (mThreadRun) {
            try {
                final byte[] data = null != mSerialPort? read() : null;
                if (null != data && data.length > 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = new String(data);
                            readShotCount(message);
                        }
                    });
                } else {
                    Thread.sleep(50);
                }
            } catch (Exception e) {
            }
        }
    }

    public synchronized void release() {
        if (mThreadRun) {
            mThreadRun = false;
            try {
                this.join(2000);
            } catch (Exception e) {
            }
        }
        if (null != mSerialPort) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void fakeStartConnection(){
        if (!mThreadRun) {
            mThreadRun = true;
            this.start();
        }
    }

    private void onBtOpen() {

        EventBus.getDefault().register(this);

        if (announcementList == null) {
            announcementList = new AnnouncementManager(this.mActivity).getAnnouncementsThatAreDownloaded();
        }

        if (null != mSerialPort) {
            return;
        }

        String message = null;
        try {

            if (null == (mSerialPort=mActivity.mLztek.openSerialPort(PATH, BAUDRATE, 8, 0, 1, 0))) {
                message = "Failed to open connection";
                Toast.makeText(this.mActivity,message,LENGTH_SHORT);
                return;
            }
        } catch (Exception e) {
            message = "Failed to open connection";
            Toast.makeText(this.mActivity,message,LENGTH_SHORT);
            e.printStackTrace();
            return;
        }

        message = "Connection opened successfully.";

        Toast.makeText(this.mActivity,message,LENGTH_SHORT);
        if (!mThreadRun) {
            mThreadRun = true;
            this.start();
        }
    }

    private void checkForTankFullMessage(){

        if (settingsLocked){
            return;
        }

        if (totalShotCount != null && totalShotCount >= 0 && percentages != null && !percentages.isEmpty() ) {

            for (int i = 0; i < percentages.size(); i++) {

                int percent = percentages.get(i);

                int percentageValue = (int) (totalShotCount * (percent/100f));

                if (currentshotsCount == percentageValue) {

                    Analytics.trackEvent("Tank warning message at: " + percentageValue);
                    if (serialPortManagerListener != null){
                        serialPortManagerListener.tankEmpty(percent);
                    }
                    break;
                }
            }
        }
    }

    private void onBtClose() {

        EventBus.getDefault().unregister(this);

        if (null == mSerialPort) {
            return;
        }
        mSerialPort.close();

        mSerialPort = null;

        String message = "Connection closed successfully.";
        Toast.makeText(this.mActivity,message,LENGTH_SHORT);
    }

    public void readShotCount(String message) {

        String countString = message.replaceAll("[\\D]", "");

        if (countString == null || countString == "") {
            return;
        }
        Log.e("SHOT", message);

        Integer newCount = Integer.parseInt(countString);

        if (newCount == null) {
            return;
        }

        if (newCount >= currentshotsCount) {
            getAnnouncementToBePlayed(message);
        }

        currentshotsCount = newCount;

        checkForTankFullMessage();

        if (currentshotsCount == 0) {
            if (serialPortManagerListener != null){
                serialPortManagerListener.tankInOut();
            }
        }

        if (isDebugMode) {
            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
        }
    }

    private void getAnnouncementToBePlayed(String message) {

        if (SerialPortManager.IS_ANNOUNCEMENT_DISPLAYING) {
            //Announcement is already playing
            return;
        }

        if (announcementList == null || announcementList.isEmpty()) {
            return;
        }

        //If there is no previous played advertisement, then play first one.
        if (lastPlayedAnnouncement < 0) {
            lastPlayedAnnouncement = 0;
        } else {

            if (announcementList.size() - 1 > lastPlayedAnnouncement) {
                lastPlayedAnnouncement++;
            } else {
                lastPlayedAnnouncement = 0;
            }
        }

        Announcement announcement = announcementList.get(lastPlayedAnnouncement);

        if (announcement != null && serialPortManagerListener != null) {
            insertLog(currentshotsCount, message, announcement.getAncID());
            serialPortManagerListener.playAnnouncement(announcement);
            Analytics.trackEvent("Played announcement: " + announcement.getAncID());
        }

    }


    private byte[] read() {

        if (null == mSerialPort) {
            return null;
        }

        InputStream input = null;
        try {
            input = mSerialPort.getInputStream();

            byte[] buffer = new byte[4096];

            int length;

            int len = input.read(buffer);
            return len > 0? java.util.Arrays.copyOfRange(buffer, 0, len) : null;
        } catch (Exception e) {
            Log.d("#ERROR#", "[COM]Read Faild: " + e.getMessage(), e);
            return null;
        }
    }

    private void insertLog(int count, String command, String announcementId){
        announcementLogsManager.insertSongPlayedStatus(count, command, announcementId);
    }

    private static byte[] hexBytes(String hexString) {
        int length;
        byte h;
        byte l;
        byte[] byteArray;

        length = null != hexString? hexString.length() : 0;
        length = (length - (length%2))/2;
        if (length < 1) {
            return null;
        }

        byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            h = (byte)hexString.charAt(i*2);
            l = (byte)hexString.charAt(i*2 + 1);

            l = (byte)('0' <= l && l <= '9'? l - '0' :
                    'A' <= l && l <= 'F'? (l - 'A') + 10 :
                            'a' <= l && l <= 'f'? (l - 'a') + 10 : 0);
            h = (byte)('0' <= h && h <= '9'? h - '0' :
                    'A' <= h && h <= 'F'? (h - 'A') + 10 :
                            'a' <= h && h <= 'f'? (h - 'a') + 10 : 3);
            byteArray[i] = (byte)(0x0FF & ((h << 4) | l));
        }
        return byteArray;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        if (event != null && event.downloadAnnouncement != null){
            announcementList.add(event.downloadAnnouncement);
        } else if (event.reloadSettings != null && event.reloadSettings){
            Log.e("TAG","Reloading settings.");
            loadShotsCountAndPercentages();
        }
    };
}
