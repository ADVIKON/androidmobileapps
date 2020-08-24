package com.advikonsignagekeyboardplayer.mediamanager;

import android.content.Context;

import com.advikonsignagekeyboardplayer.database.AnnouncementDataSource;
import com.advikonsignagekeyboardplayer.models.Announcement;

import java.sql.SQLException;
import java.util.ArrayList;

public class AnnouncementManager {

    private Context context;

    private AnnouncementDataSource announcementDataSource = null;

    public AnnouncementManager(Context context) {

        this.context = context;

        this.announcementDataSource = new AnnouncementDataSource(this.context);
    }
    public ArrayList<Announcement> getAnnouncementsToBeDownloaded(){

        try {
            announcementDataSource.open();
            return announcementDataSource.getAncThoseAreNotDownloaded();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            announcementDataSource.close();
        }
        return null;
    }

    public ArrayList<Announcement> getAnnouncementsThatAreDownloaded(){

        try {
            announcementDataSource.open();
            return announcementDataSource.getDownloadedAnnouncements();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            announcementDataSource.close();
        }
        return null;
    }

    public void announcementDownloaded(Announcement announcement){
        try {

            announcementDataSource.open();
            announcementDataSource.UpdateDownloadStatusAndPath(announcement);

        }catch (Exception e){
            e.printStackTrace();
        }

        finally {
            announcementDataSource.close();
        }
    }
}
