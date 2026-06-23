package org.bakingprocess.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.bakingprocess.registry.ModRecipeSerializers;
import org.bakingprocess.registry.ModRecipeTypes;

public class GrindingRecipe extends SimpleCraftRecipe {
    protected final int inputCount;
    protected final int grindingTime;

    public GrindingRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack output, int grindingTime) {
        super(id, input, output);
        this.inputCount = inputCount;
        this.grindingTime = grindingTime;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        ItemStack stack = inventory.getItem(0);
        return this.input.test(stack) && stack.getCount() >= this.inputCount;
    }

    public int getGrindingTime() {
        return this.grindingTime;
    }

    public int getInputCount() {
        return this.inputCount;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GRINDING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.GRINDING.get();
    }
}