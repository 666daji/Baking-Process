package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
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

    /** 褰撳墠妯″叿涓�鐨勫畾鍨嬮潰鍥� */
    @Nullable protected ShapedDoughContent shapedDough;

    public MoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MOLD.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        if (shapedDough != null) {
            nbt.putString(CONTENT_KEY, Objects.requireNonNull(TWRegistries.CONTENT.getKey(shapedDough)).toString());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        if (nbt.contains(CONTENT_KEY, Tag.TAG_STRING)) {
            Content content = TWRegistries.CONTENT.get(ResourceLocation.tryParse(nbt.getString(CONTENT_KEY)));
            if (content instanceof ShapedDoughContent dough) {
                this.shapedDough = dough;
            }
        }
    }

    /**
     * 妫�鏌ョ墿鍝佹槸鍚﹁兘浣滀负閰嶆柟杈撳叆
     * @param stack 瑕佽緭鍏ョ殑鐗╁搧鍫嗘爤
     * @return 鏄�鍚﹀彲浠ヤ綔涓洪厤鏂硅緭鍏�
     */
    private boolean isValidMoldInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return ShapedDoughContent.fromBaseGet(stack, getBlockState()) != null;
    }

    /**
     * 灏濊瘯鍚戞ā鍏蜂腑娣诲姞涓�涓�闈㈠洟銆?
     *
     * @param stack 瑕佹坊鍔犵殑鍫嗘爤
     * @return 鏄�鍚︽坊鍔犳垚鍔燂紝鍫嗘爤涓嶆槸鍚堥�傜殑闈㈠洟鎴栬�呮ā鍏峰凡缁忔嫢鏈夊畾鍨嬮潰鍥㈡椂鍒欏け璐ャ�?
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
     * 娓呯┖褰撳墠妯″叿涓�鐨勯潰鍥㈠苟杩斿洖鍘熷�嬬墿鍝併�?
     *
     * @return 褰撳墠瀹氬瀷闈㈠洟鐨勫師濮嬪爢鏍堬紝褰撲笉瀛樺湪瀹氬瀷闈㈠洟鏃惰繑鍥炵┖鐗╁搧鍫嗘爤
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
