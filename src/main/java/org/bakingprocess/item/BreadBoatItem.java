package org.bakingprocess.item;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.EmptyBreadBoatBlock;
import org.bakingprocess.container.BreadBoatContainer;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;
import org.twcore.content.FoodContent;

/**
 * 表示面包船容器的物品。
 * <p>构造函数中的物品设置中必须拥有食物组件。</p>
 */
public class BreadBoatItem extends BlockItem {

    public BreadBoatItem(Block block, Properties settings) {
        super(block, settings);
    }

    /**
     * 获取一个所有汤类型的对应物品堆栈列表
     * @param item 基础物品
     * @return 所有汤类型的物品堆栈列表
     */
    public static NonNullList<ItemStack> getAll(BreadBoatItem item) {
        NonNullList<ItemStack> result = NonNullList.create();

        for (BreadBoatContainer.BreadBoatSoupType soupType : BreadBoatContainer.BreadBoatSoupType.values()) {
            ItemStack stack = new ItemStack(item);
            ItemStack stack1 = ContainerUtil.analyze(stack).orElseThrow().replaceContent(soupType.getContent());
            result.add(stack1);
        }

        return result;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        Content content = ContainerUtil.extractContent(context.getItemInHand());
        BlockState originalState = super.getPlacementState(context);

        if (content != null) {
            BreadBoatContainer.BreadBoatSoupType soupType =
                    BreadBoatContainer.BreadBoatSoupType.fromContent(content);

            // 尝试返回盛有对应汤的方块状态
            if (originalState != null && soupType != null && originalState.getBlock()
                    instanceof EmptyBreadBoatBlock) {
                return EmptyBreadBoatBlock.asTargetState(originalState, soupType);
            }
        }

        return originalState;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (ContainerUtil.extractContent(stack) != null) {
            return super.getDescriptionId() + ".soup";
        }

        return super.getDescriptionId(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        Content content = ContainerUtil.extractContent(stack);

        if (content instanceof FoodContent soup) {
            Component soupName = soup.getDisplayName();
            return Component.translatable(this.getDescriptionId(stack), soupName);
        }

        return super.getName(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        Content soupType = ContainerUtil.extractContent(stack);

        // 如果容器中有汤并且使用物品的为玩家则喝汤
        if (soupType instanceof FoodContent soupContent && user instanceof Player player) {
            FoodProperties soup = soupContent.getFoodComponent();
            player.getFoodData().eat(soup.getNutrition(), soup.getSaturationModifier());
        }

        return super.finishUsingItem(stack, world, user);
    }
}
