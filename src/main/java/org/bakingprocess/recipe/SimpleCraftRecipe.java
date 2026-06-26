package org.bakingprocess.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public abstract class SimpleCraftRecipe implements Recipe<RecipeInput> {
    protected final Ingredient input;
    public final ItemStack output;

    public SimpleCraftRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> defaultedList = NonNullList.create();
        defaultedList.add(this.input);
        return defaultedList;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level world) {
        return this.input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput inventory, HolderLookup.Provider registryManager) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryManager) {
        return this.output;
    }
}