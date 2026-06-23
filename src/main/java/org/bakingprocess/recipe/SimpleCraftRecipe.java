package org.bakingprocess.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public abstract class SimpleCraftRecipe implements Recipe<Container> {
    protected final ResourceLocation id;
    protected final Ingredient input;
    public final ItemStack output;

    public SimpleCraftRecipe(ResourceLocation id, Ingredient input, ItemStack output) {
        this.id = id;
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
    public boolean matches(Container inventory, Level world) {
        return this.input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
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
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }
}