/* mbed Microcontroller Library
 * Copyright (c) 2006-2013 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __BLE_LED_SERVICE_H__
#define __BLE_LED_SERVICE_H__

#include "ble/BLE.h"
#include <cstdint>

// client can write true or false to the service to control LED2
class SensorService {
public:
    const static uint16_t SENSOR_SERVICE_UUID              = 0xA000;
    const static uint16_t SENSOR_ON_CHARACTERISTIC_UUID    = 0xA001;
    const static uint16_t SENSOR_STATE_CHARACTERISTIC_UUID = 0xA002;
    const static uint16_t CCCD_UUID                        = 0x2902;
    bool sensorOnInitialValue = false;
    bool sensorStateInitialValue = false;

    SensorService(
        BLE& _ble
    ) :
        ble(_ble),
        sensorOn(
            SENSOR_ON_CHARACTERISTIC_UUID,
            &sensorOnInitialValue
        ),
        sensorState(
            SENSOR_STATE_CHARACTERISTIC_UUID,
            &sensorStateInitialValue,
            GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY,
            &cccd,
            1
        )
    {
        cccd = new GattAttribute(CCCD_UUID, (uint8_t*)(cccdValue), 2, 2, false);

        GattCharacteristic *charTable[] = {&sensorOn, &sensorState};
        GattService         sensorService(SENSOR_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic*));

        ble.gattServer().addService(sensorService);
    }

    GattAttribute::Handle_t getValueHandle() const {
        return sensorOn.getValueHandle();
    }
    void updateSensorState(bool newState) {
        ble.gattServer().write(sensorState.getValueHandle(), (uint8_t*)(&newState), 1);
    }

private:
    BLE                               &ble;
    ReadWriteGattCharacteristic<bool> sensorOn;     // start sensors on rising edge
    ReadOnlyGattCharacteristic<bool>  sensorState;  // hold true for TODO period for motion detection
    GattAttribute*                    cccd;
    uint8_t                           cccdValue[2]; // the memory buffer of the attribute. need to persist
};

class LEDService {
public:
    const static uint16_t LED_SERVICE_UUID                 = 0xA100;
    const static uint16_t LED_ON_CHARACTERISTIC_UUID       = 0xA101;
    const static uint16_t LED_STRENGTH_CHARACTERISTIC_UUID = 0xA102;
    bool ledOnInitialValue = false;
    uint8_t ledStrengthInitialValue = 100;

    LEDService(
        BLE& _ble
    ) :
        ble(_ble),
        ledOn(
            LED_ON_CHARACTERISTIC_UUID,
            &ledOnInitialValue
        ),
        ledStrength(
            LED_STRENGTH_CHARACTERISTIC_UUID,
            &ledStrengthInitialValue
        )
    {
        GattCharacteristic *charTable[] = {&ledOn, &ledStrength};
        GattService         ledService(LED_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic*));

        ble.gattServer().addService(ledService);
    }

    GattAttribute::Handle_t getLedOnValueHandle() const {
        return ledOn.getValueHandle();
    }
    GattAttribute::Handle_t getLedStrengthValueHandle() const {
        return ledStrength.getValueHandle();
    }

private:
    BLE                                  &ble;
    ReadWriteGattCharacteristic<bool>    ledOn;
    ReadWriteGattCharacteristic<uint8_t> ledStrength; // 0 to 100 (0x64)
};

class MotorService {
public:
    const static uint16_t MOTOR_SERVICE_UUID           = 0xA200;
    const static uint16_t MOTOR_ON_CHARACTERISTIC_UUID = 0xA201;
    bool motorOnInitialValue = false;

    MotorService(
        BLE& _ble
    ) :
        ble(_ble),
        motorOn(
            MOTOR_SERVICE_UUID,
            &motorOnInitialValue
        )
    {
        GattCharacteristic *charTable[] = {&motorOn};
        GattService         motorService(MOTOR_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic*));

        ble.gattServer().addService(motorService);
    }

    GattAttribute::Handle_t getValueHandle() const {
        return motorOn.getValueHandle();
    }

private:
    BLE                               &ble;
    ReadWriteGattCharacteristic<bool> motorOn;
};

#endif /* #ifndef __BLE_LED_SERVICE_H__ */
