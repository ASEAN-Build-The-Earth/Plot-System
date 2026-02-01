package com.alpsbte.plotsystem.utils;

import com.sk89q.worldedit.bukkit.BukkitServerInterface;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.internal.Constants;
import org.enginehub.piston.CommandManager;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class MockBukkitPlatform extends BukkitServerInterface {
    public MockBukkitPlatform(WorldEditPlugin plugin, ServerMock server) {
        super(plugin, server);
    }

    /** We do not test in-game commands */
    @Override
    public void registerCommands(CommandManager dispatcher) { }

    /** We do not test in-game commands */
    @Override
    public void unregisterCommands() { }

    /**
     * Minecraft data-version. This better match our testing api version (paper-mc).
     *
     * @return {@value Constants#DATA_VERSION_MC_1_21_8}
     */
    @Override
    public int getDataVersion() {
        return Constants.DATA_VERSION_MC_1_21_8;
    }

    /**
     * Get and assert server instance to mockbukkit compatible instance.
     *
     * @return The server instance, or fail the test if unavailable.
     */
    public ServerMock getServer() {
        return assertInstanceOf(ServerMock.class, this.server);
    }
}