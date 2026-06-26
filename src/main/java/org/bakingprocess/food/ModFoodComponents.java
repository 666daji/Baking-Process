package org.bakingprocess.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodComponents {
    // 面食
    public static final FoodProperties HARD_BREAD = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6f).build();
    public static final FoodProperties SMALL_BREAD = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.4f).build();
    public static final FoodProperties BAGUETTE = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.7f).build();
    public static final FoodProperties TOAST = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.8f).build();

    // 可食用容器
    public static final FoodProperties HARD_BREAD_BOAT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.6f).build();

    // 切片食物
    public static final FoodProperties CARROT_SLICES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.2f).build();
    public static final FoodProperties CARROT_HEAD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1f).build();
    public static final FoodProperties COD_CUBES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.05f).build();
    public static final FoodProperties COD_HEAD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.05f).build();
    public static final FoodProperties COOKED_COD_CUBES = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3f).build();
    public static final FoodProperties COOKED_COD_HEAD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.3f).build();
    public static final FoodProperties SALMON_CUBES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1f).build();
    public static final FoodProperties COOKED_SALMON_CUBES = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.4f).build();
    public static final FoodProperties SEPARATE_POTATO_CUBES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.15f).build();
    public static final FoodProperties POTATO_CUBES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.3f)
            .effect(new MobEffectInstance(MobEffects.POISON, 60, 0), 0.3f)
            .build();
    public static final FoodProperties SEPARATE_COOKED_POTATO_CUBES = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4f).build();
    public static final FoodProperties COOKED_POTATO_CUBES = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6f).build();

    // 饮品
    public static final FoodProperties MILK = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.4f).build();

    // 调料
    public static final FoodProperties SUGAR_FLOUR = new FoodProperties.Builder()
            .nutrition(0).saturationModifier(0.05f)
            .build();

    // 菜肴
    public static final FoodProperties COOKED_BEEF_BERRIES = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(1.05f).build();
    public static final FoodProperties COOKED_ROASTED_MUSHROOMS = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.74f).build();
    public static final FoodProperties COOKED_HONEY_ROASTED_BEEF = new FoodProperties.Builder()
            .nutrition(18).saturationModifier(2.15f).build();
    public static final FoodProperties COOKED_FRY_SALMON_CUBES = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(1.50f).build();
    public static final FoodProperties COOKED_GRILLED_FISH_POTATOES = new FoodProperties.Builder()
            .nutrition(15).saturationModifier(2.10f).build();
    public static final FoodProperties COOKED_DELUXE_ROASTED_RABBIT = new FoodProperties.Builder()
            .nutrition(17).saturationModifier(2.12f).build();
    public static final FoodProperties COOKED_HONEY_ROASTED_MUTTON = new FoodProperties.Builder()
            .nutrition(18).saturationModifier(2.51f).build();
    public static final FoodProperties COOKED_DELUXE_ROAST_CHICKEN = new FoodProperties.Builder()
            .nutrition(20).saturationModifier(2.47f).build();
}