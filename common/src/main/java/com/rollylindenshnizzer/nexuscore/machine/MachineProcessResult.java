package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public record MachineProcessResult(boolean progressed,
                                   boolean completed,
                                   MachineStallReason stallReason,
                                   String detail) {
    public static MachineProcessResult progress() {
        return new MachineProcessResult(true, false, MachineStallReason.NONE, "");
    }

    public static MachineProcessResult complete() {
        return new MachineProcessResult(true, true, MachineStallReason.NONE, "");
    }

    public static MachineProcessResult stalled(MachineStallReason reason, String detail) {
        return new MachineProcessResult(false, false, reason, detail == null ? "" : detail);
    }
}
