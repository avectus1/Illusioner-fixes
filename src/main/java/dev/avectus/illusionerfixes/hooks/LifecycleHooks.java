package dev.avectus.illusionerfixes.hooks;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Implemented on The Illusioner's package-private BossLifecycle by BossLifecycleMixin. */
public interface LifecycleHooks {

   void illusionerfixes$spawnWhenReady(ServerLevel level);

   Vec3 illusionerfixes$groundCentre(ServerLevel level);
}
