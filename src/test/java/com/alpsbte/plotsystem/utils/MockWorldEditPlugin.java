package com.alpsbte.plotsystem.utils;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.extension.platform.Platform;
import org.junit.jupiter.api.Assertions;
import org.mockbukkit.mockbukkit.ServerMock;

public class MockWorldEditPlugin extends WorldEditPlugin {

    private MockBukkitPlatform mockPlatform;

    @Override
    public void onLoad() {
        ServerMock server = Assertions.assertInstanceOf(ServerMock.class, this.getServer());
        this.mockPlatform = new MockBukkitPlatform(this, server);

        // Let WorldEdit load itself
        // May need to debug if this throws exception
        try { super.onLoad(); }
        catch (RuntimeException ex) {
            this.mockPlatform.getServer().getLogger()
                .warning("Unknown exception during WorldEditPlugin#onLoad(): " + ex.toString());
        }

        WorldEdit worldEdit = WorldEdit.getInstance();

        // Unregister Loaded platform (we need it register as mocked)
        for(Platform platform : worldEdit.getPlatformManager().getPlatforms())
            worldEdit.getPlatformManager().unregister(platform);

        // Re-register platform as our mocked one
        worldEdit.getPlatformManager().register(this.mockPlatform);
    }

    @Override
    public void onEnable() {
        // We can do #onEnable normally,
        // except BStats Metrics construction that will never work (it requires relocations).
        // This happens at the end of #onEnable method calls, so It's presumably fine to ignore.
        try { super.onEnable(); }
        catch (RuntimeException ex) {
            boolean bstats = ex.getMessage().matches(".*(bStats|Metrics).*");
            if(!bstats) this.mockPlatform.getServer().getLogger()
                .warning("Unknown exception during WorldEditPlugin#onEnable(): " + ex.toString());
        }

        // This skips some guard, which doesn't make sense in unit testing
        getLocalConfiguration().unsupportedVersionEditing = true;
    }

    @Override
    public void onDisable() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        if (this.mockPlatform != null) {
            worldEdit.getEventBus().post(new PlatformUnreadyEvent(this.mockPlatform));
            worldEdit.getPlatformManager().unregister(this.mockPlatform);
            this.mockPlatform.unregisterCommands();
        }

        if (getLocalConfiguration() != null) getLocalConfiguration().unload();

        this.getServer().getScheduler().cancelTasks(this);
    }
}