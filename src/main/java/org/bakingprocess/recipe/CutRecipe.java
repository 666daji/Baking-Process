package org.bakingprocess.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.bakingprocess.registry.ModRecipeSerializers;
import org.bakingprocess.registry.ModRecipeTypes;

import java.util.Map;

/**
 * 支持多步骤切割的切菜配方
 */
public class CutRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final int totalCuts; // 总共需要切的次�?
    private final Map<Integer, NonNullList<ItemStack>> cutStateMap; // 第几刀对应的库存状�?
    private final NonNullList<ItemStack> defaultState; // 默认库存状态（5个槽位）

    public CutRecipe(ResourceLocation id, Ingredient input, int totalCuts,
                     Map<Integer, NonNullList<ItemStack>> cutStateMap,
                     NonNullList<ItemStack> defaultState) {
        this.id = id;
        this.input = input;
        this.totalCuts = totalCuts;
        this.cutStateMap = cutStateMap;
        this.defaultState = defaultState;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        // 只检查主槽位（索引0）
        return input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        // 返回最后一刀时的库存状态第一个物品
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        // 返回最后一刀时的库存状态第一个物品
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getOutput() {
        // 返回最后一刀时的库存状态第一个物品
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CUT.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CUT.get();
    }

    public Ingredient getInput() {
        return input;
    }

    public int getTotalCuts() {
        return totalCuts;
    }

    public NonNullList<ItemStack> getCutState(int cutIndex) {
        return cutStateMap.getOrDefault(cutIndex, defaultState);
    }

    public NonNullList<ItemStack> getDefaultState() {
        return defaultState;
    }

    public Map<Integer, NonNullList<ItemStack>> getCutStateMap() {
        return cutStateMap;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        return ingredients;
    }

    /**
     * 获取完成切割后的输出数量
     */
    public int getOutputCount() {
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).getCount();
        }
        return 0;
    }
}