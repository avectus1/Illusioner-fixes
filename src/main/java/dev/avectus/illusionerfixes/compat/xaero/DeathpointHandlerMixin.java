package dev.avectus.illusionerfixes.compat.xaero;

import com.fqkeh.illusioner.world.ModDimensions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.minimap.waypoint.DeathpointHandler;

/**
 * No Xaero's Minimap death waypoint for a death in the corrupted void. The Illusioner respawns the
 * player where they entered the arena and sends their drops there too (this addon sends a Corpse
 * there as well), and the arena itself is gone after the fight: a waypoint in the void only clutters
 * the void's waypoint list, and making it would also turn the player's real last deathpoint into an
 * "old" one (or delete it, depending on Xaero's settings).
 */
@Mixin(value = DeathpointHandler.class, remap = false)
abstract class DeathpointHandlerMixin {
   @Inject(method = "createDeathpoint(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
   private void illusionerfixes$noDeathpointInTheVoid(Player player, CallbackInfo ci) {
      if (player != null && ModDimensions.isArena(player.level().dimension())) {
         ci.cancel();
      }
   }
}
