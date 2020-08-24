package com.advikonsignagekeyboardplayer.api_manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ADD on 23-04-2016.
 */
public class OkHttpUtil extends AsyncTask<Void, Void ,Void> {

    private Context cntxt;
    private String json;
    private String url;
    private String response;
    private OkHttpResponse listener;
    private int tag;
    private String TAG = "OkHttpUtil";


    public OkHttpUtil(Context cntxt, String url, String json,OkHttpResponse listener,boolean isProgress, int tag){

       // Toast.makeText(cntxt, "url => "+ url+ " json => "+ json , Toast.LENGTH_SHORT).show();
        this.cntxt=cntxt;
        this.url=url;
        this.json=json;
        this.listener=listener;
        this.tag = tag;

    }

    @Override
    protected Void doInBackground(Void... params) {
        if(json==null){

            try {
                response = run(url);


            }catch (Exception e){
                Log.e(TAG,"EXCEPTION OCCURED");
                e.printStackTrace();
                listener.onError(e,this.tag);

            }
        }else{
            try {
                response = post(url, json);

            }catch (IOException e){

                Log.e(TAG,"EXCEPTION OCCURED");
                e.printStackTrace();
                listener.onError(e,this.tag);
            }
        }



        return null;
    }

    public void callRequest(){

        Ion.with(this.cntxt)
                .load(this.url)
                .setHeader("Content-Type","application/json")
                //.setHeader("Accept","application/json")
                .setStringBody(OkHttpUtil.this.json)
                .asString()
        .setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {

                if (e != null){
                    Log.e(TAG,"Call error " + OkHttpUtil.this.url + " " + OkHttpUtil.this.json);
                    e.printStackTrace();
                    listener.onError(e, OkHttpUtil.this.tag);
                    return;
                }

                if (result != null){
                    Log.e(TAG,"Call success " + OkHttpUtil.this.url + "\n" + OkHttpUtil.this.json + "\n" + result);
                    response = result;
                    listener.onResponse(result, OkHttpUtil.this.tag);
                }
            }
        });
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);



        if (response != null){
//            Log.e(TAG,response);
            Log.e(TAG,"Call success " + OkHttpUtil.this.url + "\n" + OkHttpUtil.this.json + "\n" + response);

        }


        listener.onResponse(response,this.tag);
    }

    OkHttpClient client = new OkHttpClient();


    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    
    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public interface OkHttpResponse{

        public void onResponse(String response, int tag);
        public void onError(Exception e, int tag);
    }

}
