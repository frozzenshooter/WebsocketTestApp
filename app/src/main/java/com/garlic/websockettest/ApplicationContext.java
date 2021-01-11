package com.garlic.websockettest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.garlic.websockettest.messages.MessageHandler;
import com.garlic.websockettest.messages.MessageObserver;


/**
 * This class extends the {@android.app.Application} class to guarantee that all dependencies are initialized
 */
public class ApplicationContext extends Application  {

    private static final String TAG = ApplicationContext.class.getSimpleName();
    public final static String CHANNEL_ID = "foreground_channel";
    private static final Object LOCK = new Object();

    private static volatile MessageObserver messageObserver;
    private static volatile MessageHandler messageHandler;

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

        // Needed to be able to display the foreground service
        this.createNotificationChannel();

        //TODO: probably better to use a threat to do this
        //initalize the observer for the websocket notifications
        this.initializeMessageRetrieval();

        Log.d(TAG, "onCreate() from ("+ TAG +") took " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void initializeMessageRetrieval(){
        ApplicationContext.getMessageObserver(this);
        ApplicationContext.getMessageHandler(this);
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

    public static @NonNull MessageHandler getMessageHandler(@NonNull Application context) {
        if (messageHandler == null) {
            synchronized (LOCK) {
                if (messageHandler == null) {
                    messageHandler = new MessageHandler(context);
                }
            }
        }
        return messageHandler;
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}
