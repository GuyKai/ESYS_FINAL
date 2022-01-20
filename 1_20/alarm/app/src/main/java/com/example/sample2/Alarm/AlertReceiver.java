package com.example.sample2.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {

    public NotificationHelper mNotificationHelper;

    public OnAlertReceived onAlertReceived;



    public AlertReceiver(OnAlertReceived onAlertReceived){
        this.onAlertReceived = onAlertReceived;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationHelper = new NotificationHelper(context);

        NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification1();
        mNotificationHelper.getManger().notify(1,nb.build());

        onAlertReceived.onAlertReceived();

    }

    public interface OnAlertReceived{
        void onAlertReceived();
    }

}
