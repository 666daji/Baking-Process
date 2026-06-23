package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bakingprocess.container.BreadBoatContainer;
import org.dfood.block.SimpleFoodBlock;
import org.twcore.api.content.ContainerUtil;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;

public class EmptyBreadBoatBlock extends SimpleFoodBlock {
    /** 对应的装了内容物的方块 */
    protected final BreadBoatBlock targetBlock;

    public EmptyBreadBoatBlock(Properties settings, BreadBoatBlock targetBlock) {
        super(settings, true, targetBlock.simpleShape, targetBlock.useItemTranslationKey, null);
        this.targetBlock = targetBlock;
    }

    /**
     * 将空的容器替换为对应的盛满的汤的容器
     * @param originalState 原本的容器状态
     * @param soupType 盛入的汤
     * @return 对应的盛满汤的容器状态，如果原容器状态的基础方块不是当前实例，则直接返回原方块状态
     */
    public static BlockState asTargetState(BlockState originalState, BreadBoatContainer.BreadBoatSoupType soupType) {
        if (originalState.getBlock() instanceof EmptyBreadBoatBlock edibleContainerBlock) {
            return edibleContainerBlock.targetBlock.defaultBlockState()
                    .setValue(FACING, originalState.getValue(FACING))
                    .setValue(BreadBoatBlock.SOUP_TYPE, soupType)
                    .setValue(edibleContainerBlock.targetBlock.BITES, 0);
        }

        return originalState;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack handStack = player.getItemInHand(hand);
        Content content = ContainerUtil.extractContent(handStack);
        BreadBoatContainer.BreadBoatSoupType soupType = BreadBoatContainer.BreadBoatSoupType.fromContent(content);

        if (soupType != null) {
            ContainerType containerType = ContainerUtil.getContainerType(handStack);
            if (containerType != null) {
                // 尝试清空容器
                handStack.shrink(1);
                if (handStack.isEmpty()) {
                    player.setItemInHand(hand, containerType.remainder());
                } else {
                    player.addItem(containerType.remainder());
                }

                // 播放使用声音
                world.playSound(player, pos, containerType.getUseSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            // 将汤盛进来
            if (world.setBlockAndUpdate(pos, asTargetState(state, soupType))) {
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }
}
