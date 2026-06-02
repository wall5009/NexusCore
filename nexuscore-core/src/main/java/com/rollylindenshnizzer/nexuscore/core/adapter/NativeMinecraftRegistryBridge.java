package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockHandle;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemHandle;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusCreativeTabDefinition;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusFactory;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.bridge.registry.BlockFactoryBridge;
import com.rollylindenshnizzer.nexuscore.bridge.registry.ItemFactoryBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class NativeMinecraftRegistryBridge extends InMemoryRegistryBridge {
    private final NexusTarget target;
    private final ItemFactoryBridge itemFactory;
    private final BlockFactoryBridge blockFactory;
    private final List<PendingRegistration> pendingRegistrations = new ArrayList<>();
    private final Set<String> nativeRegistrations = new LinkedHashSet<>();

    public NativeMinecraftRegistryBridge(NexusTarget target) {
        this(target, new NativeMinecraftFactoryBridge(target), new NativeMinecraftFactoryBridge(target));
    }

    public NativeMinecraftRegistryBridge(NexusTarget target, ItemFactoryBridge itemFactory, BlockFactoryBridge blockFactory) {
        super(target);
        this.target = target;
        this.itemFactory = itemFactory;
        this.blockFactory = blockFactory;
    }

    @Override
    public synchronized <T> NexusEntry<T> register(String registryName, String id, NexusFactory<T> factory) {
        NexusEntry<T> entry = super.register(registryName, id, factory);
        if (usesRegisterEvent()) {
            pendingRegistrations.add(new PendingRegistration(registryName, id, entry));
            return entry;
        }
        Object nativeValue = nativeValue(registryName, entry.get());
        if (nativeValue != null && !isFallback(nativeValue)) {
            registerNative(registryName, id, nativeValue);
        }
        return entry;
    }

    public synchronized void registerWithLoaderEvent(Object event) {
        if (event == null || pendingRegistrations.isEmpty()) {
            return;
        }
        for (PendingRegistration registration : pendingRegistrations) {
            if (nativeRegistrations.contains(registration.key())) {
                continue;
            }
            try {
                if (registerWithEvent(event, registration)) {
                    nativeRegistrations.add(registration.key());
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
    }

    private Object nativeValue(String registryName, Object value) {
        if (value instanceof NexusItemHandle handle) {
            return handle.nativeItem();
        }
        if (value instanceof NexusBlockHandle handle) {
            if (NexusRegistries.BLOCKS.equals(registryName)) {
                return handle.nativeBlock();
            }
            if (NexusRegistries.BLOCK_ITEMS.equals(registryName)) {
                return handle.nativeBlockItem();
            }
        }
        if (value instanceof NexusCreativeTabDefinition definition) {
            return nativeCreativeTab(definition);
        }
        return value;
    }

    private Object nativeCreativeTab(NexusCreativeTabDefinition definition) {
        try {
            Class<?> tabClass = firstClass("net.minecraft.world.item.CreativeModeTab", "net.minecraft.item.ItemGroup");
            Method builderMethod = tabClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            Object title = component("itemGroup." + definition.fullId().replace(':', '.'));
            builder = invokeIfPresent(builder, "title", title);
            builder = invokeIfPresent(builder, "icon", iconSupplier(definition.iconItem()));
            builder = invokeIfPresent(builder, "displayItems", displayItemsProxy(builder, definition));
            Method build = builder.getClass().getMethod("build");
            return build.invoke(builder);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return definition;
        }
    }

    private void registerNative(String registryName, String id, Object value) {
        String nativeRegistry = NexusRegistries.BLOCK_ITEMS.equals(registryName) ? NexusRegistries.ITEMS : registryName;
        try {
            Object registry = registryObject(nativeRegistry);
            Object identifier = identifier(id);
            Method register = registryRegisterMethod(registry.getClass(), identifier.getClass(), value.getClass());
            register.invoke(null, registry, identifier, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private boolean registerWithEvent(Object event, PendingRegistration registration) throws ReflectiveOperationException {
        String nativeRegistry = NexusRegistries.BLOCK_ITEMS.equals(registration.registryName()) ? NexusRegistries.ITEMS : registration.registryName();
        Object registryKey = registryKey(nativeRegistry);
        Method getRegistryKey = findMethod(event.getClass(), "getRegistryKey", 0);
        if (getRegistryKey != null) {
            Object eventKey = getRegistryKey.invoke(event);
            if (!registryKey.equals(eventKey)) {
                return false;
            }
        }
        Object identifier = identifier(registration.id());
        Supplier<Object> supplier = () -> nativeValueForLoaderEvent(registration);
        for (Method method : event.getClass().getMethods()) {
            if (!"register".equals(method.getName()) || method.getParameterCount() != 3) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters[0].isInstance(registryKey) && parameters[1].isInstance(identifier) && parameters[2].isAssignableFrom(Supplier.class)) {
                method.invoke(event, registryKey, identifier, supplier);
                return true;
            }
        }
        return false;
    }

    private Object nativeValueForLoaderEvent(PendingRegistration registration) {
        Object value = registration.entry().get();
        if (value instanceof NexusItemHandle handle) {
            Object nativeItem = inRegistryContext(itemFactory, () -> itemFactory.createItem(handle.definition()));
            handle.attachNativeItem(nativeItem);
            return nativeItem;
        }
        if (value instanceof NexusBlockHandle handle) {
            if (NexusRegistries.BLOCKS.equals(registration.registryName())) {
                Object nativeBlock = inRegistryContext(blockFactory, () -> blockFactory.createBlock(handle.definition()));
                handle.attachNativeBlock(nativeBlock);
                return nativeBlock;
            }
            if (NexusRegistries.BLOCK_ITEMS.equals(registration.registryName())) {
                NexusBlockHandle blockHandle = blockHandle(registration.id(), handle);
                Object nativeBlock = blockHandle.nativeBlock();
                if (isFallback(nativeBlock)) {
                    nativeBlock = inRegistryContext(blockFactory, () -> blockFactory.createBlock(handle.definition()));
                    blockHandle.attachNativeBlock(nativeBlock);
                }
                Object finalNativeBlock = nativeBlock;
                Object nativeBlockItem = inRegistryContext(blockFactory, () -> blockFactory.createBlockItem(handle.definition(), finalNativeBlock));
                handle.attachNativeBlock(nativeBlock);
                handle.attachNativeBlockItem(nativeBlockItem);
                blockHandle.attachNativeBlockItem(nativeBlockItem);
                return nativeBlockItem;
            }
        }
        if (value instanceof NexusCreativeTabDefinition definition) {
            return nativeCreativeTab(definition);
        }
        return nativeValue(registration.registryName(), value);
    }

    @SuppressWarnings("unchecked")
    private <T> T inRegistryContext(Object factory, Supplier<T> supplier) {
        if (factory instanceof NativeMinecraftFactoryBridge nativeFactory) {
            return nativeFactory.inRegistryContext(supplier);
        }
        return supplier.get();
    }

    private NexusBlockHandle blockHandle(String id, NexusBlockHandle fallback) {
        return find(NexusRegistries.BLOCKS, id)
            .map(NexusEntry::get)
            .filter(NexusBlockHandle.class::isInstance)
            .map(NexusBlockHandle.class::cast)
            .orElse(fallback);
    }

    private Object registryObject(String registryName) throws ReflectiveOperationException {
        Class<?> registries = firstClass("net.minecraft.core.registries.BuiltInRegistries", "net.minecraft.registry.Registries");
        return registryField(registries, registryName).get(null);
    }

    private Object registryKey(String registryName) throws ReflectiveOperationException {
        Class<?> registries = firstClass("net.minecraft.core.registries.Registries", "net.minecraft.registry.Registries");
        return registryField(registries, registryName).get(null);
    }

    private Field registryField(Class<?> registries, String registryName) throws NoSuchFieldException {
        String fieldName = switch (registryName) {
            case NexusRegistries.ITEMS -> "ITEM";
            case NexusRegistries.BLOCKS -> "BLOCK";
            case NexusRegistries.CREATIVE_TABS -> "CREATIVE_MODE_TAB";
            default -> throw new NoSuchFieldException(registryName);
        };
        try {
            return registries.getField(fieldName);
        } catch (NoSuchFieldException error) {
            return registries.getField(fieldName + "S");
        }
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
            Constructor<?> constructor = identifier.getConstructor(String.class, String.class);
            return constructor.newInstance(namespace, path);
        } catch (NoSuchMethodException ignored) {
            Constructor<?> constructor = identifier.getConstructor(String.class);
            return constructor.newInstance(full);
        }
    }

    private Object component(String translationKey) throws ReflectiveOperationException {
        Class<?> component = firstClass("net.minecraft.network.chat.Component", "net.minecraft.text.Text");
        for (String methodName : new String[]{"translatable", "translatableWithFallback", "literal"}) {
            try {
                Method method = component.getMethod(methodName, String.class);
                return method.invoke(null, translationKey);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(component.getName() + ".translatable");
    }

    private Supplier<Object> iconSupplier(String iconItem) {
        return () -> {
            try {
                Object item = iconItem == null || iconItem.isBlank() ? vanillaItem("STONE") : registeredItem(iconItem);
                Class<?> stack = firstClass("net.minecraft.world.item.ItemStack", "net.minecraft.item.ItemStack");
                return stack.getConstructor(item.getClass()).newInstance(item);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
        };
    }

    private Object registeredItem(String id) throws ReflectiveOperationException {
        Object registry = registryObject(NexusRegistries.ITEMS);
        Object identifier = identifier(id.contains(":") ? id : "minecraft:" + id);
        for (Method method : registry.getClass().getMethods()) {
            if ((method.getName().equals("get") || method.getName().equals("getValue")) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(identifier.getClass())) {
                return method.invoke(registry, identifier);
            }
        }
        return vanillaItem("STONE");
    }

    private Object vanillaItem(String fieldName) throws ReflectiveOperationException {
        Class<?> items = firstClass("net.minecraft.world.item.Items", "net.minecraft.item.Items");
        return items.getField(fieldName).get(null);
    }

    private Object displayItemsProxy(Object builder, NexusCreativeTabDefinition definition) throws ClassNotFoundException {
        Method displayItems = findMethod(builder.getClass(), "displayItems", 1);
        if (displayItems == null || !displayItems.getParameterTypes()[0].isInterface()) {
            return null;
        }
        Class<?> displayItemsType = displayItems.getParameterTypes()[0];
        return Proxy.newProxyInstance(displayItemsType.getClassLoader(), new Class<?>[]{displayItemsType}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if (args != null && args.length > 0) {
                Object output = args[args.length - 1];
                for (String entry : definition.entries()) {
                    try {
                        acceptCreativeOutput(output, registeredItem(entry));
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                    }
                }
            }
            return null;
        });
    }

    private void acceptCreativeOutput(Object output, Object item) throws ReflectiveOperationException {
        for (Method method : output.getClass().getMethods()) {
            if ((method.getName().equals("accept") || method.getName().equals("add")) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(item.getClass())) {
                method.invoke(output, item);
                return;
            }
        }
    }

    private Method registryRegisterMethod(Class<?> registryClass, Class<?> identifierClass, Class<?> valueClass) throws ReflectiveOperationException {
        Class<?> registryType = firstClass("net.minecraft.core.Registry", "net.minecraft.registry.Registry");
        for (Method method : registryType.getMethods()) {
            if (!"register".equals(method.getName())) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 3
                && parameters[0].isAssignableFrom(registryClass)
                && parameters[1].isAssignableFrom(identifierClass)
                && parameters[2].isAssignableFrom(valueClass)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Registry.register");
    }

    private Object invokeIfPresent(Object receiver, String name, Object argument) throws ReflectiveOperationException {
        if (argument == null) {
            return receiver;
        }
        for (Method method : receiver.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(argument.getClass())) {
                Object result = method.invoke(receiver, argument);
                return result == null ? receiver : result;
            }
        }
        return receiver;
    }

    private Method findMethod(Class<?> type, String name, int parameterCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        return null;
    }

    private Class<?> firstClass(String... names) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String name : names) {
            for (ClassLoader loader : new ClassLoader[]{Thread.currentThread().getContextClassLoader(), NativeMinecraftRegistryBridge.class.getClassLoader()}) {
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

    private boolean usesRegisterEvent() {
        String loader = target.loader().id();
        return "forge".equals(loader) || "neoforge".equals(loader);
    }

    private boolean isFallback(Object value) {
        return value == null || value.getClass().getName().startsWith("com.rollylindenshnizzer.nexuscore.");
    }

    private record PendingRegistration(String registryName, String id, NexusEntry<?> entry) {
        String key() {
            return registryName + ":" + id;
        }
    }
}
