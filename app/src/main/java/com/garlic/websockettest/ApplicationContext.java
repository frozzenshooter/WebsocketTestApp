package com.garlic.websockettest;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.garlic.websockettest.messages.MessageObserver;


/**
 * This class extends the @android.app.Application class to guarantee that all dependencies are initialized
 */
public class ApplicationContext extends Application implements DefaultLifecycleObserver {


    private static final String TAG = ApplicationContext.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static volatile MessageObserver messageObserver;

    private volatile boolean isAppVisible;

    /**
     * Returns the ApplicationContext of the Application
     * @param context
     * @return current ApplicationContext
     */
    public static ApplicationContext getInstance(Context context) {
        return (ApplicationContext)context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        long startTime = System.currentTimeMillis();

        super.onCreate();

        //TODO: use a threat to do this
        //initalize the observer for the websocket notifications
        this.initializeMessageRetrieval();

        Log.d(TAG, "onCreate() from ("+ TAG +") took " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        isAppVisible = true;
        Log.i(TAG, "App is now visible.");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        isAppVisible = false;
        Log.i(TAG, "App is no longer visible.");
    }

    public void initializeMessageRetrieval(){
        ApplicationContext.getMessageObserver(this);
    }

    public static @NonNull MessageObserver getMessageObserver(@NonNull Application context) {
        if (messageObserver == null) {
            synchronized (LOCK) {
                if (messageObserver == null) {
                    messageObserver = new MessageObserver(context);
                }
            }
        }
        return messageObserver;
    }
}
