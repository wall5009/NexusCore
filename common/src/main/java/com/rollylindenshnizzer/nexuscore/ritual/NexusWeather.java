package com.rollylindenshnizzer.nexuscore.ritual;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;

import java.util.Locale;

@NexusIncubating(since = "1.3")
public enum NexusWeather {
    ANY,
    CLEAR,
    RAINING,
    THUNDERING;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
