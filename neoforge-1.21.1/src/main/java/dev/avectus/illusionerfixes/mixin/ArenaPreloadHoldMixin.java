package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.world.ArenaPreload;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * keepActive runs every encounter tick; it now also puts the arena's chunk ticket back, in case a
 * chunk-management mod dropped it (adding a ticket that is already held does nothing).
 */
@Mixin(ArenaPreload.Hold.class)
abstract class ArenaPreloadHoldMixin {
   @Inject(method = "keepActive", at = @At("TAIL"))
   private void illusionerfixes$reassertTicket(ServerLevel level, CallbackInfo ci) {
      ArenaPreload.Hold self = (ArenaPreload.Hold)(Object)this;
      level.getChunkSource().addRegionTicket(self.type(), self.center(), self.radius(), self.center());
   }
}
