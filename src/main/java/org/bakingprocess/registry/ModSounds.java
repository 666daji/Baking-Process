package org.bakingprocess.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.twcore.TWCore;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BakingProcess.MOD_ID);

    public static final RegistryObject<SoundEvent> COOKING_SOUND = register("stove_baking");
    public static final RegistryObject<SoundEvent> GRINDING_STONE_GRINDING = register("grinding_stone_grinding");
    public static final RegistryObject<SoundEvent> CUT = register("cut");
    public static final RegistryObject<SoundEvent> CUT_MEAT = register("cut_meat");
    public static final RegistryObject<SoundEvent> KITCHEN_KNIFE_PLACE = register("kitchen_knife_place");
    public static final RegistryObject<SoundEvent> KITCHEN_KNIFE_FETCH = register("kitchen_knife_fetch");
    public static final RegistryObject<SoundEvent> KITCHEN_KNIFE_BOARD_PLACE = register("kitchen_knife_board_place");
    public static final RegistryObject<SoundEvent> SOUP_FILL = register("soup_fill");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        TWCore.createResourceLocation(BakingProcess.MOD_ID, name)));
    }

    public static void registerAll(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
