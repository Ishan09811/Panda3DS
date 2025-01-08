#include "audio/miniaudio_device.hpp"

OboeAudioDevice::OboeAudioDevice(const AudioDeviceConfig& audioSettings)
    : audioSettings(audioSettings) {}

OboeAudioDevice::~OboeAudioDevice() {
    close();
}

void OboeAudioDevice::init(Samples& samples, bool safe) {
    this->samples = &samples;
    running = false;

    oboe::AudioStreamBuilder builder;
    builder.setFormat(oboe::AudioFormat::I16)
           .setChannelCount(channelCount)
           .setSampleRate(sampleRate)
           .setCallback(this)
           .setPerformanceMode(oboe::PerformanceMode::LowLatency)
           .setUsage(oboe::Usage::Game)
           .setSharingMode(oboe::SharingMode::Exclusive);

    oboe::Result result = builder.openStream(&audioStream);
    if (result != oboe::Result::OK) {
        Helpers::warn("Failed to open Oboe stream");
        initialized = false;
        return;
    }

    initialized = true;
}

void OboeAudioDevice::start() {
    if (!initialized) {
        Helpers::warn("OboeAudioDevice not initialized, won't start");
        return;
    }

    if (!running) {
        oboe::Result result = audioStream->requestStart();
        if (result == oboe::Result::OK) {
            running = true;
        } else {
            Helpers::warn("Failed to start Oboe stream");
        }
    }
}

void OboeAudioDevice::stop() {
    if (!initialized) {
        Helpers::warn("OboeAudioDevice not initialized, can't stop");
        return;
    }

    if (running) {
        oboe::Result result = audioStream->requestStop();
        if (result != oboe::Result::OK) {
            Helpers::warn("Failed to stop Oboe stream");
        } else {
            running = false;
        }
    }
}

void OboeAudioDevice::close() {
    stop();

    if (initialized) {
        audioStream->close();
        initialized = false;
    }
}

oboe::DataCallbackResult OboeAudioDevice::onAudioReady(
    oboe::AudioStream* oboeStream, void* audioData, int32_t numFrames) {

    if (!running) {
        return oboe::DataCallbackResult::Stop;
    }

    auto* output = static_cast<int16_t*>(audioData);
    std::size_t samplesWritten = samples->pop(output, numFrames * channelCount);

    if (samplesWritten > 0) {
        std::memcpy(lastStereoSample.data(), &output[(samplesWritten - 1) * 2], sizeof(lastStereoSample));
    }

    float audioVolume = audioSettings.getVolume();
    if (audioVolume != 1.0f) {
        auto* sample = output;
        if (audioVolume > 1.0f) {
            audioVolume = 0.6f + 20 * std::log10(audioVolume);
            constexpr int32_t min = std::numeric_limits<int16_t>::min();
            constexpr int32_t max = std::numeric_limits<int16_t>::max();
            for (std::size_t i = 0; i < samplesWritten; i += 2) {
                sample[0] = static_cast<int16_t>(std::clamp<int32_t>(static_cast<int32_t>(sample[0] * audioVolume), min, max));
                sample[1] = static_cast<int16_t>(std::clamp<int32_t>(static_cast<int32_t>(sample[1] * audioVolume), min, max));
                sample += 2;
            }
        } else {
            if (audioSettings.volumeCurve == AudioDeviceConfig::VolumeCurve::Cubic) {
                audioVolume *= audioVolume * audioVolume;
            }
            for (std::size_t i = 0; i < samplesWritten; i += 2) {
                sample[0] = static_cast<int16_t>(sample[0] * audioVolume);
                sample[1] = static_cast<int16_t>(sample[1] * audioVolume);
                sample += 2;
            }
        }
    }

    if (samplesWritten < numFrames) {
        auto* pointer = &output[samplesWritten * channelCount];
        for (std::size_t i = samplesWritten; i < numFrames; ++i) {
            *pointer++ = lastStereoSample[0];
            *pointer++ = lastStereoSample[1];
        }
    }

    return oboe::DataCallbackResult::Continue;
}
