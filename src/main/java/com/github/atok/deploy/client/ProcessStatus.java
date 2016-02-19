package com.github.atok.deploy.client;

public class ProcessStatus {
    final public Boolean running;

    public ProcessStatus(Boolean running) {
        this.running = running;
    }

    @Override
    public String toString() {
        return "Running: " + running;
    }
}
