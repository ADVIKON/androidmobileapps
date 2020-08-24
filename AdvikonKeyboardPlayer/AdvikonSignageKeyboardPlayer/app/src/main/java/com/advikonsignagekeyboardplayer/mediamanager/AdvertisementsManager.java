package com.advikonsignagekeyboardplayer.mediamanager;

import android.content.Context;

import com.advikonsignagekeyboardplayer.database.AdvertisementDataSource;
import com.advikonsignagekeyboardplayer.models.Advertisements;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by love on 12/6/17.
 */
public class AdvertisementsManager {

    private Context context;

    private AdvertisementDataSource advertisementDataSource = null;

    public AdvertisementsManager(Context context) {

        this.context = context;

        this.advertisementDataSource = new AdvertisementDataSource(this.context);
    }
    public ArrayList<Advertisements> getAdvertisementsToBeDownloaded(){

        try {
            advertisementDataSource.open();
            return advertisementDataSource.getAdvThoseAreNotDownloaded();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            advertisementDataSource.close();
        }
        return null;
    }

    public ArrayList<Advertisements> getAdvertisementsThatAreDownloaded(){

        try {
            advertisementDataSource.open();
            return advertisementDataSource.getAllAdv();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            advertisementDataSource.close();
        }
        return null;
    }

    public void advertisementDownloaded(Advertisements advertisements){
        try {

            advertisementDataSource.open();
            advertisementDataSource.UpdateDownloadStatusAndPath(advertisements);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Advertisements> getAdvertisementsForComingTime(){

        try {
            advertisementDataSource.open();
            ArrayList<Advertisements> allAds = advertisementDataSource.getAllAdv();

            if (allAds.size() > 0){

                ArrayList<Advertisements> adsForComingTime = new ArrayList<>();

                for (Advertisements ad :allAds) {

                    /*Add only those advertisements whose end time is greater than current time.*/
                    if (ad.getStart_Adv_Time_Millis() >= System.currentTimeMillis()) {
                        adsForComingTime.add(ad);
                    }
                }

                return adsForComingTime;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            advertisementDataSource.close();
        }
        return null;
    }
}
