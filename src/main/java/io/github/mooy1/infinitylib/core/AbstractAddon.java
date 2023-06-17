package io.github.mooy1.infinitylib.core;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import io.github.mooy1.infinitylib.InfinityLib;
import io.github.mooy1.infinitylib.commands.AddonCommand;

/**
 * Extend this in your main plugin class to access a bunch of utilities
 * <br><br> Standalone version of InfinityLib
 *
 * @author Mooy1
 * @author ARVIN3108
 */
@ParametersAreNonnullByDefault
public abstract class AbstractAddon extends JavaPlugin {

    private static AbstractAddon instance;

    private final Environment environment;

    private AddonCommand command;
    private AddonConfig config;
    private boolean brokenConfig;
    private boolean disabling;
    private boolean enabling;
    private boolean loading;

    /**
     * Live Addon Constructor
     */
    public AbstractAddon() {
        this.environment = Environment.LIVE;
        validate();
    }

    /**
     * Addon Testing Constructor
     */
    public AbstractAddon(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        this(loader, description, dataFolder, file, Environment.TESTING);
    }

    /**
     * Library Testing Constructor
     */
    AbstractAddon(JavaPluginLoader loader, PluginDescriptionFile description,
                  File dataFolder, File file, Environment environment) {
        super(loader, description, dataFolder, file);
        this.environment = environment;
        validate();
    }

    private void validate() {
        if (environment == Environment.LIVE) {
            if (InfinityLib.PACKAGE.contains("mooy1.infinitylib")) {
                throw new IllegalStateException("You must relocate InfinityLib to your own package!");
            }
            String addonPackage = getClass().getPackage().getName();
            if (!addonPackage.contains(InfinityLib.ADDON_PACKAGE)) {
                throw new IllegalStateException("Shade and relocate your own InfinityLib!");
            }
        }
        if (instance != null) {
            throw new IllegalStateException("Addon " + instance.getName() + " is already using this InfinityLib, Shade an relocate your own!");
        }
    }

    @Override
    public final void onLoad() {
        if (loading) {
            throw new IllegalStateException(getName() + " is already loading! Do not call super.onLoad()!");
        }

        loading = true;

        // Load
        try {
            load();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            loading = false;
        }
    }

    @Override
    public final void onEnable() {
        if (enabling) {
            throw new IllegalStateException(getName() + " is already enabling! Do not call super.onEnable()!");
        }

        enabling = true;

        // Set static instance
        instance = this;

        // This is used to mark when the config is broken, so we should always auto update
        brokenConfig = false;

        // Create Config
        try {
            config = new AddonConfig("config.yml");
        }
        catch (RuntimeException e) {
            brokenConfig = true;
            e.printStackTrace();
        }

        // Get plugin command
        PluginCommand pluginCommand = getCommand(getName());
        if (pluginCommand != null) {
            command = new AddonCommand(pluginCommand);
        }

        // Call addon enable
        try {
            enable();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            enabling = false;
        }
    }

    @Override
    public final void onDisable() {
        if (disabling) {
            throw new IllegalStateException(getName() + " is already disabling! Do not call super.onDisable()!");
        }

        disabling = true;

        try {
            disable();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            disabling = false;
            instance = null;
            command = null;
            config = null;
        }
    }

    /**
     * Throws an exception if in a test environment, otherwise just logs the stacktrace so that the plugin functions
     */
    public void handle(RuntimeException e) {
        switch (this.environment) {
            case TESTING:
                throw e;
            case LIVE:
                e.printStackTrace();
        }
    }

    /**
     * Called when the plugin is loaded
     */
    protected void load() {

    }

    /**
     * Called when the plugin is enabled
     */
    protected abstract void enable();

    /**
     * Called when the plugin is disabled
     */
    protected abstract void disable();

    /**
     * Gets the command of the same name as this addon
     */
    @Nonnull
    protected final AddonCommand getAddonCommand() {
        return Objects.requireNonNull(instance().command, "Command '" + getName() + "' missing from plugin.yml!");
    }

    @Nonnull
    @Override
    public final AddonConfig getConfig() {
        return instance().config;
    }

    @Override
    public final void reloadConfig() {
        instance().config.reload();
    }

    @Override
    public final void saveConfig() {
        instance().config.save();
    }

    @Override
    public final void saveDefaultConfig() {
        // Do nothing, it's covered in onEnable()
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends AbstractAddon> T instance() {
        return (T) Objects.requireNonNull(instance, "Addon is not enabled!");
    }

    @Nonnull
    public static AddonConfig config() {
        return instance().getConfig();
    }

    @SuppressWarnings("unused")
    public static void log(Level level, String... messages) {
        Logger logger = instance().getLogger();
        for (String msg : messages) {
            logger.log(level, msg);
        }
    }

    /**
     * Returns the current running environment
     */
    @Nonnull
    public static Environment environment() {
        return instance().environment;
    }

    /**
     * Creates a NameSpacedKey from the given string
     */
    @Nonnull
    public static NamespacedKey createKey(String s) {
        return new NamespacedKey(instance(), s);
    }

    /**
     * Returns whether the configuration is corrupted or not
     */
    public boolean isBrokenConfig() {
        return brokenConfig;
    }

    /**
     * Sets the configuration is corrupted or not
     */
    public void setBrokenConfig(boolean brokenConfig) {
        this.brokenConfig = brokenConfig;
    }

}
