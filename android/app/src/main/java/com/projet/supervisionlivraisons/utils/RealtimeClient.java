package com.projet.supervisionlivraisons.utils;

import android.util.Log;

import com.projet.supervisionlivraisons.BuildConfig;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Thin wrapper around the Socket.IO Java client.
 * The token from {@link SessionManager} is sent in the handshake auth payload.
 */
public class RealtimeClient {

    private static final String TAG = "RealtimeClient";
    private static volatile RealtimeClient INSTANCE;
    private final Socket socket;

    private RealtimeClient(String token) throws URISyntaxException {
        IO.Options opts = new IO.Options();
        opts.auth = new java.util.HashMap<>();
        opts.auth.put("token", token);
        this.socket = IO.socket(BuildConfig.API_BASE_URL, opts);
        this.socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "connected"));
        this.socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.w(TAG, "connect_error: " + args[0]));
    }

    public static RealtimeClient connect(String token) {
        if (INSTANCE == null) {
            synchronized (RealtimeClient.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new RealtimeClient(token);
                        INSTANCE.socket.connect();
                    } catch (URISyntaxException e) {
                        Log.e(TAG, "bad URI", e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    public void on(String event, Emitter.Listener listener) { socket.on(event, listener); }
    public void off(String event)                            { socket.off(event); }

    public static void disconnect() {
        if (INSTANCE != null) {
            INSTANCE.socket.disconnect();
            INSTANCE = null;
        }
    }
}
