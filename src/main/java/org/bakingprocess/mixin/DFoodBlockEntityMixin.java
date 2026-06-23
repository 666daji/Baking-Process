package org.bakingprocess.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.bakingprocess.integration.dfood.AssistedBlocks;
import org.bakingprocess.registry.ModBlocks;
import org.dfood.block.FoodBlocks;
import org.dfood.block.entity.ComplexFoodBlockEntity;
import org.dfood.block.entity.ModBlockEntityTypes;
import org.dfood.block.entity.SuspiciousStewBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(ModBlockEntityTypes.class)
public class DFoodBlockEntityMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lorg/dfood/block/entity/ModBlockEntityTypes;registerBlockEntity(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/minecraftforge/registries/RegistryObject;",
            ordinal = 2), index = 1)
    private static <T extends BlockEntity> Supplier<?> registerStewEntities(Supplier<T> supplier) {
        return () -> BlockEntityType.Builder.of(
                SuspiciousStewBlockEntity::new,
                FoodBlocks.SUSPICIOUS_STEW,
                AssistedBlocks.CRIPPLED_SUSPICIOUS_STEW.get()
                ).build(null);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lorg/dfood/block/entity/ModBlockEntityTypes;registerBlockEntity(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/minecraftforge/registries/RegistryObject;",
            ordinal = 0), index = 1)
    private static <T extends BlockEntity> Supplier<?> registerComplexEntities(Supplier<T> supplier) {
        return () -> BlockEntityType.Builder.of(
                ComplexFoodBlockEntity::new,
                ModBlocks.BREAD_SPATULA.get()
        ).build(null);
    }
}
