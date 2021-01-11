package com.garlic.websockettest.websockets;

import android.app.NotificationManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.garlic.websockettest.ApplicationContext;
import com.garlic.websockettest.R;
import com.garlic.websockettest.messages.MessageHandler;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketConnection extends WebSocketListener {

    private static final String TAG = WebSocketConnection.class.getSimpleName();
    private static final int KEEPALIVE_TIMEOUT_SECONDS = 55;

    private final String wsUri;
    private boolean connected;

    private WebSocket client;
    private final ApplicationContext context;
    private final MessageHandler messageHandler;


    public WebSocketConnection(String uri, ApplicationContext context){
        this.wsUri = uri;
        this.connected = false;
        this.context = context;
        this.messageHandler = new MessageHandler(context);
    }

    public synchronized void connect(){
        Log.i(TAG, "connect()");

        if(client == null){
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .readTimeout(KEEPALIVE_TIMEOUT_SECONDS + 10, TimeUnit.SECONDS)
                    .connectTimeout(KEEPALIVE_TIMEOUT_SECONDS + 10, TimeUnit.SECONDS);

            OkHttpClient okHttpClient = clientBuilder.build();

            Request.Builder requestBuilder = new Request.Builder().url(this.wsUri);

            this.connected = false;
            this.client    = okHttpClient.newWebSocket(requestBuilder.build(), this);
        }
    }

    public synchronized void disconnect() {
        Log.i(TAG, "disconnect()");

        if (client != null) {
            client.close(1000, "OK");
            client    = null;
            connected = false;
        }
    }

    //-------------------------------------------------------------
    // WebSocketListener overrides
    //-------------------------------------------------------------

    /**
     * Invoked when a web socket has been accepted by the remote peer and may begin transmitting
     * messages.
     */
    @Override
    public synchronized void onOpen(WebSocket webSocket, Response response) {
        if(client != null){
            connected = true;
        }
    }

    /** Invoked when a text (type {@code 0x1}) message has been received. */
    @Override
    public synchronized void onMessage(WebSocket webSocket, String text) {
        Log.i(TAG, "onMessage("+text+")");
        try{
            this.messageHandler.addMessage(Double.valueOf(text));
        } catch(NumberFormatException ex) {
            Log.i(TAG, "onMessage("+text+") failed to parse to Double");
        }
    }

    /** Invoked when a binary (type {@code 0x2}) message has been received. */
    @Override
    public synchronized void onMessage(WebSocket webSocket, ByteString bytes) {

        String text = bytes.toByteArray().toString();
        Log.i(TAG, "onMessage("+text+") - from Bytes");
    }

    @Override
    public synchronized void onClosing(WebSocket webSocket, int code, String reason) {
        Log.i(TAG, "onClosing()");
        webSocket.close(1000, "OK");
    }

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the
     * connection has been successfully released. No further calls to this listener will be made.
     */
    @Override
    public synchronized void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(TAG, "onClose()");
        this.connected = false;

        if (client != null) {
            client.close(1000, "OK");
            client    = null;
            connected = false;
        }
    }

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the
     * network. Both outgoing and incoming messages may have been lost. No further calls to this
     * listener will be made.
     */
    @Override
    public synchronized void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        Log.w(TAG, "onFailure()", t);

        if (client != null) {
            onClosed(webSocket, 1000, "OK");
        }
    }
}
