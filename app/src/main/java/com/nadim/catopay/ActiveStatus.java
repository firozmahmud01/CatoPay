package com.nadim.catopay;

import static java.util.Collections.singletonMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ActiveStatus extends Service {
    public static boolean isrunning=false;
    public ActiveStatus() {
    }

    Socket socket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isrunning=true;
        try {
            SharedPreferences sp= getSharedPreferences(ApiCaller.database, Context.MODE_PRIVATE);
            String token=sp.getString("token",null);
            IO.Options options = IO.Options.builder()
                    .setAuth(singletonMap("token", token))
                    .build();
            socket = IO.socket(URI.create("https://api.catopay.com"),options);
        } catch (Exception e) {
            int a=10;
        }
        socket.connect();




        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int a=120;
            }
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int b=10;
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int c=103;
            }
        });







        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        try {
            socket.disconnect();
            socket.close();
        }catch (Exception e){}
        socket=null;
        super.onDestroy();
        isrunning=false;

    }
}