package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusException;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ServiceLoader;

public final class ContentModuleManager {
    private final String modId;
    private final List<ContentModule> modules;

    private ContentModuleManager(String modId, Collection<? extends ContentModule> modules) {
        this.modId = NexusIds.requireNamespace(modId);
        this.modules = List.copyOf(modules);
    }

    public static ContentModuleManager create(String modId, Collection<? extends ContentModule> modules) {
        return new ContentModuleManager(modId, modules);
    }

    public static ContentModuleManager discover(String modId) {
        List<ContentModule> discovered = new ArrayList<>();
        ServiceLoader.load(ContentModule.class).forEach(discovered::add);
        return new ContentModuleManager(modId, discovered);
    }

    public List<ContentModule> sortedModules() {
        Map<String, ContentModule> byId = new HashMap<>();
        for (ContentModule module : modules) {
            if (module.enabled()) {
                if (byId.put(module.id(), module) != null) {
                    throw new NexusException("Duplicate content module id '" + module.id() + "' in " + modId);
                }
            }
        }
        List<ContentModule> result = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (ContentModule module : byId.values()) {
            visit(module, byId, visiting, visited, result);
        }
        return result;
    }

    public void initialize() {
        NexusRegistryGroup registries = NexusRegistries.group(modId);
        for (ContentModule module : sortedModules()) {
            module.register(registries);
            module.dataGeneration();
            module.compatibility();
        }
    }

    private static void visit(ContentModule module, Map<String, ContentModule> byId, Set<String> visiting,
                              Set<String> visited, List<ContentModule> result) {
        if (visited.contains(module.id())) {
            return;
        }
        if (!visiting.add(module.id())) {
            throw new NexusException("Circular content module dependency involving '" + module.id() + "'");
        }
        for (String dependency : module.dependencies()) {
            ContentModule dependencyModule = byId.get(dependency);
            if (dependencyModule == null) {
                throw new NexusException("Content module '" + module.id() + "' depends on missing module '" + dependency + "'");
            }
            visit(dependencyModule, byId, visiting, visited, result);
        }
        visiting.remove(module.id());
        visited.add(module.id());
        result.add(module);
    }
}
