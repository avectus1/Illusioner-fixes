package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.progression.MansionBasement;
import dev.avectus.illusionerfixes.MansionPalette;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MansionBasement.class, remap = false)
abstract class MansionBasementMixin {
   @Unique
   private static final ThreadLocal<Object[]> illusionerfixes$placing = new ThreadLocal<>();

   @Inject(method = "place", at = @At("HEAD"))
   private static void illusionerfixes$remember(WorldGenLevel level, TemplateStructurePiece entrance, BoundingBox chunkBox, RandomSource random, CallbackInfo ci) {
      illusionerfixes$placing.set(new Object[]{level, entrance});
   }

   @Inject(method = "place", at = @At("RETURN"))
   private static void illusionerfixes$forget(WorldGenLevel level, TemplateStructurePiece entrance, BoundingBox chunkBox, RandomSource random, CallbackInfo ci) {
      illusionerfixes$placing.remove();
   }

   /**
    * The basement takes the palette the mansion above it was given (Regions Unexplored redwood, blackwood, ...).
    */
   @ModifyArg(
      method = "place",
      at = @At(
         value = "INVOKE",
         remap = true,
         target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z"
      ),
      index = 3
   )
   private static StructurePlaceSettings illusionerfixes$settings(StructurePlaceSettings settings) {
      Object[] placing = illusionerfixes$placing.get();
      if (placing != null) {
         WorldGenLevel level = (WorldGenLevel)placing[0];
         TemplateStructurePiece entrance = (TemplateStructurePiece)placing[1];
         MansionPalette.forMansion(level, entrance.templatePosition(), entrance.templatePosition(), entrance.placeSettings()).ifPresent(settings::addProcessor);
      }

      return settings;
   }
}
