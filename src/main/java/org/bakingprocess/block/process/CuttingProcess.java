package org.bakingprocess.block.process;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.recipe.CutRecipe;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.ModRecipeTypes;
import org.bakingprocess.registry.ModSounds;
import org.twcore.api.block.UpPlaceBlockEntity;
import org.twcore.api.process.AbstractProcess;
import org.twcore.process.step.Step;
import org.twcore.process.step.StepBuilders;
import org.twcore.process.step.StepExecutionContext;
import org.twcore.process.step.StepResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 鍒囪彍娴佺▼澶勭悊鍣�锛屾敮鎸佺壒瀹氱墿鍝佸湪鐗瑰畾鍒囧壊娆℃暟鐨勭壒娈婁氦浜掋�?
 * <p>
 * 璇ョ被绠＄悊鏁翠釜鍒囧壊杩囩▼锛屽寘鎷�锛�
 * - 鏅�閫氬垏鍓叉�ラ�?
 * - 鐗规畩鐗╁搧鐨勭壒娈婂垏鍓叉�ラ�わ紙濡傝儭钀濆崪锛?
 * - 鍒囧壊杩涘害璺熻釜
 * - 閰嶆柟鍖归厤涓庢仮澶?
 * - NBT鏁版嵁鎸佷箙鍖?
 *
 * @param <T> 鏂瑰潡瀹炰綋绫诲瀷锛屽繀椤荤户鎵胯嚜 {@link UpPlaceBlockEntity}
 */
public class CuttingProcess<T extends UpPlaceBlockEntity> extends AbstractProcess<T> {
    /** 鏅�閫氬垏鍓叉�ラ�ゆ爣璇?*/
    public static final String STEP_CUT = "cut";

    /** 瀹屾垚姝ラ�ゆ爣璇� */
    public static final String STEP_COMPLETE = "complete";

    /** 绌烘墜浜や簰 */
    public static final String STEP_EMPTY= "cut_empty";

    /** 鑳¤悵鍗滅壒娈婃�ラ�ゆ爣璇?*/
    public static final String STEP_CARROT_12 = "cut_carrot_12";

    public static final String STEP_COD_8 = "cut_cod_8";
    public static final String STEP_COD_9 = "cut_cod_9";

    public static final String STEP_COOKED_COD_8 = "cut_cooked_cod_8";
    public static final String STEP_COOKED_COD_9 = "cut_cooked_cod_9";

    public static final String STEP_SALMON_6 = "cut_salmon_6";
    public static final String STEP_SALMON_7 = "cut_salmon_7";

    public static final String STEP_COOKED_SALMON_6 = "cut_cooked_salmon_6";
    public static final String STEP_COOKED_SALMON_7 = "cut_cooked_salmon_7";

    /** 褰撳墠娲昏穬鐨勫垏鍓查厤鏂?*/
    private CutRecipe currentRecipe;
    /** 褰撳墠宸插畬鎴愮殑鍒囧壊娆℃暟锛堜粠0寮�濮嬶級 */
    private int currentCut;
    /** 閰嶆柟瑕佹眰鐨勬�诲垏鍓叉�℃�?*/
    private int totalCuts;
    /** 淇濆瓨鐨勯厤鏂笽D锛岀敤浜嶯BT鏁版嵁鎭㈠�� */
    private String savedRecipeId;
    /** 杈撳叆鐗╁搧鍫嗘爤 */
    private ItemStack inputStack;

    /**
     * 鐗规畩姝ラ�よЕ鍙戞潯浠舵槧灏勶細鐗╁�?-> (鍒囧壊娆℃暟 -> 姝ラ��ID)
     * <p>
     * 鐢ㄤ簬瀹氫箟鐗瑰畾鐗╁搧鍦ㄧ壒瀹氬垏鍓叉�℃暟瑙﹀彂鐨勭壒娈婃�ラ�ゃ�?
     */
    private static final Map<Item, Map<Integer, String>> SPECIAL_STEP_TRIGGERS = new HashMap<>();

    /** 鐗规畩姝ラ�ゅ疄渚嬫槧灏勶細姝ラ��ID -> 姝ラ�ゅ疄渚� */
    private static final Map<String, Function<CuttingProcess<?>, Step<UpPlaceBlockEntity>>> SPECIAL_STEP_INSTANCES = new HashMap<>();

    public CuttingProcess() {
        super();
        this.inputStack = ItemStack.EMPTY;
        this.savedRecipeId = "";

        registerSteps();
    }

    // ============ 姝ラ�ゅ疄鐜扮�?============

    /**
     * 鏅�閫氬垏鍓叉�ラ�ゅ疄鐜般�?
     * <p>
     * 闇�瑕佺帺瀹舵墜鎸佽彍鍒�锛屾瘡娆℃墽琛屽�炲姞鍒囧壊娆℃暟锛�
     * 娑堣�楀伐鍏疯�愪箙骞舵洿鏂板簱瀛樼姸鎬併�?
     */
    private class CuttingStep implements Step<T> {

        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            // 楠岃瘉鍒囧壊宸ュ叿
            if (!isValidCuttingTool(context.getHeldItemStack())) {
                return StepResult.fail(STEP_CUT, InteractionResult.FAIL);
            }

            // 楠岃瘉娲昏穬閰嶆柟
            if (currentRecipe == null) {
                return StepResult.fail(STEP_CUT, InteractionResult.FAIL);
            }

            // 鎾�鏀鹃煶鏁堝拰绮掑瓙鏁堟�?
            context.playSound(ModSounds.CUT.get());
            context.spawnItemParticles(inputStack);
            currentCut++;

            // 鏈嶅姟鍣ㄧ��鎵ц�屽垏鍓查�昏緫
            if (context.isServerSide()) {
                updateInventory(context.blockEntity(), currentCut);
                consumeToolDurability(context);
                context.blockEntity().markDirtyAndSync();
            }

            // 妫�鏌ユ槸鍚﹀畬鎴愭墍鏈夊垏鍓?
            if (currentCut >= totalCuts) {
                return StepResult.nextStep(STEP_COMPLETE, InteractionResult.SUCCESS);
            } else {
                return StepResult.continueSameStep(InteractionResult.SUCCESS);
            }
        }
    }

    /**
     * 绌烘�ラ�ゅ疄鐜帮紝鐢ㄤ簬鐗规畩鍒囧壊姝ラ�や腑涓嶉渶瑕佸伐鍏锋��鏌ョ殑涓�闂存�ラ�ゃ�?
     */
    private class EmptyStep implements Step<T> {

        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            if (currentRecipe == null) {
                return StepResult.fail(STEP_CUT, InteractionResult.FAIL);
            }

            context.playSound(SoundEvents.STONE_PLACE);

            if (context.isServerSide()) {
                currentCut++;
                updateInventory(context.blockEntity(), currentCut);
                context.blockEntity().markDirtyAndSync();
            }

            if (currentCut >= totalCuts) {
                return StepResult.nextStep(STEP_COMPLETE, InteractionResult.SUCCESS);
            } else {
                return StepResult.continueSameStep(InteractionResult.SUCCESS);
            }
        }
    }

    // ============ 姝ラ�ゆ墽琛屾柟娉� ============

    /**
     * 鎵ц�屽畬鎴愭�ラ�わ紝缁欎簣鐜╁�舵墍鏈夊垏鍓蹭骇鐗┿�?
     *
     * @param context 姝ラ�ゆ墽琛屼笂涓嬫�?
     * @return 鎿嶄綔缁撴灉
     */
    private InteractionResult executeComplete(StepExecutionContext<T> context) {
        if (context.isServerSide()) {
            giveAllItemsToPlayer(context.blockEntity(), context.player());
            context.playSound(SoundEvents.CHICKEN_EGG);
            reset();
            context.blockEntity().markDirtyAndSync();
        } else {
            context.blockEntity().clearContent();
        }

        return InteractionResult.SUCCESS;
    }

    // ============ 姝ラ�ゆ敞鍐� ============

    /**
     * 娉ㄥ唽鎵�鏈夊垏鍓叉�ラ�ゃ�?
     */
    private void registerSteps() {
        // 鏅�閫氬垏鍓叉�ラ�?
        registerStep(STEP_CUT, new CuttingStep());

        // 瀹屾垚姝ラ��
        registerStep(STEP_COMPLETE, StepBuilders.complete(this::executeComplete));

        // 绌烘墜浜や簰鐗规畩姝ラ��
        registerStep(STEP_EMPTY, new EmptyStep());

        for (String id : SPECIAL_STEP_INSTANCES.keySet()) {
            Function<CuttingProcess<?>, Step<UpPlaceBlockEntity>> function = SPECIAL_STEP_INSTANCES.get(id);
            Step<UpPlaceBlockEntity> step = function.apply(this);

            @SuppressWarnings("unchecked")
            Step<T> typedStep = (Step<T>) step;
            registerStep(id, typedStep);
        }
    }

    /**
     * 娉ㄥ唽鐗规畩缁欎簣姝ラ��
     *
     * @param item 瑙﹀彂鐗╁搧
     * @param cutNumber 瑙﹀彂鍒囧壊娆℃暟锛堜粠0寮�濮嬶級
     * @param stepId 姝ラ��ID
     * @param itemsToGive 瑕佺粰浜堢殑鐗╁搧
     */
    public static void registerGiveStep(Item item, int cutNumber, String stepId, ItemStack... itemsToGive) {
        // 娉ㄥ唽姝ラ��
        SPECIAL_STEP_INSTANCES.put(stepId, process ->
                StepBuilders.simple(
                        ctx -> {
                            if (ctx.isServerSide()) {
                                process.currentCut++;
                                process.updateInventory(ctx.blockEntity(), process.currentCut);
                                for (ItemStack stack : itemsToGive) {
                                    ctx.giveStack(stack.copy());
                                }
                                ctx.playSound(SoundEvents.ITEM_PICKUP);
                                ctx.blockEntity().markDirtyAndSync();
                            }
                            return InteractionResult.SUCCESS;
                        },
                        STEP_COMPLETE
                ));

        // 娣诲姞鍒拌Е鍙戞槧灏?
        SPECIAL_STEP_TRIGGERS
                .computeIfAbsent(item, k -> new HashMap<>())
                .put(cutNumber, stepId);
    }

    /**
     * 娉ㄥ唽绌烘墜鎿嶄綔姝ラ�ゆ槧灏勩�?
     *
     * @param item 瑙﹀彂鐗╁搧
     * @param cutNumber 瑙﹀彂鍒囧壊娆℃暟锛堜粠0寮�濮嬶級
     */
    public static void registerEmptyStep(Item item, int... cutNumber) {
        for (int i : cutNumber) {
            SPECIAL_STEP_TRIGGERS
                    .computeIfAbsent(item, k -> new HashMap<>())
                    .put(i, STEP_EMPTY);
        }
    }

    // ============ 杈呭姪鏂规硶 ============

    /**
     * 妫�鏌ュ伐鍏锋槸鍚︿负鏈夋晥鐨勫伐鍏凤紙鑿滃垁锛夈�?
     *
     * @param stack 寰呮��鏌ョ殑鐗╁搧鍫嗘爤
     * @return 濡傛灉鏄�鑿滃垁鍒欒繑鍥瀟rue
     */
    public boolean isValidCuttingTool(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    /**
     * 娑堣�楀伐鍏疯�愪箙搴︺�?
     *
     * @param context 姝ラ�ゆ墽琛屼笂涓嬫�?
     */
    private void consumeToolDurability(StepExecutionContext<T> context) {
        ItemStack tool = context.getHeldItemStack();
        if (!context.isCreateMode() && tool.isDamageableItem()) {
            tool.hurtAndBreak(1, context.player(), p -> p.broadcastBreakEvent(context.hand()));
        }
    }

    /**
     * 鏍规嵁鍒囧壊娆℃暟鏇存柊搴撳瓨鐘舵�併�?
     *
     * @param inventory 鐩�鏍囧簱瀛�
     * @param cutIndex 褰撳墠鍒囧壊娆℃暟
     */
    private void updateInventory(Container inventory, int cutIndex) {
        if (currentRecipe == null || inventory == null) return;

        NonNullList<ItemStack> state = currentRecipe.getCutState(cutIndex);
        int slots = Math.min(state.size(), 5);

        for (int i = 0; i < slots; i++) {
            ItemStack stack = state.get(i);
            inventory.setItem(i, !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY);
        }
    }

    /**
     * 灏嗗簱瀛樹腑鎵�鏈夌墿鍝佺粰浜堢帺瀹躲�?
     *
     * @param inventory 婧愬簱瀛?
     * @param player 鐩�鏍囩帺瀹�
     */
    private void giveAllItemsToPlayer(Container inventory, Player player) {
        if (inventory == null || player == null) return;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.addItem(stack.copy())) {
                    player.drop(stack.copy(), false);
                }

                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 妫�鏌ュ綋鍓嶅垏鍓叉槸鍚﹂渶瑕佹墽琛岀壒娈婃�ラ�ゃ�?
     *
     * @return 鐗规畩姝ラ��ID锛屽�傛灉娌℃湁鍒欒繑鍥瀗ull
     */
    private String checkSpecialStep() {
        Map<Integer, String> triggers = SPECIAL_STEP_TRIGGERS.get(inputStack.getItem());
        if (triggers != null) {
            String stepId = triggers.get(currentCut);
            if (stepId != null && steps.containsKey(stepId)) {
                return stepId;
            }
        }

        return null;
    }

    // ============ AbstractProcess鏂规硶瀹炵幇 ============

    @Override
    protected String getInitialStepId() {
        return STEP_CUT;
    }

    @Override
    protected void onStart(Level world, T blockEntity) {
        currentCut = 0;
        inputStack = blockEntity.getItem(0);

        Optional<CutRecipe> recipe = world.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.CUT.get(), blockEntity, world);

        if (recipe.isPresent()) {
            blockEntity.clearContent();
            currentRecipe = recipe.get();
            savedRecipeId = currentRecipe.getId().toString();
            totalCuts = currentRecipe.getTotalCuts();
        }
    }

    @Override
    protected void onReset() {
        currentRecipe = null;
        currentCut = 0;
        totalCuts = 0;
        savedRecipeId = "";
        inputStack = ItemStack.EMPTY;
    }

    @Override
    protected void beforeGetStep(StepExecutionContext<T> context) {
        // 灏濊瘯鎭㈠�嶉厤鏂�
        if (currentRecipe == null) {
            restoreRecipeFromId(context.world(), savedRecipeId);
        }

        // 妫�鏌ョ壒娈婃�ラ�?
        String specialStepId = checkSpecialStep();
        if (specialStepId != null) {
            jumpToStep(specialStepId);
        }
    }

    // ============ NBT鎸佷箙鍖?============

    @Override
    public void writeToNbt(CompoundTag nbt) {
        super.writeToNbt(nbt);

        nbt.putInt("CurrentCut", currentCut);
        nbt.putInt("TotalCuts", totalCuts);

        if (!inputStack.isEmpty()) {
            CompoundTag inputNbt = new CompoundTag();
            inputStack.save(inputNbt);
            nbt.put("InputItem", inputNbt);
        }

        if (currentRecipe != null) {
            nbt.putString("RecipeId", currentRecipe.getId().toString());
        } else {
            nbt.putString("RecipeId", savedRecipeId);
        }
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        super.readFromNbt(nbt);

        currentCut = nbt.getInt("CurrentCut");
        totalCuts = nbt.getInt("TotalCuts");

        if (nbt.contains("InputItem")) {
            inputStack = ItemStack.of(nbt.getCompound("InputItem"));
        }

        if (nbt.contains("RecipeId")) {
            savedRecipeId = nbt.getString("RecipeId");
        }
    }

    // ============ 鑾峰彇鍣ㄦ柟娉?============

    /**
     * 鑾峰彇褰撳墠娲昏穬鐨勫垏鍓查厤鏂广�?
     *
     * @return 褰撳墠閰嶆柟锛屽�傛灉娌℃湁鍒欒繑鍥瀗ull
     */
    public CutRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    /**
     * 鑾峰彇褰撳墠鍒囧壊杩涘害锛?.0 - 1.0锛夈�?
     *
     * @return 鍒囧壊杩涘害锛?琛ㄧず鏈�寮�濮嬶紝1琛ㄧず瀹屾垚
     */
    public float getProgress() {
        return totalCuts <= 0 ? 0.0f : Math.min((float) currentCut / totalCuts, 1.0f);
    }

    // ============ 璁剧疆鍣ㄦ柟娉?============

    /**
     * 璁剧疆褰撳墠鍒囧壊閰嶆柟銆?
     *
     * @param recipe 鍒囧壊閰嶆柟
     */
    public void setCurrentRecipe(CutRecipe recipe) {
        this.currentRecipe = recipe;

        if (recipe != null) {
            this.totalCuts = recipe.getTotalCuts();
            this.savedRecipeId = recipe.getId().toString();
        } else {
            this.totalCuts = 0;
            this.savedRecipeId = "";
        }
    }

    /**
     * 璁剧疆褰撳墠鍒囧壊娆℃暟銆?
     *
     * @param cut 鍒囧壊娆℃暟
     */
    public void setCurrentCut(int cut) {
        this.currentCut = cut;
    }

    /**
     * 浠庨厤鏂笽D鎭㈠�嶅垏鍓查厤鏂广�?
     *
     * @param world 涓栫晫瀹炰緥
     * @param recipeId 閰嶆柟ID
     */
    public void restoreRecipeFromId(Level world, String recipeId) {
        if (world == null || recipeId == null || recipeId.isEmpty()) {
            BakingProcess.LOGGER.warn("Recipe recovery was not successful");
            return;
        }

        try {
            ResourceLocation id = new ResourceLocation(recipeId);
            Optional<? extends Recipe<?>> recipe = world.getRecipeManager().byKey(id);

            if (recipe.isPresent() && recipe.get() instanceof CutRecipe cutRecipe) {
                setCurrentRecipe(cutRecipe);
            }
        } catch (Exception e) {
            BakingProcess.LOGGER.warn("Ineffective Recipe:{}", recipeId);
        }
    }

    @Override
    protected String getCustomStatusInfo() {
        StringBuilder info = new StringBuilder();

        // 鍒囧壊杩涘害淇℃伅
        info.append("鍒囧壊杩涘害: ").append(currentCut).append("/").append(totalCuts).append("\n");
        info.append("瀹屾垚搴? ").append(String.format("%.1f%%", getProgress() * 100)).append("\n");

        // 閰嶆柟淇℃伅
        if (currentRecipe != null) {
            info.append("褰撳墠閰嶆柟: ").append(currentRecipe.getId().getPath()).append("\n");
            info.append("閰嶆柟姝ラ�ゆ�? ").append(totalCuts).append("\n");
        } else {
            info.append("褰撳墠閰嶆柟: <鏃?\n");
        }

        // 杈撳叆鐗╁搧淇℃伅
        if (!inputStack.isEmpty()) {
            info.append("杈撳叆鐗╁搧: ").append(inputStack.getItem().getDescription().getString());
            if (inputStack.getCount() > 1) {
                info.append(" x").append(inputStack.getCount());
            }
            info.append("\n");
        } else {
            info.append("杈撳叆鐗╁搧: <绌?\n");
        }

        // 鐗规畩姝ラ�や俊鎭�
        String specialStepId = checkSpecialStep();
        if (specialStepId != null) {
            info.append("寰呭�勭悊鐗规畩姝ラ�? ").append(specialStepId).append("\n");
        }

        // NBT鏁版嵁鎭㈠�嶇姸鎬?
        if (savedRecipeId != null && !savedRecipeId.isEmpty()) {
            info.append("淇濆瓨鐨勯厤鏂笽D: ").append(savedRecipeId).append("\n");
        }

        return info.toString();
    }

    // ============ 鐘舵�佺�＄�?============

    /**
     * 鑾峰彇褰撳墠鍒囧壊娴佺▼鐨勭姸鎬佸揩鐓с�?
     *
     * @return 鍒囧壊鐘舵�佸�硅�?
     */
    public CuttingState getState() {
        return new CuttingState(
                currentCut,
                totalCuts,
                isActive,
                inputStack,
                checkSpecialStep() != null
        );
    }

    /**
     * 鍒囧壊娴佺▼鐘舵�佹暟鎹�绫汇�?
     * <p>
     * 鐢ㄤ簬灏佽�呭垏鍓叉祦绋嬬殑褰撳墠鐘舵�侊紝渚夸簬鏁版嵁浼犺緭鍜屾覆鏌撱�?
     *
     * @param currentCut 褰撳墠鍒囧壊娆℃暟
     * @param totalCuts 鎬诲垏鍓叉�℃�?
     * @param hasRecipe 鏄�鍚︽湁娲昏穬閰嶆�?
     * @param hasPendingSpecialStep 鏄�鍚︽湁寰呭�勭悊鐨勭壒娈婃�ラ�?
     */
    public record CuttingState(
            int currentCut,
            int totalCuts,
            boolean hasRecipe,
            ItemStack inputStack,
            boolean hasPendingSpecialStep
    ) {}

    static {
        // 娉ㄥ唽绌烘�ラ�?
        registerEmptyStep(Items.CARROT, 9, 10);
        registerEmptyStep(Items.APPLE, 5);
        registerEmptyStep(Items.COD, 6);
        registerEmptyStep(Items.COOKED_COD, 6);

        // 娉ㄥ唽缁欎簣姝ラ��
        registerGiveStep(Items.CARROT, 11, STEP_CARROT_12,
                new ItemStack(ModItems.CARROT_SLICES.get(), 1),
                new ItemStack(ModItems.CARROT_HEAD.get(), 1));

        registerGiveStep(Items.COD, 7, STEP_COD_8,
                new ItemStack(ModItems.COD_CUBES.get(), 1));
        registerGiveStep(Items.COD, 8, STEP_COD_9,
                new ItemStack(ModItems.COD_CUBES.get(), 1));

        registerGiveStep(Items.COOKED_COD, 7, STEP_COOKED_COD_8,
                new ItemStack(ModItems.COOKED_COD_CUBES.get(), 1));
        registerGiveStep(Items.COOKED_COD, 8, STEP_COOKED_COD_9,
                new ItemStack(ModItems.COOKED_COD_CUBES.get(), 1));

        registerGiveStep(Items.SALMON, 5, STEP_SALMON_6,
                new ItemStack(ModItems.SALMON_CUBES.get(), 1));
        registerGiveStep(Items.SALMON, 6, STEP_SALMON_7,
                new ItemStack(ModItems.SALMON_CUBES.get(), 1));

        registerGiveStep(Items.COOKED_SALMON, 5, STEP_COOKED_SALMON_6,
                new ItemStack(ModItems.COOKED_SALMON_CUBES.get(), 1));
        registerGiveStep(Items.COOKED_SALMON, 6, STEP_COOKED_SALMON_7,
                new ItemStack(ModItems.COOKED_SALMON_CUBES.get(), 1));
    }
}