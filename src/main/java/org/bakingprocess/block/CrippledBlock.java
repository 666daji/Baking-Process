package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.dfood.util.IntPropertyManager;

import java.util.Collections;
import java.util.List;

/**
 * 该类的实例是一个过渡方块，表示不完整的方块
 */
public abstract class CrippledBlock extends Block {
    public final IntegerProperty NUMBER_OF_USE;
    public final int useNumber;
    /** 表示被使用之前的方块 */
    protected final Block baseBlock;
    /** 破坏方块后的掉落物 */
    protected final List<ItemStack> Remainder;

    public CrippledBlock(Properties settings, int useNumber, Block baseBlock, ItemStack... Remainder) {
        super(settings);
        this.useNumber = useNumber;
        this.baseBlock = baseBlock;
        this.NUMBER_OF_USE = IntPropertyManager.create("number_of_use", useNumber);
        this.Remainder = Remainder.length == 0 ? Collections.emptyList() : List.of(Remainder);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide) {
            if (tryUse(world, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (itemStack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        return tryUse(world, pos, state, player);
    }

    /**
     * 尝试使用该方块
     * @param world 当前的世界
     * @param pos 方块的位置
     * @param state 当前的方块状态
     * @param player 使用方块的玩家实体
     * @return 使用的结果
     */
    protected abstract InteractionResult tryUse(LevelAccessor world, BlockPos pos, BlockState state, Player player);

    /**
     * 获取方块使用完之后方块状态
     * @param world 当前的世界
     * @param pos 方块的位置
     * @param state 当前的方块状态
     * @param player 使用方块的玩家实体
     * @return 使用完之后的方块状态
     */
    protected BlockState getUseFinishesState(LevelAccessor world, BlockPos pos, BlockState state, Player player){
        return Blocks.AIR.defaultBlockState();
    }

    public boolean isBaseBlock(BlockState state) {
        return state.getBlock() == baseBlock;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Remainder;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IntPropertyManager.take());
    }

    public Block getBaseBlock() {
        return baseBlock;
    }
}
