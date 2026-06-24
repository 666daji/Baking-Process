package org.bakingprocess.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BakingProcess.MOD_ID);

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, Supplier<EntityType<T>> supplier) {
        return ENTITY_TYPES.register(id, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
