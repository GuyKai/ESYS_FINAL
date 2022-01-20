package com.example.sample2.Alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import com.example.sample2.BLE.ServiceInfo;
import com.example.sample2.Main.LoadingDialog;
import com.example.sample2.Main.MainActivity;
import com.example.sample2.R;

import com.example.sample2.BLE.BluetoothLeService;
import com.example.sample2.BLE.ScannedData;

public class AlarmActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener  {
    public static final String TAG = AlarmActivity.class.getSimpleName()+"My";
    public static final int SELECTED_SERVICE = 2;
    public static final int SCHARACTERISTIC_ACCELERO = 0 ;
    public static final int CHARACTERISTIC_NOTIFY = 1 ;


    private TextView mTextAlarm;
    private Button mButtonSetAlarm;
    private Button mButtonCancelAlarm;
    private BluetoothLeService mBluetoothLeService;
    private ScannedData selectedDevice;
    private AlarmService mAlarmService;
    private LoadingDialog mLoadingDialog;
    public NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mTextAlarm = findViewById(R.id.text_alarm);
        mButtonSetAlarm = findViewById(R.id.button_setAlarm);
        mButtonCancelAlarm = findViewById(R.id.button_cancelAlarm);
        mLoadingDialog = new LoadingDialog(AlarmActivity.this);

        mButtonSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");

            }
        });
        mButtonCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
                //cancelble();
                //accelero_off();
            }
        });

        selectedDevice = (ScannedData) getIntent().getSerializableExtra("GET_DEVICE");
        Toast.makeText(AlarmActivity.this, selectedDevice.getDeviceName(), Toast.LENGTH_SHORT ).show();

        initBLE();
        initAlarm();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mLoadingDialog.startLoading();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**取消註冊廣播*/
        unregisterReceiver(mAlertReceiver);
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        unbindService(mAlarmServiceConnection);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,i);
        c.set(Calendar.MINUTE,i1);
        c.set(Calendar.SECOND,0);

        updateAlarmText(c);
        startAlarm(c);
    }

    private void updateAlarmText(Calendar c){
        String alarmText = "";
        alarmText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        mTextAlarm.setText(alarmText);
    }


    private void initAlarm(){
        Intent alarmService = new Intent(this, AlarmService.class);
        startService(alarmService);
        bindService(alarmService,mAlarmServiceConnection,BIND_AUTO_CREATE);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("WAKE_UP");
        registerReceiver(mAlertReceiver, intentFilter);

    }

    private  void  startAlarm(Calendar c){
        if (!mAlarmService.getState()){
            mAlarmService.startAlarm(c);
            //notify_on();

        }
        else{
            Toast.makeText(this,"Alarm already set!!, Please cancel alarm and try again",Toast.LENGTH_SHORT).show();
        }


    }

    public void cancelAlarm(){
        if (mAlarmService.getState()){

            mAlarmService.cancelAlarm();

            mTextAlarm.setText("No Alarm set");
        }
        else{
            Toast.makeText(this ,"No alarm set",Toast.LENGTH_SHORT).show();
            mAlarmService.cancelAlarm();
        }


    }

    public void getAlarm(){
        if (mAlarmService.getState()) {
            Log.d(TAG, "GetAlarm");
            updateAlarmText(mAlarmService.getAlarm());
        }
    }


    private ServiceConnection mAlarmServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mAlarmService = ((AlarmService.LocalBinder) service).getService();
            getAlarm();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void accelero_on(){
        //byte[] msg = {(byte) 1,(byte)0 };
        //String msg ="\41";
        byte[] msg = {0x01};
        //String msg ="1";
        ServiceInfo.CharacteristicInfo info = mBluetoothLeService.getGatt(SELECTED_SERVICE,SCHARACTERISTIC_ACCELERO);
        mBluetoothLeService.sendValue(msg,info.getCharacteristic());

    }

    public void accelero_off() {
        byte[] msg = {0x00};
        ServiceInfo.CharacteristicInfo info = mBluetoothLeService.getGatt(SELECTED_SERVICE, SCHARACTERISTIC_ACCELERO);
        mBluetoothLeService.sendValue(msg, info.getCharacteristic());

    }

    public void notify_on(){
        Toast.makeText(AlarmActivity.this, "setting notify", Toast.LENGTH_SHORT).show();
        ServiceInfo.CharacteristicInfo info = mBluetoothLeService.getGatt(SELECTED_SERVICE,CHARACTERISTIC_NOTIFY);
        mBluetoothLeService.setCharacteristicNotification(info.getCharacteristic(),true);

    }

    public void notify_off(){
        Toast.makeText(AlarmActivity.this, "turnoff notify", Toast.LENGTH_SHORT).show();
        ServiceInfo.CharacteristicInfo info = mBluetoothLeService.getGatt(SELECTED_SERVICE,CHARACTERISTIC_NOTIFY);
        mBluetoothLeService.setCharacteristicNotification(info.getCharacteristic(),false);

    }

    public void onAlertReceived(){
        mTextAlarm.setText("No Alarm set");

        //accelero_on();
        //notify_on();

    }

/*
    public static class AlertReceiver extends BroadcastReceiver {

        public NotificationHelper mNotificationHelper;

        @Override
        public void onReceive(Context context, Intent intent) {
            mNotificationHelper = new NotificationHelper(context);

            NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification();
            mNotificationHelper.getManger().notify(1,nb.build());

            onAlertReceived();
            //mServiceContainer.showGattAtLogCat();

        }


    }

 */

    private final BroadcastReceiver mAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if ("WAKE_UP".equals(action)) {
                Log.d(TAG, "Time up!");
                /*

                mNotificationHelper = new NotificationHelper(context);

                NotificationCompat.Builder nb = mNotificationHelper.getChannelNotification();
                mNotificationHelper.getManger().notify(1,nb.build());

                 */

                onAlertReceived();



            }

        }
    };

//--------------------------------------------------------------------------------

    /**初始化藍芽*/
    private void initBLE(){
        /**綁定Service
         * @see BluetoothLeService*/
        Intent bleService = new Intent(this, BluetoothLeService.class);
        startService(bleService);
        bindService(bleService,mServiceConnection,BIND_AUTO_CREATE);
        /**設置廣播*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);//連接一個GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);//從GATT服務中斷開連接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);//查找GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收)數據

        registerReceiver(mGattUpdateReceiver, intentFilter);
        //Toast.makeText(AlarmActivity.this, selectedDevice.getAddress(), Toast.LENGTH_SHORT ).show();

        if (mBluetoothLeService != null) {
            //Toast.makeText(AlarmActivity.this, selectedDevice.getAddress(), Toast.LENGTH_SHORT).show();
            mBluetoothLeService.connect(selectedDevice.getAddress());

        }

    }

    /**藍芽已連接/已斷線資訊回傳*/
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(selectedDevice.getAddress());
            if (mBluetoothLeService.getGattSet()){
                mLoadingDialog.dismissDialog();//
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService.disconnect();
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            /**如果有連接*/
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "藍芽已連線");
                Toast.makeText(AlarmActivity.this,"Connected",Toast.LENGTH_SHORT).show();


            }
            /**如果沒有連接*/
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "藍芽已斷開");
                Toast.makeText(AlarmActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();
                try {
                    mBluetoothLeService.reconnect();
                }
                catch (Error e){

                }

            }
            /**找到GATT服務*/
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "已搜尋到GATT服務");
                List<BluetoothGattService> gattList =  mBluetoothLeService.getSupportedGattServices();
                displayGattAtLogCat(gattList);
                mBluetoothLeService.setGatt(gattList);
                mLoadingDialog.dismissDialog();//
            }
            /**接收來自藍芽傳回的資料*/
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "接收到藍芽資訊");
                byte[] getByteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                StringBuilder stringBuilder = new StringBuilder(getByteData.length);
                for (byte byteChar : getByteData)
                    stringBuilder.append(String.format("%02X ", byteChar));
                String stringData = new String(getByteData);
                Log.d(TAG, "String: "+stringData+"\n"
                        +"byte[]: "+BluetoothLeService.byteArrayToHexStr(getByteData));
                //isLedOn = BluetoothLeService.byteArrayToHexStr(getByteData).equals("486173206F6E");

            }
        }
    };

    /**將藍芽所有資訊顯示在Logcat*/
    private void displayGattAtLogCat(List<BluetoothGattService> gattList){
        for (BluetoothGattService service : gattList){
            Log.d(TAG, "Service: "+service.getUuid().toString());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                Log.d(TAG, "\tCharacteristic: "+characteristic.getUuid().toString()+" ,Properties: "+
                        mBluetoothLeService.getPropertiesTagArray(characteristic.getProperties()));
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                    Log.d(TAG, "\t\tDescriptor: "+descriptor.getUuid().toString());
                }
            }
        }
    }

    private void closeBluetooth() {
        if (mBluetoothLeService == null) return;
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
    }

    public void cancelble(){
        Intent bleService = new Intent(this, BluetoothLeService.class);
        stopService(bleService);
    }

}