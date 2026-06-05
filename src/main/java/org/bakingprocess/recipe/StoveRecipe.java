package org.bakingprocess.recipe;

import com.mojang.datafixers.util.Either;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.bakingprocess.contentsystem.api.ContainerUtil;
import org.bakingprocess.contentsystem.content.AbstractContent;
import org.bakingprocess.registry.ModRecipeSerializers;
import org.bakingprocess.registry.ModRecipeTypes;

public class StoveRecipe implements Recipe<Inventory> {
    protected final Identifier id;
    protected final Either<ItemStack, AbstractContent> input;
    protected final Either<ItemStack, AbstractContent> output;
    protected final int bakingTime;
    protected final int MaxInputCount;

    public StoveRecipe(Identifier id, Either<ItemStack, AbstractContent> input, Either<ItemStack, AbstractContent> output, int MaxInputCount, int bakingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.MaxInputCount = MaxInputCount;
        this.bakingTime = bakingTime;
    }

    public int getMaxInputCount() {
        return MaxInputCount;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        ItemStack stack = inventory.getStack(0);

        return input.map(inputStack -> ItemStack.areItemsEqual(stack, inputStack),
                inputContent -> inputContent.equals(ContainerUtil.extractContent(stack)));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack stack = inventory.getStack(0);
        int count = Math.min(stack.getCount(), MaxInputCount);

        return output.map(outputStack -> outputStack.copyWithCount(count),
                outputContent -> ContainerUtil.replaceContent(stack, outputContent));
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.output.map(outputStack -> outputStack,
                outputContent -> ItemStack.EMPTY);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STOVE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.STOVE;
    }

    /**
     * 获取烘培该配方需要的总时间，这与输入的数量有关。
     * <p>输入的数量如果超过了此配方的{@linkplain StoveRecipe#MaxInputCount}</p>，则会按照配方的最大数量处理
     * @param count 烘烤的数量
     * @return 烘烤需要的总时间
     */
    public int getBakingTimeForInput(int count) {
        if (count <= 0) {
            return bakingTime;
        }
        return bakingTime * Math.min(MaxInputCount, count);
    }

    public int getBakingTime() {
        return bakingTime;
    }

    /**
     * 获取输入。
     */
    public Either<ItemStack, AbstractContent> getInput() {
        return input;
    }

    /**
     * 获取输出。
     */
    public Either<ItemStack, AbstractContent> getOutput() {
        return output;
    }
}