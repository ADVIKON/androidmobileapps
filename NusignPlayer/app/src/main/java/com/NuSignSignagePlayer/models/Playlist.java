package com.NuSignSignagePlayer.models;

import java.io.Serializable;

/**
 * Created by ParasMobile on 6/17/2016.
 */
public class Playlist implements Serializable {

    private String End_time;
    private long _id = 0;
    private String Format_id = "";
    private String Start_time;
    private String dfclient_id = "";
    private String pSc_id = "";
    private String splPlaylist_Id ="";
    private String splPlaylist_Name = "";
    private String playlistCategory = "";
    private long Start_Time_In_Milli;
    private long End_Time_In_Milli;
    private long isSeparatinActive = 0;


    public long getEnd_Time_In_Milli() {
        return End_Time_In_Milli;
    }

    public void setEnd_Time_In_Milli(long end_Time_In_Milli) {
        End_Time_In_Milli = end_Time_In_Milli;
    }

    public long getStart_Time_In_Milli() {
        return Start_Time_In_Milli;
    }

    public void setStart_Time_In_Milli(long start_Time_In_Milli) {
        Start_Time_In_Milli = start_Time_In_Milli;
    }



    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    ///*** end time***///
    public String getEnd_time() {
        return End_time;
    }
    public void setEnd_time(String end_time) {
        End_time = end_time;
    }

    ///**start time***///
    public String getStart_time() {
        return Start_time;
    }

    public void setStart_time(String start_time) {
        Start_time = start_time;
    }

    /**** format id ***/
    public String getFormat_id()
    {
        return  Format_id;
    }
    public void setFormat_id(String format_id)
    {
        Format_id = format_id;
    }


    //*** dfclient id***//
    public String getdfclient_id() {
        return dfclient_id;
    }
    public void setdfclient_id(String dfclientId) {
        dfclient_id = dfclientId;
    }


    public String getpSc_id() {
        return pSc_id;
    }
    public void setpSc_id(String pScId) {
        pSc_id = pScId;
    }


    public String getsplPlaylist_Id() {
        return splPlaylist_Id;
    }
    public void setsplPlaylist_Id(String splPlaylist_Id1) {
        splPlaylist_Id= splPlaylist_Id1;
    }


    public String getsplPlaylist_Name() {

        return splPlaylist_Name;
    }
    public void setsplPlaylist_Name(String splPlaylist_Name1) {
        splPlaylist_Name= splPlaylist_Name1;
    }

    public void setPlaylistCategory(String splPlaylistCategory1) {
        playlistCategory= splPlaylistCategory1;
    }

    public String getsplPlaylistCategory() {
        return playlistCategory;
    }
    public long getIsSeparatinActive() {
        return isSeparatinActive;
    }

    public void setIsSeparatinActive(long isSeparatinActive) {
        this.isSeparatinActive = isSeparatinActive;
    }
}