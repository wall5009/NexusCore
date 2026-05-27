package com.rollylindenshnizzer.nexuscore.energy;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public final class NexusEnergyTransfer {
    public static Result move(NexusEnergyStorage from,
                              Direction fromSide,
                              NexusEnergyStorage to,
                              Direction toSide,
                              long maxAmount,
                              boolean simulate) {
        List<String> trace = new ArrayList<>();
        if (fromSide != null && !from.access(fromSide).canExtract()) {
            trace.add("source side " + fromSide + " cannot extract");
            return new Result(0, trace);
        }
        if (toSide != null && !to.access(toSide).canInsert()) {
            trace.add("target side " + toSide + " cannot insert");
            return new Result(0, trace);
        }
        long extracted = from.extract(fromSide, maxAmount, true);
        long accepted = to.insert(toSide, extracted, true);
        if (!simulate && accepted > 0) {
            from.extract(fromSide, accepted, false);
            to.insert(toSide, accepted, false);
        }
        trace.add("moved " + accepted + " energy");
        return new Result(accepted, trace);
    }

    public static Result balance(List<NexusEnergyStorage> storages, long maxPerEdge, boolean simulate) {
        List<String> trace = new ArrayList<>();
        long moved = 0;
        for (int index = 0; index + 1 < storages.size(); index++) {
            Result result = move(storages.get(index), null, storages.get(index + 1), null, maxPerEdge, simulate);
            moved += result.moved();
            trace.addAll(result.trace());
        }
        return new Result(moved, trace);
    }

    public record Result(long moved, List<String> trace) {
        public Result {
            trace = trace == null ? List.of() : List.copyOf(trace);
        }
    }

    private NexusEnergyTransfer() {
    }
}
