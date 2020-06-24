package com.NuSignSignagePlayer.models;

import java.io.Serializable;

/**
 * Created by ParasMobile on 8/1/2016.
 */
public class Prayers implements Serializable {
    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    private long _id = 0;
    private String Response = "";
    private String End_Prayer_Date;
    private String End_Prayer_Time;
    private String Start_Prayer_Date;
    private String Start_Prayer_Time;
    private long start_time_in_milli_prayer;
    private long end_time_in_milli_prayer;



    public void setStart_time_in_milli_prayer(long start_time_in_milli_prayer) {
        this.start_time_in_milli_prayer = start_time_in_milli_prayer;
    }

    public long getStart_time_in_milli_prayer() {
        return start_time_in_milli_prayer;
    }


    public long getEnd_time_in_milli_prayer() {
        return end_time_in_milli_prayer;
    }

    public void setEnd_time_in_milli_prayer(long end_time_in_milli_prayer) {
        this.end_time_in_milli_prayer = end_time_in_milli_prayer;
    }

    public String getStart_Prayer_Time() {
        return Start_Prayer_Time;
    }

    public void setStart_Prayer_Time(String start_Prayer_Time) {
        Start_Prayer_Time = start_Prayer_Time;
    }

    public String getStart_Prayer_Date() {
        return Start_Prayer_Date;
    }

    public void setStart_Prayer_Date(String start_Prayer_Date) {
        Start_Prayer_Date = start_Prayer_Date;
    }

    public String getEnd_Prayer_Time() {
        return End_Prayer_Time;
    }

    public void setEnd_Prayer_Time(String end_Prayer_Time) {
        End_Prayer_Time = end_Prayer_Time;
    }

    public String getEnd_Prayer_Date() {
        return End_Prayer_Date;
    }

    public void setEnd_Prayer_Date(String end_Prayer_Date) {
        End_Prayer_Date = end_Prayer_Date;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

//    public String getP_ID() {
//        return p_ID;
//    }
//
//    public void setP_ID(String p_ID) {
//        this.p_ID = p_ID;
//    }
}
