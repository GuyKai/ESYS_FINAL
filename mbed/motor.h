#include <stdio.h>
#include <chrono>
#include "mbed.h"
#include <events/mbed_events.h>

class Motor {
public:
    Motor(EventQueue& eventQueue, DigitalOut motorControl):
        eventQueue(eventQueue),
        motorControl(motorControl) { }
    ~Motor() {
        stop();
    }
    void setOn(bool on) {
        if (on) start(); else stop();
    }

private:
    void start() {
        stop();
        printf("motor start\n");
        eventId = eventQueue.call_every(TOGGLE_PERIOD, this, &Motor::pulse);
    }
    void stop() {
        if (eventId) {
            printf("motor stop\n");
            motorControl = 0;
            eventQueue.cancel(eventId);
            eventId = 0;
        }
    }
    void pulse() {
        motorControl = !motorControl;
    }

private:
    const static auto constexpr TOGGLE_PERIOD = std::chrono::milliseconds(1000);

    EventQueue& eventQueue;
    DigitalOut  motorControl;
    int         eventId = 0; // valid values will not be 0
};
