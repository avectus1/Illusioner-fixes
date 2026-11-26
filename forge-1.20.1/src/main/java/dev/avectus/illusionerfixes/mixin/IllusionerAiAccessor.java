package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.boss.illusioner.IllusionerAi;
import com.fqkeh.illusioner.boss.illusioner.illusion.platform.PlatformRunAttack;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = IllusionerAi.class, remap = false)
public interface IllusionerAiAccessor {
   @Accessor("arenaOrigin")
   BlockPos illusionerfixes$arenaOrigin();

   @Accessor("platformRun")
   PlatformRunAttack illusionerfixes$platformRun();
}
