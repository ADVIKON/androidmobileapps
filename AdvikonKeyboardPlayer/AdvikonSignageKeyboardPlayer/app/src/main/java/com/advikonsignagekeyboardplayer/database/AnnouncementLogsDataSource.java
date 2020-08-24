package com.advikonsignagekeyboardplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.advikonsignagekeyboardplayer.models.AnnouncementLog;

import java.sql.SQLException;
import java.util.ArrayList;

public class AnnouncementLogsDataSource {

    //    / Database fields
    public SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG, MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME,
            MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME_STRING, MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_COMMAND,
            MySQLiteHelper.COLUMN_ANNOUNCEMENT_PLAYED_ANNC_ID
    };

    public AnnouncementLogsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {

        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Boolean createLog(AnnouncementLog log) {

        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG, log.getLog());
        values.put(MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME, System.currentTimeMillis());
        values.put(MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME_STRING, log.getLogDateTimeString());
        values.put(MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_COMMAND, log.getCommand());
        values.put(MySQLiteHelper.COLUMN_ANNOUNCEMENT_PLAYED_ANNC_ID, log.getAnnouncementId());

        long insertId = database.insert(MySQLiteHelper.TABLE_ANNOUNCEMENT_LOGS, null,
                values);

        if (insertId >= 0) {
             return true;
        }

        return false;
    }

    private AnnouncementLog cursorToAnnouncementLog(Cursor cursor) {
        AnnouncementLog log = new AnnouncementLog();
        log.setId(cursor.getLong(0));
        log.setLog(cursor.getInt(1));
        log.setLogDateTime(cursor.getInt(2));
        log.setLogDateTimeString(cursor.getString(3));
        log.setCommand(cursor.getString(4));
        log.setAnnouncementId(cursor.getString(5));
        return log;
    }

    public ArrayList<AnnouncementLog> getAnnouncementLogs(){

        ArrayList<AnnouncementLog> logs = new ArrayList<AnnouncementLog>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENT_LOGS,allColumns
                , null,null,null,null, MySQLiteHelper.COLUMN_ID + " DESC ","50");

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            AnnouncementLog status = cursorToAnnouncementLog(cursor);
            logs.add(status);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return logs;
    }

    public ArrayList<AnnouncementLog> getAnnouncementLogsNew(){

        ArrayList<AnnouncementLog> logs = new ArrayList<AnnouncementLog>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENT_LOGS,allColumns
                , null,null,null,null, MySQLiteHelper.COLUMN_ID + " ASC ","20");

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            AnnouncementLog status = cursorToAnnouncementLog(cursor);
            logs.add(status);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return logs;
    }

    public ArrayList<AnnouncementLog> getAnnouncement(String dateTime) {

        ArrayList<AnnouncementLog> logs = new ArrayList<AnnouncementLog>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_ANNOUNCEMENT_LOGS, allColumns , MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME_STRING
                + " = " + "'" + dateTime + "'", null,null,null,null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            AnnouncementLog status = cursorToAnnouncementLog(cursor);
            logs.add(status);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return logs;
    }

    public boolean deleteAnnouncements(String dateTime) {

        long status = database.delete(MySQLiteHelper.TABLE_ANNOUNCEMENT_LOGS, MySQLiteHelper.COLUMN_ANNOUNCEMENT_LOG_DATE_TIME_STRING
                + " = " + "'" + dateTime + "'", null);
        Log.d("AnnouncementLogsDataSource","Deleted announcement for " + dateTime);
        if (status >= 0) {
            return true;
        } else {
            return false;
        }
    }
}
