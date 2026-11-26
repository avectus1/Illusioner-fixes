package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.boss.AttackContext;
import com.fqkeh.illusioner.boss.illusioner.illusion.platform.PlatformDisk;
import com.fqkeh.illusioner.boss.illusioner.illusion.platform.PlatformRunAttack;
import dev.avectus.illusionerfixes.hooks.PlatformRunHooks;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tracks the disk the players stand on during the shattered path ("come to me"). */
@Mixin(PlatformRunAttack.class)
abstract class PlatformRunAttackMixin implements PlatformRunHooks {
   @Shadow
   private PlatformDisk current;
   @Unique
   private boolean illusionerfixes$running;

   @Inject(method = "start", at = @At("HEAD"))
   private void illusionerfixes$started(AttackContext ctx, CallbackInfo ci) {
      this.illusionerfixes$running = true;
   }

   @Inject(method = "tick", at = @At("RETURN"))
   private void illusionerfixes$ticked(AttackContext ctx, int age, CallbackInfoReturnable<?> cir) {
      if (cir.getReturnValue() != null && "FINISHED".equals(cir.getReturnValue().toString())) {
         this.illusionerfixes$running = false;
      }
   }

   @Inject(method = "cleanup", at = @At("HEAD"))
   private void illusionerfixes$cleanedUp(AttackContext ctx, CallbackInfo ci) {
      this.illusionerfixes$running = false;
   }

   @Override
   public BlockPos illusionerfixes$standing() {
      return this.illusionerfixes$running && this.current != null ? this.current.origin() : null;
   }
}
