package com.rollylindenshnizzer.nexuscore.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rollylindenshnizzer.nexuscore.core.NexusException;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class NexusResources {
    public static ResourceLocation builtinPack(String modId, String path) {
        return NexusIds.id(modId, "packs/" + path);
    }

    public static boolean exists(Path root, ResourceLocation id, String extension) {
        return Files.exists(root.resolve(id.getNamespace()).resolve(id.getPath() + extension));
    }

    public static Stream<Path> list(Path root, Predicate<Path> predicate) {
        try {
            return Files.walk(root).filter(Files::isRegularFile).filter(predicate);
        } catch (IOException exception) {
            throw new NexusException("Failed to list resources under " + root, exception);
        }
    }

    public static JsonElement readJson(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        } catch (IOException exception) {
            throw new NexusException("Failed to read JSON resource " + path, exception);
        }
    }

    private NexusResources() {
    }
}
