package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.MoldBlockEntity;
import org.bakingprocess.content.ShapedDoughContent;
import org.bakingprocess.registry.ModBlocks;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

import java.util.ArrayList;
import java.util.List;

public class MoldBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape CAKE_EMBRYO_MOLD_SHAPE = Block.box(0, 0, 0, 16, 9, 16);
    public static final VoxelShape TOAST_EMBRYO_MOLD_SHAPE_X = Block.box(3, 0, 0, 13, 8, 16);
    public static final VoxelShape TOAST_EMBRYO_MOLD_SHAPE_Z = Block.box(0, 0, 3, 16, 8, 13);

    public MoldBlock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof MoldBlockEntity moldBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack contentStack = moldBlockEntity.getAndClearResultStack();
        ItemStack heldStack = player.getItemInHand(hand);

        if (!contentStack.isEmpty()) {
            player.addItem(contentStack);
            return InteractionResult.SUCCESS;
        }

        if (moldBlockEntity.addDough(heldStack)) {
            if (!player.isCreative()) {
                heldStack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getBlock() == ModBlocks.TOAST_EMBRYO_MOLD.get()) {
            return state.getValue(FACING).getAxis() ==
                    Direction.Axis.Z ? TOAST_EMBRYO_MOLD_SHAPE_Z : TOAST_EMBRYO_MOLD_SHAPE_X;
        }
        return CAKE_EMBRYO_MOLD_SHAPE;
    }


    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        Content content = ContainerUtil.extractContent(itemStack);
        BlockEntity entity = world.getBlockEntity(pos);

        if (content instanceof ShapedDoughContent shapedDough && entity instanceof MoldBlockEntity moldBlockEntity) {
            moldBlockEntity.setShapedDough(shapedDough);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> result = super.getDrops(state, builder);
        BlockEntity entity = builder.getParameter(LootContextParams.BLOCK_ENTITY);

        if (entity instanceof MoldBlockEntity moldBlockEntity) {
            List<ItemStack> newList = new ArrayList<>();
            ShapedDoughContent content = moldBlockEntity.getShapedDough();
            if (content != null) {
                result.forEach(stack -> ContainerUtil.analyze(stack)
                        .map(containerStack -> newList.add(containerStack.replaceContent(content)))
                        .orElseGet(() -> newList.add(stack)));
            }

            return newList;
        }

        return result;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoldBlockEntity(pos, state);
    }
}
