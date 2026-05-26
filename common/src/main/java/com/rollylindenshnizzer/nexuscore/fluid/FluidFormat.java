package com.rollylindenshnizzer.nexuscore.fluid;

import java.text.DecimalFormat;

public final class FluidFormat {
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    public static String droplets(long droplets) {
        if (droplets >= 81_000) {
            return FORMAT.format(droplets / 81_000.0) + " B";
        }
        return droplets + " d";
    }

    public static String millibuckets(long millibuckets) {
        if (millibuckets >= 1000) {
            return FORMAT.format(millibuckets / 1000.0) + " B";
        }
        return millibuckets + " mB";
    }

    private FluidFormat() {
    }
}
