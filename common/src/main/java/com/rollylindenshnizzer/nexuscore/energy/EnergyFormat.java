package com.rollylindenshnizzer.nexuscore.energy;

import java.text.DecimalFormat;

public final class EnergyFormat {
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    public static String format(long amount) {
        return format(amount, "FE");
    }

    public static String format(long amount, String unit) {
        if (amount >= 1_000_000) {
            return FORMAT.format(amount / 1_000_000.0) + " M" + unit;
        }
        if (amount >= 1_000) {
            return FORMAT.format(amount / 1_000.0) + " k" + unit;
        }
        return amount + " " + unit;
    }

    public static String fraction(long amount, long capacity, String unit) {
        return format(amount, unit) + " / " + format(capacity, unit);
    }

    private EnergyFormat() {
    }
}
