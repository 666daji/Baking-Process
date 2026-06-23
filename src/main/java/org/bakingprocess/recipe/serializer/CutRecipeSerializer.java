package org.bakingprocess.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
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

    @Override
    public CutRecipe fromJson(ResourceLocation id, JsonObject json) {
        // 读取输入物品
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));

        // 读取总切菜次数
        int totalCuts = GsonHelper.getAsInt(json, "totalCuts", 1);

        // 读取默认库存状态
        NonNullList<ItemStack> defaultState = readInventoryState(
                GsonHelper.getAsJsonObject(json, "defaultState", new JsonObject())
        );

        // 读取特定切菜次数的库存状态映射
        Map<Integer, NonNullList<ItemStack>> cutStateMap = new HashMap<>();
        if (json.has("cutStates")) {
            JsonObject cutStates = GsonHelper.getAsJsonObject(json, "cutStates");
            for (Map.Entry<String, JsonElement> entry : cutStates.entrySet()) {
                try {
                    int cutIndex = Integer.parseInt(entry.getKey());
                    JsonObject stateObject = entry.getValue().getAsJsonObject();
                    cutStateMap.put(cutIndex, readInventoryState(stateObject));
                } catch (NumberFormatException e) {
                    // 忽略无效的键（非数字键）
                }
            }
        }

        // 确保最后一刀的状态存在，如果没有则使用默认状态
        if (!cutStateMap.containsKey(totalCuts)) {
            cutStateMap.put(totalCuts, defaultState);
        }

        // 创建并返回配方对象
        return new CutRecipe(id, input, totalCuts, cutStateMap, defaultState);
    }

    @Override
    public CutRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        // 读取输入物品
        Ingredient input = Ingredient.fromNetwork(buf);

        // 读取总切菜次数
        int totalCuts = buf.readVarInt();

        // 读取默认库存状态
        int defaultSize = buf.readVarInt();
        NonNullList<ItemStack> defaultState = NonNullList.withSize(defaultSize, ItemStack.EMPTY);
        for (int i = 0; i < defaultSize; i++) {
            defaultState.set(i, buf.readItem());
        }

        //  读取特定切菜次数的库存状态映射
        int stateCount = buf.readVarInt();
        Map<Integer, NonNullList<ItemStack>> cutStateMap = new HashMap<>();
        for (int i = 0; i < stateCount; i++) {
            int cutIndex = buf.readVarInt();
            int stateSize = buf.readVarInt();
            NonNullList<ItemStack> state = NonNullList.withSize(stateSize, ItemStack.EMPTY);
            for (int j = 0; j < stateSize; j++) {
                state.set(j, buf.readItem());
            }
            cutStateMap.put(cutIndex, state);
        }

        // 确保最后一刀的状态存在
        if (!cutStateMap.containsKey(totalCuts)) {
            cutStateMap.put(totalCuts, defaultState);
        }

        // 创建并返回配方对象
        return new CutRecipe(id, input, totalCuts, cutStateMap, defaultState);
    }

    @Override
    public void write(FriendlyByteBuf buf, CutRecipe recipe) {
        // 写入输入物品
        recipe.getInput().toNetwork(buf);

        // 写入总切菜次数
        buf.writeVarInt(recipe.getTotalCuts());

        // 写入默认库存状态
        NonNullList<ItemStack> defaultState = recipe.getDefaultState();
        buf.writeVarInt(defaultState.size());
        for (ItemStack stack : defaultState) {
            buf.writeItem(stack);
        }

        // 写入特定切菜次数的库存状态映射
        Map<Integer, NonNullList<ItemStack>> cutStateMap = recipe.getCutStateMap();
        buf.writeVarInt(cutStateMap.size());
        for (Map.Entry<Integer, NonNullList<ItemStack>> entry : cutStateMap.entrySet()) {
            buf.writeVarInt(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (ItemStack stack : entry.getValue()) {
                buf.writeItem(stack);
            }
        }
    }

    /**
     * <h3>从JSON对象读取库存状态（5个槽位）</h3>
     *
     * <p>读取格式：<code>{"0": {"item": "...", "count": 1}, "2": {...}}</code></p>
     * <p>没有指定的槽位默认为空物品堆。</p>
     *
     * @param jsonObject JSON对象，包含槽位索引到物品的映射
     * @return DefaultedList<ItemStack> 包含5个槽位的库存状态
     */
    private NonNullList<ItemStack> readInventoryState(JsonObject jsonObject) {
        NonNullList<ItemStack> state = NonNullList.withSize(5, ItemStack.EMPTY);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            try {
                int slot = Integer.parseInt(entry.getKey());
                if (slot >= 0 && slot < 5) {
                    JsonObject itemObj = entry.getValue().getAsJsonObject();
                    state.set(slot, new ItemStack(
                            GsonHelper.getAsItem(itemObj, "item"),
                            GsonHelper.getAsInt(itemObj, "count", 1)
                    ));
                }
            } catch (NumberFormatException e) {
                // 忽略无效的槽位键（非数字键）
            }
        }

        return state;
    }
}