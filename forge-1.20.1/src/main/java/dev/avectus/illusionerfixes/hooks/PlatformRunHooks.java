package dev.avectus.illusionerfixes.hooks;

import net.minecraft.core.BlockPos;

/** Implemented on PlatformRunAttack (the shattered path) by PlatformRunAttackMixin. */
public interface PlatformRunHooks {
   /** The disk the players stand on while the path is running, else null. */
   BlockPos illusionerfixes$standing();
}
