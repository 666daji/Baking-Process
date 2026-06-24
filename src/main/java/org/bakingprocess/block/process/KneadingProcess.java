package org.bakingprocess.block.process;

import org.dfood.sound.ModSoundGroups;
import org.bakingprocess.item.FlourItem;
import org.bakingprocess.recipe.DoughRecipe;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.ModRecipeTypes;
import org.twcore.api.content.ContainerStack;
import org.twcore.api.content.ContainerUtil;
import org.twcore.api.process.AbstractProcess;
import org.twcore.content.Content;
import org.twcore.process.step.Step;
import org.twcore.process.step.StepExecutionContext;
import org.twcore.process.step.StepResult;
import org.twcore.registry.Contents;
import org.twcore.registry.TWRegistries;

import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 揉面流程实现类，管理所有状态数据。
 */
public class KneadingProcess<T extends BlockEntity & Container> extends AbstractProcess<T> implements Container {
    /** 可加入盆中的额外物品集合 */
    public static final Set<Item> CAN_ADD_OTHER = Set.of(ModItems.SALT_CUBES.get(), ModItems.SALT_FLOUR.get(), Items.SUGAR, ModItems.SUGAR_FLOUR.get(), Items.EGG);
    public static final Set<Item> CAN_ADD_FLOUR = Set.of(ModItems.WHEAT_FLOUR.get(), ModItems.COCOA_FLOUR.get());

    /** 流程步骤ID常量 */
    public static final String STEP_ADD_FLOUR = "add_flour";
    public static final String STEP_ADD_LIQUID = "add_liquid";
    public static final String STEP_ADD_EXTRA = "add_extra";
    public static final String STEP_KNEAD = "knead";

    /** 额外物品库存槽位数量 */
    private static final int EXTRA_SLOT_COUNT = 10;
    private static final int TOTAL_SLOTS = EXTRA_SLOT_COUNT;

    /** 额外物品库存 */
    private final NonNullList<ItemStack> extraInventory;

    /** 面粉类型计数 */
    private final Map<FlourItem.FlourType, Integer> flourCounts;

    /** 液体类型计数 */
    private final Map<Content, Integer> liquidCounts;

    /** 揉面次数 */
    private int kneadingCount;

    /** 是否已经处理了跳过逻辑 */
    private boolean processedSkip;

    public KneadingProcess() {
        this.extraInventory = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        this.flourCounts = new HashMap<>();
        this.liquidCounts = new HashMap<>();
        this.kneadingCount = 0;
        this.processedSkip = false;

        // 注册步骤
        registerSteps();
    }

    private void registerSteps() {
        // 1. 加粉步骤 - 需要加入3次面粉
        registerStep(STEP_ADD_FLOUR, new FlourStep());

        // 2. 加水步骤 - 需要加入3次液体
        registerStep(STEP_ADD_LIQUID, new LiquidStep());

        // 3. 加额外物品步骤 - 可跳过，最多加10次
        registerStep(STEP_ADD_EXTRA, new ExtraItemStep());

        // 4. 揉面步骤 - 需要揉面3次
        registerStep(STEP_KNEAD, new KneadingStep());
    }

    // ============ beforeGetStep实现 ============

    @Override
    protected void beforeGetStep(StepExecutionContext<T> context) {
        // 重置跳过标记
        processedSkip = false;

        // 只在添加额外物品步骤时检查
        if (STEP_ADD_EXTRA.equals(currentStepId)) {
            ItemStack heldStack = context.getHeldItemStack();

            // 如果玩家空手，跳转到揉面步骤
            if (heldStack.isEmpty()) {
                jumpToStep(STEP_KNEAD);
            }
        }
    }

    // ============ 步骤实现类 ============

    private class FlourStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            ItemStack heldStack = context.getHeldItemStack();

            // 检查是否为面粉
            if (!(heldStack.getItem() instanceof FlourItem flourItem)) {
                return StepResult.fail(STEP_ADD_FLOUR, InteractionResult.PASS);
            }

            // 播放刷子清扫可疑沙砾的声音
            context.playSound(SoundEvents.BRUSH_SAND_COMPLETED);

            // 服务器端执行
            if (context.isServerSide()) {
                // 消耗一个面粉
                if (!context.isCreateMode()){
                    heldStack.shrink(1);
                }

                // 记录面粉计数
                flourCounts.put(flourItem.getFlourType(),
                        flourCounts.getOrDefault(flourItem.getFlourType(), 0) + 1);

                // 更新方块实体
                context.blockEntity().setChanged();
            }

            // 检查是否加满3个面粉
            if (getTotalFlourCount() >= 3) {
                return StepResult.nextStep(STEP_ADD_LIQUID, InteractionResult.SUCCESS);
            } else {
                return StepResult.continueSameStep(InteractionResult.SUCCESS);
            }
        }
    }

    private class LiquidStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            ItemStack heldStack = context.getHeldItemStack();

            Optional<ContainerStack> bindingOpt = ContainerUtil.analyze(heldStack);
            if (bindingOpt.isEmpty()) {
                return StepResult.fail(STEP_ADD_LIQUID, InteractionResult.PASS);
            }

            ContainerStack binding = bindingOpt.get();
            Content content = binding.content();

            if (content == null || !isAllowedContent(content)) {
                return StepResult.fail(STEP_ADD_LIQUID, InteractionResult.PASS);
            }

            // 获取容器的基本容量（每个容器提供的液体单位数）
            int capacity = binding.container().getBaseCapacity();

            // 服务器端执行消耗和记录逻辑
            if (context.isServerSide()) {

                // 播放加入液体的声音
                context.playSound(context.getItemSounds().getPlaceSound());

                // 消耗1个液体物品
                if (!context.isCreateMode()) {
                    heldStack.shrink(1);

                    // 返还空容器
                    ItemStack remainder = binding.container().remainder();
                    context.giveStack(remainder);
                }

                // 记录液体计数（使用LiquidType作为键）
                liquidCounts.put(content,
                        liquidCounts.getOrDefault(content, 0) + capacity);

                // 更新方块实体
                context.blockEntity().setChanged();
            }

            // 检查是否已加满3次液体
            if (isLiquidComplete()) {
                return StepResult.nextStep(STEP_ADD_EXTRA, InteractionResult.SUCCESS);
            } else {
                return StepResult.continueSameStep(InteractionResult.SUCCESS);
            }
        }

        /** 检查是否已加满3次液体 */
        private boolean isLiquidComplete() {
            return getTotalLiquidCount() >= 3;
        }
    }

    private class ExtraItemStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            ItemStack heldStack = context.getHeldItemStack();

            // 检查手持物品是否为可接受的额外物品
            if (!CAN_ADD_OTHER.contains(heldStack.getItem())) {
                return StepResult.fail(STEP_ADD_EXTRA, InteractionResult.PASS);
            }

            // 检查是否已满
            if (isExtraFull()) {
                return StepResult.nextStep(STEP_KNEAD, InteractionResult.SUCCESS);
            }

            // 服务器端执行消耗和存储逻辑
            if (context.isServerSide()) {
                // 播放普通的石头放置音效
                context.playSound(SoundEvents.STONE_PLACE);

                // 先创建物品的副本，用于存储
                ItemStack extraCopy = new ItemStack(heldStack.getItem(), 1);

                // 如果需要保留NBT数据，可以这样复制
                if (heldStack.getTag() != null && heldStack.hasTag()) {
                    extraCopy.setTag(heldStack.getTag().copy());
                }

                // 消耗1个额外物品
                if (!context.isCreateMode()) {
                    heldStack.shrink(1);
                }

                // 存储额外物品到库存
                for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
                    if (extraInventory.get(i).isEmpty()) {
                        extraInventory.set(i, extraCopy);

                        // 标记库存已修改
                        break;
                    }
                }

                // 更新方块实体
                context.blockEntity().setChanged();

                // 如果加满了，跳转到揉面步骤
                if (isExtraFull()) {
                    return StepResult.nextStep(STEP_KNEAD, InteractionResult.SUCCESS);
                }
            }

            // 继续执行当前步骤（添加更多额外物品）
            return StepResult.continueSameStep(InteractionResult.SUCCESS);
        }

        /** 检查是否已加满10个额外物品 */
        private boolean isExtraFull() {
            int extraCount = 0;
            for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
                if (!extraInventory.get(i).isEmpty()) {
                    extraCount++;
                }
            }
            return extraCount >= EXTRA_SLOT_COUNT;
        }
    }

    private class KneadingStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            ItemStack heldStack = context.getHeldItemStack();

            // 检查是否空手
            if (!heldStack.isEmpty()) {
                return StepResult.fail(STEP_KNEAD, InteractionResult.PASS);
            }

            context.playSound(ModSoundGroups.BREAD.getPlaceSound());

            // 服务器端执行揉面逻辑
            if (context.isServerSide()) {
                kneadingCount++;

                // 如果是第二次揉面，制作面团
                if (kneadingCount >= 2) {
                    ItemStack result = craftDough(context.world());
                    if (!result.isEmpty()) {

                        // 将面团放入盆方块实体
                        context.blockEntity().setItem(0, result);

                        // 重置流程
                        reset();

                        // 更新方块实体
                        context.blockEntity().setChanged();
                        return StepResult.complete(InteractionResult.SUCCESS);
                    } else {
                        // 没有匹配的配方，失败
                        reset();
                        context.blockEntity().setChanged();
                        return StepResult.fail(null, InteractionResult.FAIL);
                    }
                } else {
                    // 第一次揉面，继续执行当前步骤
                    context.blockEntity().setChanged();
                    return StepResult.continueSameStep(InteractionResult.SUCCESS);
                }
            }

            return StepResult.continueSameStep(InteractionResult.SUCCESS);
        }
    }

    // ============ 面团制作 ============

    private ItemStack craftDough(Level world) {
        // 查找匹配的配方
        Optional<DoughRecipe> recipe = world.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.DOUGH_MAKING.get(), this, world);

        if (recipe.isPresent()) {
            // 消耗所有原料
            clearContent();
            flourCounts.clear();
            liquidCounts.clear();
            kneadingCount = 0;
            processedSkip = false;

            return recipe.get().getResultItem(world.registryAccess()).copy();
        }

        return ItemStack.EMPTY;
    }

    /**
     * 检查内容物是否是允许添加的液体。
     *
     * @param content 要检查的内容物
     * @return 是否可以在液体步骤中添加的内容
     */
    public static boolean isAllowedContent(Content content) {
        return content.isIn(Contents.BASE_LIQUID);
    }

    // ============ 状态获取方法 ============

    public Map<FlourItem.FlourType, Integer> getFlourCounts() {
        return new HashMap<>(flourCounts);
    }

    /**
     * 获取液体类型计数
     */
    public Map<Content, Integer> getLiquidCounts() {
        return new HashMap<>(liquidCounts);
    }

    public List<ItemStack> getExtraItemStacks() {
        List<ItemStack> extras = new ArrayList<>();
        for (ItemStack stack : extraInventory) {
            if (!stack.isEmpty()) {
                extras.add(stack);
            }
        }
        return extras;
    }

    public int getKneadingCount() {
        return kneadingCount;
    }

    public int getExtraItemCount() {
        return (int) extraInventory.stream().filter(stack -> !stack.isEmpty()).count();
    }

    public int getTotalFlourCount() {
        return flourCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalLiquidCount() {
        return liquidCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static boolean isCanAddFlour(ItemStack stack) {
        return CAN_ADD_FLOUR.contains(stack.getItem());
    }

    public static boolean isCanAddExtra(ItemStack stack) {
        return CAN_ADD_OTHER.contains(stack.getItem());
    }

    // ============ AbstractProcess方法实现 ============

    @Override
    protected String getInitialStepId() {
        return STEP_ADD_FLOUR;
    }

    @Override
    protected void onStart(Level world, T blockEntit) {
        clearContent();
        flourCounts.clear();
        liquidCounts.clear();
        kneadingCount = 0;
        processedSkip = false;
    }

    @Override
    protected void onReset() {
        clearContent();
        flourCounts.clear();
        liquidCounts.clear();
        kneadingCount = 0;
        processedSkip = false;
    }

    // ============ NBT持久化 ============

    @Override
    public void writeToNbt(CompoundTag nbt) {
        super.writeToNbt(nbt);

        // 保存额外物品库存
        ContainerHelper.saveAllItems(nbt, extraInventory);

        // 保存面粉计数
        CompoundTag floursNbt = new CompoundTag();
        for (Map.Entry<FlourItem.FlourType, Integer> entry : flourCounts.entrySet()) {
            floursNbt.putInt(entry.getKey().getSerializedName(), entry.getValue());
        }
        nbt.put("flours", floursNbt);

        // 保存液体计数（LiquidType作为键）
        CompoundTag liquidsNbt = new CompoundTag();
        for (Map.Entry<Content, Integer> entry : liquidCounts.entrySet()) {
            ResourceLocation id = TWRegistries.CONTENT.get().getKey(entry.getKey());
            if (id != null) {
                liquidsNbt.putInt(id.toString(), entry.getValue());
            }
        }
        nbt.put("liquids", liquidsNbt);

        // 保存揉面次数
        nbt.putInt("kneading_count", kneadingCount);

        // 保存跳过标记
        nbt.putBoolean("processed_skip", processedSkip);
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        super.readFromNbt(nbt);

        // 清空现有数据
        extraInventory.clear();
        flourCounts.clear();
        liquidCounts.clear();

        // 读取额外物品库存
        ContainerHelper.loadAllItems(nbt, extraInventory);

        // 读取面粉计数
        if (nbt.contains("flours")) {
            CompoundTag floursNbt = nbt.getCompound("flours");
            for (String key : floursNbt.getAllKeys()) {
                flourCounts.put(FlourItem.FlourType.fromId(key), floursNbt.getInt(key));
            }
        }

        // 读取液体计数（从字符串转换为LiquidType）
        if (nbt.contains("liquids")) {
            CompoundTag liquidsNbt = nbt.getCompound("liquids");
            for (String key : liquidsNbt.getAllKeys()) {
                Content content = TWRegistries.CONTENT.get().getValue(ResourceLocation.tryParse(key));
                if (content != null && isAllowedContent(content)) {
                    liquidCounts.put(content, liquidsNbt.getInt(key));
                }
            }
        }

        // 读取揉面次数
        kneadingCount = nbt.getInt("kneading_count");

        // 读取跳过标记
        processedSkip = nbt.getBoolean("processed_skip");
    }

    // ============ Inventory接口实现 ============

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : extraInventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= extraInventory.size()) {
            return ItemStack.EMPTY;
        }
        return extraInventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(extraInventory, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(extraInventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < extraInventory.size()) {
            extraInventory.set(slot, stack);
        }
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        extraInventory.clear();
    }

    @Override
    protected String getCustomStatusInfo() {
        StringBuilder info = new StringBuilder();

        // 面粉计数详情
        info.append("面粉: ").append(getTotalFlourCount()).append("/3\n");
        if (!flourCounts.isEmpty()) {
            flourCounts.forEach((type, count) -> info.append("  - ").append(type.getSerializedName()).append(": ").append(count).append("\n"));
        }

        // 液体计数详情
        info.append("液体: ").append(getTotalLiquidCount()).append("/3\n");
        if (!liquidCounts.isEmpty()) {
            liquidCounts.forEach((type, count) -> info.append("  - ").append(type.toString()).append(": ").append(count).append("\n"));
        }

        // 额外物品详情
        info.append("额外物品: ").append(getExtraItemCount()).append("/").append(EXTRA_SLOT_COUNT).append("\n");
        if (getExtraItemCount() > 0) {
            for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
                ItemStack stack = extraInventory.get(i);
                if (!stack.isEmpty()) {
                    info.append("  - 槽位").append(i + 1).append(": ")
                            .append(stack.getItem().getDescription().getString());
                    if (stack.getCount() > 1) {
                        info.append(" x").append(stack.getCount());
                    }
                    info.append("\n");
                }
            }
        }

        // 揉面次数
        info.append("揉面次数: ").append(kneadingCount).append("/2\n");

        // 跳过逻辑状态
        info.append("跳过逻辑已处理: ").append(processedSkip).append("\n");

        // 库存空状态
        info.append("库存是否为空: ").append(isEmpty()).append("\n");

        return info.toString();
    }

    // ============ 液体类型枚举 ============

    /**
     * 获取当前流程状态的总和
     * @return 状态对象，包含当前步骤、上一个步骤和各步骤计数信息
     */
    public KneadingState getState() {
        return new KneadingState(
                currentStepId,
                previousStepId,
                getTotalFlourCount(),
                getTotalLiquidCount(),
                getExtraItemCount(),
                getKneadingCount(),
                isActive
        );
    }

    /**
     * 状态数据类
     */
    public record KneadingState(String currentStepId, String previousStepId, int flourCount, int liquidCount,
                                int extraItemCount, int kneadingCount, boolean isActive) {}
}