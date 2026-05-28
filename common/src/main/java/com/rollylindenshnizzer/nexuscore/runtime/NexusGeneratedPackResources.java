package com.rollylindenshnizzer.nexuscore.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@NexusStable(since = "1.3")
public final class NexusGeneratedPackResources implements PackResources {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PACK_DESCRIPTION = "NexusCore generated runtime content";

    private final PackLocationInfo location;

    public NexusGeneratedPackResources(PackLocationInfo location) {
        this.location = location;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length == 1 && PACK_META.equals(path[0])) {
            return bytes(rootMetadata());
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        NexusData.DataPlan plan = NexusData.plans().get(id.getNamespace());
        if (plan == null) {
            return null;
        }
        if (type == PackType.SERVER_DATA) {
            JsonObject json = plan.data().get(id.getPath());
            return json == null ? null : bytes(GSON.toJson(json));
        }
        if (type == PackType.CLIENT_RESOURCES) {
            if ("lang/en_us.json".equals(id.getPath()) && !plan.translations().isEmpty()) {
                return bytes(GSON.toJson(plan.translations()));
            }
            JsonObject json = plan.assets().get(id.getPath());
            return json == null ? null : bytes(GSON.toJson(json));
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        NexusData.DataPlan plan = NexusData.plans().get(namespace);
        if (plan == null) {
            return;
        }
        String normalizedPath = path == null || path.isBlank()
                ? ""
                : NexusIds.normalizePath(path);
        if (type == PackType.SERVER_DATA) {
            plan.data().forEach((relativePath, json) -> emit(namespace, normalizedPath, relativePath, json, output));
        } else if (type == PackType.CLIENT_RESOURCES) {
            if (!plan.translations().isEmpty()) {
                emit(namespace, normalizedPath, "lang/en_us.json", GSON.toJson(plan.translations()), output);
            }
            plan.assets().forEach((relativePath, json) -> emit(namespace, normalizedPath, relativePath, json, output));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new TreeSet<>();
        for (Map.Entry<String, NexusData.DataPlan> entry : NexusData.plans().entrySet()) {
            NexusData.DataPlan plan = entry.getValue();
            if (type == PackType.SERVER_DATA && !plan.data().isEmpty()) {
                namespaces.add(entry.getKey());
            } else if (type == PackType.CLIENT_RESOURCES
                    && (!plan.assets().isEmpty() || !plan.translations().isEmpty())) {
                namespaces.add(entry.getKey());
            }
        }
        return namespaces;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (PackMetadataSection.TYPE.getMetadataSectionName().equals(serializer.getMetadataSectionName())) {
            return (T) new PackMetadataSection(Component.literal(PACK_DESCRIPTION),
                    SharedConstants.DATA_PACK_FORMAT, Optional.empty());
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
    }

    private static void emit(String namespace, String requestedPath, String relativePath,
                             JsonObject json, ResourceOutput output) {
        emit(namespace, requestedPath, relativePath, GSON.toJson(json), output);
    }

    private static void emit(String namespace, String requestedPath, String relativePath,
                             String body, ResourceOutput output) {
        if (relativePath.startsWith(requestedPath)) {
            output.accept(ResourceLocation.fromNamespaceAndPath(namespace, relativePath), bytes(body));
        }
    }

    private static IoSupplier<InputStream> bytes(String body) {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        return () -> new ByteArrayInputStream(data);
    }

    private static String rootMetadata() {
        JsonObject root = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", SharedConstants.DATA_PACK_FORMAT);
        pack.addProperty("description", PACK_DESCRIPTION);
        root.add("pack", pack);
        return GSON.toJson(root);
    }
}
