package org.bakingprocess.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.dfood.item.HaveBlock;
import org.dfood.util.DFoodUtils;
import org.bakingprocess.registry.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 表示可以作为武器的锋利厨具
 */
public class ModSharpKitchenwareItem extends SwordItem implements HaveBlock {
    protected final Block block;

    public ModSharpKitchenwareItem(Block block, Properties settings, SpatulaMaterials materials) {
        super(materials, materials.attackDamage, materials.miningSpeed, settings);
        this.block = block;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Item item = context.getItemInHand().getItem();
        // 仅当父类方法失败时才尝试放置方块
        if (super.useOn(context) != InteractionResult.PASS || (player != null && !player.isShiftKeyDown() && DFoodUtils.isModFoodItem(item))){
            return InteractionResult.PASS;
        }
        InteractionResult actionResult = this.place(new BlockPlaceContext(context));
        if (!actionResult.consumesAction() && this.isEdible()) {
            InteractionResult actionResult2 = this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            return actionResult2 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : actionResult2;
        } else {
            return actionResult;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        this.getBlock().appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.block instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        if (this.block instanceof ShulkerBoxBlock) {
            ItemStack itemStack = entity.getItem();
            CompoundTag nbtCompound = HaveBlock.getBlockEntityNbt(itemStack);
            if (nbtCompound != null && nbtCompound.contains("Items", Tag.TAG_LIST)) {
                ListTag nbtList = nbtCompound.getList("Items", Tag.TAG_COMPOUND);
                ItemUtils.onContainerDestroyed(entity, nbtList.stream().map(CompoundTag.class::cast).map(ItemStack::of));
            }
        }
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.getBlock().requiredFeatures();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.getItem() == ModItems.KITCHEN_KNIFE.get()) {
            stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            return true;
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    public enum SpatulaMaterials implements Tier {
        KITCHEN_KNIFE(2, 1400, 1.0F, 1, 5, () -> Ingredient.of(Items.IRON_INGOT)),
        BREAD_SPATULA(2, 100, -3.5F, 4, 14, () -> Ingredient.of(Items.IRON_INGOT));

        private final int miningLevel;
        private final int itemDurability;
        private final float miningSpeed;
        private final int attackDamage;
        private final int enchantAbility;
        private final Supplier<Ingredient> repairIngredient;

        SpatulaMaterials(int miningLevel, int itemDurability, float miningSpeed, int attackDamage, int enchantAbility, Supplier<Ingredient> repairIngredient) {
            this.miningLevel = miningLevel;
            this.itemDurability = itemDurability;
            this.miningSpeed = miningSpeed;
            this.attackDamage = attackDamage;
            this.enchantAbility = enchantAbility;
            this.repairIngredient = repairIngredient;
        }

        @Override
        public int getUses() {
            return this.itemDurability;
        }

        @Override
        public float getSpeed() {
            return this.miningSpeed;
        }

        @Override
        public float getAttackDamageBonus() {
            return this.attackDamage;
        }

        @Override
        public int getLevel() {
            return this.miningLevel;
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchantAbility;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredient.get();
        }
    }
}
