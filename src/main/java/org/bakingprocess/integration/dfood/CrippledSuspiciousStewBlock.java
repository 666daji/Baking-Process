package org.bakingprocess.integration.dfood;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.dfood.block.entity.SuspiciousStewBlockEntity;
import org.jetbrains.annotations.Nullable;

public class CrippledSuspiciousStewBlock extends CrippledStewBlock implements EntityBlock {
    public CrippledSuspiciousStewBlock(Properties settings, int maxUse, FoodProperties foodComponent, Block baseBlock) {
        super(settings, maxUse, foodComponent, baseBlock);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuspiciousStewBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        // 将物品NBT中的效果数据传递给方块实体
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
            CompoundTag stackNbt = itemStack.getTag();
            if (stackNbt != null) {
                suspiciousStewBlockEntity.readCustomDataFromItem(stackNbt);
            }
        }
    }

    @Override
    protected InteractionResult tryUse(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        // 应用迷之炖菜的效果
        if (world instanceof Level) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
                // 应用所有存储的效果
                suspiciousStewBlockEntity.getEffectMap().forEach((effectId, duration) -> {
                    MobEffect effect = MobEffect.byId(effectId);
                    if (effect != null) {
                        player.addEffect(new MobEffectInstance(effect, (duration / 4) + 1));
                    }
                });
            }
        }
        // 调用父类的食用逻辑
        return super.tryUse(world, pos, state, player);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        // 在破坏方块时也应用效果
        if (!world.isClientSide && state.getValue(NUMBER_OF_USE) > 0) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
                // 应用所有存储的效果
                suspiciousStewBlockEntity.getEffectMap().forEach((effectId, duration) -> {
                    MobEffect effect = MobEffect.byId(effectId);
                    if (effect != null) {
                        int Duration = duration / 4;
                        int numberOfEat = state.getValue(NUMBER_OF_USE);
                        player.addEffect(new MobEffectInstance(effect, Duration * numberOfEat));
                    }
                });
            }
        }

        super.playerWillDestroy(world, pos, state, player);
    }
}