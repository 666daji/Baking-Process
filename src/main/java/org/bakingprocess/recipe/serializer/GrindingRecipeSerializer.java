package org.bakingprocess.recipe.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.bakingprocess.recipe.GrindingRecipe;

public class GrindingRecipeSerializer extends SimpleCraftRecipeSerializer<GrindingRecipe> {

    public GrindingRecipeSerializer() {
        super(GrindingRecipeSerializer::createRecipe);
    }

    private static GrindingRecipe createRecipe(ResourceLocation id, Ingredient input, ItemStack output, Object extraData) {
        GrindingExtraData data = (GrindingExtraData) extraData;
        return new GrindingRecipe(id, input, data.inputCount, output, data.grindingTime);
    }

    @Override
    protected Object readExtraData(JsonObject json) {
        int inputCount = GsonHelper.getAsInt(json, "MaxInputCount", 1);
        int grindingTime = GsonHelper.getAsInt(json, "grindingTime", 200);
        return new GrindingExtraData(inputCount, grindingTime);
    }

    @Override
    protected Object readExtraData(FriendlyByteBuf buf) {
        int inputCount = buf.readVarInt();
        int grindingTime = buf.readVarInt();
        return new GrindingExtraData(inputCount, grindingTime);
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf, GrindingRecipe recipe) {
        buf.writeVarInt(recipe.getInputCount());
        buf.writeVarInt(recipe.getGrindingTime());
    }

    /**
     * 封装额外数据
     * @param inputCount 成分数量
     * @param grindingTime 研磨时间
     */
    private record GrindingExtraData(int inputCount, int grindingTime) {}
}