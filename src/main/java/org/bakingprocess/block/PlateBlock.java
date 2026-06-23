package org.bakingprocess.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.entity.PlateBlockEntity;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.registry.ModContents;
import org.bakingprocess.registry.ModItems;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;
import org.twcore.content.ContentCategories;

import java.util.ArrayList;
import java.util.List;

/**
 * 琛ㄧず涓�涓�鍙�浠ユ憜鐩樼殑鐩樺瓙鏂瑰潡
 */
public class PlateBlock extends Block implements EntityBlock {
    /**
     * 琛ㄧず褰撳墠鐨勬柟鍧楁槸鍚﹀凡缁忚��鐩栧瓙瑕嗙洊銆?
     * <p>璇蜂笉瑕佺洿鎺ユ洿鏀硅�ュ睘鎬х殑鍊笺�?/p>
     */
    public static final BooleanProperty IS_COVERED = BooleanProperty.create("is_covered");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape BASE_SHAPE = Block.box(0.5, 0, 0.5, 15.5, 2,15.5);
    public static final VoxelShape LIB_SHAPE = Block.box(1, 2, 1, 15, 8, 15);

    public PlateBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(defaultBlockState().setValue(IS_COVERED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        ItemStack handStack = player.getItemInHand(hand);

        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }

        if (entity instanceof PlateBlockEntity plateBlockEntity) {
            // 灏濊瘯椋熺敤
            if (plateBlockEntity.getOutcome() != null && !state.getValue(IS_COVERED) && handStack.isEmpty()) {
                return plateBlockEntity.tryEat(player, hand, hit);
            }

            if (plateBlockEntity.getEatProcess().isActive()) {
                return InteractionResult.PASS;
            }

            // 灏濊瘯鐩栫洊瀛?
            if (plateBlockEntity.getOutcome() != null && !state.getValue(IS_COVERED) && handStack.is(ModItems.PLATE_LID.get())) {
                plateBlockEntity.coverWithLid();
                if (!player.isCreative()) {
                    handStack.shrink(1);
                }

                return InteractionResult.SUCCESS;
            }

            // 灏濊瘯鍙栦笅鐩栧瓙
            if (state.getValue(IS_COVERED) && player.isShiftKeyDown() && plateBlockEntity.removeCoverAndRestore()) {
                player.addItem(new ItemStack(ModItems.PLATE_LID.get()));
                return InteractionResult.SUCCESS;
            }

            // 鐩存帴鍙栦笅鏁翠釜鐩樺瓙
            if (state.getValue(IS_COVERED) && plateBlockEntity.getOutcome() != null && !player.isShiftKeyDown() && handStack.isEmpty()) {
                // 鏋勫缓甯︽湁鑿滆偞鏁版嵁鐨勭洏瀛愮墿鍝?
                ItemStack plateStack = new ItemStack(this.asItem());
                DishesContent outcome = plateBlockEntity.getOutcome();

                // 缁欎簣鐜╁�剁墿鍝�
                player.addItem(ContainerUtil.analyze(plateStack)
                        .map(containerStack -> containerStack.replaceContent(outcome))
                        .orElse(plateStack));

                // 绉婚櫎鏂瑰潡
                world.removeBlock(pos, false);
                return InteractionResult.SUCCESS;
            }

            // 灏濊瘯鎽嗙洏
            return plateBlockEntity.tryPlating(player, hand, hit);
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity entity = builder.getParameter(LootContextParams.BLOCK_ENTITY);

        if (state.getValue(IS_COVERED) && entity instanceof PlateBlockEntity plateBlockEntity) {
            List<ItemStack> droppedStacks = super.getDrops(state, builder);
            List<ItemStack> newList = new ArrayList<>();
            droppedStacks.forEach(stack -> ContainerUtil.analyze(stack)
                    .map(containerStack -> newList.add(containerStack.replaceContent(plateBlockEntity.getOutcome())))
                    .orElseGet(() -> newList.add(stack)));

            return droppedStacks;
        }

        return super.getDrops(state, builder);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        Content content = ContainerUtil.extractContent(itemStack);
        BlockEntity entity = world.getBlockEntity(pos);

        if (content instanceof DishesContent dishes && entity instanceof PlateBlockEntity plateBlockEntity) {
            plateBlockEntity.setOutcome(dishes);
            plateBlockEntity.coverWithLid();
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
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        Content content = ContainerUtil.extractContent(stack);

        if (content != null) {
            Component text = content.getDisplayName();
            if (text instanceof MutableComponent mutableText) {
                mutableText.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY);
            }
            tooltip.add(text);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(IS_COVERED)) {
            return Shapes.or(BASE_SHAPE, LIB_SHAPE);
        }

        return BASE_SHAPE;
    }

    public static NonNullList<ItemStack> getAll(Item item) {
        NonNullList<ItemStack> result = NonNullList.create();

        for (Content content : ContentCategories.getByCategory(ModContents.DISHES.get())) {
            ItemStack stack = new ItemStack(item);
            ItemStack stack1 = ContainerUtil.analyze(stack).orElseThrow().replaceContent(content);
            result.add(stack1);
        }

        return result;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlateBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_COVERED, FACING);
    }
}