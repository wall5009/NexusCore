package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockDefinition;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemDefinition;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.bridge.registry.BlockFactoryBridge;
import com.rollylindenshnizzer.nexuscore.bridge.registry.ItemFactoryBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Supplier;

public final class NativeMinecraftFactoryBridge implements ItemFactoryBridge, BlockFactoryBridge {
    private final NexusTarget target;
    private final ThreadLocal<Boolean> registryContext = ThreadLocal.withInitial(() -> false);
    private final SimpleItemFactoryBridge fallbackItems = new SimpleItemFactoryBridge();
    private final SimpleBlockFactoryBridge fallbackBlocks = new SimpleBlockFactoryBridge();

    public NativeMinecraftFactoryBridge() {
        this(null);
    }

    public NativeMinecraftFactoryBridge(NexusTarget target) {
        this.target = target;
    }

    @Override
    public Object createItem(NexusItemDefinition definition) {
        if (defersNativeConstruction()) {
            return fallbackItems.createItem(definition);
        }
        try {
            Object properties = itemProperties(definition);
            Class<?> itemClass = firstClass("net.minecraft.world.item.Item", "net.minecraft.item.Item");
            Constructor<?> constructor = itemClass.getConstructor(properties.getClass());
            return constructor.newInstance(properties);
        } catch (ReflectiveOperationException | RuntimeException error) {
            debugFallback("item", definition.fullId(), error);
            return fallbackItems.createItem(definition);
        }
    }

    @Override
    public Object createBlock(NexusBlockDefinition definition) {
        if (defersNativeConstruction()) {
            return fallbackBlocks.createBlock(definition);
        }
        try {
            Object properties = blockProperties(definition);
            Class<?> blockClass = firstClass("net.minecraft.world.level.block.Block", "net.minecraft.block.Block");
            Constructor<?> constructor = blockClass.getConstructor(properties.getClass());
            return constructor.newInstance(properties);
        } catch (ReflectiveOperationException | RuntimeException error) {
            debugFallback("block", definition.fullId(), error);
            return fallbackBlocks.createBlock(definition);
        }
    }

    @Override
    public Object createBlockItem(NexusBlockDefinition definition, Object block) {
        if (defersNativeConstruction()) {
            return fallbackBlocks.createBlockItem(definition, block);
        }
        try {
            if (isFallback(block)) {
                return fallbackBlocks.createBlockItem(definition, block);
            }
            Object properties = itemProperties(new NexusItemDefinition(definition.modId(), definition.id(), definition.creativeTab(), 64, false, 0, 0.0f, Map.of("block", definition.fullId())));
            Class<?> blockItemClass = firstClass("net.minecraft.world.item.BlockItem", "net.minecraft.item.BlockItem");
            Constructor<?> constructor = matchingConstructor(blockItemClass, block.getClass(), properties.getClass());
            return constructor.newInstance(block, properties);
        } catch (ReflectiveOperationException | RuntimeException error) {
            debugFallback("block item", definition.fullId(), error);
            return fallbackBlocks.createBlockItem(definition, block);
        }
    }

    public <T> T inRegistryContext(Supplier<T> supplier) {
        boolean previous = registryContext.get();
        registryContext.set(true);
        try {
            return supplier.get();
        } finally {
            registryContext.set(previous);
        }
    }

    private boolean defersNativeConstruction() {
        if (target == null || registryContext.get()) {
            return false;
        }
        String loader = target.loader().id();
        return "forge".equals(loader) || "neoforge".equals(loader);
    }

    private void debugFallback(String type, String id, Throwable error) {
        if (!Boolean.getBoolean("nexuscore.debugFactory")) {
            return;
        }
        System.out.println("[nexuscore] Native Minecraft " + type + " creation fell back for " + id + ": " + error);
        error.printStackTrace(System.out);
    }

    private Object itemProperties(NexusItemDefinition definition) throws ReflectiveOperationException {
        Object properties = construct(firstClass("net.minecraft.world.item.Item$Properties", "net.minecraft.item.Item$Settings"));
        properties = setRegistryIdIfPresent(properties, "ITEM", definition.fullId());
        properties = invokeIfPresent(properties, new Class<?>[]{int.class}, definition.maxStackSize(), "stacksTo", "maxCount");
        if (definition.fireResistant()) {
            properties = invokeIfPresent(properties, new Class<?>[0], null, "fireResistant", "fireproof");
        }
        return properties;
    }

    private Object blockProperties(NexusBlockDefinition definition) throws ReflectiveOperationException {
        Class<?> propertiesClass = firstClass("net.minecraft.world.level.block.state.BlockBehaviour$Properties", "net.minecraft.block.AbstractBlock$Settings");
        Object properties = invokeStaticIfPresent(propertiesClass, "of", "create");
        properties = setRegistryIdIfPresent(properties, "BLOCK", definition.fullId());
        properties = invokeIfPresent(properties, new Class<?>[]{float.class, float.class}, new Object[]{definition.strength(), definition.resistance()}, "strength");
        if (definition.requiresTool()) {
            properties = invokeIfPresent(properties, new Class<?>[0], null, "requiresCorrectToolForDrops", "requiresTool");
        }
        return properties;
    }

    private Class<?> firstClass(String... names) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String name : names) {
            ClassLoader[] loaders = new ClassLoader[]{
                    Thread.currentThread().getContextClassLoader(),
                    NativeMinecraftFactoryBridge.class.getClassLoader()
            };
            for (ClassLoader loader : loaders) {
                if (loader == null) {
                    continue;
                }
                try {
                    return Class.forName(name, false, loader);
                } catch (ClassNotFoundException error) {
                    last = error;
                }
            }
        }
        throw last == null ? new ClassNotFoundException("No class names supplied") : last;
    }

    private Object construct(Class<?> type) throws ReflectiveOperationException {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object invokeStaticIfPresent(Class<?> type, String... names) throws ReflectiveOperationException {
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name);
                method.setAccessible(true);
                return method.invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return construct(type);
    }

    private Object invokeIfPresent(Object receiver, Class<?>[] parameterTypes, Object argument, String... names) throws ReflectiveOperationException {
        Object[] arguments = parameterTypes.length == 0 ? new Object[0] : new Object[]{argument};
        return invokeIfPresent(receiver, parameterTypes, arguments, names);
    }

    private Object invokeIfPresent(Object receiver, Class<?>[] parameterTypes, Object[] arguments, String... names) throws ReflectiveOperationException {
        for (String name : names) {
            try {
                Method method = receiver.getClass().getMethod(name, parameterTypes);
                Object result = method.invoke(receiver, arguments);
                return result == null ? receiver : result;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return receiver;
    }

    private Object setRegistryIdIfPresent(Object properties, String registryField, String id) throws ReflectiveOperationException {
        for (Method method : properties.getClass().getMethods()) {
            if (!"setId".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            Object key = resourceKey(registryField, id);
            if (method.getParameterTypes()[0].isInstance(key)) {
                Object result = method.invoke(properties, key);
                return result == null ? properties : result;
            }
        }
        return properties;
    }

    private Object resourceKey(String registryField, String id) throws ReflectiveOperationException {
        Class<?> registries = firstClass("net.minecraft.core.registries.Registries", "net.minecraft.registry.RegistryKeys");
        Field field = registries.getField(registryField);
        Object registryKey = field.get(null);
        Object identifier = identifier(id);
        Class<?> resourceKey = firstClass("net.minecraft.resources.ResourceKey", "net.minecraft.registry.RegistryKey", "net.minecraft.class_5321");
        for (Method method : resourceKey.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals("create") || method.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters[0].isInstance(registryKey) && parameters[1].isInstance(identifier)) {
                return method.invoke(null, registryKey, identifier);
            }
        }
        throw new NoSuchMethodException(resourceKey.getName() + ".create");
    }

    private Object identifier(String id) throws ReflectiveOperationException {
        String[] parts = id.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        Class<?> identifier = firstClass("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier", "net.minecraft.util.Identifier", "net.minecraft.class_2960");
        for (String methodName : new String[]{"fromNamespaceAndPath", "of", "method_60655", "method_43902"}) {
            try {
                Method method = identifier.getMethod(methodName, String.class, String.class);
                return method.invoke(null, namespace, path);
            } catch (NoSuchMethodException ignored) {
            }
        }
        String full = namespace + ":" + path;
        for (String methodName : new String[]{"parse", "tryParse", "method_60654", "method_12829"}) {
            try {
                Method method = identifier.getMethod(methodName, String.class);
                return method.invoke(null, full);
            } catch (NoSuchMethodException ignored) {
            }
        }
        try {
            return identifier.getConstructor(String.class, String.class).newInstance(namespace, path);
        } catch (NoSuchMethodException ignored) {
            return identifier.getConstructor(String.class).newInstance(full);
        }
    }

    private Constructor<?> matchingConstructor(Class<?> type, Class<?> blockType, Class<?> propertiesType) throws NoSuchMethodException {
        for (Constructor<?> constructor : type.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 2 && parameters[0].isAssignableFrom(blockType) && parameters[1].isAssignableFrom(propertiesType)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException(type.getName() + "(Block, Item.Properties)");
    }

    private boolean isFallback(Object value) {
        return value == null || value.getClass().getName().startsWith("com.rollylindenshnizzer.nexuscore.");
    }
}
