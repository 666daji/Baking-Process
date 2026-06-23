package org.bakingprocess.recipe;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.bakingprocess.registry.ModRecipeSerializers;
import org.bakingprocess.registry.ModRecipeTypes;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

public class StoveRecipe implements Recipe<Container> {
    protected final ResourceLocation id;
    protected final Either<ItemStack, Content> input;
    protected final Either<ItemStack, Content> output;
    protected final int bakingTime;
    protected final int MaxInputCount;

    public StoveRecipe(ResourceLocation id, Either<ItemStack, Content> input, Either<ItemStack, Content> output, int MaxInputCount, int bakingTime) {
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
    public boolean matches(Container inventory, Level world) {
        ItemStack stack = inventory.getItem(0);

        return input.map(inputStack -> ItemStack.isSameItem(stack, inputStack),
                inputContent -> inputContent.equals(ContainerUtil.extractContent(stack)));
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        ItemStack stack = inventory.getItem(0);
        int count = Math.min(stack.getCount(), MaxInputCount);

        return output.map(outputStack -> outputStack.copyWithCount(count),
                outputContent -> ContainerUtil.analyze(stack)
                        .map(containerStack -> containerStack.replaceContent(outputContent))
                        .orElse(stack));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return this.output.map(outputStack -> outputStack,
                outputContent -> ItemStack.EMPTY);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STOVE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.STOVE.get();
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
    public Either<ItemStack, Content> getInput() {
        return input;
    }

    /**
     * 获取输出。
     */
    public Either<ItemStack, Content> getOutput() {
        return output;
    }
}