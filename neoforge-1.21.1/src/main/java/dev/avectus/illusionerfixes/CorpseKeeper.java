package dev.avectus.illusionerfixes;



import com.fqkeh.illusioner.player.PlayerReturnState;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Compatibility with the Corpse mod (optional, found by registry id): corpses of players who die in
 * the arena are kept on the centre of the circle the fight is on, following it when the shattered
 * path moves it, and are carried home with their owner when the encounter ends, since the arena is
 * cleared afterwards.
 */
public final class CorpseKeeper {
   private static final ResourceLocation CORPSE = ResourceLocation.fromNamespaceAndPath("corpse", "corpse");
   private static final int HOME_TIMEOUT_TICKS = 600;
   private static final List<CorpseKeeper.Pending> PENDING = new ArrayList<>();
   private static Method ownerGetter;
   private static boolean ownerGetterResolved;

   private CorpseKeeper() {
   }

   public static void initialize() {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS
         .addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, event -> tickPending(event.getServer()));
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS
         .addListener(net.neoforged.neoforge.event.server.ServerStoppedEvent.class, event -> PENDING.clear());
   }

   private static EntityType<?> corpseType() {
      return BuiltInRegistries.ENTITY_TYPE.containsKey(CORPSE) ? BuiltInRegistries.ENTITY_TYPE.get(CORPSE) : null;
   }

   private static List<? extends Entity> corpses(ServerLevel level, BlockPos origin) {
      EntityType<?> type = corpseType();
      if (type == null) {
         return List.of();
      } else {
         AABB area = new AABB(
            origin.getX() - 256, level.getMinBuildHeight() - 64, origin.getZ() - 256, origin.getX() + 256, level.getMaxBuildHeight() + 64, origin.getZ() + 256
         );
         return level.getEntities(type, area, entity -> !entity.isRemoved());
      }
   }

   /** Moves every corpse that is not lying on the circle centre onto it. */
   public static void keepOnCircle(ServerLevel level, BlockPos arenaOrigin, Vec3 centre) {
      for (Entity corpse : corpses(level, arenaOrigin)) {
         Vec3 at = corpse.position();
         double flat = Math.hypot(at.x() - centre.x(), at.z() - centre.z());
         if (flat > 1.5 || at.y() < centre.y() - 1.0 || at.y() > centre.y() + 4.0) {
            corpse.teleportTo(centre.x(), centre.y() + 0.1, centre.z());
            corpse.setDeltaMovement(Vec3.ZERO);
            corpse.fallDistance = 0.0F;
         }
      }
   }

   /** Queues every corpse in the arena to travel to where its owner entered the encounter from. */
   public static void sendHome(ServerLevel arena, BlockPos arenaOrigin, Map<UUID, PlayerReturnState> homes) {
      PlayerReturnState fallback = homes.values().stream().findFirst().orElse(null);
      int due = arena.getServer().getTickCount() + HOME_TIMEOUT_TICKS;
      for (Entity corpse : corpses(arena, arenaOrigin)) {
         PlayerReturnState home = owner(corpse).map(homes::get).orElse(fallback);
         if (home == null) {
            home = fallback;
         }

         if (home != null) {
            BlockPos at = corpse.blockPosition();
            arena.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(at), 1, at);
            PENDING.add(new CorpseKeeper.Pending(arena, corpse.getUUID(), home, due));
            IllusionerFixes.LOGGER.info("Carrying corpse {} out of the arena to {}", corpse.getUUID(), home.position());
         }
      }
   }

   private static void tickPending(MinecraftServer server) {
      for (Iterator<CorpseKeeper.Pending> it = PENDING.iterator(); it.hasNext(); ) {
         CorpseKeeper.Pending pending = it.next();
         ServerLevel target = server.getLevel(pending.home().dimension());
         Entity corpse = pending.arena().getEntity(pending.corpse());
         boolean expired = server.getTickCount() > pending.due();
         if (target == null || corpse != null && corpse.isRemoved()) {
            it.remove();
         } else {
            BlockPos homePos = BlockPos.containing(pending.home().position());
            target.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(homePos), 1, homePos);
            if (corpse != null) {
               BlockPos arenaPos = corpse.blockPosition();
               pending.arena().getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(arenaPos), 1, arenaPos);
            }

            if (corpse != null && target.areEntitiesLoaded(ChunkPos.asLong(homePos))) {
               corpse.changeDimension(
                  new DimensionTransition(target, pending.home().position(), Vec3.ZERO, corpse.getYRot(), corpse.getXRot(), DimensionTransition.DO_NOTHING)
               );
               it.remove();
            } else if (expired) {
               IllusionerFixes.LOGGER.warn("Could not carry corpse {} out of the arena in time; it stays at the arena centre", pending.corpse());
               it.remove();
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static Optional<UUID> owner(Entity corpse) {
      if (!ownerGetterResolved) {
         ownerGetterResolved = true;
         try {
            ownerGetter = corpse.getClass().getMethod("getCorpseUUID");
         } catch (NoSuchMethodException e) {
            IllusionerFixes.LOGGER.warn("Corpse entity has no getCorpseUUID(); corpses go to the first participant's return point");
         }
      }

      if (ownerGetter != null) {
         try {
            Object value = ownerGetter.invoke(corpse);
            if (value instanceof Optional<?> optional) {
               return (Optional<UUID>)optional;
            }
         } catch (ReflectiveOperationException | ClassCastException e) {
            IllusionerFixes.LOGGER.warn("Could not read the owner of corpse {}", corpse.getUUID(), e);
         }
      }

      return Optional.empty();
   }

   private record Pending(ServerLevel arena, UUID corpse, PlayerReturnState home, int due) {
   }
}
