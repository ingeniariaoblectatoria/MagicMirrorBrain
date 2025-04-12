package org.example.states;

import ai.picovoice.porcupine.PorcupineException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MagicMirrorAnswerState implements AbstractMagicMirrorState{
    private final String accessKey;

    MagicMirrorAnswerState(String accessKey){
        this.accessKey = accessKey;
    }
    @Override
    public synchronized AbstractMagicMirrorState getNextState() throws PorcupineException, InterruptedException, IOException {
        System.out.println("Magic mirror answer");
        TimeUnit.SECONDS.sleep(22);
        return new WaitingForKeywordState(accessKey);
    }

    @Override
    public int getState() {
        return 2;
    }
}
