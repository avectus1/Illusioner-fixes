package dev.avectus.illusionerfixes.compat.embeddium;

import com.fqkeh.illusioner.client.encounter.ClientEncounterController;
import com.fqkeh.illusioner.client.platform.ClientPlatforms;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The Illusioner's arena platforms are obsidian blocks that it hides while building chunk meshes
 * and draws itself. It hides them in vanilla's chunk builder, which Embeddium (and so Oculus)
 * replaces, so with Embeddium the raw obsidian showed. Same masking, in the block view Embeddium
 * builds chunk meshes from (only used for meshing, so the real world is untouched).
 */
@Mixin(value = WorldSlice.class, remap = false)
abstract class WorldSliceMixin {
   @Unique
   private static final ThreadLocal<BlockPos.MutableBlockPos> illusionerfixes$pos = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

   @ModifyReturnValue(method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"))
   private BlockState illusionerfixes$maskArena(BlockState state, @Local(ordinal = 0, argsOnly = true) int x,
      @Local(ordinal = 1, argsOnly = true) int y, @Local(ordinal = 2, argsOnly = true) int z) {
      if (state.isAir()) {
         return state;
      }
      ClientEncounterController encounter = ClientEncounterController.get();
      if (encounter.hidesArena() && state.is(Blocks.OBSIDIAN)) {
         return Blocks.AIR.defaultBlockState();
      }
      BlockPos pos = illusionerfixes$pos.get().set(x, y, z);
      if (ClientPlatforms.hidden(pos) || encounter.hidesSupport(pos)) {
         return Blocks.AIR.defaultBlockState();
      }
      return state;
   }
}
