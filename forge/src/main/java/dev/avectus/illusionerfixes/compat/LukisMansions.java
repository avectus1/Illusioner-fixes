package dev.avectus.illusionerfixes.compat;

import com.fqkeh.illusioner.McCompat;
import com.fqkeh.illusioner.progression.MansionBasement;
import com.fqkeh.illusioner.progression.MansionId;
import com.mojang.serialization.Codec;
import dev.avectus.illusionerfixes.IllusionerFixes;
import dev.avectus.illusionerfixes.mixin.SinglePoolElementAccessor;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Luki's Woodland Mansions replaces the mansion with a jigsaw structure, so The Illusioner's
 * basement (hooked to the vanilla mansion's "entrance" piece) was never built and the sculk note
 * block could not find it. Here the basement goes under the stone maze level: from the front door,
 * through the passage under the entrance terrace and the corridor straight ahead, the stairs go down
 * in the east side of the central hall (by its trial spawners). The whole basement stays under the
 * mansion's footprint, so every chunk of it is generated with the mansion.
 */
public final class LukisMansions {
   /** The jigsaw start piece; the layout below is in its template's coordinates. */
   private static final ResourceLocation ANCHOR = new ResourceLocation("mansions", "mansion/bottom_front_left");
   /** Centre of the stairwell, on the floor layer of the maze level. */
   private static final BlockPos STAIRWELL = new BlockPos(44, 0, 37);
   /** The basement turned so its stairs run front to back and its rooms lie under the mansion. */
   private static final Rotation TURN = Rotation.COUNTERCLOCKWISE_90;
   /** Centre of the stairwell opening in the basement template, on its top layer. */
   private static final BlockPos TEMPLATE_STAIRWELL = new BlockPos(41, 20, 20);
   private static final Vec3 PORTAL_CENTRE = new Vec3(6.5, 12.0, 20.5);
   private static final BlockPos PORTAL_FACING = new BlockPos(1, 0, 0);

   private static final DeferredRegister<StructureProcessorType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, IllusionerFixes.MOD_ID);
   private static final RegistryObject<StructureProcessorType<KeepFloor>> KEEP_FLOOR =
      TYPES.register("keep_mansion_floor", () -> () -> KeepFloor.CODEC);

   private LukisMansions() {
   }

   public static void register(IEventBus modBus) {
      TYPES.register(modBus);
   }

   private record Layout(BlockPos origin, Rotation rotation, int floorY) {
      StructurePlaceSettings settings() {
         return new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE);
      }
   }

   private static Optional<Layout> layout(StructureStart start) {
      for (StructurePiece piece : start.getPieces()) {
         if (piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single
            && ((SinglePoolElementAccessor)single).illusionerfixes$template().left().filter(ANCHOR::equals).isPresent()) {
            Rotation rotation = pool.getRotation().getRotated(TURN);
            BlockPos stairwell = pool.getPosition().offset(StructureTemplate.transform(STAIRWELL, Mirror.NONE, pool.getRotation(), BlockPos.ZERO));
            BlockPos origin = stairwell.subtract(StructureTemplate.transform(TEMPLATE_STAIRWELL, Mirror.NONE, rotation, BlockPos.ZERO));
            return Optional.of(new Layout(origin, rotation, stairwell.getY()));
         }
      }
      return Optional.empty();
   }

   private static Optional<StructureTemplate> template(ServerLevel level) {
      return McCompat.structureTemplates(level).get(MansionBasement.TEMPLATE);
   }

   public static void placeBasement(WorldGenLevel level, StructureStart start, BoundingBox chunkBox, RandomSource random) {
      if (!start.isValid()) {
         return;
      }
      Optional<Layout> layout = layout(start);
      if (layout.isEmpty()) {
         return;
      }
      Optional<StructureTemplate> template = template(level.getLevel());
      if (template.isEmpty()) {
         return;
      }
      Layout at = layout.get();
      StructurePlaceSettings settings = at.settings()
         .setBoundingBox(chunkBox)
         .setIgnoreEntities(false)
         .setKeepLiquids(false)
         .addProcessor(new KeepFloor(at.floorY()))
         .addProcessor(new MansionBasement.ShellProcessor());
      template.get().placeInWorld(level, at.origin(), at.origin(), settings, random, 2);
   }

   public static Optional<MansionBasement.Site> site(ServerLevel level, BlockPos pos) {
      Structure mansion = McCompat.registryValue(level.registryAccess(), BuiltinStructures.WOODLAND_MANSION);
      Optional<StructureTemplate> template = template(level);
      if (mansion == null || template.isEmpty()) {
         return Optional.empty();
      }

      Set<StructureStart> starts = new LinkedHashSet<>();
      ChunkPos centre = new ChunkPos(pos);
      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            if (level.hasChunk(centre.x + dx, centre.z + dz)) {
               starts.addAll(McCompat.startsForStructure(level.structureManager(), centre.x + dx, centre.z + dz, mansion));
            }
         }
      }

      for (StructureStart start : starts) {
         Optional<MansionBasement.Site> site = siteOf(level, start);
         if (site.isPresent() && site.get().contains(pos)) {
            return site;
         }
      }
      return Optional.empty();
   }

   /** The basement of one Luki's mansion, or empty if the start is not one. */
   public static Optional<MansionBasement.Site> siteOf(ServerLevel level, StructureStart start) {
      Optional<StructureTemplate> template = template(level);
      if (!start.isValid() || template.isEmpty()) {
         return Optional.empty();
      }
      return layout(start).map(at -> {
         BoundingBox box = template.get().getBoundingBox(at.settings(), at.origin());
         BlockPos facing = StructureTemplate.transform(PORTAL_FACING, Mirror.NONE, at.rotation(), BlockPos.ZERO);
         return new MansionBasement.Site(
            new MansionId(level.dimension(), start.getChunkPos().toLong()),
            box,
            at.floorY(),
            toWorld(at.origin(), at.rotation(), PORTAL_CENTRE),
            new Vec3(facing.getX(), facing.getY(), facing.getZ()));
      });
   }

   /** Where the stairwell comes up, for a Luki's mansion start. */
   public static Optional<BlockPos> stairwell(StructureStart start) {
      return layout(start).map(at -> at.origin().offset(StructureTemplate.transform(TEMPLATE_STAIRWELL, Mirror.NONE, at.rotation(), BlockPos.ZERO)));
   }

   /** Same as The Illusioner's own placement of a point inside the rotated template. */
   private static Vec3 toWorld(BlockPos origin, Rotation rotation, Vec3 local) {
      BlockPos block = BlockPos.containing(local);
      BlockPos placed = origin.offset(StructureTemplate.transform(block, Mirror.NONE, rotation, BlockPos.ZERO));
      Vec3 within = local.subtract(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
      BlockPos x = StructureTemplate.transform(new BlockPos(1, 0, 0), Mirror.NONE, rotation, BlockPos.ZERO);
      BlockPos y = StructureTemplate.transform(new BlockPos(0, 1, 0), Mirror.NONE, rotation, BlockPos.ZERO);
      BlockPos z = StructureTemplate.transform(new BlockPos(0, 0, 1), Mirror.NONE, rotation, BlockPos.ZERO);
      return Vec3.atCenterOf(placed)
         .add(x.getX() * within.x(), x.getY() * within.x(), x.getZ() * within.x())
         .add(y.getX() * within.y(), y.getY() * within.y(), y.getZ() * within.y())
         .add(z.getX() * within.z(), z.getY() * within.z(), z.getZ() * within.z());
   }

   /**
    * On the mansion's floor layer, the basement template only frames its stairwell with birch planks
    * (made for the vanilla entrance hall): keep Luki's floor there and only cut the stairwell.
    */
   public static final class KeepFloor extends StructureProcessor {
      static final Codec<KeepFloor> CODEC = Codec.INT.fieldOf("floor_y").xmap(KeepFloor::new, p -> p.floorY).codec();
      private final int floorY;

      KeepFloor(int floorY) {
         this.floorY = floorY;
      }

      @Override
      public StructureBlockInfo processBlock(LevelReader level, BlockPos origin, BlockPos pivot, StructureBlockInfo original,
         StructureBlockInfo current, StructurePlaceSettings settings) {
         if (current.pos().getY() == this.floorY && current.state().is(Blocks.BIRCH_PLANKS)) {
            return level.getBlockState(current.pos()).isAir() ? current : null;
         }
         return current;
      }

      @Override
      protected StructureProcessorType<?> getType() {
         return KEEP_FLOOR.get();
      }
   }
}
