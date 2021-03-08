package io.github.mooy1.infinitylib.items;

import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;

/**
 * Persistent data type for ItemStacks
 * 
 * @author Mooy1
 */
public final class PersistentItemStack implements PersistentDataType<String, ItemStack> {

    private static final PersistentDataType<String, ItemStack> INSTANCE = new PersistentItemStack();

    public static PersistentDataType<String, ItemStack> instance() {
        return INSTANCE;
    }
    
    private PersistentItemStack() {}
    
    @Nonnull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Nonnull
    @Override
    public Class<ItemStack> getComplexType() {
        return ItemStack.class;
    }

    @Nonnull
    @Override
    public String toPrimitive(@Nonnull ItemStack complex, @Nonnull PersistentDataAdapterContext context) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", complex);
        return config.saveToString();
    }
    
    @Nonnull
    @Override
    public ItemStack fromPrimitive(@Nonnull String primitive, @Nonnull PersistentDataAdapterContext context) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(primitive);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return new CustomItem(Material.STONE, "&cERROR");
        }
        ItemStack item = config.getItemStack("item");
        if (item != null) {
            return item;
        } else {
            return new CustomItem(Material.STONE, "&cERROR");
        }
    }

}
