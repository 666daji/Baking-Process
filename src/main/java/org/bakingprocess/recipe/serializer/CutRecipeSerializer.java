package org.bakingprocess.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.CutRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>切割配方序列化器</h1>
 *
 * <ul>
 *   <li>支持两种输出格式（对象格式和简写格式）</li>
 *   <li>管理切割过程中的中间状态</li>
 *   <li>处理5个槽位的库存状态</li>
 * </ul>
 *
 * <h2>JSON格式示例</h2>
 * <pre>{@code
 * {
 *   "type": "baking_process:cutting",
 *   "input": {"item": "minecraft:carrot"},
 *   "totalCuts": 5,
 *   "defaultState": {
 *     "0": {"item": "baking_process:carrot_chunk", "count": 1},
 *     "2": {"item": "baking_process:carrot_slice", "count": 2}
 *   },
 *   "cutStates": {
 *     "2": {
 *       "0": {"item": "baking_process:carrot_chunk", "count": 2},
 *       "3": {"item": "baking_process:carrot_dice", "count": 1}
 *     },
 *     "5": {
 *       "0": {"item": "baking_process:chopped_carrot", "count": 3},
 *       "1": {"item": "baking_process:carrot_dice", "count": 2}
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h2>字段说明</h2>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>必选</th><th>描述</th></tr>
 *   <tr><td>input</td><td>object</td><td>是</td><td>输入物品，使用Minecraft标准Ingredient格式</td></tr>
 *   <tr><td>totalCuts</td><td>integer</td><td>否</td><td>总切割次数，默认1</td></tr>
 *   <tr><td>defaultState</td><td>object</td><td>否</td><td>默认库存状态（5个槽位）</td></tr>
 *   <tr><td>cutStates</td><td>object</td><td>否</td><td>特定切割次数的库存状态映射</td></tr>
 * </table>
 *
 * @see CutRecipe
 * @see RecipeSerializer
 */
public class CutRecipeSerializer implements RecipeSerializer<CutRecipe> {

    private static final Codec<NonNullList<ItemStack>> STATE_CODEC =
            Codec.unboundedMap(
                            Codec.STRING.xmap(Integer::parseInt, String::valueOf),
                            ItemStack.CODEC
                    )
                    .xmap(
                            map -> {
                                NonNullList<ItemStack> list = NonNullList.withSize(5, ItemStack.EMPTY);
                                map.forEach((slot, stack) -> {
                                    if (slot >= 0 && slot < 5) {
                                        list.set(slot, stack);
                                    }
                                });
                                return list;
                            },
                            list -> {
                                Map<Integer, ItemStack> map = new HashMap<>();
                                for (int i = 0; i < list.size(); i++) {
                                    ItemStack stack = list.get(i);
                                    if (!stack.isEmpty()) {
                                        map.put(i, stack);
                                    }
                                }
                                return map;
                            }
                    );

    private static final Codec<Map<Integer, NonNullList<ItemStack>>> CUT_STATES_CODEC =
            Codec.unboundedMap(
                            Codec.STRING.xmap(Integer::parseInt, String::valueOf),
                            STATE_CODEC
                    )
                    .xmap(HashMap::new, HashMap::new);

    public static final MapCodec<CutRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("input")
                            .forGetter(CutRecipe::getInput),

                    Codec.INT.optionalFieldOf("totalCuts", 1)
                            .forGetter(CutRecipe::getTotalCuts),

                    STATE_CODEC.optionalFieldOf("defaultState", NonNullList.withSize(5, ItemStack.EMPTY))
                            .forGetter(CutRecipe::getDefaultState),

                    CUT_STATES_CODEC.optionalFieldOf("cutStates", Map.of())
                            .forGetter(CutRecipe::getCutStateMap),

                    ItemStack.CODEC.optionalFieldOf("output", ItemStack.EMPTY)
                            .forGetter(r -> ItemStack.EMPTY)
            ).apply(instance, (input, totalCuts, defaultState, cutStateMap, ignoredOutput) -> {
                Map<Integer, NonNullList<ItemStack>> mutableMap = new HashMap<>(cutStateMap);
                if (!mutableMap.containsKey(totalCuts)) {
                    mutableMap.put(totalCuts, defaultState);
                }
                return new CutRecipe(input, totalCuts, mutableMap, defaultState);
            })
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CutRecipe> PACKET_CODEC =
            StreamCodec.ofMember(CutRecipeSerializer::encode, CutRecipeSerializer::decode);

    private static void encode(CutRecipe recipe, RegistryFriendlyByteBuf buf) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
        buf.writeVarInt(recipe.getTotalCuts());

        NonNullList<ItemStack> defaultState = recipe.getDefaultState();
        buf.writeVarInt(defaultState.size());
        for (ItemStack stack : defaultState) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }

        Map<Integer, NonNullList<ItemStack>> map = recipe.getCutStateMap();
        buf.writeVarInt(map.size());
        for (var entry : map.entrySet()) {
            buf.writeVarInt(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (ItemStack stack : entry.getValue()) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    }

    private static CutRecipe decode(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        int totalCuts = buf.readVarInt();

        // 默认状态
        int defaultSize = buf.readVarInt();
        NonNullList<ItemStack> defaultState = NonNullList.withSize(defaultSize, ItemStack.EMPTY);
        for (int i = 0; i < defaultSize; i++) {
            defaultState.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }

        // 切割状态表
        int stateCount = buf.readVarInt();
        Map<Integer, NonNullList<ItemStack>> cutStateMap = new HashMap<>();
        for (int i = 0; i < stateCount; i++) {
            int cutIndex = buf.readVarInt();
            int stateSize = buf.readVarInt();
            NonNullList<ItemStack> state = NonNullList.withSize(stateSize, ItemStack.EMPTY);
            for (int j = 0; j < stateSize; j++) {
                state.set(j, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            cutStateMap.put(cutIndex, state);
        }

        if (!cutStateMap.containsKey(totalCuts)) {
            cutStateMap.put(totalCuts, defaultState);
        }

        return new CutRecipe(input, totalCuts, cutStateMap, defaultState);
    }

    @Override
    public MapCodec<CutRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CutRecipe> streamCodec() {
        return PACKET_CODEC;
    }
}