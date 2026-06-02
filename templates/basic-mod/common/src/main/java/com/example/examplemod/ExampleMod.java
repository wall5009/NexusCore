package com.example.examplemod;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;
import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockHandle;
import com.rollylindenshnizzer.nexuscore.api.block.NexusBlocks;
import com.rollylindenshnizzer.nexuscore.api.command.NexusCommands;
import com.rollylindenshnizzer.nexuscore.api.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.api.datagen.NexusDataGen;
import com.rollylindenshnizzer.nexuscore.api.event.NexusEvents;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemHandle;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItems;
import com.rollylindenshnizzer.nexuscore.api.network.NetworkPacketDefinition;
import com.rollylindenshnizzer.nexuscore.api.network.NexusNetwork;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacket;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketBuffer;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusCreativeTabs;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.core.adapter.InMemoryNetworkBridge;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public final class ExampleMod {
    public static final String COMMON_MARKER = "NEXUSCORE_RUNTIME_ASSERTIONS_PASSED";
    public static final String CLIENT_MARKER = "NEXUSCORE_CLIENT_ASSERTIONS_PASSED";
    public static final String SERVER_MARKER = "NEXUSCORE_SERVER_ASSERTIONS_PASSED";
    public static final String SERVER_WORLD_MARKER = "NEXUSCORE_SERVER_WORLD_ASSERTIONS_PASSED";
    public static final NexusMod MOD = NexusMod.create("examplemod");
    public static final NexusNetwork NETWORK = NexusNetwork.create(MOD.id(), "main");
    public static final NexusConfig CONFIG = NexusConfig.create(MOD.id())
        .booleanValue("enableRuby", true)
        .intValue("rubySpawnRate", 8, 0, 64)
        .build();
    private static NexusEntry<NexusItemHandle> ruby;
    private static NexusEntry<NexusBlockHandle> rubyBlock;
    private static boolean commonAssertionsRan;

    private ExampleMod() {
    }

    public static void init() {
        MOD.logger().info("Example Mod loaded through NexusCore.");

        NexusCreativeTabs.create(MOD, "main")
            .title("Example Mod")
            .icon("examplemod:ruby")
            .entry("examplemod:ruby")
            .entry("examplemod:ruby_block")
            .register();

        ruby = NexusItems.create(MOD, "ruby")
            .creativeTab("examplemod:main")
            .register();

        rubyBlock = NexusBlocks.create(MOD, "ruby_block")
            .strength(4.0f)
            .requiresTool()
            .creativeTab("examplemod:main")
            .withSimpleItem()
            .register();

        NexusEvents.COMMON_SETUP.register(ExampleMod::runCommonAssertions);
        NexusEvents.CLIENT_STARTED.register(client -> {
            require(commonAssertionsRan, "common assertions did not run before client startup");
            require(client != null, "client startup did not provide a native client object or loader event");
            MOD.logger().info(CLIENT_MARKER);
        });
        NexusEvents.SERVER_STARTED.register(server -> {
            require(commonAssertionsRan, "common assertions did not run before server startup");
            assertServerWorld(server);
            MOD.logger().info("Server started.");
            MOD.logger().info(SERVER_WORLD_MARKER);
            MOD.logger().info(SERVER_MARKER);
        });
        NexusEvents.PLAYER_JOINED.register(player -> {
            player.sendMessage("Welcome from Example Mod.");
            NETWORK.sendToPlayer(player, new PongPacket("welcome"));
        });

        NexusCommands.literal("example")
            .executes(context -> {
                context.reply("Hello from NexusCore.");
                return 1;
            })
            .register();

        NETWORK.serverbound("ping", PingPacket.class, PingPacket::decode)
            .encoder(PingPacket::encode)
            .handler((packet, context) -> MOD.logger().info("Received ping: " + packet.message()))
            .register();
        NETWORK.clientbound("pong", PongPacket.class, PongPacket::decode)
            .encoder(PongPacket::encode)
            .handler((packet, context) -> {
                MOD.logger().info("Received pong: " + packet.message());
                if (context.side() == NexusRuntimeSide.CLIENT) {
                    NETWORK.sendToServer(new PingPacket("reply:" + packet.message()));
                }
            })
            .register();

        NexusDataGen.create(MOD.id())
            .recipe("ruby_block", """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": ["RRR", "RRR", "RRR"],
                  "key": {
                    "R": { "item": "examplemod:ruby" }
                  },
                  "result": { "item": "examplemod:ruby_block" }
                }
                """)
            .lootTable("blocks/ruby_block", """
                {
                  "type": "minecraft:block",
                  "pools": [{
                    "rolls": 1,
                    "entries": [{ "type": "minecraft:item", "name": "examplemod:ruby_block" }]
                  }]
                }
                """)
            .tag("items", "rubies", """
                {
                  "replace": false,
                  "values": ["examplemod:ruby"]
                }
                """)
            .blockstate("ruby_block", """
                {
                  "variants": {
                    "": { "model": "examplemod:block/ruby_block" }
                  }
                }
                """)
            .model("block/ruby_block", """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": { "all": "examplemod:block/ruby_block" }
                }
                """)
            .model("item/ruby", """
                {
                  "parent": "minecraft:item/generated",
                  "textures": { "layer0": "examplemod:item/ruby" }
                }
                """)
            .model("item/ruby_block", """
                {
                  "parent": "examplemod:block/ruby_block"
                }
                """)
            .lang("en_us", """
                {
                  "item.examplemod.ruby": "Ruby",
                  "block.examplemod.ruby_block": "Ruby Block",
                  "itemGroup.examplemod.main": "Example Mod"
                }
                """)
            .build()
            .run();
    }

    private static synchronized void runCommonAssertions() {
        if (commonAssertionsRan) {
            return;
        }
        commonAssertionsRan = true;
        require(ruby != null && "examplemod:ruby".equals(ruby.id()), "ruby item was not registered");
        require(rubyBlock != null && "examplemod:ruby_block".equals(rubyBlock.id()), "ruby block was not registered");
        require(NexusServices.get().registries().find(NexusRegistries.ITEMS, "examplemod:ruby").isPresent(), "ruby item registry entry missing");
        require(NexusServices.get().registries().find(NexusRegistries.BLOCKS, "examplemod:ruby_block").isPresent(), "ruby block registry entry missing");
        require(NexusServices.get().registries().find(NexusRegistries.BLOCK_ITEMS, "examplemod:ruby_block").isPresent(), "ruby block item registry entry missing");
        require(NexusServices.get().registries().find(NexusRegistries.CREATIVE_TABS, "examplemod:main").isPresent(), "creative tab registry entry missing");
        require(NexusServices.get().commands().find("example").isPresent(), "example command missing");
        require(CONFIG.getBoolean("enableRuby"), "config boolean mismatch");
        require(CONFIG.getInt("rubySpawnRate") == 8, "config int mismatch");

        NetworkPacketDefinition<?> definition = NexusServices.get().networking()
            .find("examplemod:main/ping")
            .orElseThrow(() -> new IllegalStateException("example ping packet missing"));
        NexusPacketBuffer buffer = new NexusPacketBuffer();
        encode(definition, new PingPacket("runtime"), buffer);
        Object decoded = definition.decoder().decode(buffer);
        require(decoded instanceof PingPacket ping && "runtime".equals(ping.message()), "ping packet round-trip failed");
        if (NexusServices.get().networking() instanceof InMemoryNetworkBridge bridge) {
            bridge.handle(definition.fullId(), decoded, null, com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide.SERVER);
        }
        NetworkPacketDefinition<?> clientbound = NexusServices.get().networking()
            .find("examplemod:main/pong")
            .orElseThrow(() -> new IllegalStateException("example pong packet missing"));
        NexusPacketBuffer clientboundBuffer = new NexusPacketBuffer();
        encode(clientbound, new PongPacket("runtime"), clientboundBuffer);
        Object clientboundDecoded = clientbound.decoder().decode(clientboundBuffer);
        require(clientboundDecoded instanceof PongPacket pong && "runtime".equals(pong.message()), "pong packet round-trip failed");

        Path lang = NexusServices.get().paths().generatedResourcesDirectory().resolve("assets/examplemod/lang/en_us.json");
        require(Files.isRegularFile(lang), "generated lang file missing");
        Path generated = NexusServices.get().paths().generatedResourcesDirectory();
        require(Files.isRegularFile(generated.resolve("data/examplemod/recipes/ruby_block.json")), "generated recipe missing");
        require(Files.isRegularFile(generated.resolve("data/examplemod/loot_tables/blocks/ruby_block.json")), "generated loot table missing");
        require(Files.isRegularFile(generated.resolve("data/examplemod/tags/items/rubies.json")), "generated item tag missing");
        require(Files.isRegularFile(generated.resolve("assets/examplemod/blockstates/ruby_block.json")), "generated blockstate missing");
        require(Files.isRegularFile(generated.resolve("assets/examplemod/models/block/ruby_block.json")), "generated block model missing");
        require(Files.isRegularFile(generated.resolve("assets/examplemod/models/item/ruby.json")), "generated item model missing");
        require(Files.isRegularFile(generated.resolve("assets/examplemod/models/item/ruby_block.json")), "generated block item model missing");
        MOD.logger().info(COMMON_MARKER);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void encode(NetworkPacketDefinition definition, Object packet, NexusPacketBuffer buffer) {
        definition.encoder().encode(packet, buffer);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Example Mod runtime assertion failed: " + message);
        }
    }

    private static void assertServerWorld(Object server) {
        require(server != null, "server startup did not provide a native Minecraft server");
        Object world = serverWorld(server);
        require(world != null, "native Minecraft server did not expose a loaded world/level");
        assertNativeHandles();
        assertNativeCommand(server, "example");
        assertWorldBlockRoundTrip(world, rubyBlock.get().nativeBlock(), "examplemod:ruby_block");
    }

    private static Object serverWorld(Object server) {
        Object world = invokeNoArg(server, "overworld", "getOverworld", "method_3847");
        if (world == null) {
            world = firstWorld(invokeNoArg(server, "getAllLevels", "getWorlds"));
        }
        return world;
    }

    private static void assertNativeHandles() {
        require(ruby != null && ruby.get() != null && isNativeObject(ruby.get().nativeItem()), "ruby item did not create a native Minecraft item");
        require(rubyBlock != null && rubyBlock.get() != null && isNativeObject(rubyBlock.get().nativeBlock()), "ruby block did not create a native Minecraft block");
        require(isNativeObject(rubyBlock.get().nativeBlockItem()), "ruby block item did not create a native Minecraft item");
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

    private record InvocationResult(boolean invoked, Object value) {
    }

    public record PingPacket(String message) implements NexusPacket {
        public static PingPacket decode(NexusPacketBuffer buffer) {
            return new PingPacket(buffer.readString());
        }

        @Override
        public void encode(NexusPacketBuffer buffer) {
            buffer.writeString(message);
        }
    }

    public record PongPacket(String message) implements NexusPacket {
        public static PongPacket decode(NexusPacketBuffer buffer) {
            return new PongPacket(buffer.readString());
        }

        @Override
        public void encode(NexusPacketBuffer buffer) {
            buffer.writeString(message);
        }
    }
}
