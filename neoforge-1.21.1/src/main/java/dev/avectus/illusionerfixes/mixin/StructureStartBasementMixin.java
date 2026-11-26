package dev.avectus.illusionerfixes.mixin;

import dev.avectus.illusionerfixes.MansionPalette;
import dev.avectus.illusionerfixes.compat.LukisMansions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
abstract class StructureStartBasementMixin {
   /** Jigsaw mansions (Luki's Woodland Mansions) never reach the vanilla mansion's afterPlace. */
   @Inject(method = "placeInChunk", at = @At("TAIL"))
   private void illusionerfixes$jigsawMansionBasement(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
      RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
      LukisMansions.placeBasement(level, (StructureStart)(Object)this, chunkBox, random);
   }

   /**
    * The position vanilla hands each piece's processors as their pivot is the centre of the whole
    * structure's box. For a woodland mansion that is ~40 blocks from where it spawned, so biome
    * re-palettes judged there (Regions Unexplored's blackwood taiga) mostly missed. Mansions use
    * their entrance instead, which is also what the basement under it is judged by.
    */
   @ModifyArg(method = "placeInChunk", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/level/levelgen/structure/StructurePiece;postProcess(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V"),
      index = 6)
   private BlockPos illusionerfixes$mansionPivot(BlockPos pivot) {
      return MansionPalette.mansionPivot((StructureStart)(Object)this, pivot);
   }
}
