package org.example;

public interface ExternalyNotifiable {
    default void reportExternalChange(int next_state) {};
}
