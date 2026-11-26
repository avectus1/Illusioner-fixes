package dev.avectus.illusionerfixes.compat.sodium;

import com.fqkeh.illusioner.client.encounter.ClientEncounterController;
import com.fqkeh.illusioner.client.platform.ClientPlatforms;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The Illusioner's arena platforms are obsidian blocks that it hides while building chunk meshes
 * and draws itself. It hides them in vanilla's chunk builder, which Sodium (and so Iris) replaces,
 * so with Sodium the raw obsidian showed. Same masking, in Sodium's chunk builder.
 */
@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
abstract class MeshingTaskMixin {
   @Unique
   private static final ThreadLocal<BlockPos.MutableBlockPos> illusionerfixes$pos = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

   @Redirect(method = "execute", at = @At(value = "INVOKE",
      target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"))
   private BlockState illusionerfixes$maskArena(LevelSlice slice, int x, int y, int z) {
      BlockState state = slice.getBlockState(x, y, z);
      if (state.isAir()) {
         return state;
      }
      BlockPos pos = illusionerfixes$pos.get().set(x, y, z);
      ClientEncounterController encounter = ClientEncounterController.get();
      if (ClientPlatforms.hidden(pos) || encounter.hidesSupport(pos) || (encounter.hidesArena() && state.is(Blocks.OBSIDIAN))) {
         return Blocks.AIR.defaultBlockState();
      }
      return state;
   }
}
