package org.bakingprocess.block.entity;

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

public class PlateBlockEntity extends BlockEntity implements PlatableBlockEntity {
    /** 鏂瑰潡搴撳瓨鐨勬渶澶у�归噺锛屽悓鏃朵篃鏄�鎽嗙洏娴佺▼鐨勬渶澶ф�ラ�ゆ暟閲忋�?*/
    public static final int MAX_STEPS = 10;
    private static final String OUTCOME_KEY = "outcome";
    private static final String ACTIONS_KEY = "actions";

    /**
     * 宸叉墽琛岀殑鎿嶄綔鍒楄〃銆?
     * <p>褰搟@linkplain #platingProcess}澶勪簬鍏抽棴鐘舵�佹椂璇ュ垪琛ㄥ簲璇ヤ负绌?/p>
     */
    private final List<PlayerAction> performedActions = new ArrayList<>();
    /** 鎽嗙洏娴佺▼ */
    private final PlatingProcess<PlateBlockEntity> platingProcess;
    /** 椋熺敤娴佺▼ */
    private final EatDishesProcess<PlateBlockEntity> eatProcess;
    /** 鎽嗙洏閰嶆柟鐨勬渶缁堜骇鐗?*/
    @Nullable
    private DishesContent outcome;

    public PlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PLATE.get(), pos, state);
        this.eatProcess = new EatDishesProcess<>();
        this.platingProcess = new PlatingProcess<>();
    }

    // ==================== 鎿嶄綔绠＄悊鏂规硶 ====================

    @Override
    public List<PlayerAction> getPerformedActions() {
        return new ArrayList<>(performedActions);
    }

    @Override
    public boolean performAction(int step, PlayerAction action) {
        if (!platingProcess.isActive()) {
            return false;
        }

        // 楠岃瘉鍙傛暟
        if (step < 0 || step >= MAX_STEPS || action == null) {
            return false;
        }

        // 妫�鏌ユ�ラ�よ繛缁�鎬э細鍓嶉潰鐨勬�ラ�ゅ繀椤绘湁鎿嶄綔
        for (int i = 0; i < step; i++) {
            if (i >= performedActions.size()) {
                return false;
            }
        }

        // 妫�鏌ヨ�ユ�ラ�ゆ槸鍚﹀凡鏈夋搷浣�
        if (step < performedActions.size()) {
            return false;
        }

        // 纭�淇濆垪琛ㄨ冻澶熷�?
        while (performedActions.size() < step) {
            performedActions.add(null);
        }

        // 娣诲姞鎿嶄綔
        performedActions.add(action);

        // 鏍囪�拌剰鐘舵�佷互淇濆瓨
        setChanged();
        return true;
    }

    @Override
    public @Nullable PlayerAction removeAction(int step) {
        // 楠岃瘉鍙傛暟
        if (step < 0 || step >= performedActions.size()) {
            return null;
        }

        PlayerAction removed = performedActions.get(step);
        if (removed == null) {
            return null;
        }

        // 绉婚櫎璇ユ�ラ�ゅ強涔嬪悗鐨勬墍鏈夋搷浣滐紙淇濇寔杩炵画鎬э級
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

    // ==================== 鐩栧瓙鐩稿叧鏂规硶 ====================

    /**
     * 灏濊瘯鐩栦笂鐩栧瓙锛屽彧鏈夊綋鐩樺瓙鍐呮嫢鏈夊畬鏁寸殑鑿滆偞鏃舵墠浼氭垚鍔熴�?
     * @return 鏄�鍚︽垚鍔熺洊涓婄洊瀛�
     */
    public boolean coverWithLid() {
        if (outcome == null || level == null) {
            return false;
        }

        return level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PlateBlock.IS_COVERED, true));
    }

    /**
     * 鍙栦笅鐩栧瓙骞跺皾璇曟仮澶嶆憜鐩樻祦绋嬨�?
     */
    public boolean removeCoverAndRestore() {
        if (level == null) {
            return false;
        }

        BlockState currentState = getBlockState();
        if (!currentState.getValue(PlateBlock.IS_COVERED)) {
            return false;
        }

        // 鍙栦笅鐩栧瓙
        BlockState newState = currentState.setValue(PlateBlock.IS_COVERED, false);
        boolean coverRemoved = level.setBlock(worldPosition, newState, 3);

        if (!coverRemoved) {
            return false;
        }

        // 灏濊瘯鎭㈠�嶆憜鐩樻祦绋�
        if (outcome != null) {
            restoreProcess();
        }

        level.playSound(null, worldPosition, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);
        setChanged();
        return true;
    }

    /**
     * 灏濊瘯鏍规嵁褰撳墠鐨剓@link #outcome}鎭㈠�嶆祦绋嬨�?
     */
    public boolean restoreProcess() {
        if (platingProcess.isActive() || outcome == null || level == null) {
            return false;
        }

        // 鏍规嵁鑿滆偞鑾峰彇瀵瑰簲鐨勯厤鏂?
        PlatingRecipe recipe = PlatingRecipe.getRecipeByContainerAndDishes(getContainerType(), outcome);
        if (recipe == null) {
            return false;
        }

        // 娓呴櫎褰撳墠鐨勮彍鑲?
        setOutcome(null);

        // 鏍规嵁閰嶆柟鐨勬搷浣滄仮澶?performedActions
        List<PlayerAction> actions = recipe.getActions();
        performedActions.clear();
        performedActions.addAll(actions);

        // 鍚�鍔ㄦ憜鐩樻祦绋�
        platingProcess.start(level, this);

        // 鍒濆�嬪寲鍊欓�夐厤鏂瑰垪琛?
        boolean initialized = platingProcess.initializeCandidates(level, this);
        if (!initialized) {
            // 濡傛灉鍒濆�嬪寲澶辫触锛岄噸缃�鐘舵�?
            clearPerformedActions();
            platingProcess.reset();
            return false;
        }

        setChanged();
        return true;
    }

    // ==================== 浜や簰鏂规硶 ====================

    /**
     * 灏濊瘯鎽嗙洏銆?
     */
    public InteractionResult tryPlating(Player player, InteractionHand hand, BlockHitResult hit) {
        // 妫�鏌ユ槸鍚︽弧瓒虫憜鐩樻潯浠?
        if (eatProcess.isActive() || outcome != null || getBlockState().getValue(PlateBlock.IS_COVERED)) {
            BakingProcess.LOGGER.info("姝ｅ父鎯呭喌");
            return InteractionResult.PASS;
        }

        if (!platingProcess.isActive()) {
            platingProcess.start(level, this);
        }

        return platingProcess.executeStep(this, getBlockState(), level, worldPosition, player, hand, hit);
    }

    /**
     * 灏濊瘯椋熺敤銆?
     */
    public InteractionResult tryEat(Player player, InteractionHand hand, BlockHitResult hit) {
        // 濡傛灉鎽嗙洏娴佺▼娲昏穬锛屼笉鍏佽�稿�?
        if (platingProcess.isActive()) {
            return InteractionResult.PASS;
        }

        if (!eatProcess.isActive()) {
            eatProcess.start(level, this);
        }

        return eatProcess.executeStep(this, getBlockState(), level, worldPosition, player, hand, hit);
    }

    // ==================== NBT 搴忓垪鍖?====================

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        // 娓呴櫎褰撳墠鐘舵�?
        this.performedActions.clear();

        if (nbt.contains("eat_process")) {
            platingProcess.readFromNbt(nbt.getCompound("plating_process"));
        }

        if (nbt.contains("eat_process")) {
            eatProcess.readFromNbt(nbt.getCompound("eat_process"));
        }

        // 璇诲彇鑿滆偞
        if (nbt.contains(OUTCOME_KEY, Tag.TAG_STRING)) {
            Content content = TWRegistries.CONTENT.get(ResourceLocation.tryParse(nbt.getString(OUTCOME_KEY)));
            setOutcome((DishesContent) content);
        } else {
            // 璇诲彇鎿嶄綔鍒楄〃
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
            nbt.putString(OUTCOME_KEY, Objects.requireNonNull(TWRegistries.CONTENT.getKey(outcome)).toString());
        } else {
            // 鍐欏叆鎿嶄綔鍒楄〃
            PlayerActionListUtil.writeActionsToNbt(nbt, performedActions);
        }
    }

    // ==================== PlatableBlockEntity 鎺ュ彛瀹炵幇 ====================

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
        // 璁剧疆鑿滆偞
        setOutcome(recipe.getDishes());

        // 娑堣�椾竴涓�瀹屾垚鐗╁�?
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }

        // 鐩栦笂鐩栧瓙
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

    // ==================== 璁块棶鍣ㄦ柟娉?====================

    /**
     * 璁剧疆褰撳墠鐨勮彍鑲达紝杩欎細鍚屾椂娓呯┖褰撳墠鐨勬搷浣滃垪琛ㄣ�?
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
     * 鑾峰彇褰撳墠鎽嗙洏娴佺▼銆?
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

    // ==================== 缃戠粶鍚屾�� ====================

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