package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
 * 盆方块实体，支持揉面流程，实现Inventory接口
 */
public class PotsBlockEntity extends BlockEntity implements Container {

    /** 当前揉面流程 */
    private KneadingProcess<PotsBlockEntity> kneadingProcess;

    /** 存储最终产品的槽位 */
    private final NonNullList<ItemStack> inventory;

    public PotsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.POTS.get(), pos, state);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        this.kneadingProcess = new KneadingProcess<>();
    }

    /**
     * 尝试揉面交互。
     * @param state 方块状态
     * @param world 世界
     * @param pos 方块位置
     * @param player 交互的玩家
     * @param hand 交互的手
     * @param hit 交互的操作
     * @return 交互的结果
     */
    public InteractionResult tryKnead(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 如果没有流程，检查是否手持面粉开始新流程
        if (!kneadingProcess.isActive()) {
            if (KneadingProcess.isCanAddFlour(heldStack)) {
                kneadingProcess.start(world, this);
            } else {
                return InteractionResult.PASS;
            }
        }

        // 执行流程步骤
        return kneadingProcess.executeStep(this, state, world, pos, player, hand, hit);
    }

    /**
     * 获取当前揉面流程
     */
    public KneadingProcess<PotsBlockEntity> getKneadingProcess() {
        return kneadingProcess;
    }

    /**
     * 检查是否正在进行揉面流程
     */
    public boolean isKneadingInProgress() {
        return kneadingProcess != null && kneadingProcess.isActive();
    }

    /**
     * 获取当前步骤
     */
    public String getCurrentStep() {
        return kneadingProcess != null ? kneadingProcess.getCurrentStepId() : null;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);

        // 保存库存
        ContainerHelper.saveAllItems(nbt, inventory, registryLookup);

        // 保存揉面流程
        if (kneadingProcess != null) {
            CompoundTag processNbt = new CompoundTag();
            kneadingProcess.writeToNbt(processNbt, registryLookup);
            nbt.put("kneading_process", processNbt);
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);

        // 读取库存
        this.inventory.clear();
        ContainerHelper.loadAllItems(nbt, inventory, registryLookup);

        // 读取揉面流程
        if (nbt.contains("kneading_process")) {
            kneadingProcess = new KneadingProcess<>();
            kneadingProcess.readFromNbt(nbt.getCompound("kneading_process"), registryLookup);
        }
    }

    // ============ Inventory接口实现 ============

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
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return this.saveWithoutMetadata(registryLookup);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}