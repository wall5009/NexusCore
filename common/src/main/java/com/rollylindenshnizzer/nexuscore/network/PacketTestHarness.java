package com.rollylindenshnizzer.nexuscore.network;

import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class PacketTestHarness {
    public static <T> T roundTrip(T packet, BiConsumer<FriendlyByteBuf, T> encoder,
                                  Function<FriendlyByteBuf, T> decoder) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(buffer, packet);
        return decoder.apply(buffer);
    }

    public static <T> void assertRoundTrip(T packet, BiConsumer<FriendlyByteBuf, T> encoder,
                                           Function<FriendlyByteBuf, T> decoder) {
        T decoded = roundTrip(packet, encoder, decoder);
        if (!packet.equals(decoded)) {
            throw new AssertionError("Packet round-trip mismatch: " + packet + " != " + decoded);
        }
    }

    public static <T> void validate(T packet, java.util.function.Predicate<T> predicate, String message) {
        if (!predicate.test(packet)) {
            throw new AssertionError(message);
        }
    }

    private PacketTestHarness() {
    }
}
