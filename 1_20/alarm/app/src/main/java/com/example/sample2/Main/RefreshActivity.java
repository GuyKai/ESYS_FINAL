package com.example.sample2.Main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import RecyclerViewAdapter;
import com.example.sample2.BLE.ScannedData;
import com.example.sample2.R;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class RefreshActivity extends AppCompatActivity {
    private static final String TAG = RefreshActivity.class.getSimpleName()+"My";
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning = false;
    ArrayList<ScannedData> findDevice = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        checkPermission();
        bluetoothScan();

    }
    /**權限相關認證*/
    private void checkPermission() {
        /**確認手機版本是否在API18以上，否則退出程式*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            /**確認是否已開啟取得手機位置功能以及權限*/
            int hasGone = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasGone != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
            }
            /**確認手機是否支援藍牙BLE*/
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this,"Not support Bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
            /**開啟藍芽適配器*/
            if(!mBluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            }
        }else finish();
    }
    /**初始藍牙掃描及掃描開關之相關功能*/
    private void bluetoothScan() {
        /**啟用藍牙適配器*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        /**開始掃描*/
        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Button btScan = findViewById(R.id.button_Scan);
        findDevice.clear();
        mBluetoothAdapter.startLeScan(mLeScanCallback);

        Toast.makeText(RefreshActivity.this, "thread running" , Toast.LENGTH_SHORT).show();
    }


    /*

    new Thread(()-> {

            ScannedData selectedDevice = (ScannedData) getIntent().getSerializableExtra("GET_DEVICE");
            String name =  selectedDevice.getDeviceName();
            String address = "0";

            while (address == "0") {
                address = getAddress(findDevice, name);
            }

            Toast.makeText(RefreshActivity.this, "add: " + address, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra("ADDRESS", address);
            setResult(RESULT_OK, intent);

            finish();
        }).start();
     */

    /**避免跳轉後掃描程序係續浪費效能，因此離開頁面後即停止掃描*/
    @Override
    protected void onStop() {
        super.onStop();
        /**關閉掃描*/
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    /**顯示掃描到物件*/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**如果裝置沒有名字，就不顯示*/
                if (device.getName()!= null){
                    /**將搜尋到的裝置加入陣列*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , byteArrayToHexStr(scanRecord)
                            , device.getAddress()));
                    /**將陣列中重複Address的裝置濾除，並使之成為最新數據*/
                    ArrayList newList = getSingle(findDevice);
                }
            }).start();
        }
    };

    /**濾除重複的藍牙裝置(以Address判定)*/
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
    /**
     * 以Address篩選陣列->抓出該值在陣列的哪處
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    private String getAddress(ArrayList<ScannedData> list, String name) {
        for (ScannedData D :list ){
            if (D.getDeviceName() == name){
                return D.getAddress();
            }
            return "0";
        }
        return "0";
    }

    /**
     * Byte轉16進字串工具
     */
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        StringBuilder hex = new StringBuilder(byteArray.length * 2);
        for (byte aData : byteArray) {
            hex.append(String.format("%02X", aData));
        }
        String gethex = hex.toString();
        return gethex;
    }

}