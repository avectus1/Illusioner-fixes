package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.boss.BossBrain;
import com.fqkeh.illusioner.boss.illusioner.IllusionerAi;
import com.fqkeh.illusioner.boss.illusioner.IllusionerEntity;
import com.fqkeh.illusioner.world.ArenaAllocator;
import dev.avectus.illusionerfixes.IllusionerFixes;
import dev.avectus.illusionerfixes.hooks.LifecycleHooks;
import dev.avectus.illusionerfixes.hooks.PlatformRunHooks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Spawns the boss only once its chunk can hold it, and finds the ground it stands on (for Corpse). */
@Mixin(targets = "com.fqkeh.illusioner.encounter.BossLifecycle", remap = false)
abstract class BossLifecycleMixin implements LifecycleHooks {
   @Shadow
   @Final
   private ArenaAllocator.ArenaSlot arena;
   @Shadow
   private UUID bossId;
   @Shadow
   private IllusionerEntity spawned;

   @Shadow
   abstract boolean hasBoss();

   @Shadow
   abstract boolean canSpawn(ServerLevel level);

   @Shadow
   abstract void spawnBoss(ServerLevel level);

   @Shadow
   abstract IllusionerEntity find(ServerLevel level);

   @Override
   public void illusionerfixes$spawnWhenReady(ServerLevel level) {
      if (!this.hasBoss() && this.canSpawn(level)) {
         this.spawnBoss(level);
      }
   }

   @Override
   public Vec3 illusionerfixes$groundCentre(ServerLevel level) {
      BlockPos ground = null;
      IllusionerEntity boss = this.find(level);
      if (boss != null) {
         BossBrain<?> brain = ((BossBrainSlotAccessor)(Object)((IllusionerEntityAccessor)boss).illusionerfixes$ai()).illusionerfixes$brain();
         if (brain instanceof IllusionerAi ai) {
            IllusionerAiAccessor access = (IllusionerAiAccessor)(Object)ai;
            BlockPos standing = ((PlatformRunHooks)(Object)access.illusionerfixes$platformRun()).illusionerfixes$standing();
            ground = standing != null ? standing : access.illusionerfixes$arenaOrigin();
         }
      }

      return Vec3.atBottomCenterOf(ground != null ? ground : this.arena.origin());
   }

}
