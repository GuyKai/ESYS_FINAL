package com.example.sample2.Main;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sample2.Alarm.AlarmActivity;
import com.example.sample2.BLE.BLEActivity;
import com.example.sample2.BLE.ScannedData;
import com.example.sample2.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    final static private String TAG = "Main Activity";

    ArrayList<ScannedData> SavedDevice = new ArrayList<>();
    DeviceAdapter mAdapter;

    private LoadingDialog mLoadingDialog;

    private TextView mTextViewResult;
    private FloatingActionButton mButtonAddDevice;

    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewResult = findViewById(R.id.textview_result);
        mButtonAddDevice = findViewById(R.id.button_addDevice);

        mLoadingDialog = new LoadingDialog(MainActivity.this);

        recyclerView = findViewById(R.id.recyclerView_SavedList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, true));
        mAdapter = new DeviceAdapter(this);
        recyclerView.setAdapter(mAdapter);

        mAdapter.OnItemClick(itemClick);
        mButtonAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BLEActivity.class);
                activityLauncher.launch(intent);

                Toast.makeText(MainActivity.this, "go to device list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG,"onActivityResult :");

                    if (result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        if(intent != null){
                            ScannedData selectedDevice = (ScannedData) intent.getSerializableExtra("DEVICE");

                            String name = selectedDevice.getDeviceName();
                            mTextViewResult.setText(name);

                            SavedDevice.add(selectedDevice);

                            //Toast.makeText(MainActivity.this, "Dvice " + name + " added"+SavedDevice.size(), Toast.LENGTH_SHORT).show();

                            ArrayList newList = getSingle(SavedDevice);
                            try {
                                mAdapter.addDevice(newList);
                                SavedDevice = newList;
                            }
                            catch (ConcurrentModificationException e){

                            }
                        }
                    }
                }
            }
    );

    private ArrayList getSingle(ArrayList list) {
        ArrayList tempList = new ArrayList<>();
        try {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!tempList.contains(obj)) {
                    tempList.add(obj);
                } else {
                    tempList.set(getIndex(tempList, obj), obj);
                }
            }
            return tempList;
        } catch (ConcurrentModificationException e) {
            return tempList;
        }
    }


    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }


    private DeviceAdapter.OnItemClick itemClick = new DeviceAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {


            /*
            Intent intent = new Intent(MainActivity.this, RefreshActivity.class);
            intent.putExtra("GET_DEVICE",selectedDevice);
            activityLauncher.launch(intent);
            */

            Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
            intent.putExtra("GET_DEVICE",selectedDevice);
            startActivity(intent);


            //mLoadingDialog.startLoading();
            /*
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mLoadingDialog.dismissDialog();

                    //ScannedData device = DevicereFresh(selectedDevice);

                    Intent intent = new Intent(MainActivity.this, BLEActivity.class);
                    //Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                    intent.putExtra("GET_DEVICE",selectedDevice);
                    startActivity(intent);

                }
            },2000);
            */

        }
    };

/*
    ActivityResultLauncher<Intent> activityLauncher2 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG,"onActivityResult :");

                    if (result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        if(intent != null){

                            String address = (String) intent.getStringExtra("ADDRESS");


                            Toast.makeText(MainActivity.this, "mAddress " + address , Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );
*/
    /*
    public ScannedData DevicereFresh(ScannedData selected) {

        ScannedData device;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ArrayList<ScannedData> findDevice = new ArrayList<>();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBluetoothAdapter.startLeScan(mLeScanCallback);


        return device;
    }
    */


    /*
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**如果裝置沒有名字，就不顯示*//*
                if (device.getName()!= null){
                    /**將搜尋到的裝置加入陣列*//*
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , byteArrayToHexStr(scanRecord)
                            , device.getAddress()));
                    /**將陣列中重複Address的裝置濾除，並使之成為最新數據*//*
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**將陣列送到RecyclerView列表中*//*
                        mAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };
    */


}