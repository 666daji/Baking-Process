package org.bakingprocess.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
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
import org.bakingprocess.item.FlourItem;
import org.bakingprocess.recipe.DoughRecipe;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>面团配方序列化器</h1>
 *
 * <ul>
 *   <li>支持面粉类型（FlourType）到数量的映射</li>
 *   <li>支持液体内容物（Content）到数量的映射</li>
 *   <li>支持额外物品的数组格式和旧格式</li>
 *   <li>自动合并相同物品的数量</li>
 * </ul>
 *
 * <h2>JSON格式示例</h2>
 * <pre>{@code
 * {
 *   "type": "baking_process:dough_making",
 *   "output": {"item": "baking_process:dough", "count": 1},
 *   "flours": {
 *     "wheat": 2,
 *     "rice": 1
 *   },
 *   "liquids": {
 *     "baking_process:water": 1,
 *     "baking_process:milk": 2
 *   },
 *   "extra_items": {
 *     "items": [
 *       {"item": "minecraft:sugar", "count": 1},
 *       {"item": "minecraft:egg", "count": 2}
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <h2>字段说明</h2>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>必选</th><th>描述</th></tr>
 *   <tr><td>output</td><td>object</td><td>是</td><td>输出物品，包含item和count字段</td></tr>
 *   <tr><td>flours</td><td>object</td><td>是</td><td>面粉要求，键为面粉类型，值为数量</td></tr>
 *   <tr><td>liquids</td><td>object</td><td>是</td><td>液体要求，键为内容物标识符，值为数量（单位数）</td></tr>
 *   <tr><td>extra_items</td><td>object</td><td>否</td><td>额外物品要求，推荐使用items数组格式</td></tr>
 * </table>
 *
 * @see DoughRecipe
 * @see RecipeSerializer
 * @see FlourItem.FlourType
 */
public class DoughRecipeSerializer implements RecipeSerializer<DoughRecipe> {

    private static final Codec<FlourItem.FlourType> FLOUR_TYPE_CODEC =
            Codec.STRING.xmap(FlourItem.FlourType::fromId, FlourItem.FlourType::getSerializedName);

    private static final Codec<Content> CONTENT_CODEC =
            ResourceLocation.CODEC.xmap(
                    id -> {
                        Content content = TWRegistries.CONTENT.get().getValue(id);
                        if (content == null) throw new IllegalArgumentException("Unknown content: " + id);
                        return content;
                    },
                    content -> TWRegistries.CONTENT.get().getKey(content)
            );

    private static final Codec<Map<FlourItem.FlourType, Integer>> FLOUR_MAP_CODEC =
            Codec.unboundedMap(FLOUR_TYPE_CODEC, Codec.INT).xmap(HashMap::new, HashMap::new);

    private static final Codec<Map<Content, Integer>> LIQUID_MAP_CODEC =
            Codec.unboundedMap(CONTENT_CODEC, Codec.INT).xmap(HashMap::new, HashMap::new);

    public static final MapCodec<DoughRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.getResultItem(null)),

                    FLOUR_MAP_CODEC.fieldOf("flours").forGetter(DoughRecipe::getFlourRequirements),

                    LIQUID_MAP_CODEC.fieldOf("liquids").forGetter(DoughRecipe::getLiquidRequirements),

                    // 将 extra_items 作为原始 JSON 保留，通过 Dynamic 转换为 JsonElement
                    Codec.PASSTHROUGH
                            .xmap(dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
                                    json -> new Dynamic<>(JsonOps.INSTANCE, json))
                            .optionalFieldOf("extra_items", new JsonObject())
                            .forGetter(recipe -> new JsonObject()) // 序列化时返回空对象，无影响
            ).apply(instance, DoughRecipeSerializer::createRecipe)
    );

    private static DoughRecipe createRecipe(ItemStack output,
                                            Map<FlourItem.FlourType, Integer> flours,
                                            Map<Content, Integer> liquids,
                                            JsonElement extraItemsJson) {
        Map<Ingredient, Integer> extraRequirements = parseExtraItems(extraItemsJson);
        return new DoughRecipe(output, flours, liquids, extraRequirements);
    }

    private static Map<Ingredient, Integer> parseExtraItems(JsonElement jsonElement) {
        Map<Ingredient, Integer> result = new HashMap<>();
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return result;
        }
        JsonObject obj = jsonElement.getAsJsonObject();

        if (obj.has("items") && obj.get("items").isJsonArray()) {
            JsonArray items = obj.getAsJsonArray("items");
            Map<String, Integer> itemCounts = new HashMap<>();
            for (JsonElement itemElement : items) {
                JsonObject itemObj = itemElement.getAsJsonObject();
                String itemId = itemObj.get("item").getAsString();
                int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                itemCounts.merge(itemId, count, Integer::sum);
            }
            for (var entry : itemCounts.entrySet()) {
                ResourceLocation id = ResourceLocation.parse(entry.getKey());
                Item item = BuiltInRegistries.ITEM.get(id);
                result.put(Ingredient.of(item), entry.getValue());
            }
        }
        else {
            for (var entry : obj.entrySet()) {
                JsonObject itemObj = entry.getValue().getAsJsonObject();
                Ingredient ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, itemObj).getOrThrow();
                int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                result.merge(ingredient, count, Integer::sum);
            }
        }
        return result;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, DoughRecipe> PACKET_CODEC =
            StreamCodec.ofMember(DoughRecipeSerializer::encode, DoughRecipeSerializer::decode);

    private static void encode(DoughRecipe recipe, RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem(null));
        Map<FlourItem.FlourType, Integer> flours = recipe.getFlourRequirements();
        buf.writeVarInt(flours.size());
        for (var entry : flours.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
        Map<Content, Integer> liquids = recipe.getLiquidRequirements();
        buf.writeVarInt(liquids.size());
        for (var entry : liquids.entrySet()) {
            buf.writeResourceLocation(TWRegistries.CONTENT.get().getKey(entry.getKey()));
            buf.writeVarInt(entry.getValue());
        }
        Map<Ingredient, Integer> extras = recipe.getExtraRequirements();
        buf.writeVarInt(extras.size());
        for (var entry : extras.entrySet()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    private static DoughRecipe decode(RegistryFriendlyByteBuf buf) {
        ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
        int flourCount = buf.readVarInt();
        Map<FlourItem.FlourType, Integer> flours = new HashMap<>(flourCount);
        for (int i = 0; i < flourCount; i++) {
            FlourItem.FlourType type = buf.readEnum(FlourItem.FlourType.class);
            int count = buf.readVarInt();
            flours.put(type, count);
        }
        int liquidCount = buf.readVarInt();
        Map<Content, Integer> liquids = new HashMap<>(liquidCount);
        for (int i = 0; i < liquidCount; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int count = buf.readVarInt();
            Content content = TWRegistries.CONTENT.get().getValue(id);
            if (content != null) liquids.put(content, count);
        }
        int extraCount = buf.readVarInt();
        Map<Ingredient, Integer> extras = new HashMap<>(extraCount);
        for (int i = 0; i < extraCount; i++) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            int count = buf.readVarInt();
            extras.put(ingredient, count);
        }
        return new DoughRecipe(output, flours, liquids, extras);
    }

    @Override
    public MapCodec<DoughRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DoughRecipe> streamCodec() {
        return PACKET_CODEC;
    }
}