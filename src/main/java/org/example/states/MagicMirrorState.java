package org.example.states;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.rhino.Rhino;
import ai.picovoice.rhino.RhinoException;
import ai.picovoice.rhino.RhinoInference;
import org.example.Utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.example.states.AudioUtils.getAudioDevice;

public class MagicMirrorState implements AbstractMagicMirrorState{
    private final int audioDeviceIndex;

    private final float[] sensitivities = {0.5F, 0.5F};
    private final Rhino rhino;
    private final String accessKey;

    public MagicMirrorState(String accessKey) throws PorcupineException, RhinoException, IOException {
        this(8, accessKey);
    }

    public MagicMirrorState(int audioDeviceIndex, String accessKey) throws RhinoException, IOException {
        this.audioDeviceIndex = audioDeviceIndex;
        this.accessKey = accessKey;
        try {
            rhino = new Rhino.Builder()
                    .setAccessKey(accessKey)
                    .setContextPath((new Utils()).resourceNameToPath("/MagicMirrorContext_en_raspberry-pi_v3_0_0.rhn").toString())
                    .setLibraryPath(Rhino.LIBRARY_PATH)
                    .setModelPath(Rhino.MODEL_PATH)
                    .setSensitivity(0.5f)
                    .setEndpointDuration(0.5f)
                    .setRequireEndpoint(false)
                    .build();
        }catch (RhinoException | IOException e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public synchronized AbstractMagicMirrorState getNextState() throws PorcupineException, RhinoException {
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

        try {
            micDataLine.start();

            // buffers for processing audio
            int frameLength = rhino.getFrameLength();
            ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] rhinoBuffer = new short[frameLength];

            int numBytesRead;
            while (true) {

                // read a buffer of audio
                numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());
                totalBytesCaptured += numBytesRead;

                // don't pass to rhino if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    continue;
                }

                // copy into 16-bit buffer
                captureBuffer.asShortBuffer().get(rhinoBuffer);

                // process with rhino
                boolean isFinalized = rhino.process(rhinoBuffer);
                if (isFinalized) {

                    RhinoInference inference = rhino.getInference();
                    if (inference.getIsUnderstood()) {
                        System.out.println("Is understood");
                        return new MagicMirrorAnswerState(accessKey);
                    } else {
                        System.out.println("Is not understood");
                        return new WaitingForKeywordState(accessKey);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {
            if (rhino != null) {
                rhino.delete();
                micDataLine.close();
            }
        }
        return this;
    }

    @Override
    public int getState() {
        return 1;
    }
}
