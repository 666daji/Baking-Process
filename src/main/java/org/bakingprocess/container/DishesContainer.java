package org.bakingprocess.container;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.bakingprocess.registry.ModContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

public class DishesContainer extends ContainerType {
    public static final String DISHES_KEY = "dishes_type";

    public DishesContainer(ContainerSettings settings) {
        super(settings);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.is(getEmptyItem());
    }

    @Override
    public boolean canContain(Content content) {
        return content.isIn(ModContents.DISHES);
    }

    @Override
    public @Nullable Content extractContent(ItemStack stack) {
        CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);

        if (nbt != null && nbt.contains(DISHES_KEY)) {
            String soupKey = nbt.copyTag().getString(DISHES_KEY);
            return TWRegistries.CONTENT.get().getValue(ResourceLocation.tryParse(soupKey));
        }

        return null;
    }

    @Override
    public @NotNull ItemStack replaceContent(@NotNull ItemStack stack, @Nullable Content content) {
        validateReplace(stack, content);

        // 清空容器
        if (content == null) {
            if (stack.has(DataComponents.CUSTOM_DATA)) {
                stack.set(DataComponents.CUSTOM_DATA, stack.get(DataComponents.CUSTOM_DATA)
                        .update(nbtCompound -> nbtCompound.remove(DISHES_KEY)));
            }

            return stack;
        }

        // 替换内容物
        if (canContain(content)) {
            stack.set(DataComponents.CUSTOM_DATA, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .update(nbtCompound -> nbtCompound.putString(DISHES_KEY, TWRegistries.CONTENT.get().getKey(content).toString())));
        }

        return stack;
    }
}
