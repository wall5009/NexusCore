package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.command.NexusCommandContext;
import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockHandle;
import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemHandle;
import com.rollylindenshnizzer.nexuscore.api.network.NetworkPacketDefinition;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketBuffer;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusFeature;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusPlatform;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.core.adapter.InMemoryNetworkBridge;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public final class TestAssertions {
    public static final String COMMON_MARKER = "NEXUSCORE_RUNTIME_ASSERTIONS_PASSED";
    public static final String CLIENT_MARKER = "NEXUSCORE_CLIENT_ASSERTIONS_PASSED";
    public static final String SERVER_MARKER = "NEXUSCORE_SERVER_ASSERTIONS_PASSED";
    public static final String SERVER_WORLD_MARKER = "NEXUSCORE_SERVER_WORLD_ASSERTIONS_PASSED";

    private static boolean commonAssertionsRan;

    private TestAssertions() {
    }

    public static synchronized void runCommonSetupAssertions() {
        if (commonAssertionsRan) {
            return;
        }
        commonAssertionsRan = true;
        require(TestEvents.commonSetupFired(), "common setup event did not fire before assertions");
        require(NexusServices.isInstalled(), "NexusCore services are not installed");
        require(NexusPlatform.supports(NexusFeature.REGISTRIES), "target does not declare registry support");
        require(NexusPlatform.supports(NexusFeature.NETWORKING), "target does not declare networking support");

        assertContent();
        assertCommand();
        assertNetwork();
        assertConfig();
        assertDatagen();

        NexusCoreTestMod.MOD.logger().info(COMMON_MARKER);
    }

    public static synchronized void runClientAssertions(Object client) {
        require(commonAssertionsRan, "common assertions did not run before client assertions");
        require(TestEvents.clientStartedFired(), "client started event did not fire");
        require(client != null, "client started event did not provide a native client object or loader event");
        NexusCoreTestMod.MOD.logger().info(CLIENT_MARKER);
    }

    public static synchronized void runServerAssertions(Object server) {
        require(commonAssertionsRan, "common assertions did not run before server assertions");
        require(TestEvents.serverStartedFired(), "server started event did not fire");
        assertServerWorld(server);
        NexusCoreTestMod.MOD.logger().info(SERVER_WORLD_MARKER);
        NexusCoreTestMod.MOD.logger().info(SERVER_MARKER);
    }

    private static void assertContent() {
        require(TestContent.TEST_GEM != null, "test gem entry is null");
        require(TestContent.TEST_BLOCK != null, "test block entry is null");
        require("nexuscore_testmod:test_gem".equals(TestContent.TEST_GEM.id()), "test gem id mismatch");
        require("nexuscore_testmod:test_block".equals(TestContent.TEST_BLOCK.id()), "test block id mismatch");
        require(NexusServices.get().registries().find(NexusRegistries.ITEMS, "nexuscore_testmod:test_gem").isPresent(), "test gem was not registered");
        require(NexusServices.get().registries().find(NexusRegistries.BLOCKS, "nexuscore_testmod:test_block").isPresent(), "test block was not registered");
        require(NexusServices.get().registries().find(NexusRegistries.BLOCK_ITEMS, "nexuscore_testmod:test_block").isPresent(), "test block item was not registered");
        require(NexusServices.get().registries().find(NexusRegistries.CREATIVE_TABS, "nexuscore_testmod:test_tab").isPresent(), "creative tab was not registered");
    }

    private static void assertCommand() {
        require(NexusServices.get().commands().find("nexuscore_test").isPresent(), "test command was not registered");
        NexusPlayer player = new NexusPlayer(null, "assertion");
        NexusCommandContext context = new NexusCommandContext(null, player);
        int result = NexusServices.get().commands().execute("nexuscore_test", context);
        require(result == 1, "test command returned " + result);
        require(context.replies().contains("Hello from NexusCore!"), "test command did not reply");
    }

    private static void assertNetwork() {
        NetworkPacketDefinition<?> definition = NexusServices.get().networking()
            .find("nexuscore_testmod:main/open_menu")
            .orElseThrow(() -> new IllegalStateException("test network packet was not registered"));
        TestNetwork.OpenMenuPacket packet = new TestNetwork.OpenMenuPacket("assertion_menu");
        NexusPacketBuffer buffer = new NexusPacketBuffer();
        encode(definition, packet, buffer);
        Object decoded = definition.decoder().decode(buffer);
        require(decoded instanceof TestNetwork.OpenMenuPacket decodedPacket && "assertion_menu".equals(decodedPacket.menuId()), "test network packet did not round-trip");
        if (NexusServices.get().networking() instanceof InMemoryNetworkBridge bridge) {
            NexusPlayer player = new NexusPlayer(null, "assertion");
            bridge.handle(definition.fullId(), decoded, player, NexusRuntimeSide.SERVER);
            require(player.messages().contains("open_menu:assertion_menu"), "test network handler did not receive context/player");
        }
        NetworkPacketDefinition<?> clientbound = NexusServices.get().networking()
            .find("nexuscore_testmod:main/open_menu_ack")
            .orElseThrow(() -> new IllegalStateException("test clientbound network packet was not registered"));
        TestNetwork.OpenMenuAckPacket clientboundPacket = new TestNetwork.OpenMenuAckPacket("assertion_menu");
        NexusPacketBuffer clientboundBuffer = new NexusPacketBuffer();
        encode(clientbound, clientboundPacket, clientboundBuffer);
        Object clientboundDecoded = clientbound.decoder().decode(clientboundBuffer);
        require(clientboundDecoded instanceof TestNetwork.OpenMenuAckPacket decodedPacket && "assertion_menu".equals(decodedPacket.menuId()), "test clientbound network packet did not round-trip");
    }

    private static void assertConfig() {
        require(TestConfig.CONFIG != null, "test config is null");
        require(TestConfig.CONFIG.getBoolean("enableRuby"), "boolean config value mismatch");
        require(TestConfig.CONFIG.getInt("rubySpawnRate") == 8, "int config value mismatch");
        require("Welcome from NexusCore".equals(TestConfig.CONFIG.getString("welcomeMessage")), "string config value mismatch");
        Path configPath = NexusServices.get().config().configPath("nexuscore_testmod");
        require(Files.isRegularFile(configPath), "config file does not exist at " + configPath);
    }

    private static void assertDatagen() {
        Path generated = NexusServices.get().paths().generatedResourcesDirectory();
        require(Files.isRegularFile(generated.resolve("data/nexuscore_testmod/recipes/test_block.json")), "recipe was not generated");
        require(Files.isRegularFile(generated.resolve("data/nexuscore_testmod/loot_tables/blocks/test_block.json")), "loot table was not generated");
        require(Files.isRegularFile(generated.resolve("data/nexuscore_testmod/tags/items/test_gems.json")), "tag was not generated");
        require(Files.isRegularFile(generated.resolve("assets/nexuscore_testmod/blockstates/test_block.json")), "blockstate was not generated");
        require(Files.isRegularFile(generated.resolve("assets/nexuscore_testmod/models/block/test_block.json")), "block model was not generated");
        require(Files.isRegularFile(generated.resolve("assets/nexuscore_testmod/models/item/test_gem.json")), "item model was not generated");
        require(Files.isRegularFile(generated.resolve("assets/nexuscore_testmod/lang/en_us.json")), "lang file was not generated");
    }

    private static void assertServerWorld(Object server) {
        require(server != null, "server started event did not provide a native Minecraft server");
        Object world = serverWorld(server);
        require(world != null, "native Minecraft server did not expose a loaded world/level");
        assertNativeHandles();
        assertNativeCommand(server, "nexuscore_test");
        assertWorldBlockRoundTrip(world, TestContent.TEST_BLOCK.get().nativeBlock(), "nexuscore_testmod:test_block");
    }

    private static Object serverWorld(Object server) {
        Object world = invokeNoArg(server, "overworld", "getOverworld", "method_3847");
        if (world == null) {
            world = firstWorld(invokeNoArg(server, "getAllLevels", "getWorlds"));
        }
        return world;
    }

    private static void assertNativeHandles() {
        NexusItemHandle gem = TestContent.TEST_GEM == null ? null : TestContent.TEST_GEM.get();
        NexusBlockHandle block = TestContent.TEST_BLOCK == null ? null : TestContent.TEST_BLOCK.get();
        require(gem != null && isNativeObject(gem.nativeItem()), "test gem did not create a native Minecraft item");
        require(block != null && isNativeObject(block.nativeBlock()), "test block did not create a native Minecraft block");
        require(isNativeObject(block.nativeBlockItem()), "test block item did not create a native Minecraft item");
    }

    private static void assertNativeCommand(Object server, String literal) {
        Object commandManager = invokeNoArg(server, "getCommands", "getCommandManager", "method_37301");
        require(commandManager != null, "native Minecraft server did not expose a command manager");
        Object dispatcher = invokeNoArg(commandManager, "getDispatcher", "method_9223");
        require(dispatcher != null, "native command manager did not expose a Brigadier dispatcher");
        Object root = invokeNoArg(dispatcher, "getRoot");
        require(root != null, "native command dispatcher did not expose a root node");
        InvocationResult child = invokeAny(root, new String[]{"getChild"}, literal);
        require(child.invoked() && child.value() != null, "native command dispatcher does not contain /" + literal);
    }

    private static void assertWorldBlockRoundTrip(Object world, Object nativeBlock, String id) {
        require(isNativeObject(nativeBlock), id + " is not backed by a native Minecraft block");
        Object blockState = invokeNoArg(nativeBlock, "defaultBlockState", "getDefaultState", "method_9564");
        require(blockState != null, id + " native block did not expose a default block state");
        Object position = assertionPosition(world);
        require(position != null, "could not create a native BlockPos for the in-world assertion");

        Object previousState = null;
        boolean placed = false;
        try {
            InvocationResult previous = invokeAny(world, new String[]{"getBlockState", "method_8320"}, position);
            require(previous.invoked() && previous.value() != null, "server world did not expose getBlockState(BlockPos)");
            previousState = previous.value();

            InvocationResult set = invokeAny(world, new String[]{"setBlock", "setBlockState", "method_8652"}, position, blockState, 3);
            require(set.invoked(), "server world did not expose setBlock/setBlockState(BlockPos, BlockState, int)");
            require(!Boolean.FALSE.equals(set.value()), "server world rejected placement for " + id);
            placed = true;

            InvocationResult current = invokeAny(world, new String[]{"getBlockState", "method_8320"}, position);
            require(current.invoked() && current.value() != null, "server world did not return the placed block state");
            Object currentBlock = invokeNoArg(current.value(), "getBlock", "method_26204");
            require(nativeBlock.equals(currentBlock), "server world did not contain " + id + " after placement");
        } finally {
            if (placed && previousState != null) {
                invokeAny(world, new String[]{"setBlock", "setBlockState", "method_8652"}, position, previousState, 3);
            }
        }
    }

    private static Object assertionPosition(Object world) {
        Object spawn = invokeNoArg(world, "getSharedSpawnPos", "getSpawnPos", "method_27908");
        if (spawn != null) {
            Object raised = invokeNoArg(spawn, "above", "up", "method_10084");
            return raised == null ? spawn : raised;
        }
        try {
            Class<?> blockPos = firstClass("net.minecraft.core.BlockPos", "net.minecraft.util.math.BlockPos", "net.minecraft.class_2338");
            Constructor<?> constructor = blockPos.getConstructor(int.class, int.class, int.class);
            return constructor.newInstance(0, 80, 0);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isNativeObject(Object value) {
        return value != null && !value.getClass().getName().startsWith("com.rollylindenshnizzer.nexuscore.");
    }

    private static Object firstWorld(Object worlds) {
        if (worlds instanceof Iterable<?> iterable) {
            Iterator<?> iterator = iterable.iterator();
            return iterator.hasNext() ? iterator.next() : null;
        }
        if (worlds != null && worlds.getClass().isArray() && Array.getLength(worlds) > 0) {
            return Array.get(worlds, 0);
        }
        return null;
    }

    private static Class<?> firstClass(String... classNames) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException error) {
                last = error;
            }
        }
        throw last == null ? new ClassNotFoundException("No class names supplied") : last;
    }

    private static Object invokeNoArg(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                if (method.getParameterCount() == 0) {
                    return method.invoke(target);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static InvocationResult invokeAny(Object target, String[] methodNames, Object... args) {
        if (target == null) {
            return new InvocationResult(false, null);
        }
        for (String methodName : methodNames) {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(methodName) && accepts(method.getParameterTypes(), args)) {
                    try {
                        return new InvocationResult(true, method.invoke(target, args));
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                    }
                }
            }
        }
        return new InvocationResult(false, null);
    }

    private static boolean accepts(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            Class<?> parameterType = boxed(parameterTypes[i]);
            if (!parameterType.isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return Void.class;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void encode(NetworkPacketDefinition definition, Object packet, NexusPacketBuffer buffer) {
        definition.encoder().encode(packet, buffer);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("NexusCore test assertion failed: " + message);
        }
    }

    private record InvocationResult(boolean invoked, Object value) {
    }
}
