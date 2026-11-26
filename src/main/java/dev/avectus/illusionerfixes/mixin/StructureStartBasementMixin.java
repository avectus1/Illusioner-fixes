package dev.avectus.illusionerfixes.mixin;

import dev.avectus.illusionerfixes.compat.LukisMansions;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Jigsaw mansions (Luki's Woodland Mansions) never reach the vanilla mansion's afterPlace. */
@Mixin(StructureStart.class)
abstract class StructureStartBasementMixin {
   @Inject(method = "placeInChunk", at = @At("TAIL"))
   private void illusionerfixes$jigsawMansionBasement(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
      RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
      LukisMansions.placeBasement(level, (StructureStart)(Object)this, chunkBox, random);
   }
}
