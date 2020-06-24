package com.NuSignSignagePlayer.custom_views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.opengl.GLES20;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


public class MyClaudVideoView extends TextureView implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public interface OnMediaCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    OnMediaCompletionListener onMediaCompletionListener = null;

    MediaPlayer mediaPlayer;

    public MyClaudVideoView(Context context) {
        super(context);
    }

    public MyClaudVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnMediaCompletionListener(OnMediaCompletionListener listener){
        this.onMediaCompletionListener = listener;
    }

    public void playMedia(String path){
        playMedia(Uri.parse(path));
    }

    public void playMedia(Uri uri) {

        try {

            Surface s = new Surface(this.getSurfaceTexture());
            mediaPlayer= new MediaPlayer();
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setSurface(s);
            mediaPlayer.prepareAsync();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Boolean isPlaying(){

        if (mediaPlayer == null ){
            return false;
        }

        return mediaPlayer.isPlaying();
    }

    public void stopPlayback() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }


    /***************************************************************************************
     *
     * MediaPlayer Listener Methods
     *
     ***************************************************************************************/

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        mediaPlayer.reset();
        clearSurface(this.getSurfaceTexture());

        if (onMediaCompletionListener != null){
            onMediaCompletionListener.onCompletion(mediaPlayer);
        }


    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

        return false;
    }

    /***************************************************************************************
     *
     * TextureView.SurfaceTextureListener
     *
    ***************************************************************************************/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
       // surfaceTexture.attachToGLContext(5);

    }

    /**
     * Clear the given surface Texture by attaching a GL context and clearing the surface.
     * @param texture a valid SurfaceTexture
     */
    public void clearSurface(SurfaceTexture texture) {

        if(texture == null){
            return;
        }

        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(display, null);

        int[] attribList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        egl.eglChooseConfig(display, attribList, configs, configs.length, numConfigs);
        EGLConfig config = configs[0];
        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{
                12440, 2,
                EGL10.EGL_NONE
        });


        EGLSurface eglSurface = egl.eglCreateWindowSurface(display, config, texture,
                new int[]{
                        EGL10.EGL_NONE
                });

        egl.eglMakeCurrent(display, eglSurface, eglSurface, context);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        egl.eglSwapBuffers(display, eglSurface);
        egl.eglDestroySurface(display, eglSurface);
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        egl.eglDestroyContext(display, context);
        egl.eglTerminate(display);
    }

}
