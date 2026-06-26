package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.content.ShapedDoughContent;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.jetbrains.annotations.Nullable;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

public class MoldBlockEntity extends BlockEntity{
    private static final String CONTENT_KEY = "shaped_dough";

    /** 当前模具中的定型面团 */
    @Nullable protected ShapedDoughContent shapedDough;

    public MoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MOLD.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (shapedDough != null) {
            nbt.putString(CONTENT_KEY, Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(shapedDough)).toString());
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (nbt.contains(CONTENT_KEY, Tag.TAG_STRING)) {
            Content content = TWRegistries.CONTENT.get().getValue(ResourceLocation.tryParse(nbt.getString(CONTENT_KEY)));
            if (content instanceof ShapedDoughContent dough) {
                this.shapedDough = dough;
            }
        }
    }

    /**
     * 检查物品是否能作为配方输入
     * @param stack 要输入的物品堆栈
     * @return 是否可以作为配方输入
     */
    private boolean isValidMoldInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return ShapedDoughContent.fromBaseGet(stack, getBlockState()) != null;
    }

    /**
     * 尝试向模具中添加一个面团。
     *
     * @param stack 要添加的堆栈
     * @return 是否添加成功，堆栈不是合适的面团或者模具已经拥有定型面团时则失败。
     */
    public boolean addDough(ItemStack stack) {
        if (shapedDough != null) {
            return false;
        }

        ShapedDoughContent content = ShapedDoughContent.fromBaseGet(stack, getBlockState());
        if (content == null) {
            return false;
        }

        setShapedDough(content);
        return true;
    }

    /**
     * 清空当前模具中的面团并返回原始物品。
     *
     * @return 当前定型面团的原始堆栈，当不存在定型面团时返回空物品堆栈
     */
    public ItemStack getAndClearResultStack() {
        if (shapedDough != null) {
            ItemStack res = shapedDough.getOriginalDough().getDefaultInstance();
            shapedDough = null;
            return res;
        }

        return ItemStack.EMPTY;
    }

    public void setShapedDough(@Nullable ShapedDoughContent shapedDough) {
        this.shapedDough = shapedDough;
    }

    public @Nullable ShapedDoughContent getShapedDough() {
        return shapedDough;
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
