package org.bakingprocess.block.entity;



import net.minecraft.core.BlockPos;

import net.minecraft.core.NonNullList;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.protocol.Packet;

import net.minecraft.network.protocol.game.ClientGamePacketListener;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.state.BlockState;

import org.bakingprocess.item.FlourItem;

import org.bakingprocess.item.FlourSackItem;

import org.bakingprocess.registry.ModBlockEntityTypes;

import org.dfood.block.entity.ComplexFoodBlockEntity;



import java.util.Optional;



/**

 * 绮夊皹琚嬫柟鍧楀疄浣擄紝涓撴敞浜庣矇灏樿�嬬壒娈婃柟娉�

 */

public class FlourSackBlockEntity extends ComplexFoodBlockEntity {

    public static final int DEFAULT_FLOUR_COLOR = 0xFFFFFF; // 榛樿�ょ櫧鑹�



    public FlourSackBlockEntity(BlockPos pos, BlockState state) {

        super(ModBlockEntityTypes.FLOUR_SACK.get(), pos, state);

    }



    /**

     * 鑾峰彇鎸囧畾浣嶇疆鐨勭矇灏橀�滆�?
     */

    public int getFlourColor(int index) {

        CompoundTag nbt = getNbtAt(index);

        if (nbt != null && !nbt.isEmpty()) {

            Optional<ItemStack> sack = FlourSackItem.getBundledStack(getSackStack(index));

            if (sack.isPresent() && sack.get().getItem() instanceof FlourItem flourItem) {

                return flourItem.getColor();

            }

        }

        return DEFAULT_FLOUR_COLOR;

    }



    /**

     * 鑾峰彇鎵�鏈夌矇灏樿�嬪爢鍙犵殑棰滆壊鏁扮�?
     */

    public int[] getAllFlourColors() {

        int[] colors = new int[getNbtCount()];

        for (int i = 0; i < getNbtCount(); i++) {

            colors[i] = getFlourColor(i);

        }

        return colors;

    }



    /**

     * 鑾峰彇鎸囧畾浣嶇疆鐨勭矇灏樿�嬬墿鍝佸爢鏍�

     */

    public ItemStack getSackStack(int index) {

        CompoundTag nbt = getNbtAt(index);

        if (nbt != null && !nbt.isEmpty()) {

            // 浠庡瓨鍌ㄧ殑NBT鍒涘缓瀹屾暣鐨勭矇灏樿�嬬墿鍝�

            ItemStack sackItem = new ItemStack(getBlockState().getBlock().asItem());



            // 澶嶅埗鍘熷�婲BT鏁版嵁

            sackItem.setTag(nbt.copy());

            return sackItem;

        }

        return ItemStack.EMPTY;

    }



    /**

     * 鑾峰彇鎵�鏈夊唴瀹圭墿

     */

    public NonNullList<ItemStack> getAllContents() {

        NonNullList<ItemStack> contents = NonNullList.create();

        for (int i = 0; i < getNbtCount(); i++) {

            CompoundTag nbt = getNbtAt(i);

            if (nbt != null && !nbt.isEmpty()) {

                contents.add(ItemStack.of(nbt));

            }

        }

        return contents;

    }



    /**

     * 妫�鏌ユ寚瀹氱储寮曟槸鍚︽湁鏁?
     */

    public boolean isValidSackIndex(int index) {

        return index >= 0 && index < getNbtCount();

    }



    /**

     * 鑾峰彇绮夊皹琚嬫�诲崰鐢ㄧ┖闂?
     */

    public int getTotalOccupancy() {

        int total = 0;

        for (int i = 0; i < getNbtCount(); i++) {

            CompoundTag nbt = getNbtAt(i);

            if (nbt != null && !nbt.isEmpty()) {

                ItemStack stack = ItemStack.of(nbt);

                if (!stack.isEmpty()) {

                    total += stack.getCount();

                }

            }

        }

        return total;

    }



    /**

     * 鑾峰彇鏈�澶у�归噺锛堟瘡涓�绮夊皹琚嬫渶澶?6涓�绮夊皹锛�

     */

    public int getMaxCapacity() {

        return 16 * getNbtCount();

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