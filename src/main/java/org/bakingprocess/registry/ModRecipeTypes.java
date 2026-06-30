package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.recipe.*;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, BakingProcess.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING = register("grinding");
    public static final DeferredHolder<RecipeType<?>, RecipeType<StoveRecipe>> STOVE = register("stove");
    public static final DeferredHolder<RecipeType<?>, RecipeType<CutRecipe>> CUT = register("cut");
    public static final DeferredHolder<RecipeType<?>, RecipeType<DoughRecipe>> DOUGH_MAKING = register("dough_making");
    public static final DeferredHolder<RecipeType<?>, RecipeType<PlatingRecipe>> PLATING = register("plating");

    static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String id) {
        return RECIPE_TYPES.register(id, () -> new RecipeType<>() {
            public String toString() {
                return id;
            }
        });
    }

    public static void registerAll(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        ModRecipeSerializers.registerAll(modEventBus);
    }
}
