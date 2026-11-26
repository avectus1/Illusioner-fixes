package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.boss.BossBrainSlot;
import com.fqkeh.illusioner.boss.illusioner.IllusionerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = IllusionerEntity.class, remap = false)
public interface IllusionerEntityAccessor {
   @Accessor("ai")
   BossBrainSlot<IllusionerEntity> illusionerfixes$ai();
}
