package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandContext;
import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandDefinition;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class NativeLoaderCommandBridge extends InMemoryCommandBridge {
    private final NexusTarget target;
    private boolean fabricCallbackInstalled;

    public NativeLoaderCommandBridge(NexusTarget target) {
        this.target = target;
    }

    @Override
    public synchronized void register(NexusCommandDefinition definition) {
        super.register(definition);
        installFabricLikeCallback();
    }

    public synchronized void registerWithDispatcher(Object dispatcher) {
        for (NexusCommandDefinition command : commands()) {
            registerWithDispatcher(dispatcher, command);
        }
    }

    private void installFabricLikeCallback() {
        if (fabricCallbackInstalled || (!"fabric".equals(target.loader().id()) && !"quilt".equals(target.loader().id()))) {
            return;
        }
        fabricCallbackInstalled = true;
        try {
            Class<?> callbackType = loadClass("net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback");
            Field eventField = callbackType.getField("EVENT");
            Object event = eventField.get(null);
            Object listener = Proxy.newProxyInstance(callbackType.getClassLoader(), new Class<?>[]{callbackType}, (proxy, method, args) -> {
                if ("register".equals(method.getName()) && args != null && args.length > 0) {
                    registerWithDispatcher(args[0]);
                }
                return null;
            });
            Method register = oneArgumentMethod(event.getClass(), "register");
            register.invoke(event, listener);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private void registerWithDispatcher(Object dispatcher, NexusCommandDefinition definition) {
        if (dispatcher == null) {
            return;
        }
        try {
            if (containsCommand(dispatcher, definition.literal())) {
                return;
            }
            Object builder = commandBuilder(definition);
            for (Method method : dispatcher.getClass().getMethods()) {
                if ("register".equals(method.getName()) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(builder.getClass())) {
                    method.invoke(dispatcher, builder);
                    return;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private boolean containsCommand(Object dispatcher, String literal) {
        try {
            Object root = dispatcher.getClass().getMethod("getRoot").invoke(dispatcher);
            Method getChild = root.getClass().getMethod("getChild", String.class);
            return getChild.invoke(root, literal) != null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private Object commandBuilder(NexusCommandDefinition definition) throws ReflectiveOperationException {
        Class<?> literalBuilderType = loadClass("com.mojang.brigadier.builder.LiteralArgumentBuilder");
        Object builder = literalBuilderType.getMethod("literal", String.class).invoke(null, definition.literal());
        if (definition.executor() != null) {
            Class<?> commandType = loadClass("com.mojang.brigadier.Command");
            Object command = Proxy.newProxyInstance(commandType.getClassLoader(), new Class<?>[]{commandType}, (proxy, method, args) -> {
                if ("run".equals(method.getName()) && args != null && args.length == 1) {
                    return execute(definition.literal(), new NexusCommandContext(args[0], null));
                }
                return 0;
            });
            builder = literalBuilderType.getMethod("executes", commandType).invoke(builder, command);
        }
        Method then = literalBuilderType.getMethod("then", loadClass("com.mojang.brigadier.builder.ArgumentBuilder"));
        for (NexusCommandDefinition child : definition.children()) {
            builder = then.invoke(builder, commandBuilder(child));
        }
        return builder;
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            try {
                return Class.forName(name, false, context);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Class.forName(name, false, NativeLoaderCommandBridge.class.getClassLoader());
    }

    private Method oneArgumentMethod(Class<?> type, String name) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (name.equals(method.getName()) && method.getParameterCount() == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException(type.getName() + "." + name);
    }
}
