package com.rollylindenshnizzer.nexuscore.fluid;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public final class NexusFluidTransfer {
    public static Result move(NexusFluidTank from,
                              Direction fromSide,
                              NexusFluidTank to,
                              Direction toSide,
                              long maxAmount,
                              boolean simulate) {
        List<String> trace = new ArrayList<>();
        FluidStack drained = from.drain(fromSide, maxAmount, true);
        if (drained.isEmpty()) {
            trace.add("source has no drainable fluid");
            return new Result(FluidStack.EMPTY, trace);
        }
        long accepted = to.fill(toSide, drained, true);
        if (!simulate && accepted > 0) {
            from.drain(fromSide, accepted, false);
            to.fill(toSide, drained.withAmount(accepted), false);
        }
        trace.add("moved " + accepted + " fluid");
        return new Result(drained.withAmount(accepted), trace);
    }

    public record Result(FluidStack moved, List<String> trace) {
        public Result {
            moved = moved == null ? FluidStack.EMPTY : moved;
            trace = trace == null ? List.of() : List.copyOf(trace);
        }
    }

    private NexusFluidTransfer() {
    }
}
