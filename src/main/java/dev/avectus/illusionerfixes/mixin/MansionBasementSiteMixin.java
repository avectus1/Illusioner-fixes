package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.progression.MansionBasement;
import dev.avectus.illusionerfixes.compat.LukisMansions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the sculk note block find a basement under a jigsaw mansion too. */
@Mixin(value = MansionBasement.class, remap = false)
abstract class MansionBasementSiteMixin {
   @Inject(method = "at", at = @At("RETURN"), cancellable = true)
   private static void illusionerfixes$jigsawMansionSite(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Optional<MansionBasement.Site>> cir) {
      if (cir.getReturnValue().isEmpty()) {
         LukisMansions.site(level, pos).ifPresent(site -> cir.setReturnValue(Optional.of(site)));
      }
   }
}
