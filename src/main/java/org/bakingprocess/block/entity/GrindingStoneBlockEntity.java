package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.recipe.GrindingRecipe;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModRecipeTypes;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.animation.EnhancedAnimationState;

public class GrindingStoneBlockEntity extends BlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
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

    private final RecipeManager.CachedCheck<Container, ? extends GrindingRecipe> matchGetter;
    @Nullable private Recipe<?> lastRecipe;

    public GrindingStoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.GRINDING_STONE.get(), pos, state);
        this.matchGetter = RecipeManager.createCheck(ModRecipeTypes.GRINDING.get());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.grindingTime = nbt.getInt("GrindingTime");
        this.grindingTimeTotal = nbt.getInt("GrindingTimeTotal");
        this.age = nbt.getInt("Age");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("GrindingTime", this.grindingTime);
        nbt.putInt("GrindingTimeTotal", this.grindingTimeTotal);
        nbt.putInt("Age", this.age);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // 杈撳叆妲?0)鍙�浠ヤ粠浠讳綍闈㈡彃鍏ワ紝杈撳嚭妲�(1)鍙�浠ヤ粠浠讳綍闈㈡彁鍙�
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
            // 杈撳叆鐗╁搧鍙戠敓鍙樺寲锛岄噸缃�鐮旂（杩涘�?
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
        GrindingRecipe recipe = this.matchGetter.getRecipeFor(this, this.level).orElse(null);
        if (recipe != null) {
            return recipe.getResultItem(null).getItem();
        }
        return ItemStack.EMPTY.getItem();
    }

    /**
     * 鑾峰彇褰撳墠杈撳叆鐗╁搧瀵瑰簲鐨勯厤鏂?
     */
    @Nullable
    public GrindingRecipe getCurrentRecipe() {
        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        if (inputStack.isEmpty()) {
            return null;
        }
        Container tempInventory = new SimpleContainer(inputStack);
        return this.matchGetter.getRecipeFor(tempInventory, this.level).orElse(null);
    }

    /**
     * 妫�鏌ョ墿鍝佹槸鍚﹀彲浠ヤ綔涓轰换浣曠爺纾ㄩ厤鏂圭殑杈撳叆
     */
    private boolean isValidGrindingInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Container tempInventory = new SimpleContainer(stack);
        return this.matchGetter.getRecipeFor(tempInventory, this.level).isPresent();
    }

    /**
     * 妫�鏌ュ綋鍓嶈緭鍏ョ墿鍝佹槸鍚﹀彲浠ョ爺纾�锛堟暟閲忚冻澶燂�?
     */
    public boolean canGrindCurrentInput() {
        GrindingRecipe recipe = getCurrentRecipe();
        if (recipe == null) {
            return false;
        }

        ItemStack inputStack = this.inventory.get(INPUT_SLOT_INDEX);
        return inputStack.getCount() >= recipe.getInputCount();
    }

    /**
     * 灏濊瘯灏嗙墿鍝佹坊鍔犲埌杈撳叆妲?
     */
    public AddInputResult addInput(ItemStack stack, Player player) {
        if (!isValidGrindingInput(stack)) {
            return AddInputResult.INVALID;
        }

        ItemStack inputSlot = this.inventory.get(INPUT_SLOT_INDEX);

        // 濡傛灉杈撳叆妲戒笉涓虹┖涓旂墿鍝佷笉鍚岋紝鍏堣繑杩樺師鏈夌墿鍝?
        if (!inputSlot.isEmpty() && !ItemStack.isSameItem(inputSlot, stack)) {
            returnItemToPlayer(inputSlot, player);
            this.setItem(INPUT_SLOT_INDEX, ItemStack.EMPTY);
            inputSlot = ItemStack.EMPTY;
        }

        // 鑾峰彇閰嶆柟淇℃伅
        Container tempInventory = new SimpleContainer(stack);
        GrindingRecipe recipe = this.matchGetter.getRecipeFor(tempInventory, this.level).orElse(null);
        if (recipe == null) {
            return AddInputResult.INVALID;
        }

        int requiredCount = recipe.getInputCount();
        int playerStackCount = stack.getCount();

        // 濡傛灉杈撳叆妲戒负绌猴紝灏濊瘯涓�娆℃�ф坊鍔犳墍闇�鏁伴噺鐨勭墿鍝?
        if (inputSlot.isEmpty()) {
            int amountToAdd = Math.min(requiredCount, playerStackCount);
            if (amountToAdd < requiredCount) {
                return AddInputResult.NOT_ENOUGH; // 鐜╁�舵墜涓�鐨勭墿鍝佹暟閲忎笉瓒?
            }

            ItemStack newInput = stack.copy();
            newInput.setCount(amountToAdd);
            this.setItem(INPUT_SLOT_INDEX, newInput);

            // 娑堣�楃帺瀹剁墿鍝?
            if (!player.isCreative()) {
                stack.shrink(amountToAdd);
            }

            return AddInputResult.SUCCESS;
        }
        // 濡傛灉杈撳叆妲戒笉涓虹┖涓旂墿鍝佺浉鍚岋紝灏濊瘯琛ラ綈鍒伴厤鏂圭殑鏁存暟鍊?
        else if (ItemStack.isSameItem(inputSlot, stack)) {
            int currentCount = inputSlot.getCount();
            int remainder = currentCount % requiredCount;
            int neededToComplete = (remainder == 0) ? 0 : (requiredCount - remainder);

            // 濡傛灉宸茬粡鏄�鏁存暟鍊嶏紝妫�鏌ユ槸鍚﹀彲浠ュ啀娣诲姞涓�缁?
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
            // 灏濊瘯琛ラ綈鍒版暣鏁板�?
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
     * 灏嗙墿鍝佽繑杩樼粰鐜╁��
     */
    private void returnItemToPlayer(ItemStack stack, Player player) {
        if (stack.isEmpty()) return;

        if (!player.getInventory().add(stack.copy())) {
            // 濡傛灉鐜╁�惰儗鍖呭凡婊★紝鎺夎惤鐗╁�?
            ItemEntity itemEntity = new ItemEntity(level,
                    player.getX(), player.getY(), player.getZ(), stack.copy());
            level.addFreshEntity(itemEntity);
        }
        this.setItem(INPUT_SLOT_INDEX, ItemStack.EMPTY);
    }

    /**
     * 娓呯┖杈撳叆妲藉苟灏嗙墿鍝佽繑杩樼粰鐜╁��
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
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        this.lastRecipe = recipe;
    }

    @Override
    public @Nullable Recipe<?> getRecipeUsed() {
        return this.lastRecipe;
    }

    /**
     * 閲嶇疆鐮旂（杩涘害
     */
    public void resetGrindingProgress() {
        this.grindingTime = 0;
        this.grindingTimeTotal = 0;
        this.setChanged();
    }

    /**
     * 璁＄畻瀹屾垚鍓╀綑鐮旂（鎵�闇�鐨勮兘閲?
     */
    public int calculateRequiredEnergy() {
        int inputCount = this.inventory.get(INPUT_SLOT_INDEX).getCount();
        if (inputCount == 0) {
            return 0;
        }

        GrindingRecipe recipe = getCurrentRecipe();
        if (recipe == null) {
            return 0;
        }

        int grindingTimeForRecipe = recipe.getGrindingTime();
        int requiredCount = recipe.getInputCount();

        // 璁＄畻鍙�浠ョ爺纾ㄧ殑娆℃�?
        int grindTimes = inputCount / requiredCount;
        if (grindTimes == 0) {
            return 0;
        }

        // 褰撳墠姝ｅ湪鐮旂（鐨勭墿鍝佽繕闇�瑕?(grindingTimeForRecipe - craftTime) 鑳介噺
        // 鍓╀綑鐗╁搧姣忎釜閰嶆柟闇�瑕?grindingTimeForRecipe 鑳介噺
        int remainingEnergyForCurrent = Math.max(0, grindingTimeForRecipe - this.grindingTime);
        int energyForRemainingItems = (grindTimes - 1) * grindingTimeForRecipe;

        return remainingEnergyForCurrent + energyForRemainingItems;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, GrindingStoneBlockEntity blockEntity) {
        blockEntity.age++;
        if (blockEntity.age == Integer.MAX_VALUE) {
            blockEntity.age = 0;
        }

        // 濡傛灉褰撳墠鏈夎兘閲忎笖鍙�浠ョ爺纾�锛屽垯缁х画鎴栧紑濮嬬爺纾?
        if (blockEntity.energy > 0 && blockEntity.canGrind()) {
            // 濡傛灉褰撳墠娌℃湁鐮旂（杩涘害锛屽垯鍒濆�嬪�?
            if (blockEntity.grindingTime == 0) {
                GrindingRecipe recipe = blockEntity.getCurrentRecipe();
                if (recipe != null) {
                    blockEntity.grindingTimeTotal = recipe.getGrindingTime();
                }
            }

            // 娑堣�楄兘閲忓苟澧炲姞杩涘害
            blockEntity.energy--;
            blockEntity.grindingTime++;

            // 妫�鏌ユ槸鍚︾爺纾ㄥ畬鎴?
            if (blockEntity.grindingTime >= blockEntity.grindingTimeTotal) {
                blockEntity.resetGrindingProgress();
                blockEntity.grindItem();
            }
        }

        // 灏濊瘯缁欎簣浜х墿
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
     * 灏嗚緭鍑烘Ы鐨勭墿鍝佸悜涓婂柗鍑?
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

        GrindingRecipe recipe = this.getCurrentRecipe();
        if (recipe == null) {
            return false;
        }

        // 妫�鏌ヨ緭鍏ョ墿鍝佹暟閲忔槸鍚﹁冻澶?
        if (inputStack.getCount() < recipe.getInputCount()) {
            return false;
        }

        ItemStack output = recipe.getResultItem(null);
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
        GrindingRecipe recipe = this.getCurrentRecipe();
        if (recipe != null && this.canGrind()) {
            ItemStack input = this.inventory.get(INPUT_SLOT_INDEX);
            ItemStack output = recipe.assemble(this, null);
            ItemStack outputSlot = this.inventory.get(OUTPUT_SLOT_INDEX);
            int requiredCount = recipe.getInputCount();

            if (outputSlot.isEmpty()) {
                this.inventory.set(OUTPUT_SLOT_INDEX, output);
            } else if (ItemStack.isSameItem(outputSlot, output)) {
                outputSlot.grow(output.getCount());
            }

            // 娑堣�楅厤鏂规墍闇�鐨勭墿鍝佹暟閲?
            input.shrink(requiredCount);

            // 璁板綍浣跨敤鐨勯厤鏂?
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
     * 灏濊瘯娣诲姞鑳介噺锛岃�冭檻鏃堕棿闂撮殧闄愬埗
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
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
     * 鑾峰彇褰撳墠鐮旂（杩涘害鐨勭櫨鍒嗘瘮
     */
    public float getGrindingProgress() {
        if (grindingTimeTotal != 0) {
            return (float) grindingTime / grindingTimeTotal * 100.0f;
        }
        return 0.0f;
    }

    /**
     * 鑾峰彇鎵�鏈夌墿鍝侊紙鐢ㄤ簬鐮村潖鏂瑰潡鏃舵帀钀斤級
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