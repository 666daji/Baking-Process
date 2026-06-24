package org.bakingprocess.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.bakingprocess.recipe.PlatingRecipe;
import org.twcore.TWCore;
import org.twcore.api.process.PlayerAction;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public PlatingRecipe fromJson(ResourceLocation id, JsonObject json) {
        // 1. 读取容器物品
        String containerId = GsonHelper.getAsString(json, "container");
        Item container = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(containerId))
                .orElseThrow(() -> new JsonParseException("Unknown container item: " + containerId));

        // 2. 读取操作列表
        if (!json.has("actions")) {
            throw new JsonParseException("The plating recipe must contain an 'actions' field");
        }

        List<PlayerAction> actions = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "actions")) {
            String actionStr = element.getAsString();
            PlayerAction action = PlayerAction.fromString(actionStr);
            actions.add(action);
        }

        // 3. 读取输出结果
        String resultId = GsonHelper.getAsString(json, "result");
        ResourceLocation result = ResourceLocation.tryParse(resultId);
        if (result == null) {
            throw new JsonParseException("Invalid result ID: " + resultId);
        }

        Content output = TWRegistries.CONTENT.get().getValue(result);
        if (output == null) {
            throw new JsonParseException("No content found: " + resultId);
        }

        // 4. 创建并返回配方对象
        return new PlatingRecipe(id, container, actions, output);
    }

    @Override
    public PlatingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        // 1. 读取容器物品
        ResourceLocation containerId = buf.readResourceLocation();
        Item container = BuiltInRegistries.ITEM.getOptional(containerId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container item: " + containerId));

        // 2. 读取操作列表
        int actionCount = buf.readVarInt();
        List<PlayerAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            String actionStr = buf.readUtf();
            PlayerAction action = PlayerAction.fromString(actionStr);
            actions.add(action);
        }

        // 3. 读取输出结果
        Content output = TWRegistries.CONTENT.get().getValue(buf.readResourceLocation());
        if (output == null) {
            throw new IllegalArgumentException("No output found");
        }

        // 4. 创建并返回配方对象
        return new PlatingRecipe(id, container, actions, output);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, PlatingRecipe recipe) {
        // 1. 写入容器物品ID
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getContainer()));

        // 2. 写入操作列表
        List<PlayerAction> actions = recipe.getActions();
        buf.writeVarInt(actions.size());
        for (PlayerAction action : actions) {
            buf.writeUtf(action.toString());
        }

        // 3. 写入输出内容ID
        buf.writeResourceLocation(Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(recipe.getDishes())));
    }
}