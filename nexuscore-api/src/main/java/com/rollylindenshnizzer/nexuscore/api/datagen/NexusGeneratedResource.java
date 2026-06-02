package com.rollylindenshnizzer.nexuscore.api.datagen;

public final class NexusGeneratedResource {
    private final String path;
    private final String json;

    public NexusGeneratedResource(String path, String json) {
        this.path = path;
        this.json = json;
    }

    public String path() {
        return path;
    }

    public String json() {
        return json;
    }
}
