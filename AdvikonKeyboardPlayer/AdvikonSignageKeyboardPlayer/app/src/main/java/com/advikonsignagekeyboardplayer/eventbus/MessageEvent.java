package com.advikonsignagekeyboardplayer.eventbus;

import com.advikonsignagekeyboardplayer.models.Announcement;

public class MessageEvent {

    public Announcement downloadAnnouncement;

    public Boolean reloadSettings = false;

    public MessageEvent(Announcement announcement) {
        downloadAnnouncement = announcement;
    }

    public MessageEvent(Boolean reloadSettings){
        this.reloadSettings = reloadSettings;
    }
}