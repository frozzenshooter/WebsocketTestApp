package com.garlic.websockettest.messages;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.garlic.websockettest.ApplicationContext;
import com.garlic.websockettest.R;
import com.garlic.websockettest.SettingsActivity;
import com.garlic.websockettest.websockets.WebSocketConnection;

public class MessageObserver implements SharedPreferences.OnSharedPreferenceChangeListener{

    public  static final  int FOREGROUND_ID = 313399;

    private static final String TAG = MessageObserver.class.getSimpleName();
    private final Application context;
    private boolean appVisible;
    private volatile boolean networkLost;

    private MessageRetrievalThread messageRetrievalThread = null;


    public MessageObserver(@NonNull Application context){

        this.context = context;

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        String uri = getUri(sharedPref);

        // Start thread which will use the websocket connection to receive messages
        restartMessageRetrievalThread(uri);

        // Foreground service in order to secure that android won't shutdown the background process
        ContextCompat.startForegroundService(context, new Intent(context, MessageObserver.ForegroundService.class));

        // This can be used to enable/disable the foreground service - a visible app won't close a background task
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                onAppForegrounded();
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                onAppBackgrounded();
            }
        });

        // Handling of network changes -> if the network is lost close the socket
        // if the network is back on again reestablish the connection and start receiving
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (MessageObserver.this) {
                    if (!hasNetwork()) {
                        Log.w(TAG, "Lost network connection. Shutting down our websocket connections.");

                        //TODO: shutdown websocket
                    }
                    MessageObserver.this.notifyAll();
                }
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private synchronized void onAppForegrounded() {
        appVisible = true;
        notifyAll();
    }

    private synchronized void onAppBackgrounded() {
        appVisible = false;
        notifyAll();
    }

    /**
     * Waits until the device has established a network connection
     */
    private synchronized void waitForConnectionPossible() {
        try {
            while (!hasNetwork()) wait();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns if the device has a network connection
     *
     * @return hasNetwork
     */
    private synchronized boolean hasNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo   = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equalsIgnoreCase(SettingsActivity.HOST) || key.equalsIgnoreCase(SettingsActivity.PORT))
        {
            String uri = getUri(sharedPreferences);
            restartMessageRetrievalThread(uri);
        }
    }

    private void restartMessageRetrievalThread(String uri) {
        if(messageRetrievalThread != null){
            messageRetrievalThread.uri = uri;
            messageRetrievalThread.wasUpdated = true;
        }else{
            this.messageRetrievalThread = new MessageRetrievalThread(uri);
            this.messageRetrievalThread.start();
        }
    }

    private String getUri(SharedPreferences sharedPref){
        String port = sharedPref.getString(SettingsActivity.PORT, "8888");
        String host = sharedPref.getString(SettingsActivity.HOST, "localhost");

        //return "ws://192.168.2.100:8765";
        return "ws://"+host+":"+port;
    }

    /**
     * Thread to retrieve messages from the websocket
     */
    private class MessageRetrievalThread extends Thread  implements Thread.UncaughtExceptionHandler{
        public volatile boolean wasUpdated;
        public volatile String uri;

        MessageRetrievalThread(String uri) {
            super("MessageRetrievalService");
            setUncaughtExceptionHandler(this);
            this.wasUpdated = false;
            this.uri = uri;
        }

        @Override
        public void run(){

            while (true) {
                Log.i(TAG, "Waiting for websocket state change....");
                waitForConnectionPossible();

                Log.i(TAG, "Making websocket connection....");
                WebSocketConnection websocket = new WebSocketConnection(this.uri);
                websocket.connect();
                this.wasUpdated = false;

                try {
                    while (hasNetwork() && !this.wasUpdated) {
                        // do nothing (the websocket listener will retrieve messages in the background)
                    }
                } catch (Throwable e) {
                    Log.w(TAG, e);
                } finally {
                   websocket.disconnect();
                }

                Log.i(TAG, "Looping...");
            }
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.w(TAG, "*** Uncaught exception!");
            Log.w(TAG, e);
        }
    }

    /**
     * Foreground service which allows to run a background task, in this case the message retrieval, whom won't be terminated by the OS to reduce battery consumption
     */
    public static class ForegroundService extends Service {

        @Override
        public @Nullable
        IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ApplicationContext.CHANNEL_ID);
            builder.setContentTitle(getString(R.string.MessageRetrievalService));
            builder.setContentText(getString(R.string.MessageRetrievalService_background_connection_enabled));
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
            builder.setWhen(0);
            builder.setSmallIcon(R.drawable.outline_cached_24);
            startForeground(FOREGROUND_ID, builder.build());

            return Service.START_STICKY;
        }
    }
}
