package com.rollylindenshnizzer.nexuscore.energy;

public final class EnergyTransfer {
    public static long move(EnergyStorage from, EnergyStorage to, long maxAmount, boolean simulate) {
        long extracted = from.extract(maxAmount, true);
        long accepted = to.insert(extracted, true);
        if (!simulate && accepted > 0) {
            from.extract(accepted, false);
            to.insert(accepted, false);
        }
        return accepted;
    }

    private EnergyTransfer() {
    }
}
