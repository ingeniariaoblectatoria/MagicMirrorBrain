package org.example.states;

import ai.picovoice.porcupine.PorcupineException;
import org.example.states.AbstractMagicMirrorState;
import org.example.states.WaitingForKeywordState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AlexaState implements AbstractMagicMirrorState {
    private Integer next_state = null;
    private String accessKey;

    public AlexaState(String accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public synchronized AbstractMagicMirrorState getNextState() throws PorcupineException, InterruptedException, IOException {
        System.out.println("Alexa state");

        Runtime.getRuntime().exec("wtype A");


        while (next_state == null){
            wait();
            System.out.println("next_state is null: "+next_state == null);
        }
        return new WaitingForKeywordState(accessKey);
    }

    @Override
    public int getState() {
        return 3;
    }

    @Override
    public synchronized void reportExternalChange(int next_state){
        if (next_state != 3) {
            this.next_state = next_state;
            System.out.println("indicating that alexa should leave with state: " + this.next_state);
            notifyAll();
        }
    }
}
