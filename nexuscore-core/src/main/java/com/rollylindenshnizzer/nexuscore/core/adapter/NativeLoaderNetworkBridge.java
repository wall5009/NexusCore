package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.network.NetworkPacketDefinition;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketBuffer;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketDirection;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NativeLoaderNetworkBridge extends InMemoryNetworkBridge {
    private static final String PROTOCOL_VERSION = "1";

    private final NexusTarget target;
    private final Map<String, Object> forgeChannels = new LinkedHashMap<>();
    private final Map<String, Object> customPayloadTypes = new LinkedHashMap<>();
    private final Map<String, Object> customPayloadCodecs = new LinkedHashMap<>();
    private final Map<String, Boolean> registeredNeoForgePayloads = new LinkedHashMap<>();
    private int forgeDiscriminator;

    public NativeLoaderNetworkBridge(NexusTarget target) {
        this.target = target;
    }

    @Override
    public synchronized void registerServerbound(NetworkPacketDefinition<?> packet) {
        super.registerServerbound(packet);
        registerNativeReceiver(packet);
    }

    @Override
    public synchronized void registerClientbound(NetworkPacketDefinition<?> packet) {
        super.registerClientbound(packet);
        registerNativeReceiver(packet);
    }

    @Override
    public void sendToServer(Object packet) {
        super.sendToServer(packet);
        NetworkPacketDefinition<?> definition = findByPacket(packet, NexusPacketDirection.SERVERBOUND);
        if (definition == null) {
            throw networkFailure("send packet to server", packetName(packet), "the packet type has not been registered as serverbound", "register it with NexusNetwork.serverbound(...).register() before sending it.");
        }
        if (sendNativeToServer(definition, packet)) {
            return;
        }
        throw networkFailure("send packet to server", definition.fullId(), "no native " + target.loader().id() + " client-to-server networking path accepted the packet", "check that this code is running on a client and that the target loader networking API is present.");
    }

    @Override
    public void sendToPlayer(Object player, Object packet) {
        super.sendToPlayer(player, packet);
        NetworkPacketDefinition<?> definition = findByPacket(packet, NexusPacketDirection.CLIENTBOUND);
        if (definition == null) {
            throw networkFailure("send packet to player", packetName(packet), "the packet type has not been registered as clientbound", "register it with NexusNetwork.clientbound(...).register() before sending it.");
        }
        Object nativePlayer = unwrapNativePlayer(player);
        if (nativePlayer == null) {
            throw networkFailure("send packet to player", definition.fullId(), "the supplied player does not expose a native Minecraft player", "pass the NexusPlayer from an event or packet context, or pass the native loader player object.");
        }
        if (sendNativeToPlayer(nativePlayer, definition, packet)) {
            return;
        }
        throw networkFailure("send packet to player", definition.fullId(), "no native " + target.loader().id() + " server-to-client networking path accepted the packet", "check that this code is running on a server and that the target loader networking API is present.");
    }

    public synchronized void registerWithPayloadEvent(Object event) {
        if (!"neoforge".equals(target.loader().id()) || event == null) {
            return;
        }
        try {
            Object registrar = invoke(event, "registrar", new Class<?>[]{String.class}, PROTOCOL_VERSION);
            if (registrar == null) {
                return;
            }
            executeNeoForgePayloadsOnMain(registrar);
            for (NetworkPacketDefinition<?> packet : packets()) {
                registerNeoForgePayload(registrar, packet);
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw networkFailure("register NeoForge packet payloads", target.targetId(), error.getMessage(), "make sure the generated NeoForge entrypoint forwards RegisterPayloadHandlersEvent to NexusCore.", error);
        }
    }

    private void registerNativeReceiver(NetworkPacketDefinition<?> packet) {
        try {
            switch (target.loader().id()) {
                case "fabric", "quilt" -> registerFabricReceiver(packet);
                case "forge" -> registerForgePacket(packet);
                case "neoforge" -> {
                    // NeoForge payload registration is event-based. The generated NeoForge entrypoint calls registerWithPayloadEvent.
                }
                default -> {
                }
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw networkFailure("register native packet", packet.fullId(), error.getMessage(), "register packets during your mod init method and verify that the target loader networking API is available.", error);
        }
    }

    private boolean sendNativeToServer(NetworkPacketDefinition<?> definition, Object packet) {
        try {
            return switch (target.loader().id()) {
                case "fabric", "quilt" -> sendFabricToServer(definition, packet);
                case "forge" -> sendForgeToServer(definition, packet);
                case "neoforge" -> sendNeoForgeToServer(definition, packet);
                default -> false;
            };
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw networkFailure("send packet to server", definition.fullId(), error.getMessage(), "check packet registration, target side, and loader networking dependencies.", error);
        }
    }

    private boolean sendNativeToPlayer(Object player, NetworkPacketDefinition<?> definition, Object packet) {
        try {
            return switch (target.loader().id()) {
                case "fabric", "quilt" -> sendFabricToPlayer(player, definition, packet);
                case "forge" -> sendForgeToPlayer(player, definition, packet);
                case "neoforge" -> sendNeoForgeToPlayer(player, definition, packet);
                default -> false;
            };
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw networkFailure("send packet to player", definition.fullId(), error.getMessage(), "check packet registration, target side, and loader networking dependencies.", error);
        }
    }

    private void registerFabricReceiver(NetworkPacketDefinition<?> packet) throws ReflectiveOperationException {
        registerFabricPayloadType(packet);
        String className = packet.direction() == NexusPacketDirection.SERVERBOUND
            ? "net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking"
            : "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking";
        Class<?> networking;
        try {
            networking = loadClass(className);
        } catch (ClassNotFoundException missingSideApi) {
            // Clientbound packets are commonly registered from shared init on dedicated
            // servers; the server only needs the payload type and send path.
            return;
        } catch (RuntimeException missingSideApi) {
            if (isSideOnlyNetworkingClassFailure(className, missingSideApi)) {
                return;
            }
            throw missingSideApi;
        }
        for (Method method : networking.getMethods()) {
            if (!"registerGlobalReceiver".equals(method.getName()) || method.getParameterCount() != 2 || !method.getParameterTypes()[1].isInterface()) {
                continue;
            }
            Class<?> idType = method.getParameterTypes()[0];
            boolean customPayload = isCustomPayloadType(idType);
            if (!customPayload && !isIdentifier(idType)) {
                continue;
            }
            Object id = customPayload ? customPayloadType(packet) : nativeId(idType, packet.channel(), packet.id());
            Object receiver = Proxy.newProxyInstance(method.getParameterTypes()[1].getClassLoader(), new Class<?>[]{method.getParameterTypes()[1]}, (proxy, invoked, args) -> {
                if (isObjectMethod(invoked)) {
                    return objectMethod(proxy, invoked, args);
                }
                decodeAndHandle(packet, args == null ? new Object[0] : args);
                return defaultValue(invoked.getReturnType());
            });
            method.invoke(null, id, receiver);
            return;
        }
    }

    private void registerFabricPayloadType(NetworkPacketDefinition<?> packet) {
        try {
            Class<?> registryClass = loadClass("net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry");
            String[] methods = packet.direction() == NexusPacketDirection.SERVERBOUND
                ? new String[]{"playC2S", "serverboundPlay"}
                : new String[]{"playS2C", "clientboundPlay"};
            Object registry = null;
            for (String methodName : methods) {
                try {
                    registry = registryClass.getMethod(methodName).invoke(null);
                    break;
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (registry == null) {
                return;
            }
            Method register = findMethod(registry.getClass(), "register", 2);
            if (register != null) {
                register.invoke(registry, customPayloadType(packet), customPayloadCodec(packet));
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Fabric 1.20.x uses Identifier + PacketByteBuf channels and has no payload type registry.
        }
    }

    private boolean sendFabricToServer(NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        if (sendFabricCustomPayload("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking", null, definition, packet)) {
            return true;
        }
        Class<?> networking = loadClass("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
        for (Method method : networking.getMethods()) {
            if (!"send".equals(method.getName()) || method.getParameterCount() != 2) {
                continue;
            }
            Object id = nativeId(method.getParameterTypes()[0], definition.channel(), definition.id());
            Object buffer = nativePacketBuffer(method.getParameterTypes()[1]);
            if (buffer != null) {
                encode(definition, packet, buffer);
                method.invoke(null, id, buffer);
                return true;
            }
        }
        return false;
    }

    private boolean sendFabricToPlayer(Object player, NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        if (sendFabricCustomPayload("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking", player, definition, packet)) {
            return true;
        }
        Class<?> networking = loadClass("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");
        for (Method method : networking.getMethods()) {
            if (!"send".equals(method.getName()) || method.getParameterCount() != 3 || !method.getParameterTypes()[0].isInstance(player)) {
                continue;
            }
            Object id = nativeId(method.getParameterTypes()[1], definition.channel(), definition.id());
            Object buffer = nativePacketBuffer(method.getParameterTypes()[2]);
            if (buffer != null) {
                encode(definition, packet, buffer);
                method.invoke(null, player, id, buffer);
                return true;
            }
        }
        return false;
    }

    private boolean sendFabricCustomPayload(String className, Object player, NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Class<?> networking = loadClass(className);
        Object payload;
        try {
            payload = customPayload(definition, packet);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        for (Method method : networking.getMethods()) {
            if (!"send".equals(method.getName())) {
                continue;
            }
            if (player == null && method.getParameterCount() == 1 && method.getParameterTypes()[0].isInstance(payload)) {
                method.invoke(null, payload);
                return true;
            }
            if (player != null && method.getParameterCount() == 2 && method.getParameterTypes()[0].isInstance(player) && method.getParameterTypes()[1].isInstance(payload)) {
                method.invoke(null, player, payload);
                return true;
            }
        }
        return false;
    }

    private void registerForgePacket(NetworkPacketDefinition<?> packet) throws ReflectiveOperationException {
        Object channel = forgeChannel(packet.channel());
        Class<?> directionClass = loadClass("net.minecraftforge.network.NetworkDirection");
        Object direction = enumConstant(directionClass, packet.direction() == NexusPacketDirection.SERVERBOUND ? "PLAY_TO_SERVER" : "PLAY_TO_CLIENT");
        Method messageBuilder = channel.getClass().getMethod("messageBuilder", Class.class, int.class, directionClass);
        Object builder = messageBuilder.invoke(channel, packet.packetType(), forgeDiscriminator++, direction);
        invokeBuilder(builder, "encoder", (BiConsumer<Object, Object>) (message, buffer) -> encode(packet, message, buffer));
        invokeBuilder(builder, "decoder", (Function<Object, Object>) buffer -> decode(packet, buffer));
        invokeBuilder(builder, "consumerMainThread", (BiConsumer<Object, Object>) (message, contextSupplier) -> handleForgePacket(packet, message, contextSupplier));
        Method add = builder.getClass().getMethod("add");
        add.invoke(builder);
    }

    private Object forgeChannel(String channelId) throws ReflectiveOperationException {
        Object existing = forgeChannels.get(channelId);
        if (existing != null) {
            return existing;
        }
        Class<?> registryClass = loadClass("net.minecraftforge.network.NetworkRegistry");
        for (Method method : registryClass.getMethods()) {
            if (!"newSimpleChannel".equals(method.getName()) || method.getParameterCount() != 4) {
                continue;
            }
            Object id = nativeChannelId(method.getParameterTypes()[0], channelId);
            Supplier<String> versionSupplier = () -> PROTOCOL_VERSION;
            Predicate<String> versionPredicate = version -> PROTOCOL_VERSION.equals(version);
            Object channel = method.invoke(null, id, versionSupplier, versionPredicate, versionPredicate);
            forgeChannels.put(channelId, channel);
            return channel;
        }
        throw new NoSuchMethodException("NetworkRegistry.newSimpleChannel");
    }

    private boolean sendForgeToServer(NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Object channel = forgeChannel(definition.channel());
        Method sendToServer = channel.getClass().getMethod("sendToServer", Object.class);
        sendToServer.invoke(channel, packet);
        return true;
    }

    private boolean sendForgeToPlayer(Object player, NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Object channel = forgeChannel(definition.channel());
        Class<?> distributorClass = loadClass("net.minecraftforge.network.PacketDistributor");
        Object playerDistributor = distributorClass.getField("PLAYER").get(null);
        Method with = playerDistributor.getClass().getMethod("with", Supplier.class);
        Object packetTarget = with.invoke(playerDistributor, (Supplier<Object>) () -> player);
        Method send = findMethod(channel.getClass(), "send", 2);
        if (send == null) {
            return false;
        }
        send.invoke(channel, packetTarget, packet);
        return true;
    }

    private void handleForgePacket(NetworkPacketDefinition<?> definition, Object message, Object contextSupplier) {
        Object context = null;
        try {
            context = invoke(contextSupplier, "get", new Class<?>[0]);
            handle(definition.fullId(), message, nativePlayer(new Object[]{context}), sideFor(definition));
            invokeIfPresent(context, "setPacketHandled", true);
        } catch (ReflectiveOperationException | RuntimeException error) {
            throw networkFailure("handle Forge packet", definition.fullId(), error.getMessage(), "check the packet decoder and handler for thrown exceptions.", error);
        }
    }

    private void registerNeoForgePayload(Object registrar, NetworkPacketDefinition<?> packet) throws ReflectiveOperationException {
        if (registeredNeoForgePayloads.putIfAbsent(packet.fullId(), Boolean.TRUE) != null) {
            return;
        }
        Object type = customPayloadType(packet);
        Object codec = customPayloadCodec(packet);
        Class<?> handlerClass = loadClass("net.neoforged.neoforge.network.handling.IPayloadHandler");
        Object handler = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[]{handlerClass}, (proxy, method, args) -> {
            if (isObjectMethod(method)) {
                return objectMethod(proxy, method, args);
            }
            if ("handle".equals(method.getName()) && args != null && args.length >= 2) {
                Object decoded = packetFromPayload(args[0]);
                handle(packet.fullId(), decoded == null ? args[0] : decoded, nativePlayer(new Object[]{args[1]}), sideFor(packet));
            }
            return defaultValue(method.getReturnType());
        });
        String methodName = packet.direction() == NexusPacketDirection.SERVERBOUND ? "playToServer" : "playToClient";
        for (Method method : registrar.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 3) {
                method.invoke(registrar, type, codec, handler);
                return;
            }
        }
        throw new NoSuchMethodException("PayloadRegistrar." + methodName);
    }

    private void executeNeoForgePayloadsOnMain(Object registrar) {
        try {
            Class<?> handlerThread = loadClass("net.neoforged.neoforge.network.registration.HandlerThread");
            Object main = enumConstant(handlerThread, "MAIN");
            invoke(registrar, "executesOn", new Class<?>[]{handlerThread}, main);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private boolean sendNeoForgeToServer(NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Object payload = customPayload(definition, packet);
        for (String className : new String[]{"net.neoforged.neoforge.client.network.ClientPacketDistributor", "net.neoforged.neoforge.network.PacketDistributor"}) {
            if (invokeStaticPayloadSend(className, "sendToServer", null, payload)) {
                return true;
            }
        }
        return false;
    }

    private boolean sendNeoForgeToPlayer(Object player, NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Object payload = customPayload(definition, packet);
        return invokeStaticPayloadSend("net.neoforged.neoforge.network.PacketDistributor", "sendToPlayer", player, payload);
    }

    private boolean invokeStaticPayloadSend(String className, String methodName, Object firstArgument, Object payload) throws ReflectiveOperationException {
        Class<?> type;
        try {
                type = loadClass(className);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        for (Method method : type.getMethods()) {
            if (!methodName.equals(method.getName()) || !Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (firstArgument == null && parameters.length >= 1 && parameters[0].isInstance(payload)) {
                method.invoke(null, invocationArguments(parameters, 1, null, payload));
                return true;
            }
            if (firstArgument != null && parameters.length >= 2 && parameters[0].isInstance(firstArgument) && parameters[1].isInstance(payload)) {
                method.invoke(null, invocationArguments(parameters, 2, firstArgument, payload));
                return true;
            }
        }
        return false;
    }

    private Object[] invocationArguments(Class<?>[] parameters, int fixedCount, Object firstArgument, Object payload) {
        Object[] values = new Object[parameters.length];
        if (firstArgument == null) {
            values[0] = payload;
        } else {
            values[0] = firstArgument;
            values[1] = payload;
        }
        for (int i = fixedCount; i < parameters.length; i++) {
            Class<?> parameter = parameters[i];
            values[i] = parameter.isArray() ? Array.newInstance(parameter.getComponentType(), 0) : defaultValue(parameter);
        }
        return values;
    }

    private Object customPayloadType(NetworkPacketDefinition<?> definition) throws ReflectiveOperationException {
        Object existing = customPayloadTypes.get(definition.fullId());
        if (existing != null) {
            return existing;
        }
        Class<?> type = firstClass(
            "net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type",
            "net.minecraft.class_8710$class_9154"
        );
        for (Constructor<?> constructor : type.getConstructors()) {
            if (constructor.getParameterCount() == 1) {
                Object id = nativeId(constructor.getParameterTypes()[0], definition.channel(), definition.id());
                Object payloadType = constructor.newInstance(id);
                customPayloadTypes.put(definition.fullId(), payloadType);
                return payloadType;
            }
        }
        throw new NoSuchMethodException(type.getName() + " constructor");
    }

    private Object customPayloadCodec(NetworkPacketDefinition<?> definition) throws ReflectiveOperationException {
        Object existing = customPayloadCodecs.get(definition.fullId());
        if (existing != null) {
            return existing;
        }
        Class<?> codecClass = firstClass("net.minecraft.network.codec.StreamCodec", "net.minecraft.class_9139");
        Object codec = Proxy.newProxyInstance(codecClass.getClassLoader(), new Class<?>[]{codecClass}, (proxy, method, args) -> {
            if (isObjectMethod(method)) {
                return objectMethod(proxy, method, args);
            }
            if ("encode".equals(method.getName()) && args != null && args.length == 2) {
                Object packet = packetFromPayload(args[1]);
                encode(definition, packet == null ? args[1] : packet, args[0]);
                return null;
            }
            if ("decode".equals(method.getName()) && args != null && args.length == 1) {
                return customPayload(definition, decode(definition, args[0]));
            }
            return defaultValue(method.getReturnType());
        });
        customPayloadCodecs.put(definition.fullId(), codec);
        return codec;
    }

    private Object customPayload(NetworkPacketDefinition<?> definition, Object packet) throws ReflectiveOperationException {
        Class<?> payloadClass = firstClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload", "net.minecraft.class_8710");
        if (payloadClass.isInstance(packet)) {
            return packet;
        }
        Object type = customPayloadType(definition);
        return Proxy.newProxyInstance(payloadClass.getClassLoader(), new Class<?>[]{payloadClass}, new NexusPayloadInvocation(definition, type, packet));
    }

    private Object packetFromPayload(Object payload) {
        if (payload != null && Proxy.isProxyClass(payload.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(payload);
            if (handler instanceof NexusPayloadInvocation nexusPayload) {
                return nexusPayload.packet();
            }
        }
        return null;
    }

    private Object decode(NetworkPacketDefinition<?> definition, Object nativeBuffer) {
        return definition.decoder().decode(nativeBuffer == null ? new NexusPacketBuffer() : NexusPacketBuffer.wrap(nativeBuffer));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void encode(NetworkPacketDefinition definition, Object packet, Object nativeBuffer) {
        NexusPacketBuffer buffer = nativeBuffer == null ? new NexusPacketBuffer() : NexusPacketBuffer.wrap(nativeBuffer);
        definition.encoder().encode(packet, buffer);
    }

    private NetworkPacketDefinition<?> findByPacket(Object packet, NexusPacketDirection direction) {
        if (packet == null) {
            return null;
        }
        List<NetworkPacketDefinition<?>> packets = packets();
        for (NetworkPacketDefinition<?> definition : packets) {
            if (definition.direction() == direction && definition.packetType().isInstance(packet)) {
                return definition;
            }
        }
        return null;
    }

    private void decodeAndHandle(NetworkPacketDefinition<?> definition, Object[] args) {
        try {
            Object packet = packetFromArguments(definition, args);
            handle(definition.fullId(), packet, nativePlayer(args), sideFor(definition));
        } catch (RuntimeException error) {
            throw networkFailure("handle packet", definition.fullId(), error.getMessage(), "check the packet decoder and handler for thrown exceptions.", error);
        }
    }

    private Object packetFromArguments(NetworkPacketDefinition<?> definition, Object[] args) {
        for (Object arg : args) {
            Object payloadPacket = packetFromPayload(arg);
            if (payloadPacket != null) {
                return payloadPacket;
            }
            if (definition.packetType().isInstance(arg)) {
                return arg;
            }
        }
        Object nativeBuffer = nativeBuffer(args);
        return decode(definition, nativeBuffer);
    }

    private Object nativeBuffer(Object[] args) {
        for (Object arg : args) {
            if (arg != null && looksLikePacketBuffer(arg.getClass())) {
                return arg;
            }
        }
        return null;
    }

    private Object nativePacketBuffer(Class<?> requestedType) throws ReflectiveOperationException {
        if (!looksLikePacketBuffer(requestedType)) {
            return null;
        }
        Object byteBuf = byteBuf();
        for (Constructor<?> constructor : requestedType.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].isInstance(byteBuf)) {
                return constructor.newInstance(byteBuf);
            }
        }
        Constructor<?> constructor = requestedType.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object byteBuf() throws ReflectiveOperationException {
        Class<?> unpooled = loadClass("io.netty.buffer.Unpooled");
        for (String methodName : new String[]{"buffer", "wrappedBuffer"}) {
            try {
                Method method = unpooled.getMethod(methodName);
                return method.invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("io.netty.buffer.Unpooled.buffer");
    }

    private Object nativeChannelId(Class<?> type, String channel) throws ReflectiveOperationException {
        String[] parts = channel.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        return nativeIdentifier(type, namespace, path);
    }

    private Object nativeId(Class<?> type, String channel, String packetId) throws ReflectiveOperationException {
        String[] parts = channel.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = (parts.length == 2 ? parts[1] : parts[0]) + "/" + packetId;
        return nativeIdentifier(type, namespace, path);
    }

    private Object nativeIdentifier(Class<?> type, String namespace, String path) throws ReflectiveOperationException {
        if (isIdentifier(type)) {
            return createIdentifier(type, namespace, path);
        }
        for (Constructor<?> constructor : type.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 1) {
                return constructor.newInstance(nativeIdentifier(parameters[0], namespace, path));
            }
        }
        throw new NoSuchMethodException(type.getName() + " id constructor");
    }

    private Object createIdentifier(Class<?> type, String namespace, String path) throws ReflectiveOperationException {
        for (String methodName : new String[]{"fromNamespaceAndPath", "of", "method_60655", "method_43902"}) {
            try {
                Method method = type.getMethod(methodName, String.class, String.class);
                if (type.isAssignableFrom(method.getReturnType())) {
                    return method.invoke(null, namespace, path);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        String full = namespace + ":" + path;
        for (String methodName : new String[]{"parse", "tryParse", "method_60654", "method_12829"}) {
            try {
                Method method = type.getMethod(methodName, String.class);
                if (type.isAssignableFrom(method.getReturnType())) {
                    return method.invoke(null, full);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        try {
            return type.getConstructor(String.class, String.class).newInstance(namespace, path);
        } catch (NoSuchMethodException ignored) {
            return type.getConstructor(String.class).newInstance(full);
        }
    }

    private boolean isIdentifier(Class<?> type) {
        String name = type.getName();
        return name.equals("net.minecraft.resources.ResourceLocation")
            || name.equals("net.minecraft.resources.Identifier")
            || name.equals("net.minecraft.util.Identifier")
            || name.equals("net.minecraft.class_2960");
    }

    private boolean isCustomPayloadType(Class<?> type) {
        String name = type.getName();
        return name.endsWith("CustomPacketPayload$Type") || name.equals("net.minecraft.class_8710$class_9154");
    }

    private boolean isSideOnlyNetworkingClassFailure(String className, RuntimeException error) {
        String message = String.valueOf(error.getMessage()).toLowerCase();
        String expectedClass = className.toLowerCase();
        return expectedClass.contains(".client.")
            && message.contains(expectedClass)
            && (message.contains("environment type server") || message.contains("mismatched @env"));
    }

    private boolean looksLikePacketBuffer(Class<?> type) {
        String name = type.getName();
        if (name.endsWith("FriendlyByteBuf")
            || name.endsWith("PacketByteBuf")
            || name.endsWith("RegistryFriendlyByteBuf")
            || name.equals("net.minecraft.class_2540")
            || name.equals("net.minecraft.class_9129")) {
            return true;
        }
        return hasMethod(type, "writeUtf")
            || hasMethod(type, "writeString")
            || hasMethod(type, "readUtf")
            || hasMethod(type, "readString")
            || hasMethod(type, "method_10814")
            || hasMethod(type, "method_19772");
    }

    private boolean hasMethod(Class<?> type, String name) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private NexusPlayer nativePlayer(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof NexusPlayer nexusPlayer) {
                return nexusPlayer;
            }
            Object player = unwrapNativePlayer(arg);
            if (player != null) {
                return new NexusPlayer(player, playerName(player));
            }
        }
        return null;
    }

    private Object unwrapNativePlayer(Object player) {
        if (player == null) {
            return null;
        }
        if (player instanceof NexusPlayer nexusPlayer) {
            return nexusPlayer.nativePlayer();
        }
        if (looksLikePlayer(player.getClass())) {
            return player;
        }
        for (String methodName : new String[]{"player", "getPlayer", "getSender", "sender"}) {
            try {
                Method method = player.getClass().getMethod(methodName);
                if (method.getParameterCount() == 0) {
                    Object value = method.invoke(player);
                    if (value != null && looksLikePlayer(value.getClass())) {
                        return value;
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private boolean looksLikePlayer(Class<?> type) {
        String name = type.getName().toLowerCase();
        return name.contains("player") || name.contains("servergamepacketlistener") || name.contains("clientgamepacketlistener");
    }

    private String playerName(Object player) {
        for (String methodName : new String[]{"getGameProfile", "getName", "getDisplayName", "method_7334", "method_5477"}) {
            try {
                Object value = player.getClass().getMethod(methodName).invoke(player);
                String name = nameFromValue(value);
                if (name != null) {
                    return name;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return "player";
    }

    private String nameFromValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence sequence) {
            return sequence.toString();
        }
        try {
            Object nested = value.getClass().getMethod("getName").invoke(value);
            if (nested instanceof CharSequence sequence) {
                return sequence.toString();
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private NexusRuntimeSide sideFor(NetworkPacketDefinition<?> definition) {
        return definition.direction() == NexusPacketDirection.SERVERBOUND ? NexusRuntimeSide.SERVER : NexusRuntimeSide.CLIENT;
    }

    private Object enumConstant(Class<?> enumClass, String name) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object value = Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
        return value;
    }

    private void invokeBuilder(Object builder, String methodName, Object argument) throws ReflectiveOperationException {
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isInstance(argument)) {
                method.invoke(builder, argument);
                return;
            }
        }
        throw new NoSuchMethodException(builder.getClass().getName() + "." + methodName);
    }

    private Object invoke(Object receiver, String name, Class<?>[] parameterTypes, Object... arguments) throws ReflectiveOperationException {
        if (receiver == null) {
            return null;
        }
        Method method = receiver.getClass().getMethod(name, parameterTypes);
        return method.invoke(receiver, arguments);
    }

    private void invokeIfPresent(Object receiver, String name, Object argument) {
        if (receiver == null) {
            return;
        }
        for (Method method : receiver.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                try {
                    method.invoke(receiver, argument);
                    return;
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }
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
            try {
                return loadClass(name);
            } catch (ClassNotFoundException error) {
                last = error;
            }
        }
        throw last == null ? new ClassNotFoundException("No class names supplied") : last;
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (ClassLoader loader : new ClassLoader[]{Thread.currentThread().getContextClassLoader(), NativeLoaderNetworkBridge.class.getClassLoader()}) {
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

    private boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private Object objectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "NexusCoreNativeProxy[" + target.targetId() + "]";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
            default -> null;
        };
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == Void.TYPE) {
            return null;
        }
        if (type == Boolean.TYPE) {
            return false;
        }
        if (type == Character.TYPE) {
            return '\0';
        }
        return 0;
    }

    private String packetName(Object packet) {
        return packet == null ? "null" : packet.getClass().getName();
    }

    private IllegalStateException networkFailure(String action, String id, String reason, String fix) {
        return networkFailure(action, id, reason, fix, null);
    }

    private IllegalStateException networkFailure(String action, String id, String reason, String fix, Throwable cause) {
        String message = "NexusCore could not " + action + " '" + id + "'. Target: " + target.targetId() + ". Reason: " + (reason == null || reason.isBlank() ? "unknown loader networking failure" : reason) + ". Fix: " + fix;
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }

    private static final class NexusPayloadInvocation implements InvocationHandler {
        private final NetworkPacketDefinition<?> definition;
        private final Object type;
        private final Object packet;

        private NexusPayloadInvocation(NetworkPacketDefinition<?> definition, Object type, Object packet) {
            this.definition = Objects.requireNonNull(definition, "definition");
            this.type = Objects.requireNonNull(type, "type");
            this.packet = Objects.requireNonNull(packet, "packet");
        }

        Object packet() {
            return packet;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "NexusPayload[" + definition.fullId() + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                    default -> null;
                };
            }
            if ("type".equals(method.getName()) || "method_56479".equals(method.getName())) {
                return type;
            }
            return null;
        }
    }
}
