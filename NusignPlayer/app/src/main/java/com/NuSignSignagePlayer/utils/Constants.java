package com.NuSignSignagePlayer.utils;


public class Constants {

  //  public static final String SERVER = "http://134.119.178.26/api/";
   public static final String SERVER = "http://api.nusign.eu/api/";


    public static final String VIDEO_TAG = "video";

    public static final String PLAYER_TYPE = "Android";

    public static final String CHECK_USER_RIGHTS = SERVER + "CheckUserRightsLive_bulk";//DeviceId
    public static final int CHECK_USER_RIGHTS_TAG = 1;


    public static final String CHECK_USER_LOGIN = SERVER + "CheckUserLoginLive";//DeviceId,TokenNo,UserName
    public static final int CHECK_USER_LOGIN_TAG = 2;

    public static final String UPDATE_CRASH_LOG = SERVER + "TokenCrashLog";
    public static final int UPDATE_CRASH_LOG_TAG = 15;
    public static final String GetSplPlaylist_VIDEO =  SERVER + "GetSplPlaylistVideo";
    public static final int GetSplPlaylist_TAG = 3;
    public static final String CRASH_MESSAGE = "crash_message";

    public static final String GET_SPL_PLAY_LIST_TITLES_VIDEO = SERVER + "GetSplPlaylistTitlesVideo";
    public static final int GET_SPL_PLAY_LIST_TITLES_TAG = 4;

    public static final String UpdateFcm=SERVER + "UpdateFCMId";
    public static final String PLAYER_LOGIN_STATUS_STREAM = SERVER + "PlayerLoginStatusJsonArray";// login status
    public static final int PLAYER_LOGIN_STATUS_STREAM_TAG = 5;// login status


    public static final String PLAYED_SONG_STATUS_STREAM = SERVER + "PlayedSongsStatusJsonArray";// played song status
    public static final int PLAYED_SONG_STATUS_STREAM_TAG = 6;// login status


    public static final String PLAYER_HEARTBEAT_STATUS_STREAM = SERVER + "PlayerHeartBeatStatusJsonArray";// player heartbeat
    public static final int PLAYER_HEARTBEAT_STATUS_STREAM_TAG = 7;// login status


    public static final String ADVERTISEMENTS = SERVER + "AdvtSchedule";// prayer time
    public static final int ADVERTISEMENTS_TAG = 8;// login status

    public static final String GetSplPlaylist = SERVER + "GetSplPlaylistLive";// Special playlist
    public static final String GET_SPL_PLAY_LIST_TITLES = SERVER + "GetSplPlaylistTitlesLive";//playlist id
    public static final String PRAYER_TIME = SERVER + "PrayerTiming";// prayer time

    public static final String PLAYER_LOGOUT_STATUS_STREAM = SERVER + "PlayerLogoutStatusJsonArray";// logout status
    public static final int PLAYER_LOGOUT_STATUS_STREAM_TAG = 9;// login status

    public static final String PLAYED_ADVERTISEMENT_STATUS_STREAM = SERVER + "PlayedAdvertisementStatusJsonArray";// played advertisement status
    public static final int PLAYED_ADVERTISEMENT_TAG = 8;// login status

    public static final String DOWNLOADINGPROCESS = SERVER + "DownloadingProcess";// played advertisement status
    public static final int DOWNLOADINGPROCESS_TAG = 10;// login status

    public static final String CHECK_TOKEN_PUBLISH = SERVER + "CheckTokenPublish";
    public static final int CHECK_TOKEN_PUBLISH_TAG = 11;

    public static final String UPDATE_TOKEN_PUBLISH = SERVER + "UpdateTokenPublish";
    public static final int UPDATE_TOKEN_PUBLISH_TAG = 12;

    public static final String UPDATE_PLAYLIST_DOWNLOADED_SONGS = SERVER + "PlaylistWiseDownloadedTotalSong";
    public static final int UPDATE_PLAYLIST_DOWNLOADED_SONGS_TAG = 13;

    public static final String UPDATE_PLAYLIST_SONGS_DETAILS = SERVER + "PlaylistWiseDownloadedSongsDetail";
    public static final int UPDATE_PLAYLIST_SONGS_DETAILS_TAG = 14;

    public static final String SCHEDULED_SONGS = SERVER + "GetAllPlaylistScheduleSongs";
    public static final int SCHEDULED_SONGS_TAG = 16;

    public static final String GetTokenContent = SERVER + "GetTokenContent";
    public static final int  GetTokenContent_TAG = 17;

    public static final String PLAYED_PRAYER_STATUS_STREAM = SERVER + "PlayedPrayerStatusJsonArray";// played prayer status
    public static final String KEY_PLAYLIST_NAMES_ARRAY = "playlistNamesArray";

    public static final String ALARM_ACTION = "com.alarm.action";
    public static final String ALARM_PLAYLIST_CHANGED = "com.alarm.playlist.changed";

    public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";

    // Services for videos

    public static final String TOKEN_ID = "token_no";

    public static final String ROOT_FOLDER = "AlenkaMedia";

    public static final String ADVERTISEMENT_FOLDER = "Advertisements";

    public static final String CONTENT_FOLDER = "AlenkaMedia";

    public static final String TAG_START_DOWNLOAD_SERVICE = "TAG_START_DOWNLOAD_SERVICE";

    public static final String IS_UPDATE_IN_PROGRESS = "IS_UPDATE_IN_PROGRESS";

    public static final String TAG_FILE_EXTENSION_MP3 ="mp4";

    public static final String STORAGE_ALERT_SHOWN_ONCE = "STORAGE_ALERT_SHOWN_ONCE";

    public static final String SONGS_LAST_REMOVED = "SONGS_LAST_REMOVED";

}
