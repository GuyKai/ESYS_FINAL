#include "mbed.h"
#include "rtos.h"
#include "ble/BLE.h"

#include <cstdint>
#include <stdio.h>
#include <chrono>

#include <events/mbed_events.h>
#include "services.h"

#include "accelero.h"
#include "motor.h"
#include "heartrate.h"

static EventQueue eventQueue(/* event count */ 10 * EVENTS_EVENT_SIZE);

const static char DEVICE_NAME[] = "ESYS_FINAL";

class MyAlarm : ble::Gap::EventHandler {
public:
    MyAlarm(BLE &ble, events::EventQueue &event_queue) :
        _ble(ble),
        _event_queue(event_queue),
        _sensorService(nullptr),
        _sensorServiceUUID(SensorService::SENSOR_SERVICE_UUID),
        _ledService(nullptr),
        _ledServiceUUID(LEDService::LED_SERVICE_UUID),
        _motorService(nullptr),
        _motorServiceUUID(MotorService::MOTOR_SERVICE_UUID),
        _adv_data_builder(_adv_buffer),
        _alive_led(LED1, 1),
        _eye_led(PB_0),
        _accelero(event_queue, Callback<void(bool)>(this, &MyAlarm::acceleroCallback)),
        _motor(event_queue, PA_3) { }

    ~MyAlarm() {
        delete _sensorService;
        delete _ledService;
        delete _motorService;
    }

    void start() {
        _ble.gap().setEventHandler(this);
        _ble.init(this, &MyAlarm::on_init_complete);

        printf("starting alive blink\n");
        _event_queue.call_every(500ms, this, &MyAlarm::blink);
        //_event_queue.dispatch_forever();
    }

private:
    /** Callback triggered when the ble initialization process has finished */
    void on_init_complete(BLE::InitializationCompleteCallbackContext *params) {
        if (params->error != BLE_ERROR_NONE) {
            printf("Ble initialization failed.");
            return;
        }

        initEyeLed();

        // constructs and sets initial values for services
        _sensorService = new SensorService(_ble);
        _ledService = new LEDService(_ble);
        _motorService = new MotorService(_ble);

        _ble.gattServer().onDataWritten(this, &MyAlarm::on_data_written);

        //print_mac_address();
        start_advertising();
    }

    void start_advertising() {
        /* Create advertising parameters and payload */

        ble::AdvertisingParameters adv_parameters(
            ble::advertising_type_t::CONNECTABLE_UNDIRECTED,
            ble::adv_interval_t(ble::millisecond_t(1000))
        );

        _adv_data_builder.setFlags();
        _adv_data_builder.setLocalServiceList(mbed::make_Span(&_sensorServiceUUID, 1));
        _adv_data_builder.setLocalServiceList(mbed::make_Span(&_ledServiceUUID, 1));
        _adv_data_builder.setLocalServiceList(mbed::make_Span(&_motorServiceUUID, 1));
        _adv_data_builder.setName(DEVICE_NAME);

        /* Setup advertising */

        ble_error_t error = _ble.gap().setAdvertisingParameters(
            ble::LEGACY_ADVERTISING_HANDLE,
            adv_parameters
        );

        if (error) {
            printf("_ble.gap().setAdvertisingParameters() failed\r\n");
            return;
        }

        error = _ble.gap().setAdvertisingPayload(
            ble::LEGACY_ADVERTISING_HANDLE,
            _adv_data_builder.getAdvertisingData()
        );

        if (error) {
            printf("_ble.gap().setAdvertisingPayload() failed\r\n");
            return;
        }

        /* Start advertising */

        error = _ble.gap().startAdvertising(ble::LEGACY_ADVERTISING_HANDLE);

        if (error) {
            printf("_ble.gap().startAdvertising() failed\r\n");
            return;
        }
    }
    
    void initEyeLed() {
        _eye_led.period_us(_ledPeriod);
    }
    void setEyeLedOn(bool on) {
        _eye_led.pulsewidth_us(on ? _ledPulseWidth : 0);
    }
    void setEyeLedStrength(uint8_t strength) {
        // period = 10000
        // 0 <= strength <= 100
        // max pulsewidth = 100 * f
        // max duty cycle = 100f / 10000 = f%
        _ledPulseWidth = uint16_t(strength) * 100; // f = 100
        _eye_led.pulsewidth_us(_eye_led.read_pulsewitdth_us() ? _ledPulseWidth : 0);
    }
    void acceleroCallback(bool b) {
        _sensorService->updateSensorState(b);
    }

    /**
     * This callback allows services to receive updates to the corresponding characteristics.
     *
     * @param[in] params Information about the characterisitc being updated.
     */
    void on_data_written(const GattWriteCallbackParams *params) {
        if ((params->handle == _sensorService->getValueHandle()) && (params->len == 1)) {
            _accelero.setOn(*(params->data));
        }
        if ((params->handle == _ledService->getLedOnValueHandle()) && (params->len == 1)) {
            setEyeLedOn(*(params->data));
        }
        if ((params->handle == _ledService->getLedStrengthValueHandle()) && (params->len == 1)) {
            setEyeLedStrength(*(params->data));
        }
        if ((params->handle == _motorService->getValueHandle()) && (params->len == 1)) {
            _motor.setOn(*(params->data));
        }
    }

    void blink() {
        _alive_led = !_alive_led;
    }

private:
    /* Event handler */

    void onDisconnectionComplete(const ble::DisconnectionCompleteEvent&) {
        _ble.gap().startAdvertising(ble::LEGACY_ADVERTISING_HANDLE);
    }

private:
    BLE &_ble;
    events::EventQueue &_event_queue;

    SensorService* _sensorService;
    UUID           _sensorServiceUUID;
    LEDService*    _ledService;
    UUID           _ledServiceUUID;
    MotorService*  _motorService;
    UUID           _motorServiceUUID;

    uint8_t _adv_buffer[ble::LEGACY_ADVERTISING_MAX_SIZE];
    ble::AdvertisingDataBuilder _adv_data_builder;

    DigitalOut _alive_led;
    PwmOut     _eye_led;
    Accelero   _accelero;
    Motor      _motor;

    const uint16_t _ledPeriod = 10000;
    uint16_t _ledPulseWidth = 1000;
};

/** Schedule processing of events from the BLE middleware in the event queue. */
void schedule_ble_events(BLE::OnEventsToProcessCallbackContext *context) {
    eventQueue.call(Callback<void()>(&context->ble, &BLE::processEvents));
}

// void onMAX30100Read(int ir) {
//    printf( "%d\n", ir );
// }
//InterruptIn button(BUTTON1);
DigitalOut myled(LED1);
int main()
{
    //while(true);

    Thread t;
    t.start(Callback<void()>(&eventQueue, &EventQueue::dispatch_forever)); // blocks
 
    myled = 1;
    ThisThread::sleep_for(1s);
    myled = 0;

    //Heartrate heartrate(eventQueue, NULL, 30, &onMAX30100Read);
    // button.fall(&buttonFallCallback); // set callback
    ThisThread::sleep_for(100ms); // arbitrary, wait for init

    //heartrate.startReadLoop();
    //ThisThread::sleep_for(3s);
    //heartrate.stopReadLoop();

    BLE &ble = BLE::Instance();
    ble.onEventsToProcess(schedule_ble_events);
    MyAlarm myAlarm(ble, eventQueue);
    myAlarm.start();

    while(true);
}
