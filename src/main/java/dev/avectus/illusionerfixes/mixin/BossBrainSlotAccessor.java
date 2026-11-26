package dev.avectus.illusionerfixes.mixin;

import com.fqkeh.illusioner.boss.BossBrain;
import com.fqkeh.illusioner.boss.BossBrainSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BossBrainSlot.class, remap = false)
public interface BossBrainSlotAccessor {
   @Accessor("brain")
   BossBrain<?> illusionerfixes$brain();
}
