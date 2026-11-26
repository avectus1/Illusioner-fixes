package dev.avectus.illusionerfixes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IllusionerFixes.MOD_ID)
public final class IllusionerFixes {
   public static final String MOD_ID = "illusionerfixes";
   public static final Logger LOGGER = LoggerFactory.getLogger("Illusioner Fixes");

   public IllusionerFixes() {
      IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
      MansionPalette.register(modBus);
      dev.avectus.illusionerfixes.compat.LukisMansions.register(modBus);
      dev.avectus.illusionerfixes.compat.LukisLootFix.register(modBus);
      CorpseKeeper.initialize();
      if (!System.getProperty("illusionerfixes.test.script", "").isEmpty()) {
         try {
            Class.forName("dev.avectus.illusionerfixes.devtest.DevScript").getMethod("install").invoke(null);
         } catch (ReflectiveOperationException e) {
            LOGGER.warn("Test script requested but the dev test driver is not packaged", e);
         }
      }
   }

   /** Encounter entities saved by an interrupted encounter must not come back as a second boss. */
}
