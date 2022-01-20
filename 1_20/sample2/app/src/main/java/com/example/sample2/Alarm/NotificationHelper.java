package com.example.sample2.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.sample2.R;

public class NotificationHelper extends ContextWrapper {
    public static final String channel1ID = "channel1ID";
    public static final String channel1Name = "CHANNEL 1";

    private NotificationManager mManger;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel channel1 = new NotificationChannel(channel1ID, channel1Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.design_default_color_primary);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManger().createNotificationChannel(channel1);

    }

    public NotificationManager getManger() {
        if (mManger == null){
            mManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManger;
    }

    public NotificationCompat.Builder getChannelNotification1() {
        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("ALARM")
                .setContentText("WAKE UP!!!")
                .setSmallIcon(R.drawable.ic_one);
    }

    public NotificationCompat.Builder getChannelNotification2() {
        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("ALARM")
                .setContentText("FINISH!!!")
                .setSmallIcon(R.drawable.ic_one);
    }

    public NotificationCompat.Builder getChannelNotification3() {
        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("ALARM")
                .setContentText("LIGHT UP!!!")
                .setSmallIcon(R.drawable.ic_one);
    }

}
