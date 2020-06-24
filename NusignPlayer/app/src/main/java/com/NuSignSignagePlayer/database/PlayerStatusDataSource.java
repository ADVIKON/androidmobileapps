package com.NuSignSignagePlayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.NuSignSignagePlayer.models.PlayerStatus;

import java.sql.SQLException;
import java.util.ArrayList;



/**
 * Created by patas tech on 9/19/2016.
 */
public class PlayerStatusDataSource {

    //    / Database fields
    public SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_LOGIN_DATE, MySQLiteHelper.COLUMN_LOGIN_TIME,
            MySQLiteHelper.COLUMN_LOGOUT_DATE,MySQLiteHelper.COLUMN_LOGOUT_TIME,
            MySQLiteHelper.COLUMN_ARTIST_ID_SONG,MySQLiteHelper.COLUMN_PLAYED_DATE_TIME_SONG,MySQLiteHelper.COLUMN_TITLE_ID_SONG,
            MySQLiteHelper.COLUMN_SPL_PLAYLIST_ID_SONG,MySQLiteHelper.COLUMN_HEARTBEAT_DATETIME,MySQLiteHelper.COLUMN_ADVERTISEMENT_ID_STATUS,
            MySQLiteHelper.COLUMN_ADVERTISEMENT_PLAYED_DATE,MySQLiteHelper.COLUMN_ADVERTISEMENT_PLAYED_TIME,MySQLiteHelper.COLUMN_PRAYER_PLAYED_DATE,
            MySQLiteHelper.COLUMN_PRAYER_PLAYED_TIME,MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE};

    public PlayerStatusDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {

        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public PlayerStatus createPlayerStatus(PlayerStatus playerStatus) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_LOGIN_DATE, playerStatus.getLoginDate());
        values.put(MySQLiteHelper.COLUMN_LOGIN_TIME, playerStatus.getLoginTime());
        values.put(MySQLiteHelper.COLUMN_LOGOUT_DATE, playerStatus.getLogoutDate());
        values.put(MySQLiteHelper.COLUMN_LOGOUT_TIME, playerStatus.getLogoutTime());
        values.put(MySQLiteHelper.COLUMN_ARTIST_ID_SONG, playerStatus.getArtistIdStatusSong());
        values.put(MySQLiteHelper.COLUMN_PLAYED_DATE_TIME_SONG, playerStatus.getPlayerDateTimeSong());
        values.put(MySQLiteHelper.COLUMN_TITLE_ID_SONG, playerStatus.getTitleIdSong());
        values.put(MySQLiteHelper.COLUMN_SPL_PLAYLIST_ID_SONG, playerStatus.getSplPlaylistIdSong());
        values.put(MySQLiteHelper.COLUMN_HEARTBEAT_DATETIME, playerStatus.getHeartbeatDateTimeStatus());
        values.put(MySQLiteHelper.COLUMN_ADVERTISEMENT_ID_STATUS, playerStatus.getAdvIdStatus());
        values.put(MySQLiteHelper.COLUMN_ADVERTISEMENT_PLAYED_DATE, playerStatus.getAdvPlayedDate());
        values.put(MySQLiteHelper.COLUMN_ADVERTISEMENT_PLAYED_TIME, playerStatus.getAdvPlayedTime());
        values.put(MySQLiteHelper.COLUMN_PRAYER_PLAYED_DATE, playerStatus.getPrayerPlayedDate());
        values.put(MySQLiteHelper.COLUMN_PRAYER_PLAYED_TIME, playerStatus.getPrayerPlayedTime());
        values.put(MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE, playerStatus.getPlayerStatusAll());

        long insertId = database.insert(MySQLiteHelper.TABLE_PLAYER_STATUS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYER_STATUS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        PlayerStatus newPlayerStatus = cursorToPlayerStatus(cursor);
        cursor.close();
        return newPlayerStatus;
    }

    private PlayerStatus cursorToPlayerStatus(Cursor cursor) {
        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setId(cursor.getLong(0));
        playerStatus.setLoginDate(cursor.getString(1));
        playerStatus.setLoginTime(cursor.getString(2));
        playerStatus.setLogoutDate(cursor.getString(3));
        playerStatus.setLogoutTime(cursor.getString(4));
        playerStatus.setArtistIdStatusSong(cursor.getString(5));
        playerStatus.setPlayerDateTimeSong(cursor.getString(6));
        playerStatus.setTitleIdSong(cursor.getString(7));
        playerStatus.setSplPlaylistIdSong(cursor.getString(8));
        playerStatus.setHeartbeatDateTimeStatus(cursor.getString(9));
        playerStatus.setAdvIdStatus(cursor.getString(10));
        playerStatus.setAdvPlayedDate(cursor.getString(11));
        playerStatus.setAdvPlayedTime(cursor.getString(12));
        playerStatus.setPrayerPlayedDate(cursor.getString(13));
        playerStatus.setPrayerPlayedTime(cursor.getString(14));
        playerStatus.setPlayerStatusAll(cursor.getString(15));
        return playerStatus;
    }

    public ArrayList<PlayerStatus> getPlayedSongs(String type){

        ArrayList<PlayerStatus> playerStatus = new ArrayList<PlayerStatus>();

        /*Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYER_STATUS,
                allColumns1,MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE + " = " + "song", null, null, null, null);
*/
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYER_STATUS,allColumns
                , MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE + " = " + "'" + type + "'",null,null,null,MySQLiteHelper.COLUMN_ID + " DESC ","50");

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PlayerStatus status = cursorToPlayerStatus(cursor);
            playerStatus.add(status);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playerStatus;
    }

    public ArrayList<PlayerStatus> getPlayedSongsNew(String type){

        ArrayList<PlayerStatus> playerStatus = new ArrayList<PlayerStatus>();

        /*Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYER_STATUS,
                allColumns1,MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE + " = " + "song", null, null, null, null);
*/
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYER_STATUS,allColumns
                , MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE + " = " + "'" + type + "'",null,null,null,MySQLiteHelper.COLUMN_ID + " ASC ","50");

//        String query = "SELECT * FROM " + MySQLiteHelper.TABLE_SONGS + " ORDER BY " +
//                MySQLiteHelper.COLUMN_PLAYED_DATE_TIME_SONG + " ASC " + "LIMIT 10";

       /*Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                allColumns, MySQLiteHelper.COLUMN_SP_PLAYLIST_ID + " = " + "'" + ID + "'" + " AND " + MySQLiteHelper.COLUMN_IS_DOWNLOADED + " = " + 1, null, null, null, null);*/
//        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PlayerStatus status = cursorToPlayerStatus(cursor);
            playerStatus.add(status);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return playerStatus;
    }

  public void deletePlayedStatus(String type){
      database.delete(MySQLiteHelper.TABLE_PLAYER_STATUS, MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE
              + " = " + "'" + type + "'", null);
  }

    public void deletePlayedSongStatus(String type, String titleidSong){
        database.delete(MySQLiteHelper.TABLE_PLAYER_STATUS, MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE
                + " = " + "'" + type + "'" + " AND " + MySQLiteHelper.COLUMN_TITLE_ID_SONG + "=" + "'" + titleidSong + "'", null);
    }

    public void deletePlayedSongStatusForTime(String type, String titleidSong, String playedTime){
        database.delete(MySQLiteHelper.TABLE_PLAYER_STATUS, MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE
                + " = " + "'" + type + "'"
                + " AND " + MySQLiteHelper.COLUMN_TITLE_ID_SONG + "=" + "'" + titleidSong + "'"
                + " AND " + MySQLiteHelper.COLUMN_PLAYED_DATE_TIME_SONG + "=" + "'" + playedTime + "'", null);
    }

    public void deletePlayedAdvStatus(String type, String advid){
        database.delete(MySQLiteHelper.TABLE_PLAYER_STATUS, MySQLiteHelper.COLUMN_IS_PLAYER_STATUS_TYPE
                + " = " + "'" + type + "'" + " AND " + MySQLiteHelper.COLUMN_ADVERTISEMENT_ID_STATUS + "=" + "'" + advid + "'", null);
    }

    public void deletePlayerStatusTable(){
        String query = "Delete * from " +MySQLiteHelper.TABLE_PLAYER_STATUS;
        database.rawQuery(query,null);
        Log.d("Delete Table","Table_Deleted");
    }

}
