package org.bakingprocess.block.entity;

import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.PlateBlock;
import org.bakingprocess.block.process.EatDishesProcess;
import org.bakingprocess.block.process.PlatingProcess;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.recipe.PlatingRecipe;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModItems;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.process.PlayerAction;
import org.twcore.content.Content;
import org.twcore.process.playeraction.PlayerActionListUtil;
import org.twcore.registry.TWRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class PlateBlockEntity extends BlockEntity implements PlatableBlockEntity {
    /** 方块库存的最大容量，同时也是摆盘流程的最大步骤数量。 */
    public static final int MAX_STEPS = 10;
    private static final String OUTCOME_KEY = "outcome";
    private static final String ACTIONS_KEY = "actions";

    /**
     * 已执行的操作列表。
     * <p>当{@linkplain #platingProcess}处于关闭状态时该列表应该为空</p>
     */
    private final List<PlayerAction> performedActions = new ArrayList<>();
    /** 摆盘流程 */
    private final PlatingProcess<PlateBlockEntity> platingProcess;
    /** 食用流程 */
    private final EatDishesProcess<PlateBlockEntity> eatProcess;
    /** 摆盘配方的最终产物 */
    @Nullable
    private DishesContent outcome;

    public PlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PLATE.get(), pos, state);
        this.eatProcess = new EatDishesProcess<>();
        this.platingProcess = new PlatingProcess<>();
    }

    // ==================== 操作管理方法 ====================

    @Override
    public List<PlayerAction> getPerformedActions() {
        return new ArrayList<>(performedActions);
    }

    @Override
    public boolean performAction(int step, PlayerAction action) {
        if (!platingProcess.isActive()) {
            return false;
        }

        // 验证参数
        if (step < 0 || step >= MAX_STEPS || action == null) {
            return false;
        }

        // 检查步骤连续性：前面的步骤必须有操作
        for (int i = 0; i < step; i++) {
            if (i >= performedActions.size()) {
                return false;
            }
        }

        // 检查该步骤是否已有操作
        if (step < performedActions.size()) {
            return false;
        }

        // 确保列表足够大
        while (performedActions.size() < step) {
            performedActions.add(null);
        }

        // 添加操作
        performedActions.add(action);

        // 标记脏状态以保存
        setChanged();
        return true;
    }

    @Override
    public @Nullable PlayerAction removeAction(int step) {
        // 验证参数
        if (step < 0 || step >= performedActions.size()) {
            return null;
        }

        PlayerAction removed = performedActions.get(step);
        if (removed == null) {
            return null;
        }

        // 移除该步骤及之后的所有操作（保持连续性）
        while (performedActions.size() > step) {
            performedActions.remove(performedActions.size() - 1);
        }

        setChanged();
        return removed;
    }

    @Override
    public void clearPerformedActions() {
        performedActions.clear();
        setChanged();
    }

    // ==================== 盖子相关方法 ====================

    /**
     * 尝试盖上盖子，只有当盘子内拥有完整的菜肴时才会成功。
     * @return 是否成功盖上盖子
     */
    public boolean coverWithLid() {
        if (outcome == null || level == null) {
            return false;
        }

        return level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PlateBlock.IS_COVERED, true));
    }

    /**
     * 取下盖子并尝试恢复摆盘流程。
     */
    public boolean removeCoverAndRestore() {
        if (level == null) {
            return false;
        }

        BlockState currentState = getBlockState();
        if (!currentState.getValue(PlateBlock.IS_COVERED)) {
            return false;
        }

        // 取下盖子
        BlockState newState = currentState.setValue(PlateBlock.IS_COVERED, false);
        boolean coverRemoved = level.setBlock(worldPosition, newState, 3);

        if (!coverRemoved) {
            return false;
        }

        // 尝试恢复摆盘流程
        if (outcome != null) {
            restoreProcess();
        }

        level.playSound(null, worldPosition, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);
        setChanged();
        return true;
    }

    /**
     * 尝试根据当前的{@link #outcome}恢复流程。
     */
    public boolean restoreProcess() {
        if (platingProcess.isActive() || outcome == null || level == null) {
            return false;
        }

        // 根据菜肴获取对应的配方
        PlatingRecipe recipe = PlatingRecipe.getRecipeByContainerAndDishes(getContainerType(), outcome);
        if (recipe == null) {
            return false;
        }

        // 清除当前的菜肴
        setOutcome(null);

        // 根据配方的操作恢复 performedActions
        List<PlayerAction> actions = recipe.getActions();
        performedActions.clear();
        performedActions.addAll(actions);

        // 启动摆盘流程
        platingProcess.start(level, this);

        // 初始化候选配方列表
        boolean initialized = platingProcess.initializeCandidates(level, this);
        if (!initialized) {
            // 如果初始化失败，重置状态
            clearPerformedActions();
            platingProcess.reset();
            return false;
        }

        setChanged();
        return true;
    }

    // ==================== 交互方法 ====================

    /**
     * 尝试摆盘。
     */
    public InteractionResult tryPlating(Player player, InteractionHand hand, BlockHitResult hit) {
        // 检查是否满足摆盘条件
        if (eatProcess.isActive() || outcome != null || getBlockState().getValue(PlateBlock.IS_COVERED)) {
            BakingProcess.LOGGER.info("正常情况");
            return InteractionResult.PASS;
        }

        if (!platingProcess.isActive()) {
            platingProcess.start(level, this);
        }

        return platingProcess.executeStep(this, getBlockState(), level, worldPosition, player, hand, hit);
    }

    /**
     * 尝试食用。
     */
    public InteractionResult tryEat(Player player, InteractionHand hand, BlockHitResult hit) {
        // 如果摆盘流程活跃，不允许吃
        if (platingProcess.isActive()) {
            return InteractionResult.PASS;
        }

        if (!eatProcess.isActive()) {
            eatProcess.start(level, this);
        }

        return eatProcess.executeStep(this, getBlockState(), level, worldPosition, player, hand, hit);
    }

    // ==================== NBT 序列化 ====================

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        // 清除当前状态
        this.performedActions.clear();

        if (nbt.contains("eat_process")) {
            platingProcess.readFromNbt(nbt.getCompound("plating_process"));
        }

        if (nbt.contains("eat_process")) {
            eatProcess.readFromNbt(nbt.getCompound("eat_process"));
        }

        // 读取菜肴
        if (nbt.contains(OUTCOME_KEY, Tag.TAG_STRING)) {
            Content content = TWRegistries.CONTENT.get().getValue(ResourceLocation.tryParse(nbt.getString(OUTCOME_KEY)));
            setOutcome((DishesContent) content);
        } else {
            // 读取操作列表
            List<PlayerAction> actions = PlayerActionListUtil.readActionsFromNbt(nbt);
            performedActions.addAll(actions);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        CompoundTag platingNbt = new CompoundTag();
        platingProcess.writeToNbt(platingNbt);
        nbt.put("plating_process", platingNbt);

        CompoundTag eatNbt = new CompoundTag();
        eatProcess.writeToNbt(eatNbt);
        nbt.put("eat_process", eatNbt);

        if (outcome != null) {
            nbt.putString(OUTCOME_KEY, Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(outcome)).toString());
        } else {
            // 写入操作列表
            PlayerActionListUtil.writeActionsToNbt(nbt, performedActions);
        }
    }

    // ==================== PlatableBlockEntity 接口实现 ====================

    @Override
    public Item getContainerType() {
        return this.getBlockState().getBlock().asItem();
    }

    @Override
    public boolean isCompletionItem(ItemStack stack) {
        return stack.is(ModItems.PLATE_LID.get());
    }

    @Override
    public void onPlatingComplete(Level world, BlockPos pos, PlatingRecipe recipe, Player player, InteractionHand hand, HitResult hit) {
        // 设置菜肴
        setOutcome(recipe.getDishes());

        // 消耗一个完成物品
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }

        // 盖上盖子
        if (coverWithLid()) {
            world.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.5f, 0.8f);
        }
    }

    @Override
    public void onEatComplete(Level world, BlockPos pos, Player player, InteractionHand hand, HitResult hit) {
        setOutcome(null);
    }

    @Override
    public @Nullable DishesContent getOutcome() {
        return outcome;
    }

    @Override
    public int getContainerSize() {
        return MAX_STEPS;
    }

    // ==================== 访问器方法 ====================

    /**
     * 设置当前的菜肴，这会同时清空当前的操作列表。
     */
    public void setOutcome(@Nullable DishesContent outcome) {
        this.outcome = outcome;

        if (outcome != null) {
            clearPerformedActions();
            platingProcess.reset();
        }

        setChanged();
    }

    /**
     * 获取当前摆盘流程。
     */
    public PlatingProcess<PlateBlockEntity> getPlatingProcess() {
        return platingProcess;
    }

    public EatDishesProcess<PlateBlockEntity> getEatProcess() {
        return eatProcess;
    }

    public String getDebugInfo() {
        return platingProcess.toString() + "\n" + getPerformedActions();
    }

    // ==================== 网络同步 ====================

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}