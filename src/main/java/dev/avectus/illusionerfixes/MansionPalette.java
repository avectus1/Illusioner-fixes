package dev.avectus.illusionerfixes;

import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Gives the Illusioner's basement the same palette as the mansion above it. Mods such as Regions
 * Unexplored re-palette mansions through Lithostitched's "lithostitched:woodland_mansion" processor
 * list (redwood in redwood forests, blackwood in blackwood taiga, ...). That list decides by the
 * biome at the piece's placement position; the basement sits deep underground, so its processors
 * are run with the mansion entrance's position instead, which is what the entrance itself used.
 * Dark oak blocks the list does not cover (trapdoors, slabs, wood) follow the wood it picked.
 */
public final class MansionPalette extends StructureProcessor {
   private static final ResourceLocation LIST = ResourceLocation.fromNamespaceAndPath("lithostitched", "woodland_mansion");
   private static final DeferredRegister<StructureProcessorType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, IllusionerFixes.MOD_ID);
   private static final MapCodec<MansionPalette> CODEC = MapCodec.unit(() -> new MansionPalette(List.of(), BlockPos.ZERO, Map.of()));
   private static final net.neoforged.neoforge.registries.DeferredHolder<StructureProcessorType<?>, StructureProcessorType<MansionPalette>> TYPE =
      TYPES.register("mansion_palette", () -> () -> CODEC);
   private static final String[] DARK_OAK_SHAPES = {"planks", "stairs", "slab", "fence", "fence_gate", "door", "trapdoor", "log", "wood", "button", "pressure_plate"};
   private final List<StructureProcessor> processors;
   private final BlockPos surface;
   private final Map<Block, Block> extras;

   private MansionPalette(List<StructureProcessor> processors, BlockPos surface, Map<Block, Block> extras) {
      this.processors = processors;
      this.surface = surface;
      this.extras = extras;
   }

   static void register(IEventBus modBus) {
      TYPES.register(modBus);
   }

   /**
    * The pivot a woodland mansion's pieces are processed with when a mod re-palettes mansions: its
    * entrance (where it spawned) rather than the centre of its box. Anything else keeps {@code pivot}.
    */
   public static BlockPos mansionPivot(net.minecraft.world.level.levelgen.structure.StructureStart start, BlockPos pivot) {
      if (!(start.getStructure() instanceof net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure)
         || !ModList.get().isLoaded("lithostitched") || !breaksSeedParity()) {
         return pivot;
      }
      for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : start.getPieces()) {
         if (piece instanceof net.minecraft.world.level.levelgen.structure.TemplateStructurePiece template
            && "entrance".equals(((com.fqkeh.illusioner.mixin.TemplateStructurePieceAccessor)template).illusioner$templateName())) {
            return template.templatePosition();
         }
      }
      return pivot;
   }

   /** The palette for a basement under the mansion entrance at {@code surface}, if a mod re-palettes mansions. */
   public static Optional<MansionPalette> forMansion(WorldGenLevel level, BlockPos surface, BlockPos pivot, StructurePlaceSettings entranceSettings) {
      if (!ModList.get().isLoaded("lithostitched") || !breaksSeedParity()) {
         return Optional.empty();
      } else {
         Optional<Registry<StructureProcessorList>> lists = level.registryAccess().registry(Registries.PROCESSOR_LIST);
         StructureProcessorList list = lists.map(registry -> registry.get(LIST)).orElse(null);
         if (list == null || list.list().isEmpty()) {
            return Optional.empty();
         } else {
            MansionPalette palette = new MansionPalette(list.list(), surface.immutable(), Map.of());
            Map<Block, Block> extras = palette.followWood(level, pivot, entranceSettings);
            return Optional.of(extras.isEmpty() ? palette : new MansionPalette(list.list(), surface.immutable(), extras));
         }
      }
   }

   private static boolean breaksSeedParity() {
      try {
         Object value = Class.forName("dev.worldgen.lithostitched.Lithostitched").getMethod("breaksSeedParity").invoke(null);
         return !(value instanceof Boolean b) || b;
      } catch (ReflectiveOperationException | LinkageError e) {
         return true;
      }
   }

   /** Asks the list what dark oak planks become here, and maps the dark oak shapes it does not cover to that wood. */
   private Map<Block, Block> followWood(LevelReader level, BlockPos pivot, StructurePlaceSettings settings) {
      StructureBlockInfo probe = new StructureBlockInfo(this.surface, Blocks.DARK_OAK_PLANKS.defaultBlockState(), null);
      StructureBlockInfo result = this.runList(level, pivot, probe, probe, settings);
      if (result == null || result.state().is(Blocks.DARK_OAK_PLANKS)) {
         return Map.of();
      } else {
         ResourceLocation planks = BuiltInRegistries.BLOCK.getKey(result.state().getBlock());
         if (!planks.getPath().endsWith("_planks")) {
            return Map.of();
         } else {
            String wood = planks.getPath().substring(0, planks.getPath().length() - "planks".length());
            Map<Block, Block> extras = new HashMap<>();
            for (String shape : DARK_OAK_SHAPES) {
               Block from = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace("dark_oak_" + shape));
               ResourceLocation toId = ResourceLocation.fromNamespaceAndPath(planks.getNamespace(), wood + shape);
               if (from != Blocks.AIR && BuiltInRegistries.BLOCK.containsKey(toId)) {
                  extras.put(from, BuiltInRegistries.BLOCK.get(toId));
               }
            }

            IllusionerFixes.LOGGER.debug("Mansion basement follows {} wood", planks.getNamespace() + ":" + wood);
            return extras;
         }
      }
   }

   private StructureBlockInfo runList(LevelReader level, BlockPos pivot, StructureBlockInfo relative, StructureBlockInfo info, StructurePlaceSettings settings) {
      StructureBlockInfo current = info;
      for (StructureProcessor processor : this.processors) {
         current = processor.processBlock(level, this.surface, pivot, relative, current, settings);
         if (current == null) {
            return null;
         }
      }

      return current;
   }

   @Override
   public StructureBlockInfo processBlock(
      LevelReader level, BlockPos pos, BlockPos pivot, StructureBlockInfo relative, StructureBlockInfo absolute, StructurePlaceSettings settings
   ) {
      StructureBlockInfo processed = this.runList(level, pivot, relative, absolute, settings);
      if (processed != null) {
         Block replacement = this.extras.get(processed.state().getBlock());
         if (replacement != null) {
            processed = new StructureBlockInfo(processed.pos(), copyProperties(processed.state(), replacement.defaultBlockState()), processed.nbt());
         }
      }

      return processed;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static BlockState copyProperties(BlockState from, BlockState to) {
      BlockState result = to;
      for (Property property : from.getProperties()) {
         if (result.hasProperty(property)) {
            result = result.setValue(property, from.getValue(property));
         }
      }

      return result;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return TYPE.get();
   }
}
