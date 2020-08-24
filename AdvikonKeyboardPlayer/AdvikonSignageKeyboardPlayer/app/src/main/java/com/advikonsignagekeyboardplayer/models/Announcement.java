package com.advikonsignagekeyboardplayer.models;

import java.io.Serializable;

public class Announcement implements Serializable {

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public enum MediaType {
        IMAGE,
        VIDEO,
        UNKNOWN
    }

    public enum LogType {
        UNKNOWN, //If the status of event is not known
        SHOT, //If the shot is taken from sanitizer
        OUT, //If the tank is taken out
        IN //If the tank is put back in
    }

    private String ancFilePath;
    private String ancID;
    private String ancFileUrl;
    private int status_Download = 0;
    private int serialNo = 0;

    public MediaType getMediaType() {

        String extension = "";

        if (ancFileUrl.contains(".")) {
            extension = ancFileUrl.substring(ancFileUrl.lastIndexOf("."));
        }
        extension = extension.toLowerCase();

        if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")) {
            return MediaType.IMAGE;
        } else if (extension.equals(".mp4")){
            return MediaType.VIDEO;
        }

        return MediaType.UNKNOWN;
    }

    public String getAncFileUrl() {
        return ancFileUrl;
    }

    public void setAncFileUrl(String ancFileUrl) {
        this.ancFileUrl = ancFileUrl;
    }

    public String getAncID() {
        return ancID;
    }

    public void setAncID(String ancID) {
        this.ancID = ancID;
    }

    public String getAncFilePath() {
        return ancFilePath;
    }

    public void setAncFilePath(String ancFilePath) {
        this.ancFilePath = ancFilePath;
    }

    public int getStatus_Download() {
        return status_Download;
    }

    public void setStatus_Download(int status_Download) {
        this.status_Download = status_Download;
    }
}
