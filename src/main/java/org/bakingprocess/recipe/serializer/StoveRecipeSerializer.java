package org.bakingprocess.recipe.serializer;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.StoveRecipe;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

public class StoveRecipeSerializer implements RecipeSerializer<StoveRecipe> {

    @Override
    public StoveRecipe fromJson(ResourceLocation id, JsonObject json) {
        // 读取输入物品（可以是普通物品或内容物）
        String inputString = GsonHelper.getAsString(json, "ingredient");
        Either<ItemStack, Content> input = readStackFromString(inputString);

        // 读取结果物品（可以是普通物品或内容物）
        String resultString = GsonHelper.getAsString(json, "result");
        Either<ItemStack, Content> result = readStackFromString(resultString);

        // 读取烘烤时间、最大输入数量和模具信息
        int inputCount = GsonHelper.getAsInt(json, "MaxInputCount", 1);
        int stoveTime = GsonHelper.getAsInt(json, "stoveTime", 200);

        return new StoveRecipe(id, input, result, inputCount, stoveTime);
    }

    @Override
    public StoveRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        // 读取输入物品
        String inputString = buf.readUtf();
        Either<ItemStack, Content> input = readStackFromString(inputString);

        // 读取结果物品
        String resultString = buf.readUtf();
        Either<ItemStack, Content> result = readStackFromString(resultString);

        // 读取额外数据
        int inputCount = buf.readInt();
        int stoveTime = buf.readVarInt();

        return new StoveRecipe(id, input, result, inputCount, stoveTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, StoveRecipe recipe) {
        // 将组件转换为 "type|value" 字符串后直接写入
        buf.writeUtf(componentToString(recipe.getInput()));
        buf.writeUtf(componentToString(recipe.getOutput()));

        buf.writeInt(recipe.getMaxInputCount());
        buf.writeVarInt(recipe.getBakingTime());
    }

    /**
     * 将 Either<ItemStack, Content> 转换为与配方 JSON 格式一致的字符串。
     */
    private static String componentToString(Either<ItemStack, Content> component) {
        return component.map(
                stack -> "item|" + BuiltInRegistries.ITEM.getKey(stack.getItem()),
                content -> "content|" + TWRegistries.CONTENT.get().getKey(content)
        );
    }

    /**
     * 从字符串中读取组件。
     * <p>使用'|'分割，格式为"类别|值"：</p>
     * <ul>
     *   <li>类别为"item"时：解析为物品堆栈</li>
     *   <li>类别为"content"时：解析为内容物</li>
     * </ul>
     * <p>如果字符串中不包含'|'，则默认按普通物品处理。</p>
     *
     * @param idString 要解析的字符串
     * @return 解析后的物品堆栈
     * @throws IllegalArgumentException 如果无法解析出有效物品或内容物
     * @throws NullPointerException 如果输入字符串为null
     */
    private static Either<ItemStack, Content> readStackFromString(String idString) {
        Objects.requireNonNull(idString, "Input string cannot be null");

        // 去除前后空格
        idString = idString.trim();

        // 如果不包含'|'，按普通物品处理
        if (!idString.contains("|")) {
            return parseItemStack(idString);
        }

        // 分割字符串
        String[] args = idString.split("\\|", 2); // 限制分割为2部分
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid string format: '" + idString + "'. Expected format: 'type|value'");
        }

        String type = args[0].trim().toLowerCase();
        String value = args[1].trim();

        return switch (type) {
            case "item" -> parseItemStack(value);
            case "content" -> parseContentStack(value);
            default -> throw new IllegalArgumentException("Unknown type: '" + type + "'. Expected 'item' or 'content'");
        };
    }

    /**
     * 解析物品堆栈。
     * @param itemId 物品ID字符串
     * @return 物品堆栈
     * @throws IllegalArgumentException 如果物品不存在
     */
    private static Either<ItemStack, Content> parseItemStack(String itemId) {
        ResourceLocation identifier = ResourceLocation.tryParse(itemId);
        if (identifier == null) {
            throw new IllegalArgumentException("Invalid item ID format: '" + itemId + "'");
        }

        Item item = BuiltInRegistries.ITEM.getOptional(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        return Either.left(new ItemStack(item));
    }

    /**
     * 解析内容物。
     * @param contentId 内容物ID字符串
     * @return 内容物占位堆栈
     * @throws IllegalArgumentException 如果内容物不存在
     */
    private static Either<ItemStack, Content> parseContentStack(String contentId) {
        ResourceLocation identifier = ResourceLocation.tryParse(contentId);
        if (identifier == null) {
            throw new IllegalArgumentException("Invalid content ID format: '" + contentId + "'");
        }

        Content content = TWRegistries.CONTENT.get().getValue(identifier);
        if (content == null) {
            throw new IllegalArgumentException("Content not found: " + contentId);
        }

        return Either.right(content);
    }
}