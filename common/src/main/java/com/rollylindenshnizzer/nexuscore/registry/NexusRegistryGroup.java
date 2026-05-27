package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.core.NexusTasks;
import com.rollylindenshnizzer.nexuscore.core.TaskQueue;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class NexusRegistryGroup {
    private final String modId;
    private final DeferredRegister<Item> items;
    private final DeferredRegister<Block> blocks;
    private final DeferredRegister<BlockEntityType<?>> blockEntities;
    private final DeferredRegister<MenuType<?>> menus;
    private final DeferredRegister<EntityType<?>> entityTypes;
    private final DeferredRegister<SoundEvent> sounds;
    private final DeferredRegister<ParticleType<?>> particles;
    private final DeferredRegister<CreativeModeTab> creativeTabs;
    private final DeferredRegister<RecipeType<?>> recipeTypes;
    private final DeferredRegister<RecipeSerializer<?>> recipeSerializers;
    private final DeferredRegister<DataComponentType<?>> dataComponents;
    private final Map<String, Set<String>> paths = new HashMap<>();
    private final Map<String, NexusRegistryGroup> children = new HashMap<>();
    private final Set<String> tags = new HashSet<>();
    private final Set<String> validationRules = new HashSet<>();
    private String defaultCreativeTab = "";
    private String translationPrefix = "";
    private String assetPathPrefix = "";
    private String datagenDefaults = "";
    private boolean registered;

    NexusRegistryGroup(String modId) {
        this.modId = NexusIds.requireNamespace(modId);
        this.items = DeferredRegister.create(modId, Registries.ITEM);
        this.blocks = DeferredRegister.create(modId, Registries.BLOCK);
        this.blockEntities = DeferredRegister.create(modId, Registries.BLOCK_ENTITY_TYPE);
        this.menus = DeferredRegister.create(modId, Registries.MENU);
        this.entityTypes = DeferredRegister.create(modId, Registries.ENTITY_TYPE);
        this.sounds = DeferredRegister.create(modId, Registries.SOUND_EVENT);
        this.particles = DeferredRegister.create(modId, Registries.PARTICLE_TYPE);
        this.creativeTabs = DeferredRegister.create(modId, Registries.CREATIVE_MODE_TAB);
        this.recipeTypes = DeferredRegister.create(modId, Registries.RECIPE_TYPE);
        this.recipeSerializers = DeferredRegister.create(modId, Registries.RECIPE_SERIALIZER);
        this.dataComponents = DeferredRegister.create(modId, Registries.DATA_COMPONENT_TYPE);
    }

    public String modId() {
        return modId;
    }

    public DeferredRegister<Item> items() {
        return items;
    }

    public DeferredRegister<Block> blocks() {
        return blocks;
    }

    public DeferredRegister<BlockEntityType<?>> blockEntities() {
        return blockEntities;
    }

    public DeferredRegister<MenuType<?>> menus() {
        return menus;
    }

    public DeferredRegister<EntityType<?>> entityTypes() {
        return entityTypes;
    }

    public DeferredRegister<SoundEvent> sounds() {
        return sounds;
    }

    public DeferredRegister<ParticleType<?>> particles() {
        return particles;
    }

    public DeferredRegister<CreativeModeTab> creativeTabs() {
        return creativeTabs;
    }

    public DeferredRegister<RecipeType<?>> recipeTypes() {
        return recipeTypes;
    }

    public DeferredRegister<RecipeSerializer<?>> recipeSerializers() {
        return recipeSerializers;
    }

    public DeferredRegister<DataComponentType<?>> dataComponents() {
        return dataComponents;
    }

    public <T> RegistrySupplier<T> register(DeferredRegister<? super T> registry, String registryName, String path, Supplier<? extends T> supplier) {
        validateDuplicate(registryName, path);
        String normalized = NexusIds.normalizePath(path);
        NexusContentManifest.record(modId, "root", registryName, modId + ":" + normalized,
                NexusContentManifest.sourceHint(), "registry");
        return registry.register(normalized, supplier);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> RegistrySupplier<DataComponentType<T>> dataComponent(String path, Supplier<DataComponentType<T>> supplier) {
        validateDuplicate("data_component", path);
        String normalized = NexusIds.normalizePath(path);
        NexusContentManifest.record(modId, "root", "data_component", modId + ":" + normalized,
                NexusContentManifest.sourceHint(), "registry");
        return (RegistrySupplier) dataComponents.register(normalized, supplier);
    }

    public <T> void validateRegistryPath(String registryName, String path) {
        validateDuplicate(registryName, path);
    }

    public void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        blocks.register();
        items.register();
        blockEntities.register();
        menus.register();
        entityTypes.register();
        sounds.register();
        particles.register();
        creativeTabs.register();
        recipeTypes.register();
        recipeSerializers.register();
        dataComponents.register();
        NexusTasks.runQueued(TaskQueue.AFTER_REGISTRIES);
    }

    public NexusRegistryGroup child(String name) {
        String childName = NexusIds.normalizePath(name).replace('/', '_');
        return children.computeIfAbsent(childName, ignored -> new NexusRegistryGroup(modId));
    }

    public Map<String, NexusRegistryGroup> children() {
        return Map.copyOf(children);
    }

    public NexusRegistryGroup tag(String tag) {
        tags.add(tag);
        return this;
    }

    public List<String> tags() {
        return tags.stream().sorted().toList();
    }

    public NexusRegistryGroup defaultCreativeTab(String id) {
        this.defaultCreativeTab = id == null ? "" : id;
        return this;
    }

    public String defaultCreativeTab() {
        return defaultCreativeTab;
    }

    public NexusRegistryGroup translationPrefix(String translationPrefix) {
        this.translationPrefix = translationPrefix == null ? "" : translationPrefix;
        return this;
    }

    public String translationPrefix() {
        return translationPrefix;
    }

    public NexusRegistryGroup assetPathPrefix(String assetPathPrefix) {
        this.assetPathPrefix = assetPathPrefix == null ? "" : assetPathPrefix;
        return this;
    }

    public String assetPathPrefix() {
        return assetPathPrefix;
    }

    public NexusRegistryGroup datagenDefaults(String datagenDefaults) {
        this.datagenDefaults = datagenDefaults == null ? "" : datagenDefaults;
        return this;
    }

    public String datagenDefaults() {
        return datagenDefaults;
    }

    public NexusRegistryGroup validationRule(String rule) {
        validationRules.add(rule);
        return this;
    }

    public List<String> validationRules() {
        return validationRules.stream().sorted().toList();
    }

    private void validateDuplicate(String registryName, String path) {
        String normalized = NexusIds.normalizePath(path);
        Set<String> seen = paths.computeIfAbsent(registryName, ignored -> new HashSet<>());
        if (!seen.add(normalized)) {
            throw new DuplicateRegistrationException(modId, registryName, normalized);
        }
    }
}
