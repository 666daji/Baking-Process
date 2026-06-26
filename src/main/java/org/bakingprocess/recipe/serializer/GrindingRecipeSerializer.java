package org.bakingprocess.recipe.serializer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.GrindingRecipe;

public class GrindingRecipeSerializer implements RecipeSerializer<GrindingRecipe> {

    private static final Codec<ItemStack> RESULT_CODEC = Codec.either(
            Codec.STRING,
            ItemStack.CODEC
    ).xmap(
            either -> either.map(
                    idStr -> {
                        ResourceLocation id = ResourceLocation.tryParse(idStr);
                        if (id == null) throw new IllegalArgumentException("Invalid item ID: " + idStr);
                        Item item = BuiltInRegistries.ITEM.getOptional(id)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + idStr));
                        return new ItemStack(item);
                    },
                    stack -> stack
            ),
            stack -> Either.left(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
    );

    public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(GrindingRecipe::getInput),
                    Codec.INT.optionalFieldOf("MaxInputCount", 1).forGetter(GrindingRecipe::getInputCount),
                    RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                    Codec.INT.optionalFieldOf("grindingTime", 200).forGetter(GrindingRecipe::getGrindingTime)
            ).apply(instance, GrindingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> PACKET_CODEC =
            StreamCodec.ofMember(GrindingRecipeSerializer::encode, GrindingRecipeSerializer::decode);

    private static void encode(GrindingRecipe recipe, RegistryFriendlyByteBuf buf) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
        ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        buf.writeVarInt(recipe.getInputCount());
        buf.writeVarInt(recipe.getGrindingTime());
    }

    private static GrindingRecipe decode(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
        int inputCount = buf.readVarInt();
        int grindingTime = buf.readVarInt();
        return new GrindingRecipe(input, inputCount, output, grindingTime);
    }

    @Override
    public MapCodec<GrindingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
        return PACKET_CODEC;
    }
}