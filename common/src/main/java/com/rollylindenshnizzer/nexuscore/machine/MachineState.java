package com.rollylindenshnizzer.nexuscore.machine;

import net.minecraft.nbt.CompoundTag;

public final class MachineState {
    private int progress;
    private int maxProgress = 1;
    private boolean active;
    private RedstoneControlMode redstoneMode = RedstoneControlMode.IGNORED;

    public int progress() {
        return progress;
    }

    public int maxProgress() {
        return maxProgress;
    }

    public boolean active() {
        return active;
    }

    public RedstoneControlMode redstoneMode() {
        return redstoneMode;
    }

    public void start(int maxProgress) {
        this.maxProgress = Math.max(1, maxProgress);
        this.progress = 0;
        this.active = true;
    }

    public boolean tickProgress() {
        if (!active) {
            return false;
        }
        progress++;
        if (progress >= maxProgress) {
            active = false;
            return true;
        }
        return false;
    }

    public void stop() {
        active = false;
        progress = 0;
    }

    public boolean redstoneAllows(boolean powered) {
        return redstoneMode == RedstoneControlMode.IGNORED
                || (redstoneMode == RedstoneControlMode.HIGH && powered)
                || (redstoneMode == RedstoneControlMode.LOW && !powered);
    }

    public void redstoneMode(RedstoneControlMode redstoneMode) {
        this.redstoneMode = redstoneMode;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putBoolean("Active", active);
        tag.putString("RedstoneMode", redstoneMode.name());
        return tag;
    }

    public void load(CompoundTag tag) {
        progress = Math.max(0, tag.getInt("Progress"));
        maxProgress = Math.max(1, tag.getInt("MaxProgress"));
        active = tag.getBoolean("Active");
        try {
            redstoneMode = RedstoneControlMode.valueOf(tag.getString("RedstoneMode"));
        } catch (IllegalArgumentException ignored) {
            redstoneMode = RedstoneControlMode.IGNORED;
        }
    }
}
