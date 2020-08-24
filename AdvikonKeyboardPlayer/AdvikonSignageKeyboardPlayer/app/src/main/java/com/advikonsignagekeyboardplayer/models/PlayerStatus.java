package com.advikonsignagekeyboardplayer.models;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by patas tech on 9/19/2016.
 */
public class PlayerStatus implements Serializable, Comparator {
    private long id = 0;
    private String loginDate;
    private String loginTime;
    private String artistIdStatusSong;
    private String playerDateTimeSong;
    private String titleIdSong;
    private String splPlaylistIdSong;
    private String heartbeatDateTimeStatus;
    private String AdvIdStatus;
    private String AdvPlayedDate;
    private String AdvPlayedTime;
    private String prayerPlayedDate;
    private String prayerPlayedTime;
    private String logoutDate;
    private String logoutTime;
    private String playerStatusAll;
    private Date playedDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(String loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getArtistIdStatusSong() {
        return artistIdStatusSong;
    }

    public void setArtistIdStatusSong(String artistIdStatusSong) {
        this.artistIdStatusSong = artistIdStatusSong;
    }

    public String getPlayerDateTimeSong() {
        return playerDateTimeSong;
    }

    public void setPlayerDateTimeSong(String playerDateTimeSong) {
        this.playerDateTimeSong = playerDateTimeSong;
    }

    public String getTitleIdSong() {
        return titleIdSong;
    }

    public void setTitleIdSong(String titleIdSong) {
        this.titleIdSong = titleIdSong;
    }

    public String getSplPlaylistIdSong() {
        return splPlaylistIdSong;
    }

    public void setSplPlaylistIdSong(String splPlaylistIdSong) {
        this.splPlaylistIdSong = splPlaylistIdSong;
    }

    public String getHeartbeatDateTimeStatus() {
        return heartbeatDateTimeStatus;
    }

    public void setHeartbeatDateTimeStatus(String heartbeatDateTimeStatus) {
        this.heartbeatDateTimeStatus = heartbeatDateTimeStatus;
    }

    public String getAdvIdStatus() {
        return AdvIdStatus;
    }

    public void setAdvIdStatus(String advIdStatus) {
        AdvIdStatus = advIdStatus;
    }

    public String getAdvPlayedDate() {
        return AdvPlayedDate;
    }

    public void setAdvPlayedDate(String advPlayedDate) {
        AdvPlayedDate = advPlayedDate;
    }

    public String getAdvPlayedTime() {
        return AdvPlayedTime;
    }

    public void setAdvPlayedTime(String advPlayedTime) {
        AdvPlayedTime = advPlayedTime;
    }

    public String getPrayerPlayedDate() {
        return prayerPlayedDate;
    }

    public void setPrayerPlayedDate(String prayerPlayedDate) {
        this.prayerPlayedDate = prayerPlayedDate;
    }

    public String getPrayerPlayedTime() {
        return prayerPlayedTime;
    }

    public void setPrayerPlayedTime(String prayerPlayedTime) {
        this.prayerPlayedTime = prayerPlayedTime;
    }

    public String getLogoutDate() {
        return logoutDate;
    }

    public void setLogoutDate(String logoutDate) {
        this.logoutDate = logoutDate;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getPlayerStatusAll() {
        return playerStatusAll;
    }

    public void setPlayerStatusAll(String playerStatusAll) {
        this.playerStatusAll = playerStatusAll;
    }


    public Date getPlayedDate() {
        return playedDate;
    }

    public void setPlayedDate(Date playedDate) {
        this.playedDate = playedDate;
    }

    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }
}
