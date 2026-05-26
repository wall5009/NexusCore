package com.rollylindenshnizzer.nexuscore.client;

import java.util.ArrayList;
import java.util.List;

public final class NexusClientDescriptors {
    private static final List<ClientDescriptor> DESCRIPTORS = new ArrayList<>();

    public static void register(ClientDescriptor descriptor) {
        DESCRIPTORS.add(descriptor);
    }

    public static List<ClientDescriptor> descriptors() {
        return List.copyOf(DESCRIPTORS);
    }

    private NexusClientDescriptors() {
    }
}
