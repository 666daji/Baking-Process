package org.bakingprocess.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.CombustionFirewoodBlockEntity;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModItems;
import org.dfood.block.FoodBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 表示正在燃烧或者已经燃尽的柴火堆
 * @see FirewoodBlock
 */
public class CombustionFirewoodBlock extends BaseEntityBlock {
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<CombustionState> COMBUSTION_STATE = EnumProperty.create("combustion_state", CombustionState.class);
    public static final MapCodec<CombustionFirewoodBlock> CODEC = simpleCodec(CombustionFirewoodBlock::new);

    public CombustionFirewoodBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(COMBUSTION_STATE, CombustionState.FIRST_IGNITED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return FirewoodBlock.SHAPE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = player.getUsedItemHand();
        // 检查方块是否已完全熄灭
        if (isCompletelyExtinguished(world, pos, state)) {
            // 客户端只返回成功，服务端执行实际破坏逻辑
            if (!world.isClientSide()) {
                world.destroyBlock(pos, false, player);
                LootParams.Builder builder = new LootParams.Builder((ServerLevel)world)
                        .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                        .withParameter(LootContextParams.TOOL, Items.AIR.getDefaultInstance())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player);
                List<ItemStack> drops = this.getDrops(state, builder);
                for (ItemStack foodItem : drops) {
                    // 尝试放入玩家物品栏，放不下则掉落在地上
                    if (!player.isCreative() && !player.addItem(foodItem)){
                        player.drop(foodItem, false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 如果不是熄灭状态，检查是否手持柴火尝试添柴
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.FIREWOOD.get()) {
            return tryAddFirewood(world, pos, player, stack);
        }

        return InteractionResult.PASS;
    }

    /**
     * 检查方块是否完全熄灭
     */
    private boolean isCompletelyExtinguished(Level world, BlockPos pos, BlockState state) {
        // 客户端只检查方块状态
        if (world.isClientSide()) {
            CombustionState combustionState = state.getValue(COMBUSTION_STATE);
            return combustionState == CombustionState.FIRST_EXTINGUISHED ||
                    combustionState == CombustionState.AGAIN_EXTINGUISHED;
        }

        // 服务端检查方块实体状态
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CombustionFirewoodBlockEntity firewoodEntity) {
            return firewoodEntity.isCompletelyExtinguished();
        }

        return false;
    }

    /**
     * 尝试添柴
     */
    private InteractionResult tryAddFirewood(Level world, BlockPos pos, Player player, ItemStack stack) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof CombustionFirewoodBlockEntity firewoodEntity)) {
            return InteractionResult.FAIL;
        }

        // 尝试添柴
        boolean success = firewoodEntity.addFirewood();
        if (!success) {
            return InteractionResult.FAIL;
        }

        // 消耗物品并播放音效
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        CombustionState currentState = state.getValue(COMBUSTION_STATE);

        // 只有在燃烧状态下才显示粒子效果和声音
        if (currentState.isBurning()) {
            // 营火燃烧声音
            if (random.nextInt(5) == 0) {
                world.playLocalSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        SoundEvents.CAMPFIRE_CRACKLE,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        true
                );
            }

            // 烟雾粒子
            if (random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + random.nextDouble() + random.nextDouble(),
                            pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            0.0, 0.07, 0.0);
                }
            }

            // 火花粒子
            if (random.nextInt(3) == 0) {
                for(int i = 0; i < random.nextInt(2) + 1; ++i) {
                    world.addParticle(ParticleTypes.LAVA,
                            pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + 0.4,
                            pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                            random.nextFloat() / 2.0F, 0.04, random.nextFloat() / 2.0F);
                }
            }

            // 火焰粒子
            if (random.nextInt(4) == 0) {
                for(int i = 0; i < random.nextInt(2) + 1; ++i) {
                    world.addParticle(ParticleTypes.FLAME,
                            pos.getX() + 0.5 + random.nextDouble() / 2.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + 0.2,
                            pos.getZ() + 0.5 + random.nextDouble() / 2.0 * (random.nextBoolean() ? 1 : -1),
                            0.0, 0.04, 0.0);
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        CombustionState currentState = state.getValue(COMBUSTION_STATE);

        if (currentState.isBurning() && entity instanceof LivingEntity) {
            entity.hurt(world.damageSources().inFire(), 1);
        }

        super.entityInside(state, world, pos, entity);
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
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos downPos = pos.below();
        BlockState checkState = world.getBlockState(downPos);
        return !checkState.canBeReplaced() && !(checkState.getBlock() instanceof FoodBlock);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMBUSTION_STATE, HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CombustionFirewoodBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntityTypes.COMBUSTION_FIREWOOD.get(), CombustionFirewoodBlockEntity::tick);
    }

    /**
     * 重写掉落物方法 - 只在熄灭状态时掉落
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // 检查是否为熄灭状态
        CombustionState combustionState = state.getValue(COMBUSTION_STATE);
        if (!combustionState.isBurning()) {
            // 只在熄灭状态时调用父类方法生成掉落物
            return super.getDrops(state, builder);
        }
        // 非熄灭状态不掉落任何物品
        return List.of();
    }

    @Override
    public Item asItem() {
        return ModItems.FIREWOOD.get();
    }

    public enum CombustionState implements StringRepresentable {
        /** 3: 首次点燃 - 燃烧上面两根木棍 */
        FIRST_IGNITED("first_ignited", 0, true, 1.0f),
        /** 4: 首次燃烧过半 - 上面两根木棍碳化 */
        FIRST_HALF("first_half", 1, true, 0.5f),
        /** 4燃尽: 首次燃尽 - 完全碳化 */
        FIRST_EXTINGUISHED("first_extinguished", 2, false, 0.0f),
        /** 5: 非首次点燃 - 在碳化木棍上添加新木棍 */
        AGAIN_IGNITED("again_ignited", 3, true, 1.0f),
        /** 6: 非首次燃烧过半 - 新添加的木棍碳化 */
        AGAIN_HALF("again_half", 4, true, 0.5f),
        /** 7: 再次添柴 - 在碳化木棍上再次添加新木棍 */
        REIGNITED("reignited", 5, true, 1.0f),
        /** 6燃尽: 非首次燃尽 - 完全碳化 */
        AGAIN_EXTINGUISHED("again_extinguished", 6, false, 0.0f);

        private final String id;
        private final int index;
        private final boolean burning;
        private final float particleIntensity;

        CombustionState(String id, int index, boolean burning, float particleIntensity) {
            this.id = id;
            this.index = index;
            this.burning = burning;
            this.particleIntensity = particleIntensity;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isBurning() {
            return burning;
        }

        public float getParticleIntensity() {
            return particleIntensity;
        }

        public static CombustionState byIndex(int index) {
            for (CombustionState state : values()) {
                if (state.getIndex() == index) {
                    return state;
                }
            }
            return FIRST_IGNITED;
        }
    }
}