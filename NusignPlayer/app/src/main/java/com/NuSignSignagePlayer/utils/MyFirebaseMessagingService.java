package com.NuSignSignagePlayer.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.NuSignSignagePlayer.activities.HomeActivity;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    //  HomeActivity hm=new HomeActivity();
    private static final String TAG = "FCM Service";
    private Context context;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        final String body = remoteMessage.getNotification().getBody();
        //Toast.makeText(MyFirebaseMessagingService.this," Body => " + body,Toast.LENGTH_LONG).show();

       // RemoteMessage.Notification notification = new NotificationCompat.bu

        /* Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String str1 = body;
                Toast.makeText(MyFirebaseMessagingService.this," body "+ str1,Toast.LENGTH_LONG).show();
            }
        });*/

        if (body != null) {
            try {
                JSONObject jsonObj = new JSONObject(body);
                String id = jsonObj.getString("id");
                String Url=jsonObj.getString("url");
                String AlbumId=jsonObj.getString("albumid");
                String titlename=jsonObj.getString("title");
                String ArtistId=jsonObj.getString("artistid");
                String Artistname=jsonObj.getString("artistname");
                String type = jsonObj.getString("type");
                if (type.equals("Song")) {
                    //Toast.makeText(MyFirebaseMessagingService.this,"Hit api",Toast.LENGTH_LONG).show();
                   HomeActivity.getInstance().playsongfromweb(id,Url,AlbumId,ArtistId,titlename,Artistname);
                }

                if (type.equals("Playlist")) {
                    HomeActivity.getInstance().playplaylistfromwebnow(id);
                }

                if (type.equals("Ads")) {
                    HomeActivity.getInstance().playadvnow(id);
                }

            } catch (Exception e) {
                e.getCause();
            }

        }
        MyNotificationManager.getInstance(this);

    }
}
