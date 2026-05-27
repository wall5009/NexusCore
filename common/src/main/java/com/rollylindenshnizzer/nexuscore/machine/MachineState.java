package com.rollylindenshnizzer.nexuscore.machine;

import net.minecraft.nbt.CompoundTag;

public final class MachineState {
    private Status status = Status.IDLE;
    private int progress;
    private int maxProgress = 1;
    private RedstoneControlMode redstoneMode = RedstoneControlMode.IGNORED;

    public Status status() {
        return status;
    }

    public MachineState status(Status status) {
        this.status = status == null ? Status.IDLE : status;
        return this;
    }

    public int progress() {
        return progress;
    }

    public int maxProgress() {
        return maxProgress;
    }

    public boolean active() {
        return status == Status.RUNNING;
    }

    public RedstoneControlMode redstoneMode() {
        return redstoneMode;
    }

    public MachineState progress(int progress, int maxProgress) {
        this.maxProgress = Math.max(1, maxProgress);
        this.progress = Math.max(0, Math.min(progress, this.maxProgress));
        return this;
    }

    public void start(int maxProgress) {
        this.maxProgress = Math.max(1, maxProgress);
        this.progress = 0;
        this.status = Status.RUNNING;
    }

    public boolean tickProgress() {
        if (!active()) {
            return false;
        }
        progress++;
        if (progress >= maxProgress) {
            status = Status.IDLE;
            return true;
        }
        return false;
    }

    public void stop() {
        status = Status.IDLE;
        progress = 0;
    }

    public boolean redstoneAllows(boolean powered) {
        return redstoneMode == RedstoneControlMode.IGNORED
                || redstoneMode == RedstoneControlMode.HIGH && powered
                || redstoneMode == RedstoneControlMode.LOW && !powered;
    }

    public void redstoneMode(RedstoneControlMode redstoneMode) {
        this.redstoneMode = redstoneMode == null ? RedstoneControlMode.IGNORED : redstoneMode;
    }

    public float progressFraction() {
        return progress / (float) maxProgress;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Status", status.name());
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putBoolean("Active", active());
        tag.putString("RedstoneMode", redstoneMode.name());
        return tag;
    }

    public void load(CompoundTag tag) {
        try {
            status = Status.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException exception) {
            status = tag.getBoolean("Active") ? Status.RUNNING : Status.IDLE;
        }
        progress(tag.getInt("Progress"), Math.max(1, tag.getInt("MaxProgress")));
        try {
            redstoneMode = RedstoneControlMode.valueOf(tag.getString("RedstoneMode"));
        } catch (IllegalArgumentException exception) {
            redstoneMode = RedstoneControlMode.IGNORED;
        }
    }

    public enum Status {
        IDLE,
        RUNNING,
        BLOCKED,
        ERROR
    }
}
