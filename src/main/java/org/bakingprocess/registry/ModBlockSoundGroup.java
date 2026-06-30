package org.bakingprocess.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;

import java.util.function.Supplier;

public class ModBlockSoundGroup {
    public static final Supplier<SoundType> KITCHEN_KNIFE = () -> new DeferredSoundType(
            1.0F,
            1.0F,
            ModSounds.KITCHEN_KNIFE_FETCH::get,
            () -> SoundEvents.METAL_STEP,
            ModSounds.KITCHEN_KNIFE_PLACE::get,
            () -> SoundEvents.METAL_HIT,
            () -> SoundEvents.METAL_FALL
    );
}
