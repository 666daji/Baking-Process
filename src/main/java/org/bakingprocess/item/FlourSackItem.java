package org.bakingprocess.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 专门用于存放 {@linkplain FlourItem} 的袋子。
 */
public class FlourSackItem extends BlockItem {
    public static final String STORED_ITEM_KEY = "StoredFlour";  // 存储完整物品堆栈
    private static final int MAX_STORAGE = 16;
    private static final int ITEM_BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public FlourSackItem(Block block, Properties settings) {
        super(block, settings);
    }

    /* ========== 方块交互方法 ========== */

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        // 只有装有粉尘的粉尘袋才能放置
        if (getBundleOccupancy(stack) == 0) {
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    /* ========== 物品栏交互方法 ========== */

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        if (clickType != ClickAction.SECONDARY) {
            return false;
        }

        ItemStack slotStack = slot.getItem();

        // 如果右键点击空槽位，且手持粉尘袋，则取出一个粉尘
        if (slotStack.isEmpty() && canRemoveOne(stack)) {
            handleRemoveOneFromBundle(stack, slot, player);
            return true;
        }

        // 如果槽位有粉尘物品，且手持粉尘袋，则添加一个粉尘
        if (canAcceptItem(slotStack)) {
            handleAddOneToBundle(stack, slot, slotStack, player);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
            // 如果手持粉尘袋，右键空光标，取出所有粉尘到光标
            if (otherStack.isEmpty() && canRemoveAll(stack)) {
                handleRemoveAllToCursor(stack, cursorStackReference, player);
                return true;
            }

            // 如果手持粉尘物品，右键粉尘袋，尽可能装满粉尘袋
            if (canAcceptItem(otherStack)) {
                handleFillFromCursor(stack, otherStack, cursorStackReference, player);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        // 在世界中使用粉尘袋时，丢弃所有内容物
        if (dropAllBundledItems(itemStack, user)) {
            playDropContentsSound(user);
            user.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    /* ========== 视觉和工具提示方法 ========== */

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getBundleOccupancy(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getBundleOccupancy(stack) / MAX_STORAGE, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        Optional<ItemStack> content = getBundledStack(stack);
        return Optional.of(new FlourSackTooltipData(content, getBundleOccupancy(stack), MAX_STORAGE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        appendCapacityTooltip(stack, tooltip);
        appendContentTooltip(stack, tooltip);
        appendUsageTooltip(stack, tooltip);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        Optional<ItemStack> bundledStack = getBundledStack(stack);

        if (bundledStack.isPresent() && !bundledStack.get().isEmpty()) {
            // 将单个物品堆栈转换为Stream
            Stream<ItemStack> contents = Stream.of(bundledStack.get());
            ItemUtils.onContainerDestroyed(entity, contents);
        }
    }

    /* ========== 物品交互处理方法 ========== */

    /**
     * 从粉尘袋取出一个粉尘到槽位
     */
    private void handleRemoveOneFromBundle(ItemStack stack, Slot slot, Player player) {
        playRemoveOneSound(player);
        removeSomeStack(stack, 1).ifPresent(removedStack -> {
            ItemStack remaining = slot.safeInsert(removedStack);
            if (!remaining.isEmpty()) {
                // 如果有剩余，尝试放回粉尘袋
                addToBundle(stack, remaining);
            }
        });
    }

    /**
     * 向粉尘袋添加一个粉尘
     */
    private void handleAddOneToBundle(ItemStack stack, Slot slot, ItemStack slotStack, Player player) {
        int availableSpace = MAX_STORAGE - getBundleOccupancy(stack);

        // 如果有剩余空间，尝试添加一个粉尘
        if (availableSpace > 0) {
            // 只取一个物品
            ItemStack toAdd = slot.safeTake(1, 1, player);
            int actuallyAdded = addToBundle(stack, toAdd);
            if (actuallyAdded > 0) {
                playInsertSound(player);
            } else {
                // 如果添加失败，把物品放回原处
                slot.safeInsert(toAdd);
            }
        }
    }

    /**
     * 从粉尘袋取出所有粉尘到光标
     */
    private void handleRemoveAllToCursor(ItemStack stack, SlotAccess cursorStackReference, Player player) {
        playRemoveAllSound(player);
        removeAllStack(stack).ifPresent(cursorStackReference::set);
    }

    /**
     * 从光标添加粉尘（尽可能装满粉尘袋）
     */
    private void handleFillFromCursor(ItemStack stack, ItemStack otherStack, SlotAccess cursorStackReference, Player player) {
        int availableSpace = MAX_STORAGE - getBundleOccupancy(stack);

        // 如果没有剩余空间，直接返回
        if (availableSpace <= 0) {
            return;
        }

        // 计算可以添加的数量，取剩余空间和光标中粉尘数量的较小值
        int maxToAdd = Math.min(otherStack.getCount(), availableSpace);

        if (maxToAdd > 0) {
            ItemStack toAdd = otherStack.copyWithCount(maxToAdd);
            int actuallyAdded = addToBundle(stack, toAdd);

            if (actuallyAdded > 0) {
                playInsertSound(player);
                otherStack.shrink(actuallyAdded);
            }
        }
    }

    /* ========== 辅助方法 ========== */

    /**
     * 获取粉尘袋的填充比例（0.0 - 1.0）
     */
    public static float getAmountFilled(ItemStack stack) {
        return getBundleOccupancy(stack) / (float) MAX_STORAGE;
    }

    /**
     * 获取粉尘袋中存储的物品栈
     */
    public static Optional<ItemStack> getBundledStack(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(STORED_ITEM_KEY)) {
            return Optional.empty();
        }

        CompoundTag storedNbt = nbt.getCompound(STORED_ITEM_KEY);
        return Optional.of(ItemStack.of(storedNbt));
    }

    /**
     * 检查两个粉尘袋是否可以堆叠
     */
    public static boolean canStackWith(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;

        Optional<ItemStack> content1 = getBundledStack(stack1);
        Optional<ItemStack> content2 = getBundledStack(stack2);

        // 两个空袋可以堆叠
        if (content1.isEmpty() && content2.isEmpty()) return true;

        // 一个有内容一个空，不能堆叠
        if (content1.isEmpty() != content2.isEmpty()) return false;

        // 检查内容物是否相同（包括NBT）
        return ItemStack.matches(content1.get(), content2.get());
    }

    /**
     * 检查物品是否可以被粉尘袋接受
     */
    private static boolean canAcceptItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof FlourItem;
    }

    /**
     * 检查是否可以移除一个粉尘
     */
    private static boolean canRemoveOne(ItemStack stack) {
        return getBundleOccupancy(stack) > 0;
    }

    /**
     * 检查是否可以移除所有粉尘
     */
    private static boolean canRemoveAll(ItemStack stack) {
        return getBundleOccupancy(stack) > 0;
    }

    /**
     * 添加物品到粉尘袋
     */
    private static int addToBundle(ItemStack bundle, ItemStack stack) {
        if (!canAcceptItem(stack)) {
            return 0;
        }

        CompoundTag nbt = bundle.getOrCreateTag();
        int currentCount = getBundleOccupancy(bundle);
        int availableSpace = MAX_STORAGE - currentCount;
        int maxToAdd = Math.min(stack.getCount(), availableSpace);

        if (maxToAdd <= 0) {
            return 0;
        }

        // 如果粉尘袋是空的，设置物品
        if (!nbt.contains(STORED_ITEM_KEY)) {
            ItemStack copy = stack.copyWithCount(maxToAdd);
            CompoundTag storedNbt = new CompoundTag();
            copy.save(storedNbt);
            nbt.put(STORED_ITEM_KEY, storedNbt);
            return maxToAdd;
        }
        // 如果已经有物品，检查是否可以合并
        else {
            ItemStack existingStack = ItemStack.of(nbt.getCompound(STORED_ITEM_KEY));

            // 检查是否为同一物品（包括NBT）
            if (ItemStack.isSameItemSameTags(existingStack, stack)) {
                int newTotal = existingStack.getCount() + maxToAdd;
                if (newTotal > MAX_STORAGE) {
                    maxToAdd = MAX_STORAGE - existingStack.getCount();
                    if (maxToAdd <= 0) return 0;
                    newTotal = MAX_STORAGE;
                }

                existingStack.setCount(newTotal);
                CompoundTag storedNbt = new CompoundTag();
                existingStack.save(storedNbt);
                nbt.put(STORED_ITEM_KEY, storedNbt);
                return maxToAdd;
            } else {
                // 不同种类的粉，不能添加
                return 0;
            }
        }
    }

    /**
     * 获取粉尘袋当前占用的总空间（也就是存储的数量）
     */
    private static int getBundleOccupancy(ItemStack stack) {
        return getBundledStack(stack)
                .map(ItemStack::getCount)
                .orElse(0);
    }

    /**
     * 从粉尘袋中取出所有物品栈
     */
    private static Optional<ItemStack> removeAllStack(ItemStack stack) {
        Optional<ItemStack> bundledStack = getBundledStack(stack);
        if (bundledStack.isPresent() && !bundledStack.get().isEmpty()) {
            stack.removeTagKey(STORED_ITEM_KEY);
            return bundledStack;
        }
        return Optional.empty();
    }

    /**
     * 从粉尘袋中取出指定数量的物品
     */
    private static Optional<ItemStack> removeSomeStack(ItemStack stack, int amount) {
        Optional<ItemStack> bundledStack = getBundledStack(stack);
        if (bundledStack.isPresent() && !bundledStack.get().isEmpty()) {
            ItemStack storedStack = bundledStack.get();
            int storedCount = storedStack.getCount();

            if (amount >= storedCount) {
                // 取出全部
                stack.removeTagKey(STORED_ITEM_KEY);
                return Optional.of(storedStack);
            } else {
                // 取出部分
                ItemStack removedStack = storedStack.copyWithCount(amount);
                storedStack.setCount(storedCount - amount);

                // 更新存储的NBT
                CompoundTag nbt = stack.getOrCreateTag();
                CompoundTag storedNbt = new CompoundTag();
                storedStack.save(storedNbt);
                nbt.put(STORED_ITEM_KEY, storedNbt);

                return Optional.of(removedStack);
            }
        }
        return Optional.empty();
    }

    /**
     * 丢弃粉尘袋中的所有物品
     */
    private static boolean dropAllBundledItems(ItemStack stack, Player player) {
        Optional<ItemStack> bundledStack = removeAllStack(stack);
        if (bundledStack.isPresent() && !bundledStack.get().isEmpty()) {
            if (player instanceof ServerPlayer) {
                player.drop(bundledStack.get(), true);
            }
            return true;
        }
        return false;
    }

    /* ========== 工具提示辅助方法 ========== */

    private void appendCapacityTooltip(ItemStack stack, List<Component> tooltip) {
        int occupancy = getBundleOccupancy(stack);
        tooltip.add(Component.translatable("item.baking_process.flour_sack.fullness", occupancy, MAX_STORAGE)
                .withStyle(ChatFormatting.GRAY));
    }

    private void appendContentTooltip(ItemStack stack, List<Component> tooltip) {
        getBundledStack(stack).ifPresent(content -> {
            MutableComponent contentText = Component.translatable("item.baking_process.flour_sack.content",
                    content.getHoverName(), content.getCount());
            tooltip.add(contentText.withStyle(ChatFormatting.GRAY));
        });
    }

    private void appendUsageTooltip(ItemStack stack, List<Component> tooltip) {
        int occupancy = getBundleOccupancy(stack);
        String translationKey = occupancy == 0 ?
                "item.baking_process.flour_sack.tooltip.empty" :
                "item.baking_process.flour_sack.tooltip.non_empty";

        tooltip.add(Component.translatable(translationKey)
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }

    /* ========== 音效方法 ========== */

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveAllSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F,
                0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F,
                0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F,
                0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    /**
     * 用于绘制的物品组件信息。
     *
     * @param content
     * @param occupancy
     * @param maxStorage
     */
    public record FlourSackTooltipData(Optional<ItemStack> content, int occupancy, int maxStorage) implements TooltipComponent {}
}