package com.rollylindenshnizzer.nexuscore.machine;

public final class ProcessingTimer {
    private int progress;
    private int duration;

    public ProcessingTimer(int duration) {
        this.duration = Math.max(1, duration);
    }

    public boolean tick(boolean active) {
        if (!active) {
            return false;
        }
        progress++;
        if (progress >= duration) {
            progress = 0;
            return true;
        }
        return false;
    }

    public float fraction() {
        return progress / (float) duration;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public void duration(int duration) {
        this.duration = Math.max(1, duration);
        this.progress = Math.min(progress, this.duration - 1);
    }
}
