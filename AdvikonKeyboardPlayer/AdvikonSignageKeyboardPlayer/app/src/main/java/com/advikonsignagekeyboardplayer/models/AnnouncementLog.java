package com.advikonsignagekeyboardplayer.models;

import java.io.Serializable;
import java.util.Comparator;

public class AnnouncementLog implements Serializable, Comparator {

    private long id = 0;
    private int log;
    private int logDateTime;
    private String logDateTimeString;
    private String command;
    private String announcementId;

    public int getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(int logDateTime) {
        this.logDateTime = logDateTime;
    }

    public int getLog() {
        return log;
    }

    public void setLog(int log) {
        this.log = log;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogDateTimeString() {
        return logDateTimeString;
    }

    public void setLogDateTimeString(String logDateTimeString) {
        this.logDateTimeString = logDateTimeString;
    }

    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }
}
