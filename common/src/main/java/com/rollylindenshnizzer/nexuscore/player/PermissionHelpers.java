package com.rollylindenshnizzer.nexuscore.player;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public final class PermissionHelpers {
    public static Predicate<CommandSourceStack> level(int level) {
        return source -> source.hasPermission(level);
    }

    public static boolean friendlyFire(Entity attacker, Entity target) {
        return attacker.getTeam() == null || target.getTeam() == null || attacker.getTeam().isAllowFriendlyFire() || !attacker.isAlliedTo(target);
    }

    private PermissionHelpers() {
    }
}
