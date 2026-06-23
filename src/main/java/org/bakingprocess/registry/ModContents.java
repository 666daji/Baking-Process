package org.bakingprocess.registry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.content.ShapedDoughContent;
import org.bakingprocess.food.ModFoodComponents;
import org.twcore.TWCore;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

public class ModContents {
    public static final DeferredRegister<Content> CONTENTS =
            DeferredRegister.create(TWRegistries.CONTENT.get(), BakingProcess.MOD_ID);

    public static final String DISHES = "dishes";
    public static final String SHAPED_DOUGH = "shaped_dough";

    // 菜肴
    public static final RegistryObject<Content> BEEF_BERRIES = register("beef_berries",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_BEEF_BERRIES = register("cooked_beef_berries",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_BEEF_BERRIES, 2));
    public static final RegistryObject<Content> ROASTED_MUSHROOMS = register("roasted_mushrooms",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_ROASTED_MUSHROOMS = register("cooked_roasted_mushrooms",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_ROASTED_MUSHROOMS, 4));
    public static final RegistryObject<Content> HONEY_ROASTED_BEEF = register("honey_roasted_beef",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_HONEY_ROASTED_BEEF = register("cooked_honey_roasted_beef",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_HONEY_ROASTED_BEEF, 3));
    public static final RegistryObject<Content> FRY_SALMON_CUBES = register("fry_salmon_cubes",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_FRY_SALMON_CUBES = register("cooked_fry_salmon_cubes",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_FRY_SALMON_CUBES, 4));
    public static final RegistryObject<Content> GRILLED_FISH_POTATOES = register("grilled_fish_potatoes",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_GRILLED_FISH_POTATOES = register("cooked_grilled_fish_potatoes",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_GRILLED_FISH_POTATOES, 4));
    public static final RegistryObject<Content> DELUXE_ROASTED_RABBIT = register("deluxe_roasted_rabbit",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_DELUXE_ROASTED_RABBIT = register("cooked_deluxe_roasted_rabbit",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_DELUXE_ROASTED_RABBIT, 4));
    public static final RegistryObject<Content> HONEY_ROASTED_MUTTON = register("honey_roasted_mutton",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_HONEY_ROASTED_MUTTON = register("cooked_honey_roasted_mutton",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_HONEY_ROASTED_MUTTON, 4));
    public static final RegistryObject<Content> DELUXE_ROAST_CHICKEN = register("deluxe_roast_chicken",
            () -> new DishesContent(DISHES));
    public static final RegistryObject<Content> COOKED_DELUXE_ROAST_CHICKEN = register("cooked_deluxe_roast_chicken",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_DELUXE_ROAST_CHICKEN, 4));

    // 定型面团
    public static final RegistryObject<Content> TOAST_EMBRYO = register("toast_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "toast_dough"),
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "toast_embryo_mold")));
    public static final RegistryObject<Content> TOAST = register("toast",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "toast"),
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "toast_embryo_mold")));
    public static final RegistryObject<Content> CAKE_EMBRYO = register("cake_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "cake_dough"),
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "cake_embryo_mold")));
    public static final RegistryObject<Content> BAKED_CAKE_EMBRYO = register("baked_cake_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "baked_cake_embryo"),
                    TWCore.createResourceLocation(BakingProcess.MOD_ID, "cake_embryo_mold")));

    private static RegistryObject<Content> register(String name, java.util.function.Supplier<Content> supplier) {
        return CONTENTS.register(name, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        CONTENTS.register(modEventBus);
    }
}
