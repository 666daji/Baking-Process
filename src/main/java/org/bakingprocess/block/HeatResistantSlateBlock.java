package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.CombustionFirewoodBlockEntity;
import org.bakingprocess.block.entity.HeatResistantSlateBlockPileEntity;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.ModSounds;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.block.UpPlaceBlock;
import org.twcore.api.block.UpPlaceBlockEntity;
import org.twcore.api.blockpile.CubeBlockPileHelper;

import java.util.function.Predicate;

public class HeatResistantSlateBlock extends UpPlaceBlock {
    protected static final VoxelShape BASE_SHAPE = Block.box(0,0,0,16,2,16);

    public static final BlockPattern stove1x1;
    public static final BlockPattern stove1x2;
    public static final BlockPattern stove2x2;
    public static final BlockPattern stove2x3;

    public HeatResistantSlateBlock(Properties settings) {
        super(settings);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onPlace(state, world, pos, oldState, notify);

        if (!world.isClientSide) {
            // 澶勭悊鏂瑰潡鏀剧疆
            CubeBlockPileHelper.onBlockPlaced(world, pos, this);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, fromPos, notify);

        if (!world.isClientSide) {
            // 澶勭悊鐩搁偦鏂瑰潡鏇存柊锛屾��鏌ュ�氭柟鍧楃粨鏋勫畬鏁存�?
            CubeBlockPileHelper.onNeighborUpdate(world, pos, this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Container inventory) {
                Containers.dropContents(world, pos, inventory);
                world.updateNeighbourForOutputSignal(pos, this);
            }

            // 澶勭悊鏂瑰潡鐮村潖
            CubeBlockPileHelper.onBlockBroken(world, pos, this);
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        // 璋冪敤鐖剁被浜や簰鏂规硶
        InteractionResult result = super.use(state, world, pos, player, hand, hit);

        // 濡傛灉浜や簰澶辫触锛屽垯灏濊瘯浜や簰缁戝畾鐨勭瘽鐏?
        if (!result.consumesAction() && blockEntity instanceof HeatResistantSlateBlockPileEntity heatResistantSlateBlockEntity) {
            for (CombustionFirewoodBlockEntity firewoodEntity : heatResistantSlateBlockEntity.getFirewoodEntities()) {
                BlockState firewoodState = firewoodEntity.getBlockState();
                InteractionResult firewoodResult = firewoodState.getBlock().use(firewoodState, world, firewoodEntity.getBlockPos(), player, hand, hit);
                if (firewoodResult.consumesAction()) {
                    return firewoodResult;
                }
            }
        }

        return result;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof HeatResistantSlateBlockPileEntity heatResistantSlateBlockEntity
                    && heatResistantSlateBlockEntity.isBaking()) {
            if (random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticle(ParticleTypes.CLOUD,
                            pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            pos.getY() + random.nextDouble() + random.nextDouble(),
                            pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                            0.0, 0.07, 0.0);
                }
            }
            if (random.nextInt(5) == 0) {
                world.playLocalSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        ModSounds.COOKING_SOUND.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        true
                );
            }
        }
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    public boolean canFetched(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        return !blockEntity.isEmpty() && handStack.getItem().equals(ModItems.BREAD_SPATULA.get());
    }

    @Override
    public boolean canPlace(UpPlaceBlockEntity blockEntity, ItemStack handStack) {
        return blockEntity.isValidItem(handStack);
    }

    @Nullable
    public BlockPattern getStovePattern(int index){
        switch (index) {
            case 1 -> {
                return stove1x1;
            }
            case 2 -> {
                return stove1x2;
            }
            case 3 -> {
                return stove2x2;
            }
            case 4 -> {
                return stove2x3;
            }
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatResistantSlateBlockPileEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, (BlockEntityType<? extends HeatResistantSlateBlockPileEntity>) ModBlockEntityTypes.HEAT_RESISTANT_SLATE.get(), HeatResistantSlateBlockPileEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    static {
        Predicate<BlockInWorld> heatResistantSlatePredicate = cachedBlockPosition -> cachedBlockPosition.getState().getBlock() instanceof HeatResistantSlateBlock;
        Predicate<BlockInWorld> firewoodPredicate = cachedBlockPosition -> cachedBlockPosition.getState().isAir() ||
                cachedBlockPosition.getState().getBlock() instanceof FirewoodBlock ||
                cachedBlockPosition.getState().getBlock() instanceof CombustionFirewoodBlock;

        stove1x1 = BlockPatternBuilder.start()
                .aisle("?#?", "#|#")
                .aisle("#^#","#~#")
                .aisle("?#?", "?#?")
                .where('^', cachedBlockPosition -> cachedBlockPosition.getState().isAir())
                .where('#', cachedBlockPosition -> !cachedBlockPosition.getState().isAir())
                .where('|', heatResistantSlatePredicate)
                .where('~', firewoodPredicate)
                .where('?', cachedBlockPosition -> true)
                .build();
        stove1x2 = BlockPatternBuilder.start()
                .aisle("?#?", "#|#")
                .aisle("?#?", "#|#")
                .aisle("#^#","#~#")
                .aisle("?#?", "?#?")
                .where('^', cachedBlockPosition -> cachedBlockPosition.getState().isAir())
                .where('#', cachedBlockPosition -> !cachedBlockPosition.getState().isAir())
                .where('|', heatResistantSlatePredicate)
                .where('~', firewoodPredicate)
                .where('?', cachedBlockPosition -> true)
                .build();
        stove2x2 = BlockPatternBuilder.start()
                .aisle("?##?", "#||#")
                .aisle("?##?", "#||#")
                .aisle("#^^#","#~~#")
                .aisle("?##?", "?##?")
                .where('^', cachedBlockPosition -> cachedBlockPosition.getState().isAir())
                .where('#', cachedBlockPosition -> !cachedBlockPosition.getState().isAir())
                .where('|', heatResistantSlatePredicate)
                .where('~', firewoodPredicate)
                .where('?', cachedBlockPosition -> true)
                .build();
        stove2x3 = BlockPatternBuilder.start()
                .aisle("?##?", "#||#")
                .aisle("?##?", "#||#")
                .aisle("?##?", "#||#")
                .aisle("#^^#","#~~#")
                .aisle("?##?", "?##?")
                .where('^', cachedBlockPosition -> cachedBlockPosition.getState().isAir())
                .where('#', cachedBlockPosition -> !cachedBlockPosition.getState().isAir())
                .where('|', heatResistantSlatePredicate)
                .where('~', firewoodPredicate)
                .where('?', cachedBlockPosition -> true)
                .build();
    }
}