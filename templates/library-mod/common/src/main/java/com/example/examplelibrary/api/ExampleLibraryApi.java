package com.example.examplelibrary.api;

public final class ExampleLibraryApi {
    private ExampleLibraryApi() {
    }

    public static String greeting(String modId) {
        return "Hello, " + modId + ", from Example Library.";
    }
}
