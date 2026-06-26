package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
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
import org.bakingprocess.util.BakingProcessUtils;
import org.twcore.api.block.UpPlaceBlockEntity;

import java.util.List;
import java.util.Optional;

public class CuttingBoardBlockEntity extends UpPlaceBlockEntity {
    private static final VoxelShape CONTENT_SHAPE = Shapes.box(0.125, 0.125, 0.125, 0.875, 0.25, 0.875);

    private final RecipeManager.CachedCheck<RecipeInput, CutRecipe> cutRecipeMatchGetter;
    private final CuttingProcess<CuttingBoardBlockEntity> cuttingProcess;

    public CuttingBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.CUTTING_BOARD.get(), pos, state, 5); // 5个槽位
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

        // 临时设置物品到槽位中进行配方匹配
        ItemStack originalStack = getItem(0);
        setItem(0, stack);

        boolean isValid = cutRecipeMatchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(this), level).isPresent();

        // 恢复原始状态
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
        // 如果切菜流程在进行中，不允许取出物品
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
     * 尝试开始或继续切菜流程
     */
    public InteractionResult tryCutItem(Player player, ItemStack tool, InteractionHand hand, BlockHitResult hit) {
        // 如果没有活跃的流程，尝试开始新的流程
        if (!cuttingProcess.isActive() && cuttingProcess.isValidCuttingTool(tool) && !isEmpty()) {
            Optional<RecipeHolder<CutRecipe>> recipeOpt = cutRecipeMatchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(this), level);

            if (recipeOpt.isPresent()) {
                cuttingProcess.start(level, this);
            }
        }

        // 继续执行切菜流程
        return cuttingProcess.executeStep(
                this, getBlockState(), level, worldPosition, player, hand, hit
        );
    }

    /**
     * 获取切菜流程
     */
    public CuttingProcess<CuttingBoardBlockEntity> getCuttingProcess() {
        return cuttingProcess;
    }

    /**
     * 查找配方（用于NBT恢复）
     */
    public Optional<RecipeHolder<CutRecipe>> findRecipeById(String recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        // 重新匹配当前库存
        return cutRecipeMatchGetter.getRecipeFor(BakingProcessUtils.createRecipeInput(this), level);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);

        inventory.clear();
        ContainerHelper.loadAllItems(nbt, inventory, registryLookup);

        if (nbt.contains("CuttingProcess")) {
            CompoundTag processNbt = nbt.getCompound("CuttingProcess");
            cuttingProcess.readFromNbt(processNbt, registryLookup);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);

        ContainerHelper.saveAllItems(nbt, inventory, registryLookup);

        CompoundTag processNbt = new CompoundTag();
        cuttingProcess.writeToNbt(processNbt, registryLookup);
        nbt.put("CuttingProcess", processNbt);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return this.saveWithoutMetadata(registryLookup);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}