package org.bakingprocess.registry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.content.ShapedDoughContent;
import org.bakingprocess.food.ModFoodComponents;
import org.twcore.content.Content;
import org.twcore.registry.Contents;

public class ModContents {
    public static final DeferredRegister<Content> CONTENTS = DeferredRegister.create(Contents.CONTENT.getRegistryKey(), BakingProcess.MOD_ID);

    public static final String DISHES = "dishes";
    public static final String SHAPED_DOUGH = "shaped_dough";

    // 菜肴
    public static final DeferredHolder<Content, Content> BEEF_BERRIES = register("beef_berries",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_BEEF_BERRIES = register("cooked_beef_berries",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_BEEF_BERRIES, 2));
    public static final DeferredHolder<Content, Content> ROASTED_MUSHROOMS = register("roasted_mushrooms",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_ROASTED_MUSHROOMS = register("cooked_roasted_mushrooms",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_ROASTED_MUSHROOMS, 4));
    public static final DeferredHolder<Content, Content> HONEY_ROASTED_BEEF = register("honey_roasted_beef",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_HONEY_ROASTED_BEEF = register("cooked_honey_roasted_beef",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_HONEY_ROASTED_BEEF, 3));
    public static final DeferredHolder<Content, Content> FRY_SALMON_CUBES = register("fry_salmon_cubes",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_FRY_SALMON_CUBES = register("cooked_fry_salmon_cubes",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_FRY_SALMON_CUBES, 4));
    public static final DeferredHolder<Content, Content> GRILLED_FISH_POTATOES = register("grilled_fish_potatoes",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_GRILLED_FISH_POTATOES = register("cooked_grilled_fish_potatoes",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_GRILLED_FISH_POTATOES, 4));
    public static final DeferredHolder<Content, Content> DELUXE_ROASTED_RABBIT = register("deluxe_roasted_rabbit",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_DELUXE_ROASTED_RABBIT = register("cooked_deluxe_roasted_rabbit",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_DELUXE_ROASTED_RABBIT, 4));
    public static final DeferredHolder<Content, Content> HONEY_ROASTED_MUTTON = register("honey_roasted_mutton",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_HONEY_ROASTED_MUTTON = register("cooked_honey_roasted_mutton",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_HONEY_ROASTED_MUTTON, 4));
    public static final DeferredHolder<Content, Content> DELUXE_ROAST_CHICKEN = register("deluxe_roast_chicken",
            () -> new DishesContent(DISHES));
    public static final DeferredHolder<Content, Content> COOKED_DELUXE_ROAST_CHICKEN = register("cooked_deluxe_roast_chicken",
            () -> new DishesContent(DISHES, ModFoodComponents.COOKED_DELUXE_ROAST_CHICKEN, 4));

    // 定型面团
    public static final DeferredHolder<Content, Content> TOAST_EMBRYO = register("toast_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "toast_dough"),
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "toast_embryo_mold")));
    public static final DeferredHolder<Content, Content> TOAST = register("toast",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "toast"),
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "toast_embryo_mold")));
    public static final DeferredHolder<Content, Content> CAKE_EMBRYO = register("cake_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "cake_dough"),
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "cake_embryo_mold")));
    public static final DeferredHolder<Content, Content> BAKED_CAKE_EMBRYO = register("baked_cake_embryo",
            () -> new ShapedDoughContent(SHAPED_DOUGH,
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "baked_cake_embryo"),
                    ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "cake_embryo_mold")));

    private static DeferredHolder<Content, Content> register(String name, java.util.function.Supplier<Content> supplier) {
        return CONTENTS.register(name, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        CONTENTS.register(modEventBus);
    }
}
