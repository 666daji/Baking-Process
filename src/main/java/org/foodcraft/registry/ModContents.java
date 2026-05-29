package org.foodcraft.registry;

import net.minecraft.item.FoodComponents;
import net.minecraft.util.Identifier;
import org.foodcraft.FoodCraft;
import org.foodcraft.contentsystem.content.*;
import org.foodcraft.food.ModFoodComponents;

public class ModContents {
    // 汤
    public static final FoodContent MUSHROOM_STEW = FoodContent.createFoodContent(
            createModId("mushroom_stew"), ContentCategories.SOUP, FoodComponents.MUSHROOM_STEW);

    public static final FoodContent BEETROOT_SOUP = FoodContent.createFoodContent(
            createModId("beetroot_soup"), ContentCategories.SOUP, FoodComponents.BEETROOT_SOUP);

    public static final FoodContent RABBIT_STEW = FoodContent.createFoodContent(
            createModId("rabbit_stew"), ContentCategories.SOUP, FoodComponents.RABBIT_STEW);

    // 基础液体
    public static final AbstractContent WATER = new BaseLiquidContent(
            createModId("water"), 4159204);

    public static final AbstractContent MILK = new BaseLiquidContent(
            createModId("milk"), 0xFFFAF2ED);

    // 菜肴
    public static final DishesContent BEEF_BERRIES = new DishesContent(
            createModId("beef_berries"));

    public static final DishesContent COOKED_BEEF_BERRIES = new DishesContent(
            createModId("cooked_beef_berries"), ModFoodComponents.COOKED_BEEF_BERRIES, 2);

    public static final DishesContent ROASTED_MUSHROOMS = new DishesContent(
            createModId("roasted_mushrooms"));

    public static final DishesContent COOKED_ROASTED_MUSHROOMS = new DishesContent(
            createModId("cooked_roasted_mushrooms"), ModFoodComponents.COOKED_ROASTED_MUSHROOMS, 4);

    public static final DishesContent HONEY_ROASTED_BEEF = new DishesContent(
            createModId("honey_roasted_beef"));

    public static final DishesContent COOKED_HONEY_ROASTED_BEEF = new DishesContent(
            createModId("cooked_honey_roasted_beef"), ModFoodComponents.COOKED_HONEY_ROASTED_BEEF, 3);

    public static final DishesContent FRY_SALMON_CUBES = new DishesContent(
            createModId("fry_salmon_cubes"));

    public static final DishesContent COOKED_FRY_SALMON_CUBES = new DishesContent(
            createModId("cooked_fry_salmon_cubes"), ModFoodComponents.COOKED_FRY_SALMON_CUBES, 4);

    public static final DishesContent GRILLED_FISH_POTATOES = new DishesContent(
            createModId("grilled_fish_potatoes"));

    public static final DishesContent COOKED_GRILLED_FISH_POTATOES = new DishesContent(
            createModId("cooked_grilled_fish_potatoes"), ModFoodComponents.COOKED_GRILLED_FISH_POTATOES, 4);

    public static final DishesContent DELUXE_ROASTED_RABBIT = new DishesContent(
            createModId("deluxe_roasted_rabbit"));

    public static final DishesContent COOKED_DELUXE_ROASTED_RABBIT = new DishesContent(
            createModId("cooked_deluxe_roasted_rabbit"), ModFoodComponents.COOKED_DELUXE_ROASTED_RABBIT, 3);

    public static final DishesContent HONEY_ROASTED_MUTTON = new DishesContent(
            createModId("honey_roasted_mutton"));

    public static final DishesContent COOKED_HONEY_ROASTED_MUTTON = new DishesContent(
            createModId("cooked_honey_roasted_mutton"), ModFoodComponents.COOKED_HONEY_ROASTED_MUTTON, 4);

    public static final DishesContent DELUXE_ROAST_CHICKEN = new DishesContent(
                createModId("deluxe_roast_chicken"));

    public static final DishesContent COOKED_DELUXE_ROAST_CHICKEN = new DishesContent(
            createModId("cooked_deluxe_roast_chicken"), ModFoodComponents.COOKED_DELUXE_ROAST_CHICKEN, 4);

    // 定型面团
    public static final ShapedDoughContent TOAST_EMBRYO = new ShapedDoughContent(
            createModId("toast_embryo"), createModId("toast_dough"), createModId("toast_embryo_mold"));

    public static final ShapedDoughContent TOAST = new ShapedDoughContent(
            createModId("toast"), createModId("toast"), createModId("toast_embryo_mold"));

    public static final ShapedDoughContent CAKE_EMBRYO = new ShapedDoughContent(
            createModId("cake_embryo"), createModId("cake_dough"), createModId("cake_embryo_mold"));

    public static final ShapedDoughContent BAKED_CAKE_EMBRYO = new ShapedDoughContent(
            createModId("baked_cake_embryo"), createModId("baked_cake_embryo"), createModId("cake_embryo_mold"));

    // 糖浆
    public static final AbstractContent HONEY = AbstractContent.createSimpleContent(
            createModId("honey"), ContentCategories.SYRUP);

    // ================ 辅助方法 ================

    private static Identifier createModId(String path) {
        return new Identifier(FoodCraft.MOD_ID, path);
    }

    public static void registryContents() {}
}