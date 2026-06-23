package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.CuttingBoardBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.block.UpPlaceBlock;
import org.twcore.api.block.UpPlaceBlockEntity;

public class CuttingBoardBlock extends UpPlaceBlock {
    protected static final VoxelShape SHAPE_X = Block.box(0.0, 0.0, 0.5, 16.0, 1.5, 15.5);
    protected static final VoxelShape SHAPE_Z = Block.box(0.5, 0.0, 0.0, 15.5, 1.5, 16.0);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CuttingBoardBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public boolean canFetched(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        // 如果切菜流程在进行中，不允许取出物品
        if (blockEntity instanceof CuttingBoardBlockEntity cuttingBoard) {
            if (cuttingBoard.getCuttingProcess().isActive()) {
                return false;
            }
        }

        // 如果容器不为空，允许取出
        return !blockEntity.isEmpty();
    }

    @Override
    public boolean canPlace(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        // 如果切菜流程在进行中，不允许放置新物品
        if (blockEntity instanceof CuttingBoardBlockEntity cuttingBoard) {
            if (cuttingBoard.getCuttingProcess().isActive()) {
                return false;
            }
        }

        return blockEntity.isValidItem(handStack);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CuttingBoardBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack handStack = player.getItemInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof CuttingBoardBlockEntity cuttingBoard) {
            // 尝试切割操作
            if (cuttingBoard.tryCutItem(player, handStack, hand, hit).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            // 如果切割失败或条件不满足，执行父类逻辑（取出和放置）
            return super.use(state, world, pos, player, hand, hit);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}