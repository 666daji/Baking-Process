package org.bakingprocess.container;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.bakingprocess.content.ShapedDoughContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

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
        ResourceLocation id = ResourceLocation.tryParse(stack.getOrCreateTag().getString(DOUGH_KEY));
        if (id != null) {
            return TWRegistries.CONTENT.get().getValue(id);
        }

        return null;
    }

    @Override
    public @NotNull ItemStack replaceContent(@NotNull ItemStack stack, @Nullable Content content) {
        validateReplace(stack, content);

        // 清空容器
        if (content == null) {
            if (stack.hasTag()) {
                stack.getOrCreateTag().remove(DOUGH_KEY);
            }

            return stack;
        }

        // 替换内容物
        stack.getOrCreateTag().putString(DOUGH_KEY, Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(content)).toString());
        return stack;
    }
}
