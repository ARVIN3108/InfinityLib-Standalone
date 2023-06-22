package id.arvin3108.standalone;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;

import org.bukkit.plugin.java.JavaPlugin;

import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.core.AbstractAddon;
import io.github.mooy1.infinitylib.core.Environment;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;

public class SlimefunAddonInstance implements SlimefunAddon {

    private static SlimefunAddonInstance instance;

    /**
     * Returns the total number of Slimefun ticks that have occurred
     */
    @Getter
    private static int slimefunTickCount = 0;
    private final String githubUserName;
    private final String githubRepo;
    private final String bugTrackerURL;

    private boolean autoUpdatesEnabled;

    public SlimefunAddonInstance(String githubUserName, String githubRepo) {
        if (instance != null) {
            throw new IllegalStateException("SlimefunAddonInstance is already initialized!");
        }

        this.githubUserName = githubUserName;
        this.githubRepo = githubRepo;

        if (AbstractAddon.environment() == Environment.LIVE) {
            if (!githubUserName.matches("[\\w-]+")) {
                throw new IllegalArgumentException("Invalid githubUserName");
            }
            if (!githubRepo.matches("[\\w-]+")) {
                throw new IllegalArgumentException("Invalid githubRepo");
            }
        }

        this.bugTrackerURL = "https://github.com/" + githubUserName + "/" + githubRepo + "/issues";

        // Create total tick count
        Scheduler.repeat(Slimefun.getTickerTask().getTickRate(), () -> slimefunTickCount++);

        instance = this;
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return AbstractAddon.instance();
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return bugTrackerURL;
    }

    public void setSlimefunTickCount(int tick) {
        slimefunTickCount = tick;
    }

    public void createAndStartUpdater(String autoUpdateBranch, String autoUpdateKey) {
        boolean official = AbstractAddon.instance().getDescription().getVersion().matches("DEV - \\d+ \\(git \\w+\\)");
        boolean brokenConfig = AbstractAddon.instance().isBrokenConfig();

        // Validate autoUpdateBranch
        if (!autoUpdateBranch.matches("[\\w-]+")) {
            throw new IllegalArgumentException("Invalid autoUpdateBranch");
        }

        // Validate autoUpdateKey
        if (autoUpdateKey == null) {
            AbstractAddon.instance().setBrokenConfig(true);
            AbstractAddon.instance().handle(new IllegalStateException("Null auto update key"));
        }
        else if (autoUpdateKey.isEmpty()) {
            AbstractAddon.instance().setBrokenConfig(true);
            AbstractAddon.instance().handle(new IllegalStateException("Empty auto update key!"));
        }
        else if (!brokenConfig && !AbstractAddon.instance().getConfig().getDefaults().contains(autoUpdateKey, true)) {
            AbstractAddon.instance().setBrokenConfig(true);
            AbstractAddon.instance().handle(new IllegalStateException("Auto update key missing from the default config!"));
        }

        File file = null;

        try {
            Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            getFileMethod.setAccessible(true);
            file = (File) getFileMethod.invoke(AbstractAddon.instance());
        }
        catch (NoSuchMethodException e) {
            AbstractAddon.instance().handle(new IllegalStateException("Can't find getFile method in plugin main class"));
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            AbstractAddon.instance().handle(new IllegalStateException("Unable to access getFile method of plugin main class"));
        }
        finally {
            GitHubBuildsUpdater updater = official ? new GitHubBuildsUpdater(AbstractAddon.instance(), file,
                    githubUserName + "/" + githubRepo + "/" + autoUpdateBranch) : null;

            // Auto update if enabled
            if (updater != null) {
                if (brokenConfig) {
                    updater.start();
                }
                else if (AbstractAddon.config().getBoolean(autoUpdateKey)) {
                    autoUpdatesEnabled = true;
                    updater.start();
                }
            }
        }
    }

    /**
     * Returns whether auto updates are enabled, for use in metrics
     */
    public final boolean autoUpdatesEnabled() {
        return autoUpdatesEnabled;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends SlimefunAddonInstance> T getSFAInstance() {
        return (T) Objects.requireNonNull(instance, "SlimefunAddonInstance is not initialized!");
    }

}