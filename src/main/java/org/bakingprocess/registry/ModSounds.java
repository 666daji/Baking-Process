package org.bakingprocess.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, BakingProcess.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> COOKING_SOUND = register("stove_baking");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRINDING_STONE_GRINDING = register("grinding_stone_grinding");
    public static final DeferredHolder<SoundEvent, SoundEvent> CUT = register("cut");
    public static final DeferredHolder<SoundEvent, SoundEvent> CUT_MEAT = register("cut_meat");
    public static final DeferredHolder<SoundEvent, SoundEvent> KITCHEN_KNIFE_PLACE = register("kitchen_knife_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> KITCHEN_KNIFE_FETCH = register("kitchen_knife_fetch");
    public static final DeferredHolder<SoundEvent, SoundEvent> KITCHEN_KNIFE_BOARD_PLACE = register("kitchen_knife_board_place");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUP_FILL = register("soup_fill");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, name)));
    }

    public static void registerAll(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
