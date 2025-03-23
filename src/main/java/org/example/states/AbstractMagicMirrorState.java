package org.example.states;

import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.rhino.RhinoException;
import org.example.ExternalyNotifiable;

import java.io.IOException;

public interface AbstractMagicMirrorState extends ExternalyNotifiable {
    AbstractMagicMirrorState getNextState() throws PorcupineException, RhinoException, InterruptedException, IOException;

    int getState();
}
