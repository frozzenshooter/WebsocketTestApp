package com.garlic.websockettest.messages;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MessageObserver {

    private static final String TAG = MessageObserver.class.getSimpleName();
    private final Application context;
    private boolean appVisible;

    public MessageObserver(@NonNull Application context){

        this.context = context;

        // Start thread which will use the websocket connection to receive messages
        new MessageRetrievalThread().start();

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
}
