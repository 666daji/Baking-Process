package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bakingprocess.block.process.KneadingProcess;
import org.bakingprocess.registry.ModBlockEntityTypes;

/**
 * 鐩嗘柟鍧楀疄浣擄紝鏀�鎸佹弶闈㈡祦绋嬶紝瀹炵幇Inventory鎺ュ彛
 */
public class PotsBlockEntity extends BlockEntity implements Container {

    /** 褰撳墠鎻夐潰娴佺▼ */
    private KneadingProcess<PotsBlockEntity> kneadingProcess;

    /** 瀛樺偍鏈�缁堜骇鍝佺殑妲戒綅 */
    private final NonNullList<ItemStack> inventory;

    public PotsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.POTS.get(), pos, state);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        this.kneadingProcess = new KneadingProcess<>();
    }

    /**
     * 灏濊瘯鎻夐潰浜や簰銆?
     * @param state 鏂瑰潡鐘舵�?
     * @param world 涓栫晫
     * @param pos 鏂瑰潡浣嶇疆
     * @param player 浜や簰鐨勭帺瀹?
     * @param hand 浜や簰鐨勬墜
     * @param hit 浜や簰鐨勬搷浣?
     * @return 浜や簰鐨勭粨鏋?
     */
    public InteractionResult tryKnead(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 濡傛灉娌℃湁娴佺▼锛屾��鏌ユ槸鍚︽墜鎸侀潰绮夊紑濮嬫柊娴佺▼
        if (!kneadingProcess.isActive()) {
            if (KneadingProcess.isCanAddFlour(heldStack)) {
                kneadingProcess.start(world, this);
            } else {
                return InteractionResult.PASS;
            }
        }

        // 鎵ц�屾祦绋嬫�ラ��
        return kneadingProcess.executeStep(this, state, world, pos, player, hand, hit);
    }

    /**
     * 鑾峰彇褰撳墠鎻夐潰娴佺▼
     */
    public KneadingProcess<PotsBlockEntity> getKneadingProcess() {
        return kneadingProcess;
    }

    /**
     * 妫�鏌ユ槸鍚︽�ｅ湪杩涜�屾弶闈㈡祦绋?
     */
    public boolean isKneadingInProgress() {
        return kneadingProcess != null && kneadingProcess.isActive();
    }

    /**
     * 鑾峰彇褰撳墠姝ラ��
     */
    public String getCurrentStep() {
        return kneadingProcess != null ? kneadingProcess.getCurrentStepId() : null;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        // 淇濆瓨搴撳瓨
        ContainerHelper.saveAllItems(nbt, inventory);

        // 淇濆瓨鎻夐潰娴佺▼
        if (kneadingProcess != null) {
            CompoundTag processNbt = new CompoundTag();
            kneadingProcess.writeToNbt(processNbt);
            nbt.put("kneading_process", processNbt);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        // 璇诲彇搴撳瓨
        this.inventory.clear();
        ContainerHelper.loadAllItems(nbt, inventory);

        // 璇诲彇鎻夐潰娴佺▼
        if (nbt.contains("kneading_process")) {
            kneadingProcess = new KneadingProcess<>();
            kneadingProcess.readFromNbt(nbt.getCompound("kneading_process"));
        }
    }

    // ============ Inventory鎺ュ彛瀹炵幇 ============

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= inventory.size()) {
            return ItemStack.EMPTY;
        }
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);


        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(inventory, slot);

        setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
        setChanged();
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