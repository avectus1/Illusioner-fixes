package dev.avectus.illusionerfixes.compat;

import dev.avectus.illusionerfixes.IllusionerFixes;
import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

/**
 * Luki's Woodland Mansions' 1.20 build writes one loot table (mansions:mansion_common) in the
 * 1.20.5+ format ("value" instead of "name"), which 1.20.1 cannot read, so those chests came out
 * empty. A corrected copy is added as a built-in data pack on top of the mod's own data.
 */
public final class LukisLootFix {
   private LukisLootFix() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener(LukisLootFix::addPack);
   }

   private static void addPack(AddPackFindersEvent event) {
      if (event.getPackType() != PackType.SERVER_DATA || !ModList.get().isLoaded("mr_lukis_woodlandmansions")) {
         return;
      }
      Path root = ModList.get().getModFileById(IllusionerFixes.MOD_ID).getFile().findResource("lukisfix");
      Pack pack = Pack.readMetaAndCreate(
         IllusionerFixes.MOD_ID + ":lukis_loot_fix",
         Component.literal("Illusioner Fixes: Luki's loot fix"),
         true,
         id -> new PathPackResources(id, root, true),
         PackType.SERVER_DATA,
         Pack.Position.TOP,
         PackSource.BUILT_IN);
      if (pack != null) {
         event.addRepositorySource(consumer -> consumer.accept(pack));
      }
   }
}
