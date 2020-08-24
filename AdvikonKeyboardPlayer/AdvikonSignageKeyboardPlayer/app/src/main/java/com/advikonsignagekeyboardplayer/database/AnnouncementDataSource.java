package com.advikonsignagekeyboardplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.advikonsignagekeyboardplayer.models.Announcement;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

public class AnnouncementDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_ANC_SRNO,
            MySQLiteHelper.COLUMN_ANC_FILE_URL,
            MySQLiteHelper.COLUMN_ANC_ID ,
            MySQLiteHelper.COLUMN_ANC_FILE_PATH,
            MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS
    };

    public AnnouncementDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert inti Announcement
     */
    public Announcement createAnnouncement(Announcement announcement) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ANC_FILE_URL, announcement.getAncFileUrl());
        values.put(MySQLiteHelper.COLUMN_ANC_ID, announcement.getAncID());
        values.put(MySQLiteHelper.COLUMN_ANC_FILE_PATH, announcement.getAncFilePath());
        values.put(MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS, announcement.getStatus_Download());
        values.put(MySQLiteHelper.COLUMN_ANC_SRNO, announcement.getSerialNo());

        long insertId = database.insert(MySQLiteHelper.TABLE_ANNOUNCEMENTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Announcement announcement1 = cursorToAnnouncement(cursor);
        cursor.close();

        return announcement1;
    }

    private Announcement cursorToAnnouncement(Cursor cursor) {

        Integer serialNumber = cursor.getInt(1);
        String announcementUrl = cursor.getString(2);
        String announcementId = cursor.getString(3);
        String path = cursor.getString(4);
        Integer downloadStatus = cursor.getInt(5);

        Announcement announcement = new Announcement();
        announcement.setSerialNo(serialNumber);
        announcement.setAncID(announcementId);
        announcement.setAncFileUrl(announcementUrl);
        announcement.setAncFilePath(path);
        announcement.setStatus_Download(downloadStatus);

        return announcement;
    }

    // Here we Check the Announcement is Already Exist or not

    public void checkifExistAnc(Announcement announcement) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENTS, allColumns,
                MySQLiteHelper.COLUMN_ANC_ID + "=" + "'" + announcement.getAncID() + "'", null, null, null, null);
        if (cursor.getCount() == 0) {
            createAnnouncement(announcement);
        } else {
            UpdateAncList(announcement);
        }
        cursor.close();
    }


    private void UpdateAncList(Announcement announcement) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ANC_FILE_URL, announcement.getAncFileUrl());
        values.put(MySQLiteHelper.COLUMN_ANC_ID, announcement.getAncID());
        values.put(MySQLiteHelper.COLUMN_ANC_SRNO, announcement.getSerialNo());

        database.update(MySQLiteHelper.TABLE_ANNOUNCEMENTS,values, MySQLiteHelper.COLUMN_ANC_ID + "=" + "'" + announcement.getAncID() + "'",null);
    }

    public ArrayList<Announcement> getAncThoseAreNotDownloaded(){
        ArrayList<Announcement> arrayList = new ArrayList<Announcement>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENTS,allColumns, MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS + "=" + 0
                + " OR " +
                MySQLiteHelper.COLUMN_ANC_FILE_PATH + " IS " + "NULL",null,null,null, MySQLiteHelper.COLUMN_ANC_SRNO);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            Announcement announcement = cursorToAnnouncement(cursor);
            arrayList.add(announcement);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    public  void UpdateDownloadStatusAndPath(Announcement announcement){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS,announcement.getStatus_Download());
        values.put(MySQLiteHelper.COLUMN_ANC_FILE_PATH,announcement.getAncFilePath());

        try {
            long status = database.update(MySQLiteHelper.TABLE_ANNOUNCEMENTS,values, MySQLiteHelper.COLUMN_ANC_ID + "=" + "'" + announcement.getAncID() + "'",null);
            Log.d("sd",status + "");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Announcement> getAllAnnouncements(){

        ArrayList<Announcement> announcementArrayList = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENTS,
                allColumns, MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS + " = " + 1, null, null,null,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Announcement announcement = cursorToAnnouncement(cursor);
            String songPath = announcement.getAncFilePath();
            if(songPath!=null) {
                File file = new File(songPath);
                if (file.exists()) {
                    announcementArrayList.add(announcement);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return announcementArrayList;

    }

    public ArrayList<Announcement> getDownloadedAnnouncements() {
        {
            ArrayList<Announcement> arrayList = new ArrayList<Announcement>();
            Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENTS,allColumns, MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS + "=" + 1,null,null,null, MySQLiteHelper.COLUMN_ANC_SRNO);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Announcement announcement = cursorToAnnouncement(cursor);

                if (announcement.getAncFilePath() != null && announcement.getAncFilePath() != ""){
                    arrayList.add(announcement);
                }
                cursor.moveToNext();
            }
            cursor.close();
            return arrayList;
        }
    }

    public void deleteAncIfNotInServer(){
        database.delete(MySQLiteHelper.TABLE_ANNOUNCEMENTS,null,null);
    }

    public void deleteAnnouncements(Announcement announcement, boolean deleteSourceFile) {
        try {
            if (!database.isOpen())
                open();
            String id = announcement.getAncID();
            database.delete(MySQLiteHelper.TABLE_ANNOUNCEMENTS, MySQLiteHelper.COLUMN_ANC_ID
                    + " = " + id, null);

            if (deleteSourceFile) {
                String songpath = announcement.getAncFilePath();
                if (songpath != null) {
                    File file = new File(songpath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

        }catch (Exception e)
        {
            e.getCause();
        }

    }

    public ArrayList<Announcement> getListNotAvailableinWebResponse(String[] schIdArray){
        ArrayList<Announcement> advt = new ArrayList<Announcement>();
        String query = "SELECT * FROM " + MySQLiteHelper.TABLE_ANNOUNCEMENTS
                + " WHERE "+ MySQLiteHelper.COLUMN_ANC_ID +" NOT IN (" + makePlaceholders(schIdArray) + ")";
        Cursor cursor = database.rawQuery(query, schIdArray);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Announcement ads = cursorToAnnouncement(cursor);
            advt.add(ads);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return advt;
    }

    private String makePlaceholders(String[] schIdArray){
        StringBuilder sb = new StringBuilder(schIdArray.length * 2 - 1);
        sb.append("?");
        for (int j = 1; j < schIdArray.length; j++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    public void deleteAdvUnUsed(){
        database.delete(MySQLiteHelper.TABLE_ADVERTISEMENT,null,null);
    }


    //TODO: update the status download or not
    public void updateAdvColumnDownloadStatus(String id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SET_DOWNLOAD_STATUS, "0");
        // values.put(MySQLiteHelper.COLUMN_SONG_PATH, songs.getSongPath());
        database.update(MySQLiteHelper.TABLE_ADVERTISEMENT, values, "_id="+id, null);
        // updating row
//        database.update(MySQLiteHelper.TABLE_SONGS, values, MySQLiteHelper.COLUMN_TITLE_ID + " = ?", new String[]{String.valueOf(songs.getTitle_Id())});

    }
}

