package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.event.ClientStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.event.PlayerJoinedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.ServerStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.TickCallback;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class NativeLoaderEventBridge extends InMemoryEventBridge {
    private final NexusTarget target;

    public NativeLoaderEventBridge(NexusTarget target) {
        this.target = target;
    }

    @Override
    public void registerServerStarted(ServerStartedCallback callback) {
        super.registerServerStarted(callback);
        registerFabricEvent("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents", "SERVER_STARTED", "net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents$ServerStarted", args -> {
            Object server = args.length > 0 ? args[0] : null;
            registerNativeCommands(server);
            callback.onServerStarted(server);
        });
    }

    @Override
    public void registerClientStarted(ClientStartedCallback callback) {
        super.registerClientStarted(callback);
        registerFabricEvent("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents", "CLIENT_STARTED", "net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents$ClientStarted", args -> callback.onClientStarted(args.length > 0 ? args[0] : null));
    }

    @Override
    public void registerPlayerJoined(PlayerJoinedCallback callback) {
        super.registerPlayerJoined(callback);
        registerFabricEvent("net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents", "JOIN", "net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents$Join", args -> callback.onPlayerJoined(new NexusPlayer(args.length > 0 ? args[0] : null, "player")));
    }

    @Override
    public void registerServerTick(TickCallback callback) {
        super.registerServerTick(callback);
        registerFabricEvent("net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents", "END_SERVER_TICK", "net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents$EndTick", new NativeInvocation() {
            private long tick;

            @Override
            public void invoke(Object[] args) {
                callback.onTick(++tick);
            }
        });
    }

    @Override
    public void registerClientTick(TickCallback callback) {
        super.registerClientTick(callback);
        registerFabricEvent("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents", "END_CLIENT_TICK", "net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents$EndTick", new NativeInvocation() {
            private long tick;

            @Override
            public void invoke(Object[] args) {
                callback.onTick(++tick);
            }
        });
    }

    private void registerFabricEvent(String eventClassName, String fieldName, String listenerClassName, NativeInvocation invocation) {
        try {
            if (eventClassName.contains(".client.") && !isClientEnvironment()) {
                return;
            }
            Class<?> eventClass = loadClass(eventClassName);
            Field eventField = eventClass.getField(fieldName);
            Object event = eventField.get(null);
            Class<?> listenerClass = loadClass(listenerClassName);
            Object listener = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[]{listenerClass}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "toString" -> "NexusCore Fabric event listener for " + fieldName;
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> args != null && args.length == 1 && proxy == args[0];
                        default -> method.invoke(this, args);
                    };
                }
                invocation.invoke(args == null ? new Object[0] : args);
                return null;
            });
            Method register = fabricRegisterMethod(eventField.getType(), event.getClass(), listenerClass);
            if (register == null) {
                throw new NoSuchMethodException("Could not find Fabric event register method for " + listenerClassName + " on " + event.getClass().getName());
            }
            register.invoke(event, listener);
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw new IllegalStateException("NexusCore could not bind native Fabric/Quilt event " + eventClassName + "." + fieldName + " for target " + target.targetId(), error);
        }
    }

    private Method fabricRegisterMethod(Class<?> publicEventType, Class<?> implementationType, Class<?> listenerClass) {
        Method publicRegister = oneArgRegisterMethod(publicEventType, listenerClass);
        if (publicRegister != null) {
            return publicRegister;
        }
        return oneArgRegisterMethod(implementationType, listenerClass);
    }

    private Method oneArgRegisterMethod(Class<?> eventType, Class<?> listenerClass) {
        for (Method method : eventType.getMethods()) {
            if (!"register".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType == Object.class || parameterType.isAssignableFrom(listenerClass)) {
                return method;
            }
        }
        return null;
    }

    private boolean isClientEnvironment() {
        String environment = loaderEnvironment("net.fabricmc.loader.api.FabricLoader");
        if (environment == null) {
            environment = loaderEnvironment("org.quiltmc.loader.api.QuiltLoader");
        }
        return environment == null || "CLIENT".equalsIgnoreCase(environment);
    }

    private String loaderEnvironment(String loaderClassName) {
        try {
            Class<?> loader = loadClass(loaderClassName);
            Method getInstance = loader.getMethod("getInstance");
            Object instance = getInstance.invoke(null);
            Method getEnvironmentType = instance.getClass().getMethod("getEnvironmentType");
            Object environment = getEnvironmentType.invoke(instance);
            return environment == null ? null : environment.toString();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    public NexusTarget target() {
        return target;
    }

    private void registerNativeCommands(Object server) {
        if (server == null || !NexusServices.isInstalled()) {
            return;
        }
        if (NexusServices.get().commands() instanceof NativeLoaderCommandBridge commandBridge) {
            commandBridge.registerWithDispatcher(commandDispatcher(server));
        }
    }

    private Object commandDispatcher(Object server) {
        Object commandManager = invokeNoArg(server, "getCommands", "getCommandManager", "method_37301");
        if (commandManager == null) {
            return null;
        }
        return invokeNoArg(commandManager, "getDispatcher", "method_9223");
    }

    private Object invokeNoArg(Object target, String... names) {
        if (target == null) {
            return null;
        }
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                return method.invoke(target);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (ClassLoader loader : new ClassLoader[]{Thread.currentThread().getContextClassLoader(), NativeLoaderEventBridge.class.getClassLoader()}) {
            if (loader == null) {
                continue;
            }
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException error) {
                last = error;
            }
        }
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException error) {
            if (last != null) {
                error.addSuppressed(last);
            }
            throw error;
        }
    }

    @FunctionalInterface
    private interface NativeInvocation {
        void invoke(Object[] args);
    }
}
