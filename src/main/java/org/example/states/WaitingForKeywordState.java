package org.example.states;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import org.example.Utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;


import static org.example.states.AudioUtils.getAudioDevice;

public class WaitingForKeywordState implements AbstractMagicMirrorState{
    private final int audioDeviceIndex;

    private final float[] sensitivities = {0.5F, 0.5F};
    private final Porcupine porcupine;
    private final String accessKey;

    public WaitingForKeywordState(String accessKey) throws PorcupineException, IOException {
        this(8, accessKey);
    }

    public WaitingForKeywordState(int audioDeviceIndex, String accessKey) throws PorcupineException, IOException {
        Path path = (new Utils()).resourceNameToPath("/magic-mirror_en_raspberry-pi_v3_0_0.ppn");

        String[] keywordPaths = {
                path.toString(),
                Porcupine.BUILT_IN_KEYWORD_PATHS.get(Porcupine.BuiltInKeyword.ALEXA)
        };

        this.audioDeviceIndex = audioDeviceIndex;
        this.accessKey = accessKey;
        porcupine = new Porcupine.Builder()
                .setAccessKey(accessKey)
                .setLibraryPath(Porcupine.LIBRARY_PATH)
                .setModelPath(Porcupine.MODEL_PATH)
                .setKeywordPaths(keywordPaths)
                .setSensitivities(sensitivities)
                .build();
    }


    @Override
    public synchronized AbstractMagicMirrorState getNextState() throws PorcupineException, IOException {
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
        long totalBytesCaptured = 0;

        // get audio capture device
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine micDataLine;
        try {
            micDataLine = getAudioDevice(audioDeviceIndex, dataLineInfo);
            micDataLine.open(format);
        } catch (LineUnavailableException e) {
            System.err.println(e.toString());
            System.err.println(
                    "Failed to get a valid capture device. Use --show_audio_devices to " +
                            "show available capture devices and their indices");
            System.exit(1);
            return null;
        }


        AbstractMagicMirrorState nextState = null;
        try {
            micDataLine.start();

            // buffers for processing audio
            int frameLength = porcupine.getFrameLength();
            ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] porcupineBuffer = new short[frameLength];

            int numBytesRead;


            while (nextState == null) {

                // read a buffer of audio
                numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());
                totalBytesCaptured += numBytesRead;


                // don't pass to porcupine if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    continue;
                }

                // copy into 16-bit buffer
                captureBuffer.asShortBuffer().get(porcupineBuffer);

                // process with porcupine
                int result = porcupine.process(porcupineBuffer);
                if (result >= 0) {
                    if (result == 0) {
                        nextState = new MagicMirrorState(accessKey);
                    } else if (result == 1) {
                        nextState = new AlexaState(accessKey);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {
            if (porcupine != null) {
                porcupine.delete();
                System.out.println("closing micdataline");
                micDataLine.close();
                System.out.println("micdataline closed");
            }
        }
        if (nextState == null){
            System.err.println("next state error");
            return new WaitingForKeywordState();
        }
        return nextState;
    }

    @Override
    public int getState() {
        return 0;
    }
}
