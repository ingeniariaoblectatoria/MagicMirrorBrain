package org.example;

import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.rhino.RhinoException;
import org.example.states.AbstractMagicMirrorState;
import org.example.states.WaitingForKeywordState;

import java.io.IOException;

public class MagicMirrorContext  implements ExternalyNotifiable{
    private AbstractMagicMirrorState currentState;
    private String accessKey;

    public MagicMirrorContext(String accessKey) throws PorcupineException, IOException {
        this.accessKey = accessKey;
        currentState = new WaitingForKeywordState(accessKey);
    }

    public void update(){
        try {
            currentState = currentState.getNextState();
        } catch (PorcupineException | RhinoException | InterruptedException | IOException e) {
            System.exit(1);
        }
    }

    @Override
    public void reportExternalChange(int next_state) {
        System.out.println("informing current state of external change");
        currentState.reportExternalChange(next_state);
    }

    public int getState() {
        return currentState.getState();
    }
}
