#include <stdio.h>
#include <chrono>
#include <cstdint>
#include "mbed.h"
#include <events/mbed_events.h>
#include "stm32l475e_iot01_accelero.h"

class Accelero {
private:
    struct vec2 {
        int16_t y;
        int16_t z;
    };

public:
    Accelero(EventQueue& eventQueue, Callback<void(bool)> detectCallback):
        eventQueue(eventQueue),
        detectCallback(detectCallback)
    {
        BSP_ACCELERO_Init();
        BSP_ACCELERO_LowPower(1); // pass value other than 0 to enable low power
    }
    ~Accelero() {
        stop();
        BSP_ACCELERO_DeInit();
    }
    void setOn(bool on) {
        if (on) start(); else stop();
    }

private:
    void start() {
        stop();
        printf("accelero start\n");
        eventId = eventQueue.call_every(PERIOD, this, &Accelero::callback);
    }
    void stop() {
        if (eventId) {
            printf("accelero stop\n");
            eventQueue.cancel(eventId);
            eventId = 0;
        }
    }
    void callback() {
        int16_t pDataXYZ[3];
        BSP_ACCELERO_AccGetXYZ(pDataXYZ);
        eventQueue.call(detectCallback, (pDataXYZ[1] > 700));

        // sample();
        // detect();
    }
    // void sample() {
    //     int16_t pDataXYZ[3];
    //     BSP_ACCELERO_AccGetXYZ(pDataXYZ);
    //     push({pDataXYZ[1], pDataXYZ[2]});
    // }
    // void push(vec2 data) {
    //     cb[cbWritePointer] = data;
    //     cbWritePointer = mod8(cbWritePointer + 1);
    // }
    // void detect() {
    //     if (skipDetectCount) {
    //         --skipDetectCount;
    //         eventQueue.call(detectCallback, false);
    //         return;
    //     }

    //     int32_t v = (
    //         unitDotSquared64Signed(cb[cbWritePointer], cb[mod8(cbWritePointer + 4)]) +
    //         unitDotSquared64Signed(cb[mod8(cbWritePointer + 1)], cb[mod8(cbWritePointer + 5)]) +
    //         unitDotSquared64Signed(cb[mod8(cbWritePointer + 2)], cb[mod8(cbWritePointer + 6)]) +
    //         unitDotSquared64Signed(cb[mod8(cbWritePointer + 3)], cb[mod8(cbWritePointer + 7)])
    //     );
    //     const int32_t THRESHOLD = 150;
    //     eventQueue.call(detectCallback, (v < THRESHOLD));
    // }

    // static uint32_t mod8(uint32_t x) {
    //     return x & 7;
    // }
    // static int32_t unitDotSquared64Signed(vec2 v1, vec2 v2) {
    //     // (v1.v2)/(|v1|*|v2|) ^ 2
    //     int32_t l1square = (v1.y * v1.y + v1.z * v1.z) >> 10; // divide by 1024 to prevent overflow
    //     int32_t l2square = (v2.y * v2.y + v2.z * v2.z) >> 10; // divide by 1024 to prevent overflow
    //     int32_t dp8 = dot(v1, v2) >> 7;                       // (1024 / 8) i.e. 2 ^ (10 - 3)
    //     return dp8 * dp8 * sign(dp8) / (l1square * l2square);
    // }
    // static int32_t dot(vec2 v1, vec2 v2) {
    //     return v1.y * v2.y + v1.z * v2.z;
    // }
    // static int32_t sign(int32_t x) {
    //     // note that right shift is arithmetic (signed)
    //     return 1 + ((x & (1 << 31)) >> 30);
    // }

private:
    const static int              CB_SIZE = 8;
    const static auto constexpr   PERIOD = std::chrono::milliseconds(1000);

    EventQueue&          eventQueue;
    int                  eventId = 0;               // valid values will not be 0
    Callback<void(bool)> detectCallback;
    // vec2                 cb[CB_SIZE] = {};
    // uint32_t             cbWritePointer = 0;
    // int                  skipDetectCount = CB_SIZE; // skip detection before cb is filled
};
