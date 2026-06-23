package org.bakingprocess.block.process;



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

import org.bakingprocess.item.FlourItem;

import org.bakingprocess.recipe.DoughRecipe;

import org.bakingprocess.registry.ModItems;

import org.bakingprocess.registry.ModRecipeTypes;

import org.dfood.sound.ModSoundGroups;

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



/**

 * 鎻夐潰娴佺▼瀹炵幇绫伙紝绠＄悊鎵�鏈夌姸鎬佹暟鎹�銆?
 */

public class KneadingProcess<T extends BlockEntity & Container> extends AbstractProcess<T> implements Container {

    /** 鍙�鍔犲叆鐩嗕腑鐨勯�濆�栫墿鍝侀泦鍚� */

    public static final Set<Item> CAN_ADD_OTHER = Set.of(ModItems.SALT_CUBES.get(), ModItems.SALT_FLOUR.get(), Items.SUGAR, ModItems.SUGAR_FLOUR.get(), Items.EGG);

    public static final Set<Item> CAN_ADD_FLOUR = Set.of(ModItems.WHEAT_FLOUR.get(), ModItems.COCOA_FLOUR.get());



    /** 娴佺▼姝ラ��ID甯搁噺 */

    public static final String STEP_ADD_FLOUR = "add_flour";

    public static final String STEP_ADD_LIQUID = "add_liquid";

    public static final String STEP_ADD_EXTRA = "add_extra";

    public static final String STEP_KNEAD = "knead";



    /** 棰濆�栫墿鍝佸簱瀛樻Ы浣嶆暟閲� */

    private static final int EXTRA_SLOT_COUNT = 10;

    private static final int TOTAL_SLOTS = EXTRA_SLOT_COUNT;



    /** 棰濆�栫墿鍝佸簱瀛� */

    private final NonNullList<ItemStack> extraInventory;



    /** 闈㈢矇绫诲瀷璁℃暟 */

    private final Map<FlourItem.FlourType, Integer> flourCounts;



    /** 娑蹭綋绫诲瀷璁℃暟 */

    private final Map<Content, Integer> liquidCounts;



    /** 鎻夐潰娆℃暟 */

    private int kneadingCount;



    /** 鏄�鍚﹀凡缁忓�勭悊浜嗚烦杩囬�昏緫 */

    private boolean processedSkip;



    public KneadingProcess() {

        this.extraInventory = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

        this.flourCounts = new HashMap<>();

        this.liquidCounts = new HashMap<>();

        this.kneadingCount = 0;

        this.processedSkip = false;



        // 娉ㄥ唽姝ラ��

        registerSteps();

    }



    private void registerSteps() {

        // 1. 鍔犵矇姝ラ�� - 闇�瑕佸姞鍏?娆￠潰绮?
        registerStep(STEP_ADD_FLOUR, new FlourStep());



        // 2. 鍔犳按姝ラ�� - 闇�瑕佸姞鍏?娆℃恫浣?
        registerStep(STEP_ADD_LIQUID, new LiquidStep());



        // 3. 鍔犻�濆�栫墿鍝佹�ラ�?- 鍙�璺宠繃锛屾渶澶氬姞10娆?
        registerStep(STEP_ADD_EXTRA, new ExtraItemStep());



        // 4. 鎻夐潰姝ラ�� - 闇�瑕佹弶闈?娆?
        registerStep(STEP_KNEAD, new KneadingStep());

    }



    // ============ beforeGetStep瀹炵幇 ============



    @Override

    protected void beforeGetStep(StepExecutionContext<T> context) {

        // 閲嶇疆璺宠繃鏍囪��

        processedSkip = false;



        // 鍙�鍦ㄦ坊鍔犻�濆�栫墿鍝佹�ラ�ゆ椂妫�鏌?
        if (STEP_ADD_EXTRA.equals(currentStepId)) {

            ItemStack heldStack = context.getHeldItemStack();



            // 濡傛灉鐜╁�剁┖鎵嬶紝璺宠浆鍒版弶闈㈡�ラ��

            if (heldStack.isEmpty()) {

                jumpToStep(STEP_KNEAD);

            }

        }

    }



    // ============ 姝ラ�ゅ疄鐜扮�?============



    private class FlourStep implements Step<T> {

        @Override

        public StepResult execute(StepExecutionContext<T> context) {

            ItemStack heldStack = context.getHeldItemStack();



            // 妫�鏌ユ槸鍚︿负闈㈢矇

            if (!(heldStack.getItem() instanceof FlourItem flourItem)) {

                return StepResult.fail(STEP_ADD_FLOUR, InteractionResult.PASS);

            }



            // 鎾�鏀惧埛瀛愭竻鎵�鍙�鐤戞矙鐮剧殑澹伴�?
            context.playSound(SoundEvents.BRUSH_SAND_COMPLETED);



            // 鏈嶅姟鍣ㄧ��鎵ц��

            if (context.isServerSide()) {

                // 娑堣�椾竴涓�闈㈢�?
                if (!context.isCreateMode()){

                    heldStack.shrink(1);

                }



                // 璁板綍闈㈢矇璁℃暟

                flourCounts.put(flourItem.getFlourType(),

                        flourCounts.getOrDefault(flourItem.getFlourType(), 0) + 1);



                // 鏇存柊鏂瑰潡瀹炰綋

                context.blockEntity().setChanged();

            }



            // 妫�鏌ユ槸鍚﹀姞婊?涓�闈㈢�?
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



            // 鑾峰彇瀹瑰櫒鐨勫熀鏈�瀹归噺锛堟瘡涓�瀹瑰櫒鎻愪緵鐨勬恫浣撳崟浣嶆暟锛?
            int capacity = binding.container().getBaseCapacity();



            // 鏈嶅姟鍣ㄧ��鎵ц�屾秷鑰楀拰璁板綍閫昏緫

            if (context.isServerSide()) {



                // 鎾�鏀惧姞鍏ユ恫浣撶殑澹伴�?
                context.playSound(context.getItemSounds().getPlaceSound());



                // 娑堣�?涓�娑蹭綋鐗╁�?
                if (!context.isCreateMode()) {

                    heldStack.shrink(1);



                    // 杩旇繕绌哄�瑰�?
                    ItemStack remainder = binding.container().remainder();

                    context.giveStack(remainder);

                }



                // 璁板綍娑蹭綋璁℃暟锛堜娇鐢↙iquidType浣滀负閿�锛�

                liquidCounts.put(content,

                        liquidCounts.getOrDefault(content, 0) + capacity);



                // 鏇存柊鏂瑰潡瀹炰綋

                context.blockEntity().setChanged();

            }



            // 妫�鏌ユ槸鍚﹀凡鍔犳弧3娆℃恫浣?
            if (isLiquidComplete()) {

                return StepResult.nextStep(STEP_ADD_EXTRA, InteractionResult.SUCCESS);

            } else {

                return StepResult.continueSameStep(InteractionResult.SUCCESS);

            }

        }



        /** 妫�鏌ユ槸鍚﹀凡鍔犳弧3娆℃恫浣?*/

        private boolean isLiquidComplete() {

            return getTotalLiquidCount() >= 3;

        }

    }



    private class ExtraItemStep implements Step<T> {

        @Override

        public StepResult execute(StepExecutionContext<T> context) {

            ItemStack heldStack = context.getHeldItemStack();



            // 妫�鏌ユ墜鎸佺墿鍝佹槸鍚︿负鍙�鎺ュ彈鐨勯�濆�栫墿鍝�

            if (!CAN_ADD_OTHER.contains(heldStack.getItem())) {

                return StepResult.fail(STEP_ADD_EXTRA, InteractionResult.PASS);

            }



            // 妫�鏌ユ槸鍚﹀凡婊?
            if (isExtraFull()) {

                return StepResult.nextStep(STEP_KNEAD, InteractionResult.SUCCESS);

            }



            // 鏈嶅姟鍣ㄧ��鎵ц�屾秷鑰楀拰瀛樺偍閫昏緫

            if (context.isServerSide()) {

                // 鎾�鏀炬櫘閫氱殑鐭冲ご鏀剧疆闊虫晥

                context.playSound(SoundEvents.STONE_PLACE);



                // 鍏堝垱寤虹墿鍝佺殑鍓�鏈�锛岀敤浜庡瓨鍌?
                ItemStack extraCopy = new ItemStack(heldStack.getItem(), 1);



                // 濡傛灉闇�瑕佷繚鐣橬BT鏁版嵁锛屽彲浠ヨ繖鏍峰�嶅�?
                if (heldStack.getTag() != null && heldStack.hasTag()) {

                    extraCopy.setTag(heldStack.getTag().copy());

                }



                // 娑堣�?涓�棰濆�栫墿鍝?
                if (!context.isCreateMode()) {

                    heldStack.shrink(1);

                }



                // 瀛樺偍棰濆�栫墿鍝佸埌搴撳�?
                for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {

                    if (extraInventory.get(i).isEmpty()) {

                        extraInventory.set(i, extraCopy);



                        // 鏍囪�板簱瀛樺凡淇�鏀?
                        break;

                    }

                }



                // 鏇存柊鏂瑰潡瀹炰綋

                context.blockEntity().setChanged();



                // 濡傛灉鍔犳弧浜嗭紝璺宠浆鍒版弶闈㈡�ラ�?
                if (isExtraFull()) {

                    return StepResult.nextStep(STEP_KNEAD, InteractionResult.SUCCESS);

                }

            }



            // 缁х画鎵ц�屽綋鍓嶆�ラ�わ紙娣诲姞鏇村�氶�濆�栫墿鍝侊級

            return StepResult.continueSameStep(InteractionResult.SUCCESS);

        }



        /** 妫�鏌ユ槸鍚﹀凡鍔犳弧10涓�棰濆�栫墿鍝?*/

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



            // 妫�鏌ユ槸鍚︾┖鎵?
            if (!heldStack.isEmpty()) {

                return StepResult.fail(STEP_KNEAD, InteractionResult.PASS);

            }



            context.playSound(ModSoundGroups.BREAD.getPlaceSound());



            // 鏈嶅姟鍣ㄧ��鎵ц�屾弶闈㈤�昏緫

            if (context.isServerSide()) {

                kneadingCount++;



                // 濡傛灉鏄�绗�浜屾�℃弶闈�锛屽埗浣滈潰鍥?
                if (kneadingCount >= 2) {

                    ItemStack result = craftDough(context.world());

                    if (!result.isEmpty()) {



                        // 灏嗛潰鍥㈡斁鍏ョ泦鏂瑰潡瀹炰綋

                        context.blockEntity().setItem(0, result);



                        // 閲嶇疆娴佺▼

                        reset();



                        // 鏇存柊鏂瑰潡瀹炰綋

                        context.blockEntity().setChanged();

                        return StepResult.complete(InteractionResult.SUCCESS);

                    } else {

                        // 娌℃湁鍖归厤鐨勯厤鏂癸紝澶辫触

                        reset();

                        context.blockEntity().setChanged();

                        return StepResult.fail(null, InteractionResult.FAIL);

                    }

                } else {

                    // 绗�涓�娆℃弶闈�锛岀户缁�鎵ц�屽綋鍓嶆�ラ��

                    context.blockEntity().setChanged();

                    return StepResult.continueSameStep(InteractionResult.SUCCESS);

                }

            }



            return StepResult.continueSameStep(InteractionResult.SUCCESS);

        }

    }



    // ============ 闈㈠洟鍒朵綔 ============



    private ItemStack craftDough(Level world) {

        // 鏌ユ壘鍖归厤鐨勯厤鏂?
        Optional<DoughRecipe> recipe = world.getRecipeManager()

                .getRecipeFor(ModRecipeTypes.DOUGH_MAKING.get(), this, world);



        if (recipe.isPresent()) {

            // 娑堣�楁墍鏈夊師鏂?
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

     * 妫�鏌ュ唴瀹圭墿鏄�鍚︽槸鍏佽�告坊鍔犵殑娑蹭綋銆?
     *

     * @param content 瑕佹��鏌ョ殑鍐呭�圭�?
     * @return 鏄�鍚﹀彲浠ュ湪娑蹭綋姝ラ�や腑娣诲姞鐨勫唴瀹?
     */

    public static boolean isAllowedContent(Content content) {

        return content.isIn(Contents.BASE_LIQUID);

    }



    // ============ 鐘舵�佽幏鍙栨柟娉?============



    public Map<FlourItem.FlourType, Integer> getFlourCounts() {

        return new HashMap<>(flourCounts);

    }



    /**

     * 鑾峰彇娑蹭綋绫诲瀷璁℃暟

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



    // ============ AbstractProcess鏂规硶瀹炵幇 ============



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



    // ============ NBT鎸佷箙鍖?============



    @Override

    public void writeToNbt(CompoundTag nbt) {

        super.writeToNbt(nbt);



        // 淇濆瓨棰濆�栫墿鍝佸簱瀛�

        ContainerHelper.saveAllItems(nbt, extraInventory);



        // 淇濆瓨闈㈢矇璁℃暟

        CompoundTag floursNbt = new CompoundTag();

        for (Map.Entry<FlourItem.FlourType, Integer> entry : flourCounts.entrySet()) {

            floursNbt.putInt(entry.getKey().getSerializedName(), entry.getValue());

        }

        nbt.put("flours", floursNbt);



        // 淇濆瓨娑蹭綋璁℃暟锛圠iquidType浣滀负閿�锛�

        CompoundTag liquidsNbt = new CompoundTag();

        for (Map.Entry<Content, Integer> entry : liquidCounts.entrySet()) {

            ResourceLocation id = TWRegistries.CONTENT.getKey(entry.getKey());

            if (id != null) {

                liquidsNbt.putInt(id.toString(), entry.getValue());

            }

        }

        nbt.put("liquids", liquidsNbt);



        // 淇濆瓨鎻夐潰娆℃暟

        nbt.putInt("kneading_count", kneadingCount);



        // 淇濆瓨璺宠繃鏍囪��

        nbt.putBoolean("processed_skip", processedSkip);

    }



    @Override

    public void readFromNbt(CompoundTag nbt) {

        super.readFromNbt(nbt);



        // 娓呯┖鐜版湁鏁版嵁

        extraInventory.clear();

        flourCounts.clear();

        liquidCounts.clear();



        // 璇诲彇棰濆�栫墿鍝佸簱瀛�

        ContainerHelper.loadAllItems(nbt, extraInventory);



        // 璇诲彇闈㈢矇璁℃暟

        if (nbt.contains("flours")) {

            CompoundTag floursNbt = nbt.getCompound("flours");

            for (String key : floursNbt.getAllKeys()) {

                flourCounts.put(FlourItem.FlourType.fromId(key), floursNbt.getInt(key));

            }

        }



        // 璇诲彇娑蹭綋璁℃暟锛堜粠瀛楃�︿覆杞�鎹�涓篖iquidType锛?
        if (nbt.contains("liquids")) {

            CompoundTag liquidsNbt = nbt.getCompound("liquids");

            for (String key : liquidsNbt.getAllKeys()) {

                Content content = TWRegistries.CONTENT.get(ResourceLocation.tryParse(key));

                if (content != null && isAllowedContent(content)) {

                    liquidCounts.put(content, liquidsNbt.getInt(key));

                }

            }

        }



        // 璇诲彇鎻夐潰娆℃暟

        kneadingCount = nbt.getInt("kneading_count");



        // 璇诲彇璺宠繃鏍囪��

        processedSkip = nbt.getBoolean("processed_skip");

    }



    // ============ Inventory鎺ュ彛瀹炵幇 ============



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



        // 闈㈢矇璁℃暟璇︽儏

        info.append("闈㈢矇: ").append(getTotalFlourCount()).append("/3\n");

        if (!flourCounts.isEmpty()) {

            flourCounts.forEach((type, count) -> info.append("  - ").append(type.getSerializedName()).append(": ").append(count).append("\n"));

        }



        // 娑蹭綋璁℃暟璇︽儏

        info.append("娑蹭綋: ").append(getTotalLiquidCount()).append("/3\n");

        if (!liquidCounts.isEmpty()) {

            liquidCounts.forEach((type, count) -> info.append("  - ").append(type.toString()).append(": ").append(count).append("\n"));

        }



        // 棰濆�栫墿鍝佽�︽儏

        info.append("棰濆�栫墿鍝�: ").append(getExtraItemCount()).append("/").append(EXTRA_SLOT_COUNT).append("\n");

        if (getExtraItemCount() > 0) {

            for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {

                ItemStack stack = extraInventory.get(i);

                if (!stack.isEmpty()) {

                    info.append("  - 妲戒綅").append(i + 1).append(": ")

                            .append(stack.getItem().getDescription().getString());

                    if (stack.getCount() > 1) {

                        info.append(" x").append(stack.getCount());

                    }

                    info.append("\n");

                }

            }

        }



        // 鎻夐潰娆℃暟

        info.append("鎻夐潰娆℃暟: ").append(kneadingCount).append("/2\n");



        // 璺宠繃閫昏緫鐘舵�?
        info.append("璺宠繃閫昏緫宸插�勭�? ").append(processedSkip).append("\n");



        // 搴撳瓨绌虹姸鎬?
        info.append("搴撳瓨鏄�鍚︿负绌�: ").append(isEmpty()).append("\n");



        return info.toString();

    }



    // ============ 娑蹭綋绫诲瀷鏋氫妇 ============



    /**

     * 鑾峰彇褰撳墠娴佺▼鐘舵�佺殑鎬诲拰

     * @return 鐘舵�佸�硅薄锛屽寘鍚�褰撳墠姝ラ�ゃ�佷笂涓�涓�姝ラ�ゅ拰鍚勬�ラ�よ�℃暟淇℃�?
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

     * 鐘舵�佹暟鎹�绫�

     */

    public record KneadingState(String currentStepId, String previousStepId, int flourCount, int liquidCount,

                                int extraItemCount, int kneadingCount, boolean isActive) {}

}