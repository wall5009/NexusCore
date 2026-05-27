package com.rollylindenshnizzer.nexuscore.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.registry.NexusContentManifest;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class NexusDataProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final String modId;
    private final NexusData.DataPlan plan;

    public NexusDataProvider(PackOutput output, String modId, NexusData.DataPlan plan) {
        this.output = output;
        this.modId = modId;
        this.plan = plan;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        CompletableFuture<?> future = CompletableFuture.completedFuture(null);
        if (!plan.translations().isEmpty()) {
            JsonObject translations = new JsonObject();
            plan.translations().forEach(translations::addProperty);
            future = future.thenCompose(ignored -> save(cachedOutput,
                    output.getOutputFolder().resolve("assets").resolve(modId).resolve("lang").resolve("en_us.json"),
                    translations));
        }
        for (Map.Entry<String, JsonObject> entry : plan.assets().entrySet()) {
            future = future.thenCompose(ignored -> save(cachedOutput,
                    output.getOutputFolder().resolve("assets").resolve(modId).resolve(entry.getKey()),
                    entry.getValue()));
        }
        for (Map.Entry<String, JsonObject> entry : plan.data().entrySet()) {
            future = future.thenCompose(ignored -> save(cachedOutput,
                    output.getOutputFolder().resolve("data").resolve(modId).resolve(entry.getKey()),
                    entry.getValue()));
        }
        future = future.thenCompose(ignored -> save(cachedOutput,
                output.getOutputFolder().resolve("data").resolve(modId).resolve("nexus.content.json"),
                NexusContentManifest.json(modId)));
        return future;
    }

    @Override
    public String getName() {
        return "NexusCore generated data for " + modId;
    }

    private static CompletableFuture<?> save(CachedOutput cachedOutput, Path path, JsonObject json) {
        return DataProvider.saveStable(cachedOutput, GSON.toJsonTree(json), path);
    }
}
