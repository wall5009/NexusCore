package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@NexusStable(since = "1.2")
public final class NexusEntityDefinitions {
    private static final Map<ResourceLocation, NexusEntityDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        DebugRegistry.section("nexuscore.entities", () -> Integer.toString(DEFINITIONS.size()));
    }

    public static NexusEntityDefinition.Builder entity(String modId, String path, MobCategory category) {
        return NexusEntityDefinition.builder(NexusIds.id(modId, path), category);
    }

    public static NexusEntityDefinition.Builder projectile(String modId, String path) {
        return entity(modId, path, MobCategory.MISC);
    }

    public static NexusEntityDefinition register(NexusEntityDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> RegisteredNexusEntity<T> registerType(NexusEntityDefinition definition,
                                                                           EntityType.EntityFactory<T> factory) {
        NexusEntityDefinition registeredDefinition = register(definition);
        RegistrySupplier<EntityType<T>> type = EntityTypeBuilder.of(definition.id().getNamespace(), definition.id().getPath(), factory, definition.category())
                .sized(definition.width(), definition.height())
                .trackingRange(definition.trackingRange())
                .updateInterval(definition.updateInterval())
                .register();
        return new RegisteredNexusEntity<>(registeredDefinition, type, Optional.empty());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Mob> RegisteredNexusEntity<T> registerMobType(NexusEntityDefinition definition,
                                                                           EntityType.EntityFactory<T> factory) {
        RegisteredNexusEntity<T> registered = registerType(definition, factory);
        Optional<RegistrySupplier<Item>> spawnEgg = Optional.empty();
        if (definition.spawnEgg()) {
            String path = definition.id().getPath() + "_spawn_egg";
            RegistrySupplier<Item> egg = NexusRegistries.group(definition.id().getNamespace()).items()
                    .register(path, () -> new SpawnEggItem((EntityType<? extends Mob>) registered.type().get(),
                            definition.primaryEggColor(), definition.secondaryEggColor(), new Item.Properties()));
            spawnEgg = Optional.of(egg);
        }
        return new RegisteredNexusEntity<>(definition, registered.type(), spawnEgg);
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (NexusEntityDefinition definition : DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
            }
        }
        return plan;
    }

    public static Collection<NexusEntityDefinition> definitions() {
        return java.util.List.copyOf(DEFINITIONS.values());
    }

    public static String debugSummary() {
        return "entities=" + DEFINITIONS.keySet();
    }

    private NexusEntityDefinitions() {
    }
}
