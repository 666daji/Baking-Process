package org.bakingprocess.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.PlatingRecipe;
import org.twcore.api.process.PlayerAction;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.List;

/**
 * 摆盘配方序列化器，用于JSON格式的摆盘配方解析。
 *
 * <h2>JSON格式示例</h2>
 * <pre>{@code
 * {
 *   "type": "baking_process:plating",
 *   "container": "baking_process:iron_plate",
 *   "actions": [
 *     "add_item|minecraft:beef",
 *     "add_item|minecraft:sweet_berries",
 *   ],
 *   "result": "baking_process:beef_berries_soup"
 * }
 * }</pre>
 *
 * <h2>字段说明</h2>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>必需</th><th>描述</th></tr>
 *   <tr><td>type</td><td>string</td><td>是</td><td>配方类型，必须为"baking_process:plating"</td></tr>
 *   <tr><td>container</td><td>string</td><td>是</td><td>容器物品ID</td></tr>
 *   <tr><td>actions</td><td>string[]</td><td>是</td><td>操作序列，每个字符串格式为"操作类型|参数1|参数2..."</td></tr>
 *   <tr><td>result</td><td>string</td><td>是</td><td>输出菜肴的内容ID</td></tr>
 * </table>
 *
 * @see PlatingRecipe
 * @see RecipeSerializer
 */
public class PlatingRecipeSerializer implements RecipeSerializer<PlatingRecipe> {

    /** PlayerAction 列表的 Codec：字符串列表转换 */
    private static final Codec<List<PlayerAction>> ACTIONS_CODEC =
            Codec.STRING.listOf().xmap(
                    strings -> strings.stream().map(PlayerAction::fromString).toList(),
                    actions -> actions.stream().map(PlayerAction::toString).toList()
            );

    /** Container (Item) 的 Codec：从标识符字符串加载 */
    private static final Codec<Item> CONTAINER_CODEC =
            ResourceLocation.CODEC.xmap(
                    id -> BuiltInRegistries.ITEM.getOptional(id)
                            .orElseThrow(() -> new IllegalArgumentException("Unknown container item: " + id)),
                    BuiltInRegistries.ITEM::getKey
            );

    /** Output (Content/DishesContent) 的 Codec */
    private static final Codec<Content> OUTPUT_CODEC =
            ResourceLocation.CODEC.xmap(
                    id -> {
                        Content content = TWRegistries.CONTENT.get(id);
                        if (content == null) {
                            throw new IllegalArgumentException("Unknown content: " + id);
                        }
                        return content;
                    },
                    content -> TWRegistries.CONTENT.getKey(content)
            );

    public static final MapCodec<PlatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CONTAINER_CODEC.fieldOf("container").forGetter(PlatingRecipe::getContainer),
                    ACTIONS_CODEC.fieldOf("actions").forGetter(PlatingRecipe::getActions),
                    OUTPUT_CODEC.fieldOf("result").forGetter(PlatingRecipe::getDishes)
            ).apply(instance, PlatingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlatingRecipe> PACKET_CODEC =
            StreamCodec.ofMember(PlatingRecipeSerializer::encode, PlatingRecipeSerializer::decode);

    private static void encode(PlatingRecipe recipe, RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getContainer()));
        List<PlayerAction> actions = recipe.getActions();
        buf.writeVarInt(actions.size());
        for (PlayerAction action : actions) {
            buf.writeUtf(action.toString());
        }
        buf.writeResourceLocation(TWRegistries.CONTENT.getKey(recipe.getDishes()));
    }

    private static PlatingRecipe decode(RegistryFriendlyByteBuf buf) {
        ResourceLocation containerId = buf.readResourceLocation();
        Item container = BuiltInRegistries.ITEM.getOptional(containerId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container item: " + containerId));
        int actionCount = buf.readVarInt();
        List<PlayerAction> actions = new java.util.ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            actions.add(PlayerAction.fromString(buf.readUtf()));
        }
        Content output = TWRegistries.CONTENT.get(buf.readResourceLocation());
        if (output == null) {
            throw new IllegalArgumentException("Unknown output content");
        }
        return new PlatingRecipe(container, actions, output);
    }

    // ---------- 接口实现 ----------

    @Override
    public MapCodec<PlatingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PlatingRecipe> streamCodec() {
        return PACKET_CODEC;
    }
}