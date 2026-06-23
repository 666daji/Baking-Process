package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.block.process.CuttingProcess;
import org.bakingprocess.recipe.CutRecipe;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.ModRecipeTypes;
import org.bakingprocess.registry.ModSounds;
import org.twcore.api.block.UpPlaceBlockEntity;

import java.util.List;
import java.util.Optional;

public class CuttingBoardBlockEntity extends UpPlaceBlockEntity {
    private static final VoxelShape CONTENT_SHAPE = Shapes.box(0.125, 0.125, 0.125, 0.875, 0.25, 0.875);

    private final RecipeManager.CachedCheck<Container, CutRecipe> cutRecipeMatchGetter;
    private final CuttingProcess<CuttingBoardBlockEntity> cuttingProcess;

    public CuttingBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.CUTTING_BOARD.get(), pos, state, 5); // 5涓�妲戒�?
        this.cutRecipeMatchGetter = RecipeManager.createCheck(ModRecipeTypes.CUT.get());
        this.cuttingProcess = new CuttingProcess<>();
    }

    @Override
    public VoxelShape getContentShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return !isEmpty() ? CONTENT_SHAPE : Shapes.empty();
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;

        if (stack.getItem().equals(ModItems.KITCHEN_KNIFE.get())) {
            return true;
        }

        // 涓存椂璁剧疆鐗╁搧鍒版Ы浣嶄腑杩涜�岄厤鏂瑰尮閰�
        ItemStack originalStack = getItem(0);
        setItem(0, stack);

        boolean isValid = cutRecipeMatchGetter.getRecipeFor(this, level).isPresent();

        // 鎭㈠�嶅師濮嬬姸鎬?
        setItem(0, originalStack);

        return isValid;
    }

    @Override
    public Result tryAddItem(ItemStack stack, BlockHitResult hit) {
        if (isEmpty() && isValidItem(stack)) {
            ItemStack placedStack = stack.copy();
            placedStack.setCount(1);
            setItem(0, placedStack);
            markDirtyAndSync();
            return Result.of(placedStack, InteractionResult.SUCCESS);
        }
        return Result.of(InteractionResult.PASS);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, ItemStack placeStack, List<ItemStack> itemStacks) {
        if (placeStack.getItem().equals(ModItems.KITCHEN_KNIFE.get())) {
            world.playSound(
                    null, pos,
                    ModSounds.KITCHEN_KNIFE_BOARD_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F
            );

            if (!player.isCreative()) {
                placeStack.shrink(1);
            }
            return;
        }

        super.onPlace(state, world, pos, player, hand, hit, placeStack, itemStacks);
    }

    @Override
    public Result tryFetchItem(Player player, BlockHitResult hit) {
        // 濡傛灉鍒囪彍娴佺▼鍦ㄨ繘琛屼腑锛屼笉鍏佽�稿彇鍑虹墿鍝�
        if (cuttingProcess.isActive() ) {
            return Result.of(InteractionResult.PASS);
        }

        if (!isEmpty()) {
            ItemStack stack = removeItem(0, 1);
            if (!player.isCreative() && !player.addItem(stack)) {
                player.drop(stack, false);
            }
            markDirtyAndSync();
            return Result.of(List.of(stack.copy()), InteractionResult.SUCCESS);
        }
        return Result.of(InteractionResult.PASS);
    }

    /**
     * 灏濊瘯寮�濮嬫垨缁х画鍒囪彍娴佺▼
     */
    public InteractionResult tryCutItem(Player player, ItemStack tool, InteractionHand hand, BlockHitResult hit) {
        // 濡傛灉娌℃湁娲昏穬鐨勬祦绋嬶紝灏濊瘯寮�濮嬫柊鐨勬祦绋?
        if (!cuttingProcess.isActive() && cuttingProcess.isValidCuttingTool(tool) && !isEmpty()) {
            Optional<CutRecipe> recipeOpt = cutRecipeMatchGetter.getRecipeFor(this, level);

            if (recipeOpt.isPresent()) {
                cuttingProcess.start(level, this);
            }
        }

        // 缁х画鎵ц�屽垏鑿滄祦绋�
        return cuttingProcess.executeStep(
                this, getBlockState(), level, worldPosition, player, hand, hit
        );
    }

    /**
     * 鑾峰彇鍒囪彍娴佺▼
     */
    public CuttingProcess<CuttingBoardBlockEntity> getCuttingProcess() {
        return cuttingProcess;
    }

    /**
     * 鏌ユ壘閰嶆柟锛堢敤浜嶯BT鎭㈠�嶏�?
     */
    public Optional<CutRecipe> findRecipeById(String recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        // 閲嶆柊鍖归厤褰撳墠搴撳瓨
        return cutRecipeMatchGetter.getRecipeFor(this, level);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        inventory.clear();
        ContainerHelper.loadAllItems(nbt, inventory);

        if (nbt.contains("CuttingProcess")) {
            CompoundTag processNbt = nbt.getCompound("CuttingProcess");
            cuttingProcess.readFromNbt(processNbt);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        ContainerHelper.saveAllItems(nbt, inventory);

        CompoundTag processNbt = new CompoundTag();
        cuttingProcess.writeToNbt(processNbt);
        nbt.put("CuttingProcess", processNbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}