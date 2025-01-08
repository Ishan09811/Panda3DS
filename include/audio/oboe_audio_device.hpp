#pragma once

#include <oboe/Oboe.h>
#include <array>
#include <vector>
#include <string>

#include "helpers.hpp"
#include "ring_buffer.hpp"
#include "config.hpp"

class OboeAudioDevice : public oboe::AudioStreamCallback {
    using Samples = Common::RingBuffer<int16_t, 0x2000 * 2>;
    static constexpr int32_t sampleRate = 32768;  // 3DS sample rate
    static constexpr int32_t channelCount = 2;    // Stereo output

    oboe::AudioStream* audioStream = nullptr;
    Samples* samples = nullptr;

    const AudioDeviceConfig& audioSettings;
    bool initialized = false;
    bool running = false;

    std::array<int16_t, 2> lastStereoSample = {0, 0};
    std::vector<std::string> audioDevices;

public:
    OboeAudioDevice(const AudioDeviceConfig& audioSettings);
    ~OboeAudioDevice();

    void init(Samples& samples, bool safe = false);
    void close();

    void start();
    void stop();

    bool isInitialized() const { return initialized; }

    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* oboeStream, void* audioData, int32_t numFrames) override;
};
