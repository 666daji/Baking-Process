package org.bakingprocess.integration.dfood;

import org.dfood.block.FoodBlock;
import org.dfood.block.FoodBlocks;
import org.dfood.block.entity.SuspiciousStewBlockEntity;

public class FoodBlocksModifier {
    /** 能够让玩家像使用蛋糕那样使用炖菜。*/
    public static final FoodBlock.OnUseHook stewEatHook = (state, world, pos, player, hand, hit) -> {
        if (player.canEat(false)) {
            BlockEntity currentBlockEntity = world.getBlockEntity(pos);
            CompoundTag blockEntityData = null;
            // 如果是迷之炖菜方块实体，保存其数据
            if (currentBlockEntity instanceof SuspiciousStewBlockEntity) {
                blockEntityData = currentBlockEntity.saveWithoutMetadata();
            }
            BlockState blockState = CrippledStewBlock.getStewState(state);
            // 设置新的方块状态
            world.setBlockAndUpdate(pos, blockState);
            // 如果有方块实体数据需要传递，将其应用到新的方块实体
            if (blockEntityData != null) {
                BlockEntity newBlockEntity = world.getBlockEntity(pos);
                if (newBlockEntity instanceof SuspiciousStewBlockEntity) {
                    newBlockEntity.load(blockEntityData);
                }
            }
            blockState.getBlock().use(world, player, hand, hit);
            return InteractionResult.field_5812;
        }
        return InteractionResult.field_5811;
    };

    /** 可以使用空瓶子从水桶中盛出水 */
    protected static final FoodBlock.OnUseHook waterBucketHook = (state, world, pos, player, hand, hit) -> {
        ItemStack handStack = player.getItemInHand(hand);

        // 检查是否手持空瓶子
        if (handStack.getItem() == Items.field_8469) {
            if (world.isClientSide) {
                // 客户端播放声音
                world.playSound(player, pos, SoundEvents.field_14779, player.getSoundSource(), 1.0F, 1.0F);
                return InteractionResult.field_5812;
            }

            // 转换为残损水桶状态
            BlockState newState = CrippledBucketBlock.getWaterBucketState(state);
            world.setBlock(pos, newState, 3);

            // 播放声音
            world.playSound(player, pos, SoundEvents.field_14779, player.getSoundSource(), 1.0F, 1.0F);

            // 调用新方块的使用方法
            newState.getBlock().use(world, player, hand, hit);
            return InteractionResult.field_5812;
        }

        return InteractionResult.field_5811;
    };

    public static void FoodBlockAdd() {
        ((FoodBlock)FoodBlocks.RABBIT_STEW).setOnUseHook(stewEatHook);
        ((FoodBlock)FoodBlocks.MUSHROOM_STEW).setOnUseHook(stewEatHook);
        ((FoodBlock)FoodBlocks.BEETROOT_SOUP).setOnUseHook(stewEatHook);
        ((FoodBlock)FoodBlocks.SUSPICIOUS_STEW).setOnUseHook(stewEatHook);

        ((FoodBlock)FoodBlocks.WATER_BUCKET).setOnUseHook(waterBucketHook);
        ((FoodBlock)FoodBlocks.MILK_BUCKET).setOnUseHook(waterBucketHook);
    }
}