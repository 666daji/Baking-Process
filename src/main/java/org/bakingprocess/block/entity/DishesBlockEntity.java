package org.bakingprocess.block.entity;



import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.protocol.Packet;

import net.minecraft.network.protocol.game.ClientGamePacketListener;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.BlockItem;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;

import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.phys.shapes.CollisionContext;

import net.minecraft.world.phys.shapes.VoxelShape;

import org.bakingprocess.block.GarnishDishesBlock;

import org.bakingprocess.registry.ModBlockEntityTypes;

import org.bakingprocess.util.BakingProcessUtils;

import org.dfood.block.FoodBlock;

import org.dfood.item.DoubleBlockItem;

import org.dfood.shape.FoodShapeHandle;

import org.twcore.api.block.UpPlaceBlockEntity;



import java.util.List;



/**

 * 鐩樺瓙鏂瑰潡瀹炰綋锛岀敤浜庡瓨鍌ㄩ�熺墿鐗╁�?
 * 娉ㄦ剰锛歩tem.getBlock()杩斿洖鐨凚lock蹇呴』鏄瘂@link FoodBlock}鐨勫疄渚嬶紝鍚﹀垯鏃犳硶鏀惧叆鐗╁搧鏍?
 */

public class DishesBlockEntity extends UpPlaceBlockEntity {

    private static final int INVENTORY_SIZE = 1;

    private static final int MAX_STACK_SIZE = 11;

    private static final double FOOD_OFFSET_Y = 0.1;



    public DishesBlockEntity(BlockPos pos, BlockState state) {

        super(ModBlockEntityTypes.GARNISH_DISHES.get(), pos, state, INVENTORY_SIZE);

    }



    @Override

    public VoxelShape getContentShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {

        BlockState itemState = this.getInventoryBlockState();

        if (itemState.getBlock() instanceof FoodBlock foodBlock) {

            return FoodShapeHandle.getInstance().getShape(itemState, foodBlock.NUMBER_OF_FOOD)

                    .move(0.0, FOOD_OFFSET_Y, 0.0);

        }

        return FoodShapeHandle.shapes.ALL.getShape().move(0.0, FOOD_OFFSET_Y, 0.0);

    }



    @Override

    public boolean isValidItem(ItemStack stack) {

        if (stack.isEmpty()) {

            return false;

        }



        // 铔嬬硶鍙�浠ョ洿鎺ユ斁鍏�

        if (stack.getItem() == Items.CAKE) {

            return true;

        }



        Item item = stack.getItem();

        if (item instanceof DoubleBlockItem doubleBlockItem) {

            return doubleBlockItem.getSecondBlock() instanceof FoodBlock;

        } else if (item instanceof BlockItem blockItem) {

            return blockItem.getBlock() instanceof FoodBlock;

        }

        return false;

    }



    /**

     * 鑾峰彇褰撳墠鐗╁搧鏍忎腑鐨勭墿鍝佸�瑰簲鐨勬柟鍧楃姸鎬?
     * @return 鐗╁搧瀵瑰簲鐨勬柟鍧楃姸鎬?
     */

    public BlockState getInventoryBlockState() {

        ItemStack stack = this.inventory.get(0);

        Direction facing = this.getBlockState().getValue(GarnishDishesBlock.FACING);



        return BakingProcessUtils.createCountBlockstate(stack, facing);

    }



    @Override

    public Result tryAddItem(ItemStack stack, BlockHitResult hit) {

        if (stack.isEmpty() || !isValidItem(stack)) {

            return Result.of(InteractionResult.PASS);

        }



        Item item = stack.getItem();

        ItemStack newStack = stack.copy();

        newStack.setCount(1);

        ItemStack currentStack = this.getItem(0);



        if (currentStack.isEmpty()) {

            this.setItem(0, newStack);

            this.markDirtyAndSync();

            return Result.of(newStack, InteractionResult.SUCCESS);

        } else if (currentStack.getItem() == item) {

            FoodBlock block = (FoodBlock) getInventoryBlockState().getBlock();

            if (currentStack.getCount() < block.MAX_FOOD) {

                currentStack.grow(1);

                this.markDirtyAndSync();

                return Result.of(newStack, InteractionResult.SUCCESS);

            }

        }

        return Result.of(InteractionResult.PASS);

    }



    @Override

    public Result tryFetchItem(Player player, BlockHitResult hit) {

        if (!isEmpty()) {

            ItemStack stack = removeItem(0, 1);

            if (!player.isCreative() && !player.addItem(stack)) {

                player.drop(stack, false);

            }

            markDirtyAndSync();

            return Result.of(List.of(stack.copy()), InteractionResult.SUCCESS);

        }

        return Result.of(InteractionResult.PASS);

    }



    @Override

    public int getMaxStackSize() {

        return MAX_STACK_SIZE;

    }



    @Override

    public CompoundTag getUpdateTag() {

        return this.saveWithoutMetadata();

    }



    @Override

    public Packet<ClientGamePacketListener> getUpdatePacket() {

        return ClientboundBlockEntityDataPacket.create(this);

    }

}