package com.alpsbte.plotsystem;

import com.alpsbte.plotsystem.core.system.plot.MockPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.utils.MockBukkitPlatform;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

@DisplayName("WorldEdit Test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlot implements PlotSystemServer {
    protected static final Supplier<BuiltInClipboardFormat> TESTING_FORMAT = () -> BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;
    protected static final int MOCK_BUKKIT_WORLD_HEIGHT = 128;
    protected static final int TEST_PLOT_HEIGHT = 16;
    protected static final int TEST_PLOT_RADIUS = 25;

    protected static ServerMock server;
    protected static WorldEditPlugin worldedit;

    @Override
    public void onServerStarted(ServerMock server, WorldEditPlugin worldedit) {
        TestPlot.server = server;
        TestPlot.worldedit = worldedit;
    }

    @AfterAll @Override
    public void onServerStop() {
        server.getLogger().info("[TEST] ==========[ PLUGIN TEST END ]============");
        PlotSystemServer.super.onServerStop();
    }

    @Test @Order(1)
    @DisplayName("Check WorldEdit")
    public void checkPlatform() {
        PlatformManager platform = WorldEdit.getInstance().getPlatformManager();

        Assertions.assertTrue(platform.isInitialized());

        Assertions.assertEquals(1,
            platform.getPlatforms().size()
        , "Should only have one registered platform.");
        Assertions.assertInstanceOf(
            MockBukkitPlatform.class,
            platform.getPlatforms().getFirst()
        , "Platform should be created from our mocked class.");

        Assertions.assertNotNull(BlockTypes.AIR);
        Assertions.assertNotNull(BlockTypes.DIAMOND_BLOCK);
    }

    @Order(2)
    @RepeatedTest(name = "Plot {currentRepetition}", value = 10, failureThreshold = 1)
    @DisplayName("Test Plot")
    public void testLoadMultiplePlots(RepetitionInfo repetitionInfo) throws IOException {

        long freeMemory = Runtime.getRuntime().freeMemory();
        Assertions.assertTrue(freeMemory > (15_000_000),
            "Running out of memory to continue (Free memory: " + freeMemory + ')'
        );

        WorldMock world = new WorldMock(Material.STONE, TEST_PLOT_HEIGHT);
        world.setName("plot-" + repetitionInfo.getCurrentRepetition());
        server.addWorld(world);
        PlayerMock player = server.addPlayer("Builder" + repetitionInfo.getCurrentRepetition());

        player.setLocation(world.getSpawnLocation());
        Assertions.assertEquals(world.getName(), player.getWorld().getName());

        byte[] initialSchematic = Assertions.assertDoesNotThrow(() -> mockInitialSchematic(player));

        MockPlot plot = new MockPlot(initialSchematic, initialSchematic, "0,0|15,0|15,15|0,15", player);

        BlockVector3 center = plot.getCenter();
        Debugging debug = Assertions.assertInstanceOf(Debugging.class, PlotSystem.getPlugin().getDebugger());
        Assertions.assertEquals(3, debug.getCalls());
        debug.resetCalls();
    }

    public static byte[] mockInitialSchematic(@NotNull PlayerMock player) throws RuntimeException, WorldEditException, IOException {
        try(EditSession edits = worldedit.createEditSession(player)) {
            com.sk89q.worldedit.world.World editWorld = edits.getWorld();
            BlockVector3 center = BlockVector3.ONE;
            int maxY = MOCK_BUKKIT_WORLD_HEIGHT - 1;
            int minY = 0;

            Region region = new CylinderRegion(editWorld, center, new Vector2(TEST_PLOT_RADIUS, TEST_PLOT_RADIUS), minY, maxY);
            Clipboard clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy copy = new ForwardExtentCopy(edits, region, clipboard, region.getMinimumPoint());
            Operations.complete(copy);

            Function<Integer, BlockType> check = y -> clipboard
                .getBlock(BlockVector3.ONE.add(0, y, 0))
                .getBlockType();

            Assertions.assertEquals(BlockTypes.BEDROCK, check.apply(-1));
            for (int i = 0; i < TEST_PLOT_HEIGHT; i++)
                Assertions.assertEquals(BlockTypes.STONE, check.apply(i));
            for (int i = TEST_PLOT_HEIGHT; i < maxY; i++)
                Assertions.assertEquals(BlockTypes.AIR, check.apply(i));

            try(ByteArrayOutputStream initialSchematic = new ByteArrayOutputStream()) {
                try(ClipboardWriter writer = TESTING_FORMAT.get().getWriter(initialSchematic)) {
                    writer.write(clipboard);
                }
                return initialSchematic.toByteArray();
            }
        }
    }
}
