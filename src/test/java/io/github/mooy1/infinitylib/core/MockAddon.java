package io.github.mooy1.infinitylib.core;

import java.io.File;

import javax.annotation.Nullable;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import be.seeseemelk.mockbukkit.MockBukkit;
import id.arvin3108.standalone.SlimefunAddonInstance;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

public class MockAddon extends AbstractAddon {

    private final MockAddonTest test;
    private final SlimefunAddonInstance SFAInstance;

    public MockAddon(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        this(loader, description, dataFolder, file, Environment.LIBRARY_TESTING, null);
    }

    public MockAddon(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file,
                     Environment environment, @Nullable MockAddonTest test) {
        super(loader, description, dataFolder, file, environment);
        this.test = test;
        MockBukkit.load(Slimefun.class);
        this.SFAInstance = new SlimefunAddonInstance("Mooy1", "InfinityLib");
        SFAInstance.createAndStartUpdater(test == MockAddonTest.BAD_GITHUB_PATH ? "[!#$" : "master",
                test == MockAddonTest.MISSING_KEY ? "missing" : "auto-update");
    }

    @Override
    protected void load() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onLoad();
        }
    }

    @Override
    protected void enable() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onEnable();
        }
    }

    @Override
    protected void disable() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onDisable();
        }
    }

    public SlimefunAddonInstance getSFAInstance() {
        return SFAInstance;
    }

}
