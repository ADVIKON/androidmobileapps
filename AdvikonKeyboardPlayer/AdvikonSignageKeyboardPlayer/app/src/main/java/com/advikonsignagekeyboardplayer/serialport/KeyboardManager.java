package com.advikonsignagekeyboardplayer.serialport;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.advikonsignagekeyboardplayer.activities.HomeActivity;
import com.advikonsignagekeyboardplayer.eventbus.MessageEvent;
import com.advikonsignagekeyboardplayer.interfaces.WirelessKeyboardManagerListener;
import com.advikonsignagekeyboardplayer.utils.AlenkaMediaPreferences;
import com.advikonsignagekeyboardplayer.utils.SharedPreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

public class KeyboardManager {

    private Context context;

    WirelessKeyboardManagerListener wirelessKeyboardManagerListener;

    private Boolean isDebugMode = false;

    private Boolean settingsLocked = false;

    public KeyboardManager(Context context){
        this.context = context;
        wirelessKeyboardManagerListener = (HomeActivity) this.context;
        loadSettings();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();

    }

    private void loadSettings(){
        settingsLocked = true;
        isDebugMode = SharedPreferenceUtil.getBooleanPreference(this.context, AlenkaMediaPreferences.isDemoToken,false);
        settingsLocked = false;
    }

    KeyListener tkl = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
    Editable et = Editable.Factory.getInstance().newEditable("");
    final Debouncer debouncer = new Debouncer();

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            tkl.onKeyDown(null, et, keyCode, event);
        else if (event.getAction() == KeyEvent.ACTION_UP)
            tkl.onKeyUp(null, et, keyCode, event);
        else
            tkl.onKeyOther(null, et, event); //NOTE: My devices never used KeyEvent.ACTION_MULTIPLE so I don't know if it should get fired here or with the key down event.

        debouncer.debounce(Void.class, new Runnable() {
            @Override public void run() {

                Log.i("Test: " , et.toString());

                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        String inputString = et.toString();
                        et.clear();

                        if (inputString == null){
                            return;
                        }

                        String countString = inputString.replaceAll("[\\D]", "");

                        if (countString == null || countString.equals("")) {
                            return;
                        }
                        Log.e("KEYBOARD INPUT", countString);

                        String upToNCharacters = countString.substring(0, Math.min(countString.length(), 3));

                        Integer newCount = Integer.parseInt(upToNCharacters);

                        if (newCount < 1 || newCount > 999){
                            if (isDebugMode) {
                                Toast.makeText(KeyboardManager.this.context, "Range not between 1 and 999 - input value: " + newCount, Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        String paddedInteger = String.format("%03d", newCount);

                        if (isDebugMode) {
                            Toast.makeText(KeyboardManager.this.context, paddedInteger, Toast.LENGTH_SHORT).show();
                        }

                        Integer newCountPadded = Integer.parseInt(paddedInteger);

                        wirelessKeyboardManagerListener.keyboardInputReceived(newCountPadded - 1);
                    }
                };
                mainHandler.post(myRunnable);

            }
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        loadSettings();
    };

}
