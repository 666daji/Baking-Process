package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.recipe.serializer.*;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, BakingProcess.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> GRINDING = register("grinding", GrindingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> STOVE = register("stove", StoveRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CUT = register("cut", CutRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> DOUGH_MAKING = register("dough_making", DoughRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> PLATING = register("plating", PlatingRecipeSerializer::new);

    private static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> register(String id, java.util.function.Supplier<? extends RecipeSerializer<?>> supplier) {
        return RECIPE_SERIALIZERS.register(id, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
