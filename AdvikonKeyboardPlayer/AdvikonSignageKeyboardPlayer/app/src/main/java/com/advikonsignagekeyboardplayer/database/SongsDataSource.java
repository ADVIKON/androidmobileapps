package com.advikonsignagekeyboardplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.advikonsignagekeyboardplayer.models.Songs;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by ParasMobile on 6/21/2016.
 */
public class SongsDataSource {

    public static final String TAG = "SongsDataSource";
    //    / Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_SCHID, MySQLiteHelper.COLUMN_TITLE_ID,
            MySQLiteHelper.COLUMN_IS_DOWNLOADED, MySQLiteHelper.COLUMN_TITLE, MySQLiteHelper.COLUMN_ALBUM_ID,
            MySQLiteHelper.COLUMN_ARTIST_ID, MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_ARTIST_NAME, MySQLiteHelper.COLUMN_ALBUM_NAME,
            MySQLiteHelper.COLUMN_SP_PLAYLIST_ID, MySQLiteHelper.COLUMN_SONG_PATH, MySQLiteHelper.COLUMN_TITLE_URL,
            MySQLiteHelper.COLUMN_SERIAL_NO, MySQLiteHelper.COLUMN_FileSize
    };

    public SongsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Songs createSongs(Songs songs) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SCHID, songs.getSch_id());
        values.put(MySQLiteHelper.COLUMN_TITLE_ID, songs.getTitle_Id());
        values.put(MySQLiteHelper.COLUMN_IS_DOWNLOADED, songs.getIs_Downloaded());
        values.put(MySQLiteHelper.COLUMN_TITLE, songs.getTitle());
        values.put(MySQLiteHelper.COLUMN_TITLE_URL, songs.getTitle_Url());
        values.put(MySQLiteHelper.COLUMN_ALBUM_ID, songs.getAlbum_ID());
        values.put(MySQLiteHelper.COLUMN_ARTIST_ID, songs.getArtist_ID());
        values.put(MySQLiteHelper.COLUMN_TIME, songs.getT_Time());
        values.put(MySQLiteHelper.COLUMN_ARTIST_NAME, songs.getAr_Name());
        values.put(MySQLiteHelper.COLUMN_ALBUM_NAME, songs.getAl_Name());
        values.put(MySQLiteHelper.COLUMN_SP_PLAYLIST_ID, songs.getSpl_PlaylistId());
        values.put(MySQLiteHelper.COLUMN_SONG_PATH, songs.getSongPath());
        values.put(MySQLiteHelper.COLUMN_SERIAL_NO, songs.getSerialNo());
        values.put(MySQLiteHelper.COLUMN_FileSize, songs.getFilesize());

        long insertId = database.insert(MySQLiteHelper.TABLE_SONGS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        Songs newSongs = cursorToSongs(cursor);
        cursor.close();
        return newSongs;
    }




    public void deleteSongs(Songs songs, boolean deleteSourceFile) {

        if (!database.isOpen())
            open();
        long id = songs.get_id();
        database.delete(MySQLiteHelper.TABLE_SONGS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);

        if (deleteSourceFile){
            String songpath = songs.getSongPath();
            File file = new File(songpath);
            if (file.exists()) {
                file.delete();
            }
        }

    }

    /*public void deleteSongsWithPlaylist(String playlistId) {
        database.delete(MySQLiteHelper.TABLE_SONGS, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID
                + " = " + "'" + playlistId + "'", null);
    }  Ranjeet 28 Nov*/

    public ArrayList<Songs> getAllSongss(String id) {
        ArrayList<Songs> songss = new ArrayList<Songs>();


       /* String query = "SELECT  * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " +
                MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " +  1  + " AND " +
                MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " +  "'" + id + "'" + " "+
                "order by cast("+" "+MySQLiteHelper.COLUMN_SERIAL_NO +" as int)";*/

       //Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
               // allColumns, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " + "'" + ID + "'" + " AND " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1, null, null, null, null);*/
       // Cursor cursor = database.rawQuery(query, null);

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID +
                        " = " + "'" + id + "'" + " AND " +
                        MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1 , null, null, null, MySQLiteHelper.COLUMN_SERIAL_NO);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            songss.add(songs);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return songss;
    }

    public ArrayList<Songs> getAllSongssRandom(String id) {
        ArrayList<Songs> songss = new ArrayList<Songs>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1 , null, null, null,"RANDOM()");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            songss.add(songs);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return songss;
    }

    public ArrayList<Songs> getUnschdSongsThoseAreNotDownloaded() {
        ArrayList<Songs> songss = new ArrayList<Songs>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 0, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            songss.add(songs);
            cursor.moveToNext();

        }
        // make sure to close the cursor
        cursor.close();
        return songss;

    }

    public ArrayList<Songs> getAllDownloadedSongs(String Songid) {
        ArrayList<Songs> songss = new ArrayList<Songs>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1
                        + " AND " +
                        MySQLiteHelper.COLUMN_TITLE_ID + " = " + Songid, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            songss.add(songs);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return songss;
    }

    private Songs cursorToSongs(Cursor cursor) {
        Songs songs = new Songs();
        songs.set_id(cursor.getLong(0));
        songs.setSch_id(cursor.getString(1));
        songs.setTitle_Id(cursor.getString(2));
        songs.setIs_Downloaded(cursor.getInt(3));
        songs.setTitle(cursor.getString(4));
        songs.setAlbum_ID(cursor.getString(5));
        songs.setArtist_ID(cursor.getString(6));
        songs.setT_Time(cursor.getString(7));
        songs.setAr_Name(cursor.getString(8));
        songs.setAl_Name(cursor.getString(9));
        songs.setSpl_PlaylistId(cursor.getString(10));
        songs.setSongPath(cursor.getString(11));
        songs.setTitle_Url(cursor.getString(12));
        songs.setSerialNo(cursor.getLong(13));
        songs.setFilesize(cursor.getString(14));
        return songs;
    }





    public boolean checkifSongExist(Songs songs, Context context) {

        boolean isExist = false;

        String query = "SELECT * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " + MySQLiteHelper.COLUMN_TITLE_ID +" = "+ songs.getTitle_Id()
                + " AND " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " + songs.getSpl_PlaylistId();

        Cursor cursor = database.rawQuery(query,null);

        String queryForSimilarTitleDownloaded = "SELECT * FROM " + MySQLiteHelper.TABLE_SONGS +
                " WHERE " + MySQLiteHelper.COLUMN_TITLE_ID +" = "+ songs.getTitle_Id()
                + " AND " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1 +
                " AND " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " != " + songs.getSpl_PlaylistId();

        Cursor cursorToCheckIfSongExistsInAnotherPlaylist = database.rawQuery(queryForSimilarTitleDownloaded, null);

        /*
        Update the existing song values
         */
        if (cursor.getCount() != 0) {
            updateSongsListWithSerialNumber(songs);
        }

        /*
        Check if the song with title id exists in table but not with same playlist
         */
        else if (cursorToCheckIfSongExistsInAnotherPlaylist.moveToFirst() || cursorToCheckIfSongExistsInAnotherPlaylist.getCount() > 0){
            Songs alreadyDownloadedSong = cursorToSongs(cursorToCheckIfSongExistsInAnotherPlaylist);
            createSongWithAlreadyDownloadedTitleID(songs, alreadyDownloadedSong);
        }

        /*
        Create a new song
        */

        else {
            createSongs(songs);
        }

        // make sure to close the cursor
        cursor.close();
        cursorToCheckIfSongExistsInAnotherPlaylist.close();
        return isExist;
    }

        private void createSongWithAlreadyDownloadedTitleID(Songs newSong, Songs downloadedSongWithSameTitleId ){

        try {

            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_SCHID, newSong.getSch_id());
            values.put(MySQLiteHelper.COLUMN_TITLE_ID, newSong.getTitle_Id());
            values.put(MySQLiteHelper.COLUMN_TITLE, newSong.getTitle());
            values.put(MySQLiteHelper.COLUMN_TITLE_URL, newSong.getTitle_Url());
            values.put(MySQLiteHelper.COLUMN_ALBUM_ID, newSong.getAlbum_ID());
            values.put(MySQLiteHelper.COLUMN_ARTIST_ID, newSong.getArtist_ID());
            values.put(MySQLiteHelper.COLUMN_TIME, newSong.getT_Time());
            values.put(MySQLiteHelper.COLUMN_ARTIST_NAME, newSong.getAr_Name());
            values.put(MySQLiteHelper.COLUMN_ALBUM_NAME, newSong.getAl_Name());
            values.put(MySQLiteHelper.COLUMN_SP_PLAYLIST_ID, newSong.getSpl_PlaylistId());
            values.put(MySQLiteHelper.COLUMN_SERIAL_NO, newSong.getSerialNo());

            values.put(MySQLiteHelper.COLUMN_IS_DOWNLOADED, downloadedSongWithSameTitleId.getIs_Downloaded());
            values.put(MySQLiteHelper.COLUMN_SONG_PATH, downloadedSongWithSameTitleId.getSongPath());

            long insertId = database.insert(MySQLiteHelper.TABLE_SONGS, null,
                    values);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateSongsListWithSerialNumber(Songs songs) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SERIAL_NO, songs.getSerialNo());
        values.put(MySQLiteHelper.COLUMN_ARTIST_NAME, songs.getAr_Name());
        values.put(MySQLiteHelper.COLUMN_TITLE, songs.getTitle());

        Log.e(TAG, "Serial number updated for song" + songs.getTitle());

        int success =  database.update(MySQLiteHelper.TABLE_SONGS,values, MySQLiteHelper.COLUMN_TITLE_ID
                + " = " + "'" + songs.getTitle_Id() + "'",null);

        if (success > 0){
            Log.e(TAG,"Value updated");
        } else {
            Log.e(TAG,"Value not updated");
        }

//        database.update(MySQLiteHelper.TABLE_SONGS, values, "_id="+songs.get_id(), null);
        // updating row
//        database.update(MySQLiteHelper.TABLE_SONGS, values, MySQLiteHelper.COLUMN_TITLE_ID + " = ?", new String[]{String.valueOf(songs.getTitle_Id())});

    }

    /*
   * update Playlist
   */
  /*===========================UPDATE QUERY TO UPDATE SONG COLUMNS==========================*/
    public void updateSongsListWithDownloadstatusandPath(Songs songs) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_IS_DOWNLOADED, songs.getIs_Downloaded());
        values.put(MySQLiteHelper.COLUMN_SONG_PATH, songs.getSongPath());
        Log.e(TAG, "Update==================Successfull");
//        database.update(MySQLiteHelper.TABLE_SONGS, values, "_id="+songs.get_id(), null);
        database.update(MySQLiteHelper.TABLE_SONGS, values, "title_id="+songs.getTitle_Id(), null);

        //Find songs with same titleid but different playlist id
        //

        // updating row
//        database.update(MySQLiteHelper.TABLE_SONGS, values, MySQLiteHelper.COLUMN_TITLE_ID + " = ?", new String[]{String.valueOf(songs.getTitle_Id())});

    }


    /***************
     * GEt Songs Those are Downloaded
     ******************/

    public ArrayList<Songs> getSongsThoseAreDownloaded(String ID) {
        ArrayList<Songs> songss = new ArrayList<Songs>();

//        String query = "SELECT  * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " +  1  + " AND " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " +  "'" + ID + "'" + " ORDER BY RANDOM()";
        String query = "SELECT  * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " +
                MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " +  1  + " AND " +
                MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " +  "'" + ID + "'";

       /*Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " + "'" + ID + "'" + " AND " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1, null, null, null, null);*/
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            String s = songs.getSongPath();

//            File dir = new File(Environment.getExternalStorageDirectory() + "/MyClaudAlenka");
//            if(!dir.exists() && !dir.isDirectory()) {
//
//                return songss;
//            }else {
//                songss.add(songs);
//
//            }
            if(!s.isEmpty()){
                songss.add(songs);
            }else{
                long s1 = songs.get_id();
                updateSongsColumnDownloadStatus(String.valueOf(s1));
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return songss;
    }


    // Get all the songs that are downloaded

    public ArrayList<Songs> getAllSongsThatAreDownloaded(String playlistID) {
        ArrayList<Songs> songss = new ArrayList<Songs>();

        String query = "SELECT  * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " +  1  + " AND " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " +  "'" + playlistID + "'";
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            String s = songs.getSongPath();

            File dir = new File(s);
            if(dir.exists()){
                songss.add(songs);
            }else{
                long s1 = songs.get_id();
                updateSongsColumnDownloadStatus(String.valueOf(s1));
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return songss;

    }
    /***************
     * GEt Songs Those are Downloaded
     ******************/

    public ArrayList<Songs> getSongsThoseAreNotDownloaded(String ID) {
        ArrayList<Songs> songss = new ArrayList<Songs>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " + "'" + ID + "'" + " AND " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 0, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            songss.add(songs);
            cursor.moveToNext();

        }
        // make sure to close the cursor
        cursor.close();
        return songss;

    }
    public void updateSongsColumnDownloadStatus(String id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_IS_DOWNLOADED, "0");
        // values.put(MySQLiteHelper.COLUMN_SONG_PATH, songs.getSongPath());
        Log.e(TAG, "Update==================Successfull");
        database.update(MySQLiteHelper.TABLE_SONGS, values, "_id="+id, null);
        // updating row
//        database.update(MySQLiteHelper.TABLE_SONGS, values, MySQLiteHelper.COLUMN_TITLE_ID + " = ?", new String[]{String.valueOf(songs.getTitle_Id())});

    }

    public ArrayList<Songs> getSongListNotAvailableinWebResponse(String[] titleIDArray, String splis) {
        ArrayList<Songs> arraylistSong = new ArrayList<>();
        String query = "SELECT * FROM " + MySQLiteHelper.TABLE_SONGS
                + " WHERE " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + "=" + "'" + splis + "'" + " AND " + MySQLiteHelper.COLUMN_TITLE_ID +" NOT IN (" + makePlaceholders(titleIDArray) + ")";
        Cursor cursor = database.rawQuery(query, titleIDArray);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Songs songs = cursorToSongs(cursor);
            arraylistSong.add(songs);
            cursor.moveToNext();

        }
        // make sure to close the cursor
        cursor.close();
        return arraylistSong;
    }

    private String makePlaceholders(String[] titleIDArray) {
        StringBuilder sb = new StringBuilder(titleIDArray.length * 2 - 1);
        sb.append("?");
        for (int j = 1; j < titleIDArray.length; j++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    public boolean checkifSongsExist1(String titleid) {
        boolean isExist = false ;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_TITLE_ID +" = "+ "'" + titleid + "'", null, null, null, null);
        if (cursor.getCount() > 1){
            isExist = true ;
        }else {
            isExist = false ;

        }

        // make sure to close the cursor
        cursor.close();
        return isExist;
    }

    public ArrayList<Songs> getSongsToBeDeletedWithTitleIds(String[] titleIds){

        if (database == null){
            open();
        }

        ArrayList<Songs> arraylistSong = new ArrayList<>();

        if (titleIds.length > 0){

            String commaSeparatedTitles = "";

            for (int count = 0; count < titleIds.length; count++) {

                String titleId = titleIds[count];

                if (count == titleIds.length - 1) {
                    commaSeparatedTitles += titleId;
                } else {
                    commaSeparatedTitles += titleId + ",";
                }
            }

            String query = "SELECT * FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " +
                    MySQLiteHelper.COLUMN_TITLE_ID + " NOT IN (" + commaSeparatedTitles + ")";

            Cursor cursor = database.rawQuery(query, null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                Songs songs = cursorToSongs(cursor);
                arraylistSong.add(songs);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return arraylistSong;
    }


    public int getCountForTotalSongsDownloaded(){

        try {

            String query = "SELECT COUNT(*) FROM " + MySQLiteHelper.TABLE_SONGS + " WHERE " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1 +
                    " AND " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " IN (SELECT DISTINCT " + MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " FROM " + MySQLiteHelper.TABLE_PLAYLIST + ")";

            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();

            int count = cursor.getInt(0);

            if (count >= 0){
                return count;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        //SELECT count(*) from 'songs' where is_downloaded=1 and sp_playlist_id in(select distinct sp_playlist_id from 'playlist')

        return 0;

    }

}
