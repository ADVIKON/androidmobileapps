package com.advikonsignagekeyboardplayer.interfaces;

/**
 * Created by love on 30/5/17.
 */
public interface PlaylistLoaderListener {

    void startedGettingPlaylist();

    void finishedGettingPlaylist();

    void errorInGettingPlaylist();

    void recordSaved(boolean isSaved);

    void tokenUpdatedOnServer();

}

