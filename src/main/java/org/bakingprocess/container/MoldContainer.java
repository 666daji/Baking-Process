package org.bakingprocess.container;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.bakingprocess.content.ShapedDoughContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

public class MoldContainer extends ContainerType{
    public static final String DOUGH_KEY = "dough_type";

    public MoldContainer(ContainerSettings settings) {
        super(settings);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.is(getEmptyItem());
    }

    @Override
    public boolean canContain(Content content) {
        return content instanceof ShapedDoughContent shapedDough
                && shapedDough.getBaseMold().asItem().equals(getEmptyItem());
    }

    @Override
    public @Nullable Content extractContent(ItemStack stack) {
        CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);

        if (nbt != null && nbt.contains(DOUGH_KEY)) {
            String soupKey = nbt.copyTag().getString(DOUGH_KEY);
            return TWRegistries.CONTENT.get(ResourceLocation.tryParse(soupKey));
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
                        .update(nbtCompound -> nbtCompound.remove(DOUGH_KEY)));
            }

            return stack;
        }

        // 替换内容物
        if (canContain(content)) {
            stack.set(DataComponents.CUSTOM_DATA, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .update(nbtCompound -> nbtCompound.putString(DOUGH_KEY, TWRegistries.CONTENT.getKey(content).toString())));
        }

        return stack;
    }
}
