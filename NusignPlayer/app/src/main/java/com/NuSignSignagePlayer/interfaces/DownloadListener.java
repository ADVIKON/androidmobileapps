package com.NuSignSignagePlayer.interfaces;

import com.NuSignSignagePlayer.models.Advertisements;
import com.NuSignSignagePlayer.models.Songs;

/**
 * Created by love on 31/5/17.
 */
public interface DownloadListener {

    void onUpdate(long value);
    void downloadCompleted(boolean shouldPlay, Songs songs);
    void advertisementDownloaded(Advertisements advertisements);
    void startedCopyingSongs(int currentSong, int totalSongs, boolean isFinished);
    void showOfflineDownloadingAlert();
    void finishedDownloadingSongs(int totalSongs);
}
