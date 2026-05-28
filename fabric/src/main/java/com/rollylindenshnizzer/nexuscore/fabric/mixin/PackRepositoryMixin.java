package com.rollylindenshnizzer.nexuscore.fabric.mixin;

import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nexuscore$addGeneratedRuntimePack(RepositorySource[] sources, CallbackInfo callbackInfo) {
        this.sources = new LinkedHashSet<>(this.sources);
        this.sources.add(NexusRuntimeRepositorySource.INSTANCE);
    }
}
