package org.bakingprocess.client.render.item.replacer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.item.FlourSackItem;
import org.twcore.client.api.render.ReplaceItemModel;

import java.util.Optional;

/**
 * 粉尘袋物品的模型替换器
 */
public class FlourSackModelReplacer {

    public static BakedModel ReplaceModel(ReplaceItemModel.ReplaceContext context) {
        ItemStack stack = context.stack();
        ModelManager manager = context.modelManager();

        // 检查是否为粉尘袋物品
        if (stack.getItem() instanceof FlourSackItem) {
            // 获取粉尘袋中的内容物
            Optional<ItemStack> content = FlourSackItem.getBundledStack(stack);

            if (content.isPresent()) {
                ItemStack flourStack = content.get();
                String flourName = getFlourModelName(flourStack);

                if (flourName != null) {
                    // 使用 MOD_ID 常量创建自定义模型标识符
                    ModelResourceLocation customModelId = new ModelResourceLocation(
                            new ResourceLocation(BakingProcess.MOD_ID, flourName),
                            "inventory"
                    );

                    // 获取模型
                    BakedModel customModel = manager.getModel(customModelId);

                    // 如果找到了自定义模型且不是缺失模型，则返回自定义模型
                    if (customModel != null && !customModel.equals(manager.getMissingModel())) {
                        return customModel;
                    }
                }
            }
        }

        // 返回原始模型
        return context.originalModel();
    }

    /**
     * 根据粉尘物品获取对应的粉尘袋模型名称。
     * @apiNote 粉尘物品ID + "_sack" = 粉尘袋模型名称
     */
    public static String getFlourModelName(ItemStack flourStack) {
        // 获取粉尘物品的注册表ID
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(flourStack.getItem());

        if (itemId.getNamespace().equals(BakingProcess.MOD_ID)) {
            // 直接在粉尘物品ID后添加"_sack"作为粉尘袋模型名称
            return itemId.getPath() + "_sack";
        }

        return null;
    }
}