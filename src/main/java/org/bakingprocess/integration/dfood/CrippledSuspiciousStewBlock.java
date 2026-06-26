package org.bakingprocess.integration.dfood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
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

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
            SuspiciousStewEffects stewEffects = itemStack.getOrDefault(
                    DataComponents.SUSPICIOUS_STEW_EFFECTS,
                    SuspiciousStewEffects.EMPTY
            );
            suspiciousStewBlockEntity.readCustomDataFromItem(stewEffects);
        }
    }

    @Override
    protected InteractionResult tryUse(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        if (world instanceof Level) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
                suspiciousStewBlockEntity.createStewEffectsComponent().effects().forEach((entry) ->
                        player.addEffect(new MobEffectInstance(entry.effect(), (entry.duration() / 4) + 1)));
            }
        }
        return super.tryUse(world, pos, state, player);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(world, pos, state, player);
        if (!world.isClientSide && state.getValue(NUMBER_OF_USE) > 0) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SuspiciousStewBlockEntity suspiciousStewBlockEntity) {
                suspiciousStewBlockEntity.createStewEffectsComponent().effects().forEach((entry) -> {
                    int Duration = entry.duration() / 4;
                    int numberOfEat = state.getValue(NUMBER_OF_USE);
                    player.addEffect(new MobEffectInstance(entry.effect(), Duration * numberOfEat));
                });
            }
        }
        return result;
    }
}