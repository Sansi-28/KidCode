package com.kidcode.core.evaluator;


import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutionContext {

    public enum State {
        RUNNING,
        PAUSED,
        STEPPING,
        TERMINATED
    }

    private volatile State state = State.RUNNING;

    public synchronized void pause() {
        state = State.PAUSED;
    }

    public synchronized void step() {
        state = State.STEPPING;
        notifyAll();
    }

    public synchronized void resume() {
        state = State.RUNNING;
        notifyAll();
    }

    public synchronized void terminate() {
        state = State.TERMINATED;
        notifyAll();
    }

    public synchronized void waitIfPaused() {
        while (state == State.PAUSED) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized boolean isTerminated() {
        return state == State.TERMINATED;
    }

    public synchronized boolean isStepping() {
        return state == State.STEPPING;
    }
}
