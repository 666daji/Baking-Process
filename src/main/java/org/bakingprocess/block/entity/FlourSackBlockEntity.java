package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
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
 * 粉尘袋方块实体，专注于粉尘袋特殊方法
 */
public class FlourSackBlockEntity extends ComplexFoodBlockEntity {
    public static final int DEFAULT_FLOUR_COLOR = 0xFFFFFF; // 默认白色

    public FlourSackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FLOUR_SACK.get(), pos, state);
    }

    /**
     * 获取指定位置的粉尘颜色
     */
    public int getFlourColor(int index) {
        ItemStack sackStack = getSackStack(index);
        if (!sackStack.isEmpty()) {
            Optional<ItemStack> flourStack = FlourSackItem.getBundledStack(sackStack);
            if (flourStack.isPresent() && flourStack.get().getItem() instanceof FlourItem flourItem) {
                return flourItem.getColor();
            }
        }
        return DEFAULT_FLOUR_COLOR;
    }

    /**
     * 获取所有粉尘袋堆叠的颜色数组
     */
    public int[] getAllFlourColors() {
        int count = getAllStack().size();
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = getFlourColor(i);
        }
        return colors;
    }

    /**
     * 获取指定位置的粉尘袋物品堆栈
     */
    public ItemStack getSackStack(int index) {
        DataComponentPatch changes = getPatchAt(index);
        if (changes != null && !changes.isEmpty()) {
            // 从方块物品创建粉尘袋堆栈，并应用组件更改
            ItemStack sackItem = new ItemStack(getBlockState().getBlock().asItem());
            sackItem.applyComponents(changes);
            return sackItem;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取所有内容物（每个粉尘袋内的面粉堆栈）
     */
    public NonNullList<ItemStack> getAllContents() {
        NonNullList<ItemStack> contents = NonNullList.create();
        int count = getAllStack().size();
        for (int i = 0; i < count; i++) {
            ItemStack sack = getSackStack(i);
            if (!sack.isEmpty()) {
                FlourSackItem.getBundledStack(sack).ifPresent(contents::add);
            }
        }
        return contents;
    }

    /**
     * 检查指定索引是否有效
     */
    public boolean isValidSackIndex(int index) {
        return index >= 0 && index < getAllStack().size();
    }

    /**
     * 获取粉尘袋总占用空间（所有袋内面粉总数）
     */
    public int getTotalOccupancy() {
        int total = 0;
        int count = getAllStack().size();
        for (int i = 0; i < count; i++) {
            ItemStack sack = getSackStack(i);
            if (!sack.isEmpty()) {
                total += FlourSackItem.getBundledStack(sack)
                        .map(ItemStack::getCount)
                        .orElse(0);
            }
        }
        return total;
    }

    /**
     * 获取最大容量（每个粉尘袋最多16个粉尘）
     */
    public int getMaxCapacity() {
        return 16 * getAllStack().size();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return this.saveWithoutMetadata(registryLookup);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}