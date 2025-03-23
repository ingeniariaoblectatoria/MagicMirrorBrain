package org.example.states;

import javax.sound.sampled.*;

public class AudioUtils {
    private static TargetDataLine getDefaultCaptureDevice(DataLine.Info dataLineInfo)
            throws LineUnavailableException {

        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw new LineUnavailableException(
                    "Default capture device does not support the format required " +
                            "by Picovoice (16kHz, 16-bit, linearly-encoded, single-channel PCM).");
        }

        return (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    }


    static TargetDataLine getAudioDevice(int deviceIndex, DataLine.Info dataLineInfo)
            throws LineUnavailableException {

        if (deviceIndex >= 0) {
            try {
                Mixer.Info mixerInfo = AudioSystem.getMixerInfo()[deviceIndex];
                Mixer mixer = AudioSystem.getMixer(mixerInfo);

                if (mixer.isLineSupported(dataLineInfo)) {
                    return (TargetDataLine) mixer.getLine(dataLineInfo);
                } else {
                    System.err.printf("Audio capture device at index %s does not support the " +
                                    "audio format required by Picovoice. Using default capture device.",
                            deviceIndex);
                }
            } catch (Exception e) {
                System.err.printf(
                        "No capture device found at index %s. Using default capture device.",
                        deviceIndex);
            }
        }

        // use default capture device if we couldn't get the one requested
        return getDefaultCaptureDevice(dataLineInfo);
    }
}
