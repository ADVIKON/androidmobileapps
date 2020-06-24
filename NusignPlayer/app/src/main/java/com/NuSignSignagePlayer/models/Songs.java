package com.NuSignSignagePlayer.models;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by ParasMobile on 6/17/2016.
 */
public class Songs implements Serializable, Comparator {
    private String Album_ID = "";
    private String Artist_ID ="";
    private String Title = "";
    private String Title_Url = "";
    private String al_Name = "";
    private String Filesize = "";
    private String ar_Name = "";
    private String spl_PlaylistId = "";
    private String sch_id = "";
    private String t_Time = "";
    private String title_Id = "";
    private int is_Downloaded = 0 ;
    private String SongPath = "";
    private long SerialNo = 0;

    public String getSongPath() {
        return SongPath;
    }

    public void setSongPath(String songPath) {
        SongPath = songPath;
    }

    public String getSch_id() {
        return sch_id;
    }

    public String getFilesize()
    {
        return Filesize;
    }

    public void setFilesize(String filesize)
    {
       Filesize=filesize;
    }


    public void setSch_id(String sch_id) {
        this.sch_id = sch_id;
    }

    public int getIs_Downloaded() {
        return is_Downloaded;
    }

    public void setIs_Downloaded(int is_Downloaded) {
        this.is_Downloaded = is_Downloaded;
    }

    private long _id = 0;
    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getAlbum_ID() {
        return Album_ID;
    }

    public void setAlbum_ID(String album_ID) {
        Album_ID = album_ID;
    }

    public String getArtist_ID() {
        return Artist_ID;
    }

    public void setArtist_ID(String artist_ID) {
        Artist_ID = artist_ID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle_Url() {
        return Title_Url;
    }

    public void setTitle_Url(String title_Url) {
        Title_Url = title_Url;
    }

    public String getAl_Name() {
        return al_Name;
    }

    public void setAl_Name(String al_Name) {
        this.al_Name = al_Name;
    }

    public String getAr_Name() {
        return ar_Name;
    }

    public void setAr_Name(String ar_Name) {
        this.ar_Name = ar_Name;
    }

    public String getSpl_PlaylistId() {
        return spl_PlaylistId;
    }

    public void setSpl_PlaylistId(String spl_PlaylistId) {
        this.spl_PlaylistId = spl_PlaylistId;
    }

    public String getT_Time() {
        return t_Time;
    }

    public void setT_Time(String t_Time) {
        this.t_Time = t_Time;
    }

    public String getTitle_Id() {
        return title_Id;
    }

    public void setTitle_Id(String title_Id) {
        this.title_Id = title_Id;
    }

    public long getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(long serialNo) {
        SerialNo = serialNo;
    }


    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }
}
