package org.bakingprocess.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.CombustionFirewoodBlock;
import org.bakingprocess.block.FirewoodBlock;
import org.bakingprocess.block.HeatResistantSlateBlock;
import org.bakingprocess.item.ModSharpKitchenwareItem;
import org.bakingprocess.recipe.StoveRecipe;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModRecipeTypes;
import org.bakingprocess.util.BakingProcessUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.twcore.api.block.UpPlaceBlockEntity;
import org.twcore.api.blockpile.CubeBlockPile;
import org.twcore.api.blockpile.CubeBlockPileEntity;
import org.twcore.api.blockpile.CubeBlockPileReference;
import org.twcore.blockpile.ClientCubeBlockPileReference;
import org.twcore.blockpile.ServerCubeBlockPileReference;

import java.util.*;

public class HeatResistantSlateBlockPileEntity extends UpPlaceBlockEntity implements CubeBlockPileEntity, WorldlyContainer, RecipeHolder, StackedContentsCompatible {
    protected static final int MIN_CHECK_INTERVAL = 10;
    protected static final String PILE_REF_KEY = "CubeBlockPileRef";
    protected static final double INPUT_OFFSET_Y = 0.1;
    protected static final int MIN_BAKING_TIME = 100; // 鏈�灏忕儤鐑ゆ椂闂?
    protected static final Logger LOGGER = BakingProcess.LOGGER;

    protected CubeBlockPileReference cubeBlockPileRef;
    @Nullable
    protected BlockPattern.BlockPatternMatch currentStoveResult;
    @Nullable
    protected Direction resultDirection;
    protected int stoveStructureType = -1;
    protected boolean isValidStove = false;
    protected CompoundTag refNbt;

    protected Set<BlockPos> firewoodPos = new HashSet<>(); // 缁戝畾鐨勬煷鐏�鍫嗕綅缃�闆嗗悎
    protected Set<CombustionFirewoodBlockEntity> firewoodEntities = new HashSet<>(); // 缂撳瓨鐨勬煷鐏�鍫嗘柟鍧楀疄浣撻泦鍚�

    /**@see HeatResistantSlateBlockPileEntity#getBakingSpeed() */
    protected int bakingTime;
    protected int bakingTimeTotal;

    protected final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
    protected final RecipeManager.CachedCheck<Container, ? extends StoveRecipe> matchGetter;
    @Nullable
    protected Recipe<?> lastRecipe;

    public int age;
    protected int lastCheckTime = 0;

    public HeatResistantSlateBlockPileEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.HEAT_RESISTANT_SLATE.get(), pos, state, 1);
        this.matchGetter = RecipeManager.createCheck(ModRecipeTypes.STOVE.get());
        this.bakingTime = 0;
        this.bakingTimeTotal = 0;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        // 淇濆瓨澶氭柟鍧楀紩鐢ㄤ俊鎭?
        if (cubeBlockPileRef != null && !cubeBlockPileRef.isDisposed()) {
            nbt.put(PILE_REF_KEY, cubeBlockPileRef.toNbt());
        }

        nbt.putBoolean("IsStoveValid", isValidStove);
        nbt.putInt("StoveStructureType", stoveStructureType);

        // 淇濆瓨鏌寸伀鍫嗕綅缃�闆嗗�?
        if (!firewoodPos.isEmpty()) {
            ListTag firewoodList = new ListTag();
            for (BlockPos pos : firewoodPos) {
                firewoodList.add(BakingProcessUtils.serializeBlockPos(pos));
            }
            nbt.put("FirewoodPositions", firewoodList);
        }

        if (resultDirection != null) {
            nbt.putString("resultDirection", resultDirection.getSerializedName());
        }

        // 淇濆瓨鐑樼儰杩涘害
        nbt.putInt("BakingTime", bakingTime);
        nbt.putInt("BakingTimeTotal", bakingTimeTotal);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        // 淇濆瓨nbt鏁版嵁鐢ㄤ簬閲嶅缓寮曠敤
        if (nbt.contains(PILE_REF_KEY)) {
            this.refNbt = nbt.getCompound(PILE_REF_KEY);
        }

        // 鍙�鍦ㄦ湇鍔＄��閲嶅缓鍔熻兘鎬х殑CubeBlockPileReference
        if (level != null && !level.isClientSide) {
            if (this.refNbt != null) {
                CubeBlockPileReference ref = ServerCubeBlockPileReference.fromNbt(level, refNbt);
                if (ref != null) {
                    this.cubeBlockPileRef = ref;
                }
            }
        } else {
            // 鍦ㄥ�㈡埛绔�锛屽彧閲嶅缓鏄剧ず淇℃伅
            if (this.refNbt != null) {
                this.cubeBlockPileRef = new ClientCubeBlockPileReference(refNbt);
            }
        }

        this.isValidStove = nbt.getBoolean("IsStoveValid");
        this.stoveStructureType = nbt.getInt("StoveStructureType");

        // 璇诲彇鏌寸伀鍫嗕綅缃�闆嗗�?
        firewoodPos.clear();
        if (nbt.contains("FirewoodPositions")) {
            ListTag firewoodList = nbt.getList("FirewoodPositions", 10);
            for (int i = 0; i < firewoodList.size(); i++) {
                CompoundTag posTag = firewoodList.getCompound(i);
                BlockPos pos = BakingProcessUtils.deserializeBlockPos(posTag);
                if (pos != null) {
                    firewoodPos.add(pos);
                }
            }
        }

        if (nbt.contains("resultDirection")){
            resultDirection = Direction.byName(nbt.getString("resultDirection"));
        }

        // 璇诲彇鐑樼儰杩涘害
        bakingTime = nbt.getInt("BakingTime");
        bakingTimeTotal = nbt.getInt("BakingTimeTotal");
    }

    @Override
    public VoxelShape getContentShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getBlockShape(this.getInventoryBlockState(), world, pos);
    }

    private VoxelShape getBlockShape(BlockState blockState, BlockGetter world, BlockPos pos) {
        Block block = blockState.getBlock();

        if (block == Blocks.AIR) {
            return Shapes.empty();
        }

        VoxelShape shape = block.defaultBlockState().getShape(world, pos);

        return shape.move(0.0, INPUT_OFFSET_Y, 0.0);
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Container tempInventory = new SimpleContainer(stack);
        return this.matchGetter.getRecipeFor(tempInventory, this.level).isPresent();
    }

    @Override
    public Result tryAddItem(ItemStack stack, BlockHitResult hit) {
        if (stack.isEmpty() || !isValidItem(stack)) {
            return Result.of(InteractionResult.PASS);
        }

        // 灏濊瘯鏀剧疆鐨勬柊鍫嗘爤
        ItemStack newStack = stack.copy();

        // 鐩存帴鏀剧疆鐗╁搧
        int maxCount = getMaxInputCount(newStack);
        if (isEmpty() && maxCount != 0) {
            newStack.setCount(Math.min(newStack.getCount(), maxCount));
            this.setItem(0, newStack);
            this.markDirtyAndSync();
            return Result.of(newStack, InteractionResult.SUCCESS);
        }
        return Result.of(InteractionResult.PASS);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, ItemStack placeStack, List<ItemStack> itemStacks) {
        playSound(world, pos, placeStack, true);

        if (!player.isCreative()) {
            placeStack.shrink(getItem(0).getCount());
        }
    }

    @Override
    public Result tryFetchItem(Player player, BlockHitResult hit) {
        ItemStack contentStack = this.getItem(0);

        if (contentStack.isEmpty()) {
            return Result.of(InteractionResult.PASS);
        }

        // 鏅�閫氱墿鍝佺殑鍙栧嚭閫昏緫
        if (!player.isCreative() && !player.addItem(contentStack)) {
            player.drop(contentStack, false);
        }

        this.setItem(0, ItemStack.EMPTY);

        this.markDirtyAndSync();
        return Result.of(contentStack.copy(), InteractionResult.SUCCESS);
    }

    @Override
    public void onFetch(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, List<ItemStack> fetchStacks) {
        super.onFetch(state, world, pos, player, hand, hit, fetchStacks);
        ItemStack handStack = player.getItemInHand(hand);

        if (handStack.getItem() instanceof ModSharpKitchenwareItem){
            handStack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }

    /**
     * 鑾峰彇褰撳墠鐗╁搧鏍忎腑鐨勭墿鍝佸�瑰簲鐨勬柟鍧楃姸鎬?
     * @return 鐗╁搧瀵瑰簲鐨勬柟鍧楃姸鎬?
     */
    public BlockState getInventoryBlockState() {
        ItemStack stack = this.inventory.get(0);
        Direction facing = Direction.EAST;

        if (resultDirection != null){
            facing = this.resultDirection;
        }
        return BakingProcessUtils.createCountBlockstate(stack, facing);
    }

    /**
     * 璁剧疆澶氭柟鍧楀紩鐢�锛屽苟鏍囪�伴渶瑕佸悓姝?
     */
    @Override
    public void setCubeBlockPileReference(@Nullable CubeBlockPileReference ref) {
        // 瀹㈡埛绔�涓嶅厑璁哥洿鎺ヨ�剧疆寮曠敤
        if (level != null && (level.isClientSide || ref instanceof ClientCubeBlockPileReference)) {
            return;
        }

        // 鍏堟竻鐞嗘棫鐨勫紩鐢?
        if (this.cubeBlockPileRef != null) {
            this.cubeBlockPileRef.dispose();
        }

        this.cubeBlockPileRef = ref;

        // 閲嶇疆缁撴瀯妫�鏌ョ姸鎬?
        this.currentStoveResult = null;
        this.stoveStructureType = -1;
        this.isValidStove = false;

        // 鏍囪�伴渶瑕佷繚瀛樺拰鍚屾��
        this.setChanged();

        if (level != null) {
            // 閫氱煡瀹㈡埛绔�鏇存�?
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 鑾峰彇澶氭柟鍧楀紩鐢?
     */
    @Nullable
    @Override
    public CubeBlockPileReference getCubeBlockPileReference() {
        return cubeBlockPileRef;
    }

    @Override
    public BlockPos getCubeBlockPilePos() {
        return this.worldPosition;
    }

    /**
     * 妫�鏌ユ槸鍚︿负澶氭柟鍧楃粨鏋勭殑涓绘柟鍧?
     */
    public boolean isMasterBlock() {
        return cubeBlockPileRef != null && cubeBlockPileRef.isMasterBlock();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // 娓呯悊寮曠敤
        if (cubeBlockPileRef != null) {
            cubeBlockPileRef.dispose();
            cubeBlockPileRef = null;
        }
    }

    /**
     * 妫�鏌ユ柟鍧楀爢鏄�鍚︾�﹀悎鐐夊瓙缁撴瀯瑕佹眰
     */
    public boolean isValidStoveStructure() {
        if (cubeBlockPileRef == null || cubeBlockPileRef.isDisposed()) {
            return false;
        }
        if (cubeBlockPileRef instanceof ServerCubeBlockPileReference multiBlockReference){
            CubeBlockPile.PatternRange range = multiBlockReference.getCubeBlockPile().getRange();

            int width = range.getWidth();
            int height = range.getHeight();
            int depth = range.getDepth();

            // 鐐夊瓙缁撴瀯瑕佹眰锛氭按骞虫柟鍚戯紝楂樺害涓?
            if (height != 1) {
                return false;
            }

            // 妫�鏌ユ槸鍚︿负鏈夋晥灏哄�革�?x1, 1x2, 2x2, 2x3
            return (width == 1 && depth == 1) ||  // 1x1
                    (width == 1 && depth == 2) ||  // 1x2
                    (width == 2 && depth == 1) ||
                    (width == 2 && depth == 2) ||  // 2x2
                    (width == 3 && depth == 2) ||
                    (width == 2 && depth == 3);    // 2x3
        }
        return false;
    }

    /**
     * 鑾峰彇鐐夊瓙缁撴瀯鐨勭被鍨嬬储寮?
     */
    public int getStoveStructureType() {
        if (!isValidStoveStructure()) {
            return -1;
        }

        if (cubeBlockPileRef instanceof ServerCubeBlockPileReference multiBlockReference){
            CubeBlockPile.PatternRange range = multiBlockReference.getCubeBlockPile().getRange();
            int width = range.getWidth();
            int depth = range.getDepth();

            if (width == 1 && depth == 1) return 1;  // 1x1
            if (width == 2 && depth == 1) return 2;  // 2x1
            if (width == 1 && depth == 2) return 2;  // 1x2
            if (width == 2 && depth == 2) return 3;  // 2x2
            if (width == 3 && depth == 2) return 4;  // 3x2
            if (width == 2 && depth == 3) return 4;  // 2x3
        }
        return -1;
    }

    /**
     * 鑾峰彇瀵瑰簲鐨勭倝瀛愬浘妗?
     * @param index 鍥炬�堢储寮�
     * @return 瀵瑰簲鐨勭倝瀛愬浘妗?
     */
    @Nullable
    public BlockPattern getStovePattern(int index){
        if (getBlockState().getBlock() instanceof HeatResistantSlateBlock heatResistantSlateBlock){
            return heatResistantSlateBlock.getStovePattern(index);
        }
        return null;
    }

    /**
     * 鑾峰彇褰撳墠鐐夊瓙缁撴瀯妫�鏌ョ粨鏋?
     */
    @Nullable
    public BlockPattern.BlockPatternMatch getCurrentStoveResult() {
        return currentStoveResult;
    }

    /**
     * 鑾峰彇褰撳墠鐐夊瓙缁撴瀯绫诲瀷
     */
    public int getCurrentStoveStructureType() {
        return stoveStructureType;
    }

    /**
     * 妫�鏌ュ綋鍓嶆槸鍚︽湁鏁堢殑鐐夊瓙缁撴瀯
     */
    public boolean isStoveValid() {
        return isValidStove;
    }

    /**
     * 褰撶倝瀛愮粨鏋勬湁鏁堟椂璋冪敤
     */
    private void onStoveStructureValid(Level world, BlockPos pos, BlockPattern.BlockPatternMatch result, int patternType) {
        // 缁撴瀯鍖归厤鎴愬姛锛岀粦瀹氭煷鐏�鍫�
        bindFirewoodFromStructure(world, result, patternType);
        if (this.currentStoveResult != null) {
            this.resultDirection = this.currentStoveResult.getForwards();
        }
    }

    /**
     * 褰撶倝瀛愮粨鏋勬棤鏁堟椂璋冪敤
     */
    private void onStoveStructureInvalid(Level world, BlockPos pos) {
        clearFirewoodBinding();
        if (this.currentStoveResult == null){
            this.resultDirection = null;
        }
    }

    /**
     * 鎵╁ぇ鎼滅储鑼冨洿浠ユ彁楂樺尮閰嶆垚鍔熺巼
     */
    private BlockPattern.BlockPatternMatch searchAround(Level world, BlockPos searchPos, int patternType, BlockPattern pattern) {
        for (int i = 0; i < patternType + 2; i++) {
            List<BlockPos> params = Arrays.asList(
                    searchPos.relative(Direction.EAST, i),
                    searchPos.relative(Direction.WEST, i),
                    searchPos.relative(Direction.NORTH, i),
                    searchPos.relative(Direction.SOUTH, i)
            );

            for (BlockPos pos : params) {
                BlockPattern.BlockPatternMatch result = pattern.find(world, pos);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void bindFirewoodFromStructure(Level world, BlockPattern.BlockPatternMatch result, int patternType) {
        // 鑾峰彇鎵�鏈?~'瀛楃�﹀�瑰簲鐨勪綅缃?
        Set<BlockPos> newFirewoodPositions = BakingProcessUtils.findTargetPositionsFromPattern(result, HeatResistantSlateBlockPileEntity::isFirewoodPositionPredicate);

        if (!newFirewoodPositions.isEmpty()) {
            this.firewoodPos = newFirewoodPositions;
            this.firewoodEntities.clear(); // 灏嗗湪涓嬫��tick鏃堕噸鏂拌幏鍙?
            setChanged();
        } else {
            clearFirewoodBinding();
            LOGGER.warn("Failed to find firewood positions in pattern type: {}", patternType);
        }
    }

    /**
     * 妫�鏌ヤ綅缃�鏄�鍚﹀尮閰嶆煷鐏�鍫嗙殑璋撹瘝鏉′�?
     * @param cachedPos 缂撳瓨鐨勬柟鍧椾綅缃?
     * @return 濡傛灉浣嶇疆鏄�绌烘皵鎴栨湁鏁堢殑鏌寸伀鍫嗗垯杩斿洖true锛屽惁鍒欒繑鍥瀎alse
     */
    private static boolean isFirewoodPositionPredicate(@NotNull BlockInWorld cachedPos) {
        BlockState state = cachedPos.getState();
        return state.isAir() ||
                state.getBlock() instanceof FirewoodBlock ||
                state.getBlock() instanceof CombustionFirewoodBlock;
    }

    /**
     * 楠岃瘉浣嶇疆鏄�鍚︽槸鏈夋晥鐨勬煷鐏�鍫?
     * @param world 涓栫晫
     * @param pos 浣嶇疆
     * @return 濡傛灉浣嶇疆鏄�鏈夋晥鐨勬煷鐏�鍫嗗垯杩斿洖true锛屽惁鍒欒繑鍥瀎alse
     */
    private boolean isValidFirewoodPosition(@NotNull Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof CombustionFirewoodBlock;
    }

    /**
     * 娓呴櫎鏌寸伀鍫嗙粦瀹?
     */
    private void clearFirewoodBinding() {
        this.firewoodPos.clear();
        this.firewoodEntities.clear();
        setChanged();
    }

    /**
     * 鏇存柊缁戝畾鐨勬煷鐏�鍫�
     */
    private void updateFirewood(Level world) {
        if (world.isClientSide) return;

        // 濡傛灉娌℃湁缁戝畾鏌寸伀鍫嗕綅缃�锛屾竻绌虹紦瀛�
        if (firewoodPos.isEmpty()) {
            firewoodEntities.clear();
            return;
        }

        // 鑾峰彇鎵�鏈夋煷鐏�鍫嗘柟鍧楀疄浣�
        if (firewoodEntities.isEmpty() || firewoodEntities.stream().anyMatch(BlockEntity::isRemoved)) {
            firewoodEntities.clear();
            for (BlockPos pos : firewoodPos) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof CombustionFirewoodBlockEntity combustionBE) {
                    firewoodEntities.add(combustionBE);
                }
            }
        }
    }

    public Set<BlockPos> getFirewoodPositions() {
        return Collections.unmodifiableSet(firewoodPos);
    }

    /**
     * 娉ㄦ剰锛氬皾璇曚慨鏀规�ら泦鍚堟槸娌℃湁鏁堟灉鐨�
     * @return 缁戝畾鐨勬煷鐏�鍫嗘柟鍧楀疄浣撶殑闆嗗�?
     */
    public Set<CombustionFirewoodBlockEntity> getFirewoodEntities() {
        return firewoodEntities;
    }

    public int getActiveFirewoodCount() {
        return (int) firewoodEntities.stream()
                .filter(Objects::nonNull)
                .filter(CombustionFirewoodBlockEntity::isCombusting)
                .count();
    }

    /**
     * 澶勭悊鐑樼儰閫昏緫
     */
    private void processBaking(Level world) {
        // 妫�鏌ユ槸鍚︽湁杈撳叆鐗╁搧
        if (isEmpty()) {
            resetBakingProgress();
            return;
        }

        // 鑾峰彇鍖归厤鐨勯厤鏂?
        StoveRecipe recipe = this.matchGetter.getRecipeFor(this, world).orElse(null);
        if (recipe == null) {
            resetBakingProgress();
            return;
        }

        // 鍒濆�嬪寲鐑樼儰鎬绘椂闂?
        if (bakingTimeTotal == 0) {
            bakingTimeTotal = Math.max(recipe.getBakingTimeForInput(getItem(0).getCount()), MIN_BAKING_TIME);
        }

        // 鏍规嵁鐑�閲忕瓑绾у喅瀹氱儤鐑ら�熷害
        int bakingSpeed = getBakingSpeed();

        // 澧炲姞鐑樼儰杩涘害
        bakingTime += bakingSpeed;

        // 妫�鏌ユ槸鍚︾儤鐑ゅ畬鎴?
        if (bakingTime >= bakingTimeTotal) {
            completeBaking(world, recipe);
        }

        // 鏍囪�伴渶瑕佸悓姝ワ紙鐢ㄤ簬瀹㈡埛绔�娓叉煋杩涘害锛�
        markDirtyAndSync();
    }

    /**
     * 鑾峰彇鐑樼儰閫熷害
     */
    private int getBakingSpeed() {
        if (firewoodEntities.isEmpty()) {
            return 0;
        }

        int activeFirewoodCount = 0;
        double totalEffectiveSpeed = 0;

        // 璁＄畻姣忎釜鏌寸伀鍫嗙殑鏈夋晥閫熷害
        for (CombustionFirewoodBlockEntity firewoodBlock : firewoodEntities) {
            if (firewoodBlock != null && firewoodBlock.isCombusting()) {
                activeFirewoodCount++;

                // 鍗曚釜鏌寸伀鍫嗙殑鍩虹��閫熷害 + 鐑�閲忓姞鎴�
                double individualSpeed = 10 + (firewoodBlock.getHeatLevel() - 1);

                // 搴旂敤鏀剁泭閫掑噺锛氫娇鐢ㄥ钩鏂规牴鍑芥暟
                double effectiveSpeed = individualSpeed / Math.pow(activeFirewoodCount, 0.7);
                totalEffectiveSpeed += effectiveSpeed;
            }
        }

        if (activeFirewoodCount == 0) {
            return 0;
        }

        // 鏈�缁堢粨鏋滃彇鏁达紝骞剁‘淇濊嚦灏戜负10
        int result = (int) Math.round(totalEffectiveSpeed);
        return Math.max(10, result);
    }

    /**
     * 瀹屾垚鐑樼儰锛屼骇鍑虹粨鏋?
     */
    private void completeBaking(Level world, StoveRecipe recipe) {
        ItemStack inputStack = getItem(0);
        ItemStack outputStack = recipe.assemble(this, world.registryAccess());

        if (inputStack.isEmpty() || outputStack.isEmpty()) {
            resetBakingProgress();
            return;
        }

        // 娑堣�楄緭鍏ョ墿鍝?
        setItem(0, outputStack);

        setRecipeUsed(recipe);
        recipesUsed.addTo(recipe.getId(), 1);

        resetBakingProgress();
        world.playSound(null, worldPosition, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    /**
     * 閲嶇疆鐑樼儰杩涘害
     */
    private void resetBakingProgress() {
        this.bakingTime = 0;
        this.bakingTimeTotal = 0;
        setChanged();
    }

    /**
     * 鑾峰彇褰撳墠鐑樼儰杩涘害鐨勭櫨鍒嗘瘮
     * @return 鐑樼儰杩涘害鐧惧垎姣旓紙0.0 - 1.0锛?
     */
    public float getBakingProgress() {
        if (bakingTimeTotal > 0) {
            return (float) bakingTime / bakingTimeTotal;
        }
        return 0.0f;
    }

    /**
     * 妫�鏌ユ槸鍚﹀彲浠ュ紑濮嬬儤鐑?
     */
    public boolean canBake() {
        if (isEmpty() || !hasHeat() || !isValidStove) {
            return false;
        }

        StoveRecipe recipe = this.matchGetter.getRecipeFor(this, level).orElse(null);
        return recipe != null;
    }

    public int getBakingTime() {
        return bakingTime;
    }

    public int getBakingTimeTotal() {
        return bakingTimeTotal;
    }

    public boolean hasHeat(){
        for (CombustionFirewoodBlockEntity firewood : firewoodEntities) {
            if (firewood.isCombusting()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBaking() {
        return bakingTime > 0;
    }

    /**
     * 鑾峰彇璇ラ厤鏂瑰厑璁哥殑鏈�澶ц緭鍏ユ暟閲?
     * @param stack 瑕佽緭鍏ョ殑鐗╁搧鍫嗘爤
     * @return 璇ラ厤鏂瑰厑璁哥殑鏈�澶ц緭鍏ユ暟閲忥紝濡傛灉涓?锛岃〃绀轰负鏈�鎵惧埌鍖归厤鐨勯厤鏂�
     */
    protected int getMaxInputCount(ItemStack stack){
        Container tempInventory = new SimpleContainer(stack);
        Optional<? extends StoveRecipe> expectedRecipe = this.matchGetter.getRecipeFor(tempInventory, this.level);

        return expectedRecipe.map(StoveRecipe::getMaxInputCount).orElse(0);
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

    public static void tick(Level world, BlockPos pos, BlockState state, @NotNull HeatResistantSlateBlockPileEntity blockEntity) {
        // 姣弔ick澧炲姞鏂瑰潡瀵垮懡
        blockEntity.age++;
        if (blockEntity.age == Integer.MAX_VALUE) {
            blockEntity.age = 0;
        }

        // 灏濊瘯妫�鏌ョ粨鏋?
        boolean interval = blockEntity.age - blockEntity.lastCheckTime < MIN_CHECK_INTERVAL;
        if (!interval) {
            blockEntity.checkPattern(world, pos, state);
            blockEntity.lastCheckTime = blockEntity.age;
        }

        // 鏇存柊缁戝畾鐨勬煷鐏�鍫�
        blockEntity.updateFirewood(world);

        // 濡傛灉鏈夌儹閲忎笖鐐夊瓙缁撴瀯鏈夋晥锛屽�勭悊鐑樼儰閫昏緫
        if (blockEntity.hasHeat() && blockEntity.isValidStove) {
            blockEntity.processBaking(world);
        } else {
            // 娌℃湁鐑�閲忔椂閲嶇疆鐑樼儰杩涘�?
            blockEntity.resetBakingProgress();
        }

        blockEntity.setChanged();
    }

    private void checkPattern(Level world, BlockPos pos, BlockState state) {
        // 閲嶇疆妫�鏌ョ粨鏋?
        this.currentStoveResult = null;
        this.stoveStructureType = -1;
        this.isValidStove = false;

        if (world != null && !world.isClientSide &&
                (this.cubeBlockPileRef == null || this.cubeBlockPileRef instanceof ClientCubeBlockPileReference)){
            // 濡傛灉鍥犱负鏌愪簺鎰忓�栧�艰嚧寮曠敤涓虹┖鎴栬�呭疄鐜颁簡瀹㈡埛绔�寮曠敤锛屽垯灏濊瘯閲嶆瀯寮曠�?
            CubeBlockPileReference ref = ServerCubeBlockPileReference.fromNbt(world, refNbt);
            if (ref != null) {
                setCubeBlockPileReference(ref);
            }
        }

        // 濡傛灉娌℃湁澶氭柟鍧楀紩鐢�锛屼笉杩涜�屾��鏌?
        if (cubeBlockPileRef == null || cubeBlockPileRef.isDisposed()) {
            return;
        }

        // 鐩存帴鍚屾�ヤ富鏂瑰潡鐨勬暟鎹�
        if (!cubeBlockPileRef.isMasterBlock()){
            if (world != null &&
                    world.getBlockEntity(this.cubeBlockPileRef.getMasterWorldPos()) instanceof HeatResistantSlateBlockPileEntity masterBlockEntity) {
                // 鍚屾�ヤ富鏂瑰潡鐨勬煷鐏�鍫嗕俊鎭?
                this.firewoodPos = new HashSet<>(masterBlockEntity.firewoodPos);
                this.firewoodEntities = new HashSet<>(masterBlockEntity.firewoodEntities);

                // 鍚屾�ヤ富鏂瑰潡鐨勬柟鍧楀浘妗堟暟鎹�
                this.currentStoveResult = masterBlockEntity.currentStoveResult;
                this.resultDirection = masterBlockEntity.resultDirection;
                this.stoveStructureType = masterBlockEntity.stoveStructureType;
                this.isValidStove = masterBlockEntity.isValidStove;

                return;
            }
        }

        // 妫�鏌ュ�氭柟鍧楃粨鏋勬槸鍚︽湁鏁�
        if (!cubeBlockPileRef.checkIntegrity()) {
            return;
        }

        // 妫�鏌ユ槸鍚︾�﹀悎鐐夊瓙缁撴瀯瑕佹�?
        if (!isValidStoveStructure()) {
            return;
        }

        // 鏍规嵁澶氭柟鍧楀昂瀵歌幏鍙栧�瑰簲鐨勭倝瀛愬浘妗堢被鍨�
        int patternType = getStoveStructureType();
        if (patternType == -1) {
            return;
        }

        // 鑾峰彇瀵瑰簲鐨凚lockPattern
        BlockPattern pattern = getStovePattern(patternType);
        if (pattern == null) {
            return;
        }

        // 鍦ㄥ懆鍥存悳绱㈠尮閰嶇殑鐐夊瓙缁撴瀯
        // 浣跨敤涓绘柟鍧椾綅缃�浣滀负鎼滅储璧风�?
        BlockPos searchPos = cubeBlockPileRef.getMasterWorldPos();
        pattern.find(world, searchPos);
        BlockPattern.BlockPatternMatch result = searchAround(world, searchPos, patternType, pattern);

        if (result != null) {
            this.currentStoveResult = result;
            this.stoveStructureType = patternType;
            this.isValidStove = true;

            // 缁撴瀯鍖归厤鎴愬姛
            onStoveStructureValid(world, pos, result, patternType);
        } else {
            // 缁撴瀯涓嶅尮閰?
            onStoveStructureInvalid(world, pos);
        }
    }

    public @Nullable Direction getResultDirection(){
        return this.resultDirection;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }
}