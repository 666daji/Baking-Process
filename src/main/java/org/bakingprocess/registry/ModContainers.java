package org.bakingprocess.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.container.BreadBoatContainer;
import org.bakingprocess.container.DishesContainer;
import org.bakingprocess.container.MoldContainer;
import org.twcore.container.ContainerType;
import org.twcore.registry.ContainerTypes;

public class ModContainers {
    public static final DeferredRegister<ContainerType> CONTAINER_TYPES = DeferredRegister.create(ContainerTypes.CONTAINER_TYPE.getRegistryKey(), BakingProcess.MOD_ID);

    // 硬面包船
    public static final RegistryObject<ContainerType> HARD_BREAD_BOAT = register("hard_bread_boat",
            () -> new BreadBoatContainer(new ContainerType.ContainerSettings(ModItems.HARD_BREAD_BOAT)
                    .setUseSound(ModSounds.SOUP_FILL)));

    //铁盘
    public static final RegistryObject<ContainerType> IRON_PLATE = register("iron_plate",
            () -> new DishesContainer(new ContainerType.ContainerSettings(ModItems.IRON_PLATE)
                    .setUseSound(() -> SoundEvents.BUCKET_EMPTY)));
    //模具
    public static final RegistryObject<ContainerType> TOAST_EMBRYO_MOLD = register("toast_embryo_mold",
            () -> new MoldContainer(new ContainerType.ContainerSettings(ModItems.TOAST_EMBRYO_MOLD)
                    .setUseSound(() -> SoundEvents.SLIME_DEATH_SMALL)));
    public static final RegistryObject<ContainerType> CAKE_EMBRYO_MOLD = register("cake_embryo_mold",
            () -> new MoldContainer(new ContainerType.ContainerSettings(ModItems.CAKE_EMBRYO_MOLD)
                    .setUseSound(() -> SoundEvents.SLIME_DEATH_SMALL)));

    private static RegistryObject<ContainerType> register(String name, java.util.function.Supplier<ContainerType> supplier) {
        return CONTAINER_TYPES.register(name, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        CONTAINER_TYPES.register(modEventBus);
    }
}
