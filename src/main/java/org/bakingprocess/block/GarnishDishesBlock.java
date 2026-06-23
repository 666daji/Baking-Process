package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.DishesBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.block.UpPlaceBlock;
import org.twcore.api.block.UpPlaceBlockEntity;

import java.util.Optional;

public class GarnishDishesBlock extends UpPlaceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DishesType> TYPE = EnumProperty.create("type", DishesType.class);
    private static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);

    private static final DoubleBlockCombiner.Combiner<DishesBlockEntity, Optional<Container>> INVENTORY_RETRIEVER =
            new DoubleBlockCombiner.Combiner<>() {
                @Override
                public Optional<Container> getFromBoth(DishesBlockEntity first, DishesBlockEntity second) {
                    return Optional.of(new CompoundContainer(first, second));
                }

                @Override
                public Optional<Container> getFrom(DishesBlockEntity single) {
                    return Optional.of(single);
                }

                @Override
                public Optional<Container> acceptNone() {
                    return Optional.empty();
                }
            };

    public GarnishDishesBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, DishesType.SINGLE));
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    public boolean canFetched(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        Item contentItem = blockEntity.getContentItem();

        // 如果玩家手持物品为空或者与容器中物品不同，允许取出
        return handStack.isEmpty() ||
                (contentItem != null && handStack.getItem() != contentItem) || blockEntity.isFull();
    }

    @Override
    public boolean canPlace(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        return blockEntity.isValidItem(handStack);
    }

    @Override
    public BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor world, BlockPos pos, BlockPos neighborPos
    ) {
        // 首先检查方块是否可以放置在当前位置，如果不能则返回空气
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (neighborState.is(this) && direction.getAxis().isHorizontal()) {
            DishesType neighborType = neighborState.getValue(TYPE);
            if (state.getValue(TYPE) == DishesType.SINGLE
                    && neighborType != DishesType.SINGLE
                    && state.getValue(FACING) == neighborState.getValue(FACING)
                    && getFacing(neighborState) == direction.getOpposite()) {
                return state.setValue(TYPE, neighborType.getOpposite());
            }
        } else if (getFacing(state) == direction) {
            return state.setValue(TYPE, DishesType.SINGLE);
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    /**
     * 获取盘子方块的面朝方向
     */
    public static Direction getFacing(BlockState state) {
        Direction direction = state.getValue(FACING);
        return state.getValue(TYPE) == DishesType.LEFT ?
                direction.getClockWise() : direction.getCounterClockWise();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DishesBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * 获取相邻盘子方块的方向
     */
    @Nullable
    private Direction getNeighborDishesDirection(BlockPlaceContext ctx, Direction dir) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir));
        return blockState.is(this) && blockState.getValue(TYPE) == DishesType.SINGLE ?
                blockState.getValue(FACING) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        DishesType dishesType = DishesType.SINGLE;
        Direction direction = ctx.getHorizontalDirection();
        boolean isSneaking = ctx.isSecondaryUseActive();
        Direction side = ctx.getClickedFace();

        if (side.getAxis().isHorizontal() && isSneaking) {
            Direction neighborDirection = this.getNeighborDishesDirection(ctx, side.getOpposite());
            if (neighborDirection != null && neighborDirection.getAxis() != side.getAxis()) {
                direction = neighborDirection;
                dishesType = neighborDirection.getCounterClockWise() == side.getOpposite() ?
                        DishesType.RIGHT : DishesType.LEFT;
            }
        }

        if (dishesType == DishesType.SINGLE && !isSneaking) {
            if (direction == this.getNeighborDishesDirection(ctx, direction.getClockWise())) {
                dishesType = DishesType.LEFT;
            } else if (direction == this.getNeighborDishesDirection(ctx, direction.getCounterClockWise())) {
                dishesType = DishesType.RIGHT;
            }
        }

        return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, dishesType);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos downPos = pos.below();
        return !world.getBlockState(downPos).canBeReplaced();
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
        builder.add(FACING, TYPE);
    }

    /**
     * 盘子类型枚举
     */
    public enum DishesType implements StringRepresentable {
        SINGLE("single"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        DishesType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public DishesType getOpposite() {
            return switch (this) {
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
                default -> SINGLE;
            };
        }
    }
}