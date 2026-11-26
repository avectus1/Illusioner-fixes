package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.encounter.BossEncounter;
import com.fqkeh.illusioner.encounter.EncounterPhase;
import com.fqkeh.illusioner.player.PlayerReturnState;
import com.fqkeh.illusioner.world.ArenaAllocator;
import dev.avectus.illusionerfixes.CorpseKeeper;
import dev.avectus.illusionerfixes.hooks.LifecycleHooks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossEncounter.class)
abstract class BossEncounterMixin {
   @Shadow
   @Final
   private ArenaAllocator.ArenaSlot arena;
   @Shadow
   @Final
   private Map<UUID, PlayerReturnState> returnStates;
   @Shadow
   @Final
   private com.fqkeh.illusioner.encounter.BossLifecycle boss;
   @Shadow
   private int transferredAtTick;
   @Shadow
   private boolean closed;
   @Unique
   private final Map<UUID, PlayerReturnState> illusionerfixes$corpseHomes = new LinkedHashMap<>();

   @Shadow
   private ServerLevel requireVoid(MinecraftServer server) {
      throw new AssertionError();
   }

   @Unique
   private LifecycleHooks illusionerfixes$boss() {
      return (LifecycleHooks)(Object)this.boss;
   }

   /** The transfer no longer spawns the boss into a chunk whose entities have not loaded yet. */
   @Redirect(
      method = "transferParticipants",
      at = @At(value = "INVOKE", target = "Lcom/fqkeh/illusioner/encounter/BossLifecycle;spawnBoss(Lnet/minecraft/server/level/ServerLevel;)V")
   )
   private void illusionerfixes$spawnWhenReady(com.fqkeh.illusioner.encounter.BossLifecycle lifecycle, ServerLevel level) {
      ((LifecycleHooks)(Object)lifecycle).illusionerfixes$spawnWhenReady(level);
   }

   @Inject(method = "transferParticipants", at = @At("TAIL"))
   private void illusionerfixes$rememberHomes(MinecraftServer server, CallbackInfo ci) {
      this.illusionerfixes$corpseHomes.putAll(this.returnStates);
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void illusionerfixes$keepCorpsesOnCircle(MinecraftServer server, CallbackInfo ci) {
      if (!this.closed && this.transferredAtTick >= 0 && (server.getTickCount() - this.transferredAtTick) % 10 == 0) {
         ServerLevel level = this.requireVoid(server);
         CorpseKeeper.keepOnCircle(level, this.arena.origin(), this.illusionerfixes$boss().illusionerfixes$groundCentre(level));
      }
   }

   @Inject(method = "tearDownArena", at = @At("HEAD"))
   private void illusionerfixes$sendCorpsesHome(MinecraftServer server, CallbackInfo ci) {
      CorpseKeeper.sendHome(this.requireVoid(server), this.arena.origin(), this.illusionerfixes$corpseHomes);
   }

}
