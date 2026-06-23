package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bakingprocess.block.entity.FlourSackBlockEntity;
import org.bakingprocess.item.FlourSackItem;
import org.dfood.block.ComplexFoodBlock;
import org.dfood.block.FoodBlockBuilder;
import org.jetbrains.annotations.Nullable;

public class FlourSackBlock extends ComplexFoodBlock implements EntityBlock {
    public static final IntegerProperty SHELF_INDEX = IntegerProperty.create("shelf_index", 0, 1);

    protected FlourSackBlock(Properties settings, int maxFood) {
        super(settings, maxFood, true, null, false, null);

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(NUMBER_OF_FOOD, 1)
                .setValue(SHELF_INDEX, 0));
    }

    public static class Builder extends FoodBlockBuilder<FlourSackBlock, Builder> {
        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        @Override
        protected FlourSackBlock createBlock() {
            return new FlourSackBlock(this.settings, this.maxFood);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlourSackBlockEntity(pos, state);
    }

    @Override
    public boolean isSame(ItemStack stack, BlockState state, @Nullable BlockEntity blockEntity) {
        CompoundTag nbt = stack.getTag();

        if (nbt == null || !nbt.contains(FlourSackItem.STORED_ITEM_KEY)) {
            return false;
        }

        return super.isSame(stack, state, blockEntity);
    }

    /**
     * 获取指定位置的粉尘袋物品堆栈
     */
    public ItemStack getSackStack(Level world, BlockPos pos, int index) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FlourSackBlockEntity flourSackEntity) {
            return flourSackEntity.getSackStack(index);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHELF_INDEX);
    }
}