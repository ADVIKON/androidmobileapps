package com.advikonsignagekeyboardplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.advikonsignagekeyboardplayer.activities.Splash_Activity;

/**
 * Created by patas tech on 10/5/2016.
 */
public class LaunchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
//            Toast.makeText(context, "Launch receiver received", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, Splash_Activity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
