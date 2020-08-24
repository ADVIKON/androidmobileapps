package com.advikonsignagekeyboardplayer.models;

import java.io.Serializable;

/**
 * Created by patas tech on 9/2/2016.
 */
public class Advertisements implements Serializable {

    private String AdvtFilePath;
    private String AdvtID;
    private String AdvtName;
    private String IsMinute;
    private String IsSong;
    private String IsTime;
    private String PlayingType;
    private String Response;
    private String SRNo;
    private String TotalMinutes;
    private String TotalSongs;
    private String eDate;
    private long _id = 0;
    private String advFileUrl;
    private String sDate;
    private String sTime;
    private int status_Download = 0 ;
    private long start_Adv_Date_Millis;

    public long getStart_Adv_Date_Millis() {
        return start_Adv_Date_Millis;
    }

    public void setStart_Adv_Date_Millis(long start_Adv_Date_Millis) {
        this.start_Adv_Date_Millis = start_Adv_Date_Millis;
    }

    public long getEnd_Adv_Date_Millis() {
        return end_Adv_Date_Millis;
    }

    public void setEnd_Adv_Date_Millis(long end_Adv_Date_Millis) {
        this.end_Adv_Date_Millis = end_Adv_Date_Millis;
    }

    public long getStart_Adv_Time_Millis() {
        return start_Adv_Time_Millis;
    }

    public void setStart_Adv_Time_Millis(long start_Adv_Time_Millis) {
        this.start_Adv_Time_Millis = start_Adv_Time_Millis;
    }

    private long end_Adv_Date_Millis;
    private long start_Adv_Time_Millis;


    public int getStatus_Download() {
        return status_Download;
    }

    public void setStatus_Download(int status_Download) {
        this.status_Download = status_Download;
    }



    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }



    public String getAdvFileUrl() {
        return advFileUrl;
    }

    public void setAdvFileUrl(String advFileUrl) {
        this.advFileUrl = advFileUrl;
    }



    public String getAdvtFilePath() {
        return AdvtFilePath;
    }

    public void setAdvtFilePath(String advtFilePath) {
        AdvtFilePath = advtFilePath;
    }

    public String getAdvtID() {
        return AdvtID;
    }

    public void setAdvtID(String advtID) {
        AdvtID = advtID;
    }

    public String getAdvtName() {
        return AdvtName;
    }

    public void setAdvtName(String advtName) {
        AdvtName = advtName;
    }

    public String getIsMinute() {
        return IsMinute;
    }

    public void setIsMinute(String isMinute) {
        IsMinute = isMinute;
    }

    public String getIsSong() {
        return IsSong;
    }

    public void setIsSong(String isSong) {
        IsSong = isSong;
    }

    public String getIsTime() {
        return IsTime;
    }

    public void setIsTime(String isTime) {
        IsTime = isTime;
    }

    public String getPlayingType() {
        return PlayingType;
    }

    public void setPlayingType(String playingType) {
        PlayingType = playingType;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

    public String getSRNo() {
        return SRNo;
    }

    public void setSRNo(String SRNo) {
        this.SRNo = SRNo;
    }

    public String getTotalMinutes() {
        return TotalMinutes;
    }

    public void setTotalMinutes(String totalMinutes) {
        TotalMinutes = totalMinutes;
    }

    public String getTotalSongs() {
        return TotalSongs;
    }

    public void setTotalSongs(String totalSongs) {
        TotalSongs = totalSongs;
    }

    public String geteDate() {
        return eDate;
    }

    public void seteDate(String eDate) {
        this.eDate = eDate;
    }

    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public String getsTime() {
        return sTime;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }



}
