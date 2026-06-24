package org.bakingprocess.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

import java.util.function.Supplier;

public class ModBlockSoundGroup {
    public static final Supplier<SoundType> KITCHEN_KNIFE = () -> new ForgeSoundType(
            1.0F,
            1.0F,
            ModSounds.KITCHEN_KNIFE_FETCH,
            () -> SoundEvents.METAL_STEP,
            ModSounds.KITCHEN_KNIFE_PLACE,
            () -> SoundEvents.METAL_HIT,
            () -> SoundEvents.METAL_FALL
    );
}
