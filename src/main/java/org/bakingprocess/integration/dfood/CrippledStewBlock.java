package org.bakingprocess.integration.dfood;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import org.dfood.block.FoodBlocks;
import org.dfood.shape.FoodShapeHandle;
import org.twcore.api.util.IntPropertyManager;

/**
 * 该类的实例是一个过渡方块，表示被食用过的汤
 */
public class CrippledStewBlock extends CrippledBlock {
    public final FoodProperties foodComponent;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final FoodShapeHandle foodShapeHandle = FoodShapeHandle.getInstance();

    public CrippledStewBlock(Properties settings, int maxUse, FoodProperties foodComponent, Block baseBlock) {
        super(settings, maxUse, baseBlock, new ItemStack(Items.BOWL));
        this.foodComponent = foodComponent;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return foodShapeHandle.getShape(state, NUMBER_OF_USE);
    }

    @Override
    protected InteractionResult tryUse(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        } else {
            world.playSound(player, pos, SoundEvents.GENERIC_DRINK, player.getSoundSource(), 1.0F, 1.0F);
            player.getFoodData().eat(foodComponent.getNutrition() / 4, foodComponent.getSaturationModifier() / 4.0F);
            int i = state.getValue(NUMBER_OF_USE);
            world.gameEvent(player, GameEvent.EAT, pos);
            if (i < useNumber) {
                world.setBlock(pos, state.setValue(NUMBER_OF_USE, i + 1), Block.UPDATE_ALL);
            } else {
                world.setBlock(pos, getUseFinishesState(world, pos, state, player), Block.UPDATE_ALL);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected BlockState getUseFinishesState(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        return FoodBlocks.BOWL.defaultBlockState().setValue(FACING, state.getValue(FACING));
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world,pos, state, player);
        if (!world.isClientSide && state.getValue(NUMBER_OF_USE) > 0) {
            int hunger = foodComponent.getNutrition() / 4;
            float saturation = foodComponent.getSaturationModifier() / 4.0F;
            int numberOfEat = state.getValue(NUMBER_OF_USE);
            player.getFoodData().eat(hunger * numberOfEat, saturation * numberOfEat);
            world.gameEvent(player, GameEvent.EAT, pos);
        }
    }

    public static BlockState getStewState(BlockState state) {
        for (Block block : (Block[]) AssistedBlocks.assistedBlocks.stream().map(RegistryObject::get).toArray()) {
            if (block instanceof CrippledStewBlock crippledStewBlock && crippledStewBlock.isBaseBlock(state)) {
                return crippledStewBlock.defaultBlockState()
                        .setValue(CrippledStewBlock.FACING, state.getValue(CrippledStewBlock.FACING))
                        .setValue(IntPropertyManager.create("number_of_use", 4), 1);
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public boolean isSuspiciousStew() {
        return this instanceof CrippledSuspiciousStewBlock;
    }
}
