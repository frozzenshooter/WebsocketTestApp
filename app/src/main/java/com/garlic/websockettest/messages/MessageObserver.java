package com.garlic.websockettest.messages;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MessageObserver {

    public  static final  int FOREGROUND_ID = 313399;

    private static final String TAG = MessageObserver.class.getSimpleName();
    private final Application context;
    private boolean appVisible;

    public MessageObserver(@NonNull Application context){

        this.context = context;

        // Start thread which will use the websocket connection to receive messages
        new MessageRetrievalThread().start();

        ContextCompat.startForegroundService(context, new Intent(context, MessageObserver.ForegroundService.class));

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

    private synchronized void waitForConnectionPossible() {
        try {
            while (!hasNetwork()) wait();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private synchronized boolean hasNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo   = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class MessageRetrievalThread extends Thread  implements Thread.UncaughtExceptionHandler{

        MessageRetrievalThread() {
            super("MessageRetrievalService");
            setUncaughtExceptionHandler(this);
        }

        @Override
        public void run(){

            while(true){
                Log.i(TAG, "Waiting for websocket state change....");
                waitForConnectionPossible();

                Log.i(TAG, "Making websocket connection....");

                try{
                    while(hasNetwork()){
                        //TODO: get messages from websocket and setup job to handle it
                    }
                }catch (Throwable e){
                    Log.w(TAG, e);
                }finally {
                    //TODO: Shutdown message handling
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
