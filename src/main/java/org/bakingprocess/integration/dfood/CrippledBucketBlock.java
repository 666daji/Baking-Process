package org.bakingprocess.integration.dfood;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.block.CrippledBlock;
import org.bakingprocess.registry.ModItems;
import org.dfood.block.FoodBlocks;
import org.dfood.shape.FoodShapeHandle;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.util.IntPropertyManager;

/**
 * 水桶的残损方块，表示被使用过的桶
 */
public class CrippledBucketBlock extends CrippledBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final FoodShapeHandle foodShapeHandle = FoodShapeHandle.getInstance();

    /**
     * 药水类型，用于确定给予玩家的瓶子内容物�?
     * 为null时表示奶桶�?
     */
    @Nullable
    private final Potion potionType;

    public CrippledBucketBlock(Properties settings, int maxUse, Block baseBlock, @Nullable Potion potionType) {
        super(settings, maxUse, baseBlock, new ItemStack(Items.BUCKET));
        this.potionType = potionType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return foodShapeHandle.getShape(state, NUMBER_OF_USE);
    }

    @Override
    protected InteractionResult tryUse(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        ItemStack handStack = player.getMainHandItem();

        // 检查是否手持空瓶子
        if (handStack.getItem() == Items.GLASS_BOTTLE) {
            int i = state.getValue(NUMBER_OF_USE);
            world.playSound(player, pos, SoundEvents.BOTTLE_FILL, player.getSoundSource(), 1.0F, 1.0F);

            // 消耗空瓶子并给予水�?
            if (!player.isCreative()) {
                handStack.shrink(1);
            }
            ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), this.potionType);
            if (this.potionType == null) {
                waterBottle = new ItemStack(ModItems.MILK_POTION.get());
            }
            if (!player.addItem(waterBottle)) {
                player.drop(waterBottle, false);
            }

            world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

            if (i < 3) {
                world.setBlock(pos, state.setValue(NUMBER_OF_USE, i + 1), Block.UPDATE_ALL);
            } else {
                // 水用完了，变成空�?
                world.setBlock(pos, getUseFinishesState(world, pos, state, player), Block.UPDATE_ALL);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected BlockState getUseFinishesState(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        return FoodBlocks.BUCKET.defaultBlockState().setValue(FACING, state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public static BlockState getWaterBucketState(BlockState state) {
        for (Block block : (Block[]) AssistedBlocks.assistedBlocks.stream().map(RegistryObject::get).toArray()) {
            if (block instanceof CrippledBucketBlock crippledBucketBlock && crippledBucketBlock.isBaseBlock(state)) {
                return crippledBucketBlock.defaultBlockState()
                        .setValue(CrippledBucketBlock.FACING, state.getValue(CrippledBucketBlock.FACING))
                        .setValue(IntPropertyManager.create("number_of_use", 3), 1);
            }
        }
        return Blocks.AIR.defaultBlockState();
    }
}