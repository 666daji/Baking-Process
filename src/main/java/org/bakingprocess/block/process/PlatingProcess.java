package org.bakingprocess.block.process;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bakingprocess.block.entity.PlatableBlockEntity;
import org.bakingprocess.recipe.PlatingRecipe;
import org.bakingprocess.registry.ModRecipeTypes;
import org.jetbrains.annotations.Nullable;
import org.twcore.api.process.AbstractProcess;
import org.twcore.api.process.PlayerAction;
import org.twcore.process.playeraction.PlayerActionCreators;
import org.twcore.process.playeraction.PlayerActionFactory;
import org.twcore.process.playeraction.impl.AddContentPlayerAction;
import org.twcore.process.playeraction.impl.AddItemPlayerAction;
import org.twcore.process.step.Step;
import org.twcore.process.step.StepExecutionContext;
import org.twcore.process.step.StepResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 鎽嗙洏娴佺▼绫伙紝绠＄悊鎽嗙洏鐨勫�氭�ラ�や氦浜掓祦绋嬨�?
 *
 * <p><strong>璁捐�＄壒鐐癸�?/strong></p>
 * <ul>
 *   <li>閫氱敤鍊欓�夐厤鏂瑰垵濮嬪寲锛氭敮鎸佷粠浠绘剰鐘舵�佹仮澶嶆祦绋?/li>
 *   <li>绠�鍖栫殑鐘舵�佺�＄悊锛氭柟鍧楀疄浣撳彧瀛樺偍鎿嶄綔锛屾祦绋嬪彧绠＄悊鍊欓�夊垪琛?/li>
 *   <li>鏃犻渶NBT鎭㈠�嶏細閫�鍑洪噸杩涘悗鑷�鍔ㄩ噸鏂板垵濮嬪寲鍊欓�夊垪琛?/li>
 *   <li>鏀�鎸佹挙閿�鎿嶄綔锛氬畬鎴愮墿鍝佹斁缃�鍚庝粛鍙�鎾ゅ洖缁х画</li>
 * </ul>
 */
public class PlatingProcess<T extends BlockEntity & PlatableBlockEntity> extends AbstractProcess<T> {
    /** 鎵ц�屾搷浣滄�ラ�ょ殑ID */
    public static final String STEP_PERFORM_ACTION = "perform_action";
    /** 瀹屾垚娴佺▼姝ラ�ょ殑ID */
    public static final String STEP_COMPLETE = "complete";

    /** 褰撳墠姝ラ�ょ殑鍊欓�夐厤鏂瑰垪琛?*/
    private final List<PlatingRecipe> candidateRecipes = new ArrayList<>();

    /** 褰撳墠瀹屽叏鍖归厤鐨勯厤鏂癸紙濡傛灉瀛樺湪锛?*/
    @Nullable
    private PlatingRecipe matchedRecipe = null;

    /** 鏍囧織锛氭槸鍚﹀凡鍒濆�嬪寲鍊欓�夐厤鏂瑰垪琛?*/
    private boolean hasInitializedCandidates = false;

    /** 鏍囧織锛氭槸鍚︽�ｅ湪鍖归厤閰嶆柟锛岄槻姝㈤噸鍏� */
    private boolean isMatchingRecipes = false;

    // ==================== 鏋勯�犲櫒鍜屽垵濮嬪寲 ====================

    public PlatingProcess() {
        registerSteps();
    }

    private void registerSteps() {
        registerStep(STEP_PERFORM_ACTION, new PerformActionStep());
        registerStep(STEP_COMPLETE, new CompleteStep());
    }

    // ==================== 姝ラ�ゅ疄鐜扮�?====================

    /**
     * 鎵ц�屾搷浣滄�ラ�わ紝澶勭悊閰嶆柟鎿嶄綔鐨勬墽琛屻�?
     *
     * <p>姝ゆ�ラ�ゆ寜鐓т弗鏍奸『搴忔墽琛岋細</p>
     * <ol>
     *   <li>闃叉�㈤噸鍏�</li>
     *   <li>濡傛灉鍊欓�夊垪琛ㄦ湭鍒濆�嬪寲锛岃皟鐢� initializeCandidates 鍒濆�嬪�?/li>
     *   <li>浠庝笂涓嬫枃鍒涘缓 PlayerAction</li>
     *   <li>鏍规嵁鏄�鍚﹀凡鍒濆�嬪寲閫夋嫨涓嶅悓閫昏緫</li>
     * </ol>
     */
    protected class PerformActionStep implements Step<T> {
        private static final PlayerActionFactory.PlayerActionCreator CREATOR =
                PlayerActionCreators.firstNonNull(
                        PlayerActionFactory.getRegisteredCreator(AddContentPlayerAction.TYPE),
                        PlayerActionFactory.getRegisteredCreator(AddItemPlayerAction.TYPE)
                );

        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            // 闃叉�㈤噸鍏ラ�昏緫
            if (isMatchingRecipes) {
                return StepResult.continueSameStep(InteractionResult.PASS);
            }

            // 鎿嶄綔鐨勬柟鍧楀疄浣?
            PlatableBlockEntity plate = context.blockEntity();

            // 浠庝笂涓嬫枃鍒涘缓棰勬湡鎿嶄綔
            PlayerAction expectedAction = CREATOR.create(context);

            // 宸叉墽琛屾搷浣滃垪琛?
            List<PlayerAction> performedActions = plate.getPerformedActions();
            int currentStep = plate.getStepCount();

            // 濡傛灉鍊欓�夊垪琛ㄦ湭鍒濆�嬪寲锛屽皾璇曞垵濮嬪�?
            if (!hasInitializedCandidates) {
                // 浣跨敤棰勬湡鎿嶄綔锛堝彲鑳戒负null锛夊垵濮嬪寲鍊欓�夊垪琛?
                if (!initializeCandidates(context.world(), plate, expectedAction)) {
                    // 鍒濆�嬪寲澶辫触锛屽皾璇曚笉鍖呭惈褰撳墠鎿嶄綔鐨勫垵濮嬪�?
                    initializeCandidates(context.world(), plate);

                    if (performedActions.isEmpty()) {
                        // 濡傛灉姝ゆ椂鎿嶄綔鍒楄〃涓虹┖鍒欓噸缃�娴佺�?
                        reset();
                    }

                    return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.PASS);
                }

                // 濡傛灉棰勬湡鎿嶄綔涓簄ull锛屾�ラ�よ繑鍥濸ASS锛堟棤鎰熺煡鍒濆�嬪寲锛�
                if (expectedAction == null) {
                    return StepResult.continueSameStep(InteractionResult.PASS);
                }
            }
            // 鍊欓�夊垪琛ㄥ凡鍒濆�嬪�?
            else {
                // 濡傛灉棰勬湡鎿嶄綔涓簄ull锛屾�ラ�ょ洿鎺ヨ繑鍥濸ASS
                if (expectedAction == null) {
                    return StepResult.continueSameStep(InteractionResult.PASS);
                }

                // 浣跨敤棰勬湡鎿嶄綔杩囨护鍊欓�夊垪琛?
                List<PlatingRecipe> matchingRecipes = filterCandidatesByNextAction(
                        expectedAction, performedActions
                );

                // 杩囨护鍒扮殑鍒楄〃涓虹┖鏃讹紝姝ラ�ゅけ璐�
                if (matchingRecipes.isEmpty()) {
                    return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.FAIL);
                }

                // 鏇存柊鍊欓�夊垪琛?
                candidateRecipes.clear();
                candidateRecipes.addAll(matchingRecipes);
            }

            return executeAction(context, plate, expectedAction, currentStep);
        }

        /**
         * 鎵ц�屾搷浣滈�昏緫鐨勫叕鍏遍儴鍒嗐�?
         */
        private StepResult executeAction(StepExecutionContext<T> context, PlatableBlockEntity plate,
                                         PlayerAction action, int currentStep) {
            // 楠岃瘉鏄�鍚﹀彲浠ュ湪姝ゆ�ラ�ゆ墽琛屾搷浣�
            if (!plate.canPerformActionAtStep(currentStep)) {
                resetCandidateState();
                return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.FAIL);
            }

            // 灏濊瘯鎵ц�屾搷浣�
            if (!plate.performAction(currentStep, action)) {
                resetCandidateState();
                return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.FAIL);
            }

            // 鎵ц�屾搷浣滅殑娑堣�楅�昏緫
            action.consume(context);
            plate.setChanged();

            // 妫�鏌ユ槸鍚︽湁瀹屽叏鍖归厤鐨勯厤鏂?
            checkForExactMatch(plate, null);

            return StepResult.continueSameStep(InteractionResult.SUCCESS);
        }
    }

    /**
     * 瀹屾垚娴佺▼姝ラ�わ紝澶勭悊閰嶆柟鐨勫畬鎴愬拰杈撳嚭銆?
     */
    private class CompleteStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            PlatableBlockEntity plate = context.blockEntity();
            ItemStack heldItem = context.getHeldItemStack();

            // 妫�鏌ユ槸鍚︿负瀹屾垚鐗╁搧
            if (!plate.isCompletionItem(heldItem) || heldItem.isEmpty()) {
                return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.FAIL);
            }

            // 妫�鏌ユ槸鍚︽湁瀹屽叏鍖归厤鐨勯厤鏂?
            if (matchedRecipe == null) {
                // 濡傛灉娌℃湁鍖归厤鐨勯厤鏂癸紝浣嗙帺瀹舵墜鎸佸畬鎴愮墿鍝侊紝灏濊瘯閲嶆柊妫�鏌?
                checkForExactMatch(plate, context.world());
                if (matchedRecipe == null) {
                    return StepResult.fail(STEP_PERFORM_ACTION, InteractionResult.FAIL);
                }
            }

            // 鎵ц�屽畬鎴愰�昏緫
            plate.onPlatingComplete(context.world(), context.pos(), matchedRecipe, context.player(), context.hand(), context.hit());
            return StepResult.complete(InteractionResult.SUCCESS);
        }
    }

    // ==================== 鏍稿績绠楁硶鏂规硶 ====================

    /**
     * 閫氱敤鍊欓�夐厤鏂瑰垵濮嬪寲鏂规硶銆?
     *
     * @param world 涓栫晫瀹炰緥
     * @param plate 鎽嗙洏鏂瑰潡瀹炰綋
     * @param expectedAction 褰撳墠瑕佹墽琛岀殑鎿嶄綔锛堝彲鑳戒负绌猴級
     * @return 濡傛灉鎵惧埌鑷冲皯涓�涓�鍊欓�夐厤鏂硅繑鍥?{@code true}
     */
    public boolean initializeCandidates(Level world, PlatableBlockEntity plate, @Nullable PlayerAction expectedAction) {
        isMatchingRecipes = true;
        try {
            RecipeManager recipeManager = world.getRecipeManager();
            List<PlatingRecipe> allRecipes = recipeManager.getAllRecipesFor(ModRecipeTypes.PLATING.get());

            if (allRecipes.isEmpty()) {
                return false;
            }

            List<PlayerAction> performedActions = plate.getPerformedActions();
            Item containerType = plate.getContainerType();

            // 鏋勫缓涓存椂鍖归厤鍒楄〃锛氬凡鎵ц�屾搷浣� + 棰勬湡鎿嶄綔锛堝�傛灉涓嶄负null锛?
            List<PlayerAction> tempMatchingList = new ArrayList<>(performedActions);

            if (expectedAction != null) {
                tempMatchingList.add(expectedAction);
            }

            // 濡傛灉涓存椂鍒楄〃涓虹┖锛屾棤娉曞尮閰嶄换浣曢厤鏂?
            if (tempMatchingList.isEmpty()) {
                return false;
            }

            List<PlatingRecipe> candidates = allRecipes.stream()
                    .filter(recipe -> recipe.getContainer() == containerType)
                    .filter(recipe -> recipe.matchesPrefix(tempMatchingList))
                    .toList();

            if (candidates.isEmpty()) {
                return false;
            }

            candidateRecipes.clear();
            candidateRecipes.addAll(candidates);
            hasInitializedCandidates = true;

            // 妫�鏌ュ畬鍏ㄥ尮閰?
            checkForExactMatch(plate, world);
            return true;
        } finally {
            isMatchingRecipes = false;
        }
    }

    public boolean initializeCandidates(Level world, PlatableBlockEntity plate) {
        return initializeCandidates(world, plate, null);
    }

    /**
     * 鏍规嵁涓嬩竴姝ユ搷浣滆繃婊ゅ�欓�夐厤鏂广�?
     *
     * @param nextAction 涓嬩竴姝ヨ�佹墽琛岀殑鎿嶄�?
     * @param performedActions 宸叉墽琛岀殑鎿嶄綔鍒楄〃
     * @return 杩囨护鍚庣殑鍊欓�夐厤鏂瑰垪琛�锛屽彧鍖呭惈涓嬩竴姝ュ尮閰嶇殑閰嶆柟
     */
    private List<PlatingRecipe> filterCandidatesByNextAction(PlayerAction nextAction, List<PlayerAction> performedActions) {
        return candidateRecipes.stream()
                .filter(recipe -> {
                    // 濡傛灉宸叉墽琛屾搷浣滄暟閲?>= 閰嶆柟鎿嶄綔鏁伴噺锛屼笉鏄�鏈夋晥鍊欓�?
                    if (performedActions.size() >= recipe.getActionCount()) {
                        return false;
                    }

                    // 妫�鏌ュ凡鎵ц�屾搷浣滄槸鍚︽槸閰嶆柟鐨勬湁鏁堝墠缂�
                    if (!recipe.matchesPrefix(performedActions)) {
                        return false;
                    }

                    // 妫�鏌ヤ笅涓�姝ユ槸鍚﹀尮閰?
                    PlayerAction nextRecipeAction = recipe.getNextAction(performedActions.size());
                    return nextRecipeAction != null && nextAction.matches(nextRecipeAction);
                })
                .collect(Collectors.toList());
    }

    /**
     * 妫�鏌ュ綋鍓嶆憜鐩樼姸鎬佹槸鍚︽湁瀹屽叏鍖归厤鐨勯厤鏂广�?
     */
    public void checkForExactMatch(PlatableBlockEntity plate, Level world) {
        matchedRecipe = candidateRecipes.stream()
                .filter(recipe -> recipe.matches(plate, world))
                .findFirst()
                .orElse(null);
    }

    /**
     * 閲嶇疆鍊欓�夐厤鏂圭姸鎬併�?
     */
    private void resetCandidateState() {
        candidateRecipes.clear();
        matchedRecipe = null;
        hasInitializedCandidates = false;
        isMatchingRecipes = false;
    }

    // ==================== 娴佺▼鎺у埗閽╁瓙 ====================

    /**
     * 姝ラ�よ幏鍙栧墠鐨勯�勫�勭悊閽╁瓙銆?
     *
     * <p>褰撶帺瀹舵墜鎸佸畬鎴愮墿鍝佹椂锛屽�傛灉褰撳墠鎽嗙洏鐘舵�佸尮閰嶆煇涓�閰嶆柟锛�
     * 鐩存帴璺宠浆鍒板畬鎴愭�ラ�ゃ�?/p>
     */
    @Override
    protected void beforeGetStep(StepExecutionContext<T> context) {
        T plate = context.blockEntity();
        ItemStack heldItem = context.getHeldItemStack();

        // 濡傛灉鍊欓�夊垪琛ㄦ湭鍒濆�嬪寲锛屽皾璇曞垵濮嬪�?
        if (!hasInitializedCandidates) {
            initializeCandidates(context.world(), plate);
        }

        // 妫�鏌ユ槸鍚︽槸瀹屾垚鐗╁搧
        if (plate.isCompletionItem(heldItem) && !heldItem.isEmpty()) {
            if (matchedRecipe != null) {
                // 璺宠浆鍒板畬鎴愭�ラ�?
                jumpToStep(STEP_COMPLETE);
            }
        }
    }

    // ==================== 鐢熷懡鍛ㄦ湡鏂规硶 ====================

    @Override
    protected String getInitialStepId() {
        return STEP_PERFORM_ACTION;
    }

    @Override
    protected void onStart(Level world, T blockEntity) {
        // 寮�濮嬫柊娴佺▼鏃堕噸缃�鐘舵�?
        resetCandidateState();
    }

    @Override
    protected void onReset() {
        resetCandidateState();
    }

    // ==================== 鐘舵�佹煡璇㈡柟娉?====================

    /**
     * 鑾峰彇褰撳墠鍊欓�夐厤鏂规暟閲忋�?
     */
    public int getCandidateRecipeCount() {
        return candidateRecipes.size();
    }

    /**
     * 鑾峰彇褰撳墠鍖归厤鐨勯厤鏂广�?
     */
    public @Nullable PlatingRecipe getMatchedRecipe() {
        return matchedRecipe;
    }

    /**
     * 妫�鏌ユ槸鍚﹀凡鎵惧埌瀹屽叏鍖归厤鐨勯厤鏂广�?
     */
    public boolean hasExactMatch() {
        return matchedRecipe != null;
    }

    /**
     * 妫�鏌ュ�欓�夊垪琛ㄦ槸鍚﹀凡鍒濆�嬪寲銆?
     */
    public boolean isCandidatesInitialized() {
        return hasInitializedCandidates;
    }

    @Override
    protected String getCustomStatusInfo() {
        StringBuilder info = new StringBuilder();

        // 鍊欓�夐厤鏂逛俊鎭?
        info.append("鍊欓�夐厤鏂规暟閲? ").append(candidateRecipes.size()).append("\n");

        // 鍖归厤鐨勯厤鏂逛俊鎭?
        if (matchedRecipe != null) {
            info.append("瀹屽叏鍖归厤鐨勯厤鏂? ").append(matchedRecipe.getId().getPath()).append("\n");
            info.append("閰嶆柟鎿嶄綔鏁? ").append(matchedRecipe.getActionCount()).append("\n");
            info.append("杈撳嚭鑿滆偞: ").append(matchedRecipe.getDishes()).append("\n");
        } else {
            info.append("瀹屽叏鍖归厤鐨勯厤鏂? <鏃?\n");
        }

        // 鍒濆�嬪寲鐘舵�?
        info.append("鍊欓�夊垪琛ㄥ凡鍒濆�嬪�? ").append(hasInitializedCandidates).append("\n");

        // 鍖归厤鐘舵�?
        info.append("姝ｅ湪鍖归厤閰嶆柟: ").append(isMatchingRecipes).append("\n");

        // 鍊欓�夐厤鏂硅�︽儏锛堜粎鏄剧ず鍓�3涓�锛岄伩鍏嶈緭鍑鸿繃闀匡�?
        if (!candidateRecipes.isEmpty()) {
            info.append("鍊欓�夐厤鏂瑰垪琛?\n");
            int limit = Math.min(candidateRecipes.size(), 3);
            for (int i = 0; i < limit; i++) {
                PlatingRecipe recipe = candidateRecipes.get(i);
                info.append("  ").append(i + 1).append(". ")
                        .append(recipe.getId().getPath())
                        .append(" (鎿嶄綔: ").append(recipe.getActionCount()).append(")\n");
            }
            if (candidateRecipes.size() > limit) {
                info.append("  ... 杩樻湁").append(candidateRecipes.size() - limit).append("涓�閰嶆柟鏈�鏄剧ず\n");
            }
        }
        return info.toString();
    }
}