package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.recipe.GrindingRecipe;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModRecipeTypes;
import org.bakingprocess.util.BakingProcessUtils;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.animation.EnhancedAnimationState;

public class GrindingStoneBlockEntity extends BlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int OUTPUT_SLOT_INDEX = 1;
    public static final int DEFAULT_GRIND_TIME = 200;
    private static final int MIN_ENERGY_ADD_INTERVAL = 10;

    protected NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private int lastEnergyAddTime = 0;
    int energy;
    static final int MAX_ENERGY = 1000;
    int grindingTime;
    int grindingTimeTotal;

    public final EnhancedAnimationState grindingAnimationState = new EnhancedAnimationState();
    protected int age;

    private final RecipeManager.CachedCheck<RecipeInput, GrindingRecipe> matchGetter;
    @Nullable private RecipeHolder<?> lastRecipe;

    public GrindingStoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.GRINDING_STONE.get(), pos, state);
        this.matchGetter = RecipeManager.createCheck(ModRecipeTypes.GRINDING.get());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.inventory, registryLookup);
        this.energy = nbt.getInt("Energy");
        this.grindingTime = nbt.getInt("GrindingTime");
        this.grindingTimeTotal = nbt.getInt("GrindingTimeTotal");
        this.age = nbt.getInt("Age");
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        ContainerHelper.saveAllItems(nbt, this.inventory, registryLookup);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("GrindingTime", this.grindingTime);
        nbt.putInt("GrindingTimeTotal", this.grindingTimeTotal);
        nbt.putInt("Age", this.age);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // 输入槽(0)可以从任何面插入，输出槽(1)可以从任何面提取
        return new int[]{INPUT_SLOT_INDEX, OUTPUT_SLOT_INDEX};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == INPUT_SLOT_INDEX;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT_INDEX;
    }

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT_INDEX && !ItemStack.matches(stack, this.inventory.get(slot))) {
            // 输入物品发生变化，重置研磨进度
            this.resetGrindingProgress();
        }
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }

    public Item getExpectedOutput(){
        RecipeHolder<GrindingRecipe> recipe = this.matchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(this), this.level).orElse(null);
        if (recipe != null) {
            return recipe.value().getResultItem(null).getItem();
        }
        return ItemStack.EMPTY.getItem();
    }

    /**
     * 获取当前输入物品对应的配方
     */
    @Nullable
    public RecipeHolder<GrindingRecipe> getCurrentRecipe() {
        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        if (inputStack.isEmpty()) {
            return null;
        }
        Container tempInventory = new SimpleContainer(inputStack);
        return this.matchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(tempInventory), this.level).orElse(null);
    }

    /**
     * 检查物品是否可以作为任何研磨配方的输入
     */
    private boolean isValidGrindingInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Container tempInventory = new SimpleContainer(stack);
        return this.matchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(tempInventory), this.level).isPresent();
    }

    /**
     * 检查当前输入物品是否可以研磨（数量足够）
     */
    public boolean canGrindCurrentInput() {
        RecipeHolder<GrindingRecipe> recipe = getCurrentRecipe();
        if (recipe == null) {
            return false;
        }

        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        return inputStack.getCount() >= recipe.value().getInputCount();
    }

    /**
     * 尝试将物品添加到输入槽
     */
    public AddInputResult addInput(ItemStack stack, Player player) {
        if (!isValidGrindingInput(stack)) {
            return AddInputResult.INVALID;
        }

        ItemStack inputSlot = this.inventory.get(INPUT_SLOT_INDEX);

        // 如果输入槽不为空且物品不同，先返还原有物品
        if (!inputSlot.isEmpty() && !ItemStack.isSameItem(inputSlot, stack)) {
            returnItemToPlayer(inputSlot, player);
            this.setItem(INPUT_SLOT_INDEX, ItemStack.EMPTY);
            inputSlot = ItemStack.EMPTY;
        }

        // 获取配方信息
        Container tempInventory = new SimpleContainer(stack);
        RecipeHolder<GrindingRecipe> recipe = this.matchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(tempInventory), this.level).orElse(null);
        if (recipe == null) {
            return AddInputResult.INVALID;
        }

        int requiredCount = recipe.value().getInputCount();
        int playerStackCount = stack.getCount();

        // 如果输入槽为空，尝试一次性添加所需数量的物品
        if (inputSlot.isEmpty()) {
            int amountToAdd = Math.min(requiredCount, playerStackCount);
            if (amountToAdd < requiredCount) {
                return AddInputResult.NOT_ENOUGH; // 玩家手中的物品数量不足
            }

            ItemStack newInput = stack.copy();
            newInput.setCount(amountToAdd);
            this.setItem(INPUT_SLOT_INDEX, newInput);

            // 消耗玩家物品
            if (!player.isCreative()) {
                stack.shrink(amountToAdd);
            }

            return AddInputResult.SUCCESS;
        }
        // 如果输入槽不为空且物品相同，尝试补齐到配方的整数倍
        else if (ItemStack.isSameItem(inputSlot, stack)) {
            int currentCount = inputSlot.getCount();
            int remainder = currentCount % requiredCount;
            int neededToComplete = (remainder == 0) ? 0 : (requiredCount - remainder);

            // 如果已经是整数倍，检查是否可以再添加一组
            if (neededToComplete == 0) {
                int maxAddable = this.getMaxStackSize() - currentCount;
                int amountToAdd = Math.min(requiredCount, Math.min(maxAddable, playerStackCount));

                if (amountToAdd > 0) {
                    inputSlot.grow(amountToAdd);
                    this.setItem(INPUT_SLOT_INDEX, inputSlot);

                    if (!player.isCreative()) {
                        stack.shrink(amountToAdd);
                    }
                    return AddInputResult.SUCCESS;
                } else {
                    return AddInputResult.FULL;
                }
            }
            // 尝试补齐到整数倍
            else {
                if (playerStackCount >= neededToComplete) {
                    inputSlot.grow(neededToComplete);
                    this.setItem(INPUT_SLOT_INDEX, inputSlot);

                    if (!player.isCreative()) {
                        stack.shrink(neededToComplete);
                    }
                    return AddInputResult.SUCCESS;
                } else {
                    return AddInputResult.NOT_ENOUGH;
                }
            }
        }

        return AddInputResult.INVALID;
    }

    /**
     * 将物品返还给玩家
     */
    private void returnItemToPlayer(ItemStack stack, Player player) {
        if (stack.isEmpty()) return;

        if (!player.getInventory().add(stack.copy())) {
            // 如果玩家背包已满，掉落物品
            ItemEntity itemEntity = new ItemEntity(level,
                    player.getX(), player.getY(), player.getZ(), stack.copy());
            level.addFreshEntity(itemEntity);
        }
        this.setItem(INPUT_SLOT_INDEX, ItemStack.EMPTY);
    }

    /**
     * 清空输入槽并将物品返还给玩家
     */
    public void returnInputToPlayer(Player player) {
        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        if (!inputStack.isEmpty()) {
            returnItemToPlayer(inputStack, player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
    }

    @Override
    public void fillStackedContents(StackedContents finder) {
        for (ItemStack stack : this.inventory) {
            finder.accountStack(stack);
        }
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        this.lastRecipe = recipe;
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return this.lastRecipe;
    }

    /**
     * 重置研磨进度
     */
    public void resetGrindingProgress() {
        this.grindingTime = 0;
        this.grindingTimeTotal = 0;
        this.setChanged();
    }

    /**
     * 计算完成剩余研磨所需的能量
     */
    public int calculateRequiredEnergy() {
        int inputCount = this.inventory.get(INPUT_SLOT_INDEX).getCount();
        if (inputCount == 0) {
            return 0;
        }

        RecipeHolder<GrindingRecipe> recipe = getCurrentRecipe();
        if (recipe == null) {
            return 0;
        }

        int grindingTimeForRecipe = recipe.value().getGrindingTime();
        int requiredCount = recipe.value().getInputCount();

        // 计算可以研磨的次数
        int grindTimes = inputCount / requiredCount;
        if (grindTimes == 0) {
            return 0;
        }

        // 当前正在研磨的物品还需要 (grindingTimeForRecipe - craftTime) 能量
        // 剩余物品每个配方需要 grindingTimeForRecipe 能量
        int remainingEnergyForCurrent = Math.max(0, grindingTimeForRecipe - this.grindingTime);
        int energyForRemainingItems = (grindTimes - 1) * grindingTimeForRecipe;

        return remainingEnergyForCurrent + energyForRemainingItems;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, GrindingStoneBlockEntity blockEntity) {
        blockEntity.age++;
        if (blockEntity.age == Integer.MAX_VALUE) {
            blockEntity.age = 0;
        }

        // 如果当前有能量且可以研磨，则继续或开始研磨
        if (blockEntity.energy > 0 && blockEntity.canGrind()) {
            // 如果当前没有研磨进度，则初始化
            if (blockEntity.grindingTime == 0) {
                RecipeHolder<GrindingRecipe> recipe = blockEntity.getCurrentRecipe();
                if (recipe != null) {
                    blockEntity.grindingTimeTotal = recipe.value().getGrindingTime();
                }
            }

            // 消耗能量并增加进度
            blockEntity.energy--;
            blockEntity.grindingTime++;

            // 检查是否研磨完成
            if (blockEntity.grindingTime >= blockEntity.grindingTimeTotal) {
                blockEntity.resetGrindingProgress();
                blockEntity.grindItem();
            }
        }

        // 尝试给予产物
        if (!world.isClientSide && blockEntity.age % 10 == 0) {
            ItemStack outputStack = blockEntity.getItem(OUTPUT_SLOT_INDEX);
            if (!outputStack.isEmpty()) {
                blockEntity.ejectOutputItem(world, pos);
            }
        }

        blockEntity.setChanged();
        blockEntity.sync();
    }

    /**
     * 将输出槽的物品向上喷出
     */
    private void ejectOutputItem(Level world, BlockPos pos) {
        ItemStack outputStack = this.getItem(OUTPUT_SLOT_INDEX);
        if (outputStack.isEmpty()) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        ItemEntity itemEntity = new ItemEntity(world, x, y, z, outputStack.copy());

        itemEntity.setDeltaMovement(0, 0.3, 0);
        itemEntity.setPickUpDelay(10);

        world.addFreshEntity(itemEntity);
        this.setItem(OUTPUT_SLOT_INDEX, ItemStack.EMPTY);
        this.setChanged();
    }

    private boolean canGrind() {
        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        if (inputStack.isEmpty()) {
            return false;
        }

        RecipeHolder<GrindingRecipe> recipe = this.getCurrentRecipe();
        if (recipe == null) {
            return false;
        }

        // 检查输入物品数量是否足够
        if (inputStack.getCount() < recipe.value().getInputCount()) {
            return false;
        }

        ItemStack output = recipe.value().getResultItem(null);
        if (output.isEmpty()) {
            return false;
        }

        ItemStack outputSlot = this.inventory.get(OUTPUT_SLOT_INDEX);
        if (outputSlot.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItem(outputSlot, output)) {
            return false;
        }

        int resultCount = outputSlot.getCount() + output.getCount();
        return resultCount <= getMaxStackSize() && resultCount <= outputSlot.getMaxStackSize();
    }

    private void grindItem() {
        RecipeHolder<GrindingRecipe> recipe = this.getCurrentRecipe();
        if (recipe != null && this.canGrind()) {
            ItemStack input = this.inventory.get(INPUT_SLOT_INDEX);
            ItemStack output = recipe.value().assemble(BakingProcessUtils.createRecipeInput(this), null);
            ItemStack outputSlot = this.inventory.get(OUTPUT_SLOT_INDEX);
            int requiredCount = recipe.value().getInputCount();

            if (outputSlot.isEmpty()) {
                this.inventory.set(OUTPUT_SLOT_INDEX, output);
            } else if (ItemStack.isSameItem(outputSlot, output)) {
                outputSlot.grow(output.getCount());
            }

            // 消耗配方所需的物品数量
            input.shrink(requiredCount);

            // 记录使用的配方
            this.setRecipeUsed(recipe);
        }
    }

    public boolean isGrinding() {
        return this.grindingTime > 0 && this.energy > 0;
    }

    public int getGrindingTimeTotal() {
        return this.grindingTimeTotal;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
        setChanged();
    }

    /**
     * 尝试添加能量，考虑时间间隔限制
     */
    public boolean tryAddEnergy(int amount) {
        if (this.age - this.lastEnergyAddTime < MIN_ENERGY_ADD_INTERVAL) {
            return false;
        }

        int requiredEnergy = this.calculateRequiredEnergy();
        if (this.getEnergy() >= requiredEnergy) {
            return false;
        }

        int energyToAdd = Math.min(amount, requiredEnergy - this.getEnergy());
        this.addEnergy(energyToAdd);
        this.lastEnergyAddTime = this.age;

        return true;
    }

    public void addEnergy(int energy) {
        this.energy = Math.min(this.energy + energy, MAX_ENERGY);
        setChanged();
    }

    public void consumeEnergy(int energy) {
        this.energy = Math.max(0, this.energy - energy);
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

    public int getAge() {
        return this.age;
    }

    public boolean canPlaySound(){
        return this.energy >= 20;
    }

    public void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /**
     * 获取当前研磨进度的百分比
     */
    public float getGrindingProgress() {
        if (grindingTimeTotal != 0) {
            return (float) grindingTime / grindingTimeTotal * 100.0f;
        }
        return 0.0f;
    }

    /**
     * 获取所有物品（用于破坏方块时掉落）
     */
    public NonNullList<ItemStack> getItemsToDrop() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    public enum AddInputResult {
        SUCCESS,
        FULL,
        INVALID,
        NOT_ENOUGH
    }
}