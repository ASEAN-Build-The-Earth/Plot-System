package com.alpsbte.plotsystem;

import com.alpsbte.plotsystem.utils.MockWorldEditPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mvplugins.multiverse.core.MultiverseCore;


/**
 * Interface to mocks Minecraft server lifecycle.
 *
 * @see #onServerMock()
 * @see #onServerStarted(ServerMock, WorldEditPlugin)
 * @see #onServerStop()
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface PlotSystemServer {

    /**
     * Invoked before every test triggering server start event with server instance and its initialized plugins.
     *
     * @see #onServerStarted(ServerMock, WorldEditPlugin)
     */
    @BeforeAll
    default void onServerMock() {
        // Initialize a mocks server
        ServerMock server = Assertions.assertDoesNotThrow((ThrowingSupplier<ServerMock>) MockBukkit::mock,
            "MockBukkit should be able to mocks the server using papermc-api"
        );

        Assertions.assertDoesNotThrow(() -> server.getLogger().info("[TEST] MockBukkit Server has started."));

        // Majority of our plot management use WorldEdit
        WorldEditPlugin worldedit = Assertions.assertDoesNotThrow(
            () -> MockBukkit.load(MockWorldEditPlugin.class),
            "Creating WorldEditPlugin mocks instance should not throw fatal exception."
        );

        // We need PlotSystem#getPlugin instance
        Assertions.assertDoesNotThrow(
            () -> MockBukkit.load(MockPlotSystemPlugin.class),
            "Creating PlotSystem mocks instance should not throw fatal exception."
        );

        // Multiverse-Core is native to MockBukkit
        Assertions.assertDoesNotThrow(
            () -> MockBukkit.load(MultiverseCore.class),
            "Creating Multiverse-Core mocks instance should not throw fatal exception."
        );

        this.onServerStarted(server, worldedit);
    }

    /**
     * Invoked after all tests un-mocking the mocks-bukkit server by default.
     *
     * @see AfterAll
     * @see org.mockbukkit.mockbukkit.MockBukkit#unmock
     */
    @AfterAll
    default void onServerStop() {
        MockBukkit.unmock();
    }

    /**
     * Invoked on server started and all its instance is validated.
     *
     * @param server The {@link ServerMock} instance
     * @param worldedit WorldEdit instance
     */
    void onServerStarted(ServerMock server, WorldEditPlugin worldedit);
}
