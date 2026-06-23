package org.bakingprocess.block;

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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
 * 琛ㄧず姝ｅ湪鐕冪儳鎴栬�呭凡缁忕噧灏界殑鏌寸伀鍫?
 * @see FirewoodBlock
 */
public class CombustionFirewoodBlock extends BaseEntityBlock {
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<CombustionState> COMBUSTION_STATE = EnumProperty.create("combustion_state", CombustionState.class);

    public CombustionFirewoodBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(COMBUSTION_STATE, CombustionState.FIRST_IGNITED));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return FirewoodBlock.SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 妫�鏌ユ柟鍧楁槸鍚﹀凡瀹屽叏鐔勭伃
        if (isCompletelyExtinguished(world, pos, state)) {
            // 瀹㈡埛绔�鍙�杩斿洖鎴愬姛锛屾湇鍔＄��鎵ц�屽疄闄呯牬鍧忛�昏緫
            if (!world.isClientSide()) {
                world.destroyBlock(pos, false, player);
                LootParams.Builder builder = new LootParams.Builder((ServerLevel)world)
                        .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                        .withParameter(LootContextParams.TOOL, Items.AIR.getDefaultInstance())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player);
                List<ItemStack> drops = this.getDrops(state, builder);
                for (ItemStack foodItem : drops) {
                    // 灏濊瘯鏀惧叆鐜╁�剁墿鍝佹爮锛屾斁涓嶄笅鍒欐帀钀藉湪鍦颁�?
                    if (!player.isCreative() && !player.addItem(foodItem)){
                        player.drop(foodItem, false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 濡傛灉涓嶆槸鐔勭伃鐘舵�侊紝妫�鏌ユ槸鍚︽墜鎸佹煷鐏�灏濊瘯娣绘�?
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.FIREWOOD.get()) {
            return tryAddFirewood(world, pos, player, stack);
        }

        return InteractionResult.PASS;
    }

    /**
     * 妫�鏌ユ柟鍧楁槸鍚﹀畬鍏ㄧ唲鐏?
     */
    private boolean isCompletelyExtinguished(Level world, BlockPos pos, BlockState state) {
        // 瀹㈡埛绔�鍙�妫�鏌ユ柟鍧楃姸鎬?
        if (world.isClientSide()) {
            CombustionState combustionState = state.getValue(COMBUSTION_STATE);
            return combustionState == CombustionState.FIRST_EXTINGUISHED ||
                    combustionState == CombustionState.AGAIN_EXTINGUISHED;
        }

        // 鏈嶅姟绔�妫�鏌ユ柟鍧楀疄浣撶姸鎬?
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CombustionFirewoodBlockEntity firewoodEntity) {
            return firewoodEntity.isCompletelyExtinguished();
        }

        return false;
    }

    /**
     * 灏濊瘯娣绘煷
     */
    private InteractionResult tryAddFirewood(Level world, BlockPos pos, Player player, ItemStack stack) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof CombustionFirewoodBlockEntity firewoodEntity)) {
            return InteractionResult.FAIL;
        }

        // 灏濊瘯娣绘煷
        boolean success = firewoodEntity.addFirewood();
        if (!success) {
            return InteractionResult.FAIL;
        }

        // 娑堣�楃墿鍝佸苟鎾�鏀鹃煶鏁�
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        CombustionState currentState = state.getValue(COMBUSTION_STATE);

        // 鍙�鏈夊湪鐕冪儳鐘舵�佷笅鎵嶆樉绀虹矑瀛愭晥鏋滃拰澹伴煶
        if (currentState.isBurning()) {
            // 钀ョ伀鐕冪儳澹伴煶
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

            // 鐑熼浘绮掑瓙
            if (random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + random.nextDouble() + random.nextDouble(),
                            pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            0.0, 0.07, 0.0);
                }
            }

            // 鐏�鑺辩矑瀛�
            if (random.nextInt(3) == 0) {
                for(int i = 0; i < random.nextInt(2) + 1; ++i) {
                    world.addParticle(ParticleTypes.LAVA,
                            pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + 0.4,
                            pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                            random.nextFloat() / 2.0F, 0.04, random.nextFloat() / 2.0F);
                }
            }

            // 鐏�鐒扮矑瀛�
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

        if (currentState.isBurning() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
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
        return world.isClientSide ? null : createTickerHelper(type, ModBlockEntityTypes.COMBUSTION_FIREWOOD.get(), CombustionFirewoodBlockEntity::tick);
    }

    /**
     * 閲嶅啓鎺夎惤鐗╂柟娉?- 鍙�鍦ㄧ唲鐏�鐘舵�佹椂鎺夎惤
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // 妫�鏌ユ槸鍚︿负鐔勭伃鐘舵�?
        CombustionState combustionState = state.getValue(COMBUSTION_STATE);
        if (!combustionState.isBurning()) {
            // 鍙�鍦ㄧ唲鐏�鐘舵�佹椂璋冪敤鐖剁被鏂规硶鐢熸垚鎺夎惤鐗?
            return super.getDrops(state, builder);
        }
        // 闈炵唲鐏�鐘舵�佷笉鎺夎惤浠讳綍鐗╁搧
        return List.of();
    }

    @Override
    public Item asItem() {
        return ModItems.FIREWOOD.get();
    }

    public enum CombustionState implements StringRepresentable {
        /** 3: 棣栨�＄偣鐕� - 鐕冪儳涓婇潰涓ゆ牴鏈ㄦ�� */
        FIRST_IGNITED("first_ignited", 0, true, 1.0f),
        /** 4: 棣栨�＄噧鐑ц繃鍗� - 涓婇潰涓ゆ牴鏈ㄦ�嶇⒊鍖� */
        FIRST_HALF("first_half", 1, true, 0.5f),
        /** 4鐕冨敖: 棣栨�＄噧灏� - 瀹屽叏纰冲寲 */
        FIRST_EXTINGUISHED("first_extinguished", 2, false, 0.0f),
        /** 5: 闈為�栨�＄偣鐕?- 鍦ㄧ⒊鍖栨湪妫嶄笂娣诲姞鏂版湪妫?*/
        AGAIN_IGNITED("again_ignited", 3, true, 1.0f),
        /** 6: 闈為�栨�＄噧鐑ц繃鍗?- 鏂版坊鍔犵殑鏈ㄦ�嶇⒊鍖� */
        AGAIN_HALF("again_half", 4, true, 0.5f),
        /** 7: 鍐嶆�℃坊鏌� - 鍦ㄧ⒊鍖栨湪妫嶄笂鍐嶆�℃坊鍔犳柊鏈ㄦ�?*/
        REIGNITED("reignited", 5, true, 1.0f),
        /** 6鐕冨敖: 闈為�栨�＄噧灏?- 瀹屽叏纰冲寲 */
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