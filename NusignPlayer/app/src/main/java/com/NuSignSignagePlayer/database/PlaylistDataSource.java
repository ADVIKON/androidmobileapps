package com.NuSignSignagePlayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.NuSignSignagePlayer.models.Playlist;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



public class PlaylistDataSource {

    Calendar calander;
    SimpleDateFormat simpleDateFormat;
    String time;
    private static long currentTimeInMilli,milliSec,previousDateInMilli,finalTime;
    String formattedDate;

    //    / Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_SCHID, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID,
            MySQLiteHelper.COLUMN_START_TIME,MySQLiteHelper.COLUMN_END_TIME,
            MySQLiteHelper.COLUMN_SP_NAME,MySQLiteHelper.COLUMN_START_TIME_IN_MILI,MySQLiteHelper.COLUMN_END_TIME_IN_MILI,
            MySQLiteHelper.COLUMN_IS_SEPARATION_ACTIVE,MySQLiteHelper.COLUMN_playlistCategory};

    public PlaylistDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert inti Playlist
     */
    public Playlist createPlaylist(Playlist playlist) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SCHID, playlist.getpSc_id());
        values.put(MySQLiteHelper.COLUMN_SP_PLAYLIST_ID, playlist.getsplPlaylist_Id());
        values.put(MySQLiteHelper.COLUMN_START_TIME, playlist.getStart_time());
        values.put(MySQLiteHelper.COLUMN_END_TIME, playlist.getEnd_time());
        values.put(MySQLiteHelper.COLUMN_SP_NAME, playlist.getsplPlaylist_Name());
        values.put(MySQLiteHelper.COLUMN_START_TIME_IN_MILI,playlist.getStart_Time_In_Milli());
        values.put(MySQLiteHelper.COLUMN_END_TIME_IN_MILI,playlist.getEnd_Time_In_Milli());
        values.put(MySQLiteHelper.COLUMN_IS_SEPARATION_ACTIVE,playlist.getIsSeparatinActive());
        values.put(MySQLiteHelper.COLUMN_playlistCategory,playlist.getsplPlaylistCategory());

        long insertId = database.insert(MySQLiteHelper.TABLE_PLAYLIST, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Playlist newPlaylist = cursorToPlaylist(cursor);
        cursor.close();

        return newPlaylist;
    }

    /*
    * Delete Playlist
    */

    public void deletePlaylist(Playlist playlist) {
        long id = playlist.get_id();
        System.out.println("Playlist deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_PLAYLIST, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }
  /*
    * update Playlist
    */

    public void updatePlayList(Playlist playlist) {
        long id = playlist.get_id();
        System.out.println("Playlist updated with id: " + id);
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SP_PLAYLIST_ID, playlist.getsplPlaylist_Id());
        values.put(MySQLiteHelper.COLUMN_START_TIME, playlist.getStart_time());
        values.put(MySQLiteHelper.COLUMN_END_TIME, playlist.getEnd_time());
        values.put(MySQLiteHelper.COLUMN_SP_NAME, playlist.getsplPlaylist_Name());
        values.put(MySQLiteHelper.COLUMN_START_TIME_IN_MILI,playlist.getStart_Time_In_Milli());
        values.put(MySQLiteHelper.COLUMN_END_TIME_IN_MILI,playlist.getEnd_Time_In_Milli());
        values.put(MySQLiteHelper.COLUMN_IS_SEPARATION_ACTIVE,playlist.getIsSeparatinActive());
        values.put(MySQLiteHelper.COLUMN_playlistCategory,playlist.getsplPlaylistCategory());
        database.update(MySQLiteHelper.TABLE_PLAYLIST,values,MySQLiteHelper.COLUMN_SCHID
                + " = " + "'" + playlist.getpSc_id() + "'",null);
    }

    /*
    * get all playlist
    * */

    public ArrayList<Playlist> getAllPlaylists() {

        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        time = simpleDateFormat.format(calander.getTime());

        String array[]=time.split("\\s+");
        time=array[1]+" "+array[2];
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);
//        finalTime=currentTimeInMilli+previousDateInMilli;

//        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ finalTime +" between "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;
        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI +" <= "+ currentTimeInMilli + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI+" > "+ currentTimeInMilli;
//
//        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
//                allColumns, null, null, null, null, null);
        Cursor cursor=database.rawQuery(query,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }

    public ArrayList<Playlist> getPlaylistsForCurrentAndComingTime() {

        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        time = simpleDateFormat.format(calander.getTime());

        String array[]=time.split("\\s+");
        time=array[1]+" "+array[2];
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);
//        finalTime=currentTimeInMilli+previousDateInMilli;

//        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ finalTime +" between "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;
        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI+" >= "+ currentTimeInMilli;
//
//        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
//                allColumns, null, null, null, null, null);

        Cursor cursor=database.rawQuery(query,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }

    // Getting all Remaining Playlist
    public ArrayList<Playlist> getAllPlaylistsInPlayingOrder() {
        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");

        time = simpleDateFormat.format(calander.getTime());

        String array[]=time.split("\\s+");

        time=array[1]+" "+array[2];

        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);
//        finalTime=currentTimeInMilli+previousDateInMilli;

//        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ finalTime +" between "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;
        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " ORDER BY " + MySQLiteHelper.COLUMN_START_TIME ;
//
//        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
//                allColumns, null, null, null, null, null);

        Cursor cursor=database.rawQuery(query,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }

    // Getting all Remaining Playlist
    public ArrayList<Playlist> getRemainingAllPlaylists() {
        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        time = simpleDateFormat.format(calander.getTime());
        String array[]=time.split("\\s+");
        time=array[1]+" "+array[2];
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

//
//        Cursor cursor1 = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_PLAYLIST + " WHERE " + time + " BETWEEN " + MySQLiteHelper.COLUMN_START_TIME + " AND " + MySQLiteHelper.COLUMN_END_TIME ,null);
//
//        if(cursor1!=null) {
//
//            cursor1.moveToFirst();
//            Playlist playlist = cursorToPlaylist(cursor1);
//            playlists.add(playlist);
//        }
        // Here we change the format of date & Time
//        String time1="1/1/1900";
        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);
//        finalTime=currentTimeInMilli+previousDateInMilli;

//        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ finalTime +" between "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;
        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI +" >= "+ currentTimeInMilli + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI+" >= "+ currentTimeInMilli;
//
//        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
//                allColumns, null, null, null, null, null);

        Cursor cursor=database.rawQuery(query,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }

    public ArrayList<Playlist> getPendingPastPlaylist() {
        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        time = simpleDateFormat.format(calander.getTime());
        String array[]=time.split("\\s+");
        time=array[1]+" "+array[2];
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

//
//        Cursor cursor1 = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_PLAYLIST + " WHERE " + time + " BETWEEN " + MySQLiteHelper.COLUMN_START_TIME + " AND " + MySQLiteHelper.COLUMN_END_TIME ,null);
//
//        if(cursor1!=null) {
//
//            cursor1.moveToFirst();
//            Playlist playlist = cursorToPlaylist(cursor1);
//            playlists.add(playlist);
//        }
        // Here we change the format of date & Time
//        String time1="1/1/1900";
        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);
//        finalTime=currentTimeInMilli+previousDateInMilli;

//        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ finalTime +" between "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;
        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ MySQLiteHelper.COLUMN_START_TIME_IN_MILI +" < "+ currentTimeInMilli + " and "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI+" <" +
                " "+ currentTimeInMilli;
//
//        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
//                allColumns, null, null, null, null, null);

        Cursor cursor=database.rawQuery(query,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }



    private String changeDateFormat(String starttime) {
        DateFormat readFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        DateFormat writeFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy");
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

    private long getTimeInMilliSec(String starttime) {


        SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
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

    public boolean checkifPlaylistExist(Playlist playlist) {

        boolean isExist = false ;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
                allColumns, MySQLiteHelper.COLUMN_SCHID +" = "+ "'" + playlist.getpSc_id() + "'", null, null, null, null);
        if (cursor.getCount() > 0){
            isExist = true ;
            updatePlayList(playlist);
        }else {
            isExist = false ;
            createPlaylist(playlist);
        }

        // make sure to close the cursor
        cursor.close();
        return isExist;
    }


    public boolean checkifPlaylistExist(String playlistId) {
        boolean isExist = false ;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLIST,
                allColumns, MySQLiteHelper.COLUMN_SCHID +" = "+ "'" + playlistId + "'", null, null, null, null);
        if (cursor.getCount() > 1){
            isExist = true ;
        }else {
            isExist = false ;

        }

        // make sure to close the cursor
        cursor.close();
        return isExist;
    }

    private Playlist cursorToPlaylist(Cursor cursor) {
        Playlist playlist = new Playlist();
        playlist.set_id(cursor.getLong(0));
        playlist.setpSc_id(cursor.getString(1));
        playlist.setsplPlaylist_Id(cursor.getString(2));
        playlist.setStart_time(cursor.getString(3));
        playlist.setEnd_time(cursor.getString(4));
        playlist.setsplPlaylist_Name(cursor.getString(5));
        playlist.setStart_Time_In_Milli(cursor.getLong(6));
        playlist.setEnd_Time_In_Milli(cursor.getLong(7));
        playlist.setIsSeparatinActive(cursor.getLong(8));
        playlist.setPlaylistCategory(cursor.getString(9));
        return playlist;
    }

    public ArrayList<Playlist> getListNotAvailableinWebResponse(String[] schIdArray){
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        String query = "SELECT * FROM " + MySQLiteHelper.TABLE_PLAYLIST
                + " WHERE "+ MySQLiteHelper.COLUMN_SCHID +" NOT IN (" + makePlaceholders(schIdArray) + ")";
        Cursor cursor = database.rawQuery(query, schIdArray);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;
    }

    private String makePlaceholders(String[] schIdArray){
        StringBuilder sb = new StringBuilder(schIdArray.length * 2 - 1);
        sb.append("?");
        for (int j = 1; j < schIdArray.length; j++) {
            sb.append(",?");
        }
        return sb.toString();
    }


    public ArrayList<Playlist> getPlaylistGoneTime(){
        // Check Device Current Time In 12 Hours Format
        calander = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
        time = simpleDateFormat.format(calander.getTime());

        String array[]=time.split("\\s+");
        time=array[1]+" "+array[2];
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        playlists.clear();
        String currentTime=changeDateFormat("1/1/1900"+" "+ time);

        // Here change the Date & Time in Milliseconds
        currentTimeInMilli=getTimeInMilliSec(currentTime);

        String query= "select * from " + MySQLiteHelper.TABLE_PLAYLIST + " where "+ currentTimeInMilli+" > "+ MySQLiteHelper.COLUMN_END_TIME_IN_MILI;

        Cursor cursor=database.rawQuery(query,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = cursorToPlaylist(cursor);

//            String starttime = playlist.getStart_time();
//            String endTime=playlist.getEnd_time();

//            String query= "select * from"+ MySQLiteHelper.TABLE_PLAYLIST + "where"+ time +"between"+ starttime+ "and"+ endTime;
            playlists.add(playlist);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playlists;

    }
}