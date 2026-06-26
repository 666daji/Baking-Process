package org.bakingprocess.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.bakingprocess.registry.ModRecipeSerializers;
import org.bakingprocess.registry.ModRecipeTypes;

import java.util.Map;

/**
 * 支持多步骤切割的切菜配方
 */
public class CutRecipe implements Recipe<RecipeInput> {
    private final Ingredient input;
    private final int totalCuts;
    private final Map<Integer, NonNullList<ItemStack>> cutStateMap;
    private final NonNullList<ItemStack> defaultState;

    public CutRecipe(Ingredient input, int totalCuts,
                     Map<Integer, NonNullList<ItemStack>> cutStateMap,
                     NonNullList<ItemStack> defaultState) {
        this.input = input;
        this.totalCuts = totalCuts;
        this.cutStateMap = cutStateMap;
        this.defaultState = defaultState;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level world) {
        // 只检查主槽位（索引0）
        return input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registryManager) {
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
    public ItemStack getResultItem(HolderLookup.Provider registryManager) {
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getOutput() {
        NonNullList<ItemStack> finalState = getCutState(totalCuts);
        if (!finalState.isEmpty() && !finalState.get(0).isEmpty()) {
            return finalState.get(0).copy();
        }
        return ItemStack.EMPTY;
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