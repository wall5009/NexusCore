package com.rollylindenshnizzer.nexuscore.api.network;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NexusPacketBuffer {
    private final List<Object> values = new ArrayList<>();
    private final Object nativeBuffer;
    private int readIndex;

    public NexusPacketBuffer() {
        this(null);
    }

    private NexusPacketBuffer(Object nativeBuffer) {
        this.nativeBuffer = nativeBuffer;
    }

    public static NexusPacketBuffer wrap(Object nativeBuffer) {
        return new NexusPacketBuffer(nativeBuffer);
    }

    public Optional<Object> nativeBuffer() {
        return Optional.ofNullable(nativeBuffer);
    }

    public void writeString(String value) {
        if (invokeNativeVoid("writeUtf", new Class<?>[]{String.class}, value)
            || invokeNativeVoid("writeString", new Class<?>[]{String.class}, value)
            || invokeNativeVoid("writeString", new Class<?>[]{String.class, int.class}, value, 32767)
            || invokeNativeVoid("method_10814", new Class<?>[]{String.class}, value)
            || invokeNativeVoid("method_10788", new Class<?>[]{String.class, int.class}, value, 32767)
            || invokeNativeVoid("writeCharSequence", new Class<?>[]{CharSequence.class, java.nio.charset.Charset.class}, value, StandardCharsets.UTF_8)) {
            return;
        }
        values.add(value);
    }

    public String readString() {
        Object nativeValue = invokeNative("readUtf");
        if (nativeValue == null) {
            nativeValue = invokeNative("readString");
        }
        if (nativeValue == null) {
            nativeValue = invokeNative("method_19772");
        }
        if (nativeValue == null) {
            nativeValue = invokeNative("readUtf", new Class<?>[]{int.class}, 32767);
        }
        if (nativeValue == null) {
            nativeValue = invokeNative("method_10800", new Class<?>[]{int.class}, 32767);
        }
        if (nativeValue != null) {
            return nativeValue.toString();
        }
        return (String) values.get(readIndex++);
    }

    public void writeInt(int value) {
        if (invokeNativeVoid("writeVarInt", new Class<?>[]{int.class}, value)
            || invokeNativeVoid("writeInt", new Class<?>[]{int.class}, value)
            || invokeNativeVoid("method_10804", new Class<?>[]{int.class}, value)) {
            return;
        }
        values.add(value);
    }

    public int readInt() {
        Object nativeValue = invokeNative("readVarInt");
        if (nativeValue == null) {
            nativeValue = invokeNative("readInt");
        }
        if (nativeValue == null) {
            nativeValue = invokeNative("method_10816");
        }
        if (nativeValue instanceof Number number) {
            return number.intValue();
        }
        return (Integer) values.get(readIndex++);
    }

    public void writeBoolean(boolean value) {
        if (invokeNativeVoid("writeBoolean", new Class<?>[]{boolean.class}, value)) {
            return;
        }
        values.add(value);
    }

    public boolean readBoolean() {
        Object nativeValue = invokeNative("readBoolean");
        if (nativeValue instanceof Boolean bool) {
            return bool;
        }
        return (Boolean) values.get(readIndex++);
    }

    public List<Object> snapshot() {
        return new ArrayList<>(values);
    }

    private Object invokeNative(String methodName) {
        return invokeNative(methodName, new Class<?>[0]);
    }

    private Object invokeNative(String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (nativeBuffer == null) {
            return null;
        }
        try {
            Method method = nativeBuffer.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(nativeBuffer, arguments);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private boolean invokeNativeVoid(String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (nativeBuffer == null) {
            return false;
        }
        try {
            Method method = nativeBuffer.getClass().getMethod(methodName, parameterTypes);
            method.invoke(nativeBuffer, arguments);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
