package com.example.sample2.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.sample2.BLE.BluetoothLeService;

import java.util.Calendar;

public class AlarmService extends Service {
    public static final String TAG = "MyAlarmService";
    public static int preTime = 20;
    BroadcastReceiver mReceiver;
    private Calendar alarm ;
    private boolean state = false;
    public NotificationHelper mNotificationHelper;
    Handler mHandler;


    private final IBinder mBinder = new AlarmService.LocalBinder();


    @Override
    public void onCreate() {
        mHandler = new Handler();
        IntentFilter filter = new IntentFilter();
        filter.addAction("WAKE_UP");
        filter.addAction("AWAKE");
        filter.addAction("SLEPT");
        filter.addAction("FINISH");
        filter.addAction("LIGHT_UP");
        mReceiver = new mAlarmReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public class LocalBinder extends Binder {
        public AlarmService getService() {
            return AlarmService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG,"onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        //close();
        return true;
    }

    public Calendar getAlarm() {
        return alarm;
    }

    public boolean getState() {
        return state;
    }

    public void startAlarm(Calendar c){
        if (alarm == null){
            this.alarm = c;

            AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("WAKE_UP");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);
            Intent lightIntent = new Intent("LIGHT_UP");
            PendingIntent lightPendingIntent = PendingIntent.getBroadcast(this,2,lightIntent,0);

            if (c.before(Calendar.getInstance())){
                c.add(Calendar.DATE ,1);
            }

            c.add(Calendar.SECOND,-preTime);
            if (c.before(Calendar.getInstance())){
                broadcastUpdate("LIGHT_UP");
            }else {
                mAlarmManager.setExact(AlarmManager.RTC, c.getTimeInMillis(), lightPendingIntent);
            }

            c.add(Calendar.SECOND,preTime);
            mAlarmManager.setExact(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);

            //accelero_on();
            //notify_on();

            state = true;
            broadcastUpdate("ALARM_SET");

        }
    }

    public void cancelAlarm(){
        if (alarm != null){
            AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("WAKE_UP");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);
            mAlarmManager.cancel(pendingIntent);

            alarm = null;
            state = false;
            //broadcastUpdate("ALARM_CANCEL");

            //mTextAlarm.setText("No Alarm set");
        }
        Log.d(TAG, "cancel");
        broadcastUpdate("ALARM_CANCEL");

    }

    final Runnable mRun = new Runnable() {
        @Override
        public void run() {
            broadcastUpdate("FINISH");
        }

    };

    public void awake(){

        mHandler.postDelayed(mRun,10000);
    }


    public void slept(){
        mHandler.removeCallbacks(mRun);

    }

    public class mAlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if ("WAKE_UP".equals(action)) {
                Log.d(TAG, "Time up!");

                mNotificationHelper = new NotificationHelper(context);

                NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification1();
                mNotificationHelper.getManger().notify(1,nb.build());


                alarm = null;
                state = false;

            }

            if ("AWAKE".equals(action)) {
                Log.d(TAG, "Awake!");
                awake();


            }
            if ("SLEPT".equals(action)) {
                Log.d(TAG, "Slept!");
                slept();



            }
            if ("FINISH".equals(action)) {
                Log.d(TAG, "Finish!");

                mNotificationHelper = new NotificationHelper(context);

                NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification2();
                mNotificationHelper.getManger().notify(1,nb.build());

                cancelAlarm();


            }

            if ("LIGHT_UP".equals(action)) {
                Log.d(TAG, "Light Up!");

                mNotificationHelper = new NotificationHelper(context);

                NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification3();
                mNotificationHelper.getManger().notify(1,nb.build());


            }


        }
    }

    /**更新廣播*/
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


}