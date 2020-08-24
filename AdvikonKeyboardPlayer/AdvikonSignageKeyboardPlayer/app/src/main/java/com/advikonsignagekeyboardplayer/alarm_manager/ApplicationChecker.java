package com.advikonsignagekeyboardplayer.alarm_manager;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.advikonsignagekeyboardplayer.activities.HomeActivity;
import com.advikonsignagekeyboardplayer.activities.Splash_Activity;

import java.util.List;

public class ApplicationChecker extends Service {

    private Handler mHandler = null;

    private HandlerThread mHandlerThread = null;

    private static int CHECK_TIME = 300000;

    static final String TAG = ApplicationChecker.class.getSimpleName();

    public ApplicationChecker() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // code to execute when the service is first created
        super.onCreate();
        Log.d(TAG, "Service Started.");
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid)
    {
        mHandler.postDelayed(runnable,CHECK_TIME);

        return START_REDELIVER_INTENT;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

            if (runningAppProcessInfo != null){
                Log.d(TAG,"Currently running activities" + runningAppProcessInfo.size());
                boolean isApplicationRunning = false;
                String appRunningStatus = "App is in state ";

                if(isAppRunning()){
                    Log.d(TAG,"App is in running state = ");
                    isApplicationRunning = true;
                    appRunningStatus += "Running";
                } else {
                    appRunningStatus += "Not Running";
                }

              //  Utilities.showToast(ApplicationChecker.this,appRunningStatus);

                if (!isApplicationRunning){
                    Context ctx = ApplicationChecker.this; // or you can replace **'this'** with your **ActivityName.this**
                    Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.advikonsignagekeyboardplayer");
                    ctx.startActivity(i);
                }
            }

            mHandler.postDelayed(runnable,CHECK_TIME);
        }
    };

    protected Boolean isAppRunning()
    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        /*
        In case of any failure we assume the app is running.
         */
        if (tasks == null){
            return true;
        }

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (Splash_Activity.class.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()) ||
                    HomeActivity.class.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

}

