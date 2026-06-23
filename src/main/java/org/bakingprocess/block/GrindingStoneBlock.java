package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.GrindingStoneBlockEntity;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModSounds;
import org.jetbrains.annotations.Nullable;

public class GrindingStoneBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape TOP = Block.box(0.0,0.0,0.0,16.0,5.0,16.0);
    protected static final VoxelShape BOTTOM = Block.box(1.0,5.0,1.0,15.0,14.0,15.0);

    public GrindingStoneBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(BOTTOM,TOP);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrindingStoneBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, (BlockEntityType<? extends GrindingStoneBlockEntity>) ModBlockEntityTypes.GRINDING_STONE.get(), GrindingStoneBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack handStack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GrindingStoneBlockEntity grindingEntity) {
            // 绌烘墜鏃舵��鏌?
            if (handStack.isEmpty()) {
                // 妫�鏌ユ槸鍚﹀彲浠ョ爺纾ㄥ綋鍓嶇墿鍝?
                if (!grindingEntity.canGrindCurrentInput()) {
                    // 濡傛灉涓嶈兘鐮旂（锛岃繑杩樼墿鍝佺粰鐜╁��
                    grindingEntity.returnInputToPlayer(player);
                    return InteractionResult.SUCCESS;
                }

                // 濡傛灉鍙�浠ョ爺纾�锛屽皾璇曟坊鍔犺兘閲?
                if (grindingEntity.tryAddEnergy(40)) {
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            }
            // 鎵嬫寔鐗╁搧鏃跺皾璇曟坊鍔犵墿鍝?
            else {
                GrindingStoneBlockEntity.AddInputResult result = grindingEntity.addInput(handStack, player);
                return switch (result) {
                    case INVALID, FULL, NOT_ENOUGH -> InteractionResult.PASS;
                    case SUCCESS -> InteractionResult.SUCCESS;
                };
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof GrindingStoneBlockEntity grindingEntity) {
                // 鎺夎惤鎵�鏈夌墿鍝?
                dropItems(grindingEntity, world, pos);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    /**
     * 鎺夎惤鏂瑰潡瀹炰綋涓�鐨勬墍鏈夌墿鍝?
     */
    private void dropItems(GrindingStoneBlockEntity blockEntity, Level world, BlockPos pos) {
        var items = blockEntity.getItemsToDrop();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                itemEntity.setDefaultPickUpDelay();
                world.addFreshEntity(itemEntity);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!world.getBlockState(pos.relative(direction)).isAir()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos
    ) {
        return !state.canSurvive(world, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof GrindingStoneBlockEntity grindingStoneBlockEntity && grindingStoneBlockEntity.canPlaySound()){
            world.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    ModSounds.GRINDING_STONE_GRINDING.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    true
            );

            for (int i = 0; i < world.random.nextInt(3) + 2; i++) {
                world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, grindingStoneBlockEntity.getItem(0)),
                        pos.getX() + world.random.nextFloat(), pos.getY() + 1, pos.getZ() + world.random.nextFloat(),
                        world.random.nextGaussian() * 0.05, 0.005, world.random.nextGaussian() * 0.05);
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection());
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}