package org.bakingprocess.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.container.BreadBoatContainer;
import org.bakingprocess.food.SimpleFoodComponent;
import org.dfood.block.SimpleFoodBlock;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.content.ContainerUtil;
import org.twcore.api.util.IntPropertyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreadBoatBlock extends SimpleFoodBlock {
    public static final EnumProperty<BreadBoatContainer.BreadBoatSoupType> SOUP_TYPE = EnumProperty.create("soup_type", BreadBoatContainer.BreadBoatSoupType.class);
    /** 表示当前已食用次数 */
    public final IntegerProperty BITES;

    public final int maxUse;

    public BreadBoatBlock(Properties settings, VoxelShape shape, int maxUse, @Nullable EnforceAsItem cItem) {
        super(settings, true, shape, false, cItem);
        this.maxUse = maxUse;
        this.BITES = IntPropertyManager.create("bites", 0, maxUse);

        this.registerDefaultState(this.defaultBlockState().setValue(BITES, 0));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 如果玩家可以吃东西，尝试喝汤
        if (player.canEat(false)) {
            return tryDrinkSoup(world, pos, state, player);
        }

        // 如果还没被食用，执行父类逻辑
        if (state.getValue(BITES) == 0) {
            return super.use(state, world, pos, player, hand, hit);
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemStack createStack(int count, BlockState state, @Nullable BlockEntity blockEntity) {
        return ContainerUtil.analyze(super.createStack(count, state, blockEntity))
                .map(containerStack -> containerStack.replaceContent(state.getValue(SOUP_TYPE).getContent()))
                .orElse(super.createStack(count, state, blockEntity));
    }

    /**
     * 直接获取当前的食物属性。
     * @param state 当前的方块状态
     * @return 当前的食物属性，如果对应的物品不是食物则返回空。
     */
    @Nullable
    protected static SimpleFoodComponent getFoodComponent(BlockState state) {
        BreadBoatContainer.BreadBoatSoupType soupType = state.getValue(SOUP_TYPE);
        FoodProperties containerFood = state.getBlock().asItem().getFoodProperties();
        FoodProperties soupFood = soupType.getFoodComponent();

        if (containerFood == null) {
            BakingProcess.LOGGER.warn("EdibleContainer must have food component!");
            return null;
        }

        SimpleFoodComponent containerComponent = SimpleFoodComponent.fromFoodComponent(containerFood);
        SimpleFoodComponent soupComponent = SimpleFoodComponent.fromFoodComponent(soupFood);

        return containerComponent.merge(soupComponent);
    }

    /**
     * 计算每次食用的食物属性（占总属性的1/maxUse）
     */
    private SimpleFoodComponent getFoodPerBite(SimpleFoodComponent totalFood) {
        return totalFood.percent(100 / maxUse);
    }

    /**
     * 计算剩余次数的食物属性
     */
    private SimpleFoodComponent getRemainingFood(SimpleFoodComponent totalFood, int remainingBites) {
        int percentage = (remainingBites * 100) / maxUse;
        return totalFood.percent(percentage);
    }

    /**
     * 尝试喝一口汤。
     * @param player 喝汤的玩家
     */
    protected InteractionResult tryDrinkSoup(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        int currentBites = state.getValue(BITES);

        // 播放喝汤声音
        if (currentBites < 3) {
            world.playSound(player, pos, SoundEvents.GENERIC_DRINK, player.getSoundSource(), 1.0F, 1.0F);
        } else {
            world.playSound(player, pos, SoundEvents.GENERIC_EAT, player.getSoundSource(), 1.0F, 1.0F);
        }

        SimpleFoodComponent totalFood = getFoodComponent(state);
        if (totalFood == null) {
            return InteractionResult.FAIL;
        }

        // 计算本次食用的食物属性
        SimpleFoodComponent foodPerBite = getFoodPerBite(totalFood);

        // 恢复饥饿值和饱和度
        foodPerBite.eat(player);
        world.gameEvent(player, GameEvent.EAT, pos);

        // 更新喝汤次数
        if (currentBites < maxUse - 1) {
            // 还有剩余次数，增加喝汤次数
            world.setBlock(pos, state.setValue(BITES, currentBites + 1), Block.UPDATE_ALL);
        } else {
            // 喝完了，面包船被吃掉，不留下任何物品
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        int currentBites = state.getValue(BITES);
        SimpleFoodComponent totalFood = getFoodComponent(state);

        // 如果已经被吃过，强制返还剩余饱食度
        if (!world.isClientSide && currentBites > 0 && totalFood != null) {
            int remainingBites = maxUse - currentBites;

            // 计算剩余的食物属性
            SimpleFoodComponent remainingFood = getRemainingFood(totalFood, remainingBites);

            // 恢复剩余的饥饿值和饱和度
            remainingFood.eat(player);
            world.gameEvent(player, GameEvent.EAT, pos);
        }

        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int bites = state.getValue(BITES);
        if (bites == 0){
            List<ItemStack> droppedStacks = super.getDrops(state, builder);
            List<ItemStack> newList = new ArrayList<>();
            droppedStacks.forEach(stack -> ContainerUtil.analyze(stack).map(containerStack ->
                    newList.add(containerStack.replaceContent(state.getValue(SOUP_TYPE).getContent())))
                    .orElseGet(() -> newList.add(stack)));

            return droppedStacks;
        }

        // 如果已经被食用，则不掉落任何物品
        return Collections.emptyList();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IntPropertyManager.take(), SOUP_TYPE);
    }
}