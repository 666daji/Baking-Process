package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BakingProcess.MOD_ID);

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String id, Supplier<EntityType<T>> supplier) {
        return ENTITY_TYPES.register(id, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
