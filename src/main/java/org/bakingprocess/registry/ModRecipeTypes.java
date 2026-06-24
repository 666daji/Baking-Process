package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.recipe.*;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, BakingProcess.MOD_ID);

    public static final RegistryObject<RecipeType<GrindingRecipe>> GRINDING = register("grinding");
    public static final RegistryObject<RecipeType<StoveRecipe>> STOVE = register("stove");
    public static final RegistryObject<RecipeType<CutRecipe>> CUT = register("cut");
    public static final RegistryObject<RecipeType<DoughRecipe>> DOUGH_MAKING = register("dough_making");
    public static final RegistryObject<RecipeType<PlatingRecipe>> PLATING = register("plating");

    static <T extends Recipe<?>> RegistryObject<RecipeType<T>> register(String id) {
        return RECIPE_TYPES.register(id, () -> new RecipeType<T>() {
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
