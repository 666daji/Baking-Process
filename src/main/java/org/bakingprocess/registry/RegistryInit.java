package org.bakingprocess.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public class RegistryInit {
    public static void init(IEventBus modEventBus) {
        ModSounds.registerAll(modEventBus);
        ModBlocks.registerAll(modEventBus);
        ModItems.registerAll(modEventBus);
        ModContents.registerAll(modEventBus);
        ModContainers.registerAll(modEventBus);
        ModBlockEntityTypes.registerAll(modEventBus);
        ModEntityTypes.registerAll(modEventBus);
        ModRecipeTypes.registerAll(modEventBus);
        ModItemGroups.registerAll(modEventBus);
    }
}
