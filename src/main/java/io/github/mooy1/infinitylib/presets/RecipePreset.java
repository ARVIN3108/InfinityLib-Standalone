package io.github.mooy1.infinitylib.presets;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class RecipePreset {

    @Nonnull
    public static ItemStack[] Compress(@Nonnull ItemStack item) {
        return new ItemStack[]{
                item, item, item, item, item, item, item, item, item,
        };
    }

    @Nonnull
    public static ItemStack[] MiddleItem(@Nonnull ItemStack item) {
        return new ItemStack[] {
                null, null, null, null , item, null, null, null, null
        };
    }
    
}
