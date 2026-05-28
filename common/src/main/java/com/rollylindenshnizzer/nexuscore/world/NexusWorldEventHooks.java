package com.rollylindenshnizzer.nexuscore.world;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.event.EventDiagnosticRegistry;
import com.rollylindenshnizzer.nexuscore.event.EventTrace;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import dev.architectury.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NexusIncubating(since = "1.3")
public final class NexusWorldEventHooks {
    private static final List<HookStatus> HOOKS = new ArrayList<>();
    private static boolean installed;

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;

        hook("dev.architectury.event.events.common.BlockEvent", "PLACE", NexusWorldEvent.Kind.BLOCK_PLACE);
        hook("dev.architectury.event.events.common.BlockEvent", "BREAK", NexusWorldEvent.Kind.BLOCK_BREAK);
        hook("dev.architectury.event.events.common.BlockEvent", "FLUID_PLACE", NexusWorldEvent.Kind.BLOCK_PLACE);

        hook("dev.architectury.event.events.common.InteractionEvent", "RIGHT_CLICK_BLOCK", NexusWorldEvent.Kind.BLOCK_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "LEFT_CLICK_BLOCK", NexusWorldEvent.Kind.BLOCK_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "RIGHT_CLICK_ITEM", NexusWorldEvent.Kind.ITEM_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "RIGHT_CLICK_EMPTY", NexusWorldEvent.Kind.ITEM_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "INTERACT_ENTITY", NexusWorldEvent.Kind.ENTITY_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "INTERACT_ENTITY_AT", NexusWorldEvent.Kind.ENTITY_INTERACT);
        hook("dev.architectury.event.events.common.InteractionEvent", "ATTACK_ENTITY", NexusWorldEvent.Kind.ENTITY_INTERACT);

        hook("dev.architectury.event.events.common.TickEvent", "SERVER_PRE", NexusWorldEvent.Kind.SERVER_TICK_START);
        hook("dev.architectury.event.events.common.TickEvent", "SERVER_POST", NexusWorldEvent.Kind.SERVER_TICK_END);
        hook("dev.architectury.event.events.common.TickEvent", "SERVER_LEVEL_PRE", NexusWorldEvent.Kind.LEVEL_TICK_START);
        hook("dev.architectury.event.events.common.TickEvent", "SERVER_LEVEL_POST", NexusWorldEvent.Kind.LEVEL_TICK_END);
        hook("dev.architectury.event.events.common.TickEvent", "PLAYER_PRE", NexusWorldEvent.Kind.LEVEL_TICK_START);
        hook("dev.architectury.event.events.common.TickEvent", "PLAYER_POST", NexusWorldEvent.Kind.LEVEL_TICK_END);
        hook("dev.architectury.event.events.common.TickEvent", "LEVEL_PRE", NexusWorldEvent.Kind.LEVEL_TICK_START);
        hook("dev.architectury.event.events.common.TickEvent", "LEVEL_POST", NexusWorldEvent.Kind.LEVEL_TICK_END);
        hook("dev.architectury.event.events.common.TickEvent", "WORLD_PRE", NexusWorldEvent.Kind.LEVEL_TICK_START);
        hook("dev.architectury.event.events.common.TickEvent", "WORLD_POST", NexusWorldEvent.Kind.LEVEL_TICK_END);

        DebugRegistry.section("nexuscore.world_hooks", NexusWorldEventHooks::debugSummary);
    }

    public static List<HookStatus> hooks() {
        return List.copyOf(HOOKS);
    }

    public static String debugSummary() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (HookStatus hook : HOOKS) {
            counts.merge(hook.status(), 1L, Long::sum);
        }
        return "installed=" + installed + ", hooks=" + HOOKS.size() + ", " + counts;
    }

    private static void hook(String eventClassName, String fieldName, NexusWorldEvent.Kind kind) {
        String hookName = eventClassName.substring(eventClassName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) + "." + fieldName.toLowerCase(Locale.ROOT);
        try {
            Class<?> eventClass = Class.forName(eventClassName);
            Field field = eventClass.getField(fieldName);
            Object event = field.get(null);
            Class<?> listenerType = listenerType(field);
            if (listenerType == null) {
                HOOKS.add(HookStatus.failed(hookName, "listener type could not be resolved"));
                return;
            }
            Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, new Handler(hookName, kind));

            if (!(event instanceof Event<?> architecturyEvent)) {
                HOOKS.add(HookStatus.failed(hookName, "field is not an Architectury Event"));
                return;
            }
            registerArchitecturyEvent(architecturyEvent, listener);
            HOOKS.add(HookStatus.installed(hookName, listenerType.getName()));
            EventDiagnosticRegistry.mark("world_hook_registered:" + hookName);
        } catch (ClassNotFoundException | NoSuchFieldException missing) {
            HOOKS.add(HookStatus.skipped(hookName, missing.getClass().getSimpleName()));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            HOOKS.add(HookStatus.failed(hookName, exception.getClass().getSimpleName() + ": " + exception.getMessage()));
        }
    }

    private static Class<?> listenerType(Field field) throws ClassNotFoundException {
        Type type = field.getGenericType();
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        Type listener = parameterizedType.getActualTypeArguments()[0];
        if (listener instanceof Class<?> listenerClass) {
            return listenerClass;
        }
        if (listener instanceof ParameterizedType nested && nested.getRawType() instanceof Class<?> listenerClass) {
            return listenerClass;
        }
        return Class.forName(listener.getTypeName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerArchitecturyEvent(Event<?> event, Object listener) {
        ((Event) event).register(listener);
    }

    private static final class Handler implements InvocationHandler {
        private final String hookName;
        private final NexusWorldEvent.Kind kind;

        private Handler(String hookName, NexusWorldEvent.Kind kind) {
            this.hookName = hookName;
            this.kind = kind;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "NexusWorldEventHook[" + hookName + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null ? null : args[0]);
                    default -> null;
                };
            }

            NexusWorldEvent event = toWorldEvent(args == null ? new Object[0] : args);
            if (event.serverSide()) {
                EventDiagnosticRegistry.mark("world_hook_invoked:" + hookName);
                EventTrace.record("world." + kind.name().toLowerCase(Locale.ROOT), hookName + detail(event));
                NexusMultiblocks.assembly().handleWorldEvent(event);
                NexusRituals.runtime().handleWorldEvent(event);
            }
            return neutralReturn(method.getReturnType());
        }

        private NexusWorldEvent toWorldEvent(Object[] args) {
            Level level = first(Level.class, args);
            BlockPos pos = first(BlockPos.class, args);
            BlockState state = first(BlockState.class, args);
            Player player = first(Player.class, args);
            InteractionHand hand = first(InteractionHand.class, args);
            Direction direction = first(Direction.class, args);
            if (level == null && player != null) {
                level = player.level();
            }
            if (pos == null && player != null) {
                pos = player.blockPosition();
            }
            if (level == null) {
                MinecraftServer server = first(MinecraftServer.class, args);
                if (server != null) {
                    // Server-only tick events do not have one canonical world. The runtime still needs
                    // the tick pulse, so the world context intentionally stays null here.
                }
            }
            if (state == null && level != null && pos != null) {
                try {
                    state = level.getBlockState(pos);
                } catch (RuntimeException ignored) {
                    state = null;
                }
            }
            return new NexusWorldEvent(kind, hookName, level, pos, state, player, hand, direction, Instant.now());
        }

        private String detail(NexusWorldEvent event) {
            if (event.pos() == null) {
                return "";
            }
            return " " + event.dimension() + " " + event.pos().toShortString() + " " + event.blockId();
        }
    }

    private static <T> T first(Class<T> type, Object[] args) {
        for (Object arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        return null;
    }

    private static Object neutralReturn(Class<?> returnType) throws ReflectiveOperationException {
        if (returnType == Void.TYPE) {
            return null;
        }
        if (returnType == Boolean.TYPE) {
            return false;
        }
        if (returnType == Integer.TYPE || returnType == Short.TYPE || returnType == Byte.TYPE) {
            return 0;
        }
        if (returnType == Long.TYPE) {
            return 0L;
        }
        if (returnType == Float.TYPE) {
            return 0.0F;
        }
        if (returnType == Double.TYPE) {
            return 0.0D;
        }
        if (returnType.getName().equals("dev.architectury.event.EventResult")) {
            return Class.forName("dev.architectury.event.EventResult").getMethod("pass").invoke(null);
        }
        if (returnType.isEnum()) {
            Object[] constants = returnType.getEnumConstants();
            for (Object constant : constants) {
                if (((Enum<?>) constant).name().equals("PASS")) {
                    return constant;
                }
            }
            return constants.length == 0 ? null : constants[0];
        }
        return null;
    }

    public record HookStatus(String name, String status, String detail) {
        public static HookStatus installed(String name, String detail) {
            return new HookStatus(name, "installed", detail);
        }

        public static HookStatus skipped(String name, String detail) {
            return new HookStatus(name, "skipped", detail);
        }

        public static HookStatus failed(String name, String detail) {
            return new HookStatus(name, "failed", detail);
        }
    }

    private NexusWorldEventHooks() {
    }
}
