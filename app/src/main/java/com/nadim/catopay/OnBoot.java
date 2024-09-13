package com.nadim.catopay;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class OnBoot extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel nc=new NotificationChannel("running_channel","Running Notificaton", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(nc);
        }
    }
}
