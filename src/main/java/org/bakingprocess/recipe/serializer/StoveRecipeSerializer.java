package org.bakingprocess.recipe.serializer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.StoveRecipe;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

public class StoveRecipeSerializer implements RecipeSerializer<StoveRecipe> {

    private static final Codec<Either<ItemStack, Content>> COMPONENT_CODEC =
            Codec.STRING.comapFlatMap(
                    s -> {
                        try {
                            return DataResult.success(parseComponentFromString(s));
                        } catch (Exception e) {
                            return DataResult.error(() -> "Invalid component string: " + s + " (" + e.getMessage() + ")");
                        }
                    },
                    StoveRecipeSerializer::componentToString
            );

    public static final MapCodec<StoveRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    COMPONENT_CODEC.fieldOf("ingredient").forGetter(StoveRecipe::getInput),
                    COMPONENT_CODEC.fieldOf("result").forGetter(StoveRecipe::getOutput),
                    Codec.INT.optionalFieldOf("MaxInputCount", 1).forGetter(StoveRecipe::getMaxInputCount),
                    Codec.INT.optionalFieldOf("stoveTime", 200).forGetter(StoveRecipe::getBakingTime)
            ).apply(instance, StoveRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StoveRecipe> PACKET_CODEC =
            StreamCodec.ofMember(StoveRecipeSerializer::encode, StoveRecipeSerializer::decode);

    private static void encode(StoveRecipe recipe, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(componentToString(recipe.getInput()));
        buf.writeUtf(componentToString(recipe.getOutput()));
        buf.writeInt(recipe.getMaxInputCount());
        buf.writeVarInt(recipe.getBakingTime());
    }

    private static StoveRecipe decode(RegistryFriendlyByteBuf buf) {
        Either<ItemStack, Content> input = parseComponentFromString(buf.readUtf());
        Either<ItemStack, Content> output = parseComponentFromString(buf.readUtf());
        int maxInputCount = buf.readInt();
        int bakingTime = buf.readVarInt();
        return new StoveRecipe(input, output, maxInputCount, bakingTime);
    }

    @Override
    public MapCodec<StoveRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StoveRecipe> streamCodec() {
        return PACKET_CODEC;
    }

    /** 将 Either<ItemStack, Content> 转换为 "item|..." 或 "content|..." 字符串 */
    private static String componentToString(Either<ItemStack, Content> component) {
        return component.map(
                stack -> "item|" + BuiltInRegistries.ITEM.getKey(stack.getItem()),
                content -> "content|" + TWRegistries.CONTENT.getKey(content)
        );
    }

    /** 从 "type|value" 或纯 "value" 字符串解析组件 */
    private static Either<ItemStack, Content> parseComponentFromString(String s) {
        Objects.requireNonNull(s, "Input string cannot be null");
        s = s.trim();

        if (!s.contains("|")) {
            return parseItemStack(s);
        }

        String[] parts = s.split("\\|", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format: '" + s + "'");
        }

        return switch (parts[0].trim().toLowerCase()) {
            case "item"    -> parseItemStack(parts[1].trim());
            case "content" -> parseContent(parts[1].trim());
            default        -> throw new IllegalArgumentException("Unknown type: '" + parts[0] + "'");
        };
    }

    private static Either<ItemStack, Content> parseItemStack(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) throw new IllegalArgumentException("Invalid item ID: " + itemId);
        Item item = BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        return Either.left(new ItemStack(item));
    }

    private static Either<ItemStack, Content> parseContent(String contentId) {
        ResourceLocation id = ResourceLocation.tryParse(contentId);
        if (id == null) throw new IllegalArgumentException("Invalid content ID: " + contentId);
        Content content = TWRegistries.CONTENT.get(id);
        if (content == null) throw new IllegalArgumentException("Content not found: " + contentId);
        return Either.right(content);
    }
}