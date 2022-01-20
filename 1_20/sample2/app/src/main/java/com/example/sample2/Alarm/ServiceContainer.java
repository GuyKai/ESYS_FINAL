package com.example.sample2.Alarm;

import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.example.sample2.BLE.ServiceInfo;


import java.util.ArrayList;
import java.util.List;


public class ServiceContainer {
    private List<ServiceInfo> serviceInfo = new ArrayList<>();

    public void setServiceInfo(List<BluetoothGattService> services){
        for (BluetoothGattService s: services) {
            this.serviceInfo.add(new ServiceInfo(s));
        }
    }

    public ServiceInfo.CharacteristicInfo getInfo (int service,int characteristic){
        return serviceInfo.get(service).getCharacteristicInfo().get(characteristic);
    }


    public void showGattAtLogCat(){
        Log.d(AlarmActivity.TAG, "Service size: "+serviceInfo.size());
        for (ServiceInfo service : serviceInfo){
            Log.d(AlarmActivity.TAG, "Characteristic size: "+ service.getCharacteristicInfo().size());
            Log.d(AlarmActivity.TAG, "Service: "+service.getUuid().toString());
            for (ServiceInfo.CharacteristicInfo characteristic : service.getCharacteristicInfo()){
                Log.d(AlarmActivity.TAG, "descriptor size: "+ characteristic.getDescriptorsInfo().size());
                Log.d(AlarmActivity.TAG, "\tCharacteristic: "+characteristic.getUuid().toString());
                for (ServiceInfo.CharacteristicInfo.DescriptorsInfo descriptor : characteristic.getDescriptorsInfo()){
                    Log.d(AlarmActivity.TAG, "\t\tDescriptor: "+descriptor.getUuid().toString());
                }
            }
        }
    }



}
