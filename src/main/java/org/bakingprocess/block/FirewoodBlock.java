package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.CombustionFirewoodBlockEntity;
import org.dfood.block.FoodBlock;
import org.dfood.block.FoodBlockBuilder;
import org.dfood.shape.FoodShapeHandle;

public class FirewoodBlock extends FoodBlock {
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);

    /** 被点燃后变成的方块 */
    protected final Block targetBlock;

    protected FirewoodBlock(Properties settings, int maxFood, Block targetBlock) {
        super(settings, maxFood, false, null, false, null);
        this.targetBlock = targetBlock;
    }

    public static class Builder extends FoodBlockBuilder<FirewoodBlock, Builder> {
        private Block targetBlock;

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        /**
         * 设置点燃后变成的方块
         * @param targetBlock 目标方块
         */
        public Builder targetBlock(Block targetBlock) {
            this.targetBlock = targetBlock;
            return self();
        }

        @Override
        protected void validateSettings() {
            if (this.targetBlock == null) {
                throw new IllegalStateException("Target block must be set for FirewoodBlock.");
            }

            super.validateSettings();
        }

        @Override
        protected FirewoodBlock createBlock() {
            return new FirewoodBlock(
                    this.settings,
                    this.maxFood,
                    this.targetBlock
            );
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_HANDLE.getShape(state, NUMBER_OF_FOOD, Shapes.class);
    }

    /**
     * 使用打火石点燃柴火堆
     */
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getBlock() instanceof FirewoodBlock firewoodBlock && player.getItemInHand(hand).getItem() == Items.FLINT_AND_STEEL){
            boolean bl = firewoodBlock.tryIgnite(state, world, pos, player);
            return bl? InteractionResult.SUCCESS: InteractionResult.PASS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    /**
     * 尝试点燃柴火堆,只有当柴火的堆叠数为2时才能成功点燃
     * @param state 当前方块状态
     * @param world 世界
     * @param pos 方块位置
     * @param player 触发点燃的玩家
     * @return 是否成功点燃
     */
    public boolean tryIgnite(BlockState state, Level world, BlockPos pos, Player player) {
        int currentCount = state.getValue(NUMBER_OF_FOOD);
        if (currentCount != 2) {
            return false;
        }

        // 检查上方空间
        if (!hasClearSpaceAbove(world, pos)) {
            // 播放失败音效或给玩家提示
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
            return false;
        }

        world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);

        // 设置燃烧柴火方块状态为首次点燃
        world.setBlockAndUpdate(pos, targetBlock.defaultBlockState()
                .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
                .setValue(CombustionFirewoodBlock.COMBUSTION_STATE, CombustionFirewoodBlock.CombustionState.FIRST_IGNITED));

        // 设置方块实体的初始能量
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CombustionFirewoodBlockEntity firewoodEntity) {
            firewoodEntity.setEnergy(CombustionFirewoodBlockEntity.getMaxEnergy());
            firewoodEntity.setFirstCycle(true);
            firewoodEntity.setCycleCount(0);
        }

        world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }

    /**
     * 检查柴火堆上方6格内是否为空气
     * @param world 世界
     * @param pos 柴火堆位置
     * @return 上方6格内是否全部为空气
     */
    public static boolean hasClearSpaceAbove(Level world, BlockPos pos) {
        for (int i = 1; i <= 6; i++) {
            BlockPos checkPos = pos.above(i);
            BlockState state = world.getBlockState(checkPos);

            // 检查方块是否为空气或可替换方块
            if (!state.isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否可以点燃（包括上方空间检查）
     */
    public boolean canIgnite(Level world, BlockPos pos) {
        return hasClearSpaceAbove(world, pos);
    }

    public enum Shapes implements FoodShapeHandle.ShapeConvertible {
        SHAPE_A(1, Block.box(0,0,0,16,4,16)),
        SHAPE_B(2, Block.box(0,0,0,16,8,16)),
        SHAPE_C(3, Block.box(0,0,0,16,9,16)),
        SHAPE_D(4, Block.box(0,0,0,16,13,16)),
        SHAPE_E(5, Block.box(0,0,0,16,16,16));

        private final VoxelShape shape;
        private final int id;

        Shapes(int id, VoxelShape shape) {
            this.shape = shape;
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public VoxelShape getShape() {
            return shape;
        }

        public static VoxelShape getShape(int id) {
            for (Shapes s : values()) {
                if (s.id == id) {
                    return s.shape;
                }
            }
            return SHAPE_A.shape;
        }
    }
}
