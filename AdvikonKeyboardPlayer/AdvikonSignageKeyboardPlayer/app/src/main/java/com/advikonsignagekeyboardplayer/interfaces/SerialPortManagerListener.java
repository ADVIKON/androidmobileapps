package com.advikonsignagekeyboardplayer.interfaces;

import com.advikonsignagekeyboardplayer.models.Announcement;

public interface SerialPortManagerListener {
    void playAnnouncement(Announcement announcement);
    void tankInOut();
    void tankEmpty(int status);
}
